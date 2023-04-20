package com.weaponanimationreplacer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import lombok.Data;
import lombok.Getter;
import net.runelite.api.EquipmentInventorySlot;

/**
 * Represents a set of:
 *  Item ids which trigger this swap.
 *  List of items whose models ot use when one of the trigger items is worn.
 *  List of animation replacements, which each contain an animation type to replace, and animation set to use for replacement, and, optionally, an animation type to use as the replacement (if it is different from the one being replaced).
 */
public class Swap
{
	/*
	Slot data:
	this plugin has a lot of different concepts of what slot an item equips into:
	Real equip slot:
	- Actual item equip slot - the slot the item occupies when equipped. This information isn't available directly from the game unless you witness the item equipped on a player in-game, so the wiki scrape data has to be used most of the time.
	- Wiki scrape data - what's available from ItemManager#getEquipment. Sometimes does not include items, especially new items.
	Model swap equip slot - the slot that an item should be transmogged into, so that you can show multiple item models that normally equip into the same slot, at the same time:
	- My equip slot - This is the slot that I assign to items that are not considered equippable by the wiki scrape data, or cannot be equipped in-game, but have player models that I like. Manually generated. Can affect equippable items too, such as the skis, which I marked into the shield slot, even though they equip into the weapon slot, back in a plugin version before users could customize the equip slot of an item.
	- Custom equip slot - the slot the user wants an item to appear in.
	 */

    private final List<Integer> itemRestrictions;
    // Ideally this would be an array of size KitType.values().length where the index is the kit index. This would also
	// deal with any possible issues where an item changes from equippable to unequippable in the wiki data or in my
	// own slot data. But this change requires work and save format changes, so I'm not doing it for now.
	private final List<Integer> modelSwaps;
	public final List<AnimationReplacement> animationReplacements;
	@Getter
	private final List<ProjectileSwap> projectileSwaps;
	@Getter
	private final List<GraphicEffect> graphicEffects;
	/**
	 * Model swap overrides. For equipping items into the wrong slot, or for equipping items that have no known slot.
	 */
	private final Map<Integer, Integer> slotOverrides;
	/**
	 * This is used for items that do not have a wiki scraped slot. These items should still be usable by players who
	 * have them equipped at the time of the item being added, since we can grab the equip slot from the player's
	 * current equipment, but they will have to have their slot recorded.
	 */
	private final Map<Integer, Integer> triggerItemSlotOverrides;

