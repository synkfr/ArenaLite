# ArenaLite API Overview

ArenaLite exposes a simple, manager-based API you can use inside other plugins. Grab the singleton and query the managers you need.

```java
ArenaLite plugin = ArenaLite.getInstance();
```

## ArenaManager

```java
ArenaManager arenaManager = plugin.getArenaManager();

Arena arena = arenaManager.getArena("desert");
Set<String> names = arenaManager.getArenaNames();

if (arena != null && arena.isInRegion(player.getLocation())) {
    // Player is inside the arena region
}
```

Key methods:

- `getArena(String)` / `getArenaNames()`
- `loadArenas()` / `saveArenas()`

Each `Arena` exposes:

- Region (`getPos1()`, `getPos2()`, `isInRegion(Location)`)
- Spawn (`getSpawn()`)
- Settings (`isRegenEnabled()`, `setBuildEnabled(boolean)`, etc.)

## KitManager

```java
KitManager kitManager = plugin.getKitManager();

Kit kit = kitManager.getKit("diamond");
Set<String> kits = kitManager.getKitNames();

if (kit != null) {
    kitManager.applyKit(player, kit);
}
```

Utilities:

- `createKit(String)` / `deleteKit(String)`
- `populateKitFromPlayer(Kit, Player)`
- `applyKit(Player, Kit)`
- `openPreview(Player, Kit)` – honours configuration flags for previews.

Kits store a slot-perfect copy of a player’s storage contents plus armour and off-hand items.

## StatsManager

```java
StatsManager stats = plugin.getStatsManager();

PlayerData data = stats.getPlayerData(player);
data.addKill();
stats.savePlayerData(data);  // async
```

`PlayerData` exposes current arena context and persistent statistics (kills, deaths, streak). Saving is performed asynchronously via `CompletableFuture`.

## Hooks

- `getFAWEHook()` – present when FAWE is installed.
- `getPlaceholderAPIHook()` – present when PlaceholderAPI is installed.
- `getVaultHook()` – present when Vault is installed.

Always null-check hooks before use.

```java
FAWEHook fawe = plugin.getFAWEHook();
if (fawe != null && fawe.isAvailable()) {
    fawe.regenArena(arena);
}
```

## Storage Layer

Player data storage is abstracted behind `PlayerDataStorage`. You can inspect the active mode if needed:

```java
PlayerDataStorage storage = plugin.getPlayerDataStorage();
// storage.reload() will re-read configuration settings
```

## Reload Semantics

`ArenaLite#reload()` reloads configuration, arenas, kits, and propagates changes to the storage layer. If you hook into ArenaLite, listen for configuration changes by re-querying the managers after reload.

