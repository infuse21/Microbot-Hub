# Simple Mining

Simple Mining separates two jobs that are often mixed together:

- **Ladders** follows a compact, fast-XP route: copper/tin to 15, iron to 45, then granite on members worlds. Free worlds remain on iron.
- **Manual** exposes the useful rock catalogue: copper/tin, clay and Trahaearn soft clay, iron, silver, coal, gold, granite, Shilo gem rocks, mithril, adamantite, Weiss salts and basalt, runite, and amethyst. Slower ores remain manual-only and never dilute the fast-XP ladder.

The sidebar mirrors Simple Fishing with side-by-side members/free ladders, a compact manual catalogue, unlock reasons, location pinning, live XP rate, and start/pause/stop controls. Manual rock cards stay compact until selected; the selected card expands into stacked, wrapping location rows designed for RuneLite's narrow sidebar.

Manual locations offer both **Nearest accessible**, which chooses from the audited preset routes, and **Current area**, which remembers the player's starting tile and mines matching rocks nearby. Current area returns to that anchor after banking and stops with a clear message if the selected rock is not present.

## Notes

- Granite offers the 34-rock Desert Quarry, the five-rock Necropolis mine after Beneath Cursed Sands, and the heat-free nine-rock Cape Conch mine after Troubled Tortugans with 45 Sailing. Use **Drop mined items** for the intended XP rate.
- At desert-heat granite locations, setup automatically prefers a worn Desert amulet 4, then a charged circlet of water, then three charged waterskins. The script returns to a bank before continuing when consumable protection runs out and stops safely if none is available.
- Shilo Village gem rocks require completion of Shilo Village.
- Trahaearn Mine requires Song of the Elves and is available for iron, silver, coal, gold, mithril, adamantite, runite, and level-70 soft clay. Its deposit minecart and crystal-shard rolls make it the main high-unlock P2P standard mine.
- Weiss salts and basalt require level 72 Mining and completion of Making Friends with My Arm. Urt, efh, and te salt have separate manual cards so the requested recipe material is mined deliberately; basalt is separate because Snowflake can note it.
- Runite and amethyst use the members section of the Mining Guild.
- F2P choices include Lumbridge swamp, Varrock, Rimmington, Al Kharid, Barbarian Village, the Dwarven Mine, west Lumbridge swamp, and the Mining Guild after level 60.
- P2P choices add West Falador, Lovakengj's iron triangle, the Mining Guild members' area, Lovakite Mine coal, Mor Ul Rek silver/gold, North Brimhaven gold after Shilo Village, Trahaearn Mine, and the Weiss Salt Mine.
- West Falador's short bank route uses the level-5 Agility crumbled-wall shortcut. Mor Ul Rek still requires fire-cape access.
- Mining Guild location buttons stay locked until level 60. Specialist Arzinian gold is not advertised because it needs gold-helmet entry and Dwarven Boatman banking rather than the plugin's normal walk-to-bank loop.
- Volcanic ash, ancient essence, daeyalt, Lovakite ore, and Sailing ores are deliberately excluded until their route switching or specialist processing can be handled cleanly.
- Banking is the default for money rocks. Dropping only removes the selected stage's mined output and preserves pickaxes, waterskins, and gem bags.
