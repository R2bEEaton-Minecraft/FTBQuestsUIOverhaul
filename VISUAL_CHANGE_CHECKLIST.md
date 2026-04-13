# FTB Quests UI Overhaul Visual Change Checklist

## Summary

This file tracks requested visual and behavior changes for the FTB Quests UI Overhaul work. Each item includes implementation notes and a manual testing procedure so future QA can verify both visuals and behavior.

## Public APIs, Interfaces, And Types

- Keep `mod_id=ftbquestsvisualoverhaul` unchanged for compatibility with existing configs, saves, and resource paths.
- Change only the user-facing `mod_name` to `FTB Quests UI Overhaul`.
- Primary implementation areas:
  - `OverhaulQuestScreen`: main visuals, chapter selector, tree panning, tile switcher, modal, connectors, Accept/Complete behavior.
  - `QuestDataSnapshot` and `QuestDataSnapshotBuilder`: chapter groups, locked state, task/reward state, pinned state.
  - `QuestActionRouter`: edit-mode toggles, pin/unpin, checkmark submission, reward claiming, vanilla fallback routing.
  - `QuestDataController`: snapshot refresh and persisted overhaul-only quest-pack metadata.
- Prefer existing FTB Quests behavior where available:
  - `ToggleEditingModeMessage` for editing mode.
  - `TogglePinnedMessage` for Accept/Unaccept quest tracking.
  - `SubmitTaskMessage` for checkmark task completion.
  - `ClaimRewardMessage`, `ClaimChoiceRewardMessage`, and existing fallback flow for reward claiming.
  - `SelectImageResourceScreen` and `ImageResourceConfig` for tile image selection.

## Checklist

- [x] Rename mod display name to `FTB Quests UI Overhaul`.
  - Implementation notes: `mod_id` remains `ftbquestsvisualoverhaul`; only the display name was changed.
  - Setup: Launch the mod or inspect generated mod metadata.
  - Steps: Open the mod list or inspect the resolved `mods.toml` values.
  - Expected Result: Display name reads `FTB Quests UI Overhaul`.
  - Regression Check: Existing resource namespace, configs, and quest data still use `ftbquestsvisualoverhaul`.

- [x] Match the main quest tree visual layout to the provided screenshot.
  - Implementation notes: The main frame was enlarged about one third, texture backups were copied to `backups/gui_textures_original_2026-04-13`, the title was nudged upward, the advancement area was made taller, and scroll/chapter panel offsets were adjusted through follow-up tuning.
  - Setup: Use a quest file with several chapters and visible quest nodes.
  - Steps: Open the overhaul UI in survival and creative; compare the frame, chapter selector, title band, oak-plank tree area, shadows, borders, and button positions to the reference screenshot.
  - Expected Result: The UI is larger, less cramped, and visually close to the screenshot while remaining pixel-aligned.
  - Regression Check: Quest selection, hover tooltips, chapter scrolling, and modal opening still work at small and large GUI scales.

- [x] Add quest chapter group titles, sourced from default FTB chapter groups.
  - Implementation notes: Snapshot data includes group information and the chapter selector renders group headings before the first chapter in each group.
  - Setup: Use quests with the default chapter group and at least one custom chapter group.
  - Steps: Open the quest UI and scroll the chapter selector.
  - Expected Result: Group titles appear above their chapters and use FTB chapter group names.
  - Regression Check: Ungrouped/default chapters still appear once, spacing remains stable, and scroll math remains correct.

- [x] Add a locked icon overlay for locked quests, using FTB quest availability state.
  - Implementation notes: The hand-drawn lock was replaced with FTB Quests' `ftbquests:textures/gui/quest_locked.png`; it is rendered at the FTB-style bottom-right position and scale for the quest node.
  - Setup: Use a quest with unmet dependencies that is still visible in the tree.
  - Steps: Open the quest tree and locate the locked quest.
  - Expected Result: The FTB Quests lock icon appears cleanly on the locked node.
  - Regression Check: Completed, available, hidden, and pinned quests do not incorrectly show the lock.

