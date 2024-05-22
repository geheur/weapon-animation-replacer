package com.weaponanimationreplacer;

import com.google.gson.Gson;
import static com.weaponanimationreplacer.Swap.AnimationType.ROTATE;
import static com.weaponanimationreplacer.Swap.AnimationType.SHUFFLE_LEFT;
import static com.weaponanimationreplacer.Swap.AnimationType.SHUFFLE_RIGHT;
import static com.weaponanimationreplacer.Swap.AnimationType.STAND;
import static com.weaponanimationreplacer.Swap.AnimationType.WALK_BACKWARD;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import net.runelite.api.Actor;
import net.runelite.api.ItemID;
import net.runelite.api.kit.KitType;
import static net.runelite.api.kit.KitType.SHIELD;
import net.runelite.client.game.ItemVariationMapping;

public class Constants
{
	public static final int HEAD_SLOT = KitType.HEAD.getIndex();
	public static final int CAPE_SLOT = KitType.CAPE.getIndex();
	public static final int AMULET_SLOT = KitType.AMULET.getIndex();
	public static final int WEAPON_SLOT = KitType.WEAPON.getIndex();
	public static final int TORSO_SLOT = KitType.TORSO.getIndex();
	public static final int SHIELD_SLOT = KitType.SHIELD.getIndex();
	public static final int ARMS_SLOT = KitType.ARMS.getIndex();
	public static final int LEGS_SLOT = KitType.LEGS.getIndex();
	public static final int HAIR_SLOT = KitType.HAIR.getIndex();
	public static final int HANDS_SLOT = KitType.HANDS.getIndex();
	public static final int BOOTS_SLOT = KitType.BOOTS.getIndex();
	public static final int JAW_SLOT = KitType.JAW.getIndex();

	public static Set<Integer> SHOWS_ARMS;
	public static Set<Integer> HIDES_HAIR;
	public static Set<Integer> HIDES_JAW;
	public static Map<String, int[]> poseanims;
	public static List<ProjectileCast> projectiles = new ArrayList<>();
	public static ProjectileCast[] projectilesById = new ProjectileCast[0];

	// key is item id, value is the slot id it should go in. A slot id of -1 means the item should be considered unequippable.
	public static Map<Integer, Integer> SLOT_OVERRIDES = new HashMap<>();
	public static Map<Integer, NameAndIconId> NAME_ICON_OVERRIDES = new HashMap<>();

	public static final List<AnimationSet> animationSets = new ArrayList<>();
	public static final List<Integer> doNotReplaceIdles = new ArrayList<>();
	public static final Map<Integer, String> descriptions = new HashMap<>();

	public static AnimationSet getAnimationSet(int itemId)
	{
		Collection<Integer> variations = ItemVariationMapping.getVariations(ItemVariationMapping.map(itemId));
		for (Map.Entry<String, int[]> entry : poseanims.entrySet())
		{
			for (int itemId2 : entry.getValue())
			{
				if (variations.contains(itemId2)) {
					return AnimationSet.getAnimationSet(entry.getKey());
				}
			}
		}
		return null;
	}

	public static Data getBundledData(Gson gson) {
		InputStream resourceAsStream = Constants.class.getResourceAsStream("data.json");
		Data data = gson.fromJson(new InputStreamReader(resourceAsStream), Data.class);
		return data;
	}

	public static void loadData(Gson gson)
	{
		if (SLOT_OVERRIDES.size() > 0) return; // Already loaded.

		InputStream resourceAsStream = Constants.class.getResourceAsStream("data.json");
		Data data = gson.fromJson(new InputStreamReader(resourceAsStream), Data.class);

		loadData(data);
	}

	public static void loadData(Data data)
	{
		// Load slot overrides.
		for (Map.Entry<Integer, List<Integer>> entry : data.slotOverrides.entrySet())
		{
			int kitIndex = entry.getKey();
			for (Integer itemId : entry.getValue())
			{
				SLOT_OVERRIDES.put(itemId, kitIndex);
			}
		}

		// skis (I want them in the shield slot not the weapon slot. They are marked as equippable though). This is in
		// here as a grandfathered in thing from before you could choose custom slots for items, and it remains here to
		// avoid messing up existing transmog sets.
		addUnequippable(ItemID.SKIS, SHIELD);

		SHOWS_ARMS = data.showArms;
		HIDES_HAIR = data.hideHair;
		HIDES_JAW = data.hideJaw;
		poseanims = data.poseanims;
		NAME_ICON_OVERRIDES = data.nameIconOverrides;
		projectiles = data.projectiles;
		projectilesById = createProjectilesById(projectiles);

		animationSets.clear();
		animationSets.addAll(data.animationSets);
		Collections.sort(animationSets);

		doNotReplaceIdles.clear();
		for (AnimationSet animationSet : animationSets)
		{
			if (animationSet.doNotReplace) {
				doNotReplaceIdles.add(animationSet.getAnimation(STAND));
			}
		}

		descriptions.clear();
		descriptions.putAll(data.descriptions);
	}

