# MysteryDrops
### Minecraft plugin that mimics SethBling's datapack

My first public, open source plugin
  
### What is Mystery Drops?
  During the summer of 2019, SethBling created a [Minecraft datapack that everyone on YouTube went crazy over.](https://www.youtube.com/watch?v=3JEXAZOrykQ)
  
  Basically, the datapack featured a "drop table randomization", which means that all blocks drop a different block, that is randomly assigned.
  
  So, why re-create this when SethBling already made it?

  Well, SethBling's randomizer relies on external tools, which means it requires the user to **manually re-randomize the drop table**.
  
  The one downside to SethBling's approach is the main focus that this plugin improves on.

### Features:
  - Automatic randomization of drop tables (blocks AND mob drops too!)

  - Auto loot-table **re-randomization** (configurable to do once per day, at a specified time OR able to be done with */regenerate*)
  
  - New command */blocks* enables the user to see which blocks he has already discovered (or blocks the whole server has discovered, configurable)

  - MySQL integration with player-discovered blocks to further create a web-based stat tracker or leaderboard (or anything else you would like to do with the MySQL table), not necessary for */blocks* to function
  
  - Configurable item blacklist
  
  - Lightweight resource usage due to many optimizations

### Commands:

*/regenerate*
- Regenerates loot table on-the-fly
- Permission: MysteryDrops.admin

*/blocks*
- Opens a GUI that shows all blocks discovered by either individual player or all players on server (able to be set or be disabled altogether in config.yml)
- Permission: MysteryDrops.basic 

### Planned features:
  - Multi-world support
  
  - Competitive achievement system (for number of discovered blocks)
  
  - Replayability features (for example, removing tools from randomly being dropped. Tools are achieved by completing a random server-generated objective, such as "destroy X block" or "discover 14 new blocks"

### Problems with this plugin:
  Since all blocks are randomized, that feature innately **kills** replayability. What if a player gets a diamond pickaxe from a dirt block? A beacon from a leaf block? The problem is that after a while, players get sick of the staleness and there is no valuable currency, since every item has an equal chance of being dropped (and if the config is set to re-randomize daily, the chances of naturally rare drops dropping skyrockets). I am attempting to resolve this problem with the features I mentioned in the planned features section.
