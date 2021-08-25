package com.weaponanimationreplacer;

import com.google.gson.reflect.TypeToken;
import com.weaponanimationreplacer.Constants.ActorAnimation;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.Player;
import net.runelite.api.PlayerComposition;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameTick;
import net.runelite.api.kit.KitType;
import net.runelite.client.RuneLite;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
@RequiredArgsConstructor
public class Devtools
{
	private boolean logAnimation = false;
	private String logName = null;
	int lastLogAnimation = -1;
	private List<Integer> lastPlayerPoseAnimations = Collections.emptyList();

	public List<Integer> kitForce = new ArrayList<>(12);
	{
		for (int i = 0; i < 12; i++)
		{
			kitForce.add(-1);
		}
	}

	private Set<Integer> showSleeves = new HashSet<>(Constants.showSleeves);
	private Set<Integer> hideSleeves = new HashSet<>();

	private Set<Integer> showJaw = new HashSet<>();
	private Set<Integer> hideJaw = new HashSet<>();

	private Set<Integer> showHair = new HashSet<>();
	private Set<Integer> hideHair = new HashSet<>();

	private Map<Integer, Map<ActorAnimation, Integer>> weaponIdToAnimationSet = new HashMap<>();

	private File data = new File(RuneLite.RUNELITE_DIR, "weaponanimationreplacer_datafile" + System.currentTimeMillis());
	private File issues = new File(RuneLite.RUNELITE_DIR, "weaponanimationreplacer_issuefile" + System.currentTimeMillis());
	private FileWriter dataWriter;
	private FileWriter issuesWriter;

