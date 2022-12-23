# Custom Advancements
A minecraft mod that gives players creative freedom about advancements

This mod enables players to add their own custom advancements to the game. It functions a little like a datapack but for all created worlds.
It’s especially useful for modpack creators who want to base their modpack on an advancement system.



How to add Custom Advancements:

Simply use an advancement generator like: https://misode.github.io/advancement/ and create your own custom advancement (for information on how advancements work, please read the Minecraft wiki page: https://minecraft.fandom.com/wiki/Advancement/JSON_format)


When you first start up the game with Custom Advancements installed, the mod will generate a folder inside your Minecraft directory called “customadvancements”. Simply place the created “.json” file from the advancement generator inside this directory and start the game.
You can add as many advancements as you want.
For a more structured approach you should add subfolders to the “customadvancements” folder and put all advancements regarding an advancement tab inside the corresponding subfolder.
The mod will create resource locations for the advancements after this scheme:


customadvancements:subfolder_name/json_file_name


or


customadvancements:json_file_name


if you don’t use subfolders.

When creating advancements, you must state the parent advancements resource location in the “parent” field of the advancement or if it is a root advancement, add a background images resource location.

On startup the mod also creates a subfolder inside the “customadvancements” folder called “textures”. If you want to add custom textures as a background image for your advancements, you can do so by placing them inside the “textures” subfolder. The textures must be in the “.png” format.
The mod will create resource locations for the textures after this scheme:


customadvancements:textures/png_file_name


So, if you want to add a custom background image for your Custom Advancement, you must state the correct textures resource location in the “background” field of the advancement you want to add the texture. Note: only root advancements can add a background texture.

Known incompatibilities:

All mods that prevent the loading of all advancements (don’t know why you would combine these with this mod)
If you notice any other incompatibilities, please report them under the issues tab.



Will you port the mod…

…to newer versions of the game?   -->  Yes, probably 1.18

…to older versions of the game?  -->   No, there are a few other mods that add the same functionality.



Currently, planned features:

Add ability to remove certain game advancements by their resource location
Add ability to edit vanilla and modded advancements (Not sure if this is possible)
If you have any suggestions, please report them under the issues tab.



You are allowed to use this mod in your modpack, as long as you credit the author.













