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
