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
		return ProjectileCast.projectiles.get(toReplace);
	}

	public ProjectileCast getToReplaceWith() {
		return ProjectileCast.projectiles.get(toReplaceWith);
	}

	public static ProjectileSwap createTemplate()
	{
		return new ProjectileSwap();
	}
}
