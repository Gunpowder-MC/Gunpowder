## Gunpowder 0.3.0

Bugs fixed:

- None

New Features:

- None

Improvements:

- (database) Renamed "Essentials Database Thread" to "Gunpowder Database Thread"

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
