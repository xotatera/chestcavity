# Chest Cavity

A Minecraft mod that gives every living entity a chest cavity — a secondary inventory filled with organs that determine their biological stats. Use a chest opener to access it, swap organs between creatures, and face the consequences.

## NeoForge 1.21.1 Port

This is an unofficial port of [Tigereye504's Chest Cavity](https://github.com/Tigereye504/chestcavity) from Fabric 1.19.2 to **NeoForge 1.21.1**, rewritten in Kotlin.

### Requirements

- Minecraft 1.21.1
- NeoForge 21.1.x
- Kotlin for Forge (KFF) 5.5.0

### What's Ported

- All original organs (hearts, lungs, muscles, intestines, stomachs, spines, ribs, appendix, kidneys, liver, spleen, etc.)
- Chest opener tool and cleavers (wooden through netherite)
- Organ scores and stat effects (health, speed, attack, defense, etc.)
- Organ compatibility and rejection system
- Mob-specific chest cavities (player, undead, ender, nether, boss, golem, creeper, etc.)
- Organ drops from killed mobs (2.5% base chance, +1% per looting level)
- Food crafting (sausages, organ meat, butchered meat)
- Shulker boxes as pseudo-organs
- Venom gland potion effects
- Keybindings for opening your own chest cavity
- All recipes (cleavers, sausages, saltwater organs, salvaging, cooking)

### Changes from Original

- Ported from Fabric + Cardinal Components to NeoForge + Data Attachments
- Java to Kotlin
- Uses NeoForge DataComponents instead of NBT for organ data
- Uses NeoForge DeferredRegister for all registrations
- 1.21.1 API changes (item tags path, ItemStack.save(), attribute modifiers, etc.)

## Gameplay

Use a **chest opener** (iron ingot + lever + stick) to access any mob's chest cavity when they're below half health. Your own cavity can be opened anytime.

Organs determine stats:
- **Hearts** grant max health
- **Muscles** increase attack damage and movement speed
- **Lungs** allow breathing underwater longer
- **Intestines/Stomach** affect food processing
- **Ribs** provide armor
- **Spine** improves attack speed and mining speed
- **Kidneys** filter poisons faster
- **Liver** provides detoxification
- **Appendix** grants luck

Removing organs has consequences. Putting in new ones from other creatures requires compatibility — mismatched organs cause rejection damage.

**Cleavers** are butchering tools that massively boost organ drop rates when used as the killing blow.

## Building

```bash
./build.sh
```

Requires podman and Java 21. The script builds via a container and outputs the jar to `./jars/`.

## License

Apache License 2.0 — see [LICENSE](LICENSE)

## Credits

Original mod by [Tigereye504](https://github.com/Tigereye504/chestcavity)