- [x] Add creative-only Free Pan toggle without zoom.
  - Implementation notes: Free Pan is stored in `QuestViewState`. Zoom was intentionally removed after testing; the toggle now controls bounded versus unbounded panning only.
  - Setup: Enter creative mode and open a chapter with nodes extending beyond the viewport.
  - Steps: Toggle Free Pan off and on, then drag/pan the tree.
  - Expected Result: Locked mode clamps panning to the bounded view; Free Pan allows unbounded panning. The mouse wheel does not zoom the UI.
  - Regression Check: The toggle is hidden in survival mode and survival panning remains bounded.

- [x] Add creative-only title/background switcher in the quest tree frame.
  - Implementation notes: The `Change Tile` button uses FTB Library's image selector and applies the selected texture to the oak-plank advancement tile background, not a separate title image.
  - Setup: Enter creative mode with edit permission and open a chapter.
  - Steps: Click `Change Tile`, choose a block texture such as `minecraft:block/ancient_debris_side`, close and reopen the quest UI.
  - Expected Result: The oak-plank tree background is replaced by the selected texture and no missing-texture warning appears for atlas-style IDs.
  - Regression Check: The button is hidden in survival mode; chapters without a selected tile continue using oak planks.

- [x] Persist selected tile textures in a quest addon-owned file.
  - Implementation notes: Tile selections now save to `config/ftbquests/quests/ftbquestsvisualoverhaul_tiles.properties` so they can travel with the quest pack. The old client-side file `config/ftbquestsvisualoverhaul_tiles.properties` is imported as a fallback when the new file is missing.
  - Setup: In creative mode, change a chapter tile and confirm the game has write access to the config folder.
  - Steps: Pick a tile, close/reopen the UI, then inspect `run/config/ftbquests/quests/ftbquestsvisualoverhaul_tiles.properties`.
  - Expected Result: The selected chapter ID maps to the selected texture in the quest-pack file and the tile persists after reopening.
  - Regression Check: Existing old client-side selections migrate once instead of being lost.

- [x] Rename `default view` to `Editing Mode`, activate FTB Quests editing mode, and add `Clean UI Mode` in default FTB Quests to return.
  - Implementation notes: The overhaul button routes through FTB edit mode before opening vanilla/default FTB Quests, and `CleanUiModeOverlay` provides the return path.
  - Setup: Use a creative-mode player with FTB Quests editor permission.
  - Steps: Open the overhaul UI, click `Editing Mode`, then click `Clean UI Mode` from the default FTB Quests screen.
  - Expected Result: `Editing Mode` opens the default editor UI; `Clean UI Mode` returns to the overhaul UI.
  - Regression Check: Players without edit permission cannot use this path to force an invalid editor state.

- [x] Persist the last-used clean/editing/default UI mode when the interface closes and reopens.
  - Implementation notes: The last UI mode is stored in the persisted view state.
  - Setup: Use creative mode with editor permission.
  - Steps: Switch modes, close the quest interface, then reopen it.
  - Expected Result: The UI reopens in the last-used mode where permissions allow it.
  - Regression Check: Changing worlds or losing edit permission does not strand the player in an invalid editor-only UI.

- [x] Resize the whole menu by about one third while keeping pixel accuracy.
  - Implementation notes: GUI PNGs were scaled by 4/3 as a working baseline and the originals were backed up under `backups/gui_textures_original_2026-04-13`.
  - Setup: Test GUI scales 2, 3, and Auto if available.
  - Steps: Open the main quest UI and quest detail modal at each GUI scale.
  - Expected Result: The UI is larger and remains crisp without fractional-pixel blur.
  - Regression Check: Text, icons, click hitboxes, and scissor regions line up with the visuals and do not clip.

- [x] Rework the quest description modal toward the provided advancement-style screenshot.
  - Implementation notes: Modal borders, dark body, title/header area, description body, requirements/rewards rows, Accept button, and Complete button were updated in `OverhaulQuestScreen`.
  - Setup: Use a quest with a title, subtitle/objective, long description, multiple requirements, and rewards.
  - Steps: Open the quest detail modal and compare it to the screenshot.
  - Expected Result: The modal reads as an extended vanilla advancement-style panel and long content scrolls inside the body.
  - Regression Check: Hidden-details quests still respect FTB visibility rules.

