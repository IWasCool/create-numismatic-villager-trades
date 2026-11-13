# Create: Numismatic Villager Trades

A Create: Numismatics addon for **Minecraft 1.21.1 / NeoForge** that turns villagers into coin-based merchants. Buy goods and resources with Numismatics coins across all thirteen vanilla trading professions and the added **Miner** profession.

Version **1.0.0** requires **Java 21**, **NeoForge 21.1.241+**, and **Create: Numismatics** with its Create dependency. The development build uses **Numismatics 1.1.0** and **Create 6.0.10**.

## Install

Download `numismatic_villager_trades-neoforge-1.21.1-1.0.0.jar` from the [1.0.0 release](https://github.com/IWasCool/create-numismatic-villager-trades/releases/tag/neoforge-v1.0.0). Place it in the client and server `mods` folders alongside Create and Create: Numismatics. Remove any previous Numismatic Villager Trades JAR before updating, then restart Minecraft.

Choose **JEI**, **EMI**, or **Roughly Enough Items (REI)** to browse trades through a recipe viewer. All three integrations are native and optional; villagers can still trade when no recipe viewer is installed. **Just Enough Resources (JER)**, **Villagers Reborn**, and **Amendments** are also optional. Only trades whose items are installed appear, so optional mod items do not require the entire modpack.

## Trading

Villagers sell items for Numismatics coins instead of buying items from players. The Miner uses an **anvil**, including chipped and damaged anvils, as its job site. Librarian trades require coins and one book, and retain their listed enchantment and level.

Prices use at most seven coins of one denomination, choosing the largest denomination that represents the exact price. Numismatics exchange rates are unchanged. Finished armor and tools normally cost twice one unit of their main material; the shop discount compared with buying every crafting ingredient is intentional.

Actual villagers and all three recipe viewers list each profession's trades from cheapest to most expensive by total coin value. Items retain their order within the same price tier. Existing villagers receive the updated order when they load or when you open their trading screen.

## Recipe viewers and compatibility

Use your recipe viewer's normal recipe and usage controls to open the **Villager Trades** category. Coins and librarian books are indexed as inputs, goods retain their item counts and enchantments, and job sites are shown as workstations.

**JEI** keeps one scrollable page per profession. Scroll with the mouse wheel or drag the scrollbar; recipe and usage searches automatically scroll to matching offers.

**EMI** and **REI** use their native recipe pages, with up to six trades per profession page. Each output and input is searchable, and matching items open the page containing that offer. Pages include a villager preview, job site, and position within the profession's complete trade list. These alternative merchant offers are excluded from automatic crafting/recipe-tree filling.

The dedicated categories stay available when Create: New World Core hides JER's villager category. Coin trades are also published to the shared villager trade table for other trade viewers. The standalone browser screen, V keybind, and extra trade-browser buttons have been removed.

With Villagers Reborn, recipe-viewer previews use its human NPC models and bundled skins, following the world's Modern or Fantasy theme. With Amendments, the vanilla villager trade buttons remain available.

## Configuration

The common config is `config/numismatic_villager_trades-common.toml`:

| Setting | Default | Effect |
| --- | --- | --- |
| `enableCoinTrades` | `true` | Enables coin-based villager trades. |
| `replaceVanillaTrades` | `true` | Replaces vanilla trades for supported professions. |
| `maxUses` | `999` | Sets the default maximum uses per offer. |

Bundled profession trade definitions are in `src/main/resources/data/numismatic_villager_trades/trades/`.

Report problems through [GitHub Issues](https://github.com/IWasCool/create-numismatic-villager-trades/issues).

## Build from source

Use Java 21. On Windows:

```powershell
.\gradlew.bat build
```

On Linux and macOS:

```sh
./gradlew build
```

Dependencies download automatically; local modpack JARs are not needed. The build produces `build/libs/numismatic_villager_trades-1.0.0.jar`.

## Translations

All 143 locale files include the complete set of 10 viewer/configuration keys. Job-site tooltips are localized in 111 non-English locale files; 20 rarer locales use English fallbacks for that tooltip. Regional variants share their base language's labels where appropriate. Removed browser/keybind strings are no longer bundled. Translation corrections are welcome.

Source: [IWasCool/create-numismatic-villager-trades](https://github.com/IWasCool/create-numismatic-villager-trades). Available under the [MIT license](LICENSE). The NeoForge template's original notice is retained in [TEMPLATE_LICENSE.txt](TEMPLATE_LICENSE.txt); both notices are included in the mod JAR.