	private static ProjectileCast[] createProjectilesById(List<ProjectileCast> projectiles)
	{
		OptionalInt max = projectiles.stream().mapToInt(p -> p.id).max();
		if (!max.isPresent()) {
			return new ProjectileCast[0];
		}
		int highestId = max.getAsInt();
		ProjectileCast[] projectilesById = new ProjectileCast[highestId + 1];
		for (ProjectileCast projectile : projectiles)
		{
			projectilesById[projectile.id] = projectile;
		}
		return projectilesById;
	}

	static final class Data
	{
		int version;
		Set<Integer> showArms;
		Set<Integer> hideHair;
		Set<Integer> hideJaw;
		Map<Integer, List<Integer>> slotOverrides;
		Map<Integer, NameAndIconId> nameIconOverrides;
		Map<String, int[]> poseanims;
		List<ProjectileCast> projectiles;
		public Map<Integer, String> descriptions;
		public List<AnimationSet> animationSets;
	}

	private static void addUnequippable(int itemId, KitType kitType) {
		addUnequippable(itemId, kitType, null);
	}

	private static void addUnequippable(int itemId, KitType kitType, String name) {
		addUnequippable(itemId, kitType, name, -1);
	}

	private static void addUnequippable(int itemId, KitType kitType, String name, int iconId) {
		SLOT_OVERRIDES.put(itemId, kitType.getIndex());
		if (name != null || iconId != -1) {
			NAME_ICON_OVERRIDES.put(itemId, new NameAndIconId(name, iconId));
		}
	}

	public static int getIconId(int itemId)
	{
		NameAndIconId nameAndIconId = NAME_ICON_OVERRIDES.get(itemId);
		return nameAndIconId == null ? itemId : nameAndIconId.iconId(itemId);
	}

	public static String getName(int itemId, String name)
	{
		NameAndIconId nameAndIconId = NAME_ICON_OVERRIDES.get(itemId);
		return nameAndIconId == null ? name : nameAndIconId.name(name);
	}

	@Value
	public static class NameAndIconId {
		String name;
		int iconId;

		public String name(String actualName) {
			return name == null ? actualName : name;
		}

		public int iconId(int actualIconId) {
			return iconId == -1 ? actualIconId : iconId;
		}
	}

	public static Constants.NegativeId mapNegativeId(int id) {
		if (id == -1) return new Constants.NegativeId(Constants.NegativeIdsMap.NULL, -1);
		else if (id <= -2 && id > -2 - KitType.values().length) {
			return new Constants.NegativeId(Constants.NegativeIdsMap.HIDE_SLOT, id * -1 - 2);
		}
		else if (id == -14) {
			return new Constants.NegativeId(Constants.NegativeIdsMap.SHOW_SLOT, ARMS_SLOT);
		}
		else if (id == -15) {
			return new Constants.NegativeId(Constants.NegativeIdsMap.SHOW_SLOT, HAIR_SLOT);
		}
		else if (id == -16) {
			return new Constants.NegativeId(Constants.NegativeIdsMap.SHOW_SLOT, JAW_SLOT);
		}
		throw new IllegalArgumentException();
	}

	public static int mapNegativeId(Constants.NegativeId id) {
		return mapNegativeId(id.type, id.id);
	}

	public static int mapNegativeId(Constants.NegativeIdsMap type, int id) {
		if (type == Constants.NegativeIdsMap.NULL) return -1;
		else if (type == Constants.NegativeIdsMap.HIDE_SLOT) {
			return -2 - id;
		}
		else if (type == Constants.NegativeIdsMap.SHOW_SLOT) {
			if (id == ARMS_SLOT) {
				return -14;
			}
			else if (id == HAIR_SLOT) {
				return -15;
			}
			else if (id == JAW_SLOT) {
				return -16;
			}
		}
		throw new IllegalArgumentException();
	}

	public static IdIconNameAndSlot getModelSwap(int modelSwap) {
		if (modelSwap < 0) {
			NegativeId negativeId = mapNegativeId(modelSwap);
			if (negativeId.type == NegativeIdsMap.HIDE_SLOT)
			{
				HiddenSlot hiddenSlot = HiddenSlot.values()[negativeId.id];
				return new IdIconNameAndSlot(modelSwap, hiddenSlot.iconIdToShow, hiddenSlot.actionName, hiddenSlot.kitType, true);
			}
			else if (negativeId.type == NegativeIdsMap.SHOW_SLOT)
			{
				ShownSlot shownSlot = ShownSlot.values()[negativeId.id];
				return new IdIconNameAndSlot(modelSwap, shownSlot.iconIdToShow, "Show " + shownSlot.kitType.name().toLowerCase(), shownSlot.kitType, false);
			}
			else
			{
				return new IdIconNameAndSlot(-1, -1, null, null, false);
			}
		}
		return null;
	}

