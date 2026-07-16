# CubixLevels

A Paper 1.21.4 plugin that adds a leveling system to the Cubix server. Players earn experience (XP) for various in‑game activities to progress through levels.

## Features

### XP Sources

| Activity | Description | XP |
|----------|-------------|:--:|
| ⛏ Mining | Break ores (coal → ancient debris) | 1–10 XP |
| 🌾 Farming | Harvest crops, pumpkins, melons, berries, honey | 0.5–5 XP |
| 🌲 Woodcutting | Chop any log | 0.2 XP |
| 🎣 Fishing | Catch any fish | 5 XP |
| ⚔ Hunting | Kill mobs (all configurable: zombie 3 XP, warden 50 XP, etc.) | 1–50 XP |
| 🚶 Distance | Every 1000 blocks traveled | 5 XP |
| ⏱ Playtime | Every 30 minutes online | 10 XP |
| ☀ Daily Bonus | Once per day via `/cubixlevel daily` | 50 XP |

### Security

- **Natural‑only checks** — XP is only awarded for naturally spawned blocks and mobs (not player‑placed, not from spawners or eggs)
- **Spawner proximity detection** — Mobs near a matching spawner in adjacent chunks are blocked
- **Crop age check** — Only fully grown crops give XP

### Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/cubixlevel` | Show your stats (level, XP, progress bar) | `cubixlevels.user` |
| `/cubixlevel daily` | Claim daily bonus | `cubixlevels.user` |
| `/cubixlevel reload` | Reload config | `cubixlevels.reload` (op) |

### PlaceholderAPI

If PlaceholderAPI is installed, the following placeholders are available:

| Placeholder | Description |
|-------------|-------------|
| `%cubix_level%` | Current level |
| `%cubix_level_xp%` | Current XP towards next level |
| `%cubix_level_xp_needed%` | XP needed for next level |
| `%cubix_level_progress%` | Percentage progress (e.g. `42%`) |
| `%cubix_level_playtime%` | Total playtime (e.g. `3ч 15мин`) |

## Installation

1. Place `CubixLevels-1.0.0.jar` in your server's `plugins/` folder
2. (Optional) Install **PlaceholderAPI** for placeholder support
3. Restart the server or run `/reload`
4. Configure XP values in `plugins/CubixLevels/config.yml`

## Configuration

All XP values are fully configurable in `config.yml`:

```yaml
settings:
  xp-base: 100            # XP needed for level 0→1
  xp-multiplier: 1.5      # Each level needs (base + level × multiplier) XP
  max-level: 100

mining:
  blocks:
    DIAMOND_ORE: 5
    ANCIENT_DEBRIS: 10

hunting:
  mobs:
    WARDEN: 50
    ZOMBIE: 3
```

## Building from source

```bash
cd CubixLevels
./gradlew shadowJar
```

The built JAR will be in `build/libs/CubixLevels-1.0.0.jar`.

## Requirements

- **Server:** Paper 1.21.4 (or fork)
- **Java:** 21+
- **Optional:** PlaceholderAPI 2.11+

## License

This project is licensed under the **GNU Affero General Public License v3.0**.  
See the [LICENSE](./LICENSE) file for details.
