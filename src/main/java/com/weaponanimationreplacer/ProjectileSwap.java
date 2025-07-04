package com.weaponanimationreplacer;

import lombok.Data;

@Data
public class ProjectileSwap
{
	int toReplace = -1;
	int toReplaceWith = -1;
	ProjectileCast toReplaceCustom = null;
	ProjectileCast toReplaceWithCustom = null;

	public ProjectileCast getToReplace() {
		if (toReplace == -1) return null;
		return Constants.projectilesById[toReplace];
	}

	public ProjectileCast getToReplaceWith() {
		if (toReplaceWithCustom != null) return toReplaceWithCustom;
		if (toReplaceWith == -1) return null;
		return Constants.projectilesById[toReplaceWith];
	}

	public static ProjectileSwap createTemplate()
	{
		return new ProjectileSwap();
	}

	public void createCustomIfNull()
	{
		if (toReplaceWithCustom != null) return;

		if (toReplaceWith == -1) {
			toReplaceWithCustom = ProjectileCast.p().build();
		} else {
			toReplaceWithCustom = Constants.projectilesById[toReplaceWith].toBuilder().build();
		}
	}
}
