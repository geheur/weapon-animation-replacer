package com.weaponanimationreplacer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import lombok.Data;
import net.runelite.api.ItemID;

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
	private List<GraphicEffect> graphicEffects;

    public Swap(List<Integer> itemRestrictions, List<Integer> modelSwaps, List<AnimationReplacement> animationReplacements, List<GraphicEffect> graphicEffects) {
		if (itemRestrictions.isEmpty()) {
        	this.itemRestrictions = new ArrayList<>();
        	this.itemRestrictions.add(-1);
		} else {
			this.itemRestrictions = new ArrayList<>(itemRestrictions);
		}
		if (modelSwaps.isEmpty()) {
			this.modelSwaps = new ArrayList<>();
			this.modelSwaps.add(-1);
		} else {
			this.modelSwaps = new ArrayList<>(modelSwaps);
		}
		this.animationReplacements = new ArrayList<>(animationReplacements);
		this.graphicEffects = new ArrayList<>(graphicEffects);
    }

    public List<Integer> getItemRestrictions() {
    	return Collections.unmodifiableList(itemRestrictions);
	}

	public void setItemRestriction(int index, int itemId)
	{
		setItem(index, itemId, itemRestrictions);
	}

	public List<Integer> getModelSwaps() {
		return Collections.unmodifiableList(modelSwaps);
	}

	public void setModelSwap(int index, int itemId)
	{
		setItem(index, itemId, modelSwaps);
	}

	public List<GraphicEffect> getGraphicEffects() {
    	if (graphicEffects == null) graphicEffects = new ArrayList<>(); // idk, gson overrides the default value if there's not value for it in the json.
		return graphicEffects;
	}

	private void setItem(int index, int itemId, List<Integer> list)
	{
		for (int i = 0; i < list.size(); i++)
		{
			if (list.get(i) == -1) {
				index = i;
				break;
			}
		}
		if (itemId != ItemID.BANK_FILLER || index < list.size())
		{
			if (itemId == ItemID.BANK_FILLER && list.size() == 1)
			{
				list.set(index, -1);
			}
			else if (itemId == ItemID.BANK_FILLER && list.size() > 1)
			{
				list.remove(index);
			}
			else
			{
				if (index >= list.size())
				{
					list.add(itemId);
				}
				else
				{
					list.set(index, itemId);
				}
			}
		}
	}

	public static Swap createTemplate() {
        List<Integer> itemRestrictions = new ArrayList<>();
        List<AnimationReplacement> animationReplacements = new ArrayList<>();
		List<GraphicEffect> graphicEffects = new ArrayList<>();
		Swap swap = new Swap(itemRestrictions, new ArrayList<>(), animationReplacements, graphicEffects);
        return swap;
    }

	public void addNewAnimationReplacement()
	{
		animationReplacements.add(AnimationReplacement.createTemplate());
	}

	public void addNewGraphicEffect()
	{
		graphicEffects.add(GraphicEffect.createTemplate());
	}

	public void addNewModelSwap(Integer itemId)
	{
		setModelSwap(modelSwaps.size(), itemId);
	}

	public void addNewTriggerItem(Integer itemId)
	{
		setItemRestriction(itemRestrictions.size(), itemId);
	}

	public boolean appliesToGear(List<Integer> equippedItemIds, Function<Integer, Integer> getSlot)
	{
		return appliesToGear(equippedItemIds, false, getSlot);
	}

	public boolean appliesSpecificallyToGear(List<Integer> equippedItemIds, Function<Integer, Integer> getSlot)
	{
		return appliesToGear(equippedItemIds, true, getSlot);
	}

	private boolean appliesToGear(List<Integer> equippedItemIds, boolean specific, Function<Integer, Integer> getSlot)
	{
		Set<Integer> slots = new HashSet<>();
		Set<Integer> slotsSatisfied = new HashSet<>();
		for (Integer itemRestriction : itemRestrictions)
		{
			if (itemRestriction == -1) {
				continue;
			}

			int slot = getSlot.apply(itemRestriction);
			slots.add(slot);
			if (equippedItemIds.contains(itemRestriction) || (!specific && itemRestriction == -1)) {
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
            if (animationSet == null) return;
			Integer animation1 = animationSet.getAnimation(animationtypeReplacement, false);
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

        @Override
        public int compareTo(AnimationReplacement o) {
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
