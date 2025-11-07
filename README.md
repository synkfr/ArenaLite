# ArenaLite

A configurable kit-based Free-For-All (FFA) plugin for Paper 1.21+ servers. ArenaLite focuses on a smooth admin workflow, reliable kit handling, and battle-ready defaults that you can tailor through configuration.

## Highlights

- **Arena Workflow** – Create, list, delete, and configure arenas in-game (`/ffasetup create/delete/setspawn/build/setregen/list`).
- **Kit Management** – Save slot-accurate kits from your inventory, link them to arenas, and preview them safely.
- **Build Toggles** – Per-arena build flags with high-priority listeners ensure your FFA rules override other plugins when players join.
- **Safety Options** – Join protection and anti-cleanup buffs are fully configurable (or disable them entirely).
- **Storage Choices** – Player stats persist to YAML by default with optional MySQL support.
- **Optional Hooks** – Integrates with FastAsyncWorldEdit (FAWE), PlaceholderAPI, and Vault when present.

## Commands

### Player Commands

| Command | Description |
|---------|-------------|
| `/ffa join <kit>` | Join with the specified kit. |
| `/ffa leave` | Leave the arena and return to the lobby spawn. |
| `/ffa kit preview <kit>` | View a read-only preview (if enabled). |
| `/ffa kit create <kit>` | Create a kit from your inventory (requires `arenalite.admin`). |
| `/ffa setspawn` | Set the global FFA lobby spawn (requires `arenalite.admin`). |
| `/ffa regen <arena>` | Manually trigger a regen (requires `arenalite.admin`). |
| `/ffa reload` | Reload configuration and data (requires `arenalite.admin`). |

### Setup Commands (`arenalite.admin`)

| Command | Description |
|---------|-------------|
| `/ffasetup wand` | Receive the region selection wand. |
| `/ffasetup create <arena>` | Create an arena using your current world. |
| `/ffasetup delete <arena>` | Remove an arena (kits linked to it are unlinked). |
| `/ffasetup setspawn <arena>` | Set the arena spawn point. |
| `/ffasetup setregen <arena> <true|false>` | Toggle regeneration for the arena. |
| `/ffasetup build <arena> <true|false>` | Enable or disable player building for the arena. |
| `/ffasetup list` | Display arena summary (spawn/regen/build state). |
| `/ffasetup kit create <kit>` | Save your current inventory as a kit. |
| `/ffasetup kit delete <kit>` | Delete an existing kit. |
| `/ffasetup kitlink <kit> <arena>` | Link a kit to an arena. |

## Configuration Overview

`config.yml` ships with sensible defaults and is fully documented inline. Key sections:

```yaml
join-protection:
  enabled: true
  duration: 10            # seconds
  clear-effects: true     # remove potion effects/fire when joining/leaving

anti-cleanup:
  enabled: true
  regen:
    enabled: true
    duration: 3           # seconds
  resistance:
    enabled: true
    duration: 5           # seconds
    level: 2

arenas:
  default-build-enabled: true
  default-regen-enabled: false
  default-regen-interval: 300

settings:
  hotbar-enabled: false
  scoreboard-enabled: true
  kit-previews:
    enabled: true
    readonly: true
  instant-respawn-delay: 1   # ticks

ffa:
  spawn: ""                 # set via /ffa setspawn

storage:
  type: YAML                 # YAML or MYSQL
  mysql:
    host: localhost
    port: 3306
    database: arenalite
    username: root
    password: password
    use-ssl: false
    table-prefix: arenalite_
```

Additional configuration lives in:

- `messages.yml` – Fully translatable messages.
- `arenas.yml` / `kits.yml` – Generated data stores (editing is optional).
- `hotbar.yml`, `scoreboard.yml`, `settings.yml` – Stubs for future features (safe to disable).

## Storage Options

- **YAML (default)** – Zero setup, perfect for small/medium servers.
- **MySQL** – Enable in `config.yml` for asynchronous read/write through the Bukkit scheduler.

## Optional Integrations

- **FastAsyncWorldEdit** – Required for asynchronous regeneration. Without FAWE, regen commands are ignored.
- **PlaceholderAPI** – Registers `%arenalite_*%` placeholders automatically.
- **Vault** – Foundation for upcoming economy rewards (hook loads when Vault is present).

## Installation

1. Drop the plugin into your `plugins/` directory.
2. Start the server once to generate configuration files.
3. Adjust `config.yml`, `messages.yml`, and the data files as needed.
4. Use the `/ffasetup` commands to create arenas and kits.

## Permissions

- `arenalite.use` – Access to player commands (default: everyone).
- `arenalite.admin` – Access to setup/admin commands (default: OP).
- `arenalite.*` – Grants both of the above.

## Useful Links

- [docs/SETUP.md](docs/SETUP.md) – Step-by-step arena & kit setup.
- [docs/API.md](docs/API.md) – Working with ArenaLite in your own plugins.
- [docs/PERFORMANCE.md](docs/PERFORMANCE.md) – Tuning advice for larger servers.

## Building From Source

```bash
git clone <repository-url>
cd ArenaLite
mvn clean package
```

The compiled JAR is produced in `target/`.

## License & Support

ArenaLite is released under the MIT License. If you run into issues:

1. Check the documentation.
2. Search existing GitHub issues.
3. Open a new issue with reproduction steps and server logs.

Enjoy the fights!

