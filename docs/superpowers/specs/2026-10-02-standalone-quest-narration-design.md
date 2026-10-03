# Standalone Quest Narration Design

## Goal

Make first-open quest-description narration a native client-only feature of FTB Quests UI Overhaul, with no runtime dependency on Easy NPC or Easy NPC Dialogue Immersion (ENDI).

## Scope

The existing first-open ledger, description-only rendering, skip input behavior, and description fingerprinting remain unchanged. The external ENDI reflection bridge is replaced with addon-owned pacing and sound code.

## Native narration behavior

- Reveal at a fixed 50 characters per second.
- Apply ENDI's stock punctuation pauses: comma 300 ms, period 600 ms, exclamation 500 ms, and question mark 600 ms.
- Preserve the existing UTF-16 reveal accounting and code-point-safe styled prefix rendering.
- Play a local copy of ENDI's default `dialogue_blip.ogg` for newly revealed letters and digits only.
- Cap a lagged frame to the newest eight candidate blips while revealing all visual characters immediately.
- Add one client-side config toggle, `enable_quest_narration` (default `false`), controlled from this addon's own config screen. No pack-side control, no speed/sound customization, and no network/server behavior.

## Components

`QuestNarrationPacing` owns stateless reveal-count calculation and punctuation timing constants. `QuestNarrationSoundPlayer` owns local sound playback and a minimal active-sound overlap guard. `OverhaulQuestScreen` creates and uses one native narration presenter; it must not check `ModList` or load any optional-mod class.

The copied sound is stored at `assets/ftbquestsvisualoverhaul/sounds/dialogue_blip.ogg`, declared in the addon's `sounds.json`, and played through the addon's own `SoundEvent` registration.

## Dependency and development policy

- Remove the optional `easynpcdialogueimmersion` declaration from `mods.toml`.
- Remove Easy NPC and ENDI remapped development-runtime entries from `build.gradle`.
- Restore the addon's Forge baseline to 47.2.19.
- Easy NPC/ENDI jars remain outside the published addon and are not required by its tests or development client.

## Verification

- Unit-test punctuation pacing, including the stock delays and a surrogate-pair-safe visible prefix boundary where practical.
- Build and run the client with no Easy NPC/ENDI jars present.
- Confirm the feature is inert with `enable_quest_narration` at its default `false`: quests render exactly as before, with no typewriter and no blips.
- With the toggle turned on, manually open a fresh eligible quest and confirm description-only typewriter reveal, punctuation pauses, default blip, click/key skip, and persistence after reopen.