	@RequiredArgsConstructor
	public enum HiddenSlot {
		HEAD(KitType.HEAD, ItemID.IRON_MED_HELM, "Hide helm"),
		CAPE(KitType.CAPE, 3779, "Hide cape"),
		AMULET(KitType.AMULET, 1796, "Hide amulet"),
		WEAPON(KitType.WEAPON, ItemID.SHADOW_SWORD, "Hide weapon"),
		TORSO(KitType.TORSO, ItemID.GHOSTLY_ROBE, "Hide torso"),
		SHIELD(KitType.SHIELD, ItemID.IRON_SQ_SHIELD, "Hide off hand"),
		ARMS(KitType.ARMS, ItemID.EXPEDITIOUS_BRACELET, "Hide sleeves"),
		LEGS(KitType.LEGS, ItemID.GHOSTLY_ROBE_6108, "Hide legs"),
		HAIR(KitType.HAIR, 2421, "Hide hair"),
		HANDS(KitType.HANDS, 21736, "Hide hands"),
		BOOTS(KitType.BOOTS, ItemID.IRON_BOOTS, "Hide boots"),
		JAW(KitType.JAW, 4593, "Hide jaw"),
		;

		final KitType kitType;
		final int iconIdToShow;
		public final String actionName;
	}

	@RequiredArgsConstructor
	public enum ShownSlot {
		HEAD(KitType.HEAD, ItemID.IRON_MED_HELM),
		CAPE(KitType.CAPE, 3779),
		AMULET(KitType.AMULET, 1796),
		WEAPON(KitType.WEAPON, ItemID.SHADOW_SWORD),
		TORSO(KitType.TORSO, ItemID.GHOSTLY_ROBE),
		SHIELD(KitType.SHIELD, ItemID.IRON_SQ_SHIELD),
		ARMS(KitType.ARMS, ItemID.GOLD_BRACELET),
		LEGS(KitType.LEGS, ItemID.GHOSTLY_ROBE_6108),
		HAIR(KitType.HAIR, 2419),
		HANDS(KitType.HANDS, 21736),
		BOOTS(KitType.BOOTS, ItemID.IRON_BOOTS),
		JAW(KitType.JAW, ItemID.BASILISK_JAW),
		;

		final KitType kitType;
		final int iconIdToShow;
	}

	public enum ActorAnimation
	{
		IDLE(Actor::getIdlePoseAnimation, Actor::setIdlePoseAnimation, STAND),
		IDLE_ROTATE_LEFT(Actor::getIdleRotateLeft, Actor::setIdleRotateLeft, ROTATE),
		IDLE_ROTATE_RIGHT(Actor::getIdleRotateRight, Actor::setIdleRotateRight, ROTATE),
		WALK(Actor::getWalkAnimation, Actor::setWalkAnimation, Swap.AnimationType.WALK),
		WALK_ROTATE_180(Actor::getWalkRotate180, Actor::setWalkRotate180, WALK_BACKWARD),
		WALK_ROTATE_LEFT(Actor::getWalkRotateLeft, Actor::setWalkRotateLeft, SHUFFLE_LEFT),
		WALK_ROTATE_RIGHT(Actor::getWalkRotateRight, Actor::setWalkRotateRight, SHUFFLE_RIGHT),
		RUN(Actor::getRunAnimation, Actor::setRunAnimation, Swap.AnimationType.RUN),
		;

		interface AnimationGetter
		{
			int getAnimation(Actor a);
		}

		interface AnimationSetter
		{
			void setAnimation(Actor a, int animationId);
		}

		@Getter
		private final Swap.AnimationType type;
		private final AnimationGetter animationGetter;
		private final AnimationSetter animationSetter;

		ActorAnimation(AnimationGetter animationGetter, AnimationSetter animationSetter, Swap.AnimationType type)
		{
			this.type = type;
			this.animationGetter = animationGetter;
			this.animationSetter = animationSetter;
		}

		public int getAnimation(Actor actor)
		{
			return animationGetter.getAnimation(actor);
		}

		public void setAnimation(Actor actor, int animationId)
		{
			animationSetter.setAnimation(actor, animationId);
		}
	}

	public enum NegativeIdsMap {
		NULL,
		HIDE_SLOT,
		SHOW_SLOT,
//		SLEEVES,
//		HAIR,
//		JAW,
		;
	}

	@RequiredArgsConstructor
		public static final class NegativeId {
		public final NegativeIdsMap type;
		public final int id;
	}

	@Value
	public static class IdIconNameAndSlot
	{
		int id;
		int iconId;
		String name;
		KitType kitType;
		boolean showNotSign;
	}

	public static final class TriggerItemIds {
		public static final List<IdIconNameAndSlot> EMPTY_SLOTS = new ArrayList<>();
		static {
			for (int i = 0; i < KitType.values().length; i++)
			{
				EMPTY_SLOTS.add(new IdIconNameAndSlot(-i - 1_000_000, HiddenSlot.values()[i].iconIdToShow, "empty " + KitType.values()[i].name().toLowerCase() + " slot", KitType.values()[i], true));
			}
		}

		public static IdIconNameAndSlot getHiddenSlot(int itemId) {
			if (itemId > -1_000_000) {
				return null;
			}
			return EMPTY_SLOTS.get(-itemId - 1_000_000);
		}
	}
}
