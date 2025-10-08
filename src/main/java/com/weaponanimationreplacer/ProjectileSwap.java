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
		if (toReplaceCustom != null) return toReplaceCustom;
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

	public void createCustomIfNull(boolean rhs)
	{
		int id = rhs ? toReplaceWith : toReplace;
		ProjectileCast pc = rhs ? toReplaceWithCustom : toReplaceCustom;

		if (pc != null) return;

		if (id == -1) {
			pc = ProjectileCast.p().build();
		} else {
			pc = Constants.projectilesById[id].toBuilder().build();
		}
		if (rhs) toReplaceWithCustom = pc;
		else toReplaceCustom = pc;
	}

	public ProjectileCast getCustom(boolean rhs)
	{
		return rhs ? toReplaceWithCustom : toReplaceCustom;
	}
}
