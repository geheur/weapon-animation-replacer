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
import java.awt.image.BufferedImage;
import java.io.FileReader;
import java.io.FileWriter;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import java.util.ArrayList;
import java.util.Arrays;
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
import net.runelite.api.Player;
import net.runelite.api.PlayerComposition;
import net.runelite.api.Projectile;
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
	public void onCommandExecuted(CommandExecuted commandExecuted) {
		String[] arguments = commandExecuted.getArguments();
		String command = commandExecuted.getCommand();

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

			swap = new Swap(Arrays.asList(-1), Arrays.asList(-1), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
			swap.updateForSortOrderAndUniqueness(plugin);
			if (swap.getModelSwaps().size() != 0 || swap.getItemRestrictions().size() != 0) {
				System.out.println("test 1 failed.");
			}

			swap = new Swap(Arrays.asList(-1), Arrays.asList(-14, -15, -16), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
			swap.updateForSortOrderAndUniqueness(plugin);

			swap = new Swap(
				Arrays.asList(ItemID.SLAYER_HELMET_I, ItemID.ABYSSAL_TENTACLE, ItemID.GHRAZI_RAPIER, ItemID.DRAGON_SCIMITAR, ItemID.CHEFS_HAT),
				Arrays.asList(ItemID.SLAYER_HELMET_I, ItemID.ABYSSAL_TENTACLE, ItemID.GHRAZI_RAPIER, ItemID.DRAGON_SCIMITAR, ItemID.CHEFS_HAT, ItemID.SKIS),
				Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
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
			ProjectileCast.initializeSpells();
			SwingUtilities.invokeLater(plugin.pluginPanel::rebuild);
			Constants.loadData(plugin.getGson());
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

		Map<Integer, Set<List<Integer>>> poseanims = new HashMap<>(this.poseanims);
//		addSlotData(showsArms, hidesHair, hidesJaw, poseanims);
		data.slotOverrides = kitIndexToItemIds;
		data.nameIconOverrides = EQUIPPABLE_ITEMS_NOT_MARKED_AS_EQUIPMENT_NAMES;

		String s = plugin.getGson().toJson(data);
		System.out.println("your uhohlist is " + uhohList);
		System.out.println("json is \n" + s);
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
//		System.out.println(menuOptionClicked.getMenuOption() + " " + Text.removeTags(menuOptionClicked.getMenuTarget()));
	}

}
