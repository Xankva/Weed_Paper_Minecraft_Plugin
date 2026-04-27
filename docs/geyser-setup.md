# Geyser Setup Guide

This guide covers getting WeedPlugin custom textures working for **Bedrock clients** connecting via [Geyser](https://geysermc.org/).

## How it Works

Geyser supports custom item mappings — you tell it "when a Java client sees item X with CustomModelData Y, show Bedrock clients custom item Z instead." WeedPlugin provides the mapping file and a Bedrock-compatible resource pack.

## Files Needed

From the [Releases](../../releases/latest) page, download `WeedPlugin-GeyserFiles-v1.0.0.zip`. Extract it — you'll find:

```
custom_mappings/
  weedplugin.json          ← Geyser item mapping file
packs/
  WeedPlugin/              ← Bedrock resource pack folder
    manifest.json
    textures/
      item_texture.json
      items/
        *.png              ← All 6 item textures
```

## Installation

**1. Copy the mapping file:**
```
plugins/Geyser-Spigot/custom_mappings/weedplugin.json
```

**2. Copy the resource pack:**
```
plugins/Geyser-Spigot/packs/WeedPlugin/   ← entire folder
```

Your Geyser folder should look like:
```
plugins/Geyser-Spigot/
├── config.yml
├── custom_mappings/
│   └── weedplugin.json
└── packs/
    └── WeedPlugin/
        ├── manifest.json
        └── textures/
            ├── item_texture.json
            └── items/
                ├── weed_seed.png
                ├── cannabis_bud.png
                ├── premium_bud.png
                ├── joint.png
                ├── premium_joint.png
                └── edible.png
```

**3. Restart the server.**

Geyser will automatically send the resource pack to connecting Bedrock clients.

## Verifying It Works

- Join with a Bedrock client
- Run `/weed give <yourself> seed`
- The item should show the custom weed seed texture instead of vanilla wheat seeds

## Troubleshooting

**Textures not showing on Bedrock:**
- Make sure Geyser is version 2.2.0 or newer (custom mappings support)
- Check `plugins/Geyser-Spigot/logs/` for any pack loading errors
- Confirm `packs/WeedPlugin/manifest.json` exists and is valid JSON

**Items showing as vanilla:**
- Confirm `custom_mappings/weedplugin.json` is in the right place
- Restart the server fully (not just `/geyser reload`)
- Check that the Java resource pack is also installed (Geyser maps Java→Bedrock, but needs the Java CMD values to be set by the plugin)
