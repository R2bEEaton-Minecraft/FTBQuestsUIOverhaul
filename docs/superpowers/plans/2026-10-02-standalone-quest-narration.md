# Standalone Quest Narration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the ENDI-reflection narration bridge with a native, dependency-free typewriter reveal + blip sound owned entirely by this addon.

**Architecture:** Two new stateless/local classes (`QuestNarrationPacing`, `QuestNarrationSoundPlayer`) replace `EasyNpcDialogueImmersionBridge`/`QuestNarrationBridge`. `OverhaulQuestScreen` constructs one `QuestNarrationPresenter` unconditionally — no `ModList` check, no reflection. The addon registers and ships its own `dialogue_blip` `SoundEvent` and `.ogg` asset. Optional-mod declarations and dev-runtime jars for Easy NPC/ENDI are removed; the Forge baseline reverts to 47.2.19.

**Tech Stack:** Java 17, Forge 47.2.19 / MC 1.20.1, Gradle 8.1.1 + ForgeGradle, JUnit 5 (Jupiter) for new pure-logic unit tests.

**Spec:** `docs/superpowers/specs/2026-10-02-standalone-quest-narration-design.md`

## Global Constraints

- Reveal at a fixed 50 characters per second.
- Punctuation pauses: comma 300 ms, period 600 ms, exclamation 500 ms, question mark 600 ms.
- Preserve existing UTF-16 reveal accounting and code-point-safe styled prefix rendering (do not re-wrap text, do not split surrogate pairs).
- Play a local `dialogue_blip.ogg` for newly revealed letters/digits only; cap a lagged frame to the newest 8 candidate blips while revealing all visual characters immediately.
- No new user-facing setting, no network/server behavior.
- `OverhaulQuestScreen` must not check `ModList` or load any optional-mod class for narration.
- Remove the optional `easynpcdialogueimmersion` mods.toml declaration and the Easy NPC/ENDI dev-runtime `build.gradle` entries.
- Restore `forge_version` to `47.2.19` in `gradle.properties`.
- The existing first-open ledger (`QuestDataController.shouldAnimateQuestNarration`/`markQuestNarrationSeen`), description-only rendering, skip input behavior, and fingerprinting stay unchanged.

---

### Task 1: Native pacing logic with unit tests

**Files:**
- Create: `src/main/java/dev/ftb/mods/ftbquestsvisualoverhaul/client/narration/QuestNarrationPacing.java`
- Test: `src/test/java/dev/ftb/mods/ftbquestsvisualoverhaul/client/narration/QuestNarrationPacingTest.java`
- Modify: `build.gradle` (add JUnit 5 test dependency + `useJUnitPlatform()`)

**Interfaces:**
- Produces: `QuestNarrationPacing.revealedCharacterCount(String text, long elapsedMillis) -> int` (char-index based, matches existing ENDI-bridge signature so Task 3 is a drop-in swap).
- Produces: `QuestNarrationPacing.codePointWithinPrefix(int usedUtf16CodeUnits, int codePoint, int visibleUtf16CodeUnits) -> boolean` (pure surrogate-pair-safe boundary check, consumed by `OverhaulQuestScreen.takePrefix` in Task 3).

This class has zero Minecraft imports so it runs in the plain `test` source set without the Minecraft/Forge classpath.

- [ ] **Step 1: Write the failing tests**

