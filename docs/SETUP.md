# ArenaLite Setup Guide

Follow this guide to deploy ArenaLite and create your first playable arena.

## Prerequisites

- Paper 1.21+ (or a compatible fork)
- Operator/`arenalite.admin` permission
- (Optional) FastAsyncWorldEdit to speed up regeneration. Without FAWE, ArenaLite uses built-in snapshot-based regen.
- (Optional) PlaceholderAPI for placeholders
- (Optional) Vault for future economy rewards

## 1. Installation

1. Download the latest ArenaLite release.
2. Drop the JAR into your server’s `plugins/` directory.
3. Start the server to generate configuration and data files.

## 2. Configure the Plugin

Open `config.yml` and review the defaults. Key toggles:

- `join-protection` – Enable/disable invulnerability, change duration, clear potion effects.
- `anti-cleanup` – Apply regeneration/resistance (per-effect toggles).
- `arenas.default-*` – Control whether new arenas start with building and regeneration enabled.
- `settings.kit-previews` – Decide if previews are available and read-only.
- `storage.type` – `YAML` (default) or `MYSQL`.

Save the file and run `/ffa reload` whenever you adjust it.

## 3. Create an Arena

1. **Get the wand**
   ```
   /ffasetup wand
   ```

2. **Create the arena**
   ```
   /ffasetup create desert
   ```

3. **Select the region**
   - Left-click a block for position 1.
   - Right-click another block for position 2.

4. **Set the spawn**
   ```
   /ffasetup setspawn desert
   ```
   Ensure the spawn is inside the selected region. It will be used to safely teleport players during regeneration.

5. **Optional toggles**
   ```
   /ffasetup setregen desert true
   /ffasetup build desert true
   ```

## 4. Create & Link a Kit

1. Equip yourself with the desired loadout.
2. Save the kit:
   ```
   /ffasetup kit create diamond
   ```
3. Link it to an arena:
   ```
   /ffasetup kitlink diamond desert
   ```

## 5. Test the Flow

```text
/ffa join diamond   # enter the arena
/ffa leave          # return to the lobby spawn
```

On join you should be teleported, receive your kit, and (if enabled) gain join protection. Use `/ffasetup list` to double-check arena settings.

## Advanced Topics

### Managing Multiple Arenas

Repeat the creation steps for each arena/kit pair:

```text
/ffasetup create nether
/ffasetup kit create blaze
/ffasetup kitlink blaze nether
```

### Switching to MySQL

1. Edit `config.yml`:
   ```yaml
   storage:
     type: MYSQL
     mysql:
       host: localhost
       port: 3306
       database: arenalite
       username: your_username
       password: your_password
       use-ssl: false
       table-prefix: arenalite_
   ```
2. Create the database if needed:
   ```sql
   CREATE DATABASE arenalite;
   ```
3. Reload the plugin:
   ```
   /ffa reload
   ```

### PlaceholderAPI Placeholders

- `%arenalite_kills%`
- `%arenalite_deaths%`
- `%arenalite_kdr%`
- `%arenalite_streak%`
- `%arenalite_kit%`
- `%arenalite_arena%`

## Troubleshooting

| Issue | Checks |
|-------|--------|
| **Players cannot build/break** | Ensure `/ffasetup build <arena> true` is set. |
| **Players spawn outside the arena** | Re-run `/ffasetup setspawn <arena>`. |
| **Kit not found** | Confirm `/ffasetup kit create` succeeded and `/ffasetup kitlink` is set. |
| **Regen not working** | Ensure the arena region and spawn are set, then enable regen via `/ffasetup setregen <arena> true`. FAWE is optional. |

## Next Steps

- Customise `messages.yml` for your branding.
- Edit `arenas.yml`/`kits.yml` if you prefer manual tweaks.
- Disable kit previews or join protection if you’re integrating with other PvP plugins.

