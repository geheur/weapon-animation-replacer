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
		return ProjectileCast.projectiles.get(toReplace);
	}

	public ProjectileCast getToReplaceWith() {
		if (toReplaceWithCustom != null) return toReplaceWithCustom;
		if (toReplaceWith == -1) return null;
		return ProjectileCast.projectiles.get(toReplaceWith);
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
			toReplaceWithCustom = ProjectileCast.copy(ProjectileCast.projectiles.get(toReplaceWith));
		}
	}
}