	// This is necessary for the gson to not do its own dumb stuff where it ignores default values of fields that are
	// normally assigned in the constructor, and assigns them to null.
	public Swap() {
		this(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
	}

	public Swap(
		List<Integer> itemRestrictions,
		List<Integer> modelSwaps,
		List<AnimationReplacement> animationReplacements,
		List<ProjectileSwap> projectileSwaps,
		List<GraphicEffect> graphicEffects
	) {
		this(itemRestrictions, modelSwaps, animationReplacements, projectileSwaps, graphicEffects, new HashMap<>(), new HashMap<>());
	}

	public Swap(
		List<Integer> itemRestrictions,
		List<Integer> modelSwaps,
		List<AnimationReplacement> animationReplacements,
		List<ProjectileSwap> projectileSwaps,
		List<GraphicEffect> graphicEffects,
		Map<Integer, Integer> slotOverrides,
		Map<Integer, Integer> triggerItemSlotOverrides
	) {
		this.itemRestrictions = new ArrayList<>(itemRestrictions);
		this.modelSwaps = new ArrayList<>(modelSwaps);
		this.animationReplacements = new ArrayList<>(animationReplacements);
		this.projectileSwaps = new ArrayList<>(projectileSwaps);
		this.graphicEffects = new ArrayList<>(graphicEffects);
		this.slotOverrides = new HashMap<>(slotOverrides);
		this.triggerItemSlotOverrides = new HashMap<>(triggerItemSlotOverrides);
    }

    public List<Integer> getItemRestrictions() {
    	return Collections.unmodifiableList(itemRestrictions);
	}

	public List<Integer> getModelSwaps() {
		return Collections.unmodifiableList(modelSwaps);
	}

	/**
	 * Takes into account slotOverrides. Do not use for getting the slot of an item not used as a model swap, otherwise
	 * the result will not be useful.
	 */
	private int getModelSwapSlot(int itemId, WeaponAnimationReplacerPlugin plugin) {
		Integer integer = slotOverrides.get(itemId);
		if (integer != null) return integer;
		integer = plugin.getMySlot(itemId);
		if (integer != null) return integer;
		return -1;
	}

	private int getTriggerItemSlot(int itemId, WeaponAnimationReplacerPlugin plugin) {
		Integer integer = triggerItemSlotOverrides.get(itemId);
		if (integer != null) return integer;
		integer = plugin.getWikiScrapeSlot(itemId);
		if (integer != null) return integer;
		return -1;
	}

	public int getSlotOverride(int itemId) {
		return slotOverrides.getOrDefault(itemId, -1);
	}

	/** Use default slot for the item. */
	public void addModelSwap(int itemId, WeaponAnimationReplacerPlugin plugin)
	{
		addModelSwap(itemId, plugin, -1);
	}

	/** for assigning item to a custom slot. */
	public void addModelSwap(int itemId, WeaponAnimationReplacerPlugin plugin, int customSlot)
	{
		if (itemId == -1) return;

		final int targetSlot;
		if (customSlot == -1)
		{
			Integer s = plugin.getMySlot(itemId);
			if (s == null || s == EquipmentInventorySlot.RING.getSlotIdx() || s == EquipmentInventorySlot.AMMO.getSlotIdx())
				return;
			targetSlot = s;
		} else {
			targetSlot = customSlot;
		}

		// remove the item if it exists, and any item in the target slot.
		modelSwaps.removeIf(id -> {
			boolean remove;
			if (id == itemId) {
				remove = true;
			} else {
				int slot = getModelSwapSlot(id, plugin);
				remove =
					slot == -1 || // ??? something must have changed in the wiki data or my own slot overrides.
					slot == targetSlot ||
					slot == EquipmentInventorySlot.RING.getSlotIdx() || // do some housekeeping I guess?
					slot == EquipmentInventorySlot.AMMO.getSlotIdx() // do some housekeeping I guess?
				;
			}
			if (remove) slotOverrides.remove(id);
			return remove;
		});

		if (customSlot != -1) slotOverrides.put(itemId, targetSlot);
		int index = Collections.binarySearch(modelSwaps, itemId, itemComparator(i -> getModelSwapSlot(i, plugin)));
		modelSwaps.add(~index, itemId);
	}

	public void removeModelSwap(int prevItemId)
	{
		modelSwaps.remove((Integer) prevItemId); // Cast is necessary to use the right overload of the method.
		slotOverrides.remove(prevItemId);
	}

	public void replaceModelSwap(int prevItemId, int newItemId, WeaponAnimationReplacerPlugin plugin)
	{
		removeModelSwap(prevItemId);
		addModelSwap(newItemId, plugin);
	}

	public void addTriggerItem(int itemId, WeaponAnimationReplacerPlugin plugin)
	{
		addTriggerItem(itemId, -1, plugin);
	}

	/**
	 * Slot is necessary for items that do not have equip slots set from the runelite wiki scraper.
	 */
	public void addTriggerItem(int itemId, int slot, WeaponAnimationReplacerPlugin plugin)
	{
		removeTriggerItem(itemId);

		if (slot == -1)
		{
			Integer newItemSlot = plugin.getWikiScrapeSlot(itemId);
			if (newItemSlot == null || newItemSlot == EquipmentInventorySlot.RING.getSlotIdx() || newItemSlot == EquipmentInventorySlot.AMMO.getSlotIdx())
				return;
		} else {
			triggerItemSlotOverrides.put(itemId, slot);
		}

		int index = Collections.binarySearch(itemRestrictions, itemId, itemComparator(i -> getTriggerItemSlot(i, plugin)));
		itemRestrictions.add(~index, itemId);
	}

	public void removeTriggerItem(int itemId)
	{
		itemRestrictions.remove((Integer) itemId); // Cast is necessary to use the right overload of the method.
		triggerItemSlotOverrides.remove(itemId);
	}

	private static final int[] MY_SLOT_ORDER = new int[]{2, 5, 6, 0, 7, 1, 8, 9, 3, 10, 11, 4, 12, 13};

	private Comparator<Integer> itemComparator(Function<Integer, Integer> getSlot)
	{
		return (id1, id2) -> {
			int slot1 = getSlot.apply(id1);
			int slot2 = getSlot.apply(id2);

			if (slot1 == slot2) {
				return Integer.compare(id1, id2);
			}

			// It shouldn't be possible for these to be -1, but just in case.
			if (slot1 == -1) {
				return -1;
			} else if (slot2 == -1) {
				return 1;
			}

			return Integer.compare(MY_SLOT_ORDER[slot1], MY_SLOT_ORDER[slot2]);
		};
	}

	public void addNewAnimationReplacement()
	{
		animationReplacements.add(AnimationReplacement.createTemplate());
	}

	public void addNewGraphicEffect()
	{
		graphicEffects.add(GraphicEffect.createTemplate());
	}

	public boolean appliesToGear(List<Integer> equippedItemIds, WeaponAnimationReplacerPlugin plugin)
	{
		// -1 used to represent "Any", I think. idk if this can still happen.
		if (itemRestrictions.contains(-1) || itemRestrictions.isEmpty()) return true;
		return appliesSpecificallyToGear(equippedItemIds, plugin);
	}

	/**
	 * returns true if each slot of the equipped gear that has a corresponding trigger item matches at least one of the trigger items for that slot.
	 * In other words, all trigger items must match unless there are multiple for the same slot in which case only one much match.
	 */
	public boolean appliesSpecificallyToGear(List<Integer> equippedItemIds, WeaponAnimationReplacerPlugin plugin)
	{
		if (itemRestrictions.isEmpty()) return false;
		Set<Integer> slots = new HashSet<>();
		Set<Integer> slotsSatisfied = new HashSet<>();
		for (Integer itemRestriction : itemRestrictions)
		{
			// -1 used to represent "Any", I think. idk if this can still happen.
			if (itemRestriction == -1) {
				return false;
			}

			int slot = getTriggerItemSlot(itemRestriction, plugin);
			slots.add(slot);
			if (equippedItemIds.contains(itemRestriction)) {
				slotsSatisfied.add(slot);
			}
		}
		for (Integer slot : slots)
		{
			if (!slotsSatisfied.contains(slot)) {
				return false;
			}
		}
		return true;
	}

	public void updateForSortOrderAndUniqueness(WeaponAnimationReplacerPlugin plugin)
	{
		List<Integer> modelSwapsCopy = new ArrayList<>(modelSwaps);
		Collections.reverse(modelSwapsCopy); // The first item in the list for a particular slot was the one that was used previously, so add that one last so it ends up being the one in the list.
		modelSwaps.clear();
		for (Integer itemId : modelSwapsCopy)
		{
			addModelSwap(itemId, plugin);
		}

		List<Integer> itemRestrictionsCopy = new ArrayList<>(itemRestrictions);
		itemRestrictions.clear();
		for (Integer itemId : itemRestrictionsCopy)
		{
			addTriggerItem(itemId, plugin);
		}
	}

	public void addNewProjectileSwap()
	{
		projectileSwaps.add(ProjectileSwap.createTemplate());
	}

	@Data
    public static class AnimationReplacement implements Comparable<AnimationReplacement> {
        public AnimationSet animationSet;
        public AnimationType animationtypeToReplace;
        public AnimationSet.Animation animationtypeReplacement;

		public static AnimationReplacement createTemplate()
		{
			return new AnimationReplacement(null, null, null);
		}

		public AnimationReplacement(AnimationSet animationSet, AnimationType animationtypeToReplace) {
			this(animationSet, animationtypeToReplace, animationtypeToReplace);
		}

		public AnimationReplacement(AnimationSet animationSet, AnimationType animationtypeToReplace, AnimationType animationtypeReplacement) {
            this.animationSet = animationSet;
            this.animationtypeToReplace = animationtypeToReplace;
            if (animationSet != null && animationtypeToReplace != null && animationtypeReplacement != null)
			{
				Integer animation1 = animationSet.getAnimation(animationtypeReplacement);
				if (animation1 != null)
				{
					Integer animation = animationSet == null ? -1 : animation1;
					if (animation == null) animation = -1;
					this.animationtypeReplacement = new AnimationSet.Animation(
						animationtypeReplacement,
						animation,
						null
					);
				}
			}
        }

        @Override
        public int compareTo(AnimationReplacement o) {
			boolean isNull = animationtypeToReplace == null;
			boolean oIsNull = o.animationtypeToReplace == null;
			if (isNull && oIsNull) return 0;
			if (isNull) return -1;
			if (oIsNull) return 1;
            return animationtypeToReplace.compareTo(o.animationtypeToReplace);
        }

        @Override
        public String toString() {
            return (animationSet == null ? null : animationSet.name) + " " + animationtypeToReplace + " " + animationtypeReplacement;
        }
    }

    /**
     * Ordered by specificity.
     *
     * TODO this is kinda gross - combination categories at the same logical level as animations.
     */
    public enum AnimationType {
        // Order matters - it is used to determine specificity (more specific enums earlier in the list).
        STAND, WALK, RUN, WALK_BACKWARD, SHUFFLE_LEFT, SHUFFLE_RIGHT, ROTATE, ATTACK_STAB("Stab"),
        ATTACK_SLASH("Slash"), ATTACK_CRUSH("Crush"), ATTACK_SPEC("Special"), DEFEND,
        ATTACK_SLASH2("Slash2"), ATTACK_CRUSH2("Crush2"),

//        MAGIC_LOW_LEVEL_SPELL_UNARMED, MAGIC_LOW_LEVEL_SPELL_STAFF, // strike,bolt,blast spells.
//        MAGIC_UNARMED_WAVE_SPELL, MAGIC_STAFF_WAVE_SPELL,
//        MAGIC_GOD_SPELL,
//        MAGIC_CRUMBLE_UNDEAD_UNARMED, MAGIC_CRUMBLE_UNDEAD_STAFF,
//        MAGIC_CONFUSE_UNARMED, MAGIC_CONFUSE_STAFF,
//        MAGIC_WEAKEN_UNARMED, MAGIC_WEAKEN_STAFF,
//        MAGIC_CURSE_VULNERABILITY_UNARMED, MAGIC_CURSE_VULNERABILITY_STAFF,
//        MAGIC_ENFEEBLE_UNARMED, MAGIC_ENFEEBLE_STAFF,
//        MAGIC_STUN_UNARMED, MAGIC_STUN_STAFF,
//        MAGIC_BIND_UNARMED, MAGIC_BIND_STAFF,
//        MAGIC_ANCIENT_SINGLE_TARGET,
//        MAGIC_ANCIENT_MULTI_TARGET,

        ATTACK(ATTACK_STAB, ATTACK_SLASH, ATTACK_SLASH2, ATTACK_CRUSH, ATTACK_CRUSH2, ATTACK_SPEC),
//        MAGIC(),
        MOVEMENT(WALK, RUN, WALK_BACKWARD, SHUFFLE_LEFT, SHUFFLE_RIGHT, ROTATE),
        STAND_PLUS_MOVEMENT("Stand/Move", STAND, MOVEMENT),
        ALL(ATTACK, STAND_PLUS_MOVEMENT, DEFEND);

        public static final List<AnimationType> comboBoxOrder = new ArrayList<>();
        static {
            comboBoxOrder.add(ALL);
            comboBoxOrder.add(STAND_PLUS_MOVEMENT);
            comboBoxOrder.add(STAND);
            comboBoxOrder.add(MOVEMENT);
//            addItems(MOVEMENT, comboBoxOrder);
            comboBoxOrder.add(ATTACK);
            addItems(ATTACK, comboBoxOrder);
        }

        private static void addItems(AnimationType animationType, List<AnimationType> comboBoxOrder) {
            animationType.children.forEach(comboBoxOrder::add);
        }

        public final List<AnimationType> children;
        public final String prettyName;

        AnimationType(AnimationType... children) {
            this(null, children);
        }

        AnimationType(String prettyName, AnimationType... children) {
            String s = toString().replaceAll("_", " ");
            this.prettyName = prettyName != null ? prettyName : s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase();
            this.children = Arrays.asList(children);
        }

        public boolean appliesTo(AnimationType type) {
            return this == type || children.stream().anyMatch(child -> child.appliesTo(type));
        }

		public boolean hasChildren() {
			return !children.isEmpty();
		}

		public boolean hasChild(AnimationType type) {
            return children.stream().anyMatch(child -> child.appliesTo(type));
        }

        public String getComboBoxName() {
            int depth = 0;
            if (ATTACK.hasChild(this)) {
                depth = 2;
            } else if (MOVEMENT.hasChild(this)) {
                depth = 4;
            } else if (STAND_PLUS_MOVEMENT.hasChild(this)) {
                depth = 3;
            } else if (ALL.hasChild(this)) {
                depth = 1;
            }
            String s = prettyName;
            for (int i = 0; i < depth; i++) {
                s = "  " + s;
            }
            return s;
        }
    }

    @Override
    public String toString() {
        return itemRestrictions + " " + modelSwaps + " " + animationReplacements;
    }
}