- [x] Improve connector spacing and bend placement.
  - Implementation notes: Horizontal quest spacing was increased to 1.5x and the elbow bend now happens halfway between the parent and child icon centers. A local test chapter named `Connector Stress Demo` was added to demonstrate dense dependencies.
  - Setup: Open `Connector Stress Demo` from the local run quest data.
  - Steps: Inspect the connectors from left parents into the center join, from the center join into upper/middle/lower branches, and from those branches into the right join.
  - Expected Result: The pipe direction change happens halfway between icons and the wider spacing makes bends easier to see.
  - Regression Check: Quest node hover and click detection remain aligned with the icons.

- [ ] Add full least-obstructed connector routing from different sides of quest squares.
  - Implementation notes: Not fully complete. Current routing uses midpoint orthogonal elbows, not full side-anchor selection with obstacle avoidance.
  - Setup: Create or use a dense chapter with close, crossing, and reverse-direction dependencies.
  - Steps: Inspect whether connectors should leave from top/bottom/left/right rather than always using the midpoint elbow style.
  - Expected Result: Future implementation should choose sensible entry/exit sides and reduce overlaps.
  - Regression Check: Green completed-line behavior and midpoint behavior should remain stable unless intentionally replaced.

- [x] Turn dependency connectors green when the parent quest is completed and the next quest is available, matching default FTB behavior.
  - Implementation notes: Connector color uses parent completion plus child availability state.
  - Setup: Create a parent quest, complete it, and unlock the child quest.
  - Steps: Open the quest tree after completing the parent.
  - Expected Result: The connector between the completed parent and available child is green.
  - Regression Check: Incomplete or unavailable dependency lines stay white/default/locked styling.

- [x] Change `Accept` behavior.
  - Implementation notes: Accept uses FTB pin/unpin routing, submits checkmark-only tasks when applicable, and does not submit unrelated task types.
  - [x] Hide checkmark-only tasks from Requirements.
  - [x] Clicking `Accept` submits a checkmark-only task when applicable.
  - [x] Clicking `Accept` pins the quest to the FPS/HUD tracker.
  - [x] Accepted state appears pressed and label changes to `Unaccept`.
  - [x] Clicking `Unaccept` unpins and restores the default state.
  - Setup: Create one checkmark-only quest and one quest with a non-checkmark task.
  - Steps: Open each quest detail modal and click `Accept`; click `Unaccept` afterward.
  - Expected Result: Checkmark-only quests hide the requirement row, submit the checkmark, and pin on Accept. Non-checkmark quests only pin/unpin.
  - Regression Check: Existing FTB pin state and HUD tracking remain synchronized with the button state.

- [x] Add `Complete` button.
  - Implementation notes: Complete uses existing reward claim routing and status markers based on claimable reward/task completion state.
  - [x] Show a red marker when the quest has claimable rewards or completed tasks pending.
  - [x] Clicking claims all available rewards.
  - [x] Mark rewards/task indicators with green ticks after completion/claiming.
  - Setup: Use quests with claimable normal rewards, choice rewards, already claimed rewards, and no rewards.
  - Steps: Complete a quest, open the detail modal, inspect the `Complete` button, and click it.
  - Expected Result: Claimable quests show a red marker; clicking `Complete` claims normal rewards, opens choice selection when needed, and updates green tick indicators after refresh.
  - Regression Check: Custom/unsupported rewards fall back to vanilla handling instead of silently failing.

- [x] Tune chapter selector button texture scale and hitboxes.
  - Implementation notes: Active, hover, and inactive PNGs render at their native `98x40` texture size. Inactive/hover logical hover/click/text area is `86px` wide with standard padding.
  - Setup: Use chapters with long names and multiple rows.
  - Steps: Move the mouse across the inactive/hover button art, especially the rightmost 12 pixels, then select a chapter.
  - Expected Result: Text uses the `86px` logical area and hover/click does not extend across the full `98px` art for inactive/hover rows. Active row art remains full size.
  - Regression Check: Active selection still works and long text scrolls/clips within the intended logical row.

