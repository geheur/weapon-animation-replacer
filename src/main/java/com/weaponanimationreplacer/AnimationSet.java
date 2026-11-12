package com.weaponanimationreplacer;

import com.weaponanimationreplacer.Swap.AnimationType;
import static com.weaponanimationreplacer.Swap.AnimationType.ATTACK;
import static com.weaponanimationreplacer.Swap.AnimationType.ATTACK_CRUSH;
import static com.weaponanimationreplacer.Swap.AnimationType.ATTACK_CRUSH2;
import static com.weaponanimationreplacer.Swap.AnimationType.ATTACK_SLASH;
import static com.weaponanimationreplacer.Swap.AnimationType.ATTACK_SLASH2;
import static com.weaponanimationreplacer.Swap.AnimationType.ATTACK_SPEC;
import static com.weaponanimationreplacer.Swap.AnimationType.ATTACK_STAB;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AnimationSet implements Comparable<AnimationSet> {
	public final String name;
	public final int[] animations;
	public final int[] gfx;
	public final int[] gfxHeight;
	public final boolean doNotReplace;
	public final boolean custom;

	public enum GfxSlots {
		STAB_GFX(AnimationType.ATTACK_STAB.ordinal()),
		SLASH_GFX(AnimationType.ATTACK_SLASH.ordinal()),
		CRUSH_GFX(AnimationType.ATTACK_CRUSH.ordinal()),
		SPEC_GFX(AnimationType.ATTACK_SPEC.ordinal()),
		SLASH2_GFX(AnimationType.ATTACK_SLASH2.ordinal()),
		CRUSH2_GFX(AnimationType.ATTACK_CRUSH2.ordinal());
		// TODO add ATTACK_GFX

		final int animationSetOrdinal;

		GfxSlots(int animationSetOrdinal) {
			this.animationSetOrdinal = animationSetOrdinal;
		}

		private static GfxSlots[] gfxMap = new GfxSlots[AnimationType.values().length];
		static {
			gfxMap[ATTACK_STAB.ordinal()] = GfxSlots.STAB_GFX;
			gfxMap[ATTACK_SLASH.ordinal()] = GfxSlots.SLASH_GFX;
			gfxMap[ATTACK_SLASH2.ordinal()] = GfxSlots.SLASH2_GFX;
			gfxMap[ATTACK_CRUSH.ordinal()] = GfxSlots.CRUSH_GFX;
			gfxMap[ATTACK_CRUSH2.ordinal()] = GfxSlots.CRUSH2_GFX;
			gfxMap[ATTACK_SPEC.ordinal()] = GfxSlots.SPEC_GFX;
		}

		public static GfxSlots forAnimationType(AnimationType child)
		{
			return gfxMap[child.ordinal()];
		}
	}

	AnimationSet() {
		this("", false);
	}

	AnimationSet(String name, boolean custom) {
		this(name, false, new int[AnimationType.values().length], new int[GfxSlots.values().length], new int[GfxSlots.values().length], custom);
		Arrays.fill(animations, -1);
		Arrays.fill(gfx, -1);
		Arrays.fill(gfxHeight, -1);
	}

	AnimationSet(String name, boolean doNotReplace, int[] animations, boolean custom) {
		this(name, doNotReplace, animations, new int[GfxSlots.values().length], new int[GfxSlots.values().length], custom);
	}

	AnimationSet(String name, boolean doNotReplace, int[] animations, int[] gfx, int[] gfxHeight, boolean custom) {
		this.name = name;
		this.doNotReplace = doNotReplace;
		assert animations.length == AnimationType.values().length;
		this.animations = animations;
		this.gfx = gfx;
		this.gfxHeight = gfxHeight;
		this.custom = custom;
	}

	public static AnimationSet withName(AnimationSet as, String name)
	{
		return new AnimationSet(name, false, Arrays.copyOf(as.animations, as.animations.length), Arrays.copyOf(as.gfx, as.gfx.length), Arrays.copyOf(as.gfxHeight, as.gfxHeight.length), true);
	}

	@Override
	public int compareTo(AnimationSet o) {
		return name.compareTo(o.name);
	}

	public List<AnimationType> getAttackAnimations()
	{
		List<AnimationType> result = new ArrayList<>();
		if (animations[ATTACK.ordinal()] != -1) {
			result.add(AnimationType.values()[ATTACK.ordinal()]);
		}
		for (AnimationType child : ATTACK.children)
		{
			if (animations[child.ordinal()] != -1) {
				result.add(AnimationType.values()[child.ordinal()]);
			}
		}
		return result;
	}

	public static String getDescription(AnimationSet animationSet, AnimationType animation)
	{
		int animationId = animationSet.getAnimation(animation);
		if (animationId != -1) {
			String s = Constants.descriptions.get(animationId);
			if (s != null) return s;
		}
		return animation.getComboBoxName();
	}

	public static AnimationSet getAnimationSet(String name) {
		return Constants.animationSets.stream().filter(a -> name.equals(a.name)).findAny().orElse(null);
	}

	public int getAnimation(AnimationType type) {
		return animations[type.ordinal()];
	}

	public int getGraphic(GfxSlots gfxType) {
		return gfx[gfxType.ordinal()];
	}

	public int getGraphic(AnimationType type) {
		return gfx[GfxSlots.forAnimationType(type).ordinal()];
	}

	public int getGraphicHeight(GfxSlots gfxType) {
		return gfxHeight[gfxType.ordinal()];
	}

	public int getGraphicHeight(AnimationType type) {
		return gfxHeight[GfxSlots.forAnimationType(type).ordinal()];
	}

	public AnimationType getType(int animationId) {
		for (int i = 0; i < animations.length; i++)
		{
			int animation = animations[i];
			if (animation > 0 && animation == animationId) {
				return AnimationType.values()[i];
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return (custom ? "(c) " : "") + name + " " + Arrays.stream(animations).boxed().collect(Collectors.toList());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AnimationSet that = (AnimationSet) o;
		return Objects.equals(name, that.name) && this.custom == that.custom && this.doNotReplace == that.doNotReplace && Arrays.equals(animations, that.animations) && Arrays.equals(this.gfx, that.gfx);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, animations);
	}

	public String getComboBoxName() {
		return name;
	}
}