```java
package dev.ftb.mods.ftbquestsvisualoverhaul.client.narration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestNarrationPacingTest {

    @Test
    void revealsNothingAtZeroElapsed() {
        assertEquals(0, QuestNarrationPacing.revealedCharacterCount("Hello", 0L));
    }

    @Test
    void revealsAtFiftyCharactersPerSecond() {
        // 20ms per character at 50 CPS; 4 chars need [60ms, 80ms) of budget.
        assertEquals(3, QuestNarrationPacing.revealedCharacterCount("Hello", 60L));
        assertEquals(4, QuestNarrationPacing.revealedCharacterCount("Hello", 80L));
    }

    @Test
    void revealsFullTextOnceElapsedCoversIt() {
        assertEquals(5, QuestNarrationPacing.revealedCharacterCount("Hello", 100L));
        assertEquals(5, QuestNarrationPacing.revealedCharacterCount("Hello", 10_000L));
    }

    @Test
    void appliesCommaPauseAfterComma() {
        // 'a' and ',' each cost the 20ms base delay; 'b' additionally waits out the
        // 300ms comma pause on top of its own 20ms base delay: 20+20+(20+300)=360ms.
        assertEquals(2, QuestNarrationPacing.revealedCharacterCount("a,b", 359L));
        assertEquals(3, QuestNarrationPacing.revealedCharacterCount("a,b", 360L));
    }

    @Test
    void appliesPeriodPauseAfterPeriod() {
        assertEquals(2, QuestNarrationPacing.revealedCharacterCount("a.b", 659L));
        assertEquals(3, QuestNarrationPacing.revealedCharacterCount("a.b", 660L));
    }

    @Test
    void appliesExclamationPauseAfterExclamation() {
        assertEquals(2, QuestNarrationPacing.revealedCharacterCount("a!b", 559L));
        assertEquals(3, QuestNarrationPacing.revealedCharacterCount("a!b", 560L));
    }

    @Test
    void appliesQuestionPauseAfterQuestionMark() {
        assertEquals(2, QuestNarrationPacing.revealedCharacterCount("a?b", 659L));
        assertEquals(3, QuestNarrationPacing.revealedCharacterCount("a?b", 660L));
    }

    @Test
    void handlesNullAndEmptyText() {
        assertEquals(0, QuestNarrationPacing.revealedCharacterCount(null, 1000L));
        assertEquals(0, QuestNarrationPacing.revealedCharacterCount("", 1000L));
    }

    @Test
    void codePointWithinPrefixAllowsAsciiUpToBudget() {
        assertTrue(QuestNarrationPacing.codePointWithinPrefix(0, 'a', 1));
        assertFalse(QuestNarrationPacing.codePointWithinPrefix(1, 'b', 1));
    }

    @Test
    void codePointWithinPrefixRefusesToSplitASurrogatePair() {
        int emoji = 0x1F600; // U+1F600, charCount == 2 in UTF-16.
        assertFalse(QuestNarrationPacing.codePointWithinPrefix(0, emoji, 1));
        assertTrue(QuestNarrationPacing.codePointWithinPrefix(0, emoji, 2));
    }
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `./gradlew test --tests "*.QuestNarrationPacingTest"`
Expected: FAIL (compilation error — `QuestNarrationPacing` does not exist yet).

- [ ] **Step 3: Add JUnit 5 to the build**

In `build.gradle`, add to the `dependencies { ... }` block (after the existing `runtimeOnly fg.deobf("curse.maven:natures-compass-252848:4712189")` line, before the `run/mods-disabled` conditional blocks):

```groovy
    testImplementation platform('org.junit:junit-bom:5.10.2')
    testImplementation 'org.junit.jupiter:junit-jupiter'
```

Add a top-level `test` task block (after the `jar { ... }` block at the end of the file):

```groovy
test {
    useJUnitPlatform()
}
```

- [ ] **Step 4: Write the minimal implementation**

```java
package dev.ftb.mods.ftbquestsvisualoverhaul.client.narration;

/**
 * Stateless re-derivation of the typewriter reveal count and the surrogate-pair-safe
 * prefix boundary. Carries no Minecraft dependency so it is directly unit-testable.
 */
public final class QuestNarrationPacing {
    private static final float CHARACTERS_PER_SECOND = 50F;
    private static final long BASE_DELAY_MILLIS = Math.round(1000D / CHARACTERS_PER_SECOND);
    private static final long COMMA_PAUSE_MILLIS = 300L;
    private static final long PERIOD_PAUSE_MILLIS = 600L;
    private static final long EXCLAMATION_PAUSE_MILLIS = 500L;
    private static final long QUESTION_PAUSE_MILLIS = 600L;

    private QuestNarrationPacing() {
    }

