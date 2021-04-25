## Gunpowder 0.5.0

Bugs fixed:
- Time static boolean was inverted.
- XP is no longer empty on dimension teleport.

New Features:
- Specify database in gunpowder.yaml
  - THIS IS A BREAKING CHANGE! OLD CONFIGS WILL NOT LOAD!

---

## Gunpowder 0.4.5

Bugs fixed:
- Time is no longer static in custom worlds
- ServerWorldProperties are passed directly to the constructor of the world now

---

## Gunpowder 0.4.4


Bugs fixed:

- None

New Features:

- Added the ability to link two dimensions through nether portals.

Improvements:

- No longer using the deprecated gameDirectory field.

Other:

- None

---

## Gunpowder 0.4.3

Bugs fixed:
- Default implementation for priority was not working correctly

---

## Gunpowder 0.4.2

New Features:
- (Module) Modules can now specify a priority for loading.

Other:
- (Util) Added a mixin that prints netty disconnect stacktraces.


---

## Gunpowder 0.4.0

Improvements:
- (ChestGUI) ChestGUI is now easier to use
- (Dimension) The `persist` option is now removed, mods should handle re-registering dimensions themselves


---

## Gunpowder 0.3.7

Bugs fixed:

- (ChestGUI) Fixed refreshInterval not working
- (ChestGUI) Fixed refreshInterval being set to 21 ticks rather than 20

New Features:

- None

Improvements:

- (ChestGUI) ChestGUI button callbacks now receive the Container as argument

Other:

- (misc) Made errors in commands log the full error 

---

## Gunpowder 0.3.6

Bugs fixed:

- None

New Features:

- None

Improvements:

- TranslatedText defaults to en_us rather than the key.

Other:

- Gunpowder now states dependencies on CF automatically.
- Add [Hermes](https://github.com/Haven-King/hermes) support.

---

## Gunpowder 0.3.5

Bugs fixed:

- None

New Features:

- None

Improvements:

- (database) Database Thread is now a daemon thread.

Other:

- Updated to 1.16.5


---

## Gunpowder 0.3.4

Bugs fixed:

- (sign) Owner data is retained after server reboot
- (sign) `requires` check is now applied to destroying

New Features:

- (module) Added reload hook

Improvements:

- None

Other:

- None

---

## Gunpowder 0.3.3

Bugs fixed:

- (language) Fix incompatibility with hwyla
- (sign) Fix bug with streams usage

New Features:

- None

Improvements:

- (language) Added methods to get the language for a user

Other:

- None

---

## Gunpowder 0.3.2

Bugs fixed:

- (teleport) Facing now properly defaults to where a player was looking

New Features:

- None

Improvements:

- (language) Now loads all mods instead of just gunpowder extensions

Other:

- None

---

## Gunpowder 0.3.1

Bugs fixed:

- (database) Fixed missing libraries
- (language) Fixed NPE

New Features:

- None

Improvements:

- None

Other:

- None

---

## Gunpowder 0.3.0

Bugs fixed:

- None

New Features:

- (api) Added TranslatedText

Improvements:

- (api) Added more documentation
- (database) Renamed "Essentials Database Thread" to "Gunpowder Database Thread"
- (base) Change server brand to "Fabric/Gunpowder"

Other:

- (gradle) Removed maven task, replaced with runtime jar
- (gradle) Made it runnable in dev by relocating Exposed

---

## Gunpowder 0.2.10

New Features:

- WorldSleep callbacks

---

## Gunpowder 0.2.9

Bugs fixed:

- Database is now loaded on mod load instead of server start
- Sign generation no longer crash the game

New Features:

- ChestGUI now supports a refresh interval with callback

Improvements:

- Signs now ignore namespace if only one of the value is registered

Other:

- Updated to 1.16.2

---

## Gunpowder 0.2.8

Bugs fixed:

- Server-side languages *should* now work

New Features:

- SignType API
- Runtime Dimension API

Improvements:

- /teleport now correctly calls PlayerPreTeleportEvent and PlayerTeleportEvent

Other:

- Rename to Gunpowder

---
