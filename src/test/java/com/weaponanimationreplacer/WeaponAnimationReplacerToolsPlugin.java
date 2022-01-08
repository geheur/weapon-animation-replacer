package com.weaponanimationreplacer;

import java.awt.image.BufferedImage;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Player;
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

	int demoanim = -1;
	int demogfx = -1;

	@Subscribe
	public void onCommandExecuted(CommandExecuted commandExecuted) {
		String[] arguments = commandExecuted.getArguments();
		String command = commandExecuted.getCommand();

		if (command.equals("reload")) {
			System.out.println("reloading animations sets");
			AnimationSet.loadAnimationSets();
		}

		if (command.equals("demoanim")) {
			demoanim = Integer.parseInt(arguments[0]);
		}

		if (command.equals("demogfx")) {
			demogfx = Integer.parseInt(arguments[0]);
		}

		if (command.equals("poseanims")) {
			String name = arguments[0];
			for (Player player : client.getPlayers())
			{
				if (player.getName().equals(name)) {
					for (Constants.ActorAnimation value : Constants.ActorAnimation.values())
					{
						System.out.println(value + " " + value.getAnimation(player));
					}
				}
			}
			for (Constants.ActorAnimation value : Constants.ActorAnimation.values())
			{
				System.out.println(value + " " + value.getAnimation(client.getLocalPlayer()));
			}
		}

		if (command.equals("itemicons")) {
			for (Integer integer : Constants.EQUIPPABLE_ITEMS_NOT_MARKED_AS_EQUIPPABLE.keySet())
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

	@Subscribe
	public void onClientTick(ClientTick clientTick) {
		if (demoanim != -1) {
			client.getLocalPlayer().setAnimation(demoanim);
			client.getLocalPlayer().setAnimationFrame(0);
		}
		if (demogfx != -1) {
			client.getLocalPlayer().setGraphic(demogfx);
			client.getLocalPlayer().setSpotAnimFrame(0);
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked)
	{
		if (menuOptionClicked.getMenuOption().equals("Wield")) {
			if (demoanim != -1) {
				demoanim--;
				System.out.println("demo anim " + demoanim);
			}
			if (demogfx != -1) {
				demogfx--;
				System.out.println("demo gfx " + demogfx);
			}
		} else if (menuOptionClicked.getMenuOption().equals("Use")){
			if (demoanim != -1) {
				demoanim++;
				System.out.println("demo anim " + demoanim);
			}
			if (demogfx != -1) {
				demogfx++;
				System.out.println("demo gfx " + demogfx);
			}
		}
		System.out.println(menuOptionClicked.getMenuOption() + " " + Text.removeTags(menuOptionClicked.getMenuTarget()));
	}

}
