# ArenaLite Wiki

This wiki covers all player/admin commands, permissions, placeholders, and usage tips.

## Prefix & Colors

- Global message prefix is configurable in `messages.yml` under `prefix`.
- Hex colors are supported using the token format `&#RRGGBB`.
- Normal messages use aqua/gray. Errors and no-permission use `&#FF0000` (red).

## Commands

### Player Commands

- /ffa join <kit>
  - Join the FFA with a specific kit.
- /ffa leave
  - Leave the current arena and return to the lobby.
- /ffa kit preview <kit>
  - Preview a kit (read-only when enabled).
- /ffa setspawn
  - Set the global FFA lobby spawn (requires admin).
- /ffa regen <arena>
  - Manually trigger a regeneration for the specified arena (requires admin).
- /ffa reload
  - Reload plugin configuration and data (requires admin).

### Setup/Admin Commands

- /ffasetup wand
  - Get the region selection wand.
- /ffasetup create <arena>
  - Create a new arena in your current world.
- /ffasetup delete <arena>
  - Delete an arena.
- /ffasetup setspawn <arena>
  - Set the arena spawn. Must be inside the arena region.
- /ffasetup setregen <arena> <true|false>
  - Toggle scheduled regeneration for the arena.
- /ffasetup build <arena> <true|false>
  - Toggle building in the arena.
- /ffasetup list
  - List all arenas with status summary.
- /ffasetup kit create <kit>
  - Create a kit from your current inventory.
- /ffasetup kit delete <kit>
  - Delete a kit.
- /ffasetup kitlink <kit> <arena>
  - Link a kit to an arena.

## Regeneration Behavior

- Safe regen teleports players inside the arena region to that arena's spawn and locks movement until regen finishes.
- Works without FAWE via built-in snapshot restore; FAWE improves performance on large regions.
- Tune performance using `regen.blocks-per-tick` in `config.yml`.

## Permissions

- arenalite.use
  - Access to basic player commands. Default: everyone.
- arenalite.admin
  - Access to all setup/admin commands. Default: OP.
- arenalite.*
  - Grants both of the above.

## Placeholders (PlaceholderAPI)

- %arenalite_kills%
- %arenalite_deaths%
- %arenalite_kdr%
- %arenalite_streak%
- %arenalite_kit%
- %arenalite_arena%

### Arena regen placeholders

- Format: `%arenaregen_next_<arena>%`
  - Shows time remaining until the next regeneration for the specified arena in `MM:SS`.
  - Returns `N/A` if the arena doesn't exist or regen is disabled.

Examples:

- Next regen for arena "desert": `%arenaregen_next_desert%`

Usage with PlaceholderAPI example:

- In chat formats or scoreboards: `Kills: %arenalite_kills%`

### Leaderboard placeholders

Format: `%arenalite_leaderboard_<type>_<kit>_<position>%`

- **type**: `kills`, `deaths`, `kdr`, `streak`, `player`
  - `kills`, `deaths`, `kdr`, `streak` → returns the numeric value at that rank for the kit
  - `player` → returns the player name at that rank (tries kills, then deaths, then kdr, then streak)
- **kit**: kit name; may contain underscores (everything between type and position is treated as the kit name)
- **position**: 1-based index (1 = top)

Examples:

- Top killer value for kit "diamond": `%arenalite_leaderboard_kills_diamond_1%`
- Top player name (best available) for kit "diamond": `%arenalite_leaderboard_player_diamond_1%`
- 3rd highest KDR for kit "my_pvp_kit": `%arenalite_leaderboard_kdr_my_pvp_kit_3%`

Notes:

- Returns `N/A` if there is no entry at the requested position.
- Returns `Invalid type` if `<type>` is not one of the supported values.

## Usage Tips

- Ensure `/ffasetup setspawn <arena>` is inside the defined region for proper respawn and safe regen teleport.
- Keep arena regions tight for faster snapshot saves/restores.
- Use `/ffasetup list` to quickly verify arena configuration.
- After editing `messages.yml` or configs, run `/ffa reload`.
