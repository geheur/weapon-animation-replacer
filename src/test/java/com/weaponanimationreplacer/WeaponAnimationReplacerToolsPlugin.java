package com.weaponanimationreplacer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.gson.reflect.TypeToken;
import static com.weaponanimationreplacer.Constants.ARMS_SLOT;
import static com.weaponanimationreplacer.Constants.ActorAnimation.IDLE;
import static com.weaponanimationreplacer.Constants.ActorAnimation.IDLE_ROTATE_LEFT;
import static com.weaponanimationreplacer.Constants.ActorAnimation.IDLE_ROTATE_RIGHT;
import static com.weaponanimationreplacer.Constants.ActorAnimation.RUN;
import static com.weaponanimationreplacer.Constants.ActorAnimation.WALK;
import static com.weaponanimationreplacer.Constants.ActorAnimation.WALK_ROTATE_180;
import static com.weaponanimationreplacer.Constants.ActorAnimation.WALK_ROTATE_LEFT;
import static com.weaponanimationreplacer.Constants.ActorAnimation.WALK_ROTATE_RIGHT;
import static com.weaponanimationreplacer.Constants.ActorAnimation.values;
import static com.weaponanimationreplacer.Constants.HAIR_SLOT;
import static com.weaponanimationreplacer.Constants.HEAD_SLOT;
import static com.weaponanimationreplacer.Constants.JAW_SLOT;
import static com.weaponanimationreplacer.Constants.SLOT_OVERRIDES;
import static com.weaponanimationreplacer.Constants.TORSO_SLOT;
import static com.weaponanimationreplacer.Constants.WEAPON_SLOT;
import static com.weaponanimationreplacer.Constants.mapNegativeId;
import static com.weaponanimationreplacer.ProjectileCast.p;
import java.awt.image.BufferedImage;
import java.io.FileReader;
import java.io.FileWriter;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemID;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.PlayerComposition;
import net.runelite.api.Projectile;
import net.runelite.api.SpriteID;
import net.runelite.api.WorldType;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.PlayerChanged;
import net.runelite.api.events.PlayerSpawned;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.api.kit.KitType;
import static net.runelite.api.kit.KitType.AMULET;
import static net.runelite.api.kit.KitType.CAPE;
import static net.runelite.api.kit.KitType.HANDS;
import static net.runelite.api.kit.KitType.HEAD;
import static net.runelite.api.kit.KitType.LEGS;
import static net.runelite.api.kit.KitType.SHIELD;
import static net.runelite.api.kit.KitType.TORSO;
import static net.runelite.api.kit.KitType.WEAPON;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ClientShutdown;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemVariationMapping;
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
	@Inject private WeaponAnimationReplacerPlugin plugin;
	@Inject private ItemManager itemManager;
	@Inject private ClientThread clientThread;
	@Inject private Client client;

	@Inject
	private EventBus eventBus;

	int demoanim = -1;
	int demogfx = -1;

	private final Set<Integer> hidesArms = new HashSet<>();
	private final Set<Integer> hidesHair = new HashSet<>();
	private final Set<Integer> hidesJaw = new HashSet<>();
	private final Set<Integer> showsArms = new HashSet<>();
	private final Set<Integer> showsHair = new HashSet<>();
	private final Set<Integer> showsJaw = new HashSet<>();
	private final Set<Integer> uhohList = new HashSet<>();
	private Set<Integer>[] sets = new Set[]{hidesArms, hidesHair, hidesJaw, showsArms, showsHair, showsJaw, uhohList};
	private String[] fileNames = new String[]{"hidesArms.json", "hidesHair.json", "hidesJaw.json", "showsArms.json", "showsHair.json", "showsJaw.json", "uhoh.json"};
	private final Map<Integer, Set<List<Integer>>> poseanims = new HashMap<>();

	@Override
	public void startUp()
	{
		SpellDataCollector sdc = new SpellDataCollector(plugin);
		this.getInjector().injectMembers(sdc);
		eventBus.register(sdc);

		for (int i = 0; i < fileNames.length; i++)
		{
			String fileName = fileNames[i];
			System.out.println("reading " + fileName);
			Set<Integer> set = sets[i];
			set.clear();
			try
			{
				FileReader reader = new FileReader(RuneLite.RUNELITE_DIR.toPath().resolve(fileName).toFile());
				set.addAll(plugin.getGson().fromJson(reader, new TypeToken<Set<Integer>>() {}.getType()));
				System.out.println(set);
				reader.close();
			} catch (Exception e) {
				// is ok, set is cleared.
			}
		}
		poseanims.clear();
		try
		{
			FileReader reader = new FileReader(RuneLite.RUNELITE_DIR.toPath().resolve("poseanims.json").toFile());
			poseanims.putAll(plugin.getGson().fromJson(reader, new TypeToken<Map<Integer, Set<List<Integer>>>>() {}.getType()));
			System.out.println(poseanims);
			reader.close();
		} catch (Exception e) {
			// is ok, set is cleared.
		}
	}

	@Override
	public void shutDown() {
		for (int i = 0; i < fileNames.length; i++)
		{
			String fileName = fileNames[i];
			Set<Integer> set = sets[i];
			System.out.println("writing " + fileName + " " + set);
			try
			{
				FileWriter writer = new FileWriter(RuneLite.RUNELITE_DIR.toPath().resolve(fileName).toFile());
				plugin.getGson().toJson(set, writer);
				writer.close();
			} catch (Exception e) {
				e.printStackTrace(System.out);
				System.out.println("exception");
				// is ok, set is cleared.
			}
		}
		try
		{
			FileWriter writer = new FileWriter(RuneLite.RUNELITE_DIR.toPath().resolve("poseanims.json").toFile());
			plugin.getGson().toJson(poseanims, writer);
			writer.close();
		} catch (Exception e) {
			System.out.println("exception");
			// is ok, set is cleared.
		}
	}

	@Subscribe
	public void onClientShutdown(ClientShutdown e) {
		shutDown();
	}

	private ProjectileCast manualSpellCastNoCastGfx = null;

	@Subscribe(priority = 100)
	public void onPlayerSpawned(PlayerSpawned e) {
		if (e.getPlayer() == client.getLocalPlayer()) return; // Sometimes I get wrong data from transmog I have applied to myself.
//		System.out.println("playerSpawned " + e.getPlayer().getName());
		getPlayerData(e.getPlayer());
	}

	@Subscribe(priority = 100)
	public void onPlayerChanged(PlayerChanged e) {
//		System.out.println("playerchanged " + e.getPlayer().getName());
		Player player = e.getPlayer();
		getPlayerData(player);
	}

	private void getPlayerData(Player player)
	{
//		System.out.println(player.getName());
		PlayerComposition comp = player.getPlayerComposition();
		int[] kits = comp.getEquipmentIds();
		int body = kits[TORSO_SLOT] - 512;
		int arms = kits[ARMS_SLOT];
		int head = kits[HEAD_SLOT] - 512;
		int hair = kits[HAIR_SLOT];
		int jaw = kits[JAW_SLOT];
		boolean female = comp.getGender() == 1;
//		System.out.println(plugin.itemName(body) + " " + arms + " " + plugin.itemName(head) + " " + hair + (female ? "" : (" " + jaw)));
		boolean uhoh = false;
		if (body >= 0) {
			boolean b = updateLists(body, arms, hidesArms, showsArms, "arms", female);
			if (b) uhohList.add(body);
			uhoh |= b;
		}
		if (head >= 0) {
			boolean b = updateLists(head, hair, hidesHair, showsHair, "hair", female);
			if (b) uhohList.add(body);
			uhoh |= b;
			if (!female)
			{
				if (jaw < 1000) { // do not record stuff for blue icon smuggle.
					boolean c = updateLists(head, jaw, hidesJaw, showsJaw, "jaw", female);
					if (c) uhohList.add(body);
					uhoh |= c;
				}
			}
		}
		if (uhoh) System.out.println("\tuhoh " + player.getName() + " " + Arrays.stream(kits).boxed().collect(Collectors.toList()) + " " + female);

		int weapon = kits[WEAPON_SLOT] - 512;
		if (weapon >= 0) {
			if (plugin.itemManager.getItemComposition(weapon).isMembers() && !client.getWorldType().contains(WorldType.MEMBERS))
				return;

			int idlePoseAnimation = player.getIdlePoseAnimation();
			if (
				idlePoseAnimation == 8070 ||
					idlePoseAnimation == 763 ||
					idlePoseAnimation == 745 ||
					idlePoseAnimation == 773 ||
					idlePoseAnimation == 765 ||
					idlePoseAnimation == 3418 ||
					idlePoseAnimation == 6998 ||
					idlePoseAnimation == 845
			) {
				return;
			}

			Set<List<Integer>> lists = poseanims.getOrDefault(weapon, new HashSet<>());
//			int stand, int rotate, int walk, int walkBackwards, int shuffleLeft, int shuffleRight, int run) {
			List<Integer> currentPoseanims = Arrays.asList(
				player.getIdlePoseAnimation(),
				player.getIdleRotateRight(),
				player.getWalkAnimation(),
				player.getWalkRotate180(),
				player.getWalkRotateLeft(),
				player.getWalkRotateRight(),
				player.getRunAnimation(),
				player.getIdleRotateLeft()
			);
			if (!lists.contains(currentPoseanims)) {
				System.out.println("not duplicate: " + plugin.itemManager.getItemComposition(weapon).getName() + " " + currentPoseanims);
			}
//			System.out.println("not duplicate: " + plugin.itemManager.getItemComposition(weapon).getName() + " " + currentPoseanims);
			lists.add(currentPoseanims);
			poseanims.put(weapon, lists);
		}
	}

	private boolean updateLists(int body, int arms, Set<Integer> hiddenSet, Set<Integer> shownSet, String name, boolean female)
	{
		boolean hidden = arms == 0;
		boolean uhoh = false;
		Set<Integer> wrongList = !hidden ? hiddenSet : shownSet;
		Set<Integer> rightList = hidden ? hiddenSet : shownSet;
		if (wrongList.contains(body)) {
			System.out.println("(uh oh) " + name + " " + (hidden ? "hidden" : "shown") + " for " + plugin.itemName(body) + " " + body);
			uhoh = true;
			System.out.println(female);
			new Exception().printStackTrace(System.out);
			wrongList.remove(body);
		} else {
			if (!rightList.contains(body)) {
				System.out.println(name + " " + (hidden ? "hidden" : "shown") + " for " + plugin.itemName(body) + " " + body);
			} else {
//				System.out.println(name + " " + (hidden ? "hidden" : "shown") + " for " + plugin.itemName(body));
			}
		}
		rightList.add(body);
		return uhoh;
	}

	@Subscribe
	public void onProjectileMoved(ProjectileMoved projectileMoved) {
		Projectile projectile = projectileMoved.getProjectile();

		// skip already seen projectiles.
		if (client.getGameCycle() >= projectile.getStartCycle()) {
			return;
		}

		LocalPoint point = client.getLocalPlayer().getLocalLocation();
		int plane = client.getLocalPlayer().getWorldLocation().getPlane();
		int sceneX = point.getSceneX();
		int sceneY = point.getSceneY();
		if (sceneX >= 0 && sceneY >= 0 && sceneX < 104 && sceneY < 104) {
			byte[][][] tileSettings = client.getTileSettings();
			int[][][] tileHeights = client.getTileHeights();
			int z1 = plane;
			if (plane < 3 && (tileSettings[1][sceneX][sceneY] & 2) == 2) {
				z1 = plane + 1;
			}

			int x = point.getX() & 127;
			int y = point.getY() & 127;
			int var8 = x * tileHeights[z1][sceneX + 1][sceneY] + (128 - x) * tileHeights[z1][sceneX][sceneY] >> 7;
			int var9 = tileHeights[z1][sceneX][sceneY + 1] * (128 - x) + x * tileHeights[z1][sceneX + 1][sceneY + 1] >> 7;
			int height = projectile.getHeight();
			int tileHeight = Perspective.getTileHeight(client, client.getLocalPlayer().getLocalLocation(), client.getLocalPlayer().getWorldLocation().getPlane());
			System.out.println(height + " " + tileHeight + " " + var8 + " " + var9);
			System.out.println(height + " " + (height - tileHeight) + " " + (height - var8) + " " + (height - var9));
//			return (128 - y) * var8 + y * var9 >> 7;
//		} else {
//			return 0;
		}

		System.out.println(Perspective.getTileHeight(client, client.getLocalPlayer().getLocalLocation(), client.getLocalPlayer().getWorldLocation().getPlane()));
		System.out.println(
				projectile.getId() + ", " +
				(projectile.getStartCycle() - client.getGameCycle()) + ", " +
				projectile.getStartHeight() + ", " +
				projectile.getEndHeight() + ", " +
					projectile.getScalar() + " " + projectile.getHeight() + " " + projectile.getZ() + " " +
				projectile.getSlope() + ", "
		);

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

	private List<AnimationSet> findMatchingAnimationSets(int itemId)
	{
		Set<List<Integer>> list = this.poseanims.get(itemId);
		int variationId = itemId;
		if (list == null)
		{
			Collection<Integer> variations = ItemVariationMapping.getVariations(ItemVariationMapping.map(itemId));
			for (Integer variation : variations)
			{
				if (this.poseanims.containsKey(variation))
				{
					list = poseanims.get(variation);
					variationId = variation;
					break;
				}
			}
		}

		boolean hasPoseAnims = list != null;
		List<AnimationSet> matchingSets = new ArrayList<>();
		boolean uhoh = false;
		List<Integer> poseanims = null;
		if (hasPoseAnims)
		{
			uhoh = list.size() > 1;
			if (uhoh) System.out.println("more than 1 set of pose animations: " + itemId + " " + variationId);
			poseanims = list.iterator().next();
			for (AnimationSet animationSet : AnimationSet.animationSets)
			{
				if (
					Objects.equals(animationSet.getAnimation(Swap.AnimationType.STAND), poseanims.get(0))
						&& Objects.equals(animationSet.getAnimation(Swap.AnimationType.ROTATE), poseanims.get(1))
						&& Objects.equals(animationSet.getAnimation(Swap.AnimationType.WALK), poseanims.get(2))
						&& Objects.equals(animationSet.getAnimation(Swap.AnimationType.WALK_BACKWARD), poseanims.get(3))
						&& Objects.equals(animationSet.getAnimation(Swap.AnimationType.SHUFFLE_LEFT), poseanims.get(4))
						&& Objects.equals(animationSet.getAnimation(Swap.AnimationType.SHUFFLE_RIGHT), poseanims.get(5))
						&& Objects.equals(animationSet.getAnimation(Swap.AnimationType.RUN), poseanims.get(6))
				)
				{
					matchingSets.add(animationSet);
				}
			}
		}
		return matchingSets;
	}

	private void listUnseen(Set<Integer> shows, Set<Integer> hides, KitType kitType)
	{
		System.out.println("shows " + shows);
		System.out.println("hides " + hides);
		for (Integer showsArm : shows)
		{
			if (hides.contains(showsArm)) {

				System.out.println("dupe id " + showsArm);
			}
		}
		Set<Integer> s = new HashSet<>();
		s.addAll(shows);
		s.addAll(hides);
		System.out.println(s.size() + " " + s);
		int count = 0;
		int countf2p = 0;
		for (int i = 0; i < client.getItemCount(); i++)
		{
			if (s.contains(i)) continue;
			ItemStats itemStats = itemManager.getItemStats(i, false);
			if (
				itemStats != null
				&& itemStats.isEquipable()
				&& itemStats.getEquipment().getSlot() == kitType.getIndex()
				&& itemManager.getItemComposition(i).getPlaceholderTemplateId() == -1
				&& itemManager.getItemComposition(i).getNote() == -1
			) {
				System.out.println(itemManager.getItemComposition(i).getName() + " " + i);
				count++;
			}
		}
		System.out.println(count);
	}

	public static final Map<Integer, Integer> OVERRIDE_EQUIPPABILITY_OR_SLOT = new HashMap<>();
	public static final Map<Integer, Constants.NameAndIconId> EQUIPPABLE_ITEMS_NOT_MARKED_AS_EQUIPMENT_NAMES = new HashMap<>();

	@Subscribe
	public void onCommandExecuted(CommandExecuted commandExecuted) {
		Player player2 = client.getLocalPlayer();
		final WorldPoint playerPos = player2.getWorldLocation();
		if (playerPos == null)
		{
			return;
		}

		final LocalPoint playerPosLocal = LocalPoint.fromWorld(client, playerPos);
		System.out.println("here");
		Projectile p = client.createProjectile(1465, playerPos.getPlane(), playerPosLocal.getX(), playerPosLocal.getY(), -636, client.getGameCycle(), client.getGameCycle() + 50, 16, 64, 124, null, 6208, 6464);
//		Projectile p = client.createProjectile(1465, plane, playerPosLocal.getX(), playerPosLocal.getY(), -636, client.getGameCycle(), client.getGameCycle() + 50, 16, 64, 124, null, targetX, targetY);
		String[] arguments = commandExecuted.getArguments();
		String command = commandExecuted.getCommand();

		if (command.equals("sfx")) {
			int i = Integer.parseInt(arguments[0]);
			client.playSoundEffect(i);
		}

		if (command.equals("listunseen"))
		{
			System.out.println("arms");
			listUnseen(showsArms, hidesArms, TORSO);

			System.out.println("hair");
			listUnseen(showsHair, hidesHair, HEAD);

			System.out.println("jaw");
			listUnseen(showsJaw, hidesJaw, HEAD);

			for (Map.Entry<Integer, Set<List<Integer>>> entry : poseanims.entrySet())
			{
				int itemId = entry.getKey();
				String name = itemManager.getItemComposition(itemId).getName();
				Set<List<Integer>> poseanims = entry.getValue();
				if (poseanims.size() > 1) {
					System.out.println("more than 1: " + name + " " + itemId + " " + poseanims);
				} else {
					List<Integer> next = poseanims.iterator().next();
					boolean foundMatch = false;
					if (!Objects.equals(next.get(1), next.get(7))) {
						System.out.println("different rotate animations! " + name + " " + itemId + " " + next.get(1) + " " + next.get(7));
					}
					for (AnimationSet animationSet : AnimationSet.animationSets)
					{
						if (
							Objects.equals(animationSet.getAnimation(Swap.AnimationType.STAND), next.get(0))
							&& Objects.equals(animationSet.getAnimation(Swap.AnimationType.ROTATE), next.get(1))
							&& Objects.equals(animationSet.getAnimation(Swap.AnimationType.WALK), next.get(2))
							&& Objects.equals(animationSet.getAnimation(Swap.AnimationType.WALK_BACKWARD), next.get(3))
							&& Objects.equals(animationSet.getAnimation(Swap.AnimationType.SHUFFLE_LEFT), next.get(4))
							&& Objects.equals(animationSet.getAnimation(Swap.AnimationType.SHUFFLE_RIGHT), next.get(5))
							&& Objects.equals(animationSet.getAnimation(Swap.AnimationType.RUN), next.get(6))
						) {
							foundMatch = true;
							break;
						}
					}
					if (!foundMatch) {
						System.out.println("no match: " + name + " " + itemId + " " + next);
					}
				}
			}
		}

		if (command.equals("weapondata")) {
			System.out.println("weapondata");
			Set<Integer> seen = new HashSet<>();
			for (int i = 0; i < client.getItemCount(); i++)
			{
				ItemComposition comp = itemManager.getItemComposition(i);
				if (comp.getPlaceholderTemplateId() != -1 || comp.getNote() != -1) continue;

				Integer mySlot = SLOT_OVERRIDES.get(i);
				ItemStats itemStats = itemManager.getItemStats(i, false);
				Integer wikiSlot = itemStats != null && itemStats.isEquipable() ? itemStats.getEquipment().getSlot() : null;
				if (mySlot == null) mySlot = wikiSlot;
				if (mySlot == null) continue;
				if (mySlot == -1) continue;
				if (mySlot != WEAPON_SLOT) continue;

				int baseId = ItemVariationMapping.map(i);
				if (seen.contains(baseId)) continue;
				seen.add(baseId);

				List<AnimationSet> matchingSets = findMatchingAnimationSets(i);
				List<String> matchingSetNames = matchingSets.stream().map(set -> set.name).collect(Collectors.toList());
				System.out.println("weapon " + i + " " + comp.getMembersName() + " " + (!matchingSetNames.isEmpty() ? matchingSetNames : "no match")/* + " " + (hasPoseAnims ? poseanims : "not seen") + " " + (uhoh ? "uhoh" : "")*/);
			}
			for (Map.Entry<Integer, Set<List<Integer>>> entry : poseanims.entrySet())
			{
				int itemId = entry.getKey();
				ItemComposition comp = itemManager.getItemComposition(itemId);
				ItemStats stats = itemManager.getItemStats(itemId, false);
			}
		}

		if (command.equals("countslots"))
		{
			tempHidesHair = new HashSet<>(this.hidesHair);
			tempHidesJaw = new HashSet<>(this.hidesJaw);
			tempShowsHair = new HashSet<>(this.showsHair);
			tempShowsJaw = new HashSet<>(this.showsJaw);
			addHidesHairAndJaw();

			int torso = 0;
			int head = 0;
			int weapon = 0;
			int torsoSeen = 0;
			int hairSeen = 0;
			int jawSeen = 0;
			int weaponSeen = 0;
			int torsoVar = 0;
			int hairVar = 0;
			int jawVar = 0;
			int weaponVar = 0;
			for (int i = 0; i < client.getItemCount(); i++)
			{
				ItemComposition itemComposition = plugin.itemManager.getItemComposition(i);
				if (itemComposition.getPlaceholderTemplateId() != -1 || itemComposition.getNote() != -1) continue;

				Integer slot = SLOT_OVERRIDES.get(i);
				if (slot == -1) continue;
				if (slot == null) {
					ItemStats itemStats = plugin.itemManager.getItemStats(i, false);
					if (itemStats != null && itemStats.isEquipable())
					{
						slot = itemStats.getEquipment().getSlot();
					}
				}
				if (slot == null) continue;

				if (slot == TORSO.ordinal())
				{
					torso++;
					if (hidesArms.contains(i) || showsArms.contains(i)) {
						torsoSeen++;
						torsoVar++;
					} else {
						Collection<Integer> variations = ItemVariationMapping.getVariations(ItemVariationMapping.map(i));
						boolean found = false;
						for (Integer variation : variations)
						{
							if (hidesArms.contains(variation) || showsArms.contains(variation)) {
								found = true;
								break;
							}
						}
						if (found)
						{
							torsoVar++;
						} else {
//								System.out.println(itemManager.getItemComposition(i).getName() + " " + i);
						}
					}
				}
				else if (slot == HEAD.ordinal())
				{
					head++;
					if (tempHidesHair.contains(i) || tempShowsHair.contains(i)) {
						hairSeen++;
						hairVar++;
					} else {
						Collection<Integer> variations = ItemVariationMapping.getVariations(ItemVariationMapping.map(i));
						boolean found = false;
						for (Integer variation : variations)
						{
							if (hidesHair.contains(variation) || showsHair.contains(variation)) {
								found = true;
								break;
							}
						}
						if (found)
						{
							hairVar++;
						} else {
								System.out.println("hair: " + itemManager.getItemComposition(i).getName() + " " + i);
						}
					}
					if (tempHidesJaw.contains(i) || tempShowsJaw.contains(i)) {
						if (i == 27400) {

							System.out.println("in here 2");
						}
						jawSeen++;
						jawVar++;
					} else {
						Collection<Integer> variations = ItemVariationMapping.getVariations(ItemVariationMapping.map(i));
						boolean found = false;
						for (Integer variation : variations)
						{
							if (hidesJaw.contains(variation) || showsJaw.contains(variation)) {
								found = true;
								break;
							}
						}
						if (found)
						{
							jawVar++;
						} else {
								System.out.println("jaw: " + itemManager.getItemComposition(i).getName() + " " + i);
						}
					}
				}
				else if (slot == WEAPON.ordinal())
				{
					weapon++;
					if (poseanims.containsKey(i)) {
						weaponSeen++;
						weaponVar++;
					} else {
						Collection<Integer> variations = ItemVariationMapping.getVariations(ItemVariationMapping.map(i));
						boolean found = false;
						for (Integer variation : variations)
						{
							if (poseanims.containsKey(variation)) {
								found = true;
								break;
							}
						}
						if (found)
						{
							weaponVar++;
						} else {
//							System.out.println(Constants.getName(i, itemComposition.getName()) + " " + i);
						}
					}
				}
			}
			System.out.println(torso + " " + head + " " + weapon);
			System.out.println(torsoVar + " (" + (torso - torsoVar) + ") " + hairVar + " (" + (head - hairVar) + ") " + jawVar + " (" + (head - jawVar) + ") " + weaponVar + " (" + (weapon - weaponVar) + ") ");
			System.out.println(torsoSeen + " (" + (torso - torsoSeen) + ") " + hairSeen + " (" + (head - hairSeen) + ") " + jawSeen + " (" + (head - jawSeen) + ") " + weaponSeen + " (" + (weapon - weaponSeen) + ") ");
		}

		if (command.equals("json")) {
			json();
		}

		if (command.equals("checkunequippables"))
		{
			System.out.println("checking unequippables");
			for (Map.Entry<Integer, Integer> integerIntegerEntry : Constants.SLOT_OVERRIDES.entrySet())
			{
				ItemStats itemStats = itemManager.getItemStats(integerIntegerEntry.getKey(), false);
				if (itemStats == null || !itemStats.isEquipable())
					continue;
				if (itemStats.getEquipment().getSlot() != integerIntegerEntry.getValue()) {
					System.out.println("item " + integerIntegerEntry.getKey() + " " + itemManager.getItemComposition(integerIntegerEntry.getKey()).getName() + " is in the wrong slot");
				} else {
					System.out.println("item " + integerIntegerEntry.getKey() + " " + itemManager.getItemComposition(integerIntegerEntry.getKey()).getName() + " should be removed from constants.");
				}
			}
		}

		if (command.equals("bla")) {
			for (TransmogSet transmogSet : plugin.transmogSets)
			{
				for (Swap swap : transmogSet.getSwaps())
				{
					for (Swap.AnimationReplacement animationReplacement : swap.animationReplacements)
					{
						System.out.println(animationReplacement.auto);
						animationReplacement.auto = -1;
					}
				}
			}
			if (true) return;
			long accountHash = -1;//client.getAccountHash();
			byte[] key = {
				(byte) accountHash,
				(byte) (accountHash >> 8),
				(byte) (accountHash >> 16),
				(byte) (accountHash >> 24),
				(byte) (accountHash >> 32),
				(byte) (accountHash >> 40),
			};
			key[0] += 0;
			String keyStr = Base64.getUrlEncoder().encodeToString(key);
			System.out.println(Base64.getUrlEncoder().encodeToString(key));
			if (true) return;
			for (TransmogSet transmogSet : plugin.transmogSets)
			{
				if (transmogSet.getName().equals("test")) {
					Swap swap = transmogSet.getSwaps().get(0);
//					swap.getModelSwaps().add(26700);
//					swap.getModelSwaps().add(ItemID.SCYTHE_OF_VITUR);
					System.out.println("here " + swap.getModelSwaps());
				}
			}
			System.out.println(showsArms);
			System.out.println(hidesArms);
			System.out.println(uhohList);
			showsArms.remove(21021);
			hidesArms.add(21021);
			uhohList.clear();
			if (true) return;
			List<Integer> unequippableWithModel = new ArrayList<>();
			List<Integer> hashes = new ArrayList<>();
			Multimap<Integer, Integer> hashToItemIds = ArrayListMultimap.create();
			System.out.println("testing all items for having models:");
			ArrayList<TransmogSet> transmogSetsBackup = new ArrayList<>(plugin.transmogSets);
			for (int i = 0; i < client.getItemCount(); i++)
			{
				ItemComposition itemComposition = itemManager.getItemComposition(i);
				// skip notes and placeholders.
				if (itemComposition.getNote() != -1 || itemComposition.getPlaceholderTemplateId() != -1/* || ChatBoxFilterableSearch.WEIGHT_REDUCING_ITEMS.get(i) != null*/)
					continue;

				Swap swap = new Swap();
				for (Constants.HiddenSlot hiddenSlot : Constants.HiddenSlot.values())
				{
					int itemId = mapNegativeId(new Constants.NegativeId(Constants.NegativeIdsMap.HIDE_SLOT, hiddenSlot.ordinal()));
					swap.addModelSwap(itemId, plugin);
				}
				swap.addModelSwap(i, plugin, WEAPON.ordinal());
				TransmogSet transmogSet = new TransmogSet(Collections.singletonList(swap));
				plugin.transmogSets.clear();
				plugin.transmogSets.add(transmogSet);
				plugin.handleTransmogSetChange();

				int length = client.getLocalPlayer().getModel().getFaceColors1().length;
				if (length > 0)
				{
					int h =
						Arrays.hashCode(client.getLocalPlayer().getModel().getFaceColors1()) +
						Arrays.hashCode(client.getLocalPlayer().getModel().getFaceColors2()) +
						Arrays.hashCode(client.getLocalPlayer().getModel().getFaceColors3()) +
						Arrays.hashCode(client.getLocalPlayer().getModel().getFaceTransparencies()) +
						Arrays.hashCode(client.getLocalPlayer().getModel().getFaceTextures()) +
						Arrays.hashCode(client.getLocalPlayer().getModel().getTextureFaces()) +
						Arrays.hashCode(client.getLocalPlayer().getModel().getTexIndices1()) +
						Arrays.hashCode(client.getLocalPlayer().getModel().getTexIndices2()) +
						Arrays.hashCode(client.getLocalPlayer().getModel().getTexIndices3()) +
//						Arrays.hashCode(client.getLocalPlayer().getModel().getVerticesX()) +
//						Arrays.hashCode(client.getLocalPlayer().getModel().getVerticesY()) +
//						Arrays.hashCode(client.getLocalPlayer().getModel().getVerticesZ()) +
//						Arrays.hashCode(client.getLocalPlayer().getModel().getFaceIndices1()) +
//						Arrays.hashCode(client.getLocalPlayer().getModel().getFaceIndices2()) +
//						Arrays.hashCode(client.getLocalPlayer().getModel().getFaceIndices3()) +
//						Arrays.hashCode(client.getLocalPlayer().getModel().getFaceRenderPriorities()) +
//						Arrays.hashCode(client.getLocalPlayer().getModel().getVertexNormalsX()) +
//						Arrays.hashCode(client.getLocalPlayer().getModel().getVertexNormalsY()) +
//						Arrays.hashCode(client.getLocalPlayer().getModel().getVertexNormalsZ()) +
							0
						;
					hashToItemIds.put(h, i);
					ItemStats itemStats = itemManager.getItemStats(i, false);
					if (itemStats != null && itemStats.isEquipable()) continue;
					unequippableWithModel.add(i);
//					System.out.println(i + " " + itemManager.getItemComposition(i).getName());
					hashes.add(h);
				}
			}
			System.out.println("results:");
			int count = 0;
			for (int i = 0; i < unequippableWithModel.size(); i++)
			{
				int itemId = unequippableWithModel.get(i);
				int hash = hashes.get(i);
				Collection<Integer> itemIds = hashToItemIds.get(hash);
				boolean dupe = false;
				boolean hasEquippableCounterpart = false;
				if (itemIds.size() > 1) {
					if (itemIds.stream().sorted(Integer::compare).findFirst().get() != itemId) dupe = true;
					for (Integer id : itemIds)
					{
						ItemStats itemStats = itemManager.getItemStats(id, false);
						if (itemStats != null && itemStats.isEquipable()) {
							hasEquippableCounterpart = true;
							break;
						}
					}
				}
				if (!hasEquippableCounterpart && !dupe)
				{
					System.out.println(itemId + " " + itemManager.getItemComposition(itemId).getName());
					if (itemIds.size() > 1) {
						for (Integer integer1 : itemIds)
						{
							if (integer1 == itemId) continue;
							boolean equippable = false;
							ItemStats itemStats = itemManager.getItemStats(integer1, false);
							if (itemStats != null && itemStats.isEquipable()) {
								equippable = true;
							}
							System.out.println("\t" + (equippable ? "e" : " ") + " " + integer1 + " " + plugin.itemManager.getItemComposition(integer1).getName());
						}
					}
					count++;
				}
			}
			System.out.println(count);
//			for (Integer integer : hashToItemIds.keySet())
//			{
//				Collection<Integer> integers = hashToItemIds.get(integer);
//				if (integers.size() > 1) {
//
//					System.out.println("matching for id " + integer + " ");
//					for (Integer integer1 : integers)
//					{
//						boolean equippable = false;
//						ItemStats itemStats = itemManager.getItemStats(integer1, false);
//						if (itemStats != null && itemStats.isEquipable()) {
//							equippable = true;
//						}
//						System.out.println("\t" + (equippable ? "e" : " ") + " " + integer1 + " " + plugin.itemManager.getItemComposition(integer1).getName());
//					}
//				}
//			}
			plugin.transmogSets = transmogSetsBackup;
			plugin.handleTransmogSetChange();
		}

		if (command.equals("testsortupdate")) {
			System.out.println("doing test.");
			Swap swap;

			swap = new Swap(Arrays.asList(-1), Arrays.asList(-1), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
			swap.updateForSortOrderAndUniqueness(plugin);
			if (swap.getModelSwaps().size() != 0 || swap.getItemRestrictions().size() != 0) {
				System.out.println("test 1 failed.");
			}

			swap = new Swap(Arrays.asList(-1), Arrays.asList(-14, -15, -16), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
			swap.updateForSortOrderAndUniqueness(plugin);

			swap = new Swap(
				Arrays.asList(ItemID.SLAYER_HELMET_I, ItemID.ABYSSAL_TENTACLE, ItemID.GHRAZI_RAPIER, ItemID.DRAGON_SCIMITAR, ItemID.CHEFS_HAT),
				Arrays.asList(ItemID.SLAYER_HELMET_I, ItemID.ABYSSAL_TENTACLE, ItemID.GHRAZI_RAPIER, ItemID.DRAGON_SCIMITAR, ItemID.CHEFS_HAT, ItemID.SKIS),
				Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
			swap.updateForSortOrderAndUniqueness(plugin);
			if (
				swap.getItemRestrictions().get(0) != ItemID.DRAGON_SCIMITAR || swap.getItemRestrictions().get(1) != ItemID.ABYSSAL_TENTACLE || swap.getItemRestrictions().get(2) != ItemID.GHRAZI_RAPIER || swap.getItemRestrictions().get(3) != ItemID.CHEFS_HAT || swap.getItemRestrictions().get(4) != ItemID.SLAYER_HELMET_I || swap.getItemRestrictions().size() != 5 ||
				swap.getModelSwaps().get(0) != ItemID.ABYSSAL_TENTACLE || swap.getModelSwaps().get(1) != ItemID.SKIS || swap.getModelSwaps().get(2) != ItemID.SLAYER_HELMET_I || swap.getModelSwaps().size() != 3
			) {
				System.out.println("test 2 failed");
			}
			System.out.println("test done.");
		}

		if (command.equals("reload")) {
			AnimationSet.loadAnimationSets();
			Constants.loadData(plugin.getGson());
			SwingUtilities.invokeLater(plugin.pluginPanel::rebuild);
			System.out.println("reloaded animations sets, projectiles, and equippable.");
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
			for (Integer integer : Constants.SLOT_OVERRIDES.keySet())
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

	private void putWeapon(Map<Integer, AnimationSet> itemIdToAnimationSet, int itemId, String animationSetName)
	{
		int baseId = ItemVariationMapping.map(itemId);
		AnimationSet animationSet;
		if ("".equals(animationSetName)) {
			List<AnimationSet> matchingAnimationSets = findMatchingAnimationSets(baseId);
			if (matchingAnimationSets.size() > 1) {
				System.out.println("multiple matching sets for item " + itemId + " " + matchingAnimationSets);
				return;
			} else if (matchingAnimationSets.isEmpty()) {
				System.out.println("no matching sets for item " + itemId + " " + matchingAnimationSets);
				return;
			}
			animationSet = matchingAnimationSets.iterator().next();
		} else {
			animationSet = AnimationSet.getAnimationSet(animationSetName);
		}

		if (animationSet == null) {
			System.out.println("null animationset " + animationSetName);
			return;
		}

		itemIdToAnimationSet.put(baseId, animationSet);
	}

	Set<Integer> tempHidesHair;
	Set<Integer> tempHidesJaw;
	Set<Integer> tempShowsHair;
	Set<Integer> tempShowsJaw;
	private void addHidesHairAndJaw()
	{
		List<Integer> removeItem = new ArrayList<>();

		follow(ItemID.OCHRE_SNELM_3341, ItemID.OCHRE_SNELM);
		follow(ItemID.LAW_TIARA, ItemID.COSMIC_TIARA);
		follow(ItemID.A_SPECIAL_TIARA, ItemID.COSMIC_TIARA);
		follow(ItemID.ASTRAL_TIARA, ItemID.COSMIC_TIARA);
		follow(ItemID.ORANGE_BOATER, ItemID.PURPLE_BOATER);
		follow(ItemID.SWEET_NUTCRACKER_HAT, ItemID.FESTIVE_NUTCRACKER_HAT);
		showAll(ItemID.MOUTH_GRIP);
		showAll(ItemID.A_CHAIR);
		showAll(ItemID.ONE_BARREL);
		showAll(ItemID.TWO_BARRELS);
		showAll(ItemID.THREE_BARRELS);
		showAll(ItemID.FOUR_BARRELS);
		showAll(ItemID.FIVE_BARRELS);
		showAll(ItemID.PIRATE_HAT);
		follow(ItemID.ADVENTURERS_HOOD_T1, ItemID.MAX_HOOD);
		follow(ItemID.SARADOMIN_MAX_HOOD, ItemID.MAX_HOOD);
		follow(ItemID.ZAMORAK_MAX_HOOD, ItemID.MAX_HOOD);
		follow(ItemID.GUTHIX_MAX_HOOD, ItemID.MAX_HOOD);
		follow(ItemID.ACCUMULATOR_MAX_HOOD, ItemID.MAX_HOOD);
		follow(ItemID.SANGUINE_TORVA_FULL_HELM, ItemID.TORVA_FULL_HELM);
		follow(ItemID.SANGUINE_TORVA_PLATEBODY, ItemID.TORVA_PLATEBODY);
		removeItem.add(ItemID.SCYTHE_OF_VITUR_22664);
		removeItem.add(ItemID.ARMADYL_GODSWORD_22665);
		removeItem.add(ItemID.RUBBER_CHICKEN_22666);
		removeItem.add(ItemID.DRAGON_KNIFE_22812);
		removeItem.add(ItemID.DRAGON_KNIFE_22814);
		// broken pvp arena gear. no model.
		removeItem.addAll(Arrays.asList(26686, 26686, 26687, 26687, 26688, 26688, 26698, 26698, 26699, 26699, 26700, 26700, 26701, 26701, 26702, 26702, 26703, 26703));
		for (int i = 0; i < client.getItemCount(); i++)
		{
			ItemComposition itemComposition = itemManager.getItemComposition(i);
			if (itemComposition.getPlaceholderTemplateId() != -1 || itemComposition.getNote() != -1) continue;

			Integer slot = SLOT_OVERRIDES.get(i);
			if (slot == null) {
				slot = plugin.getWikiScrapeSlot(i);
			}
			if (slot == null || slot != HEAD_SLOT) continue;
			if (removeItem.contains(i)) continue;

			Collection<Integer> variations = ItemVariationMapping.getVariations(ItemVariationMapping.map(i));
			for (Integer variation : variations)
			{
				if (tempHidesHair.contains(variation)) {
					tempHidesHair.add(i);
				}
				if (tempHidesJaw.contains(variation)) {
					tempHidesJaw.add(i);
				}
				if (tempShowsHair.contains(variation)) {
					tempShowsHair.add(i);
				}
				if (tempShowsJaw.contains(variation)) {
					tempShowsJaw.add(i);
				}
			}

			String name = itemComposition.getName().toLowerCase();
			if (name.contains("bedsheet")) {
				removeItem.add(i);
			}
			if (name.contains("tricorn hat")) {
				showAll(i);
			}
			if (name.contains("saika's")) {
				showHairJaw(i, false, !name.contains("shroud"));
			}
			if (name.contains("koriff's")) {
				showHairJaw(i, false, !name.contains("cowl"));
			}
			if (name.contains("maoma's")) {
				showHairJaw(i, false, name.contains("med"));
			}
		}
		System.out.println(removeItem);
	}

	private void showAll(int itemId)
	{
		showHairJaw(itemId, true, true);
	}

	private void showHairJaw(int itemId, boolean showHair, boolean showJaw)
	{
		if (
			((hidesHair.contains(itemId) && !showHair) || (showsHair.contains(itemId) && showHair)) &&
			((hidesJaw.contains(itemId) && !showJaw) || (showsJaw.contains(itemId) && showJaw))
		) {
			System.out.println("superflous " + itemId);
		}
		if (
			(hidesHair.contains(itemId) && showHair) ||
			(showsHair.contains(itemId) && !showHair) ||
			(hidesJaw.contains(itemId) && showJaw) ||
			(showsJaw.contains(itemId) && !showJaw)
		) {
			System.out.println("goes against collected hair and jaw data " + itemId);
		}
		if (!showHair) tempHidesHair.add(itemId);
		else tempShowsHair.add(itemId);
		if (!showJaw) tempHidesJaw.add(itemId);
		else tempShowsJaw.add(itemId);
	}

	private void follow(int itemId, int itemIdToCopy)
	{
		Boolean currentlyHidingHair = hidesHair.contains(itemId) ? TRUE : showsHair.contains(itemId) ? FALSE : null;
		Boolean currentlyHidingJaw = hidesJaw.contains(itemId) ? TRUE : showsJaw.contains(itemId) ? FALSE : null;
		Boolean hairToCopy = hidesHair.contains(itemIdToCopy) ? TRUE : showsHair.contains(itemIdToCopy) ? FALSE : null;
		Boolean jawToCopy = hidesJaw.contains(itemIdToCopy) ? TRUE : showsJaw.contains(itemIdToCopy) ? FALSE : null;
		if (hairToCopy == null || jawToCopy == null) {
			System.out.println("copied item " + itemId + " didn't have enough data " + hairToCopy + " " + jawToCopy);
			return;
		}

		boolean warnSuperfluous = false;
		if (hairToCopy) {
			if (currentlyHidingHair == null) {
				tempHidesHair.add(itemId);
			} else if (currentlyHidingHair) {
				// skip;
				warnSuperfluous = true;
			} else {
				System.out.println("item " + itemId + " was told to follow something that the data disagrees with.");
			}
		} else {
			if (currentlyHidingHair == null) {
				tempShowsHair.add(itemId);
			} else if (currentlyHidingHair) {
				System.out.println("item " + itemId + " was told to follow something that the data disagrees with.");
			} else {
				// skip;
				warnSuperfluous = true;
			}
		}
		if (jawToCopy) {
			if (currentlyHidingJaw == null) {
				tempHidesJaw.add(itemId);
			} else if (currentlyHidingJaw) {
				// skip;
				if (warnSuperfluous) {
					System.out.println("itemId " + itemId + " follow is superfluous.");
				}
			} else {
				System.out.println("item " + itemId + " was told to follow something that the data disagrees with.");
			}
		} else {
			if (currentlyHidingJaw == null) {
				tempShowsJaw.add(itemId);
			} else if (currentlyHidingJaw) {
				System.out.println("item " + itemId + " was told to follow something that the data disagrees with.");
			} else {
				// skip;
				if (warnSuperfluous) {
					System.out.println("itemId " + itemId + " follow is superfluous.");
				}
			}
		}
	}

	private void addSlotData(Set<Integer> showsArms, Set<Integer> hidesHair, Set<Integer> hidesJaw, Map<Integer, Set<List<Integer>>> poseanims)
	{
		for (int i = 0; i < client.getItemCount(); i++)
		{
			ItemComposition itemComposition = plugin.itemManager.getItemComposition(i);
			if (itemComposition.getPlaceholderTemplateId() != -1 || itemComposition.getNote() != -1) continue;

			Integer slot = SLOT_OVERRIDES.get(i);
			if (slot == null) {
				ItemStats itemStats = plugin.itemManager.getItemStats(i, false);
				if (itemStats != null && itemStats.isEquipable())
				{
					slot = itemStats.getEquipment().getSlot();
				}
			}
			if (slot == null) continue;

			if (slot == TORSO_SLOT)
			{
				if (!hidesArms.contains(i) && !this.showsArms.contains(i))
				{
					Collection<Integer> variations = ItemVariationMapping.getVariations(ItemVariationMapping.map(i));
					boolean shown = false;
					for (Integer variation : variations)
					{
						if (this.showsArms.contains(variation)) {
							shown = true;
							break;
						}
					}
					if (shown)
					{
						showsArms.add(i);
					} else {
//								System.out.println(itemManager.getItemComposition(i).getName() + " " + i);
					}
				}
			}
			else if (slot == HEAD_SLOT)
			{
				if (!hidesHair.contains(i) && !this.showsHair.contains(i))
				{
					Collection<Integer> variations = ItemVariationMapping.getVariations(ItemVariationMapping.map(i));
					boolean hides = false;
					for (Integer variation : variations)
					{
						if (this.hidesHair.contains(variation)) {
							hides = true;
							break;
						}
					}
					if (hides)
					{
						hidesHair.add(i);
					} else {
//								System.out.println(itemManager.getItemComposition(i).getName() + " " + i);
					}
				}
				if (!hidesJaw.contains(i) && !this.showsJaw.contains(i))
				{
					Collection<Integer> variations = ItemVariationMapping.getVariations(ItemVariationMapping.map(i));
					boolean hides = false;
					for (Integer variation : variations)
					{
						if (this.hidesJaw.contains(variation)) {
							hides = true;
							break;
						}
					}
					if (hides)
					{
						hidesJaw.add(i);
					} else {
//								System.out.println(itemManager.getItemComposition(i).getName() + " " + i);
					}
				}
			}
//			else if (slot == WEAPON_SLOT)
//			{
//				if (!this.poseanims.containsKey(i))
//				{
//					Collection<Integer> variations = ItemVariationMapping.getVariations(ItemVariationMapping.map(i));
//					boolean found = false;
//					for (Integer variation : variations)
//					{
//						if (this.poseanims.containsKey(variation)) {
//							poseanims.put(i, this.poseanims.get(variation));
//							found = true;
//							break;
//						}
//					}
//					if (found)
//					{
//					} else {
////						System.out.println(itemManager.getItemComposition(i).getName() + " " + i);
//					}
//				}
//			}
		}
	}

	private static void addUnequippable(int itemId, KitType kitType) {
		addUnequippable(itemId, kitType, null);
	}

	private static void addUnequippable(int itemId, KitType kitType, String name) {
		addUnequippable(itemId, kitType, name, -1);
	}

	private static void addUnequippable(int itemId, KitType kitType, String name, int iconId) {
		OVERRIDE_EQUIPPABILITY_OR_SLOT.put(itemId, kitType.getIndex());
		if (name != null || iconId != -1) {
			EQUIPPABLE_ITEMS_NOT_MARKED_AS_EQUIPMENT_NAMES.put(itemId, new Constants.NameAndIconId(name, iconId));
		}
	}

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
		if (menuOptionClicked.getMenuOption().equals("Use") && menuOptionClicked.getItemId() == 563) {
			if (demoanim != -1) {
				demoanim--;
				for (Constants.ActorAnimation value : values())
				{
					value.setAnimation(client.getLocalPlayer(), demoanim);
				}
				System.out.println("demo anim " + demoanim);
				client.playSoundEffect(demoanim);
			}
			if (demogfx != -1) {
				demogfx--;
				client.getLocalPlayer().setGraphic(demogfx);
				client.getLocalPlayer().setSpotAnimFrame(0);
				client.getLocalPlayer().setGraphicHeight(0);
				System.out.println("demo gfx " + demogfx);
			}
		} else if (menuOptionClicked.getMenuOption().equals("Use") && menuOptionClicked.getItemId() == 995){
			if (demoanim != -1) {
				demoanim++;
				client.playSoundEffect(demoanim);
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
//		System.out.println(menuOptionClicked.getMenuOption() + " " + Text.removeTags(menuOptionClicked.getMenuTarget()));
	}

	private void json()
	{
		OVERRIDE_EQUIPPABILITY_OR_SLOT.clear();
		EQUIPPABLE_ITEMS_NOT_MARKED_AS_EQUIPMENT_NAMES.clear();
		Constants.Data data = new Constants.Data();

		addUnequippable(48, WEAPON); // Longbow (u)
		addUnequippable(50, WEAPON); // Shortbow (u)
		addUnequippable(54, WEAPON); // Oak shortbow (u)
		addUnequippable(56, WEAPON); // Oak longbow (u)
		addUnequippable(58, WEAPON); // Willow longbow (u)
		addUnequippable(60, WEAPON); // Willow shortbow (u)
		addUnequippable(62, WEAPON); // Maple longbow (u)
		addUnequippable(64, WEAPON); // Maple shortbow (u)
		addUnequippable(66, WEAPON); // Yew longbow (u)
		addUnequippable(68, WEAPON); // Yew shortbow (u)
		addUnequippable(70, WEAPON); // Magic longbow (u)
		addUnequippable(72, WEAPON); // Magic shortbow (u)
		addUnequippable(229, WEAPON); // Vial
		addUnequippable(6664, WEAPON); // Fishing explosive
		addUnequippable(272, SHIELD); // Fish food
		addUnequippable(301, WEAPON); // Lobster pot
		addUnequippable(303, WEAPON); // Small fishing net
		addUnequippable(305, WEAPON); // Big fishing net
		addUnequippable(21652, WEAPON); // Drift net
		addUnequippable(307, WEAPON); // Fishing rod
		addUnequippable(309, WEAPON); // Fly fishing rod
		addUnequippable(311, WEAPON); // Harpoon
		addUnequippable(314, WEAPON); // Feather
		addUnequippable(413, SHIELD); // Oyster pearls
		addUnequippable(421, AMULET); // Lathas' amulet
		addUnequippable(583, WEAPON); // Bailing bucket (icon empty, appears empty)
		addUnequippable(1925, WEAPON); // Bucket
		addUnequippable(590, WEAPON); // Tinderbox
		addUnequippable(675, WEAPON); // Rock pick
		addUnequippable(677, WEAPON); // Panning tray
		addUnequippable(717, SHIELD); // Scrawled note
		addUnequippable(718, SHIELD); // A scribbled note
		addUnequippable(719, SHIELD); // Scrumpled note
//		addUnequippable(727, WEAPON); // Hollow reed (dupe of 1785)
//		addUnequippable(728, WEAPON); // Hollow reed (dupe of 1785)
		addUnequippable(796, WEAPON, "Exploding vial"); // null
		addUnequippable(797, SHIELD, "Mortar (Pestle and mortar)"); // null
		addUnequippable(798, WEAPON, "Pestle (Pestle and mortar)"); // null (item icon is invisible)
		addUnequippable(818, WEAPON); // Poisoned dart(p)
		addUnequippable(945, WEAPON); // Throwing rope
		addUnequippable(946, WEAPON); // Knife
		addUnequippable(952, WEAPON); // Spade
		addUnequippable(954, WEAPON); // Rope
		addUnequippable(970, SHIELD); // Papyrus
		addUnequippable(973, WEAPON); // Charcoal
		addUnequippable(1235, WEAPON); // Poisoned dagger(p)
		addUnequippable(1511, SHIELD); // Logs
		addUnequippable(1601, WEAPON); // Diamond
		addUnequippable(1603, WEAPON); // Ruby
		addUnequippable(1605, WEAPON); // Emerald
		addUnequippable(1607, WEAPON); // Sapphire
		addUnequippable(1609, WEAPON); // Opal
		addUnequippable(1611, WEAPON); // Jade
		addUnequippable(1613, WEAPON); // Red topaz
		addUnequippable(1615, WEAPON); // Dragonstone
		addUnequippable(1733, WEAPON); // Needle
		addUnequippable(1735, WEAPON); // Shears
		addUnequippable(1741, SHIELD); // Leather
		addUnequippable(1761, SHIELD); // Soft clay
		addUnequippable(1785, WEAPON); // Glassblowing pipe
		addUnequippable(1917, WEAPON); // Beer
		addUnequippable(1919, WEAPON); // Beer glass. This one is the same model as one the wiki considers
		// equippable, but this one has the better icon so I choose to include this one.
		addUnequippable(1931, WEAPON); // Pot
		addUnequippable(1963, WEAPON); // banana (right-handed)
		addUnequippable(1973, SHIELD); // Chocolate bar
		addUnequippable(2347, WEAPON); // Hammer
		addUnequippable(2395, WEAPON); // Magic ogre potion
		addUnequippable(2520, WEAPON); // Brown toy horsey
		addUnequippable(2522, WEAPON); // White toy horsey
		addUnequippable(2524, WEAPON); // Black toy horsey
		addUnequippable(2526, WEAPON); // Grey toy horsey
		addUnequippable(2888, WEAPON); // A stone bowl
		addUnequippable(2946, WEAPON); // Golden tinderbox
		addUnequippable(2949, WEAPON); // Golden hammer
		addUnequippable(2968, SHIELD); // Druidic spell
		addUnequippable(3080, WEAPON, "Infernal pickaxe (yellow)"); // null
		addUnequippable(3157, WEAPON); // Karambwan vessel
		addUnequippable(3164, SHIELD); // Karamjan rum
		addUnequippable(3177, SHIELD); // Left-handed banana
		addUnequippable(3711, WEAPON); // Keg of beer
		addUnequippable(3803, WEAPON); // Beer tankard
		addUnequippable(3850, WEAPON, "Open book (green)"); // null
		addUnequippable(3893, WEAPON); // Stool
		addUnequippable(3935, WEAPON, "Bench"); // null
		addUnequippable(3937, WEAPON, "Bench"); // null
		addUnequippable(3939, WEAPON, "Bench"); // null
		addUnequippable(3941, WEAPON, "Bench"); // null
		addUnequippable(3943, WEAPON, "Bench"); // null
		addUnequippable(3945, WEAPON, "Bench"); // null
		addUnequippable(3947, WEAPON, "Bench"); // null
		addUnequippable(3949, WEAPON, "Bench"); // null
//		addUnequippable(3953, WEAPON); // null (lyre but held in a weird way)
//		addUnequippable(3969, WEAPON); // null (dragon harpoon but floating)
//		addUnequippable(3971, WEAPON); // null (infernal harpoon but floating)
		addUnequippable(4032, WEAPON, "Mod ash's mug"); // null
		addUnequippable(4080, WEAPON, "Yoyo"); // null
		addUnequippable(4085, SHIELD); // Wax
		addUnequippable(4155, WEAPON); // Enchanted gem
		addUnequippable(4161, WEAPON); // Bag of salt
		addUnequippable(4162, WEAPON); // Rock hammer
		addUnequippable(4193, WEAPON); // Extended brush
		addUnequippable(4251, WEAPON); // Ectophial
		addUnequippable(4435, SHIELD); // Weather report
		addUnequippable(4498, WEAPON); // Rope
		addUnequippable(4500, WEAPON); // Pole
		addUnequippable(4605, WEAPON); // Snake charm
		addUnequippable(4613, WEAPON); // Spinning plate
		addUnequippable(4614, WEAPON); // Broken plate
		addUnequippable(4692, SHIELD); // Gold leaf
		addUnequippable(4704, WEAPON); // Stone bowl
		addUnequippable(4705, WEAPON, "Open book (red/yellow)"); // null
		addUnequippable(4809, SHIELD); // Torn page
		addUnequippable(4814, SHIELD); // Sithik portrait
		addUnequippable(4817, SHIELD); // Book of portraiture
		addUnequippable(4829, SHIELD); // Book of 'h.a.m'
		addUnequippable(4837, SHIELD); // Necromancy book
		addUnequippable(5060, WEAPON); // Dwarven battleaxe
		addUnequippable(5061, WEAPON); // Dwarven battleaxe
		addUnequippable(5081, WEAPON, "Dragon pickaxe (yellow)"); // null
		addUnequippable(5083, WEAPON, "Dragon pickaxe (or) (yellow)"); // null
		addUnequippable(5325, WEAPON); // Gardening trowel
		addUnequippable(5329, WEAPON); // Secateurs
		addUnequippable(5331, WEAPON); // Watering can
		addUnequippable(5341, WEAPON); // Rake
		addUnequippable(5343, WEAPON); // Seed dibber
		addUnequippable(5350, WEAPON); // Empty plant pot
		addUnequippable(5560, WEAPON); // Stethoscope
		addUnequippable(5614, WEAPON); // Magic carpet
		addUnequippable(5732, WEAPON); // Stool
		addUnequippable(5769, SHIELD); // Calquat keg
		addUnequippable(5982, SHIELD); // Watermelon
		addUnequippable(6036, WEAPON); // Plant cure
		addUnequippable(6281, SHIELD); // Thatch spar light
		addUnequippable(6448, WEAPON); // Spadeful of coke
		addUnequippable(6449, WEAPON); // null
		addUnequippable(6451, WEAPON); // null
		addUnequippable(6468, WEAPON); // Plant cure
		addUnequippable(6470, WEAPON); // Compost potion(4)
		addUnequippable(6565, WEAPON, "Bob the cat", ItemID.PET_CAT_1564); // null (item icon is invisible)
		addUnequippable(6573, WEAPON); // Onyx
		addUnequippable(6635, WEAPON); // Commorb
		addUnequippable(6657, TORSO); // Camo top (sleeveless)
		addUnequippable(6658, LEGS); // Camo bottoms (with shoes)
//		addUnequippable(6659, WEAPON); // Camo helmet (looks identical to the regular one)
		addUnequippable(6670, WEAPON); // Fishbowl
		addUnequippable(6671, WEAPON); // Fishbowl
		addUnequippable(6672, WEAPON); // Fishbowl
		addUnequippable(6673, WEAPON); // Fishbowl and net
		addUnequippable(6713, WEAPON); // Wrench
		addUnequippable(6714, WEAPON); // Holy wrench
		addUnequippable(6721, WEAPON); // Rusty scimitar
		addUnequippable(6722, WEAPON); // Zombie head
		addUnequippable(6748, WEAPON); // Demonic sigil
		addUnequippable(6772, SHIELD); // Smouldering pot
		addUnequippable(6788, SHIELD); // Torn robe
		addUnequippable(6789, SHIELD); // Torn robe
		addUnequippable(6817, WEAPON); // Slender blade
		addUnequippable(6818, WEAPON); // Bow-sword
		addUnequippable(6864, WEAPON); // Marionette handle
		addUnequippable(7004, WEAPON, "Chisel"); // null
		addUnequippable(7118, SHIELD); // Canister
		addUnequippable(7119, SHIELD); // Cannon ball
		addUnequippable(7120, SHIELD); // Ramrod
		addUnequippable(7121, SHIELD); // Repair plank
		addUnequippable(7410, WEAPON); // Queen's secateurs
		addUnequippable(7412, WEAPON, "Bench"); // null
		addUnequippable(7421, WEAPON); // Fungicide spray 10
		addUnequippable(7475, WEAPON); // Brulee
		addUnequippable(7509, WEAPON); // Dwarven rock cake
		addUnequippable(7572, SHIELD); // Red banana
		addUnequippable(7637, WEAPON); // Silvthrill rod
		addUnequippable(7682, WEAPON); // Hoop
		addUnequippable(7684, WEAPON); // Dart
		addUnequippable(7686, WEAPON); // Bow and arrow
		addUnequippable(7688, WEAPON); // Kettle
		addUnequippable(7728, WEAPON); // Empty cup
		addUnequippable(7732, WEAPON); // Porcelain cup
		addUnequippable(7735, WEAPON); // Porcelain cup
		addUnequippable(7744, WEAPON); // Asgarnian ale
		addUnequippable(7746, WEAPON); // Greenman's ale
		addUnequippable(7748, WEAPON); // Dragon bitter
		addUnequippable(7750, WEAPON); // Moonlight mead
		addUnequippable(7752, WEAPON); // Cider
		addUnequippable(7754, WEAPON); // Chef's delight
		addUnequippable(7756, WEAPON); // Paintbrush
		addUnequippable(7758, WEAPON, "Rusty sword (looks weird)"); // null
		addUnequippable(7773, WEAPON); // Branch
		addUnequippable(7778, WEAPON); // Short vine
		addUnequippable(7804, WEAPON); // Ancient mjolnir
		addUnequippable(8794, WEAPON); // Saw
		addUnequippable(8798, WEAPON, "Chair/bench"); // null
		addUnequippable(8799, WEAPON, "Chair/bench"); // null
		addUnequippable(8800, WEAPON, "Chair/bench"); // null
		addUnequippable(8801, WEAPON, "Chair/bench"); // null
		addUnequippable(8802, WEAPON, "Chair/bench"); // null
		addUnequippable(8803, WEAPON, "Chair/bench"); // null
		addUnequippable(8804, WEAPON, "Chair/bench"); // null
		addUnequippable(8805, WEAPON, "Chair/bench"); // null
		addUnequippable(8806, WEAPON, "Chair/bench"); // null
		addUnequippable(8807, WEAPON, "Chair/bench"); // null
		addUnequippable(8808, WEAPON, "Chair/bench"); // null
		addUnequippable(8809, WEAPON, "Chair/bench"); // null
		addUnequippable(8810, WEAPON, "Chair/bench"); // null
		addUnequippable(8811, WEAPON, "Chair/bench"); // null
		addUnequippable(8812, WEAPON, "Chair/bench"); // null
		addUnequippable(8813, WEAPON, "Chair/bench"); // null
		addUnequippable(8814, WEAPON, "Chair/bench"); // null
		addUnequippable(8815, WEAPON, "Chair/bench"); // null
		addUnequippable(8816, WEAPON, "Chair/bench"); // null
		addUnequippable(8817, WEAPON, "Chair/bench"); // null
		addUnequippable(8818, WEAPON, "Chair/bench"); // null
		addUnequippable(8819, WEAPON, "Chair/bench"); // null
		addUnequippable(8820, WEAPON, "Chair/bench"); // null
		addUnequippable(8821, WEAPON, "Chair/bench"); // null
		addUnequippable(8822, WEAPON, "Chair/bench"); // null
		addUnequippable(8823, WEAPON, "Chair/bench"); // null
		addUnequippable(8824, WEAPON, "Chair/bench"); // null
		addUnequippable(8825, WEAPON, "Chair/bench"); // null
		addUnequippable(8826, WEAPON, "Chair/bench"); // null
		addUnequippable(8827, WEAPON, "Chair/bench"); // null
		addUnequippable(8829, WEAPON, "Chair/bench"); // null
		addUnequippable(8830, WEAPON, "Chair/bench"); // null
		addUnequippable(8831, WEAPON, "Chair/bench"); // null
		addUnequippable(8832, WEAPON, "Chair/bench"); // null
		addUnequippable(8833, WEAPON, "Chair/bench"); // null
		addUnequippable(8834, WEAPON, "Chair/bench"); // null
		addUnequippable(8835, WEAPON, "Chair/bench"); // null
		addUnequippable(8857, WEAPON); // Shot
		addUnequippable(8940, SHIELD); // Rum
		addUnequippable(8941, SHIELD); // Rum
		addUnequippable(8986, WEAPON); // Bucket
		addUnequippable(8987, WEAPON); // Torch
		addUnequippable(9060, SHIELD, "Red bottle (offhand)"); // null
		addUnequippable(9061, SHIELD, "Blue bottle (offhand)"); // null
		addUnequippable(9062, WEAPON, "Blue bottle"); // null
		addUnequippable(9063, WEAPON, "Yellow bottle"); // null
		addUnequippable(9065, SHIELD); // Emerald lantern
		addUnequippable(9085, SHIELD); // Empty vial
		addUnequippable(9087, SHIELD); // Waking sleep vial
		addUnequippable(9103, HEAD); // A special tiara
		addUnequippable(9106, HEAD); // Astral tiara
		addUnequippable(9138, SHIELD, "Logs"); // null (logs)
		addUnequippable(9420, SHIELD); // Bronze limbs
		addUnequippable(9422, SHIELD); // Blurite limbs
		addUnequippable(9423, SHIELD); // Iron limbs
		addUnequippable(9425, SHIELD); // Steel limbs
		addUnequippable(9427, SHIELD); // Mithril limbs
		addUnequippable(9429, SHIELD); // Adamantite limbs
		addUnequippable(9431, SHIELD); // Runite limbs
		addUnequippable(9440, WEAPON); // Wooden stock
		addUnequippable(9442, WEAPON); // Oak stock
		addUnequippable(9444, WEAPON); // Willow stock
		addUnequippable(9446, WEAPON); // Teak stock
		addUnequippable(9448, WEAPON); // Maple stock
		addUnequippable(9450, WEAPON); // Mahogany stock
		addUnequippable(9452, WEAPON); // Yew stock
		addUnequippable(9454, WEAPON); // Bronze crossbow (u)
		addUnequippable(9456, WEAPON); // Blurite crossbow (u)
		addUnequippable(9457, WEAPON); // Iron crossbow (u)
		addUnequippable(9459, WEAPON); // Steel crossbow (u)
		addUnequippable(9461, WEAPON); // Mithril crossbow (u)
		addUnequippable(9463, WEAPON); // Adamant crossbow (u)
		addUnequippable(9465, WEAPON); // Runite crossbow (u)
		addUnequippable(9590, WEAPON); // Dossier
		addUnequippable(9631, SHIELD, "Bucket"); // null
		addUnequippable(9660, WEAPON); // Bucket
		addUnequippable(9665, WEAPON); // Torch
		addUnequippable(9893, WEAPON, "Mortar (Pestle and mortar)"); // null
		addUnequippable(9894, WEAPON, "Hammer"); // null
		addUnequippable(9895, SHIELD, "Chisel (offhand)"); // null
		addUnequippable(9896, WEAPON, "Frying pan"); // null
		addUnequippable(9897, WEAPON, "Axe"); // null
		addUnequippable(9898, SHIELD, "Red shield"); // null
		addUnequippable(9899, WEAPON, "Small red shield"); // null
		addUnequippable(9905, WEAPON, "Barrel"); // null
		addUnequippable(9906, WEAPON); // Ghost buster 500
		addUnequippable(9907, WEAPON); // Ghost buster 500
		addUnequippable(9908, WEAPON); // Ghost buster 500
		addUnequippable(9909, WEAPON); // Ghost buster 500
		addUnequippable(9910, WEAPON); // Ghost buster 500
		addUnequippable(9911, WEAPON); // Ghost buster 500
		addUnequippable(9912, WEAPON); // Ghost buster 500
		addUnequippable(9913, SHIELD); // White destabiliser
		addUnequippable(9914, SHIELD); // Red destabiliser
		addUnequippable(9915, SHIELD); // Blue destabiliser
		addUnequippable(9916, SHIELD); // Green destabiliser
		addUnequippable(9917, SHIELD); // Yellow destabiliser
		addUnequippable(9918, SHIELD); // Black destabiliser
		addUnequippable(9943, WEAPON); // Sandbag
		addUnequippable(10022, SHIELD); // null
		addUnequippable(10029, WEAPON); // Teasing stick
		addUnequippable(10131, WEAPON, "Barb-tail harpoon (held backwards)"); // null
		addUnequippable(10152, WEAPON, "Brown barb-tail kebbit in noose"); // null
		addUnequippable(10153, WEAPON, "White barb-tail kebbit in noose"); // null
		addUnequippable(10154, WEAPON, "Beige barb-tail kebbit in noose"); // null
		addUnequippable(10155, WEAPON, "Dark brown barb-tail kebbit in noose"); // null
		addUnequippable(10484, WEAPON, "Brown spiky kebbit in noose"); // null
		addUnequippable(10485, WEAPON); // Scroll
		addUnequippable(10488, SHIELD); // Selected iron
		addUnequippable(10544, WEAPON); // Healing vial(2)
		addUnequippable(10566, CAPE, "Fire cape (untextured)"); // Fire cape
		addUnequippable(10568, WEAPON, "3rd age pickaxe (light)"); // null
		addUnequippable(10810, SHIELD); // Arctic pine logs
		addUnequippable(10841, WEAPON); // Apricot cream pie
		addUnequippable(10842, WEAPON); // Decapitated head
		addUnequippable(10857, WEAPON); // Severed leg
		addUnequippable(10860, WEAPON, "Tea flask"); // null
		addUnequippable(10861, SHIELD, "Tiny tea cup"); // null
		addUnequippable(10886, WEAPON); // Prayer book
		addUnequippable(10952, WEAPON); // Slayer bell
		addUnequippable(11012, WEAPON); // Wand
		addUnequippable(11013, WEAPON); // Infused wand
		addUnequippable(11027, WEAPON); // Easter egg
		addUnequippable(11028, WEAPON); // Easter egg
		addUnequippable(11029, WEAPON); // Easter egg
		addUnequippable(11030, WEAPON); // Easter egg
		addUnequippable(11046, WEAPON); // Rope
		addUnequippable(11063, WEAPON, "Paintbrush (I think)"); // null
		addUnequippable(11132, HANDS); // Onyx bracelet
		addUnequippable(11154, WEAPON); // Dream potion
		addUnequippable(11167, WEAPON); // Phoenix crossbow
		addUnequippable(11204, WEAPON); // Shrink-me-quick
		addUnequippable(11279, WEAPON); // Elvarg's head
		addUnequippable(11288, SHIELD, "Yellow vial (offhand)"); // null
		addUnequippable(11289, WEAPON, "Yellow vial"); // null
		addUnequippable(11290, WEAPON, "Cyan vial"); // null
		addUnequippable(11291, WEAPON, "Red vial"); // null
		addUnequippable(11292, WEAPON, "Lime green vial"); // null
		addUnequippable(11293, WEAPON, "Light turquoise vial"); // null
		addUnequippable(11294, WEAPON, "Blue vial"); // null
		addUnequippable(11295, WEAPON, "Dark gray vial"); // null
		addUnequippable(11296, WEAPON, "White vial"); // null
		addUnequippable(11297, WEAPON, "Orange vial"); // null
		addUnequippable(11298, WEAPON, "Light lime green vial"); // null
		addUnequippable(11299, WEAPON, "Pink vial"); // null
		addUnequippable(11300, WEAPON, "Light blue vial"); // null
		addUnequippable(11301, WEAPON, "Light green vial"); // null
		addUnequippable(11302, WEAPON, "Purple vial"); // null
		addUnequippable(11303, WEAPON, "Light orange vial"); // null
		addUnequippable(11304, WEAPON, "Turquoise vial"); // null
		addUnequippable(11305, WEAPON, "Black vial"); // null
		addUnequippable(11306, WEAPON, "Training bow (held incorrectly)"); // null
		addUnequippable(11307, WEAPON, "Shortbow (held incorrectly)"); // null
		addUnequippable(11308, WEAPON, "Oak Shortbow (held incorrectly)"); // null
		addUnequippable(11309, WEAPON, "Willow Shortbow (held incorrectly)"); // null
		addUnequippable(11310, WEAPON, "Maple Shortbow (held incorrectly)"); // null
		addUnequippable(11311, WEAPON, "Yew Shortbow (held incorrectly)"); // null
		addUnequippable(11312, WEAPON, "Magic Shortbow (held incorrectly)"); // null
		addUnequippable(11313, WEAPON, "Longbow (held incorrectly)"); // null
		addUnequippable(11314, WEAPON, "Oak Longbow (held incorrectly)"); // null
		addUnequippable(11315, WEAPON, "Willow Longbow (held incorrectly)"); // null
		addUnequippable(11316, WEAPON, "Maple Longbow (held incorrectly)"); // null
		addUnequippable(11317, WEAPON, "Yew Longbow (held incorrectly)"); // null
		addUnequippable(11318, WEAPON, "Magic Longbow (held incorrectly)"); // null
		addUnequippable(11319, WEAPON, "Seercull (held incorrectly)"); // null
		addUnequippable(11320, WEAPON, "Shark"); // null
		addUnequippable(11321, WEAPON, "Swordfish"); // null
		addUnequippable(11322, WEAPON, "Tuna"); // null
		addUnequippable(11323, WEAPON); // Barbarian rod
		addUnequippable(11542, WEAPON, "Stool"); // null
		addUnequippable(11543, WEAPON, "Bench"); // null
		addUnequippable(12401, WEAPON, "Map (buggy graphic)"); // null
		// 12660 through 12690 are just jaw slot items? They are called "Clan wars cape" but idk what they do.
		addUnequippable(13215, WEAPON); // Tiger toy
		addUnequippable(13216, WEAPON); // Lion toy
		addUnequippable(13217, WEAPON); // Snow leopard toy
		addUnequippable(13218, WEAPON); // Amur leopard toy
		addUnequippable(13233, SHIELD); // Smouldering stone
		addUnequippable(13353, WEAPON); // Gricoller's can
		addUnequippable(13446, WEAPON); // Dark essence block
		addUnequippable(13570, WEAPON); // Juniper charcoal
		addUnequippable(13682, WEAPON, "Cabbage"); // null
		addUnequippable(13683, WEAPON, "Cabbage"); // null
		addUnequippable(13685, WEAPON, "Red cabbage"); // null
		addUnequippable(19492, HANDS); // Zenyte bracelet
		addUnequippable(19493, WEAPON); // Zenyte
		addUnequippable(20275, WEAPON); // Gnomish firelighter
		addUnequippable(20397, WEAPON); // Spear
		addUnequippable(21186, CAPE, "Fire max cape (untextured)"); // Fire max cape
		addUnequippable(21253, HEAD); // Farmer's strawhat
		addUnequippable(21284, CAPE, "Infernal max cape (untextured)"); // Infernal max cape
		addUnequippable(21297, CAPE, "Infernal cape (untextured)"); // Infernal cape
		addUnequippable(21347, WEAPON); // Amethyst
		addUnequippable(21655, WEAPON); // Pufferfish
		addUnequippable(21918, SHIELD); // Dragon limbs
		addUnequippable(21921, WEAPON); // Dragon crossbow (u)
		addUnequippable(22997, WEAPON); // Bottomless compost bucket
		addUnequippable(23679, WEAPON, "Dragon pickaxe (zalcano) (yellow)"); // null
		addUnequippable(23684, WEAPON, "Crystal pickaxe (glowing)"); // null
//		addUnequippable(23766, WEAPON); // null // TODO this looks identical to the crystal harpoon.
		addUnequippable(23767, WEAPON, "Crystal harpoon (corrupted)"); // null
		addUnequippable(23819, WEAPON, "Orb of light"); // null
		addUnequippable(24077, SHIELD, "Bolt tip"); // null
		addUnequippable(24080, SHIELD, "Bolt tip"); // null
		addUnequippable(24081, SHIELD, "Bolt tip"); // null
		addUnequippable(24082, SHIELD, "Bolt tip"); // null
		addUnequippable(24083, SHIELD, "Bolt tip"); // null
		addUnequippable(24084, SHIELD, "Bolt tip"); // null
		addUnequippable(24085, SHIELD, "Bolt tip"); // null
		addUnequippable(24086, SHIELD, "Bolt tip"); // null
		addUnequippable(24087, SHIELD, "Bolt tip"); // null
		addUnequippable(24088, SHIELD, "Bolt tip"); // null
		addUnequippable(24089, WEAPON, "Bolt"); // null
		addUnequippable(24090, WEAPON, "Bolt"); // null
		addUnequippable(24091, WEAPON, "Bolt"); // null
		addUnequippable(24093, WEAPON, "Bolt"); // null
		addUnequippable(24094, WEAPON, "Bolt"); // null
		addUnequippable(24095, WEAPON, "Bolt"); // null
		addUnequippable(24096, WEAPON, "Bolt"); // null
		addUnequippable(24097, WEAPON, "Bolt"); // null
		addUnequippable(24098, WEAPON, "Bolt"); // null
		addUnequippable(24099, WEAPON, "Bolt"); // null
		addUnequippable(24100, WEAPON, "Bolt"); // null
		addUnequippable(24101, WEAPON, "Bolt"); // null
		addUnequippable(24102, WEAPON, "Bolt"); // null
		addUnequippable(24103, WEAPON, "Bolt"); // null
		addUnequippable(24104, WEAPON, "Bolt"); // null
		addUnequippable(24105, WEAPON, "Bolt"); // null
		addUnequippable(24106, WEAPON, "Headless arrow"); // null
		addUnequippable(24107, WEAPON, "Arrow shaft"); // null
		addUnequippable(24108, WEAPON, "Arrowhead"); // null
		addUnequippable(24109, WEAPON, "Arrowhead"); // null
		addUnequippable(24110, WEAPON, "Arrowhead"); // null
		addUnequippable(24111, WEAPON, "Arrowhead"); // null
		addUnequippable(24112, WEAPON, "Arrowhead"); // null
		addUnequippable(24113, WEAPON, "Arrowhead"); // null
		addUnequippable(24114, WEAPON, "Arrowhead"); // null
		addUnequippable(24115, WEAPON, "Arrowhead"); // null
		addUnequippable(24116, WEAPON, "Dart tip"); // null
		addUnequippable(24117, WEAPON, "Dart tip"); // null
		addUnequippable(24118, WEAPON, "Dart tip"); // null
		addUnequippable(24119, WEAPON, "Dart tip"); // null
		addUnequippable(24120, WEAPON, "Dart tip"); // null
		addUnequippable(24121, WEAPON, "Dart tip"); // null
		addUnequippable(24122, WEAPON, "Dart tip"); // null
		addUnequippable(24273, WEAPON, "Basilisk stone prison"); // null
		addUnequippable(24274, WEAPON, "Basilisk stone prison"); // null
		addUnequippable(24275, WEAPON, "Basilisk stone prison"); // null
		addUnequippable(24386, WEAPON, "Green and brown stick"); // null
		addUnequippable(24435, WEAPON); // Festive pot
		addUnequippable(24437, SHIELD); // Gingerbread shield
		addUnequippable(24460, WEAPON); // Twisted teleport scroll
		addUnequippable(24487, SHIELD, "Sextant"); // null
		addUnequippable(24998, SHIELD, "Black crystal"); // null (item icon is invisible)
		addUnequippable(24999, SHIELD, "Stick"); // null (item icon is invisible)
		addUnequippable(25000, WEAPON, "Paper"); // null (item icon is invisible)
		addUnequippable(25087, WEAPON); // Trailblazer teleport scroll
		addUnequippable(25102, SHIELD); // Fairy mushroom
		addUnequippable(25106, CAPE); // Extradimensional bag
		addUnequippable(25484, WEAPON); // Webweaver bow (u)
		addUnequippable(25485, WEAPON); // Webweaver bow
		addUnequippable(25486, WEAPON); // Ursine chainmace (u)
		addUnequippable(25487, WEAPON); // Ursine chainmace
		addUnequippable(25488, WEAPON); // Accursed sceptre (u)
		addUnequippable(25489, WEAPON); // Accursed sceptre
		addUnequippable(25490, WEAPON); // Voidwaker
		addUnequippable(25491, WEAPON); // Accursed sceptre (au)
		addUnequippable(25492, WEAPON); // Accursed sceptre (a)
		addUnequippable(25710, WEAPON); // Stool
		addUnequippable(25848, WEAPON, "Amethyst dart tip"); // null
		addUnequippable(25938, WEAPON, "Ghommal's hilt 1 (mainhand)", 25926); // Anim offhand
		addUnequippable(25941, WEAPON, "Ghommal's hilt 2 (mainhand)", 25928); // Anim offhand
		addUnequippable(25944, WEAPON, "Ghommal's hilt 3 (mainhand)", 25930); // Anim offhand
		addUnequippable(25947, WEAPON, "Ghommal's hilt 4 (mainhand)", 25932); // Anim offhand
		addUnequippable(25950, WEAPON, "Ghommal's hilt 5 (mainhand)", 25934); // Anim offhand
		addUnequippable(25953, WEAPON, "Ghommal's hilt 6 (mainhand)", 25936); // Anim offhand
		addUnequippable(26549, AMULET); // Portable waystone
		addUnequippable(26551, SHIELD); // Arcane grimoire
		addUnequippable(26581, SHIELD); // Goblin potion(4)
		addUnequippable(26880, WEAPON); // Catalytic guardian stone
		addUnequippable(27416, WEAPON); // Speedy teleport scroll
		addUnequippable(27546, WEAPON, "Ghommal's avernic defender 5 (mainhand)", 27550); // Anim offhand
		addUnequippable(27548, WEAPON, "Ghommal's avernic defender 6 (mainhand)", 27552); // Anim offhand
		addUnequippable(27873, WEAPON); // Eastfloor spade
		addUnequippable(7414, WEAPON); // Paddle
		addUnequippable(6123, WEAPON); // Beer glass
		addUnequippable(9702, WEAPON); // Stick
		addUnequippable(10840, WEAPON); // A jester stick
		int[] shouldNotBeEquippable = {22664, 22665, 22666, 22812, 22814, 26686, 26686, 26687, 26687, 26688, 26688, 26698, 26698, 26699, 26699, 26700, 26700, 26701, 26701, 26702, 26702, 26703, 26703, 4284, 4285};
		for (int i : shouldNotBeEquippable)
		{
			OVERRIDE_EQUIPPABILITY_OR_SLOT.put(i, -1);
		}

		Set<Integer> jawSlotItems = ImmutableSet.of(10556, 10557, 10558, 10559, 10567, 20802, 22308, 22309, 22310, 22311, 22312, 22313, 22314, 22315, 22337, 22338, 22339, 22340, 22341, 22342, 22343, 22344, 22345, 22346, 22347, 22348, 22349, 22721, 22722, 22723, 22724, 22725, 22726, 22727, 22728, 22729, 22730, 23460, 23461, 23462, 23463, 23464, 23465, 23466, 23467, 23468, 23469, 23470, 23471, 23472, 23473, 23474, 23475, 23476, 23477, 23478, 23479, 23480, 23481, 23482, 23483, 23484, 23485, 23486, 25228, 25229, 25230, 25231, 25232, 25233, 25234, 25235, 25236, 25237, 25238, 25239, 25240, 25241, 25242, 25243, 25212, 25213, 25214, 25215, 25216, 25217, 25218, 25219, 25220, 25221, 25222, 25223, 25224, 25225, 25226, 25227);
		for (Integer itemId : jawSlotItems)
		{
			OVERRIDE_EQUIPPABILITY_OR_SLOT.put(itemId, Constants.JAW_SLOT);
		}

		Map<Integer, List<Integer>> kitIndexToItemIds = new HashMap<>();
		for (Integer itemId : OVERRIDE_EQUIPPABILITY_OR_SLOT.keySet())
		{
			List<Integer> itemIds = kitIndexToItemIds.getOrDefault(OVERRIDE_EQUIPPABILITY_OR_SLOT.get(itemId), new ArrayList<>());
			itemIds.add(itemId);
			kitIndexToItemIds.put(OVERRIDE_EQUIPPABILITY_OR_SLOT.get(itemId), itemIds);
		}

//		Set<Integer> showsArms = addShowsArms(this.showsArms); // nothing to add.
		tempHidesHair = new HashSet<>(this.hidesHair);
		tempHidesJaw = new HashSet<>(this.hidesJaw);
		tempShowsHair = new HashSet<>(this.showsHair);
		tempShowsJaw = new HashSet<>(this.showsJaw);
		addHidesHairAndJaw();
		data.showArms = showsArms;
		data.hideHair = tempHidesHair;
		data.hideJaw = tempHidesJaw;

//		Map<Integer, Set<List<Integer>>> poseanims = new HashMap<>(this.poseanims);
//		addSlotData(showsArms, hidesHair, hidesJaw, poseanims);
		data.slotOverrides = kitIndexToItemIds;
		data.nameIconOverrides = EQUIPPABLE_ITEMS_NOT_MARKED_AS_EQUIPMENT_NAMES;

		Map<Integer, AnimationSet> itemIdToAnimationSet = new HashMap<>();
		String mace = "Dragon mace";
		String warhammer = "Dragon warhammer";
		String battleaxe = "Dragon battleaxe";
		String dagger = "Dragon sword";
		String shortsword = "Dragon sword";
		String longsword = "Dragon longsword/Saeldor";
		String machete = shortsword;
		String blackjack = mace;
		String crozier = "Staff2/Wand";
		String keris = "Spear";
		// keris is actually a different crush animation than the spear, but it looks the same (419 vs 382)
		// with shield it also uses 419.
		String sarasword = "Saradomin sword";
		String axe = "Dragon axe";
		String pickaxe = axe;
		String cane = mace;
		String poweredstaff = "Trident of the swamp";
		String harpoon = shortsword;
		String rod = "Unarmed";
		putWeapon(itemIdToAnimationSet, 35, shortsword); // Excalibur [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 278, dagger); // Cattleprod [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 667, longsword); // Blurite sword [Dragon longsword/Saeldor, Ghrazi rapier, Osmumten's Fang] [809, 823, 819, 820, 821, 822, 824, 823]
		/* TODO test */putWeapon(itemIdToAnimationSet, 732, "Dart"); // Holy water [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 746, dagger); // Dark dagger [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 747, dagger); // Glowing dagger [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 751, "Unarmed"); // Gnomeball [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Unarmed
		putWeapon(itemIdToAnimationSet, 767, "Crossbow"); // Phoenix crossbow [Crossbow, Zaryte crossbow] [4591, 823, 4226, 4227, 821, 822, 4228, 823] follow Crossbow
		putWeapon(itemIdToAnimationSet, 772, "Staff"); // Dramen staff [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 805, "Thrownaxe"); // Rune thrownaxe [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Thrownaxe
		putWeapon(itemIdToAnimationSet, 806, "Dart"); // Bronze dart [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dart
		putWeapon(itemIdToAnimationSet, 807, "Dart"); // Iron dart [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dart
		putWeapon(itemIdToAnimationSet, 808, "Dart"); // Steel dart [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dart
		putWeapon(itemIdToAnimationSet, 809, "Dart"); // Mithril dart [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dart
		putWeapon(itemIdToAnimationSet, 810, "Dart"); // Adamant dart [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dart
		putWeapon(itemIdToAnimationSet, 811, "Dart"); // Rune dart [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dart
		putWeapon(itemIdToAnimationSet, 837, "Crossbow"); // Crossbow [Crossbow, Zaryte crossbow] [4591, 823, 4226, 4227, 821, 822, 4228, 823] follow Crossbow
		putWeapon(itemIdToAnimationSet, 863, "Knife (non-dragon)"); // Iron knife [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Knife (non-dragon)
		putWeapon(itemIdToAnimationSet, 864, "Knife (non-dragon)"); // Bronze knife [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Knife (non-dragon)
		putWeapon(itemIdToAnimationSet, 865, "Knife (non-dragon)"); // Steel knife [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Knife (non-dragon)
		putWeapon(itemIdToAnimationSet, 866, "Knife (non-dragon)"); // Mithril knife [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Knife (non-dragon)
		putWeapon(itemIdToAnimationSet, 867, "Knife (non-dragon)"); // Adamant knife [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Knife (non-dragon)
		putWeapon(itemIdToAnimationSet, 868, "Knife (non-dragon)"); // Rune knife [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Knife (non-dragon)
		putWeapon(itemIdToAnimationSet, 869, "Knife (non-dragon)"); // Black knife [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Knife (non-dragon)
		putWeapon(itemIdToAnimationSet, 975, machete); // Machete [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 1203, dagger); // Iron dagger [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 1205, dagger); // Bronze dagger [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 1207, dagger); // Steel dagger [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 1209, dagger); // Mithril dagger [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 1211, dagger); // Adamant dagger [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 1213, dagger); // Rune dagger [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 1215, "Dragon dagger"); // Dragon dagger [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon dagger
		putWeapon(itemIdToAnimationSet, 1217, dagger); // Black dagger [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 1237, "Spear"); // Bronze spear [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209] follow Spear
		putWeapon(itemIdToAnimationSet, 1239, "Spear"); // Iron spear [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209] follow Spear
		putWeapon(itemIdToAnimationSet, 1241, "Spear"); // Steel spear [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209] follow Spear
		putWeapon(itemIdToAnimationSet, 1243, "Spear"); // Mithril spear [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209] follow Spear
		putWeapon(itemIdToAnimationSet, 1245, "Spear"); // Adamant spear [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209] follow Spear
		putWeapon(itemIdToAnimationSet, 1247, "Spear"); // Rune spear [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209] follow Spear
		putWeapon(itemIdToAnimationSet, 1249, "Spear"); // Dragon spear [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209] follow Spear
		putWeapon(itemIdToAnimationSet, 1265, pickaxe); // Bronze pickaxe [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 1267, pickaxe); // Iron pickaxe [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 1269, pickaxe); // Steel pickaxe [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 1271, pickaxe); // Adamant pickaxe [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 1273, pickaxe); // Mithril pickaxe [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 1275, pickaxe); // Rune pickaxe [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 1277, shortsword); // Bronze sword [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 1279, shortsword); // Iron sword [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 1281, shortsword); // Steel sword [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 1283, shortsword); // Black sword [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 1285, shortsword); // Mithril sword [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 1287, shortsword); // Adamant sword [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 1289, shortsword); // Rune sword [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 1291, shortsword); // Bronze longsword [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 1293, shortsword); // Iron longsword [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 1295, shortsword); // Steel longsword [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 1297, shortsword); // Black longsword [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 1299, shortsword); // Mithril longsword [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 1301, shortsword); // Adamant longsword [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 1303, shortsword); // Rune longsword [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 1305, "Dragon longsword/Saeldor"); // Dragon longsword [Dragon longsword/Saeldor, Ghrazi rapier, Osmumten's Fang] [809, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 1307, "2h sword"); // Bronze 2h sword [2h sword] [2561, 823, 2562, 2562, 2562, 2562, 2563, 823] follow 2h sword
		putWeapon(itemIdToAnimationSet, 1309, "2h sword"); // Iron 2h sword [2h sword] [2561, 823, 2562, 2562, 2562, 2562, 2563, 823] follow 2h sword
		putWeapon(itemIdToAnimationSet, 1311, "2h sword"); // Steel 2h sword [2h sword] [2561, 823, 2562, 2562, 2562, 2562, 2563, 823] follow 2h sword
		putWeapon(itemIdToAnimationSet, 1313, "2h sword"); // Black 2h sword [2h sword] [2561, 823, 2562, 2562, 2562, 2562, 2563, 823] follow 2h sword
		putWeapon(itemIdToAnimationSet, 1315, "2h sword"); // Mithril 2h sword [2h sword] [2561, 823, 2562, 2562, 2562, 2562, 2563, 823] follow 2h sword
		putWeapon(itemIdToAnimationSet, 1317, "2h sword"); // Adamant 2h sword [2h sword] [2561, 823, 2562, 2562, 2562, 2562, 2563, 823] follow 2h sword
		putWeapon(itemIdToAnimationSet, 1319, "2h sword"); // Rune 2h sword [2h sword] [2561, 823, 2562, 2562, 2562, 2562, 2563, 823] follow 2h sword
		putWeapon(itemIdToAnimationSet, 1321, shortsword); // Bronze scimitar [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon scimitar
		putWeapon(itemIdToAnimationSet, 1323, shortsword); // Iron scimitar [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon scimitar
		putWeapon(itemIdToAnimationSet, 1325, shortsword); // Steel scimitar [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon scimitar
		putWeapon(itemIdToAnimationSet, 1327, shortsword); // Black scimitar [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon scimitar
		putWeapon(itemIdToAnimationSet, 1329, shortsword); // Mithril scimitar [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon scimitar
		putWeapon(itemIdToAnimationSet, 1331, shortsword); // Adamant scimitar [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon scimitar
		putWeapon(itemIdToAnimationSet, 1333, shortsword); // Rune scimitar [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon scimitar
		putWeapon(itemIdToAnimationSet, 1335, warhammer); // Iron warhammer [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon warhammer
		putWeapon(itemIdToAnimationSet, 1337, warhammer); // Bronze warhammer [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon warhammer
		putWeapon(itemIdToAnimationSet, 1339, warhammer); // Steel warhammer [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon warhammer
		putWeapon(itemIdToAnimationSet, 1341, warhammer); // Black warhammer [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon warhammer
		putWeapon(itemIdToAnimationSet, 1343, warhammer); // Mithril warhammer [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon warhammer
		putWeapon(itemIdToAnimationSet, 1345, warhammer); // Adamant warhammer [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon warhammer
		putWeapon(itemIdToAnimationSet, 1347, warhammer); // Rune warhammer [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon warhammer
		putWeapon(itemIdToAnimationSet, 1349, axe); // Iron axe [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon axe
		putWeapon(itemIdToAnimationSet, 1351, axe); // Bronze axe [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon axe
		putWeapon(itemIdToAnimationSet, 1353, axe); // Steel axe [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon axe
		putWeapon(itemIdToAnimationSet, 1355, axe); // Mithril axe [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon axe
		putWeapon(itemIdToAnimationSet, 1357, axe); // Adamant axe [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon axe
		putWeapon(itemIdToAnimationSet, 1359, axe); // Rune axe [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon axe
		putWeapon(itemIdToAnimationSet, 1361, axe); // Black axe [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon axe
		putWeapon(itemIdToAnimationSet, 1363, battleaxe); // Iron battleaxe [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon battleaxe
		putWeapon(itemIdToAnimationSet, 1365, battleaxe); // Steel battleaxe [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon battleaxe
		putWeapon(itemIdToAnimationSet, 1367, battleaxe); // Black battleaxe [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon battleaxe
		putWeapon(itemIdToAnimationSet, 1369, battleaxe); // Mithril battleaxe [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon battleaxe
		putWeapon(itemIdToAnimationSet, 1371, battleaxe); // Adamant battleaxe [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon battleaxe
		putWeapon(itemIdToAnimationSet, 1373, battleaxe); // Rune battleaxe [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon battleaxe
		putWeapon(itemIdToAnimationSet, 1375, battleaxe); // Bronze battleaxe [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon battleaxe
		putWeapon(itemIdToAnimationSet, 1377, "Dragon battleaxe"); // Dragon battleaxe [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon battleaxe
		putWeapon(itemIdToAnimationSet, 1379, "Staff"); // Staff [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209] follow Staff
		putWeapon(itemIdToAnimationSet, 1381, "Staff"); // Staff of air [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209] follow Staff
		putWeapon(itemIdToAnimationSet, 1383, "Staff"); // Staff of water [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209] follow Staff
		putWeapon(itemIdToAnimationSet, 1385, "Staff"); // Staff of earth [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209] follow Staff
		putWeapon(itemIdToAnimationSet, 1387, "Staff"); // Staff of fire [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209] follow Staff
		putWeapon(itemIdToAnimationSet, 1389, "Staff"); // Magic staff [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209] follow Staff
		putWeapon(itemIdToAnimationSet, 1391, "Staff"); // Battlestaff [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209] follow Staff
		putWeapon(itemIdToAnimationSet, 1393, "Staff"); // Fire battlestaff [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209] follow Staff
		putWeapon(itemIdToAnimationSet, 1395, "Staff"); // Water battlestaff [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209] follow Staff
		putWeapon(itemIdToAnimationSet, 1397, "Staff"); // Air battlestaff [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209] follow Staff
		putWeapon(itemIdToAnimationSet, 1399, "Staff"); // Earth battlestaff [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209] follow Staff
		putWeapon(itemIdToAnimationSet, 1401, "Staff"); // Mystic fire staff [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209] follow Staff
		putWeapon(itemIdToAnimationSet, 1403, "Staff"); // Mystic water staff [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209] follow Staff
		putWeapon(itemIdToAnimationSet, 1405, "Staff"); // Mystic air staff [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209] follow Staff
		putWeapon(itemIdToAnimationSet, 1407, "Staff"); // Mystic earth staff [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209] follow Staff
		putWeapon(itemIdToAnimationSet, 1409, "Staff"); // Iban's staff [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209] follow Staff
		putWeapon(itemIdToAnimationSet, 1419, "Scythe (holiday item)"); // Scythe [Scythe (holiday item)] [847, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 1420, mace); // Iron mace [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon mace
		putWeapon(itemIdToAnimationSet, 1422, mace); // Bronze mace [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon mace
		putWeapon(itemIdToAnimationSet, 1424, mace); // Steel mace [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon mace
		putWeapon(itemIdToAnimationSet, 1428, mace); // Mithril mace [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon mace
		putWeapon(itemIdToAnimationSet, 1430, mace); // Adamant mace [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon mace
		putWeapon(itemIdToAnimationSet, 1432, mace); // Rune mace [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon mace
		putWeapon(itemIdToAnimationSet, 1434, "Dragon mace"); // Dragon mace [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon mace
		putWeapon(itemIdToAnimationSet, 2402, shortsword); // Silverlight [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 2415, "Ancient mace"); // Saradomin staff [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 2416, "Ancient mace"); // Guthix staff [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 2417, "Ancient mace"); // Zamorak staff [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 2460, mace); // Assorted flowers [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon mace
		putWeapon(itemIdToAnimationSet, 2462, mace); // Red flowers [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon mace
		putWeapon(itemIdToAnimationSet, 2464, mace); // Blue flowers [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon mace
		putWeapon(itemIdToAnimationSet, 2466, mace); // Yellow flowers [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon mace
		putWeapon(itemIdToAnimationSet, 2468, mace); // Purple flowers [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon mace
		putWeapon(itemIdToAnimationSet, 2470, mace); // Orange flowers [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon mace
		putWeapon(itemIdToAnimationSet, 2472, mace); // Mixed flowers [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon mace
		putWeapon(itemIdToAnimationSet, 2474, mace); // White flowers [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon mace
		putWeapon(itemIdToAnimationSet, 2476, mace); // Black flowers [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Dragon mace
		putWeapon(itemIdToAnimationSet, 2883, "Bow"); // Ogre bow [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] follow Bow
		putWeapon(itemIdToAnimationSet, 2952, dagger); // Wolfbane [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 2961, shortsword); // Silver sickle [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 3053, "Staff"); // Lava battlestaff [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209] follow Staff
		putWeapon(itemIdToAnimationSet, 3054, "Staff"); // Mystic lava staff [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209] follow Staff
		putWeapon(itemIdToAnimationSet, 3095, "Claws"); // Bronze claws [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 3096, "Claws"); // Iron claws [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 3097, "Claws"); // Steel claws [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 3098, "Claws"); // Black claws [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 3099, "Claws"); // Mithril claws [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 3100, "Claws"); // Adamant claws [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 3101, "Claws"); // Rune claws [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 3192, "Halberd"); // Iron halberd [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 3194, "Halberd"); // Steel halberd [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 3196, "Halberd"); // Black halberd [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 3198, "Halberd"); // Mithril halberd [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 3200, "Halberd"); // Adamant halberd [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 3202, "Halberd"); // Rune halberd [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 3204, "Halberd"); // Dragon halberd [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 3689, "Unarmed"); // Lyre [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 3690, "Unarmed"); // Enchanted lyre [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 3695, "Pet rock"); // Pet rock [Pet rock] [6657, 6661, 6658, 6659, 6662, 6663, 6660, 6661]
		putWeapon(itemIdToAnimationSet, 3757, shortsword); // Fremennik blade [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 3899, shortsword); // Iron sickle [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 4023, mace); // Monkey talisman [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 4037, "Banner"); // Saradomin banner [Banner] [1421, 1426, 1422, 1423, 1424, 1425, 1427, 1426]
		putWeapon(itemIdToAnimationSet, 4039, "Banner"); // Zamorak banner [Banner] [1421, 1426, 1422, 1423, 1424, 1425, 1427, 1426]
		putWeapon(itemIdToAnimationSet, 4068, shortsword); // Decorative sword [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 4086, mace); // Trollweiss [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 4151, "Abyssal whip"); // Abyssal whip [Abyssal whip] [808, 823, 1660, 1660, 1660, 1660, 1661, 823]
		putWeapon(itemIdToAnimationSet, 4153, "Granite maul"); // Granite maul [Granite maul] [1662, 823, 1663, 1663, 1663, 1663, 1664, 823]
		putWeapon(itemIdToAnimationSet, 4158, "Spear"); // Leaf-bladed spear [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 4170, "Staff"); // Slayer's staff [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 4565, "Easter basket"); // Easter basket [Easter basket] [1837, 823, 1836, 1836, 1836, 1836, 1836, 823]
		putWeapon(itemIdToAnimationSet, 4566, "Rubber chicken"); // Rubber chicken [Rubber chicken] [1832, 823, 1830, 1830, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 4580, "Spear"); // Black spear [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 4587, "Dragon scimitar"); // Dragon scimitar [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 4599, mace); // Oak blackjack [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 4600, mace); // Willow blackjack [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 4675, "Staff"); // Ancient staff [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 4710, "Staff"); // Ahrim's staff [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 4718, "Dharok's greataxe"); // Dharok's greataxe [Dharok's greataxe] [2065, 823, 2064, 2064, 2064, 2064, 824, 823]
		putWeapon(itemIdToAnimationSet, 4726, "Guthan's warspear"); // Guthan's warspear [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 4734, "Karil's crossbow"); // Karil's crossbow [Karil's crossbow] [2074, 823, 2076, 2076, 2076, 2076, 2077, 823]
		putWeapon(itemIdToAnimationSet, 4747, "Torag's hammers"); // Torag's hammers [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 4755, "Verac's flail"); // Verac's flail [Verac's flail] [2061, 823, 2060, 2060, 2060, 2060, 824, 823]
		putWeapon(itemIdToAnimationSet, 4827, "Bow"); // Comp ogre bow [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 5016, "Spear"); // Bone spear [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 5018, mace); // Bone club [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 6082, "Fixed device"); // Fixed device [Fixed device] [2316, 2321, 2317, 2318, 2319, 2320, 2322, 2321]
		putWeapon(itemIdToAnimationSet, 6313, machete); // Opal machete [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 6315, machete); // Jade machete [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 6317, machete); // Red topaz machete [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 6416, blackjack); // Maple blackjack [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 6522, "Toktz-xil-ul (obsidian ring)"); // Toktz-xil-ul [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 6523, shortsword); // Toktz-xil-ak [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 6525, shortsword); // Toktz-xil-ek [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 6526, "Staff"); // Toktz-mej-tal [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 6527, mace); // Tzhaar-ket-em [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 6528, "Tzhaar-ket-om"); // Tzhaar-ket-om [Dharok's greataxe] [2065, 823, 2064, 2064, 2064, 2064, 824, 823]
		putWeapon(itemIdToAnimationSet, 6541, "Abyssal whip"); // Mouse toy [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 6562, "Staff"); // Mud battlestaff [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 6563, "Staff"); // Mystic mud staff [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 6587, "Claws"); // White claws [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 6603, "Staff"); // White magic staff [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 6605, shortsword); // White sword [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 6607, shortsword); // White longsword [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 6609, "2h sword"); // White 2h sword [2h sword] [2561, 823, 2562, 2562, 2562, 2562, 2563, 823]
		putWeapon(itemIdToAnimationSet, 6611, shortsword); // White scimitar [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 6722, "Unarmed"); // Zombie head [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 6724, "Bow"); // Seercull [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 6739, "Dragon axe"); // Dragon axe [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 6746, shortsword); // Darklight [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 6760, "Spear"); // Guthix mjolnir [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 6762, "Spear"); // Saradomin mjolnir [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 6773, "Banner"); // Rat pole [Banner] [1421, 1426, 1422, 1423, 1424, 1425, 1427, 1426]
		putWeapon(itemIdToAnimationSet, 6908, "Staff2/Wand"); // Beginner wand [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 6910, "Staff2/Wand"); // Apprentice wand [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 6912, "Staff2/Wand"); // Teacher wand [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 6914, "Staff2/Wand"); // Master wand [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 7141, shortsword); // Harry's cutlass [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 7142, shortsword); // Rapier [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 7158, "2h sword"); // Dragon 2h sword [2h sword] [2561, 823, 2562, 2562, 2562, 2562, 2563, 823]
		putWeapon(itemIdToAnimationSet, 7170, "Chinchompa"); // Mud pie [Chinchompa] [3175, 3177, 3177, 3177, 3177, 3177, 3178, 3177]
		putWeapon(itemIdToAnimationSet, 7409, shortsword); // Magic secateurs [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 7433, shortsword); // Wooden spoon [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 7435, shortsword); // Egg whisk [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 7439, "2h sword"); // Spatula [2h sword] [2561, 823, 2562, 2562, 2562, 2562, 2563, 823]
		putWeapon(itemIdToAnimationSet, 7441, mace); // Frying pan [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 7443, shortsword); // Skewer [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 7445, mace); // Rolling pin [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 7447, shortsword); // Kitchen knife [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 7449, "Dharok's greataxe"); // Meat tenderiser [Dharok's greataxe] [2065, 823, 2064, 2064, 2064, 2064, 824, 823]
		putWeapon(itemIdToAnimationSet, 7451, shortsword); // Cleaver [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 7639, "Staff"); // Rod of ivandis (10) [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 7668, "Granite maul"); // Gadderhammer [Granite maul] [1662, 823, 1663, 1663, 1663, 1663, 1664, 823]
		putWeapon(itemIdToAnimationSet, 7671, "Boxing gloves"); // Boxing gloves [Boxing gloves] [3677, 823, 3680, 3680, 3680, 3680, 824, 823]
		putWeapon(itemIdToAnimationSet, 8650, "Banner"); // Banner [Banner] [1421, 1426, 1422, 1423, 1424, 1425, 1427, 1426]
		putWeapon(itemIdToAnimationSet, 8841, mace); // Void knight mace [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 8872, dagger); // Bone dagger [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 8880, "Crossbow"); // Dorgeshuun crossbow [Crossbow, Zaryte crossbow] [4591, 823, 4226, 4227, 821, 822, 4228, 823]
		putWeapon(itemIdToAnimationSet, 8971, "Banner"); // Phasmatys flag [Banner] [1421, 1426, 1422, 1423, 1424, 1425, 1427, 1426]
		putWeapon(itemIdToAnimationSet, 9013, "Staff"); // Skull sceptre [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 9084, "Staff"); // Lunar staff [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 9174, "Crossbow"); // Bronze crossbow [Crossbow, Zaryte crossbow] [4591, 823, 4226, 4227, 821, 822, 4228, 823]
		putWeapon(itemIdToAnimationSet, 9176, "Crossbow"); // Blurite crossbow [Crossbow, Zaryte crossbow] [4591, 823, 4226, 4227, 821, 822, 4228, 823]
		putWeapon(itemIdToAnimationSet, 9177, "Crossbow"); // Iron crossbow [Crossbow, Zaryte crossbow] [4591, 823, 4226, 4227, 821, 822, 4228, 823]
		putWeapon(itemIdToAnimationSet, 9179, "Crossbow"); // Steel crossbow [Crossbow, Zaryte crossbow] [4591, 823, 4226, 4227, 821, 822, 4228, 823]
		putWeapon(itemIdToAnimationSet, 9181, "Crossbow"); // Mithril crossbow [Crossbow, Zaryte crossbow] [4591, 823, 4226, 4227, 821, 822, 4228, 823]
		putWeapon(itemIdToAnimationSet, 9183, "Crossbow"); // Adamant crossbow [Crossbow, Zaryte crossbow] [4591, 823, 4226, 4227, 821, 822, 4228, 823]
		putWeapon(itemIdToAnimationSet, 9185, "Crossbow"); // Rune crossbow [Crossbow, Zaryte crossbow] [4591, 823, 4226, 4227, 821, 822, 4228, 823]
		putWeapon(itemIdToAnimationSet, 9703, shortsword); // Training sword [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 9705, "Bow"); // Training bow [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 10010, longsword); // Butterfly net [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 10033, "Chinchompa"); // Chinchompa [Chinchompa] [3175, 3177, 3177, 3177, 3177, 3177, 3178, 3177]
		putWeapon(itemIdToAnimationSet, 10034, "Chinchompa"); // Red chinchompa [Chinchompa] [3175, 3177, 3177, 3177, 3177, 3177, 3178, 3177]
		putWeapon(itemIdToAnimationSet, 10129, shortsword); // Barb-tail harpoon [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 10146, "Red salamander"); // Orange salamander [Red salamander] [5246, 823, 5245, 5245, 5245, 5245, 824, 823]
		putWeapon(itemIdToAnimationSet, 10147, "Red salamander"); // Red salamander [Red salamander] [5246, 823, 5245, 5245, 5245, 5245, 824, 823]
		putWeapon(itemIdToAnimationSet, 10148, "Red salamander"); // Black salamander [Red salamander] [5246, 823, 5245, 5245, 5245, 5245, 824, 823]
		putWeapon(itemIdToAnimationSet, 10149, "Red salamander"); // Swamp lizard [Red salamander] [5246, 823, 5245, 5245, 5245, 5245, 824, 823]
		putWeapon(itemIdToAnimationSet, 10150, "Noose wand"); // Noose wand [Noose wand] [5254, 5252, 5250, 5251, 1207, 1208, 5253, 5252]
		putWeapon(itemIdToAnimationSet, 10156, "Crossbow"); // Hunters' crossbow [Crossbow, Zaryte crossbow] [4591, 823, 4226, 4227, 821, 822, 4228, 823]
		putWeapon(itemIdToAnimationSet, 10280, "Comp bow"); // Willow comp bow [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 10282, "Comp bow"); // Yew comp bow [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 10284, "Comp bow"); // Magic comp bow [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 10440, crozier); // Saradomin crozier [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 10442, crozier); // Guthix crozier [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 10444, crozier); // Zamorak crozier [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 10487, "Undead chicken"); // Undead chicken [Undead chicken] [5363, 823, 5364, 5438, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 10491, axe); // Blessed axe [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 10501, "Snowball"); // Snowball [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 10581, keris); // Keris [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 10857, mace); // Severed leg [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 10858, "2h sword"); // Shadow sword [2h sword] [2561, 823, 2562, 2562, 2562, 2562, 2563, 823]
		putWeapon(itemIdToAnimationSet, 10887, "Barrelchest anchor"); // Barrelchest anchor [Barrelchest anchor] [5869, 823, 5867, 5867, 5867, 5867, 5868, 823]
		putWeapon(itemIdToAnimationSet, 11037, shortsword); // Brine sabre [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 11061, "Ancient mace"); // Ancient mace [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 11230, "Dart"); // Dragon dart [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 11235, "Bow"); // Dark bow [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 11259, "Magic butterfly net"); // Magic butterfly net [Magic butterfly net] [6604, 6611, 6607, 6608, 6610, 6609, 6603, 6611]
		putWeapon(itemIdToAnimationSet, 11371, "Spear"); // Steel hasta [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 11705, "Boxing gloves"); // Beach boxing gloves [Boxing gloves] [3677, 823, 3680, 3680, 3680, 3680, 824, 823]
		putWeapon(itemIdToAnimationSet, 11707, mace); // Cursed goblin hammer [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 11708, "Bow"); // Cursed goblin bow [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 11709, "Staff"); // Cursed goblin staff [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 11785, "Crossbow"); // Armadyl crossbow [Crossbow, Zaryte crossbow] [4591, 823, 4226, 4227, 821, 822, 4228, 823]
		putWeapon(itemIdToAnimationSet, 11787, "Staff"); // Steam battlestaff [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 11789, "Staff"); // Mystic steam staff [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 11791, "Staff"); // Staff of the dead [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 11802, "Godsword (Armadyl)"); // Armadyl godsword [Godsword (Ancient), Godsword (Ancient, alternative spec), Godsword (Armadyl), Godsword (Bandos), Godsword (Saradomin), Godsword (Zamorak)] [7053, 7044, 7052, 7052, 7048, 7047, 7043, 7044] uhoh
		putWeapon(itemIdToAnimationSet, 11804, "Godsword (Bandos)"); // Bandos godsword [Godsword (Ancient), Godsword (Ancient, alternative spec), Godsword (Armadyl), Godsword (Bandos), Godsword (Saradomin), Godsword (Zamorak)] [7053, 7044, 7052, 7052, 7048, 7047, 7043, 7044]
		putWeapon(itemIdToAnimationSet, 11806, "Godsword (Saradomin)"); // Saradomin godsword [Godsword (Ancient), Godsword (Ancient, alternative spec), Godsword (Armadyl), Godsword (Bandos), Godsword (Saradomin), Godsword (Zamorak)] [7053, 7044, 7052, 7052, 7048, 7047, 7043, 7044]
		putWeapon(itemIdToAnimationSet, 11808, "Godsword (Zamorak)"); // Zamorak godsword [Godsword (Ancient), Godsword (Ancient, alternative spec), Godsword (Armadyl), Godsword (Bandos), Godsword (Saradomin), Godsword (Zamorak)] [7053, 7044, 7052, 7052, 7048, 7047, 7043, 7044]
		putWeapon(itemIdToAnimationSet, 11824, "Zamorakian spear"); // Zamorakian spear [Zamorakian spear] [1713, 1702, 1703, 1704, 1706, 1705, 1707, 1702]
		putWeapon(itemIdToAnimationSet, 11838, sarasword); // Saradomin sword [Godsword (Ancient), Godsword (Ancient, alternative spec), Godsword (Armadyl), Godsword (Bandos), Godsword (Saradomin), Godsword (Zamorak)] [7053, 7044, 7052, 7052, 7048, 7047, 7043, 7044]
		putWeapon(itemIdToAnimationSet, 11889, "Zamorakian hasta"); // Zamorakian hasta [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 11902, shortsword); // Leaf-bladed sword [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 11905, poweredstaff); // Trident of the seas (full) [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 11920, pickaxe); // Dragon pickaxe [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 11959, "Chinchompa"); // Black chinchompa [Chinchompa] [3175, 3177, 3177, 3177, 3177, 3177, 3178, 3177]
		putWeapon(itemIdToAnimationSet, 11998, "Staff"); // Smoke battlestaff [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 12000, "Staff"); // Mystic smoke staff [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 12006, "Abyssal whip"); // Abyssal tentacle [Abyssal whip] [808, 823, 1660, 1660, 1660, 1660, 1661, 823]
		putWeapon(itemIdToAnimationSet, 12199, crozier); // Ancient crozier [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 12263, crozier); // Armadyl crozier [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 12275, crozier); // Bandos crozier [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 12297, pickaxe); // Black pickaxe [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 12357, shortsword); // Katana [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 12373, cane); // Dragon cane [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 12375, cane); // Black cane [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 12377, cane); // Adamant cane [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 12379, cane); // Rune cane [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 12389, shortsword); // Gilded scimitar [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 12422, "Staff2/Wand"); // 3rd age wand [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 12424, "Bow"); // 3rd age bow [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 12426, shortsword); // 3rd age longsword [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 12439, cane); // Royal sceptre [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 12727, "Fixed device"); // Goblin paint cannon [Fixed device] [2316, 2321, 2317, 2318, 2319, 2320, 2322, 2321]
		putWeapon(itemIdToAnimationSet, 12808, sarasword); // Sara's blessed sword (full) [Godsword (Ancient), Godsword (Ancient, alternative spec), Godsword (Armadyl), Godsword (Bandos), Godsword (Saradomin), Godsword (Zamorak)] [7053, 7044, 7052, 7052, 7048, 7047, 7043, 7044]
		putWeapon(itemIdToAnimationSet, 12809, sarasword); // Saradomin's blessed sword [Godsword (Ancient), Godsword (Ancient, alternative spec), Godsword (Armadyl), Godsword (Bandos), Godsword (Saradomin), Godsword (Zamorak)] [7053, 7044, 7052, 7052, 7048, 7047, 7043, 7044]
		putWeapon(itemIdToAnimationSet, 12899, poweredstaff); // Trident of the swamp [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 12902, "Staff"); // Toxic staff (uncharged) [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 12924, "Toxic blowpipe"); // Toxic blowpipe (empty) [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 13108, shortsword); // Wilderness sword 1 [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 13141, "Banner"); // Western banner 1 [Banner] [1421, 1426, 1422, 1423, 1424, 1425, 1427, 1426]
		putWeapon(itemIdToAnimationSet, 13241, axe); // Infernal axe [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 13243, pickaxe); // Infernal pickaxe [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 13263, ""); // Abyssal bludgeon [Abyssal bludgeon] [1652, 823, 3293, 3293, 3293, 3293, 2847, 823]
		putWeapon(itemIdToAnimationSet, 13265, "Abyssal dagger"); // Abyssal dagger [Abyssal dagger] [3296, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 13328, "Banner"); // Green banner [Banner] [1421, 1426, 1422, 1423, 1424, 1425, 1427, 1426]
		putWeapon(itemIdToAnimationSet, 13576, "Dragon warhammer"); // Dragon warhammer [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 13652, "Claws"); // Dragon claws [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 19478, "Ballista"); // Light ballista [Ballista] [7220, 823, 7223, 7223, 7223, 7223, 7221, 823]
		putWeapon(itemIdToAnimationSet, 19481, "Ballista"); // Heavy ballista [Ballista] [7220, 823, 7223, 7223, 7223, 7223, 7221, 823]
		putWeapon(itemIdToAnimationSet, 19675, shortsword); // Arclight [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 19918, mace); // Nunchaku [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 19941, "Giant boulder"); // Heavy casket [Giant boulder] [4193, 4194, 4194, 4194, 4194, 4194, 7274, 4194]
		putWeapon(itemIdToAnimationSet, 20011, axe); // 3rd age axe [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 20014, pickaxe); // 3rd age pickaxe [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 20056, "Crystal grail"); // Ale of the gods [Crystal grail] [3040, 823, 3039, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 20155, "2h sword"); // Gilded 2h sword [2h sword] [2561, 823, 2562, 2562, 2562, 2562, 2563, 823]
		putWeapon(itemIdToAnimationSet, 20158, "Spear"); // Gilded spear [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 20161, "Spear"); // Gilded hasta [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 20164, "Godsword (Armadyl)"); // Large spade [Godsword (Ancient), Godsword (Ancient, alternative spec), Godsword (Armadyl), Godsword (Bandos), Godsword (Saradomin), Godsword (Zamorak)] [7053, 7044, 7052, 7052, 7048, 7047, 7043, 7044]
		putWeapon(itemIdToAnimationSet, 20243, mace); // Crier bell [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 20249, "Clueless Scroll"); // Clueless scroll [Clueless Scroll] [7271, 823, 7272, 820, 821, 822, 7273, 823]
		putWeapon(itemIdToAnimationSet, 20251, "Banner"); // Arceuus banner [Banner] [1421, 1426, 1422, 1423, 1424, 1425, 1427, 1426]
		putWeapon(itemIdToAnimationSet, 20254, "Banner"); // Hosidius banner [Banner] [1421, 1426, 1422, 1423, 1424, 1425, 1427, 1426]
		putWeapon(itemIdToAnimationSet, 20257, "Banner"); // Lovakengj banner [Banner] [1421, 1426, 1422, 1423, 1424, 1425, 1427, 1426]
		putWeapon(itemIdToAnimationSet, 20260, "Banner"); // Piscarilius banner [Banner] [1421, 1426, 1422, 1423, 1424, 1425, 1427, 1426]
		putWeapon(itemIdToAnimationSet, 20263, "Banner"); // Shayzien banner [Banner] [1421, 1426, 1422, 1423, 1424, 1425, 1427, 1426]
		putWeapon(itemIdToAnimationSet, 20590, "Rubber chicken"); // Stale baguette [Rubber chicken] [1832, 823, 1830, 1830, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 20720, mace); // Bruma torch [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 20727, "Leaf-bladed battleaxe"); // Leaf-bladed battleaxe [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 20730, "Staff"); // Mist battlestaff [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 20733, "Staff"); // Mystic mist staff [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 20736, "Staff"); // Dust battlestaff [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 20739, "Staff"); // Mystic dust staff [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 20756, ""); // Hill giant club [Dharok's greataxe] [2065, 823, 2064, 2064, 2064, 2064, 824, 823]
		putWeapon(itemIdToAnimationSet, 20779, "Hunting knife"); // Hunting knife [Hunting knife] [2911, 823, 7327, 7327, 821, 822, 2322, 823]
		putWeapon(itemIdToAnimationSet, 20836, "Giant boulder"); // Giant present [Giant boulder] [4193, 4194, 4194, 4194, 4194, 4194, 7274, 4194]
		putWeapon(itemIdToAnimationSet, 20849, "Thrownaxe"); // Dragon thrownaxe [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 20997, "Bow"); // Twisted bow [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 21003, "Elder maul"); // Elder maul [Elder maul] [7518, 823, 7520, 7520, 7520, 7520, 7519, 823]
		putWeapon(itemIdToAnimationSet, 21006, "Staff2/Wand"); // Kodai wand [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 21009, shortsword); // Dragon sword [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 21012, "Crossbow"); // Dragon hunter crossbow [Crossbow, Zaryte crossbow] [4591, 823, 4226, 4227, 821, 822, 4228, 823] uhoh
		putWeapon(itemIdToAnimationSet, 21015, "Dinh's bulwhark"); // Dinh's bulwark [Dinh's bulwhark] [7508, 823, 7510, 7510, 7510, 7510, 7509, 823]
		putWeapon(itemIdToAnimationSet, 21028, harpoon); // Dragon harpoon [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 21031, harpoon); // Infernal harpoon [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 21209, "Birthday balloons"); // Birthday balloons [Birthday balloons] [7538, 823, 7539, 7539, 821, 822, 7540, 823]
		putWeapon(itemIdToAnimationSet, 21354, "Hand fan"); // Hand fan [Hand fan] [6297, 6297, 7629, 7630, 7631, 7632, 7633, 6297]
		putWeapon(itemIdToAnimationSet, 21646, shortsword); // Granite longsword [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 21649, "Spear"); // Merfolk trident [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 21742, mace); // Granite hammer [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 21902, "Crossbow"); // Dragon crossbow [Crossbow, Zaryte crossbow] [4591, 823, 4226, 4227, 821, 822, 4228, 823]
		putWeapon(itemIdToAnimationSet, 22296, "Staff"); // Staff of light [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 22316, "Prop sword/candy cane"); // Prop sword [Prop sword/candy cane] [2911, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 22323, poweredstaff); // Sanguinesti staff [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 22324, "Ghrazi rapier"); // Ghrazi rapier [Dragon longsword/Saeldor, Ghrazi rapier, Osmumten's Fang] [809, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 22325, "Scythe of Vitur"); // Scythe of vitur [Scythe of Vitur] [8057, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 22355, "Unarmed"); // Holy handegg [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 22358, "Unarmed"); // Peaceful handegg [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 22361, "Unarmed"); // Chaotic handegg [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823] uhoh
		putWeapon(itemIdToAnimationSet, 22368, "Staff"); // Bryophyta's staff (uncharged) [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 22398, "Ivandis flail"); // Ivandis flail [Ivandis flail] [8009, 8015, 8011, 8012, 8013, 8014, 8016, 8015]
		putWeapon(itemIdToAnimationSet, 22435, shortsword); // Enchanted emerald sickle (b) [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 22542, "Viggora's chainmace"); // Viggora's chainmace (u) [Viggora's chainmace] [244, 823, 247, 247, 247, 247, 248, 823]
		putWeapon(itemIdToAnimationSet, 22547, "Bow"); // Craw's bow (u) [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 22552, ""); // Thammaron's sceptre (u) no match [813, 1205, 1205, 1206, 1207, 1208, 1210, 1205]
		putWeapon(itemIdToAnimationSet, 22610, "Spear"); // Vesta's spear [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 22613, longsword); // Vesta's longsword [Dragon longsword/Saeldor, Ghrazi rapier, Osmumten's Fang] [809, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 22622, mace); // Statius's warhammer [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 22634, "Thrownaxe"); // Morrigan's throwing axe [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 22636, ""); // Morrigan's javelin [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 22647, "Staff"); // Zuriel's staff [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 22684, ""); // Eek [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 22719, "Prop sword/candy cane"); // Candy cane [Prop sword/candy cane] [2911, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 22731, "Spear"); // Dragon hasta [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 22804, "Dragon knife"); // Dragon knife [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 22840, "Golden tench"); // Golden tench [Golden tench] [8208, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 22842, rod); // Pearl barbarian rod [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 22844, rod); // Pearl fly fishing rod [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 22846, rod); // Pearl fishing rod [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 22978, "Dragon hunter lance"); // Dragon hunter lance [Dragon hunter lance] [813, 1209, 1205, 1206, 1207, 1208, 2563, 1209]
		putWeapon(itemIdToAnimationSet, 23108, "Giant boulder"); // Birthday cake [Giant boulder] [4193, 4194, 4194, 4194, 4194, 4194, 7274, 4194]
		putWeapon(itemIdToAnimationSet, 23122, rod); // Oily pearl fishing rod [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 23206, ""); // Dual sai [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 23276, pickaxe); // Gilded pickaxe [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 23279, axe); // Gilded axe [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 23282, "2h sword"); // Gilded spade [2h sword] [2561, 823, 2562, 2562, 2562, 2562, 2563, 823]
		putWeapon(itemIdToAnimationSet, 23342, "Staff"); // 3rd age druidic staff [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 23357, "Bow"); // Rain bow [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 23360, mace); // Ham joint [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 23363, "Staff"); // Staff of bob the cat [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 23446, "Giant boulder"); // Giant easter egg [Giant boulder] [4193, 4194, 4194, 4194, 4194, 4194, 7274, 4194]
		putWeapon(itemIdToAnimationSet, 23528, "Sarachnis cudgel"); // Sarachnis cudgel [Dragon longsword/Saeldor, Ghrazi rapier, Osmumten's Fang] [809, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 23673, axe); // Crystal axe [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 23680, pickaxe); // Crystal pickaxe [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 23762, harpoon); // Crystal harpoon [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 23895, "Halberd"); // Crystal halberd (basic) [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 23901, "Bow"); // Crystal bow (basic) [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 23995, longsword); // Blade of saeldor [Dragon longsword/Saeldor, Ghrazi rapier, Osmumten's Fang] [809, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 24000, "Crystal grail"); // Crystal grail [Crystal grail] [3040, 823, 3039, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 24144, "Staff"); // Staff of balance [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 24219, dagger); // Swift blade [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 24327, "Skeleton lantern"); // Skeleton lantern [Skeleton lantern] [8521, 8492, 8492, 8492, 8492, 8492, 8492, 8492]
		putWeapon(itemIdToAnimationSet, 24395, cane); // Twisted cane [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 24413, "Banner"); // Twisted banner [Banner] [1421, 1426, 1422, 1423, 1424, 1425, 1427, 1426]
		putWeapon(itemIdToAnimationSet, 24417, "Inquisitor's mace"); // Inquisitor's mace [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 24422, "Nightmare Staff"); // Nightmare staff [Nightmare Staff] [4504, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 24423, "Nightmare Staff"); // Harmonised nightmare staff [Nightmare Staff] [4504, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 24424, "Nightmare Staff"); // Volatile nightmare staff [Nightmare Staff] [4504, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 24425, "Nightmare Staff"); // Eldritch nightmare staff [Nightmare Staff] [4504, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 24537, longsword); // Carrot sword [Dragon longsword/Saeldor, Ghrazi rapier, Osmumten's Fang] [809, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 24539, longsword); // '24-carat' sword [Dragon longsword/Saeldor, Ghrazi rapier, Osmumten's Fang] [809, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 24617, longsword); // Vesta's blighted longsword [Dragon longsword/Saeldor, Ghrazi rapier, Osmumten's Fang] [809, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 24699, "Ivandis flail"); // Blisterwood flail [Ivandis flail] [8009, 8015, 8011, 8012, 8013, 8014, 8016, 8015]
		putWeapon(itemIdToAnimationSet, 24727, mace); // Hallowed hammer [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 24880, ""); // Amy's saw [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 25013, cane); // Trailblazer cane [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 25042, ""); // Trailblazer dragon trophy [Giant boulder] [4193, 4194, 4194, 4194, 4194, 4194, 7274, 4194]
		putWeapon(itemIdToAnimationSet, 25046, ""); // Trailblazer adamant trophy [Giant boulder] [4193, 4194, 4194, 4194, 4194, 4194, 7274, 4194]
		putWeapon(itemIdToAnimationSet, 25314, ""); // Giant boulder [Giant boulder] [4193, 4194, 4194, 4194, 4194, 4194, 7274, 4194]
		putWeapon(itemIdToAnimationSet, 25484, ""); // Soulreaper axe [Dharok's greataxe] [2065, 823, 2064, 2064, 2064, 2064, 824, 823]
		putWeapon(itemIdToAnimationSet, 25489, ""); // Blood ancient sceptre [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 25490, ""); // Ice ancient sceptre [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 25491, ""); // Smoke ancient sceptre [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 25492, ""); // Shadow ancient sceptre [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 25500, ""); // Cursed banana [Cursed banana] [4646, 823, 4682, 6276, 6268, 6275, 6277, 823]
		putWeapon(itemIdToAnimationSet, 25604, ""); // Gregg's eastdoor [Dinh's bulwhark] [7508, 823, 7510, 7510, 7510, 7510, 7509, 823]
		putWeapon(itemIdToAnimationSet, 25641, mace); // Barronite mace [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 25644, mace); // Imcando hammer [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 25721, ""); // Clan vexillum [Clan vexillum] [9018, 7044, 9017, 9017, 9021, 9020, 9019, 7044]
		putWeapon(itemIdToAnimationSet, 25731, poweredstaff); // Holy sanguinesti staff [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 25734, "Ghrazi rapier"); // Holy ghrazi rapier [Dragon longsword/Saeldor, Ghrazi rapier, Osmumten's Fang] [809, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 25736, "Scythe of Vitur"); // Holy scythe of vitur [Nightmare Staff] [4504, 1209, 1205, 1206, 1207, 1208, 1210, 1209] uhoh
		putWeapon(itemIdToAnimationSet, 25739, "Scythe of Vitur"); // Sanguine scythe of vitur [Scythe of Vitur] [8057, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 25822, ""); // Protest banner [Clan vexillum] [9018, 7044, 9017, 9017, 9021, 9020, 9019, 7044]
		putWeapon(itemIdToAnimationSet, 25849, "Dart"); // Amethyst dart [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 25862, "Bow"); // Bow of faerdhinen (inactive) [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 25979, keris); // Keris partisan [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 25981, keris); // Keris partisan of breaching [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 26219, "Osmumten's Fang"); // Osmumten's fang [Dragon longsword/Saeldor, Ghrazi rapier, Osmumten's Fang] [809, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 26233, "Godsword (Ancient)"); // Ancient godsword [Godsword (Ancient), Godsword (Ancient, alternative spec), Godsword (Armadyl), Godsword (Bandos), Godsword (Saradomin), Godsword (Zamorak)] [7053, 7044, 7052, 7052, 7048, 7047, 7043, 7044]
		putWeapon(itemIdToAnimationSet, 26260, mace); // Haunted wine bottle [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 26374, "Zaryte crossbow"); // Zaryte crossbow [Crossbow, Zaryte crossbow] [4591, 823, 4226, 4227, 821, 822, 4228, 823]
		putWeapon(itemIdToAnimationSet, 26424, ""); // Shattered banner no match [9263, 9268, 9264, 9265, 9266, 9267, 9269, 9268]
		putWeapon(itemIdToAnimationSet, 26515, ""); // Shattered relics dragon trophy no match [9272, 9273, 9273, 9273, 9273, 9273, 9261, 9273]
		putWeapon(itemIdToAnimationSet, 26517, ""); // Shattered cane no match [813, 823, 1146, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 26945, ""); // Pharaoh's sceptre (uncharged) [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 27021, ""); // Colossal blade [Colossal Blade] [9460, 10055, 9461, 9461, 10055, 10054, 9459, 10055]
		putWeapon(itemIdToAnimationSet, 27275, ""); // Tumeken's shadow [Tumeken's Shadow] [9494, 1702, 1703, 1704, 1706, 1705, 1707, 1702]
		putWeapon(itemIdToAnimationSet, 27287, keris); // Keris partisan of corruption [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 27291, keris); // Keris partisan of the sun [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 27414, ""); // Giant stopwatch no match [9814, 9813, 9813, 9813, 9813, 9813, 9815, 9813]
		putWeapon(itemIdToAnimationSet, 27580, ""); // Festive nutcracker staff [Banner] [1421, 1426, 1422, 1423, 1424, 1425, 1427, 1426]
		putWeapon(itemIdToAnimationSet, 27586, ""); // Sweet nutcracker staff [Banner] [1421, 1426, 1422, 1423, 1424, 1425, 1427, 1426]
		putWeapon(itemIdToAnimationSet, 27610, ""); // Venator bow [Venator Bow] [9857, 9863, 9859, 9859, 9861, 9862, 9860, 9863]
		putWeapon(itemIdToAnimationSet, 27624, ""); // Ancient sceptre [Comp bow, Guthan's warspear, Halberd, Spear, Staff, Staff2/Wand, Toxic blowpipe, Trident of the swamp, Zamorakian hasta] [813, 1209, 1205, 1206, 1207, 1208, 1210, 1209]
		putWeapon(itemIdToAnimationSet, 27645, ""); // Mystic cards [Mystic cards] [9847, 823, 9849, 820, 9851, 9852, 9850, 823]
		putWeapon(itemIdToAnimationSet, 27652, "Bow"); // Webweaver bow (u) [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 27657, ""); // Ursine chainmace (u) [Viggora's chainmace] [244, 823, 247, 247, 247, 247, 248, 823]
		putWeapon(itemIdToAnimationSet, 27662, ""); // Accursed sceptre (u) no match [813, 1205, 1205, 1206, 1207, 1208, 1210, 1205]
		putWeapon(itemIdToAnimationSet, 27690, shortsword); // Voidwaker [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 27810, "Dragon dagger"); // Dragon candle dagger [Ancient mace, Arclight, Bow, Claws, Dart, Dragon axe, Dragon battleaxe, Dragon dagger, Dragon knife, Dragon knife (poisoned), Dragon mace, Dragon scimitar, Dragon sword, Dragon warhammer, Inquisitor's mace, Knife (non-dragon), Leaf-bladed battleaxe, Snowball, Thrownaxe, Torag's hammers, Unarmed] [808, 823, 819, 820, 821, 822, 824, 823]
		putWeapon(itemIdToAnimationSet, 27820, ""); // 10th birthday balloons no match [10032, 823, 10033, 10033, 821, 822, 10034, 823]
		putWeapon(itemIdToAnimationSet, 27871, ""); // Giant bronze dagger [Colossal Blade] [9460, 10055, 9461, 9461, 10055, 10054, 9459, 10055]

		Map<String, List<Integer>> poseanims = new HashMap<>();
		for (Map.Entry<Integer, AnimationSet> entry : itemIdToAnimationSet.entrySet())
		{
			List<Integer> itemIds = poseanims.getOrDefault(entry.getValue().name, new ArrayList<>());
			itemIds.add(entry.getKey());
			poseanims.put(entry.getValue().name, itemIds);
		}
		data.poseanims = new HashMap<>();
		for (Map.Entry<String, List<Integer>> entry : poseanims.entrySet())
		{
			data.poseanims.put(entry.getKey(), entry.getValue().stream().mapToInt(i -> i).sorted().toArray());
		}

		/*
		Types of projectiles:
		hit gfx (most spells).
			the hit gfx is applied when the spell casts, but has a timer before it actually shows.
				This means if you're at a very long range it is possible for the hit gfx to never occur because it is replaced by the next spell cast before it goes off.
			many of these can splash animation instead.
		no hit gfx (ranged attacks mostly).
		no cast gfx or projectile - requires additional information to identify what spell was cast.
		no projectile spells (such as ice barrage) - when replacing, requires hit delay to be calculated.
		TODO multiple projectile spells (such as dark bow).
			Dark bow spec is actually 4 projectiles (with 2 different versions, one with dragon arrows one without).
		enchanted bolts.
		 */
		// TODO any arrow, any spell.

		List<ProjectileCast> projectiles = new ArrayList<>();
		projectiles.add(p().id(0).name("Wind Strike").sprite(SpriteID.SPELL_WIND_STRIKE).cast(1162, 90).projectile(91, 51, 64, -172, 124, 16).hitGfx(92, 124).build());
		projectiles.add(p().id(1).name("Confuse").sprite(SpriteID.SPELL_CONFUSE).cast(1163, 102).projectile(103, 61, 64, -172, 124, 16).hitGfx(104, 124).build());
		projectiles.add(p().id(2).name("Water Strike").sprite(SpriteID.SPELL_WATER_STRIKE).cast(1162, 93).projectile(94, 51, 64, -172, 124, 16).hitGfx(95, 124).build());
		projectiles.add(p().id(3).name("Earth Strike").sprite(SpriteID.SPELL_EARTH_STRIKE).cast(1162, 96).projectile(97, 51, 64, -172, 124, 16).hitGfx(98, 124).build());
		projectiles.add(p().id(4).name("Weaken").sprite(SpriteID.SPELL_WEAKEN).cast(1164, 105).projectile(106, 44, 64, -172, 124, 16).hitGfx(107, 124).build());
		projectiles.add(p().id(5).name("Fire Strike").sprite(SpriteID.SPELL_FIRE_STRIKE).cast(1162, 99).projectile(100, 51, 64, -172, 124, 16).hitGfx(101, 124).build());
		projectiles.add(p().id(6).name("Wind Bolt").sprite(SpriteID.SPELL_WIND_BOLT).cast(1162, 117).projectile(118, 51, 64, -172, 124, 16).hitGfx(119, 124).build());
		projectiles.add(p().id(7).name("Curse").sprite(SpriteID.SPELL_CURSE).cast(1165, 108).projectile(109, 51, 64, -172, 124, 16).hitGfx(110, 124).build());
		projectiles.add(p().id(8).name("Bind").sprite(SpriteID.SPELL_BIND).cast(1161, 177).projectile(178, 75, 64, -172, 0, 16).hitGfx(181, 0).hitGfx(181, 124).build());
		projectiles.add(p().id(9).name("Water Bolt").sprite(SpriteID.SPELL_WATER_BOLT).cast(1162, 120).projectile(121, 51, 64, -172, 124, 16).hitGfx(122, 124).build());
		projectiles.add(p().id(10).name("Earth Bolt").sprite(SpriteID.SPELL_EARTH_BOLT).cast(1162, 123).projectile(124, 51, 64, -172, 124, 16).hitGfx(125, 124).build());
		projectiles.add(p().id(11).name("Telegrab").sprite(SpriteID.SPELL_TELEKINETIC_GRAB).cast(723, 142).projectile(143, 48, 64, -172, 0, 16).hitGfx(144, 0).build());
		projectiles.add(p().id(12).name("Fire Bolt").sprite(SpriteID.SPELL_FIRE_BOLT).cast(1162, 126).projectile(127, 51, 64, -172, 124, 16).hitGfx(128, 124).build());
		projectiles.add(p().id(13).name("Crumble Undead").sprite(SpriteID.SPELL_CRUMBLE_UNDEAD).cast(1166, 145).projectile(146, 46, 64, -172, 124, 16).hitGfx(147, 124).build());
		projectiles.add(p().id(14).name("Wind Blast").sprite(SpriteID.SPELL_WIND_BLAST).cast(1162, 132).projectile(133, 51, 64, -172, 124, 16).hitGfx(134, 124).build());
		projectiles.add(p().id(15).name("Water Blast").sprite(SpriteID.SPELL_WATER_BLAST).cast(1162, 135).projectile(136, 51, 64, -172, 124, 16).hitGfx(137, 124).build());
		projectiles.add(p().id(16).name("Iban Blast").sprite(SpriteID.SPELL_IBAN_BLAST).cast(708, 87).projectile(88, 60, 64, -172, 124, 16).hitGfx(89, 124).build());
		projectiles.add(p().id(17).name("Snare").sprite(SpriteID.SPELL_SNARE).cast(1161, 177).projectile(178, 75, 64, -172, 0, 16).hitGfx(180, 0).hitGfx(180, 124).build());
		projectiles.add(p().id(18).name("Magic Dart").sprite(SpriteID.SPELL_MAGIC_DART).cast(1576, -1).projectile(328, 51, 64, -172, 124, 16).hitGfx(329, 124).build());
		projectiles.add(p().id(19).name("Earth Blast").sprite(SpriteID.SPELL_EARTH_BLAST).cast(1162, 138).projectile(139, 51, 64, -172, 124, 16).hitGfx(140, 124).build());
		projectiles.add(p().id(20).name("Fire Blast").sprite(SpriteID.SPELL_FIRE_BLAST).cast(1162, 129).projectile(130, 51, 64, -172, 124, 16).hitGfx(131, 124).build());
		projectiles.add(p().id(21).name("Saradomin Strike").sprite(SpriteID.SPELL_SARADOMIN_STRIKE).ids(811, -1, 76).build());
		projectiles.add(p().id(22).name("Claws of Guthix").sprite(SpriteID.SPELL_CLAWS_OF_GUTHIX).ids(811, -1, 77).build());
		projectiles.add(p().id(23).name("Flames of Zamorak").sprite(SpriteID.SPELL_FLAMES_OF_ZAMORAK).ids(811, -1, 78).build());
		projectiles.add(p().id(24).name("Wind Wave").sprite(SpriteID.SPELL_WIND_WAVE).cast(1167, 158).projectile(159, 51, 64, -172, 124, 16).hitGfx(160, 124).build());
		projectiles.add(p().id(25).name("Water Wave").sprite(SpriteID.SPELL_WATER_WAVE).cast(1167, 161).projectile(162, 51, 64, -172, 124, 16).hitGfx(163, 124).build());
		projectiles.add(p().id(26).name("Vulnerability").sprite(SpriteID.SPELL_VULNERABILITY).cast(1165, 167).projectile(168, 34, 64, -172, 124, 16).hitGfx(169, 124).build());
		projectiles.add(p().id(27).name("Earth Wave").sprite(SpriteID.SPELL_EARTH_WAVE).cast(1167, 164).projectile(165, 51, 64, -172, 124, 16).hitGfx(166, 124).build());
		projectiles.add(p().id(28).name("Enfeeble").sprite(SpriteID.SPELL_ENFEEBLE).cast(1168, 170).projectile(171, 48, 64, -172, 124, 16).hitGfx(172, 124).build());
		projectiles.add(p().id(29).name("Fire Wave").sprite(SpriteID.SPELL_FIRE_WAVE).cast(1167, 155).projectile(156, 51, 64, -172, 124, 16).hitGfx(157, 124).build());
		projectiles.add(p().id(30).name("Entangle").sprite(SpriteID.SPELL_ENTANGLE).cast(1161, 177).projectile(178, 75, 64, -172, 0, 16).hitGfx(179, 0).hitGfx(179, 124).build());
		projectiles.add(p().id(31).name("Stun").sprite(SpriteID.SPELL_STUN).cast(1169, 173).projectile(174, 52, 64, -172, 124, 16).hitGfx(80, 124).build());
		projectiles.add(p().id(32).name("Wind Surge").sprite(SpriteID.SPELL_WIND_SURGE).cast(7855, 1455).projectile(1456, 51, 64, -172, 124, 16).hitGfx(1457, 124).build());
		projectiles.add(p().id(33).name("Water Surge").sprite(SpriteID.SPELL_WATER_SURGE).cast(7855, 1458).projectile(1459, 51, 64, -172, 124, 16).hitGfx(1460, 124).build());
		projectiles.add(p().id(34).name("Earth Surge").sprite(SpriteID.SPELL_EARTH_SURGE).cast(7855, 1461).projectile(1462, 51, 64, -172, 124, 16).hitGfx(1463, 124).build());
		projectiles.add(p().id(35).name("Fire Surge").sprite(SpriteID.SPELL_FIRE_SURGE).cast(7855, 1464).projectile(1465, 51, 64, -172, 124, 16).hitGfx(1466, 124).build());

		// Ancient spellbook.
		projectiles.add(p().id(36).name("Smoke Rush").sprite(SpriteID.SPELL_SMOKE_RUSH).ids(1978, -1, 384, 385, 51, 64, 124, 16).build());
		projectiles.add(p().id(37).name("Shadow Rush").sprite(SpriteID.SPELL_SHADOW_RUSH).ids(1978, -1, 378, 379, 51, 64, 0, 16).build());
		projectiles.add(p().id(38).name("Blood Rush").sprite(SpriteID.SPELL_BLOOD_RUSH).ids(1978, -1, 373).build());
		projectiles.add(p().id(39).name("Ice Rush").sprite(SpriteID.SPELL_ICE_RUSH).ids(1978, -1, 360, 361, 51, 64, 0, 16).build());
		projectiles.add(p().id(40).name("Smoke Burst").sprite(SpriteID.SPELL_SMOKE_BURST).ids(1979, -1, 389).build());
		projectiles.add(p().id(41).name("Shadow Burst").sprite(SpriteID.SPELL_SHADOW_BURST).ids(1979, -1, 382).build());
		projectiles.add(p().id(42).name("Blood Burst").sprite(SpriteID.SPELL_BLOOD_BURST).ids(1979, -1, 376).build());
		projectiles.add(p().id(43).name("Ice Burst").sprite(SpriteID.SPELL_ICE_BURST).ids(1979, -1, 363).build());
		projectiles.add(p().id(44).name("Smoke Blitz").sprite(SpriteID.SPELL_SMOKE_BLITZ).ids(1978, -1, 386, 387, 51, 64, 124, 16).build());
		projectiles.add(p().id(45).name("Shadow Blitz").sprite(SpriteID.SPELL_SHADOW_BLITZ).ids(1978, -1, 380, 381, 51, 64, 0, 16).build());
		projectiles.add(p().id(46).name("Blood Blitz").sprite(SpriteID.SPELL_BLOOD_BLITZ).ids(1978, -1, 374, 375, 51, 64, 0, 16).build());
		projectiles.add(p().id(47).name("Ice Blitz").sprite(SpriteID.SPELL_ICE_BLITZ).ids(1978, 366, 367).build());
		projectiles.add(p().id(48).name("Smoke Barrage").sprite(SpriteID.SPELL_SMOKE_BARRAGE).ids(1979, -1, 391).build());
		projectiles.add(p().id(49).name("Shadow Barrage").sprite(SpriteID.SPELL_SHADOW_BARRAGE).ids(1979, -1, 383).build());
		projectiles.add(p().id(50).name("Blood Barrage").sprite(SpriteID.SPELL_BLOOD_BARRAGE).ids(1979, -1, 377).build());
		projectiles.add(p().id(51).name("Ice Barrage").sprite(SpriteID.SPELL_ICE_BARRAGE).ids(1979, -1, 369).build());

		// Arceuus spellbook.
		projectiles.add(p().id(52).name("Ghostly Grasp").sprite(SpriteID.SPELL_GHOSTLY_GRASP).ids(8972, 1856, 1858).build());
		projectiles.add(p().id(53).name("Skeletal Grasp").sprite(SpriteID.SPELL_SKELETAL_GRASP).ids(8972, 1859, 1861).build());
		projectiles.add(p().id(54).name("Undead Grasp").sprite(SpriteID.SPELL_UNDEAD_GRASP).ids(8972, 1862, 1863).build());
		projectiles.add(p().id(55).name("Inferior Demonbane").sprite(SpriteID.SPELL_INFERIOR_DEMONBANE).ids(8977, 1865, 1866).build());
		projectiles.add(p().id(56).name("Superior Demonbane").sprite(SpriteID.SPELL_SUPERIOR_DEMONBANE).ids(8977, 1867, 1868).build());
		projectiles.add(p().id(57).name("Dark Demonbane").sprite(SpriteID.SPELL_DARK_DEMONBANE).ids(8977, 1869, 1870).build());
		projectiles.add(p().id(58).name("Dark Lure").sprite(SpriteID.SPELL_DARK_LURE).ids(8974, 1882, 1884).build());

		// Powered staves.
		// TODO black trident. I forget the ID.
		projectiles.add(p().id(59).itemId(ItemID.TRIDENT_OF_THE_SEAS).ids(1167, 1251, 1252, 1253, 51, 64, 60, 16).build());
		projectiles.add(p().id(60).itemId(ItemID.TRIDENT_OF_THE_SWAMP).ids(1167, 665, 1040, 1042, 51, 64, 60, 16).build());
		projectiles.add(p().id(61).name("trident (purple and gold)").itemId(ItemID.GOLDEN_SCARAB).ids(1167, 1543, 1544, 1545, 51, 64, 60, 16).artificial().build());
		projectiles.add(p().id(62).name("trident (purple and silver)").itemId(ItemID.STONE_SCARAB).ids(1167, 1546, 1547, 1548, 51, 64, 60, 16).artificial().build());
		projectiles.add(p().id(63).name("Sanguinesti staff (regular)").itemId(ItemID.SANGUINESTI_STAFF).ids(1167, 1540, 1539, 1541, 51, 64, 60, 16).build());
		projectiles.add(p().id(64).name("Sanguinesti staff (health restore)").itemId(ItemID.SANGUINESTI_STAFF).ids(1167, 1540, 1539, 1542, 51, 64, 60, 16).build());
		projectiles.add(p().id(65).name("Holy sanguinesti staff (regular)").itemId(ItemID.HOLY_SANGUINESTI_STAFF).ids(1167, 1900, 1899, 1901, 51, 64, 60, 16).build());
		projectiles.add(p().id(66).name("Holy sanguinesti staff (health restore)").itemId(ItemID.HOLY_SANGUINESTI_STAFF).ids(1167, 1900, 1899, 1902, 51, 64, 60, 16).build());
		projectiles.add(p().id(157).itemId(ItemID.TUMEKENS_SHADOW).cast(9493, 2125).projectile(2126, 56, 40, -400, 124, 32).hitGfx(2127, 124).build());

		// Arrows. Many values guessed based off of iron arrow, so stuff like height/slope could be off for some arrows.
		projectiles.add(p().id(67).itemId(ItemID.BRONZE_ARROW).ids(426, 19, 10, -1, 41, 11, 144, 15).build());
		projectiles.add(p().id(68).itemId(ItemID.IRON_ARROW).ids(426, 18, 9, -1, 41, 11, 144, 15).build());
		projectiles.add(p().id(69).itemId(ItemID.STEEL_ARROW).ids(426, 20, 11, -1, 41, 11, 144, 15).build());
		projectiles.add(p().id(70).name("Black arrow").itemId(ItemID.HEADLESS_ARROW).ids(426, 23, 14, -1, 41, 11, 144, 15).build());
		projectiles.add(p().id(71).itemId(ItemID.MITHRIL_ARROW).ids(426, 21, 12, -1, 41, 11, 144, 15).build());
		projectiles.add(p().id(72).itemId(ItemID.ADAMANT_ARROW).ids(426, 22, 13, -1, 41, 11, 144, 15).build());
		projectiles.add(p().id(73).itemId(ItemID.RUNE_ARROW).ids(426, 24, 15, -1, 41, 11, 144, 15).build());
		projectiles.add(p().id(74).itemId(ItemID.AMETHYST_ARROW).ids(426, 1385, 1384, -1, 41, 11, 144, 15).build());
		projectiles.add(p().id(75).itemId(ItemID.DRAGON_ARROW).ids(426, 1116, 1120, -1, 41, 11, 144, 15).build());
		projectiles.add(p().id(76).itemId(ItemID.ICE_ARROWS).ids(426, 25, 16, -1, 41, 11, 144, 15).build());
		projectiles.add(p().id(77).name("Fire arrow").itemId(ItemID.BRONZE_FIRE_ARROW_LIT).ids(426, 26, 17, -1, 41, 11, 144, 15).build());
		projectiles.add(p().id(78).itemId(ItemID.TRAINING_ARROWS).ids(426, 806, 805, -1, 41, 11, 144, 15).build());
		projectiles.add(p().id(79).itemId(ItemID.CRYSTAL_BOW).ids(426, 250, 249, -1, 41, 11, 144, 15).build());
		projectiles.add(p().id(80).itemId(ItemID.OGRE_ARROW).ids(426, 243, 242, -1, 41, 11, 144, 15).build());
		projectiles.add(p().id(142).name("Dark bow spec (non-dragon arrows)").itemId(ItemID.DARK_BOW).ids(426, 1105, 1101, 1103, 41, 11, 144, 5).build());
		projectiles.add(p().id(143).name("Dark bow spec (dragon arrows)").itemId(ItemID.DARK_BOW).ids(426, 1111, 1099, 1100, 41, 11, 144, 5).build());
		projectiles.add(p().id(144).name("Seercull").itemId(ItemID.SEERCULL).ids(426, 472, 473, 474, 41, 11, 144, 15).hitGfx(474, 0).build());
		// TODO ba arrows, brutal arrow, broad arrow.
		// TODO specs (seercull, msb, magic longbow), dark bow.

		// bow of faerdhinen bofa
		projectiles.add(p().id(81).itemId(ItemID.BOW_OF_FAERDHINEN).ids(426, 1889, 1888, -1, 41, 11, 144, 15).build());
		projectiles.add(p().id(82).name("Bow of faerdhinen (red)").itemId(ItemID.BOW_OF_FAERDHINEN_C_25884).ids(426, 1923, 1922, -1, 41, 11, 144, 15).build());
		projectiles.add(p().id(83).name("Bow of faerdhinen (white)").itemId(ItemID.BOW_OF_FAERDHINEN_C_25886).ids(426, 1925, 1924, -1, 41, 11, 144, 15).build());
		projectiles.add(p().id(84).name("Bow of faerdhinen (black)").itemId(ItemID.BOW_OF_FAERDHINEN_C_25888).ids(426, 1927, 1926, -1, 41, 11, 144, 15).build());
		projectiles.add(p().id(85).name("Bow of faerdhinen (purple)").itemId(ItemID.BOW_OF_FAERDHINEN_C_25890).ids(426, 1929, 1928, -1, 41, 11, 144, 15).build());
		projectiles.add(p().id(86).name("Bow of faerdhinen (green)").itemId(ItemID.BOW_OF_FAERDHINEN_C_25892).ids(426, 1931, 1930, -1, 41, 11, 144, 15).build());
		projectiles.add(p().id(87).name("Bow of faerdhinen (yellow)").itemId(ItemID.BOW_OF_FAERDHINEN_C_25894).ids(426, 1933, 1932, -1, 41, 11, 144, 15).build());
		projectiles.add(p().id(88).name("Bow of faerdhinen (blue)").itemId(ItemID.BOW_OF_FAERDHINEN_C_25896).ids(426, 1935, 1934, -1, 41, 11, 144, 15).build());

		// Bolts.
		projectiles.add(p().id(89).name("Bolts").itemId(ItemID.RUNITE_BOLTS).ids(7552, -1, 27, -1, 41, 11, 144, 5).build());
		projectiles.add(p().id(141).name("Dragon crossbow spec").itemId(ItemID.DRAGON_CROSSBOW).ids(4230, -1, 698, 157, 41, 11, 144, 5).build());
		// TODO bolt effects.
		// diamond (e) 9168, -1, 27, 758, 41, 11, 144, 5, 0
		// ruby (e) 9168, -1, 27, 754, 41, 11, 144, 5, 0
		// TODO it would be neat if different bolt types could have different projectiles.

		// Knives.
		projectiles.add(p().id(90).itemId(ItemID.BRONZE_KNIFE).ids(7617, 219, 212, -1, 32, 11, 144, 15).build());
		projectiles.add(p().id(91).itemId(ItemID.IRON_KNIFE).ids(7617, 220, 213, -1, 32, 11, 144, 15).build());
		projectiles.add(p().id(92).itemId(ItemID.STEEL_KNIFE).ids(7617, 221, 214, -1, 32, 11, 144, 15).build());
		projectiles.add(p().id(93).itemId(ItemID.BLACK_KNIFE).ids(7617, 222, 215, -1, 32, 11, 144, 15).build());
		projectiles.add(p().id(94).itemId(ItemID.MITHRIL_KNIFE).ids(7617, 223, 216, -1, 32, 11, 144, 15).build());
		projectiles.add(p().id(95).itemId(ItemID.ADAMANT_KNIFE).ids(7617, 224, 217, -1, 32, 11, 144, 15).build());
		projectiles.add(p().id(96).itemId(ItemID.RUNE_KNIFE).ids(7617, 225, 218, -1, 32, 11, 144, 15).build());
		projectiles.add(p().id(97).itemId(ItemID.DRAGON_KNIFE).ids(8194, -1, 28, -1, 32, 11, 144, 15).build());
		projectiles.add(p().id(98).name("Dragon knife (spec)").itemId(ItemID.DRAGON_KNIFE).ids(8291, -1, 699, -1, 25, 11, 144, 15).build());
		projectiles.add(p().id(99).itemId(ItemID.DRAGON_KNIFEP_22808).ids(8195, -1, 697, -1, 32, 11, 144, 15).build());
		projectiles.add(p().id(100).name("Dragon knife (p++) (spec)").itemId(ItemID.DRAGON_KNIFEP_22808).ids(8292, -1, 1629, -1, 25, 11, 144, 15).build());

		// Darts.
		projectiles.add(p().id(101).itemId(ItemID.BRONZE_DART).ids(7554, -1, 226, -1, 32, 11, 144, 15).build());
		projectiles.add(p().id(102).itemId(ItemID.IRON_DART).ids(7554, -1, 227, -1, 32, 11, 144, 15).build());
		projectiles.add(p().id(103).itemId(ItemID.STEEL_DART).ids(7554, -1, 228, -1, 32, 11, 144, 15).build());
		projectiles.add(p().id(104).itemId(ItemID.BLACK_DART).ids(7554, -1, 32, -1, 32, 11, 144, 15).build());
		projectiles.add(p().id(105).itemId(ItemID.MITHRIL_DART).ids(7554, -1, 229, -1, 32, 11, 144, 15).build());
		projectiles.add(p().id(106).itemId(ItemID.ADAMANT_DART).ids(7554, -1, 230, -1, 32, 11, 144, 15).build());
		projectiles.add(p().id(107).itemId(ItemID.RUNE_DART).ids(7554, -1, 231, -1, 32, 11, 144, 15).build());
		projectiles.add(p().id(108).itemId(ItemID.AMETHYST_DART).ids(7554, -1, 1936, -1, 32, 11, 144, 15).build());
		projectiles.add(p().id(109).itemId(ItemID.DRAGON_DART).ids(7554, -1, 1122, -1, 32, 11, 144, 15).build());

		// Blowpipe.
		projectiles.add(p().id(110).name("Bronze Dart").itemId(ItemID.TOXIC_BLOWPIPE).ids(5061, -1, 226, -1, 32, 105, 144, 15).build());
		projectiles.add(p().id(111).name("Iron Dart").itemId(ItemID.TOXIC_BLOWPIPE).ids(5061, -1, 227, -1,  32, 105, 144, 15).build());
		projectiles.add(p().id(112).name("Steel Dart").itemId(ItemID.TOXIC_BLOWPIPE).ids(5061, -1, 228, -1, 32, 105, 144, 15).build());
		projectiles.add(p().id(113).name("Black Dart").itemId(ItemID.TOXIC_BLOWPIPE).ids(5061, -1, 32, -1, 32, 105, 144, 15).build());
		projectiles.add(p().id(114).name("Mithril Dart").itemId(ItemID.TOXIC_BLOWPIPE).ids(5061, -1, 229, -1, 32, 105, 144, 15).build());
		projectiles.add(p().id(115).name("Adamant Dart").itemId(ItemID.TOXIC_BLOWPIPE).ids(5061, -1, 230, -1, 32, 105, 144, 15).build());
		projectiles.add(p().id(116).name("Rune Dart").itemId(ItemID.TOXIC_BLOWPIPE).ids(5061, -1, 231, -1, 32, 105, 144, 15).build());
		projectiles.add(p().id(117).name("Amethyst Dart").itemId(ItemID.TOXIC_BLOWPIPE).ids(5061, -1, 1936, -1, 32, 105, 144, 15).build());
		projectiles.add(p().id(118).name("Dragon Dart").itemId(ItemID.TOXIC_BLOWPIPE).ids(5061, -1, 1122, -1, 32, 105, 144, 15).build());

		// Thrownaxes.
		projectiles.add(p().id(119).itemId(ItemID.BRONZE_THROWNAXE).ids(7617, 43, 36, -1, 32, 11, 144, 15).build());
		projectiles.add(p().id(120).itemId(ItemID.IRON_THROWNAXE).ids(7617, 42, 35, -1, 32, 11, 144, 15).build());
		projectiles.add(p().id(121).itemId(ItemID.STEEL_THROWNAXE).ids(7617, 44, 37, -1, 32, 11, 144, 15).build());
		projectiles.add(p().id(122).itemId(ItemID.MITHRIL_THROWNAXE).ids(7617, 45, 38, -1, 32, 11, 144, 15).build());
		projectiles.add(p().id(123).itemId(ItemID.ADAMANT_THROWNAXE).ids(7617, 46, 39, -1, 32, 11, 144, 15).build());
		projectiles.add(p().id(124).itemId(ItemID.RUNE_THROWNAXE).ids(7617, 48, 41, -1, 32, 11, 144, 15).build());
		projectiles.add(p().id(125).name("Rune thrownaxe (spec)").itemId(ItemID.RUNE_THROWNAXE).ids(1068, 257, 258, -1, 41, 11, 144, 0).build());
		projectiles.add(p().id(126).itemId(ItemID.DRAGON_THROWNAXE).ids(7617, 1320, 1319, -1, 32, 11, 144, 15).build());
		projectiles.add(p().id(127).name("Dragon thrownaxe (spec)").itemId(ItemID.DRAGON_THROWNAXE).ids(7521, 1317, 1318, -1, 25, 11, 144, 15).build());

		// javelins.
		projectiles.add(p().id(128).itemId(ItemID.BRONZE_JAVELIN).ids(7555, -1, 200, -1, 32, 11, 144, 15).build());
		projectiles.add(p().id(129).itemId(ItemID.IRON_JAVELIN).ids(7555, -1, 201, -1, 32, 11, 144, 15).build());
		projectiles.add(p().id(130).itemId(ItemID.STEEL_JAVELIN).ids(7555, -1, 202, -1, 32, 11, 144, 15).build());
		projectiles.add(p().id(131).itemId(ItemID.MITHRIL_JAVELIN).ids(7555, -1, 203, -1, 32, 11, 144, 15).build());
		projectiles.add(p().id(132).itemId(ItemID.ADAMANT_JAVELIN).ids(7555, -1, 204, -1, 32, 11, 144, 15).build());
		projectiles.add(p().id(133).itemId(ItemID.RUNE_JAVELIN).ids(7555, -1, 205, -1, 32, 11, 144, 15).build());
		projectiles.add(p().id(134).itemId(ItemID.AMETHYST_JAVELIN).ids(7555, -1, 1386, -1, 32, 11, 144, 15).build());
		projectiles.add(p().id(135).itemId(ItemID.DRAGON_JAVELIN).ids(7555, -1, 1301, -1, 32, 11, 144, 15).build());

		projectiles.add(p().id(136).itemId(ItemID.BLACK_CHINCHOMPA).ids(7618, -1, 1272, 157, 21, 11, 144, 15).build()); // only has hit gfx when in multicombat area.
		projectiles.add(p().id(137).itemId(ItemID.RED_CHINCHOMPA).ids(7618, -1, 909, 157, 21, 11, 144, 15).hitGfx(157, 0).build()); // only has hit gfx when in multicombat area.
		projectiles.add(p().id(138).itemId(ItemID.CHINCHOMPA).ids(7618, -1, 908, 157, 21, 11, 144, 15).hitGfx(157, 0).build()); // only has hit gfx when in multicombat area.

		projectiles.add(p().id(139).itemId(ItemID.TOKTZXILUL).ids(7558, -1, 442, -1, 32, 11, 144, 15).build());

		projectiles.add(p().id(140).name("Snowball").itemId(ItemID.SNOWBALL).ids(5063, 860, 861, 862, 62, 11, 44, 15).build());
		projectiles.add(p().id(148).name("Rotten tomato").itemId(ItemID.ROTTEN_TOMATO).ids(5063, 30, 29, 31, 62, 11, 44, 15).build());
		projectiles.add(p().id(149).name("Rock").itemId(ItemID.PET_ROCK).ids(5063, 33, 32, -1, 62, 11, 44, 15).build());
		projectiles.add(p().id(150).itemId(ItemID.VIAL).ids(7617, 50, 49, -1, 32, 11, 144, 15).build());
		projectiles.add(p().id(151).itemId(ItemID.ENCHANTED_VIAL).ids(7617, 52, 51, -1, 32, 11, 144, 15).build());
		projectiles.add(p().id(152).itemId(ItemID.HOLY_WATER).ids(7617, 193, 192, -1, 32, 11, 144, 15).build());
		projectiles.add(p().id(153).itemId(ItemID.NINJA_IMPLING_JAR).ids(7617, 210, 211, 209, 32, 11, 144, 15).hitGfx(209, 0).build());

		projectiles.add(p().id(145).name("Corp sperm 1").sprite(SpriteID.SPELL_WIND_STRIKE).ids(1162, 90, 314, 92, 51, 64, 124, 16).artificial().build());
		projectiles.add(p().id(146).name("Corp sperm 2").sprite(SpriteID.SPELL_WIND_STRIKE).ids(1162, 90, 315, 92, 51, 64, 124, 16).artificial().build());
		projectiles.add(p().id(147).name("Corp sperm 3").sprite(SpriteID.SPELL_WIND_STRIKE).ids(1162, 90, 316, 92, 51, 64, 124, 16).artificial().build());
		projectiles.add(p().id(154).name("Dragon breath (large)").sprite(SpriteID.SPELL_FIRE_SURGE).ids(7855, 1464, 54, 1466, 51, 64, 124, 16).build());
		projectiles.add(p().id(155).name("Dark Strike").sprite(SpriteID.SPELL_WIND_STRIKE_DISABLED).ids(1162, 194, 195, 196, 51, 64, 124, 16).build());
		projectiles.add(p().id(156).name("Tempoross harpoonfish").itemId(ItemID.HARPOONFISH).ids(426, 18, 1837, 3, 41, 11, 144, 15).hitGfx(3, 0).build());

		data.projectiles = projectiles;

		String s = plugin.getGson().toJson(data);
		System.out.println("your uhohlist is " + uhohList);
		System.out.println("json is \n" + s);

		Constants.loadData(plugin.getGson().fromJson(s, Constants.Data.class));
	}

//	@Subscribe
	public void not_onMenuOptionClicked(MenuOptionClicked e) {
		// Cannot use hit gfx because of splashing, plus I don't know what happens if someone else casts on the same target at the same time.

		if (e.getMenuOption().equals("Attack")) {
			manualSpellCastNoCastGfx = null;
		}
		if (e.getMenuOption().equals("Cast")) {
			String spellName = Text.removeTags(e.getMenuTarget());
			for (ProjectileCast projectileCast : Constants.projectiles) { // TODO smaller lookup table maybe? That's a lot of list items to go through, many of which don't matter because they have cast gfx!
				if (projectileCast.getName(itemManager).equals(spellName)) {
					manualSpellCastNoCastGfx = projectileCast;
					break;
				}
			}
		}
	}

}
