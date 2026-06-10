# Changelog

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