- [x] Tune chapter selector scroll region and bottom vignette.
  - Implementation notes: The chapter scrollable region was shortened by 5 pixels from the bottom. A bottom shadow/vignette appears only when more chapters are hidden below, and the vignette stops 10 pixels before the right edge.
  - Setup: Use enough chapters to require scrolling.
  - Steps: Open the chapter selector at the top, scroll partway down, then scroll to the bottom.
  - Expected Result: A bottom shadow hints that more content is below until the list reaches the bottom. The shadow does not cover the last 10 pixels on the right.
  - Regression Check: Scissor clipping, scrollbar hitbox, and chapter row click targets still match the visible area.

## Focused Test Cases

- [ ] Survival mode: Free Pan and Change Tile controls are hidden.
- [ ] Creative mode: Free Pan toggles bounded versus unbounded panning; mouse wheel does not zoom.
- [ ] Creative mode without FTB edit mode: `Editing Mode` is visible and toggles FTB editing mode.
- [ ] Creative mode with FTB edit mode active: `Clean UI Mode` returns to the overhaul UI and mode persists after close/reopen.
- [ ] Tile switcher: selecting an atlas-style ID such as `minecraft:block/ancient_debris_side` changes the oak-plank tree background without missing-texture warnings.
- [ ] Tile persistence: selected tiles save to `config/ftbquests/quests/ftbquestsvisualoverhaul_tiles.properties` and survive close/reopen.
- [ ] Legacy tile migration: deleting the quest-pack tile file while keeping `config/ftbquestsvisualoverhaul_tiles.properties` imports the old selection into the new file.
- [ ] Locked quest: node is visible when appropriate and the FTB Quests lock icon appears cleanly.
- [ ] Completed dependency to available child: connector becomes green.
- [ ] Incomplete or unavailable dependency: connector remains default/locked styling.
- [ ] Connector stress demo: midpoint elbows appear halfway between icons in `Connector Stress Demo`.
- [ ] Least-obstructed routing: side-anchor path selection is still pending and should be tested when implemented.
- [ ] Checkmark-only quest: Requirements hides the checkmark task, `Accept` submits it and pins the quest.
- [ ] Non-checkmark quest: `Accept` only pins/unpins without submitting unrelated task types.
- [ ] Quest with normal rewards: `Complete` claims all claimable rewards and updates indicators.
- [ ] Quest with choice/custom rewards: choice UI or vanilla fallback is used instead of silent failure.
- [ ] Long title/description: modal text wraps and scrolls without overflowing.
- [ ] Small and large GUI scales: enlarged UI remains pixel-aligned and does not clip.
- [ ] Chapter groups: default-group and custom-group titles appear in the chapter list and spacing stays consistent.
- [ ] Chapter selector buttons: inactive/hover textures render at `98px`, but hover/click/text use the `86px` logical width.
- [ ] Chapter selector vignette: bottom shadow appears only when more chapters are hidden below and is trimmed 10 pixels from the right.

## Assumptions And Defaults

- This tracking file lives at the repo root as `VISUAL_CHANGE_CHECKLIST.md`.
- The mod ID stays `ftbquestsvisualoverhaul`; only the displayed mod name changes to avoid breaking configs, saves, or resource paths.
- "1 on 1 with the screenshot" means match the attached visual reference as closely as possible within Minecraft GUI rendering constraints.
- Existing FTB Quests UI/network behavior should be reused wherever possible instead of replacing server-side quest mechanics.
- Quest-pack-owned overhaul metadata belongs under `config/ftbquests/quests/` unless a future server-synced FTB Quests extension point is added.
- Original GUI texture backups should remain in `backups/gui_textures_original_2026-04-13` while the scaled PNGs are still being manually tuned.
