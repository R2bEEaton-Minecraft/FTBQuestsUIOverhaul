# Re-skinning FTB Quests UI Overhaul with resource packs

The mod ships vanilla-style grey textures as the default look. The classic brown
quest book look is included as a built-in resource pack called **Questbook
Edition** that players can enable from the standard resource pack menu
(Options > Resource Packs). No extra download is needed.

Custom looks work exactly like any other Minecraft resource pack: create a zip
with a `pack.mcmeta` and override any of the texture paths below.

## Pack skeleton

```
my_pack.zip
├── pack.mcmeta
├── pack.png                  (optional square icon)
└── assets/
    ├── ftbquestsvisualoverhaul/textures/...
    └── minecraft/textures/gui/sprites/advancements/...
```

`pack.mcmeta`:

```json
{
  "pack": {
    "pack_format": 15,
    "description": "My custom skin for FTB Quests UI Overhaul"
  }
}
```

`pack_format` 15 is correct for Minecraft 1.20.1.

## Overridable textures

All sizes are in pixels. Keep the canvas sizes identical to the defaults; the
screen reads fixed coordinates from these textures.

### Main UI (`assets/ftbquestsvisualoverhaul/textures/gui/`)

| File | Canvas | Notes |
|---|---|---|
| `quests_background.png` | 512x256 | Whole quest book frame. The visible UI uses the top-left 392x217. Chapter list panel on the left; quest tree viewport at x=119, y=27, 248x166 with the chapter title band directly above it (band interior rows 9-25). |
| `quest_line_button_inactive.png` | 98x40 | Chapter button, idle. Drawn region starts 8 px down (`y` offset baked into the texture). |
| `quest_line_button_hover.png` | 98x40 | Chapter button, hovered. |
| `quest_line_button_active.png` | 98x40 | Chapter button, selected (wider tab that overlaps the tree panel). |
| `in_progress_wheel_strip.png` | 9x432 | Spinner: 48 frames of 9x9 stacked vertically. |

### Icons (`assets/ftbquestsvisualoverhaul/textures/icons/`)

| File | Notes |
|---|---|
| `lock.png` | Locked quest overlay |
| `checked.png` | Completed quest check |
| `notification.png` | Claimable reward `!` badge |
| `up.png`, `down.png` | Scroll arrows |

### Quest node frames (`assets/minecraft/textures/gui/sprites/advancements/`)

These are the vanilla advancement sprite paths the overhaul screen reads
directly, so overriding them in a pack only affects what this mod (and vanilla
advancements) draw with them:

| File | Notes |
|---|---|
| `box_obtained.png` / `box_unobtained.png` | Tooltip/description boxes (200x26 nine-slice, 10 px border) |
| `title_box.png` | Quest tooltip title box |
| `task_frame_obtained.png` / `task_frame_unobtained.png` | 26x26 quest node frames |

## Tips

- Start by copying the built-in pack: the brown textures live in this repo at
  `src/main/resources/resourcepacks/questbook_edition/`, or extract them from
  the released jar at the same path. Recolor from there.
- The quest tree tile background (oak planks by default) is not a texture
  override; it is chosen per chapter in-game with the creative-mode
  `Change Tile` button.
- Text drop shadows and colors are drawn by the mod, not the textures, so dark
  texture themes generally stay readable without edits.

## How the built-in pack works (for contributors)

`FTBQuestsVisualOverhaul#addPackFinders` registers
`src/main/resources/resourcepacks/questbook_edition/` through Forge's
`AddPackFindersEvent` as an optional client resource pack named "Questbook
Edition". To add another built-in variant, drop a second pack folder next to it
and register it the same way.
