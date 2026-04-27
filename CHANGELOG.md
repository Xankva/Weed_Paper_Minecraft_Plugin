# Changelog

All notable changes to WeedPlugin are documented here.

---

## [1.0.0] — 2025

### Added
- Custom cannabis crop with 4 growth stages and pixel-art textures
- 6 custom items: Weed Seed, Cannabis Bud, Premium Bud, Joint, Premium Joint, Space Brownie
- Rolling mechanic — right-click bud with Paper in inventory to craft a joint
- Smoking animation — multi-puff particle sequence with sound effects
- Space Brownie edible with configurable delayed kick-in effect
- Jump Boost II on all joints and edibles by default
- Fire Resistance on all consumables by default
- Premium Bud — rare harvest drop (15% chance) with stronger effects
- Bonemeal support for accelerated growth
- Auto-replant option
- World restrictions — disable growing in specific worlds
- Full Adventure API support — correct color rendering in 1.21+
- Custom resource pack with unique item textures (pack_format 94, Java 1.21.11)
- Geyser mapping support for Bedrock clients
- `/weed give <player> <item> [amount]` admin command
- `/weed reload` — hot-reload config without restart
- `/weed info` — live plugin stats
- Aliases: `/cannabis`, `/herb`
- Permissions: `weedplugin.use`, `weedplugin.give`, `weedplugin.admin`
- BlockPhysicsEvent cancellation — cannabis plants no longer pop off when planted adjacent to each other
- Duplicate event deduplication — OFF_HAND event correctly ignored

### Fixed
- Crops breaking when planted next to each other (vanilla wheat physics)
- Bud not consumed when rolling joint (stale item reference bug)
- Effects not applying (incorrect PotionEffectType lookup for 1.21 Registry API)
- Resource pack version mismatch (wrong pack_format for 1.21.11)
- `/weed` command blocked for all players (command-level permission gate removed)
- Lore rendering as plain text (switched from legacy `setLore` to Adventure `meta.lore()`)
- Paper check counting weed items as vanilla paper (PDC tag filtering added)
