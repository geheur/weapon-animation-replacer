package com.weaponanimationreplacer;

import static com.weaponanimationreplacer.Swap.AnimationReplacement;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * Represents a set of:
 *  Configurable name to describe what this does.
 *  List of weapon slot items that this should be used with. If there are none, it applies always.
 *  List of animation replacements, which each contain an animation type to replace, and animation set to use for replacement, and, optionally, an animation type to use as the replacement (if it is different from the one being replaced).
 */
public class AnimationReplacementRule_OLD {
	String name = "";
	public int modelSwap = -1;
	public List<ItemRestriction> itemRestrictions;
	public List<AnimationReplacement> animationReplacements;

	public AnimationReplacementRule_OLD(List<ItemRestriction> itemRestrictions, List<AnimationReplacement> animationReplacements) {
		this.itemRestrictions = itemRestrictions;
		this.animationReplacements = animationReplacements;
	}

	public static AnimationReplacementRule_OLD createTemplate(AnimationSet animationSet) {
		List<ItemRestriction> itemRestrictions = new ArrayList<>();
		List<AnimationReplacement> animationReplacements = new ArrayList<>();
		AnimationReplacementRule_OLD animationReplacementRule = new AnimationReplacementRule_OLD(itemRestrictions, animationReplacements);
		animationReplacementRule.name = "New Replacement";
		return animationReplacementRule;
	}

	@Data
	public static class ItemRestriction {
		boolean enabled;
		int itemId;

		public ItemRestriction(int itemId) {
			this(itemId, true);
		}

		public ItemRestriction(int itemId, boolean enabled) {
			this.itemId = itemId;
			this.enabled = enabled;
		}

		public boolean appliesToItem(int itemId) {
			return enabled && itemId == this.itemId;
		}
	}
}
