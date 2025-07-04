package com.weaponanimationreplacer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import net.runelite.client.game.ItemManager;

@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public final class ProjectileCast
{
	int id;

	String name;
	int itemIdIcon;
	int spriteIdIcon;

	int castAnimation;
	int castGfx;
	int castGfxHeight;
	int projectileId;
	int hitGfx;
	int hitGfxHeight;

	int startMovement;
	int startPos;
	int startHeight;
	int endHeight;
	int slope;

	boolean artificial;

	public String getName(ItemManager itemManager) {
		return name != null ? name : itemManager.getItemComposition(itemIdIcon).getName() + itemIdIcon;
	}

	public static class ProjectileCastBuilder {

		// These need to be -1 by default because -1 means not present and 0 will result in a projectile/icon/etc.
		int spriteIdIcon = -1;
		int itemIdIcon = -1;
		int castGfx = -1;
		int hitGfx = -1;
		int projectileId = -1;

		boolean artificial = false;

		public ProjectileCastBuilder cast(int castAnimation, int castGfx, int castGfxHeight) {
			this.castAnimation = castAnimation;
			this.castGfx = castGfx;
			this.castGfxHeight = castGfxHeight;
			return this;
		}

		public ProjectileCastBuilder projectile(int projectileId, int startMovement, int startPos, int startHeight, int endHeight, int slope) {
			this.projectileId = projectileId;
			this.hitGfxHeight = endHeight; // Good enough for most spells, only a few have a different value here.
			this.startMovement = startMovement;
			this.startPos = startPos;
			this.startHeight = startHeight;
			this.endHeight = endHeight;
			this.slope = slope;
			return this;
		}

		// TODO ideally remove this once you have the right values.
		// reasonable default.
		@Deprecated
		public ProjectileCastBuilder ids(int castAnimation, int castGfx, int projectileId, int hitGfx, int startMovement, int startPos, int endHeight, int slope) {
			return ids(castAnimation, castGfx, 92, projectileId, hitGfx, startMovement, startPos, 172, endHeight, slope);
		}

		@Deprecated
		public ProjectileCastBuilder ids(int castAnimation, int castGfx, int castGfxHeight, int projectileId, int hitGfx, int startMovement, int startPos, int startHeight, int endHeight, int slope) {
			this.castAnimation = castAnimation;
			this.castGfx = castGfx;
			this.castGfxHeight = castGfxHeight;
			this.hitGfx = hitGfx;
			this.hitGfxHeight = endHeight; // Good enough for most spells, only a few have a different value here.

			this.projectileId = projectileId;
			this.startMovement = startMovement;
			this.startPos = startPos;
			this.startHeight = startHeight;
			this.endHeight = endHeight;
			this.slope = slope;

			return this;
		}

		public ProjectileCastBuilder hit(int hitGfx, int hitGfxHeight) {
			this.hitGfx = hitGfx;
			this.hitGfxHeight = hitGfxHeight;
			return this;
		}

		public ProjectileCastBuilder simpleSpell(int castAnimation, int hitGfx) {
			this.castAnimation = castAnimation;
			this.hitGfx = hitGfx;
			this.hitGfxHeight = 0;
			return this;
		}

		public ProjectileCastBuilder sprite(int spriteId) {
			this.spriteIdIcon = spriteId;
			return this;
		}

		public ProjectileCastBuilder itemId(int itemId) { //
			this.itemIdIcon = itemId;
			return this;
		}

		public ProjectileCastBuilder artificial(boolean artificial) {
			this.artificial = artificial;
			return this;
		}

		public ProjectileCastBuilder artificial() { // more convenient syntax
			this.artificial = true;
			return this;
		}
	}

	public static ProjectileCastBuilder p() {
		return new ProjectileCastBuilder();
	}
}
