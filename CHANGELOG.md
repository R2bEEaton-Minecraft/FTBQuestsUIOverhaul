# Changelog

## 1.0.3

### Navigation
- Opening the questbook or switching chapters now focuses the camera on your progress instead of the middle of the chapter: your tracked quest, then anything with rewards waiting, then whatever you can do next, and in an untouched chapter its starting quests
- When several quests need framing, the view zooms out just enough to fit them - it never zooms in past the zoom you chose
- New rebindable key (default **Space**) re-centers the quest view on your progress, like the default FTB Quests UI
- New **Tracked Quest** button under the chapter list jumps straight to the quest on your HUD and opens it, so you can untrack it without hunting through chapters - it cycles if you have several tracked
- New chevron beside the QUEST CHAPTERS title collapses or expands every chapter group at once

### Fixes
- Rewards marked invisible (commonly command rewards used for backend functionality) are no longer shown in the quest popup
- Long quest subtitles no longer overflow outside the quest popup

## 1.0.2

### Colors
- All cream/brown text is now white or grey by default, including chapter list labels, the quest detail popup, tooltips, the choice reward popup, the chapter scrollbar, and paragraph separator lines
- New: every UI text/scrollbar/separator color can be overridden by resource packs via `assets/ftbquestsvisualoverhaul/ui_colors.json` - pack devs can match text colors to their pack, and the file hot-reloads with F3+T (see RESOURCE_PACKS.md)
- The built-in Questbook Edition pack keeps the classic cream palette via its own ui_colors.json

### Fixes
- The pannable quest area no longer renders on top of the inner drop shadow around the quest tree viewport

## 1.0.1

### New default look
- Vanilla-style UI textures are now the default look of the mod
- The classic brown quest book look is now a built-in resource pack called **Questbook Edition** - enable it from Options > Resource Packs, no separate download needed
- Want to make your own skin? See RESOURCE_PACKS.md on GitHub for a full guide to the overridable textures

### UI polish
- Chapter list texts now have vanilla-style drop shadows for better readability
- Removed the background fill behind chapter group headers (group headers now highlight on hover instead)
- Chapter title is now properly centered on the header band above the quest tree
- Texts drawn on the frame are now white for better contrast with resource packs
