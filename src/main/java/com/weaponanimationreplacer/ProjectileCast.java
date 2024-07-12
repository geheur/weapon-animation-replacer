package com.weaponanimationreplacer;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.client.game.ItemManager;

@AllArgsConstructor
@Data
public final class ProjectileCast
{
	int id;

	String name;
	int itemIdIcon;
	int spriteIdIcon;

	int castAnimation;
	int castGfx;
	int projectileId;
	int hitGfx;
	int hitGfxHeight;

	int startMovement;
	int startHeight; // This is not the start height, this is the horizontal distance from the source towards the target at which the projectile is spawned.
	int height; // This is the actual height above the current tile height that the projectile should be at.
	int endHeight;
	int slope;

	boolean artificial;

	public static ProjectileCast copy(ProjectileCast toCopy)
	{
		return new ProjectileCast(toCopy.id, toCopy.name, toCopy.itemIdIcon, toCopy.spriteIdIcon, toCopy.castAnimation, toCopy.castGfx, toCopy.projectileId, toCopy.hitGfx, toCopy.hitGfxHeight, toCopy.startMovement, toCopy.startHeight, toCopy.height, toCopy.endHeight, toCopy.slope, true);
	}

	public String getName(ItemManager itemManager) {
		return name != null ? name : itemManager.getItemComposition(itemIdIcon).getName() + itemIdIcon;
	}

	public static class ProjectileCastBuilder {
		int id = -1;

		String name = null;
		int itemIdIcon = -1;
		int spriteIdIcon = -1;

		int castAnimation = -1;
		int castGfx = -1;
		int castGfxHeight = -1;
		int projectileId = -1;
		int hitGfx = -1;
		int hitGfxHeight = -1;

		int startMovement = -1;
		int startHeight = -1;
		int height = -172; // reasonable default.
		int endHeight = -1;
		int slope = -1;

		boolean artificial = false;

		public ProjectileCastBuilder cast(int castAnimation, int castGfx) {
			this.castAnimation = castAnimation;
			this.castGfx = castGfx;
			return this;
		}

		public ProjectileCastBuilder id(int id) {
			this.id = id;
			return this;
		}

		public ProjectileCastBuilder projectile(int projectileId, int startMovement, int startHeight, int height, int endHeight, int slope) {
			this.projectileId = projectileId;
			this.hitGfxHeight = endHeight; // Good enough for most spells, only a few have a different value here.
			this.startMovement = startMovement;
			this.startHeight = startHeight;
			this.height = height;
			this.endHeight = endHeight;
			this.slope = slope;
			return this;
		}

		// TODO ideally remove this once you have the right values.
		// reasonable default.
		public ProjectileCastBuilder ids(int castAnimation, int castGfx, int projectileId, int hitGfx, int startMovement, int startHeight, int endHeight, int slope) {
			return ids(castAnimation, castGfx, 0, projectileId, hitGfx, startMovement, startHeight, -172, endHeight, slope);
		}

		public ProjectileCastBuilder ids(int castAnimation, int castGfx, int castGfxHeight, int projectileId, int hitGfx, int startMovement, int startHeight, int height, int endHeight, int slope) {
			this.castAnimation = castAnimation;
			this.castGfx = castGfx;
			this.castGfxHeight = castGfxHeight;
			this.projectileId = projectileId;
			this.hitGfx = hitGfx;
			this.hitGfxHeight = endHeight;
			this.startMovement = startMovement;
			this.startHeight = startHeight;
			this.endHeight = endHeight;
			this.slope = slope;
			return this;
		}

		public ProjectileCastBuilder hitGfx(int hitGfx, int hitGfxHeight) {
			this.hitGfx = hitGfx;
			this.hitGfxHeight = hitGfxHeight;
			return this;
		}

		public ProjectileCastBuilder ids(int castAnimation, int castGfx, int hitGfx) {
			return ids(castAnimation, castGfx, hitGfx, 0);
		}

		public ProjectileCastBuilder ids(int castAnimation, int castGfx, int hitGfx, int hitGfxHeight) {
			this.castAnimation = castAnimation;
			this.castGfx = castGfx;
			this.projectileId = -1;
			this.hitGfx = hitGfx;
			this.hitGfxHeight = hitGfxHeight;
			this.startMovement = -1;
			this.startHeight = -1;
			this.endHeight = -1;
			this.slope = -1;
			return this;
		}

		public ProjectileCastBuilder name(String name) {
			this.name = name;
			return this;
		}

		public ProjectileCastBuilder itemId(int itemId) {
			this.itemIdIcon = itemId;
			return this;
		}

		public ProjectileCastBuilder sprite(int spriteId) {
			this.spriteIdIcon = spriteId;
			return this;
		}

		public ProjectileCastBuilder artificial() {
			this.artificial = true;
			return this;
		}

		public ProjectileCast build() {
			return new ProjectileCast(id, name, itemIdIcon, spriteIdIcon, castAnimation, castGfx, projectileId, hitGfx, hitGfxHeight, startMovement, startHeight, height, endHeight, slope, artificial);
		}
	}

	public static ProjectileCastBuilder p() {
		return new ProjectileCastBuilder();
	}
}