    public static int revealedCharacterCount(String text, long elapsedMillis) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        long remaining = Math.max(0L, elapsedMillis);
        int revealed = 0;
        while (revealed < text.length()) {
            long delay = BASE_DELAY_MILLIS + (revealed == 0 ? 0L : pauseAfter(text.charAt(revealed - 1)));
            if (remaining < delay) {
                break;
            }
            remaining -= delay;
            revealed++;
        }
        return revealed;
    }

    /** True if appending {@code codePoint} would not exceed the visible UTF-16 budget mid-surrogate-pair. */
    public static boolean codePointWithinPrefix(int usedUtf16CodeUnits, int codePoint, int visibleUtf16CodeUnits) {
        return usedUtf16CodeUnits + Character.charCount(codePoint) <= visibleUtf16CodeUnits;
    }

    private static long pauseAfter(char character) {
        return switch (character) {
            case ',' -> COMMA_PAUSE_MILLIS;
            case '.' -> PERIOD_PAUSE_MILLIS;
            case '!' -> EXCLAMATION_PAUSE_MILLIS;
            case '?' -> QUESTION_PAUSE_MILLIS;
            default -> 0L;
        };
    }
}
```

- [ ] **Step 5: Run the tests to verify they pass**

Run: `./gradlew test --tests "*.QuestNarrationPacingTest"`
Expected: PASS (10 tests).

- [ ] **Step 6: Commit**

```bash
git add build.gradle src/main/java/dev/ftb/mods/ftbquestsvisualoverhaul/client/narration/QuestNarrationPacing.java src/test/java/dev/ftb/mods/ftbquestsvisualoverhaul/client/narration/QuestNarrationPacingTest.java
git commit -m "Add native stateless quest narration pacing with unit tests"
```

---

### Task 2: Native sound asset, registration, and playback

**Files:**
- Create: `src/main/resources/assets/ftbquestsvisualoverhaul/sounds/dialogue_blip.ogg` (binary copy)
- Create: `src/main/resources/assets/ftbquestsvisualoverhaul/sounds.json`
- Create: `src/main/java/dev/ftb/mods/ftbquestsvisualoverhaul/client/narration/ModSounds.java`
- Create: `src/main/java/dev/ftb/mods/ftbquestsvisualoverhaul/client/narration/QuestNarrationSoundPlayer.java`
- Modify: `src/main/java/dev/ftb/mods/ftbquestsvisualoverhaul/FTBQuestsVisualOverhaul.java`

**Interfaces:**
- Consumes: nothing from Task 1.
- Produces: `ModSounds.DIALOGUE_BLIP` (a `RegistryObject<SoundEvent>`), `ModSounds.register(IEventBus modEventBus)`.
- Produces: `new QuestNarrationSoundPlayer().playNewlyRevealed(String characters)` — instance method, caps to the newest 8 characters, plays only letters/digits, applies a minimal overlap guard. Consumed by `QuestNarrationPresenter` in Task 3.

This task has no unit test: it is thin glue over `Minecraft.getInstance()` and registry wiring, which only a running client can exercise. Verification is the manual client check in Task 4.

- [ ] **Step 1: Copy the sound asset**

```bash
mkdir -p "src/main/resources/assets/ftbquestsvisualoverhaul/sounds"
cp "F:/EasyNPCDialogueImmersion/src/main/resources/assets/easynpcdialogueimmersion/sounds/dialogue_blip.ogg" \
   "src/main/resources/assets/ftbquestsvisualoverhaul/sounds/dialogue_blip.ogg"
```

- [ ] **Step 2: Declare the sound**

Create `src/main/resources/assets/ftbquestsvisualoverhaul/sounds.json`:

```json
{
  "dialogue_blip": {
    "sounds": [
      "ftbquestsvisualoverhaul:dialogue_blip"
    ]
  }
}
```

- [ ] **Step 3: Register the SoundEvent**

```java
package dev.ftb.mods.ftbquestsvisualoverhaul.client.narration;

import dev.ftb.mods.ftbquestsvisualoverhaul.FTBQuestsVisualOverhaul;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, FTBQuestsVisualOverhaul.MOD_ID);

    public static final RegistryObject<SoundEvent> DIALOGUE_BLIP = SOUND_EVENTS.register("dialogue_blip",
            () -> SoundEvent.createVariableRangeEvent(
                    new ResourceLocation(FTBQuestsVisualOverhaul.MOD_ID, "dialogue_blip")));

    private ModSounds() {
    }

    public static void register(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }
}
```

- [ ] **Step 4: Wire registration into mod startup**

In `FTBQuestsVisualOverhaul.java`, add an import and call `ModSounds.register(...)` in the constructor:

```java
import dev.ftb.mods.ftbquestsvisualoverhaul.client.narration.ModSounds;
```

```java
    public FTBQuestsVisualOverhaul() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ModClientConfig.SPEC);
        MinecraftForge.EVENT_BUS.register(this);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::addPackFinders);
        ModSounds.register(FMLJavaModLoadingContext.get().getModEventBus());

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> OverhaulClient::init);
    }
```

- [ ] **Step 5: Implement local playback with a minimal overlap guard**

```java
package dev.ftb.mods.ftbquestsvisualoverhaul.client.narration;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * Plays the addon's own dialogue_blip for newly revealed letters/digits.
 * Guards against overlap so a lag spike cannot sound like a burst of clicks.
 */
