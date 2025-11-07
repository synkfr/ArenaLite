# Safe Regeneration

ArenaLite performs a safe regeneration when an arena is restored (scheduled or via command).

- Players currently inside the arena region are teleported to that arena's set spawn (`/ffasetup setspawn <arena>`).
- Those players are temporarily movement-locked until the regeneration is complete.
- Once regen finishes, movement is restored automatically.

## How it works

1. When regen starts, the plugin identifies all players inside the arena region.
2. Each player is immediately frozen (walk/fly speed set to 0, invulnerable) and teleported to the arena spawn if set.
3. Blocks are restored from the snapshot in throttled batches to avoid lag.
4. When the restore completes, all frozen players for that arena are unfrozen.

## Requirements

- The arena must have a region set (via selection wand) and a spawn set inside the region.
- Set regeneration on with `/ffasetup setregen <arena> true`.
- Optionally configure intervals and performance settings in `config.yml`.

## Admin Commands

- `/ffa regen <arena>` – Manually trigger a regen.
- `/ffasetup setspawn <arena>` – Define the spawn used during regen.
- `/ffasetup setregen <arena> <true|false>` – Toggle scheduled regeneration.

## Configuration

- `regen.default-interval` – Default interval (seconds) when enabling regen using setup command.
- `regen.blocks-per-tick` – Throttle for snapshot restoration to balance performance.

