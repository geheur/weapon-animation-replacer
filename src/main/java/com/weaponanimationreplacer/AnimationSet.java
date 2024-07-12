package com.weaponanimationreplacer;

import com.weaponanimationreplacer.Swap.AnimationType;
import static com.weaponanimationreplacer.Swap.AnimationType.ATTACK;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AnimationSet implements Comparable<AnimationSet> {
	public final String name;
	public final int[] animations;
	public final boolean doNotReplace;

	AnimationSet() {
		this("", false, new int[AnimationType.values().length]);
	}

	AnimationSet(String name, boolean doNotReplace, int[] animations) {
		this.name = name;
		this.doNotReplace = doNotReplace;
		assert animations.length == AnimationType.values().length;
		this.animations = animations;
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
		return name + " " + Arrays.stream(animations).boxed().collect(Collectors.toList());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AnimationSet that = (AnimationSet) o;
		return Objects.equals(name, that.name) && Arrays.equals(animations, that.animations);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, animations);
	}

	public String getComboBoxName() {
		return name;
	}
}