public final class QuestNarrationSoundPlayer {
    private static final float PITCH = 1.0F;
    private static final float VOLUME = 0.70F;
    private static final long TAIL_THRESHOLD_MILLIS = 40L;
    private static final int MAX_QUEUED_BLIPS = 8;

    private SoundInstance activeBlip;
    private long activeBlipStartedAtMillis;

    public void playNewlyRevealed(String characters) {
        if (characters == null || characters.isEmpty()) {
            return;
        }
        int start = Math.max(0, characters.length() - MAX_QUEUED_BLIPS);
        for (int i = start; i < characters.length(); i++) {
            playFor(characters.charAt(i));
        }
    }

    private void playFor(char character) {
        if (!Character.isLetterOrDigit(character)) {
            return;
        }
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        boolean blipActive = activeBlip != null && soundManager.isActive(activeBlip);
        long activeDurationMillis = Math.max(0L, Util.getMillis() - activeBlipStartedAtMillis);
        if (blipActive && activeDurationMillis < TAIL_THRESHOLD_MILLIS) {
            return;
        }
        SoundInstance blip = new ResolvedBlipSoundInstance(PITCH, VOLUME);
        soundManager.play(blip);
        activeBlip = blip;
        activeBlipStartedAtMillis = Util.getMillis();
    }

    /** Retains the same weighted sound variant through playback, matching UI-sound behavior. */
    private static final class ResolvedBlipSoundInstance extends SimpleSoundInstance {
        private WeighedSoundEvents resolvedEvents;

        ResolvedBlipSoundInstance(float pitch, float volume) {
            super(ModSounds.DIALOGUE_BLIP.get().getLocation(), SoundSource.MASTER, volume, pitch,
                    SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.NONE, 0, 0, 0, true);
        }

        @Override
        public WeighedSoundEvents resolve(SoundManager manager) {
            if (resolvedEvents == null) {
                resolvedEvents = super.resolve(manager);
            }
            return resolvedEvents;
        }
    }
}
```

- [ ] **Step 6: Compile to confirm no errors**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 7: Commit**

```bash
git add src/main/resources/assets/ftbquestsvisualoverhaul/sounds src/main/resources/assets/ftbquestsvisualoverhaul/sounds.json src/main/java/dev/ftb/mods/ftbquestsvisualoverhaul/client/narration/ModSounds.java src/main/java/dev/ftb/mods/ftbquestsvisualoverhaul/client/narration/QuestNarrationSoundPlayer.java src/main/java/dev/ftb/mods/ftbquestsvisualoverhaul/FTBQuestsVisualOverhaul.java
git commit -m "Ship and register a native dialogue_blip sound for quest narration"
```

---

### Task 3: Swap OverhaulQuestScreen onto the native presenter and delete the ENDI bridge

**Files:**
- Create: `src/main/java/dev/ftb/mods/ftbquestsvisualoverhaul/client/narration/QuestNarrationPresenter.java`
- Modify: `src/main/java/dev/ftb/mods/ftbquestsvisualoverhaul/client/screen/OverhaulQuestScreen.java`
- Delete: `src/main/java/dev/ftb/mods/ftbquestsvisualoverhaul/client/integration/EasyNpcDialogueImmersionBridge.java`
- Delete: `src/main/java/dev/ftb/mods/ftbquestsvisualoverhaul/client/integration/QuestNarrationBridge.java`

**Interfaces:**
- Consumes: `QuestNarrationPacing.revealedCharacterCount(String, long)` and `QuestNarrationPacing.codePointWithinPrefix(int, int, int)` (Task 1); `new QuestNarrationSoundPlayer().playNewlyRevealed(String)` (Task 2).
- Produces: `QuestNarrationPresenter.revealedCharacterCount(String text, long elapsedMillis) -> int`, `QuestNarrationPresenter.playNewlyRevealed(String characters) -> void` — the single object `OverhaulQuestScreen` owns for narration.

- [ ] **Step 1: Add the presenter**

```java
package dev.ftb.mods.ftbquestsvisualoverhaul.client.narration;

/** The one native narration object OverhaulQuestScreen owns; no optional-mod awareness. */
public final class QuestNarrationPresenter {
    private final QuestNarrationSoundPlayer soundPlayer = new QuestNarrationSoundPlayer();

    public int revealedCharacterCount(String text, long elapsedMillis) {
        return QuestNarrationPacing.revealedCharacterCount(text, elapsedMillis);
    }

