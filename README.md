# Quick Stack
A simple stack implementation in C  
just kidding, github copilot generated that

A Spigot plugin for Minecraft that tries to mimic Terraria's *"quick stack to nearby chests"* button to help Minecraft get one step closer to being as good as Terraria. (magic mirror next pls)

## Usage
Put the .jar file in your plugins folder first.
### Configuring range
You will need the permission called `quickstack.range`, or operator privileges.  
To set the range, use the command `/qs range <value>`. The value is in blocks, and the default is 8.  
The plugin will use this value as the radius of a ~~sphere~~ cube around the player, and will try to find **regular** chests (or barrels) within. Neither trapped nor ender chests work as of now.
### Excluding types of items
You don't need any permissions to do this, because it's just a personal setting that only matters when the quick stacking feature is used.  
- To toggle exclusion of a type of item (that's how far the plugin's differentiation between items logic goes), use the command `/qs exclude <item>`. Provide the item's ID as it appears in any regular command, for example `minecraft:stone` or `minecraft:iron_ingot`. You may skip the `minecraft:` part if you want.  
- To exclude the type of item you're currently holding, use `/qs exclude this`.  
- To view the list of the types of items you've excluded so far, use `/qs exclude list`.  
- To clear the list of excluded items, use `/qs exclude clear`.

It ignores your hotbar, armor, and offhand slots by default, so you don't have to worry about accidentally quick stacking your sword or something.

### Using the main feature
You will need the permission called `quickstack.use`, or operator privileges.  
Just type `/qs` or `/quickstack` to quick stack your inventory to nearby chests. If you hear a sound, it probably worked.

I don't take responsibility for any lost items, but I'm pretty sure it's safe *(=/= efficient)*.  
**Please send me a pull request if you'd like to help!** (I am not a Java programmer)
