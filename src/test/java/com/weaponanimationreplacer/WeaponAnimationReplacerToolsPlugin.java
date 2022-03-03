package com.weaponanimationreplacer;

import static com.weaponanimationreplacer.Constants.ActorAnimation.IDLE;
import static com.weaponanimationreplacer.Constants.ActorAnimation.IDLE_ROTATE_LEFT;
import static com.weaponanimationreplacer.Constants.ActorAnimation.IDLE_ROTATE_RIGHT;
import static com.weaponanimationreplacer.Constants.ActorAnimation.RUN;
import static com.weaponanimationreplacer.Constants.ActorAnimation.WALK;
import static com.weaponanimationreplacer.Constants.ActorAnimation.WALK_ROTATE_180;
import static com.weaponanimationreplacer.Constants.ActorAnimation.WALK_ROTATE_LEFT;
import static com.weaponanimationreplacer.Constants.ActorAnimation.WALK_ROTATE_RIGHT;
import static com.weaponanimationreplacer.Constants.ActorAnimation.values;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Projectile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;
import net.runelite.http.api.item.ItemEquipmentStats;
import net.runelite.http.api.item.ItemStats;

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
	public void onProjectileMoved(ProjectileMoved projectileMoved) {
		Projectile projectile = projectileMoved.getProjectile();

		// skip already seen projectiles.
		if (client.getGameCycle() >= projectile.getStartCycle()) {
			return;
		}

		for (Player clientPlayer : client.getPlayers())
		{
			final WorldPoint playerPos = clientPlayer.getWorldLocation();
			if (playerPos == null)
			{
				return;
			}

			final LocalPoint playerPosLocal = LocalPoint.fromWorld(client, playerPos);
			if (playerPosLocal == null)
			{
				return;
			}

			if (projectile.getX1() == playerPosLocal.getX() && projectile.getY1() == playerPosLocal.getY())
			{
				System.out.println(
					clientPlayer.getAnimation() + ", " +
						clientPlayer.getGraphic() + ", " +
						projectile.getId() + ", " +
						(clientPlayer.getInteracting() != null ? clientPlayer.getInteracting().getGraphic() : "-1") + ", " +
						(projectile.getStartCycle() - client.getGameCycle()) + ", " +
						projectile.getStartHeight() + ", " +
						projectile.getEndHeight() + ", " +
						projectile.getSlope() + ", " +
						(clientPlayer.getInteracting() != null ? clientPlayer.getInteracting().getGraphicHeight() : "-1")
				);
			}
		}
	}

	@Subscribe
	public void onCommandExecuted(CommandExecuted commandExecuted) {
		String[] arguments = commandExecuted.getArguments();
		String command = commandExecuted.getCommand();
		System.out.println(arguments.length);

		if (command.equals("reload")) {
			AnimationSet.loadAnimationSets();
			ProjectileCast.initializeSpells();
			SwingUtilities.invokeLater(plugin.pluginPanel::rebuild);
			Constants.loadEquippableItemsNotMarkedAsEquippable();
			System.out.println("reloaded animations sets and projectiles");
		}

		if (command.equals("listanimsets")) {
			System.out.println("transmog sets: " + plugin.getTransmogSets());
			System.out.println("applicable: " + plugin.getApplicableModelSwaps());
		}

		if (command.equals("listweapons")) {
			Map<Integer, Integer> map = new HashMap<>();
			for (int i = 0; i < client.getItemCount(); i++)
			{
				ItemStats itemStats = itemManager.getItemStats(i, false);
				if (itemStats == null) continue;
				ItemEquipmentStats equipment = itemStats.getEquipment();
				if (equipment == null) continue;
				int slot = equipment.getSlot();
				map.put(slot, map.getOrDefault(slot, 0) + 1);
			}
			for (Map.Entry<Integer, Integer> integerIntegerEntry : map.entrySet())
			{

				System.out.println(integerIntegerEntry.getKey() + " " + integerIntegerEntry.getValue());
			}
			System.out.println("reloading animations sets");
			AnimationSet.loadAnimationSets();
		}

		if (command.equals("demo")) {
			int demoanim = Integer.parseInt(arguments[0]);
			for (Constants.ActorAnimation value : values())
			{
				value.setAnimation(client.getLocalPlayer(), demoanim);
			}
		}

		if (command.equals("stand")) {
			int demoanim = Integer.parseInt(arguments[0]);
			for (Constants.ActorAnimation value : Arrays.asList(IDLE, IDLE_ROTATE_LEFT, IDLE_ROTATE_RIGHT))
			{
				value.setAnimation(client.getLocalPlayer(), demoanim);
			}
		}

		if (command.equals("move")) {
			int demoanim = Integer.parseInt(arguments[0]);
			for (Constants.ActorAnimation value : Arrays.asList(WALK, WALK_ROTATE_180, WALK_ROTATE_LEFT, WALK_ROTATE_RIGHT, RUN))
			{
				value.setAnimation(client.getLocalPlayer(), demoanim);
			}
		}

		if (command.equals("demoanim")) {
			demoanim = Integer.parseInt(arguments[0]);
			for (Constants.ActorAnimation value : values())
			{
				value.setAnimation(client.getLocalPlayer(), demoanim);
			}
		}

		if (command.equals("demogfx")) {
			demogfx = Integer.parseInt(arguments[0]);
			client.getLocalPlayer().setSpotAnimFrame(0);
		}

		if (command.equals("testthing")) {
			client.getLocalPlayer().setAnimation(713);
			client.getLocalPlayer().setAnimationFrame(Integer.parseInt(arguments[0]));
		}

		if (command.equals("pose")) {

		}

		if (command.equals("poseanims")) {
			String name = arguments.length == 0 ? client.getLocalPlayer().getName() : String.join(" ", arguments);
			System.out.println("pose anims for player " + name);
			for (Player player : client.getPlayers())
			{
				if (player.getName().toLowerCase().equals(name))
				{
					for (Constants.ActorAnimation value : values())
					{
						System.out.println(value.getType() + ", " + value.getAnimation(player) + ",");
					}
				}
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
//		for (int i = 0; i < Math.min(100, client.getLocalPlayer().getModel().getFaceColors1().length); i++)
//		for (int i = 0; i < client.getLocalPlayer().getModel().getFaceColors1().length; i++)
//		{
//			client.getLocalPlayer().getModel().getFaceColors1()[i] = 0;
//		}
		if (demoanim != -1) {
//			client.getLocalPlayer().setAnimation(demoanim);
//			client.getLocalPlayer().setAnimationFrame(0);
		}
		if (demogfx != -1 && client.getLocalPlayer().getGraphic() != demogfx) {
			client.getLocalPlayer().setGraphic(demogfx);
			client.getLocalPlayer().setSpotAnimFrame(0);
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked)
	{
		if (menuOptionClicked.getMenuOption().equals("Use") && menuOptionClicked.getId() == 563) {
			if (demoanim != -1) {
				demoanim--;
				for (Constants.ActorAnimation value : values())
				{
					value.setAnimation(client.getLocalPlayer(), demoanim);
				}
				System.out.println("demo anim " + demoanim);
			}
			if (demogfx != -1) {
				demogfx--;
				client.getLocalPlayer().setGraphic(demogfx);
				client.getLocalPlayer().setSpotAnimFrame(0);
				client.getLocalPlayer().setGraphicHeight(0);
				System.out.println("demo gfx " + demogfx);
			}
		} else if (menuOptionClicked.getMenuOption().equals("Use") && menuOptionClicked.getId() == 995){
			if (demoanim != -1) {
				demoanim++;
				for (Constants.ActorAnimation value : values())
				{
					value.setAnimation(client.getLocalPlayer(), demoanim);
				}
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