    public void playNewlyRevealed(String characters) {
        soundPlayer.playNewlyRevealed(characters);
    }
}
```

- [ ] **Step 2: Swap the screen's imports and field**

In `OverhaulQuestScreen.java`, replace (around line 29-31):

```java
import dev.ftb.mods.ftbquestsvisualoverhaul.client.integration.RecipeViewer;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.integration.EasyNpcDialogueImmersionBridge;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.integration.QuestNarrationBridge;
```

with:

```java
import dev.ftb.mods.ftbquestsvisualoverhaul.client.integration.RecipeViewer;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.narration.QuestNarrationPacing;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.narration.QuestNarrationPresenter;
```

Replace the field (around line 264):

```java
    private final QuestNarrationBridge narrationBridge = EasyNpcDialogueImmersionBridge.create();
```

with:

```java
    private final QuestNarrationPresenter narrationPresenter = new QuestNarrationPresenter();
```

- [ ] **Step 3: Update `ensureNarration` eligibility check**

Find (around line 2373):

```java
        narrationActive = narrationBridge != QuestNarrationBridge.NONE
                && !text.isEmpty()
                && QuestDataController.shouldAnimateQuestNarration(quest.id(), fingerprint, eligible);
```

Replace with:

```java
        narrationActive = !text.isEmpty()
                && QuestDataController.shouldAnimateQuestNarration(quest.id(), fingerprint, eligible);
```

- [ ] **Step 4: Update `visibleNarrationCharacters`**

Find (around line 2383-2387):

```java
        int revealed = Mth.clamp(narrationBridge.revealedCharacterCount(narrationText,
                Math.max(0L, Util.getMillis() - narrationStartedAtMs)), 0, narrationText.length());
        if (revealed > previouslyRevealed) {
            narrationBridge.playNewlyRevealed(narrationText.substring(previouslyRevealed, revealed));
            previouslyRevealed = revealed;
        }
```

Replace with:

```java
        int revealed = Mth.clamp(narrationPresenter.revealedCharacterCount(narrationText,
                Math.max(0L, Util.getMillis() - narrationStartedAtMs)), 0, narrationText.length());
        if (revealed > previouslyRevealed) {
            narrationPresenter.playNewlyRevealed(narrationText.substring(previouslyRevealed, revealed));
            previouslyRevealed = revealed;
        }
```

- [ ] **Step 5: Route `takePrefix` through the pure surrogate-pair-safe helper**

Find the existing `takePrefix` method:

```java
    /** Retains the original glyph styles while refusing to split a surrogate pair. */
    private FormattedCharSequence takePrefix(FormattedCharSequence source, int visibleUtf16Characters) {
        return sink -> {
            final int[] used = {0};
            return source.accept((index, style, codePoint) -> {
                int width = Character.charCount(codePoint);
                if (used[0] + width > visibleUtf16Characters) return false;
                used[0] += width;
                return sink.accept(index, style, codePoint);
            });
        };
    }
```

Replace with:

```java
    /** Retains the original glyph styles while refusing to split a surrogate pair. */
    private FormattedCharSequence takePrefix(FormattedCharSequence source, int visibleUtf16Characters) {
        return sink -> {
            final int[] used = {0};
            return source.accept((index, style, codePoint) -> {
                if (!QuestNarrationPacing.codePointWithinPrefix(used[0], codePoint, visibleUtf16Characters)) return false;
                used[0] += Character.charCount(codePoint);
                return sink.accept(index, style, codePoint);
            });
        };
    }
