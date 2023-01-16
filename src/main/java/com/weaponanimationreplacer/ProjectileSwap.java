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
		if (toReplace == -1 || toReplace == toReplaceWith) return null; // if they're the same this leads to a stackoverflow.
		return ProjectileCast.projectiles.get(toReplace);
	}

	public ProjectileCast getToReplaceWith() {
		if (toReplaceWith == -1 || toReplace == toReplaceWith) return null; // if they're the same this leads to a stackoverflow.
		return ProjectileCast.projectiles.get(toReplaceWith);
	}

	public static ProjectileSwap createTemplate()
	{
		return new ProjectileSwap();
	}
}
