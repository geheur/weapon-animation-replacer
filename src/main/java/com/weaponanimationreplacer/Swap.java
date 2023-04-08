package com.weaponanimationreplacer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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
    private final List<Integer> itemRestrictions;
	private final List<Integer> modelSwaps;
	public final List<AnimationReplacement> animationReplacements;
	@Getter
	private final List<ProjectileSwap> projectileSwaps;
	@Getter
	private final List<GraphicEffect> graphicEffects;

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
		this.itemRestrictions = new ArrayList<>(itemRestrictions);
		this.modelSwaps = new ArrayList<>(modelSwaps);
		this.animationReplacements = new ArrayList<>(animationReplacements);
		this.projectileSwaps = new ArrayList<>(projectileSwaps);
		this.graphicEffects = new ArrayList<>(graphicEffects);
    }

    public List<Integer> getItemRestrictions() {
    	return Collections.unmodifiableList(itemRestrictions);
	}

	public List<Integer> getModelSwaps() {
		return Collections.unmodifiableList(modelSwaps);
	}

	public void addModelSwap(Integer itemId, WeaponAnimationReplacerPlugin plugin)
	{
		Function<Integer, Integer> getSlot = plugin::getMySlot;
		// cannot equip multiple items in the same slot.
		Integer newItemSlot = getSlot.apply(itemId);
		if (newItemSlot == null || newItemSlot == EquipmentInventorySlot.RING.getSlotIdx() || newItemSlot == EquipmentInventorySlot.AMMO.getSlotIdx()) return;
		modelSwaps.removeIf(id -> {
			Integer slot = getSlot.apply(id);
			return slot == null || slot == EquipmentInventorySlot.RING.getSlotIdx() || slot == EquipmentInventorySlot.AMMO.getSlotIdx() ?
				true : slot == newItemSlot;
		});

		int index = Collections.binarySearch(modelSwaps, itemId, itemComparator(getSlot));
		if (index >= 0) return; // no duplicates allowed in this list. This shouldn't happen due to the same slot removal above, but it doesn't hurt to check.
		modelSwaps.add(~index, itemId);
	}

	public void removeModelSwap(int prevItemId)
	{
		modelSwaps.remove((Integer) prevItemId); // Cast is necessary to use the right overload of the method.
	}

	public void replaceModelSwap(int prevItemId, int newItemId, WeaponAnimationReplacerPlugin plugin)
	{
		removeModelSwap(prevItemId);
		addModelSwap(newItemId, plugin);
	}

	public void addTriggerItem(Integer itemId, WeaponAnimationReplacerPlugin plugin)
	{
		Function<Integer, Integer> getSlot = plugin::getSlot;
		Integer newItemSlot = getSlot.apply(itemId);
		if (newItemSlot == null || newItemSlot == EquipmentInventorySlot.RING.getSlotIdx() || newItemSlot == EquipmentInventorySlot.AMMO.getSlotIdx()) return;

		int index = Collections.binarySearch(itemRestrictions, itemId, itemComparator(getSlot));
		if (index >= 0) return; // no duplicates allowed in this list.
		itemRestrictions.add(~index, itemId);
	}

	public void removeTriggerItem(int prevItemId)
	{
		itemRestrictions.remove((Integer) prevItemId); // Cast is necessary to use the right overload of the method.
	}

	public void replaceTriggerItem(int prevItemId, int newItemId, WeaponAnimationReplacerPlugin plugin)
	{
		removeTriggerItem(prevItemId);
		addTriggerItem(newItemId, plugin);
	}

	private static final int[] MY_SLOT_ORDER = new int[]{2, 5, 6, 0, 7, 1, 8, 9, 3, 10, 11, 4, 12, 13};

	private Comparator<Integer> itemComparator(Function<Integer, Integer> getSlot)
	{
		return (id1, id2) -> {
			Integer slotForItem1 = getSlot.apply(id1);
			Integer slotForItem2 = getSlot.apply(id2);
			if (slotForItem1 == null) {
				if (slotForItem2 == null) {
					return Integer.compare(id1, id2);
				} else {
					return -1;
				}
			} else if (slotForItem2 == null) {
				return 1;
			}

			if (slotForItem1 == slotForItem2) {
				return Integer.compare(id1, id2);
			}

			return Integer.compare(MY_SLOT_ORDER[slotForItem1], MY_SLOT_ORDER[slotForItem2]);
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

	public boolean appliesToGear(List<Integer> equippedItemIds, Function<Integer, Integer> getSlot)
	{
		if (itemRestrictions.contains(-1)) return true;
		return appliesSpecificallyToGear(equippedItemIds, getSlot);
	}

	/**
	 * returns true if each slot of the equipped gear that has a corresponding trigger item matches at least one of the trigger items for that slot.
	 * In other words, all trigger items must match unless there are multiple for the same slot in which case only one much match.
	 */
	public boolean appliesSpecificallyToGear(List<Integer> equippedItemIds, Function<Integer, Integer> getSlot)
	{
		Set<Integer> slots = new HashSet<>();
		Set<Integer> slotsSatisfied = new HashSet<>();
		for (Integer itemRestriction : itemRestrictions)
		{
			if (itemRestriction == -1) {
				return false;
			}

			int slot = getSlot.apply(itemRestriction);
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

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}
		Swap swap = (Swap) o;
		return Objects.equals(itemRestrictions, swap.itemRestrictions) && Objects.equals(modelSwaps, swap.modelSwaps) && Objects.equals(animationReplacements, swap.animationReplacements) && Objects.equals(graphicEffects, swap.graphicEffects);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(itemRestrictions, modelSwaps, animationReplacements, graphicEffects);
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
            return this == type || (children != null && children.stream().anyMatch(child -> child.appliesTo(type)));
        }

        public boolean hasChild(AnimationType type) {
            return (children != null && children.stream().anyMatch(child -> child.appliesTo(type)));
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