```

- [ ] **Step 6: Delete the ENDI bridge classes**

```bash
git rm src/main/java/dev/ftb/mods/ftbquestsvisualoverhaul/client/integration/EasyNpcDialogueImmersionBridge.java
git rm src/main/java/dev/ftb/mods/ftbquestsvisualoverhaul/client/integration/QuestNarrationBridge.java
```

- [ ] **Step 7: Compile and run the full test suite**

Run: `./gradlew compileJava test`
Expected: BUILD SUCCESSFUL, all `QuestNarrationPacingTest` cases still pass (the production code they exercise did not change).

- [ ] **Step 8: Commit**

```bash
git add src/main/java/dev/ftb/mods/ftbquestsvisualoverhaul/client/narration/QuestNarrationPresenter.java src/main/java/dev/ftb/mods/ftbquestsvisualoverhaul/client/screen/OverhaulQuestScreen.java
git commit -m "Move OverhaulQuestScreen narration onto the native presenter"
```

---

### Task 4: Remove the ENDI dependency/dev-runtime declarations and restore the Forge baseline

**Files:**
- Modify: `src/main/resources/META-INF/mods.toml`
- Modify: `build.gradle`
- Modify: `gradle.properties`

**Interfaces:**
- Consumes: nothing (pure removal/config task); depends on Task 3 being complete so no Java source still references ENDI/Easy NPC classes before this task removes their dev-runtime jars.
- Produces: nothing consumed by later tasks — this is the last task.

- [ ] **Step 1: Remove the optional mods.toml dependency**

In `src/main/resources/META-INF/mods.toml`, delete the trailing block (currently lines 54-59):

```toml
[[dependencies.${mod_id}]]
modId = "easynpcdialogueimmersion"
mandatory = false
versionRange = "[0,)"
ordering = "AFTER"
side = "CLIENT"
```

Leave the file ending after the `ftbquests` dependency block's `side = "BOTH"` line.

- [ ] **Step 2: Remove the Easy NPC/ENDI dev-runtime block from build.gradle**

In `build.gradle`, delete:

```groovy
    // Easy NPC uses mixins, so the development runtime must load remapped copies
    // through ForgeGradle rather than raw production jars from run/mods.
    if (file("run/mods-disabled/easy_npc-forge-1.20.1-7.12.1.jar").exists()) {
        runtimeOnly fg.deobf("dev.local:easy_npc-forge-1.20.1:7.12.1")
        runtimeOnly fg.deobf("dev.local:easy_npc_config_ui-forge-1.20.1:7.12.1")
        runtimeOnly fg.deobf("dev.local:easy_npc_bundle-forge-1.20.1:7.12.1")
        runtimeOnly fg.deobf("dev.local:easy-npc-dialogue-immersion-forge-1.20.1:0.3.1")
    }
```

leaving the `Placebo` conditional block as the last entry before the closing `}` of `dependencies { ... }`.

- [ ] **Step 3: Restore the Forge baseline**

In `gradle.properties`, change:

```properties
forge_version=47.4.10
```

to:

```properties
forge_version=47.2.19
```

- [ ] **Step 4: Confirm no remaining ENDI/Easy NPC references**

Run: `grep -rn "easynpcdialogueimmersion\|easy_npc\|ClientDialogConfig" --include=*.java --include=*.toml --include=*.gradle --include=*.properties . | grep -v "/build/"`
Expected: no output (the only remaining hits should be the ENDI design doc and handoff doc under `docs/`, which this grep's include filters already exclude).

- [ ] **Step 5: Full clean build**

Run: `./gradlew clean build`
Expected: BUILD SUCCESSFUL, with no `run/mods-disabled` Easy NPC jars present (confirms the addon's dev classpath no longer needs them).

- [ ] **Step 6: Commit**

```bash
git add src/main/resources/META-INF/mods.toml build.gradle gradle.properties
git commit -m "Drop the optional ENDI dependency and restore the 47.2.19 Forge baseline"
```

---

### Task 5: Manual client verification

**Files:** none (manual QA pass; no code changes expected unless a regression is found, in which case fix it in the relevant file from Tasks 1-4 and re-run this task).

- [ ] **Step 1: Launch the dev client with no Easy NPC/ENDI jars present**

Run: `./gradlew runClient`
Expected: client starts with no missing-dependency or classloading errors.

- [ ] **Step 2: Open a fresh eligible quest for the first time**

In-game, open a quest that has never been viewed before and has a non-empty description.
Expected: the description types in character-by-character, audibly clicking on letters/digits, pausing longer after `,`/`.`/`!`/`?`.

- [ ] **Step 3: Confirm click/key skip**

While the above quest is still typing, click inside the modal body (not on a task/reward/close target) or press a non-Escape key.
Expected: remaining text reveals instantly; no task/reward is triggered; the modal stays open. Escape still closes the modal as before.

- [ ] **Step 4: Confirm persistence after reopen**

Close and reopen the same quest (and/or restart the client).
Expected: the description now renders fully immediately, with no typewriter and no blips.

- [ ] **Step 5: Spot-check images/page-breaks/multi-page descriptions**

Open a quest whose description contains an image or page break, and one with a long multi-page description.
Expected: images/page-breaks keep their layout height from the first frame (no scroll jump); narration still reveals only `TextBlock` content.

- [ ] **Step 6: Record the result**

No commit needed for this task — it is verification only. If a regression is found, fix it as a new commit in the appropriate file from Tasks 1-4, then re-run the affected manual step.
