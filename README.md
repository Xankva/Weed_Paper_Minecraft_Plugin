# 🌿 WeedPlugin

A fun cannabis growing plugin for **Paper 1.21.3+** servers. Grow, harvest, roll, and smoke — fully configurable with custom textures.

![Version](https://img.shields.io/badge/version-1.0.0-brightgreen)
![Paper](https://img.shields.io/badge/Paper-1.21.3%2B-blue)
![License](https://img.shields.io/badge/license-MIT-green)

---

## ✨ Features

- 🌱 **Custom crop system** — 4 growth stages with pixel-art textures, independent of vanilla wheat
- 🌿 **Harvest drops** — seeds + buds, with a configurable chance for rare **Premium Buds**
- 🚬 **Roll joints** — right-click a bud with **Paper** in your inventory
- 💨 **Smoking animation** — multi-puff particle sequence when smoking
- 🍫 **Space Brownies** — edibles with a delayed kick-in effect
- ⚗️ **Fully configurable effects** — edit any potion effect, duration, and amplifier in `config.yml`
- 🎨 **Custom resource pack** — unique textures for all items and plant stages
- 🔒 **Permission-based** — control who can grow, use, and give items
- 🔄 **Bonemeal support** — speed up plant growth
- 🌍 **World restrictions** — disable growing in specific worlds

---

## 📋 Requirements

| Requirement | Version |
|---|---|
| Java | 21+ |
| Paper / Folia | 1.21.3+ |
| Geyser (optional) | Any recent build |

> ⚠️ Does **not** work on Spigot or CraftBukkit — Paper-specific APIs are used.

---

## 🚀 Installation

### Plugin
1. Download `WeedPlugin-v1.0.0.jar` from [Releases](../../releases/latest)
2. Drop it into your server's `/plugins/` folder
3. Restart the server
4. Edit `plugins/WeedPlugin/config.yml` to your liking
5. Run `/weed reload` to apply changes without restarting

### Resource Pack (Java clients)
1. Download `WeedPlugin-ResourcePack-v1.0.0.zip` from [Releases](../../releases/latest)
2. Host it somewhere publicly accessible (GitHub Releases works great)
3. Add to your `server.properties`:
```properties
resource-pack=https://github.com/Xankva/Weed_Paper_Minecraft_Plugin/raw/refs/heads/main/Texture%20Pack/WeedPlugin-ResourcePack-v1.0.0.zip
resource-pack-sha1=889a0422d4d8f1c2f4b3e5eee18eca28e3620ce4
resource-pack-prompt=§aWeedPlugin textures — accept for custom visuals!
```

### Geyser (Bedrock clients)
See the [Geyser Setup Guide](docs/geyser-setup.md) for Bedrock client texture support.

---

## 🎮 How to Play

### Growing
```
1. Get a Weed Seed  (/weed give <you> seed)
2. Right-click on Farmland, Dirt, or Grass to plant
3. Wait for 4 growth stages  (~2 min each by default)
4. Break the fully-grown plant to harvest
```

### Crafting
```
Cannabis Bud + Paper (in inventory) → right-click bud → Joint
Premium Bud + Paper (in inventory)  → right-click bud → Premium Joint
```

### Consuming
```
Joint         → right-click to smoke → 30s effects
Premium Joint → right-click to smoke → 60s stronger effects
Space Brownie → right-click to eat  → kicks in after 10s, lasts 120s
```

---

## 🧪 Items

| Item | How to Get | Use |
|---|---|---|
| 🌱 Weed Seed | `/weed give`, harvest | Plant on soil |
| 🌿 Cannabis Bud | Harvest (common) | Roll a Joint (needs Paper) |
| ✨ Premium Bud | Harvest (15% chance) | Roll a Premium Joint (needs Paper) |
| 🚬 Joint | Roll from Bud | Right-click to smoke |
| ✨ Premium Joint | Roll from Premium Bud | Right-click to smoke (stronger) |
| 🍫 Space Brownie | `/weed give` | Right-click to eat (delayed effect) |

---

## ⚗️ Effects

### Joint (30s)
| Effect | Level |
|---|---|
| Jump Boost | II |
| Slow Falling | I |
| Regeneration | I |
| Fire Resistance | I |
| Slowness | I |
| Nausea | I |

### Premium Joint (60s)
| Effect | Level |
|---|---|
| Jump Boost | II |
| Slow Falling | II |
| Regeneration | II |
| Absorption | II |
| Fire Resistance | I |
| Resistance | I |
| Nausea | I |

### Space Brownie (120s, 10s delay)
| Effect | Level |
|---|---|
| Jump Boost | II |
| Slow Falling | III |
| Regeneration | III |
| Fire Resistance | I |
| Resistance | II |
| Nausea | I |
| Hunger | II |

All effects are fully configurable in `config.yml`.

---

## 💬 Commands

| Command | Description | Permission |
|---|---|---|
| `/weed give <player> <item> [amount]` | Give weed items | `weedplugin.give` |
| `/weed reload` | Reload config.yml | `weedplugin.admin` |
| `/weed info` | Show plugin stats | Anyone |
| `/cannabis` | Alias for `/weed` | — |
| `/herb` | Alias for `/weed` | — |

**Item names for `/weed give`:**
`seed` · `bud` · `premium_bud` · `joint` · `premium_joint` · `edible`

---

## 🔒 Permissions

| Permission | Default | Description |
|---|---|---|
| `weedplugin.use` | Everyone | Grow plants, smoke, eat edibles |
| `weedplugin.give` | OP | Give items via `/weed give` |
| `weedplugin.admin` | OP | Reload config |

---

## ⚙️ Configuration

Full `config.yml` with all options documented — see [`src/main/resources/config.yml`](src/main/resources/config.yml).

Key settings:

```yaml
settings:
  growth-time-seconds: 120   # seconds per growth stage
  allow-bonemeal: true        # allow bonemeal to speed growth
  valid-soil:                 # blocks you can plant on
    - FARMLAND
    - DIRT
    - GRASS_BLOCK

harvest:
  premium-bud-chance: 0.15   # 15% chance of premium bud
  auto-replant: false         # auto replant on harvest

joint-effects:
  duration-seconds: 30
  effects:
    - type: jump_boost
      amplifier: 1            # 0=I, 1=II, 2=III
```

---

## 🏗️ Building from Source

```bash
git clone https://github.com/yourname/WeedPlugin.git
cd WeedPlugin
mvn package
# Output: target/WeedPlugin-1.0.0.jar
```

**Requirements:** Java 21, Maven 3.8+

---

## 📁 Project Structure

```
WeedPlugin/
├── src/main/java/dev/weedplugin/
│   ├── WeedPlugin.java              # Main plugin class
│   ├── commands/
│   │   └── WeedCommand.java         # /weed command
│   ├── listeners/
│   │   ├── PlantListener.java       # Planting & harvesting
│   │   ├── CropGrowthListener.java  # Growth & bonemeal
│   │   └── ItemUseListener.java     # Smoking, rolling, eating
│   └── managers/
│       ├── ConfigManager.java       # Config wrapper
│       ├── GrowthManager.java       # Plant growth tracking
│       └── ItemManager.java         # Custom item creation
├── src/main/resources/
│   ├── plugin.yml
│   └── config.yml
└── pom.xml
```

---

## 🤝 Contributing

Pull requests are welcome! Please open an issue first to discuss major changes.

1. Fork the repo
2. Create a branch: `git checkout -b feature/my-feature`
3. Commit: `git commit -m 'Add my feature'`
4. Push: `git push origin feature/my-feature`
5. Open a Pull Request

---

## 📜 License

MIT — see [LICENSE](LICENSE) for details.
