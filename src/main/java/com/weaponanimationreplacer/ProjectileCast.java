package com.weaponanimationreplacer;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import net.runelite.api.ItemID;
import net.runelite.api.SpriteID;
import net.runelite.client.game.ItemManager;

@Value
public final class ProjectileCast
{
	/*
	Types of projectiles:
	hit gfx (most spells).
		the hit gfx is applied when the spell casts, but has a timer before it actually shows.
			This means if you're at a very long range it is possible for the hit gfx to never occur because it is replaced by the next spell cast before it goes off.
		many of these can splash animation instead.
	no hit gfx (ranged attacks mostly).
	no projectile spells (such as ice barrage).
	multiple projectile spells (such as dark bow).
		Dark bow spec is actually 4 projectiles (with 2 different versions, one with dragon arrows one without).
	 */
	public static final List<ProjectileCast> projectiles = new ArrayList<>();
	// TODO how to handle ids, using indexes makes this list not be nicely sortable.
	// TODO any arrow, any spell.
	static {
		initializeSpells();
	}
	static void initializeSpells() {
		projectiles.clear();
		// Standard spellbook.
		projectiles.add(p().name("Wind Strike").sprite(SpriteID.SPELL_WIND_STRIKE).ids(1162, 90, 91, 92, 51, 64, 124, 16).build());
		projectiles.add(p().name("Confuse").sprite(SpriteID.SPELL_CONFUSE).ids(1163, 102, 103, 104, 61, 64, 124, 16).build());
		projectiles.add(p().name("Water Strike").sprite(SpriteID.SPELL_WATER_STRIKE).ids(1162, 93, 94, 95, 51, 64, 124, 16).build());
		projectiles.add(p().name("Earth Strike").sprite(SpriteID.SPELL_EARTH_STRIKE).ids(1162, 96, 97, 98, 51, 64, 124, 16).build());
		projectiles.add(p().name("Weaken").sprite(SpriteID.SPELL_WEAKEN).ids(1164, 105, 106, 107, 44, 64, 124, 16).build());
		projectiles.add(p().name("Fire Strike").sprite(SpriteID.SPELL_FIRE_STRIKE).ids(1162, 99, 100, 101, 51, 64, 124, 16).build());
		projectiles.add(p().name("Wind Bolt").sprite(SpriteID.SPELL_WIND_BOLT).ids(1162, 117, 118, 119, 51, 64, 124, 16).build());
		projectiles.add(p().name("Curse").sprite(SpriteID.SPELL_CURSE).ids(1165, 108, 109, 110, 51, 64, 124, 16).build());
		projectiles.add(p().name("Bind").sprite(SpriteID.SPELL_BIND).ids(1161, 177, 178, 181, 75, 64, 0, 16).build());
		projectiles.add(p().name("Water Bolt").sprite(SpriteID.SPELL_WATER_BOLT).ids(1162, 120, 121, 122, 51, 64, 124, 16).build());
		projectiles.add(p().name("Earth Bolt").sprite(SpriteID.SPELL_EARTH_BOLT).ids(1162, 123, 124, 125, 51, 64, 124, 16).build());
		projectiles.add(p().name("Fire Bolt").sprite(SpriteID.SPELL_FIRE_BOLT).ids(1162, 126, 127, 128, 51, 64, 124, 16).build());
		projectiles.add(p().name("Crumble Undead").sprite(SpriteID.SPELL_CRUMBLE_UNDEAD).ids(1166, 145, 146, 147, 46, 64, 124, 16).build());
		projectiles.add(p().name("Wind Blast").sprite(SpriteID.SPELL_WIND_BLAST).ids(1162, 132, 133, 134, 51, 64, 124, 16).build());
		projectiles.add(p().name("Water Blast").sprite(SpriteID.SPELL_WATER_BLAST).ids(1162, 135, 136, 137, 51, 64, 124, 16).build());
		projectiles.add(p().name("Snare").sprite(SpriteID.SPELL_SNARE).ids(1161, 177, 178, 180, 75, 64, 0, 16).build());
		projectiles.add(p().name("Earth Blast").sprite(SpriteID.SPELL_EARTH_BLAST).ids(1162, 138, 139, 140, 51, 64, 124, 16).build());
		projectiles.add(p().name("Fire Blast").sprite(SpriteID.SPELL_FIRE_BLAST).ids(1162, 129, 130, 131, 51, 64, 124, 16).build());
		projectiles.add(p().name("Wind Wave").sprite(SpriteID.SPELL_WIND_WAVE).ids(1167, 158, 159, 160, 51, 64, 124, 16).build());
		projectiles.add(p().name("Water Wave").sprite(SpriteID.SPELL_WATER_WAVE).ids(1167, 161, 162, 163, 51, 64, 124, 16).build());
		projectiles.add(p().name("Vulnerability").sprite(SpriteID.SPELL_VULNERABILITY).ids(1165, 167, 168, 169, 34, 64, 124, 16).build());
		projectiles.add(p().name("Earth Wave").sprite(SpriteID.SPELL_EARTH_WAVE).ids(1167, 164, 165, 166, 51, 64, 124, 16).build());
		projectiles.add(p().name("Enfeeble").sprite(SpriteID.SPELL_ENFEEBLE).ids(1168, 170, 171, 172, 48, 64, 124, 16).build());
		projectiles.add(p().name("Fire Wave").sprite(SpriteID.SPELL_FIRE_WAVE).ids(1167, 155, 156, 157, 51, 64, 124, 16).build());
		projectiles.add(p().name("Entangle").sprite(SpriteID.SPELL_ENTANGLE).ids(1161, 177, 178, 179, 75, 64, 0, 16).build());
		projectiles.add(p().name("Stun").sprite(SpriteID.SPELL_STUN).ids(1169, 173, 174, 80, 52, 64, 124, 16).build());
		projectiles.add(p().name("Wind Surge").sprite(SpriteID.SPELL_WIND_SURGE).ids(7855, 1455, 1456, 1457, 51, 64, 124, 16).build());
		projectiles.add(p().name("Water Surge").sprite(SpriteID.SPELL_WATER_SURGE).ids(7855, 1458, 1459, 1460, 51, 64, 124, 16).build());
		projectiles.add(p().name("Earth Surge").sprite(SpriteID.SPELL_EARTH_SURGE).ids(7855, 1461, 1462, 1463, 51, 64, 124, 16).build());
		projectiles.add(p().name("Fire Surge").sprite(SpriteID.SPELL_FIRE_SURGE).ids(7855, 1464, 1465, 1466, 51, 64, 124, 16).build());

		// Ancient spellbook.

		// Arceuus spellbook.

		// Powered staves.
		projectiles.add(p().itemId(ItemID.TRIDENT_OF_THE_SEAS).ids(1167, 1251, 1252, 1253, 51, 64, 60, 16).build());
		projectiles.add(p().itemId(ItemID.TRIDENT_OF_THE_SWAMP).ids(1167, 665, 1040, 1042, 51, 64, 60, 16).build());
		projectiles.add(p().name("trident (purple and gold)").itemId(ItemID.GOLDEN_SCARAB).ids(1167, 1543, 1544, 1545, 51, 64, 60, 16).artificial().build());
		projectiles.add(p().name("trident (purple and silver)").itemId(ItemID.STONE_SCARAB).ids(1167, 1546, 1547, 1548, 51, 64, 60, 16).artificial().build());
		projectiles.add(p().name("Sanguinesti staff (regular)").itemId(ItemID.SANGUINESTI_STAFF).ids(1167, 1540, 1539, 1541, 51, 64, 60, 16).build());
		projectiles.add(p().name("Sanguinesti staff (health restore)").itemId(ItemID.SANGUINESTI_STAFF).ids(1167, 1540, 1539, 1542, 51, 64, 60, 16).build());
		projectiles.add(p().name("Holy sanguinesti staff (regular)").itemId(ItemID.HOLY_SANGUINESTI_STAFF).ids(1167, 1900, 1899, 1901, 51, 64, 60, 16).build());
		projectiles.add(p().name("Holy sanguinesti staff (health restore)").itemId(ItemID.HOLY_SANGUINESTI_STAFF).ids(1167, 1900, 1899, 1902, 51, 64, 60, 16).build());

		// Arrows. Many values guessed based off of iron arrow, so stuff like height/slope could be off for some arrows.
		projectiles.add(p().itemId(ItemID.BRONZE_ARROW).ids(426, 19, 10, -1, 41, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.IRON_ARROW).ids(426, 18, 9, -1, 41, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.STEEL_ARROW).ids(426, 20, 11, -1, 41, 11, 144, 15).build());
		projectiles.add(p().name("Black arrow").itemId(ItemID.HEADLESS_ARROW).ids(426, 23, 14, -1, 41, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.MITHRIL_ARROW).ids(426, 21, 12, -1, 41, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.ADAMANT_ARROW).ids(426, 22, 13, -1, 41, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.RUNE_ARROW).ids(426, 24, 15, -1, 41, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.DRAGON_ARROW).ids(426, 18, 9, -1, 41, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.ICE_ARROWS).ids(426, 25, 16, -1, 41, 11, 144, 15).build());
		projectiles.add(p().name("Fire arrow").itemId(ItemID.BRONZE_FIRE_ARROW_LIT).ids(426, 26, 17, -1, 41, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.TRAINING_ARROWS).ids(426, 806, 805, -1, 41, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.CRYSTAL_BOW).ids(426, 250, 249, -1, 41, 11, 144, 15).build());
		// TODO amethyst arrow, dragon arrow, ba arrows, ogre arrow, brutal arrow, broad arrow, poisoned arrows.
		// TODO fbow, specs (seercull, msb, magic longbow), dark bow.

		// Bolts.

		// Knives.

		// Darts.
		// TODO test blowpipe.

		// Thrownaxes.

		projectiles.add(p().name("Black chinchompa").itemId(ItemID.BLACK_CHINCHOMPA).ids(7618, -1, 1272, 157, 21, 11, 144, 15).build()); // only has hit gfx when in multicombat area.
		projectiles.add(p().name("Snowball").itemId(ItemID.SNOWBALL).ids(5063, 860, 861, 862, 62, 11, 44, 15).build());
		projectiles.add(p().name("Dragon crossbow spec").itemId(ItemID.DRAGON_CROSSBOW).ids(4230, -1, 698, 157, 41, 11, 144, 5).build());
		projectiles.add(p().name("Mithril knife").itemId(ItemID.MITHRIL_KNIFE).ids(7617, 223, 216, -1, 32, 11, 144, 15).build());
		projectiles.add(p().name("Mithril dart").itemId(ItemID.MITHRIL_DART).ids(7554, 235, 229, -1, 32, 11, 144, 15).build());
		projectiles.add(p().name("Dark bow spec (non-dragon arrows)").itemId(ItemID.DARK_BOW).ids(426, 1105, 1101, 1103, 41, 11, 144, 5).build());
		projectiles.add(p().name("Dark bow spec (dragon arrows)").itemId(ItemID.DARK_BOW).ids(426, 1111, 1099, 1100, 41, 11, 144, 5).build());
		projectiles.add(p().name("Seercull").itemId(ItemID.SEERCULL).ids(426, 472, 473, 474, 41, 11, 144, 15).build());

		// TODO
		projectiles.add(p().name("Corp sperm 1").sprite(SpriteID.SPELL_WIND_STRIKE).ids(1162, 90, 314, 92, 51, 64, 124, 16).build());
		projectiles.add(p().name("Corp sperm 2").sprite(SpriteID.SPELL_WIND_STRIKE).ids(1162, 90, 315, 92, 51, 64, 124, 16).build());
		projectiles.add(p().name("Corp sperm 3").sprite(SpriteID.SPELL_WIND_STRIKE).ids(1162, 90, 316, 92, 51, 64, 124, 16).build());
	}

