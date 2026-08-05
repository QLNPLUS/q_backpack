# Third-party notices

This project adapts the backpack appearance and the inventory sorting design from
[Quark](https://github.com/VazkiiMods/Quark), release 1.20.1-4.0-462. The copied and
modified Quark textures are under `src/main/resources/assets/q_backpack/textures/`.
Quark is by Vazkii and contributors and is licensed under CC BY-NC-SA 3.0. A copy
of that license is included as `LICENSE_QUARK.md`.

The Curios equipment behavior was informed by
[Curios Quark Oddities Backpack](https://github.com/yzl210/CuriosQuarkOBP), licensed
under GPL-3.0. This implementation uses the public Curios API directly and does not
include its Mixin sources.

Changes in this adaptation include four fixed capacities, storage integrated into the
player inventory screen, Curios-native equipment, server-authoritative sorting,
automatic pickup overflow, unequip protection, and backpack nesting prevention.
