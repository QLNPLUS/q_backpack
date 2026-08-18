# Q Backpack

Q Backpack is a lightweight, standalone Curios backpack mod for Minecraft 1.20.1 Forge.
It adds five upgradeable backpacks whose storage is integrated directly into the normal
player inventory screen.

## Features

- Leather Backpack: 9 slots
- Iron Backpack: 18 slots
- Gold Backpack: 27 slots
- Diamond Backpack: 36 slots
- Netherite Backpack: 52 slots (4 rows x 13 columns)
- Equipped in the Curios `back` slot
- Backpack storage integrated into the player inventory screen
- Automatic pickup overflow when the player inventory is full
- Quark-style sorting for the player inventory and backpack
- Optional sort-button tooltip, configured in `q_backpack-client.toml`
- Non-empty backpacks cannot be unequipped
- Backpack nesting prevention
- Separate item textures and wearable models for every tier
- Resource-pack replaceable wearable JSON models and GUI textures for every tier
- Upgrade recipes for all five tiers

## Requirements

- Minecraft 1.20.1
- Minecraft Forge 47.4.10 or newer
- Curios API 5.6.1 or newer
- Java 17

Quark is not required.

## Resource Pack Paths

Each tier has an independent wearable model and GUI texture:

- `assets/q_backpack/models/wearable/<tier>_backpack.json`
- `assets/q_backpack/textures/block/<tier>_backpack.png`
- `assets/q_backpack/textures/gui/<tier>_backpack.png`

The internal tier names are `small`, `medium`, `large`, and `huge`. Wearable models use
the vanilla Java Block/Item JSON format exported by Blockbench. The model is centered at
`[8, 8, 8]` before being attached to the player's body.

## Relationship to Quark

Q Backpack is an unofficial, independent adaptation of the backpack feature from
[Quark](https://github.com/VazkiiMods/Quark), release 1.20.1-4.0-462.

The backpack appearance, modified texture assets, and inventory sorting design are
derived from Quark by Vazkii and contributors under CC BY-NC-SA 3.0. This project is
not affiliated with, maintained by, sponsored by, or endorsed by Vazkii, the Quark
team, or Violet Moon. See [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) for details.

## Building

```powershell
.\gradlew.bat build
```

Build artifacts are written to `build/libs/`.

## License

Q Backpack is distributed under the
[Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported license](LICENSE.md).
