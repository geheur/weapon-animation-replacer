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
	public static void initializeSpells() {
		projectiles.clear();
		// Standard spellbook.
		projectiles.add(new ProjectileCast("Wind Strike", -1, SpriteID.SPELL_WIND_STRIKE, 1162, 90, 91, 92, 51, 64, 124, 16));
		projectiles.add(new ProjectileCast("Confuse", -1, SpriteID.SPELL_CONFUSE, 1163, 102, 103, 104, 61, 64, 124, 16));
		projectiles.add(new ProjectileCast("Water Strike", -1, SpriteID.SPELL_WATER_STRIKE, 1162, 93, 94, 95, 51, 64, 124, 16));
		projectiles.add(new ProjectileCast("Earth Strike", -1, SpriteID.SPELL_EARTH_STRIKE, 1162, 96, 97, 98, 51, 64, 124, 16));
		projectiles.add(new ProjectileCast("Weaken", -1, SpriteID.SPELL_WEAKEN, 1164, 105, 106, 107, 44, 64, 124, 16));
		projectiles.add(new ProjectileCast("Fire Strike", -1, SpriteID.SPELL_FIRE_STRIKE, 1162, 99, 100, 101, 51, 64, 124, 16));
		projectiles.add(new ProjectileCast("Wind Bolt", -1, SpriteID.SPELL_WIND_BOLT, 1162, 117, 118, 119, 51, 64, 124, 16));
		projectiles.add(new ProjectileCast("Curse", -1, SpriteID.SPELL_CURSE, 1165, 108, 109, 110, 51, 64, 124, 16));
		projectiles.add(new ProjectileCast("Bind", -1, SpriteID.SPELL_BIND, 1161, 177, 178, 181, 75, 64, 0, 16));
		projectiles.add(new ProjectileCast("Water Bolt", -1, SpriteID.SPELL_WATER_BOLT, 1162, 120, 121, 122, 51, 64, 124, 16));
		projectiles.add(new ProjectileCast("Earth Bolt", -1, SpriteID.SPELL_EARTH_BOLT, 1162, 123, 124, 125, 51, 64, 124, 16));
		projectiles.add(new ProjectileCast("Fire Bolt", -1, SpriteID.SPELL_FIRE_BOLT, 1162, 126, 127, 128, 51, 64, 124, 16));
		projectiles.add(new ProjectileCast("Crumble Undead", -1, SpriteID.SPELL_CRUMBLE_UNDEAD, 1166, 145, 146, 147, 46, 64, 124, 16));
		projectiles.add(new ProjectileCast("Wind Blast", -1, SpriteID.SPELL_WIND_BLAST, 1162, 132, 133, 134, 51, 64, 124, 16));
		projectiles.add(new ProjectileCast("Water Blast", -1, SpriteID.SPELL_WATER_BLAST, 1162, 135, 136, 137, 51, 64, 124, 16));
		projectiles.add(new ProjectileCast("Snare", -1, SpriteID.SPELL_SNARE, 1161, 177, 178, 180, 75, 64, 0, 16));
		projectiles.add(new ProjectileCast("Earth Blast", -1, SpriteID.SPELL_EARTH_BLAST, 1162, 138, 139, 140, 51, 64, 124, 16));
		projectiles.add(new ProjectileCast("Fire Blast", -1, SpriteID.SPELL_FIRE_BLAST, 1162, 129, 130, 131, 51, 64, 124, 16));
		projectiles.add(new ProjectileCast("Wind Wave", -1, SpriteID.SPELL_WIND_WAVE, 1167, 158, 159, 160, 51, 64, 124, 16));
		projectiles.add(new ProjectileCast("Water Wave", -1, SpriteID.SPELL_WATER_WAVE, 1167, 161, 162, 163, 51, 64, 124, 16));
		projectiles.add(new ProjectileCast("Vulnerability", -1, SpriteID.SPELL_VULNERABILITY, 1165, 167, 168, 169, 34, 64, 124, 16));
		projectiles.add(new ProjectileCast("Earth Wave", -1, SpriteID.SPELL_EARTH_WAVE, 1167, 164, 165, 166, 51, 64, 124, 16));
		projectiles.add(new ProjectileCast("Enfeeble", -1, SpriteID.SPELL_ENFEEBLE, 1168, 170, 171, 172, 48, 64, 124, 16));
		projectiles.add(new ProjectileCast("Fire Wave", -1, SpriteID.SPELL_FIRE_WAVE, 1167, 155, 156, 157, 51, 64, 124, 16));
		projectiles.add(new ProjectileCast("Entangle", -1, SpriteID.SPELL_ENTANGLE, 1161, 177, 178, 179, 75, 64, 0, 16));
		projectiles.add(new ProjectileCast("Stun", -1, SpriteID.SPELL_STUN, 1169, 173, 174, 80, 52, 64, 124, 16));
		projectiles.add(new ProjectileCast("Wind Surge", -1, SpriteID.SPELL_WIND_SURGE, 7855, 1455, 1456, 1457, 51, 64, 124, 16));
		projectiles.add(new ProjectileCast("Water Surge", -1, SpriteID.SPELL_WATER_SURGE, 7855, 1458, 1459, 1460, 51, 64, 124, 16));
		projectiles.add(new ProjectileCast("Earth Surge", -1, SpriteID.SPELL_EARTH_SURGE, 7855, 1461, 1462, 1463, 51, 64, 124, 16));
		projectiles.add(new ProjectileCast("Fire Surge", -1, SpriteID.SPELL_FIRE_SURGE, 7855, 1464, 1465, 1466, 51, 64, 124, 16));

		// Ancient spellbook.

		// Arceuus spellbook.

		// Powered staves.
		projectiles.add(new ProjectileCast(null, ItemID.TRIDENT_OF_THE_SEAS, -1, 1167, 1251, 1252, 1253, 51, 64, 60, 16));
		projectiles.add(new ProjectileCast(null, ItemID.TRIDENT_OF_THE_SWAMP, -1, 1167, 665, 1040, 1042, 51, 64, 60, 16));
		projectiles.add(new ProjectileCast("trident (purple and gold)", ItemID.GOLDEN_SCARAB, -1, 1167, 1543, 1544, 1545, 51, 64, 60, 16)); // artificial.
		projectiles.add(new ProjectileCast("trident (purple and silver)", ItemID.STONE_SCARAB, -1, 1167, 1546, 1547, 1548, 51, 64, 60, 16)); // artificial.
		projectiles.add(new ProjectileCast("Sanguinesti staff (regular)", ItemID.SANGUINESTI_STAFF, -1, 1167, 1540, 1539, 1541, 51, 64, 60, 16));
		projectiles.add(new ProjectileCast("Sanguinesti staff (health restore)", ItemID.SANGUINESTI_STAFF, -1, 1167, 1540, 1539, 1542, 51, 64, 60, 16));
		projectiles.add(new ProjectileCast("Holy sanguinesti staff (regular)", ItemID.HOLY_SANGUINESTI_STAFF, -1, 1167, 1900, 1899, 1901, 51, 64, 60, 16));
		projectiles.add(new ProjectileCast("Holy sanguinesti staff (health restore)", ItemID.HOLY_SANGUINESTI_STAFF, -1, 1167, 1900, 1899, 1902, 51, 64, 60, 16));

		// Arrows. Many values guessed based off of iron arrow, so stuff like height/slope could be off for some arrows.
		projectiles.add(new ProjectileCast(null, ItemID.BRONZE_ARROW, -1, 426, 19, 10, -1, 41, 11, 144, 15));
		projectiles.add(new ProjectileCast(null, ItemID.IRON_ARROW, -1, 426, 18, 9, -1, 41, 11, 144, 15));
		projectiles.add(new ProjectileCast(null, ItemID.STEEL_ARROW, -1, 426, 20, 11, -1, 41, 11, 144, 15));
		projectiles.add(new ProjectileCast("Black arrow", ItemID.HEADLESS_ARROW, -1, 426, 23, 14, -1, 41, 11, 144, 15));
		projectiles.add(new ProjectileCast(null, ItemID.MITHRIL_ARROW, -1, 426, 21, 12, -1, 41, 11, 144, 15));
		projectiles.add(new ProjectileCast(null, ItemID.ADAMANT_ARROW, -1, 426, 22, 13, -1, 41, 11, 144, 15));
		projectiles.add(new ProjectileCast(null, ItemID.RUNE_ARROW, -1, 426, 24, 15, -1, 41, 11, 144, 15));
		projectiles.add(new ProjectileCast(null, ItemID.DRAGON_ARROW, -1, 426, 18, 9, -1, 41, 11, 144, 15));
		projectiles.add(new ProjectileCast(null, ItemID.ICE_ARROWS, -1, 426, 25, 16, -1, 41, 11, 144, 15));
		projectiles.add(new ProjectileCast("Fire arrow", ItemID.BRONZE_FIRE_ARROW_LIT, -1, 426, 26, 17, -1, 41, 11, 144, 15));
		projectiles.add(new ProjectileCast(null, ItemID.TRAINING_ARROWS, -1, 426, 806, 805, -1, 41, 11, 144, 15));
		projectiles.add(new ProjectileCast(null, ItemID.CRYSTAL_BOW, -1, 426, 250, 249, -1, 41, 11, 144, 15));
		// TODO amethyst arrow, dragon arrow, ba arrows, ogre arrow, brutal arrow, broad arrow, poisoned arrows.
		// TODO fbow, specs (seercull, msb, magic longbow), dark bow.

		// Bolts.

		// Knives.

		// Darts.
		// TODO test blowpipe.

		// Thrownaxes.

		projectiles.add(new ProjectileCast("Black chinchompa", ItemID.BLACK_CHINCHOMPA, -1, 7618, -1, 1272, 157, 21, 11, 144, 15)); // only has hit gfx when in multicombat area.
		projectiles.add(new ProjectileCast("Snowball", ItemID.SNOWBALL, -1, 5063, 860, 861, 862, 62, 11, 44, 15));
		projectiles.add(new ProjectileCast("Dragon crossbow spec", ItemID.DRAGON_CROSSBOW, -1, 4230, -1, 698, 157, 41, 11, 144, 5));
		projectiles.add(new ProjectileCast("Mithril knife", ItemID.MITHRIL_KNIFE, -1, 7617, 223, 216, -1, 32, 11, 144, 15));
		projectiles.add(new ProjectileCast("Mithril dart", ItemID.MITHRIL_DART, -1, 7554, 235, 229, -1, 32, 11, 144, 15));
		projectiles.add(new ProjectileCast("Dark bow spec (non-dragon arrows)", ItemID.DARK_BOW, -1, 426, 1105, 1101, 1103, 41, 11, 144, 5));
		projectiles.add(new ProjectileCast("Dark bow spec (dragon arrows)", ItemID.DARK_BOW, -1, 426, 1111, 1099, 1100, 41, 11, 144, 5));
		projectiles.add(new ProjectileCast("Seercull", ItemID.SEERCULL, -1, 426, 472, 473, 474, 41, 11, 144, 15));

		// TODO
		projectiles.add(new ProjectileCast("Corp sperm 1", -1, SpriteID.SPELL_WIND_STRIKE, 1162, 90, 314, 92, 51, 64, 124, 16));
		projectiles.add(new ProjectileCast("Corp sperm 2", -1, SpriteID.SPELL_WIND_STRIKE, 1162, 90, 315, 92, 51, 64, 124, 16));
		projectiles.add(new ProjectileCast("Corp sperm 3", -1, SpriteID.SPELL_WIND_STRIKE, 1162, 90, 316, 92, 51, 64, 124, 16));

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

	public String getName(ItemManager itemManager) {
		return name != null ? name : itemManager.getItemComposition(itemIdIcon).getName();
	}
}
