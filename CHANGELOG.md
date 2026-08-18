# Changelog

All notable changes to this project will be documented in this file.

## [1.4.2] - 2026-08-18

### Added

- Added compatibility handling for the Apothic Attributes panel on Q Backpack screens.
- Added Shift-click routing between the player inventory, hotbar, and Q Backpack.

### Fixed

- Kept the Apothic Attributes panel above backpack slots and blocked clicks through its overlay.
- Prevented the Apothic Attributes panel from shifting the backpack, Curios controls, or recipe button.
- Fixed startup Mixin failures caused by incompatible screen injections.
- Shift-clicking player inventory items now targets the hotbar first, then Q Backpack when the hotbar is full.
- Shift-clicking Q Backpack items now targets the hotbar first, then the player inventory when the hotbar is full.
- Shift-clicking hotbar items now targets the player inventory first, then Q Backpack when the player inventory is full.
