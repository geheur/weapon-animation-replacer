package com.weaponanimationreplacer;

import java.awt.image.BufferedImage;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = "[Tools] Weapon/Gear/Anim Replacer",
	description = "",
	tags = {"transmog", "fashionscape"}
)
@PluginDependency(WeaponAnimationReplacerPlugin.class)
public class WeaponAnimationReplacerToolsPlugin extends Plugin
{

	@Inject
	private WeaponAnimationReplacerPlugin plugin;

	@Inject
	private ItemManager itemManager;

	@Subscribe
	public void onCommandExecuted(CommandExecuted commandExecuted) {
		if ("u".equals(commandExecuted.getCommand())) {
			ProjectileCast.initializeSpells();
		}
		if ("plugintest".equals(commandExecuted.getCommand())) {
			System.out.println("test " + plugin);
		}

		if (commandExecuted.getCommand().equals("itemicons")) {
			for (Integer integer : Constants.EQUIPPABLE_ITEMS_NOT_MARKED_AS_EQUIPMENT.keySet())
			{
				System.out.println("here " + integer);
				BufferedImage image = itemManager.getImage(integer);
				int transparent = 0;
				for (int y = 0; y < image.getHeight(); y++) {
					for (int x = 0; x < image.getWidth(); x++) {
						int  clr   = image.getRGB(x, y);
						int  red   = (clr & 0x00ff0000) >> 16;
						int  green = (clr & 0x0000ff00) >> 8;
						int  blue  =  clr & 0x000000ff;
						int  alpha  =  (clr & 0xff000000) >> 24;
//						System.out.println("\t" + alpha + " " + red + " " + green + " " + blue);
						if (alpha == 0) {

							transparent++;
						}
						image.setRGB(x, y, clr);
					}
				}
				if (transparent > 15 * image.getHeight() * image.getWidth() / 16) {

					System.out.println("alpha high on item " + integer);
				}
			}
		}
	}

	@Inject
	private Client client;

	int graphic = 0;

	@Subscribe
	public void onClientTick(ClientTick clientTick) {
//		if (graphic > 350) graphic = 350;
		if (client.getLocalPlayer().getGraphic() != graphic)
		{
			client.getLocalPlayer().setGraphic(graphic);
			client.getLocalPlayer().setSpotAnimFrame(0);
		}
//		client.getLocalPlayer().setAnimation(8000);
//		client.getLocalPlayer().setAnimationFrame(20);
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked)
	{
		if (menuOptionClicked.getMenuOption().equals("Wield")) {
			graphic--;
			System.out.println("graphic is now " + graphic);
		} else if (menuOptionClicked.getMenuOption().equals("Use")){
			graphic++;
			System.out.println("graphic is now " + graphic);
		}
		System.out.println(menuOptionClicked.getMenuOption() + " " + Text.removeTags(menuOptionClicked.getMenuTarget()));
	}

}