	@Getter(AccessLevel.NONE)
	String name;
	int itemIdIcon;
	int spriteIdIcon;

	int castAnimation;
	int castGfx;
	int projectileId;
	int hitGfx;

	int startMovement;
	int startHeight;
	int endHeight;
	int slope;

	boolean artificial;

	public String getName(ItemManager itemManager) {
		return name != null ? name : itemManager.getItemComposition(itemIdIcon).getName();
	}

	private static class ProjectileCastBuilder {
		String name = null;
		int itemIdIcon = -1;
		int spriteIdIcon = -1;

		int castAnimation;
		int castGfx;
		int projectileId;
		int hitGfx;

		int startMovement;
		int startHeight;
		int endHeight;
		int slope;

		boolean artificial = false;

		public ProjectileCastBuilder ids(int castAnimation, int castGfx, int projectileId, int hitGfx, int startMovement, int startHeight, int endHeight, int slope) {
			this.castAnimation = castAnimation;
			this.castGfx = castGfx;
			this.projectileId = projectileId;
			this.hitGfx = hitGfx;
			this.startMovement = startMovement;
			this.startHeight = startHeight;
			this.endHeight = endHeight;
			this.slope = slope;
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
			return new ProjectileCast(name, itemIdIcon, spriteIdIcon, castAnimation, castGfx, projectileId, hitGfx, startMovement, startHeight, endHeight, slope, artificial);
		}
	}

	private static ProjectileCastBuilder p() {
		return new ProjectileCastBuilder();
	}
}
