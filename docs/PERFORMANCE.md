# Performance Notes

ArenaLite is lightweight by design, but the following tips will help keep your FFA smooth when player counts rise.

## Storage Choices

| Mode | When to use | Notes |
|------|-------------|-------|
| YAML (default) | Small/medium servers | Zero configuration, stored in `plugins/ArenaLite/playerdata/`. |
| MySQL | Shared or high-population servers | Async read/write through the Bukkit scheduler. Ensure your database server is nearby. |

## Arena Regeneration

- **FAWE is required** for regeneration. Without it, regen commands are ignored.
- Keep selections as tight as possible—larger regions mean longer snapshots.
- Adjust `arenas.default-regen-interval` and per-arena regen intervals based on server TPS.

## Build & PvP Overrides

ArenaLite registers high-priority listeners to ensure your build settings override other plugins. If you intentionally want another plugin to control building, disable the feature in `config.yml` (`arenas.default-build-enabled`) or via `/ffasetup build`.

## Join & Cleanup Buffs

Join protection and anti-cleanup effects use standard potion effects and invulnerability. They are configurable because:

- Disabling them saves a few scheduler tasks.
- Lowering durations reduces the chance of interference with other combat plugins.

## Monitoring Checklist

- Watch your console for FAWE warnings during arena regen.
- Track database latency if you switch to MySQL—slow connections will show as delayed stat updates.
- Use `/ffasetup list` periodically to verify arena state at a glance.

## General Advice

- Keep backups of `arenas.yml` and `kits.yml` before major edits.
- Use `/ffa reload` sparingly on busy servers—saving kits/arenas writes synchronously.
- Remove unused arenas with `/ffasetup delete <arena>` to keep configuration lean.