	{
		try
		{
			dataWriter = new FileWriter(data);
			issuesWriter = new FileWriter(issues);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Subscribe
	public void onGameTick(GameTick gameTick) {
		for (Player player : plugin.client.getPlayers())
		{
			// make sure you don't take transmog from the player since that isn't accurate.
			if (player.equals(plugin.client.getLocalPlayer()) && !plugin.isRecordingOwnGearSlotOverrides()) {
				continue;
			}

			PlayerComposition playerComposition = player.getPlayerComposition();
			int[] equipmentIds = playerComposition.getEquipmentIds();

//			String s = "";
//			for (int equipmentId : equipmentIds)
//			{
//				s += plugin.itemName(equipmentId - 512) + " ";
//			}
//			System.out.println(player.getName() + " " + "["+s+"]");

			int torsoId = equipmentIds[EquipmentInventorySlot.BODY.getSlotIdx()] - 512;
			if (torsoId >= 0) // Item is equipped.
			{
				int sleevesId = equipmentIds[KitType.ARMS.getIndex()];
				boolean hiddenSleeves = sleevesId == 0;
				boolean recordedShowSleeves = showSleeves.contains(torsoId);
				boolean recordedHideSleeves = hideSleeves.contains(torsoId);
				if (recordedShowSleeves && recordedHideSleeves)
				{
					writeIssue("Shouldn't be possible: " + torsoId + " (" + plugin.itemName(torsoId) + ") is recorded as both hidden and shown.");
				}
				if (recordedHideSleeves && !hiddenSleeves)
				{
					writeIssue("Incorrect sleeves? Seen with sleeves but recorded as hidden sleeves: " + torsoId + " (" + plugin.itemName(torsoId) + ")");
				}
				if (recordedShowSleeves && hiddenSleeves)
				{
					writeIssue("Incorrect sleeves? Seen without sleeves but recorded as shown sleeves: " + torsoId + " (" + plugin.itemName(torsoId) + ")");
				}
				if (!recordedHideSleeves && !recordedShowSleeves)
				{
					System.out.println("New item: " + torsoId + " (" + plugin.itemName(torsoId) + "), " + (hiddenSleeves ? "hidden sleeves " : "shown sleeves"));
					(hiddenSleeves ? hideSleeves : showSleeves).add(torsoId);
					writeData(torsoId + " " + (hiddenSleeves ? "hidden_sleeves " : "shown_sleeves") + " " + plugin.itemName(torsoId) + "\n");
				}
			}

			int headId = equipmentIds[EquipmentInventorySlot.HEAD.getSlotIdx()] - 512;
			if (headId >= 0) // Item is equipped.
			{
				int hairId = equipmentIds[KitType.HAIR.getIndex()];
				int jawId = equipmentIds[KitType.JAW.getIndex()];
				boolean hiddenHair = hairId == 0;
				boolean hiddenJaw = jawId == 0;
				boolean recordedShowHair = showHair.contains(headId);
				boolean recordedHideHair = hideHair.contains(headId);
				boolean recordedShowJaw = showJaw.contains(headId);
				boolean recordedHideJaw = hideJaw.contains(headId);

				if (recordedShowHair && recordedHideHair)
				{
					writeIssue("Shouldn't be possible: " + headId + " (" + plugin.itemName(headId) + ") is recorded as both hidden and shown.");
				}
				if (recordedHideHair && !hiddenHair)
				{
					writeIssue("Incorrect hair? Seen with hair but recorded as hidden hair: " + headId + " (" + plugin.itemName(headId) + ")");
				}
				if (recordedShowHair && hiddenHair)
				{
					writeIssue("Incorrect hair? Seen without hair but recorded as shown hair: " + headId + " (" + plugin.itemName(headId) + ")");
				}

				if (recordedShowJaw && recordedHideJaw)
				{
					writeIssue("Shouldn't be possible: " + headId + " (" + plugin.itemName(headId) + ") is recorded as both hidden and shown.");
				}
				if (recordedHideJaw && !hiddenJaw)
				{
					writeIssue("Incorrect jaw? Seen with jaw but recorded as hidden jaw: " + headId + " (" + plugin.itemName(headId) + ")");
				}
				if (recordedShowJaw && hiddenJaw)
				{
					writeIssue("Incorrect jaw? Seen without jaw but recorded as shown jaw: " + headId + " (" + plugin.itemName(headId) + ")");
				}

				if (!recordedHideHair && !recordedShowHair && !recordedHideJaw && !recordedShowJaw)
				{
					System.out.println("New item: " + headId + " (" + plugin.itemName(headId) + "), " + (hiddenHair ? "hidden hair " : "shown hair") + " " + (hiddenJaw ? "hidden jaw " : "shown jaw"));

					(hiddenHair ? hideHair : showHair).add(headId);
					writeData(headId + " " + (hiddenHair ? "hidden_hair " : "shown_hair") + " " + plugin.itemName(headId) + "\n");

					(hiddenJaw ? hideJaw : showJaw).add(headId);
					writeData(headId + " " + (hiddenJaw ? "hidden_jaw " : "shown_jaw") + " " + plugin.itemName(headId) + "\n");
				}
			}

			int weaponId = equipmentIds[KitType.WEAPON.getIndex()] - 512;
			if (weaponId >= 0) {
				Map<ActorAnimation, Integer> poseAnimations = new HashMap<>();
				for (ActorAnimation animation : Constants.ActorAnimation.values())
				{
					poseAnimations.put(animation, animation.getAnimation(player));
				}
				Map<ActorAnimation, Integer> recordedPoseAnimations = weaponIdToAnimationSet.get(weaponId);
				if (recordedPoseAnimations == null) {
					System.out.println("new weapon: " + weaponId + " (" + plugin.itemName(weaponId) + ")" + " " + poseAnimations);
					weaponIdToAnimationSet.put(weaponId, poseAnimations);
					writeData(weaponId + " " + poseAnimations + " " + plugin.itemName(weaponId) + "\n");
				} else if (!poseAnimations.equals(recordedPoseAnimations)) {
					writeIssue("mismatching pose animations: " + weaponId + " (" + plugin.itemName(weaponId) + ")");
				}
			}
		}
	}

	private void writeData(String s)
	{
		try
		{
			dataWriter.append(s);
			dataWriter.flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private Set<String> writtenIssueMessages = new HashSet<>();

	private void writeIssue(String s)
	{
		if (writtenIssueMessages.contains(s)) return;
		writtenIssueMessages.add(s);
		System.out.println(s);
		try
		{
			issuesWriter.append(s);
			issuesWriter.flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private final WeaponAnimationReplacerPlugin plugin;

	@Subscribe
	public void onCommandExecuted(CommandExecuted commandExecuted)
	{
//		configManager.setConfiguration("WeaponAnimationReplacer", "rules", "[{\"name\":\"Monkey run\",\"modelSwapEnabled\":false,\"enabled\":false,\"minimized\":false,\"modelSwap\":-1,\"itemRestrictions\":[],\"animationReplacements\":[{\"enabled\":true,\"animationSet\":\"Cursed banana\",\"animationtypeToReplace\":\"ALL\"}]},{\"name\":\"Drunk stagger\",\"modelSwapEnabled\":false,\"enabled\":false,\"minimized\":false,\"modelSwap\":-1,\"itemRestrictions\":[],\"animationReplacements\":[{\"enabled\":true,\"animationSet\":\"Crystal grail\",\"animationtypeToReplace\":\"ALL\"}]},{\"name\":\"Elder scythe\",\"modelSwapEnabled\":false,\"enabled\":true,\"minimized\":false,\"modelSwap\":22325,\"itemRestrictions\":[{\"enabled\":true,\"itemId\":22324},{\"enabled\":true,\"itemId\":4151},{\"enabled\":true,\"itemId\":12006},{\"enabled\":true,\"itemId\":4587},{\"enabled\":true,\"itemId\":24551}],\"animationReplacements\":[{\"enabled\":true,\"animationSet\":\"Elder maul\",\"animationtypeToReplace\":\"ALL\"},{\"enabled\":true,\"animationSet\":\"Scythe of Vitur\",\"animationtypeToReplace\":\"ATTACK\",\"animationtypeReplacement\":{\"type\":\"ATTACK_SLASH\",\"id\":8056}}]},{\"name\":\"Scythe\",\"modelSwapEnabled\":false,\"enabled\":false,\"minimized\":false,\"modelSwap\":22325,\"itemRestrictions\":[{\"enabled\":true,\"itemId\":12006},{\"enabled\":true,\"itemId\":4587},{\"enabled\":true,\"itemId\":4151},{\"enabled\":true,\"itemId\":22324},{\"enabled\":true,\"itemId\":24551}],\"animationReplacements\":[{\"enabled\":true,\"animationSet\":\"Scythe of Vitur\",\"animationtypeToReplace\":\"ALL\"}]},{\"name\":\"Shoulder halberd\",\"modelSwapEnabled\":false,\"enabled\":true,\"minimized\":false,\"modelSwap\":-1,\"itemRestrictions\":[{\"enabled\":true,\"itemId\":3204},{\"enabled\":true,\"itemId\":23987}],\"animationReplacements\":[{\"enabled\":true,\"animationSet\":\"Dharok's greataxe\",\"animationtypeToReplace\":\"STAND_PLUS_MOVEMENT\"}]},{\"name\":\"Saeldor Slash\",\"modelSwapEnabled\":false,\"enabled\":true,\"minimized\":false,\"modelSwap\":-1,\"itemRestrictions\":[{\"enabled\":true,\"itemId\":24551},{\"enabled\":true,\"itemId\":23995},{\"enabled\":true,\"itemId\":23997}],\"animationReplacements\":[{\"enabled\":true,\"animationSet\":\"Inquisitor's mace\",\"animationtypeToReplace\":\"ATTACK_SLASH\",\"animationtypeReplacement\":{\"type\":\"ATTACK_CRUSH\",\"id\":4503}}]},{\"name\":\"Magic secateurs\",\"modelSwapEnabled\":false,\"enabled\":true,\"minimized\":false,\"modelSwap\":22370,\"itemRestrictions\":[{\"enabled\":true,\"itemId\":7409}],\"animationReplacements\":[{\"enabled\":true,\"animationSet\":\"Staff\",\"animationtypeToReplace\":\"ALL\"}]},{\"name\":\"Master staff\",\"modelSwapEnabled\":false,\"enabled\":true,\"minimized\":false,\"modelSwap\":24424,\"itemRestrictions\":[{\"enabled\":true,\"itemId\":6914}],\"animationReplacements\":[{\"enabled\":true,\"animationSet\":\"Nightmare Staff\",\"animationtypeToReplace\":\"ALL\"}]},{\"name\":\"Trident Sanguinesti\",\"modelSwapEnabled\":false,\"enabled\":true,\"minimized\":false,\"modelSwap\":22323,\"itemRestrictions\":[{\"enabled\":true,\"itemId\":12899}],\"animationReplacements\":[]}]");
//		configManager.setConfiguration("WeaponAnimationReplacer", "rules", "[{\"name\":\"New Replacement\",\"modelSwapEnabled\":false,\"enabled\":true,\"minimized\":false,\"modelSwap\":-1,\"itemRestrictions\":[],\"animationReplacements\":[]},{\"name\":\"New Replacement\",\"modelSwapEnabled\":false,\"enabled\":true,\"minimized\":false,\"modelSwap\":-1,\"itemRestrictions\":[{\"enabled\":true,\"itemId\":-1}],\"animationReplacements\":[]},{\"name\":\"New Replacement\",\"modelSwapEnabled\":false,\"enabled\":true,\"minimized\":false,\"modelSwap\":-1,\"itemRestrictions\":[],\"animationReplacements\":[{\"enabled\":true,\"animationSet\":\"Dragon dagger\",\"animationtypeToReplace\":\"ATTACK\",\"animationtypeReplacement\":{\"type\":\"ATTACK_SPEC\",\"id\":1062}},{\"enabled\":true,\"animationSet\":\"2h sword\",\"animationtypeToReplace\":\"STAND_PLUS_MOVEMENT\"}]},{\"name\":\"New Replacement\",\"modelSwapEnabled\":false,\"enabled\":true,\"minimized\":false,\"modelSwap\":-1,\"itemRestrictions\":[],\"animationReplacements\":[{\"enabled\":true,\"animationSet\":\"Abyssal whip\",\"animationtypeToReplace\":\"STAND_PLUS_MOVEMENT\"}]},{\"name\":\"New Replacement\",\"modelSwapEnabled\":false,\"enabled\":true,\"minimized\":false,\"modelSwap\":-1,\"itemRestrictions\":[],\"animationReplacements\":[{\"enabled\":true,\"animationtypeReplacement\":{\"id\":-1}}]},{\"name\":\"New Replacement\",\"modelSwapEnabled\":false,\"enabled\":true,\"minimized\":false,\"modelSwap\":-1,\"itemRestrictions\":[{\"enabled\":true,\"itemId\":12389},{\"enabled\":true,\"itemId\":23332}],\"animationReplacements\":[]},{\"name\":\"New Replacement\",\"modelSwapEnabled\":false,\"enabled\":true,\"minimized\":false,\"modelSwap\":23334,\"itemRestrictions\":[{\"enabled\":true,\"itemId\":-1}],\"animationReplacements\":[]},{\"name\":\"New Replacement\",\"modelSwapEnabled\":false,\"enabled\":true,\"minimized\":false,\"modelSwap\":-1,\"itemRestrictions\":[{\"enabled\":true,\"itemId\":1333}],\"animationReplacements\":[]}]");
//		configManager.unsetConfiguration(GROUP_NAME, TRANSMOG_SET_KEY);
		if ("testmigrate".equals(commandExecuted.getCommand())) {
			String configuration = plugin.configManager.getConfiguration("WeaponAnimationReplacer", "rules");
			if (configuration == null) return; // do nothing.

			configuration = "[{\"name\":\"Monkey run\",\"modelSwapEnabled\":false,\"enabled\":false,\"minimized\":false,\"modelSwap\":-1,\"itemRestrictions\":[],\"animationReplacements\":[{\"enabled\":true,\"animationSet\":\"Cursed banana\",\"animationtypeToReplace\":\"ALL\"}]},{\"name\":\"Drunk stagger\",\"modelSwapEnabled\":false,\"enabled\":false,\"minimized\":false,\"modelSwap\":-1,\"itemRestrictions\":[],\"animationReplacements\":[{\"enabled\":true,\"animationSet\":\"Crystal grail\",\"animationtypeToReplace\":\"ALL\"}]},{\"name\":\"Elder scythe\",\"modelSwapEnabled\":false,\"enabled\":true,\"minimized\":false,\"modelSwap\":22325,\"itemRestrictions\":[{\"enabled\":true,\"itemId\":22324},{\"enabled\":true,\"itemId\":4151},{\"enabled\":true,\"itemId\":12006},{\"enabled\":true,\"itemId\":4587},{\"enabled\":true,\"itemId\":24551}],\"animationReplacements\":[{\"enabled\":true,\"animationSet\":\"Elder maul\",\"animationtypeToReplace\":\"ALL\"},{\"enabled\":true,\"animationSet\":\"Scythe of Vitur\",\"animationtypeToReplace\":\"ATTACK\",\"animationtypeReplacement\":{\"type\":\"ATTACK_SLASH\",\"id\":8056}}]},{\"name\":\"Scythe\",\"modelSwapEnabled\":false,\"enabled\":false,\"minimized\":false,\"modelSwap\":22325,\"itemRestrictions\":[{\"enabled\":true,\"itemId\":12006},{\"enabled\":true,\"itemId\":4587},{\"enabled\":true,\"itemId\":4151},{\"enabled\":true,\"itemId\":22324},{\"enabled\":true,\"itemId\":24551}],\"animationReplacements\":[{\"enabled\":true,\"animationSet\":\"Scythe of Vitur\",\"animationtypeToReplace\":\"ALL\"}]},{\"name\":\"Shoulder halberd\",\"modelSwapEnabled\":false,\"enabled\":true,\"minimized\":false,\"modelSwap\":-1,\"itemRestrictions\":[{\"enabled\":true,\"itemId\":3204},{\"enabled\":true,\"itemId\":23987}],\"animationReplacements\":[{\"enabled\":true,\"animationSet\":\"Dharok\\\\u0027s greataxe\",\"animationtypeToReplace\":\"STAND_PLUS_MOVEMENT\"}]},{\"name\":\"Saeldor Slash\",\"modelSwapEnabled\":false,\"enabled\":true,\"minimized\":false,\"modelSwap\":-1,\"itemRestrictions\":[{\"enabled\":true,\"itemId\":24551},{\"enabled\":true,\"itemId\":23995},{\"enabled\":true,\"itemId\":23997}],\"animationReplacements\":[{\"enabled\":true,\"animationSet\":\"Inquisitor\\\\u0027s mace\",\"animationtypeToReplace\":\"ATTACK_SLASH\",\"animationtypeReplacement\":{\"type\":\"ATTACK_CRUSH\",\"id\":4503}}]},{\"name\":\"Magic secateurs\",\"modelSwapEnabled\":false,\"enabled\":true,\"minimized\":false,\"modelSwap\":22370,\"itemRestrictions\":[{\"enabled\":true,\"itemId\":7409}],\"animationReplacements\":[{\"enabled\":true,\"animationSet\":\"Staff\",\"animationtypeToReplace\":\"ALL\"}]},{\"name\":\"Master staff\",\"modelSwapEnabled\":false,\"enabled\":true,\"minimized\":false,\"modelSwap\":24424,\"itemRestrictions\":[{\"enabled\":true,\"itemId\":6914}],\"animationReplacements\":[{\"enabled\":true,\"animationSet\":\"Nightmare Staff\",\"animationtypeToReplace\":\"ALL\"}]},{\"name\":\"Trident Sanguinesti\",\"modelSwapEnabled\":false,\"enabled\":true,\"minimized\":false,\"modelSwap\":22323,\"itemRestrictions\":[{\"enabled\":true,\"itemId\":12899}],\"animationReplacements\":[]}]";

			List<AnimationReplacementRule_OLD> rules = plugin.customGson.fromJson(configuration, new TypeToken<ArrayList<AnimationReplacementRule_OLD>>() {}.getType());
			List<TransmogSet> transmogSets = new ArrayList<>();
			System.out.println("config string: \"" + configuration + "\"");
			System.out.println("previous format: " + rules);
			for (AnimationReplacementRule_OLD rule : rules)
			{
				TransmogSet transmogSet = new TransmogSet(
					Collections.singletonList(
						new Swap(
							rule.itemRestrictions.stream().map(r -> r.itemId).collect(Collectors.toList()),
							Collections.singletonList(rule.modelSwap),
							rule.animationReplacements,
							Collections.emptyList())));
				transmogSet.setName(rule.name);
				transmogSets.add(transmogSet);
			}
			System.out.println("migration result: " + transmogSets);
		}
		if ("recordmode".equals(commandExecuted.getCommand())) {
			plugin.toggleRecordingOwnGearSlotOverrides();
			System.out.println("recording mode is " + (plugin.isRecordingOwnGearSlotOverrides() ? "on" : "off"));
		}
		if ("printscraping".equals(commandExecuted.getCommand())) {
			System.out.println("showSleeves: " + showSleeves);
			System.out.println("hideSleeves: " + hideSleeves);

			System.out.println("showHair: " + showHair);
			System.out.println("hideHair: " + hideHair);

			System.out.println("showJaw: " + showJaw);
			System.out.println("hideJaw: " + hideJaw);

			System.out.println("animations: " + weaponIdToAnimationSet);
		}
		if ("printposeanims".equals(commandExecuted.getCommand())) {
			for (ActorAnimation animation : Constants.ActorAnimation.values())
			{
				int id = animation.getAnimation(plugin.client.getLocalPlayer());
				System.out.println("\t" + animation + " " + id);
			}
		}
		if ("mything".equals(commandExecuted.getCommand()))
		{
			System.out.println("red icon name: " + plugin.itemManager.getItemComposition(25228).getName());
			for (int i = 0; i < plugin.client.getItemCount(); i++)
			{
				ItemComposition itemComposition = plugin.itemManager.getItemComposition(plugin.itemManager.canonicalize(i));
				if (
//					itemComposition.getName().equals("Attacker icon") ||
//					itemComposition.getName().equals("Defender icon") ||
//					itemComposition.getName().equals("Collector icon") ||
//					itemComposition.getName().equals("Healer icon") ||
					itemComposition.getName().equals("Red icon") ||
						itemComposition.getName().equals("Blue icon")
				) {

					System.out.println(i);
				}
			}
		}

		if ("forcekit".equals(commandExecuted.getCommand())) {
			int slot = Integer.valueOf(commandExecuted.getArguments()[0]);
			int id = Integer.valueOf(commandExecuted.getArguments()[1]);
			kitForce.set(slot, id);
			System.out.println("set " + slot + " to " + id + ". all kits: " + kitForce + ".");
			plugin.updateAnimationsAndTransmog();
		}
		if ("wal".equals(commandExecuted.getCommand())) {
			if (commandExecuted.getArguments().length >= 1) {
				logAnimation = true;
				logName = String.join(" ", commandExecuted.getArguments());
				log.debug("following " + logName);
			} else {
				logName = null;
				logAnimation = !logAnimation;
				log.debug("logging is now " + (logAnimation ? "on" : "off"));
			}
			lastPlayerPoseAnimations = null;
			lastLogAnimation = -2;
		}
		if ("dumpanim".equals(commandExecuted.getCommand())) {
			log.debug(
				plugin.client.getLocalPlayer().getRunAnimation() + " " +
					plugin.client.getLocalPlayer().getWalkAnimation() + " " +
					plugin.client.getLocalPlayer().getIdlePoseAnimation() + " " +
					plugin.client.getLocalPlayer().getWalkRotate180() + " " +
					plugin.client.getLocalPlayer().getIdleRotateLeft() + " " +
					plugin.client.getLocalPlayer().getWalkRotateLeft() + " " +
					plugin.client.getLocalPlayer().getWalkRotateRight()
			);
		}
	}

	private void logPoseAnimationChanges(Player localPlayer) {
		if (localPlayer == null) return;
		ArrayList<Integer> newPoses = new ArrayList<>();
		newPoses.add(localPlayer.getRunAnimation());
		newPoses.add(localPlayer.getWalkAnimation());
		newPoses.add(localPlayer.getIdlePoseAnimation());
		newPoses.add(localPlayer.getWalkRotate180());
		newPoses.add(localPlayer.getIdleRotateLeft());
		newPoses.add(localPlayer.getWalkRotateLeft());
		newPoses.add(localPlayer.getWalkRotateRight());

		if (!newPoses.equals(lastPlayerPoseAnimations)) {
			Integer equippedWeapon = getEquippedWeaponOnLoggedPlayer();
			if (logAnimation) {
				log.debug("zz_" + (equippedWeapon == null ? "no weapon" : plugin.itemManager.getItemComposition(equippedWeapon).getName()));
				log.debug("zz_" + "STAND, " + localPlayer.getIdlePoseAnimation() + ",");
				log.debug("zz_" + "RUN, " + localPlayer.getRunAnimation() + ",");
				log.debug("zz_" + "WALK, " + localPlayer.getWalkAnimation() + ",");
				log.debug("zz_" + "WALK_BACKWARD, " + localPlayer.getWalkRotate180() + ",");
				log.debug("zz_" + "SHUFFLE_LEFT, " + localPlayer.getWalkRotateLeft() + ",");
				log.debug("zz_" + "SHUFFLE_RIGHT, " + localPlayer.getWalkRotateRight() + ",");
				log.debug("zz_" + "ROTATE, " + localPlayer.getIdleRotateLeft() + ",");
				if (localPlayer.getIdleRotateLeft() != localPlayer.getIdleRotateRight()) {
					log.debug("ALKDJSFLAKJSDL:AKSJD:ALKSJD:LASKJD:LAKSJD");
					log.debug("ALKDJSFLAKJSDL:AKSJD:ALKSJD:LASKJD:LAKSJD");
					log.debug("ALKDJSFLAKJSDL:AKSJD:ALKSJD:LASKJD:LAKSJD");
					log.debug("ALKDJSFLAKJSDL:AKSJD:ALKSJD:LASKJD:LAKSJD");
					log.debug("ALKDJSFLAKJSDL:AKSJD:ALKSJD:LASKJD:LAKSJD");
					log.debug("ALKDJSFLAKJSDL:AKSJD:ALKSJD:LASKJD:LAKSJD");
					log.debug("ALKDJSFLAKJSDL:AKSJD:ALKSJD:LASKJD:LAKSJD");
					log.debug("ALKDJSFLAKJSDL:AKSJD:ALKSJD:LASKJD:LAKSJD");
					log.debug("ALKDJSFLAKJSDL:AKSJD:ALKSJD:LASKJD:LAKSJD");
					log.debug("ALKDJSFLAKJSDL:AKSJD:ALKSJD:LASKJD:LAKSJD");
					log.debug("ALKDJSFLAKJSDL:AKSJD:ALKSJD:LASKJD:LAKSJD");
				}
			}
//            log.debug(newPoses);
			lastPlayerPoseAnimations = newPoses;
		}
	}

	private void logLoggedPlayerInformation()
	{
		Player logPlayer = logName == null ? plugin.client.getLocalPlayer() : plugin.client.getPlayers().stream().filter(p -> logName.equalsIgnoreCase(p.getName())).findFirst().orElse(null);
		if (logPlayer != null) {
			logPoseAnimationChanges(logPlayer);
			int logPlayerAnimation = logPlayer.getAnimation();
			if (lastLogAnimation != logPlayerAnimation) {
				if (logAnimation && logPlayerAnimation != -1) {
					Integer equippedWeapon = getEquippedWeaponOnLoggedPlayer();
					log.debug("zz_" + (equippedWeapon == null ? "null" : plugin.itemManager.getItemComposition(equippedWeapon).getName()));
					log.debug("zz_" + "ATTACK_ZZZ, " + logPlayerAnimation + ",");
				}
			}
			lastLogAnimation = logPlayerAnimation;
		} else {
			lastPlayerPoseAnimations = null;
			lastLogAnimation = -2;
		}
	}

	private Integer getEquippedWeaponOnLoggedPlayer() {
		Integer weaponItemId;
		if (logName != null) {
			Player logPlayer = plugin.client.getPlayers().stream().filter(p -> logName.equalsIgnoreCase(p.getName())).findFirst().orElse(null);
			weaponItemId = logPlayer != null ? logPlayer.getPlayerComposition().getEquipmentIds()[3] - 512 : -1;
		} else {
			ItemContainer equipmentContainer = plugin.client.getItemContainer(InventoryID.EQUIPMENT);
			if (equipmentContainer == null) return null;
			Item[] equippedItems = equipmentContainer.getItems();
			if (equippedItems.length < EquipmentInventorySlot.WEAPON.getSlotIdx()) return null;
			weaponItemId = equippedItems[EquipmentInventorySlot.WEAPON.getSlotIdx()].getId();
		}
		return weaponItemId;
	}

	@Subscribe
	public void onClientTick(ClientTick event)
	{
		logLoggedPlayerInformation();
	}
}
