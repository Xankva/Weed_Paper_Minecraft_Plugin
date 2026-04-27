# Resource Pack Guide

## Pack Format

The resource pack targets **Java 1.21.11** (pack_format `94`).

## Structure

```
WeedPlugin-ResourcePack-v1.0.0.zip
├── pack.mcmeta
└── assets/
    ├── minecraft/
    │   ├── blockstates/
    │   │   └── wheat.json          ← Overrides wheat ages 0,2,4,6 → cannabis stages
    │   └── items/                  ← (empty — weedplugin namespace handles items)
    └── weedplugin/
        ├── items/                  ← Item definitions (minecraft:item_model)
        │   ├── weed_seed.json
        │   ├── cannabis_bud.json
        │   ├── premium_bud.json
        │   ├── joint.json
        │   ├── premium_joint.json
        │   └── edible.json
        ├── models/
        │   ├── item/               ← Item models (generated / handheld parents)
        │   └── block/              ← Plant stage block models (cross sprite)
        └── textures/
            ├── item/               ← 32x32 PNG item textures
            └── block/              ← 32x32 PNG plant stage textures
```

## How Items Work (1.21.4+ system)

Items use `minecraft:item_model` component instead of the old `CustomModelData` override system.

The plugin calls `meta.setItemModel(new NamespacedKey(plugin, "joint"))` which sets the item's model key to `weedplugin:joint`. The client then looks up `assets/weedplugin/items/joint.json`:

```json
{
  "model": {
    "type": "minecraft:model",
    "model": "weedplugin:item/joint"
  }
}
```

Which points to `assets/weedplugin/models/item/joint.json`, which references the texture. This chain means the item is completely visually independent from its base material.

## How Plant Stages Work

The plugin uses `WHEAT` blocks at ages `0, 2, 4, 6` for growth stages `0–3`. The resource pack overrides `assets/minecraft/blockstates/wheat.json` to intercept only those ages:

```json
{
  "variants": {
    "age=0": { "model": "weedplugin:block/cannabis_stage0" },
    "age=2": { "model": "weedplugin:block/cannabis_stage1" },
    "age=4": { "model": "weedplugin:block/cannabis_stage2" },
    "age=6": { "model": "weedplugin:block/cannabis_stage3" },
    "age=1": { "model": "minecraft:block/wheat_stage0" },
    ...
  }
}
```

Ages `1, 3, 5, 7` remain vanilla wheat. Since the plugin never sets those ages on cannabis plants, you'll never see vanilla wheat on your crops.

## Installing Client-Side

1. Copy `WeedPlugin-ResourcePack-v1.0.0.zip` to `.minecraft/resourcepacks/`
2. Enable in **Options → Resource Packs**

## Installing Server-Side (recommended)

Host the zip at a public URL, then add to `server.properties`:

```properties
resource-pack=https://example.com/WeedPlugin-ResourcePack-v1.0.0.zip
resource-pack-sha1=<sha1 hash of the zip>
resource-pack-prompt=§aAccept for custom WeedPlugin textures!
```

To get the SHA1 hash:
```bash
sha1sum WeedPlugin-ResourcePack-v1.0.0.zip
```
