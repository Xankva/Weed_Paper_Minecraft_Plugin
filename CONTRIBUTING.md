# Contributing to WeedPlugin

Thanks for your interest in contributing! Here's how to get started.

## Setting Up

```bash
git clone https://github.com/yourname/WeedPlugin.git
cd WeedPlugin
mvn package
```

You'll need **Java 21** and **Maven 3.8+**.

For testing, drop the built jar into a local Paper 1.21.3+ server.

## Making Changes

- Open an issue before starting large changes so we can discuss approach
- Keep PRs focused — one feature or fix per PR
- Follow the existing code style (4-space indent, no wildcard imports)
- Test your changes on a real Paper server before submitting

## Reporting Bugs

Please include:
- Paper version (`/version`)
- WeedPlugin version
- What you did, what you expected, what happened
- Any console errors (`latest.log`)

## Code Structure

| Class | Responsibility |
|---|---|
| `WeedPlugin` | Plugin entry point, registers everything |
| `ConfigManager` | All config reads in one place |
| `ItemManager` | Creates custom ItemStacks with PDC tags and item_model |
| `GrowthManager` | Tracks plant locations and growth stages in memory |
| `PlantListener` | Handles planting (right-click soil) and harvesting (block break) |
| `CropGrowthListener` | Cancels vanilla wheat physics, handles bonemeal |
| `ItemUseListener` | Rolling joints, smoking, eating edibles |
| `WeedCommand` | `/weed` command and tab completion |

## Key Design Decisions

**In-memory plant tracking** — `GrowthManager` stores plant data in a `HashMap` keyed by `worldUUID,x,y,z`. Plants reset on server restart. This is intentional — simpler and avoids stale-data bugs.

**Adventure API throughout** — all messages use `LegacyComponentSerializer` to convert `§`-coded strings from config into proper Adventure `Component` objects. This ensures correct rendering in 1.21+.

**`setItemModel()`** — items use the `minecraft:item_model` component via Paper's `meta.setItemModel(NamespacedKey)` API instead of CustomModelData. This means items have their own identity (`weedplugin:joint`) rather than piggy-backing on vanilla items.

**OFF_HAND deduplication** — Paper fires `PlayerInteractEvent` once per hand. `ItemUseListener` immediately cancels and returns on `EquipmentSlot.OFF_HAND` to prevent double-processing.
