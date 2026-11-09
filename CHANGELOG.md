# Changelog

All versions below are for Minecraft 1.21.1 / NeoForge.

## v1.0.0 | Full Release

- Added Native EMI And Roughly Enough Items (REI) Integrations Alongside JEI
- Removed The Standalone Trade Browser Screen, V Keybind, And Extra Trade-Browser Buttons
- Kept Trade Browsing Inside Each Recipe Viewer, With Searchable Inputs And Outputs, Profession Names, Job Sites, And Villager Previews
- Updated All 143 Locale Files For The 10 Remaining Viewer And Configuration Translation Keys
- Added Build-Time Translation Key And Placeholder Validation
- Matched Villager And Recipe-Viewer Trade Ordering: Cheapest To Most Expensive By Total Coin Value, Preserving Equal-Price Item Order
- Applied The Same Ordering When Existing Villagers Load Or Their Trading Screen Opens
- Verified NeoForge 21.1.241 Compatibility So Both Tempo Test Packs Can Use The Same Build
- Redesigned The Miner And Zombie Miner Outfits With Slate-Blue Work Shirts, Charcoal Overalls, Tan Suspenders, Utility Pockets, And Dark Boots On The Original 64x64 Pixel Grid
- Kept The Gray Miner Hat And Lamp And Restored Visible Miner And Zombie Miner Eyes When Resource Packs Replace The Base Face Texture
- Published The Public GitHub Source Repository Under The MIT License
- Included The Mod And NeoForge Template License Notices In The JAR
- Made Build Dependencies Download Automatically Without Local Modpack JARs
- Added Optional Villagers Reborn Compatibility For Human NPC Models And World-Themed Skins In Recipe-Viewer Previews
- Removed The Trade Browser Button From Villager Trading Screens
- Rebalanced All Professions With Smaller Price Steps And Reduced 286 Overpriced Offers
- Priced Finished Armor And Tools Around Twice One Unit Of Their Material Instead Of Charging For Every Crafting Ingredient
- Replaced Large Coin Stacks With Clean Price Tiers And The Largest Exact Coin Denomination
- Ordered JEI Profession Trade Lists From Cheap To Expensive, Keeping The Existing Item Order Within Each Price Tier
- Grouped All JEI Trades Into One Page Per Villager Profession, With A Villager Preview And Job Site
- Added Mouse Wheel Scrolling And A Draggable Scrollbar For Long Profession Trade Lists
- Kept Every Trade Searchable In JEI And Automatically Scrolled To Matching Items
- Fixed Missing JEI Trade Listings When Create: New World Core Hides JER's Villager Category
- Published Coin Trades To The Shared Villager Trade Table For Trade Viewer Compatibility
- Matched Recipe-Viewer Listings To Actual Available Offers, Including Enchantments And Optional Mod Items
- Added Profession Job Sites To Recipe-Viewer Trade Listings

## v0.0.3 | Balance Update
- Added Nether Wart To Cleric Trades For One Cog Each
- Removed Seven Simply Light Trades Not Featured In Create: New World v4.1
- Rebalanced Equipment, Crafting Materials, String, And Wool Against Crafting Costs And Item Sink Payouts
- Matched Quartz And Redstone Prices Across Professions
- Added Respiration II, Thorns II, Depth Strider II, And Fortune II Librarian Trades
- Kept Every Purchase To One Coin Type, With One Book Added For Librarian Trades
- Preserved All Create: Numismatics Coin Exchange Rates
- Corrected Trade Selection Counts And Added Automated Trade Validation

## v0.0.2 | Full Release
- Added Miner Villager
- Added New Trades For Miner Villager
- Fixed Zombie Miner Villager Texture
- Restored The Original Minecraft Villager Trade UI Buttons When Amendments Is Installed
