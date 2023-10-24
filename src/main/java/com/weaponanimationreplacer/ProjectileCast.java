package com.weaponanimationreplacer;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.runelite.api.ItemID;
import net.runelite.api.SpriteID;
import net.runelite.client.game.ItemManager;

@AllArgsConstructor
@Data
public final class ProjectileCast
{
	/*
	Types of projectiles:
	hit gfx (most spells).
		the hit gfx is applied when the spell casts, but has a timer before it actually shows.
			This means if you're at a very long range it is possible for the hit gfx to never occur because it is replaced by the next spell cast before it goes off.
		many of these can splash animation instead.
	no hit gfx (ranged attacks mostly).
	no cast gfx or projectile - requires additional information to identify what spell was cast.
	no projectile spells (such as ice barrage) - when replacing, requires hit delay to be calculated.
	TODO multiple projectile spells (such as dark bow).
		Dark bow spec is actually 4 projectiles (with 2 different versions, one with dragon arrows one without).
	enchanted bolts.
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
		// TODO hit gfx heights.
		projectiles.add(p().name("Wind Strike").sprite(SpriteID.SPELL_WIND_STRIKE).cast(1162, 90).projectile(92, 51, 64, -172, 124, 16).hitGfx(91, 124).build());
		projectiles.add(p().name("Confuse").sprite(SpriteID.SPELL_CONFUSE).cast(1163, 102).projectile(104, 61, 64, -172, 124, 16).hitGfx(103, 124).build());
		projectiles.add(p().name("Water Strike").sprite(SpriteID.SPELL_WATER_STRIKE).cast(1162, 93).projectile(95, 51, 64, -172, 124, 16).hitGfx(94, 124).build());
		projectiles.add(p().name("Earth Strike").sprite(SpriteID.SPELL_EARTH_STRIKE).cast(1162, 96).projectile(98, 51, 64, -172, 124, 16).hitGfx(97, 124).build());
		projectiles.add(p().name("Weaken").sprite(SpriteID.SPELL_WEAKEN).cast(1164, 105).projectile(107, 44, 64, -172, 124, 16).hitGfx(106, 124).build());
		projectiles.add(p().name("Fire Strike").sprite(SpriteID.SPELL_FIRE_STRIKE).cast(1162, 99).projectile(101, 51, 64, -172, 124, 16).hitGfx(100, 124).build());
		projectiles.add(p().name("Wind Bolt").sprite(SpriteID.SPELL_WIND_BOLT).cast(1162, 117).projectile(119, 51, 64, -172, 124, 16).hitGfx(118, 124).build());
		projectiles.add(p().name("Curse").sprite(SpriteID.SPELL_CURSE).cast(1165, 108).projectile(110, 51, 64, -172, 124, 16).hitGfx(109, 124).build());
		projectiles.add(p().name("Bind").sprite(SpriteID.SPELL_BIND).cast(1161, 177).projectile(181, 75, 64, -172, 0, 16).hitGfx(178, 0).hitGfx(181, 124).build());
		projectiles.add(p().name("Water Bolt").sprite(SpriteID.SPELL_WATER_BOLT).cast(1162, 120).projectile(122, 51, 64, -172, 124, 16).hitGfx(121, 124).build());
		projectiles.add(p().name("Earth Bolt").sprite(SpriteID.SPELL_EARTH_BOLT).cast(1162, 123).projectile(125, 51, 64, -172, 124, 16).hitGfx(124, 124).build());
		projectiles.add(p().name("Telegrab").sprite(SpriteID.SPELL_TELEKINETIC_GRAB).cast(723, 142).projectile(144, 48, 64, -172, 0, 16).hitGfx(143, 0).build());
		projectiles.add(p().name("Fire Bolt").sprite(SpriteID.SPELL_FIRE_BOLT).cast(1162, 126).projectile(128, 51, 64, -172, 124, 16).hitGfx(127, 124).build());
		projectiles.add(p().name("Crumble Undead").sprite(SpriteID.SPELL_CRUMBLE_UNDEAD).cast(1166, 145).projectile(147, 46, 64, -172, 124, 16).hitGfx(146, 124).build());
		projectiles.add(p().name("Wind Blast").sprite(SpriteID.SPELL_WIND_BLAST).cast(1162, 132).projectile(134, 51, 64, -172, 124, 16).hitGfx(133, 124).build());
		projectiles.add(p().name("Water Blast").sprite(SpriteID.SPELL_WATER_BLAST).cast(1162, 135).projectile(137, 51, 64, -172, 124, 16).hitGfx(136, 124).build());
		projectiles.add(p().name("Iban Blast").sprite(SpriteID.SPELL_IBAN_BLAST).cast(708, 87).projectile(89, 60, 64, -172, 124, 16).hitGfx(88, 124).build());
		projectiles.add(p().name("Snare").sprite(SpriteID.SPELL_SNARE).cast(1161, 177).projectile(180, 75, 64, -172, 0, 16).hitGfx(178, 0).hitGfx(180, 124).build());
		projectiles.add(p().name("Magic Dart").sprite(SpriteID.SPELL_MAGIC_DART).cast(1576, -1).projectile(329, 51, 64, -172, 124, 16).hitGfx(328, 124).build());
		projectiles.add(p().name("Earth Blast").sprite(SpriteID.SPELL_EARTH_BLAST).cast(1162, 138).projectile(140, 51, 64, -172, 124, 16).hitGfx(139, 124).build());
		projectiles.add(p().name("Fire Blast").sprite(SpriteID.SPELL_FIRE_BLAST).cast(1162, 129).projectile(131, 51, 64, -172, 124, 16).hitGfx(130, 124).build());
		projectiles.add(p().name("Saradomin Strike").sprite(SpriteID.SPELL_SARADOMIN_STRIKE).ids(811, -1, 76).build());
		projectiles.add(p().name("Claws of Guthix").sprite(SpriteID.SPELL_CLAWS_OF_GUTHIX).ids(811, -1, 77).build());
		projectiles.add(p().name("Flames of Zamorak").sprite(SpriteID.SPELL_FLAMES_OF_ZAMORAK).ids(811, -1, 78).build());
		projectiles.add(p().name("Wind Wave").sprite(SpriteID.SPELL_WIND_WAVE).cast(1167, 158).projectile(160, 51, 64, -172, 124, 16).hitGfx(159, 124).build());
		projectiles.add(p().name("Water Wave").sprite(SpriteID.SPELL_WATER_WAVE).cast(1167, 161).projectile(163, 51, 64, -172, 124, 16).hitGfx(162, 124).build());
		projectiles.add(p().name("Vulnerability").sprite(SpriteID.SPELL_VULNERABILITY).cast(1165, 167).projectile(169, 34, 64, -172, 124, 16).hitGfx(168, 124).build());
		projectiles.add(p().name("Earth Wave").sprite(SpriteID.SPELL_EARTH_WAVE).cast(1167, 164).projectile(166, 51, 64, -172, 124, 16).hitGfx(165, 124).build());
		projectiles.add(p().name("Enfeeble").sprite(SpriteID.SPELL_ENFEEBLE).cast(1168, 170).projectile(172, 48, 64, -172, 124, 16).hitGfx(171, 124).build());
		projectiles.add(p().name("Fire Wave").sprite(SpriteID.SPELL_FIRE_WAVE).cast(1167, 155).projectile(157, 51, 64, -172, 124, 16).hitGfx(156, 124).build());
		projectiles.add(p().name("Entangle").sprite(SpriteID.SPELL_ENTANGLE).cast(1161, 177).projectile(179, 75, 64, -172, 0, 16).hitGfx(178, 0).hitGfx(179, 124).build());
		projectiles.add(p().name("Stun").sprite(SpriteID.SPELL_STUN).cast(1169, 173).projectile(80, 52, 64, -172, 124, 16).hitGfx(174, 124).build());
		projectiles.add(p().name("Wind Surge").sprite(SpriteID.SPELL_WIND_SURGE).cast(7855, 1455).projectile(1457, 51, 64, -172, 124, 16).hitGfx(1456, 124).build());
		projectiles.add(p().name("Water Surge").sprite(SpriteID.SPELL_WATER_SURGE).cast(7855, 1458).projectile(1460, 51, 64, -172, 124, 16).hitGfx(1459, 124).build());
		projectiles.add(p().name("Earth Surge").sprite(SpriteID.SPELL_EARTH_SURGE).cast(7855, 1461).projectile(1463, 51, 64, -172, 124, 16).hitGfx(1462, 124).build());
		projectiles.add(p().name("Fire Surge").sprite(SpriteID.SPELL_FIRE_SURGE).cast(7855, 1464).projectile(1466, 51, 64, -172, 124, 16).hitGfx(1465, 124).build());

		// Ancient spellbook.
		projectiles.add(p().name("Smoke Rush").sprite(SpriteID.SPELL_SMOKE_RUSH).ids(1978, -1, 384, 385, 51, 64, 124, 16).build());
		projectiles.add(p().name("Shadow Rush").sprite(SpriteID.SPELL_SHADOW_RUSH).ids(1978, -1, 378, 379, 51, 64, 0, 16).build());
		projectiles.add(p().name("Blood Rush").sprite(SpriteID.SPELL_BLOOD_RUSH).ids(1978, -1, 373).build());
		projectiles.add(p().name("Ice Rush").sprite(SpriteID.SPELL_ICE_RUSH).ids(1978, -1, 360, 361, 51, 64, 0, 16).build());
		projectiles.add(p().name("Smoke Burst").sprite(SpriteID.SPELL_SMOKE_BURST).ids(1979, -1, 389).build());
		projectiles.add(p().name("Shadow Burst").sprite(SpriteID.SPELL_SHADOW_BURST).ids(1979, -1, 382).build());
		projectiles.add(p().name("Blood Burst").sprite(SpriteID.SPELL_BLOOD_BURST).ids(1979, -1, 376).build());
		projectiles.add(p().name("Ice Burst").sprite(SpriteID.SPELL_ICE_BURST).ids(1979, -1, 363).build());
		projectiles.add(p().name("Smoke Blitz").sprite(SpriteID.SPELL_SMOKE_BLITZ).ids(1978, -1, 386, 387, 51, 64, 124, 16).build());
		projectiles.add(p().name("Shadow Blitz").sprite(SpriteID.SPELL_SHADOW_BLITZ).ids(1978, -1, 380, 381, 51, 64, 0, 16).build());
		projectiles.add(p().name("Blood Blitz").sprite(SpriteID.SPELL_BLOOD_BLITZ).ids(1978, -1, 374, 375, 51, 64, 0, 16).build());
		projectiles.add(p().name("Ice Blitz").sprite(SpriteID.SPELL_ICE_BLITZ).ids(1978, 366, 367).build());
		projectiles.add(p().name("Smoke Barrage").sprite(SpriteID.SPELL_SMOKE_BARRAGE).ids(1979, -1, 391).build());
		projectiles.add(p().name("Shadow Barrage").sprite(SpriteID.SPELL_SHADOW_BARRAGE).ids(1979, -1, 383).build());
		projectiles.add(p().name("Blood Barrage").sprite(SpriteID.SPELL_BLOOD_BARRAGE).ids(1979, -1, 377).build());
		projectiles.add(p().name("Ice Barrage").sprite(SpriteID.SPELL_ICE_BARRAGE).ids(1979, -1, 369).build());

		// Arceuus spellbook.
		projectiles.add(p().name("Ghostly Grasp").sprite(SpriteID.SPELL_GHOSTLY_GRASP).ids(8972, 1856, 1858).build());
		projectiles.add(p().name("Skeletal Grasp").sprite(SpriteID.SPELL_SKELETAL_GRASP).ids(8972, 1859, 1861).build());
		projectiles.add(p().name("Undead Grasp").sprite(SpriteID.SPELL_UNDEAD_GRASP).ids(8972, 1862, 1863).build());
		projectiles.add(p().name("Inferior Demonbane").sprite(SpriteID.SPELL_INFERIOR_DEMONBANE).ids(8977, 1865, 1866).build());
		projectiles.add(p().name("Superior Demonbane").sprite(SpriteID.SPELL_SUPERIOR_DEMONBANE).ids(8977, 1867, 1868).build());
		projectiles.add(p().name("Dark Demonbane").sprite(SpriteID.SPELL_DARK_DEMONBANE).ids(8977, 1869, 1870).build());
		projectiles.add(p().name("Dark Lure").sprite(SpriteID.SPELL_DARK_LURE).ids(8974, 1882, 1884).build());

		// Powered staves.
		// TODO black trident. I forget the ID.
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
		projectiles.add(p().itemId(ItemID.AMETHYST_ARROW).ids(426, 1385, 1384, -1, 41, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.DRAGON_ARROW).ids(426, 1116, 1120, -1, 41, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.ICE_ARROWS).ids(426, 25, 16, -1, 41, 11, 144, 15).build());
		projectiles.add(p().name("Fire arrow").itemId(ItemID.BRONZE_FIRE_ARROW_LIT).ids(426, 26, 17, -1, 41, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.TRAINING_ARROWS).ids(426, 806, 805, -1, 41, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.CRYSTAL_BOW).ids(426, 250, 249, -1, 41, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.OGRE_ARROW).ids(426, 243, 242, -1, 41, 11, 144, 15).build());
		// TODO ba arrows, brutal arrow, broad arrow.
		// TODO specs (seercull, msb, magic longbow), dark bow.

		// bow of faerdhinen bofa
		projectiles.add(p().itemId(ItemID.BOW_OF_FAERDHINEN).ids(426, 1889, 1888, -1, 41, 11, 144, 15).build());
		projectiles.add(p().name("Bow of faerdhinen (red)").itemId(ItemID.BOW_OF_FAERDHINEN_C_25884).ids(426, 1923, 1922, -1, 41, 11, 144, 15).build());
		projectiles.add(p().name("Bow of faerdhinen (white)").itemId(ItemID.BOW_OF_FAERDHINEN_C_25886).ids(426, 1925, 1924, -1, 41, 11, 144, 15).build());
		projectiles.add(p().name("Bow of faerdhinen (black)").itemId(ItemID.BOW_OF_FAERDHINEN_C_25888).ids(426, 1927, 1926, -1, 41, 11, 144, 15).build());
		projectiles.add(p().name("Bow of faerdhinen (purple)").itemId(ItemID.BOW_OF_FAERDHINEN_C_25890).ids(426, 1929, 1928, -1, 41, 11, 144, 15).build());
		projectiles.add(p().name("Bow of faerdhinen (green)").itemId(ItemID.BOW_OF_FAERDHINEN_C_25892).ids(426, 1931, 1930, -1, 41, 11, 144, 15).build());
		projectiles.add(p().name("Bow of faerdhinen (yellow)").itemId(ItemID.BOW_OF_FAERDHINEN_C_25894).ids(426, 1933, 1932, -1, 41, 11, 144, 15).build());
		projectiles.add(p().name("Bow of faerdhinen (blue)").itemId(ItemID.BOW_OF_FAERDHINEN_C_25896).ids(426, 1935, 1934, -1, 41, 11, 144, 15).build());

		// Bolts.
		projectiles.add(p().name("Bolts").itemId(ItemID.RUNITE_BOLTS).ids(7552, -1, 27, -1, 41, 11, 144, 5).build());
		// TODO bolt effects.
		// diamond (e) 9168, -1, 27, 758, 41, 11, 144, 5, 0
		// ruby (e) 9168, -1, 27, 754, 41, 11, 144, 5, 0
		// TODO it would be neat if different bolt types could have different projectiles.

		// Knives.
		projectiles.add(p().itemId(ItemID.BRONZE_KNIFE).ids(7617, 219, 212, -1, 32, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.IRON_KNIFE).ids(7617, 220, 213, -1, 32, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.STEEL_KNIFE).ids(7617, 221, 214, -1, 32, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.BLACK_KNIFE).ids(7617, 222, 215, -1, 32, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.MITHRIL_KNIFE).ids(7617, 223, 216, -1, 32, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.ADAMANT_KNIFE).ids(7617, 224, 217, -1, 32, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.RUNE_KNIFE).ids(7617, 225, 218, -1, 32, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.DRAGON_KNIFE).ids(8194, -1, 28, -1, 32, 11, 144, 15).build());
		projectiles.add(p().name("Dragon knife (spec)").itemId(ItemID.DRAGON_KNIFE).ids(8291, -1, 699, -1, 25, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.DRAGON_KNIFEP_22808).ids(8195, -1, 697, -1, 32, 11, 144, 15).build());
		projectiles.add(p().name("Dragon knife (p++) (spec)").itemId(ItemID.DRAGON_KNIFEP_22808).ids(8292, -1, 1629, -1, 25, 11, 144, 15).build());

		// Darts.
		projectiles.add(p().itemId(ItemID.BRONZE_DART).ids(7554, -1, 226, -1, 32, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.IRON_DART).ids(7554, -1, 227, -1, 32, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.STEEL_DART).ids(7554, -1, 228, -1, 32, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.BLACK_DART).ids(7554, -1, 32, -1, 32, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.MITHRIL_DART).ids(7554, -1, 229, -1, 32, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.ADAMANT_DART).ids(7554, -1, 230, -1, 32, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.RUNE_DART).ids(7554, -1, 231, -1, 32, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.AMETHYST_DART).ids(7554, -1, 1936, -1, 32, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.DRAGON_DART).ids(7554, -1, 1122, -1, 32, 11, 144, 15).build());

		// Blowpipe.
		projectiles.add(p().name("Bronze Dart").itemId(ItemID.TOXIC_BLOWPIPE).ids(5061, -1, 226, -1, 32, 105, 144, 15).build());
		projectiles.add(p().name("Iron Dart").itemId(ItemID.TOXIC_BLOWPIPE).ids(5061, -1, 227, -1,  32, 105, 144, 15).build());
		projectiles.add(p().name("Steel Dart").itemId(ItemID.TOXIC_BLOWPIPE).ids(5061, -1, 228, -1, 32, 105, 144, 15).build());
		projectiles.add(p().name("Black Dart").itemId(ItemID.TOXIC_BLOWPIPE).ids(5061, -1, 32, -1, 32, 105, 144, 15).build());
		projectiles.add(p().name("Mithril Dart").itemId(ItemID.TOXIC_BLOWPIPE).ids(5061, -1, 229, -1, 32, 105, 144, 15).build());
		projectiles.add(p().name("Adamant Dart").itemId(ItemID.TOXIC_BLOWPIPE).ids(5061, -1, 230, -1, 32, 105, 144, 15).build());
		projectiles.add(p().name("Rune Dart").itemId(ItemID.TOXIC_BLOWPIPE).ids(5061, -1, 231, -1, 32, 105, 144, 15).build());
		projectiles.add(p().name("Amethyst Dart").itemId(ItemID.TOXIC_BLOWPIPE).ids(5061, -1, 1936, -1, 32, 105, 144, 15).build());
		projectiles.add(p().name("Dragon Dart").itemId(ItemID.TOXIC_BLOWPIPE).ids(5061, -1, 1122, -1, 32, 105, 144, 15).build());

		// Thrownaxes.
		projectiles.add(p().itemId(ItemID.BRONZE_THROWNAXE).ids(7617, 43, 36, -1, 32, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.IRON_THROWNAXE).ids(7617, 42, 35, -1, 32, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.STEEL_THROWNAXE).ids(7617, 44, 37, -1, 32, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.MITHRIL_THROWNAXE).ids(7617, 45, 38, -1, 32, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.ADAMANT_THROWNAXE).ids(7617, 46, 39, -1, 32, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.RUNE_THROWNAXE).ids(7617, 48, 41, -1, 32, 11, 144, 15).build());
		projectiles.add(p().name("Rune thrownaxe (spec)").itemId(ItemID.RUNE_THROWNAXE).ids(1068, 257, 258, -1, 41, 11, 144, 0).build());
		projectiles.add(p().itemId(ItemID.DRAGON_THROWNAXE).ids(7617, 1320, 1319, -1, 32, 11, 144, 15).build());
		projectiles.add(p().name("Dragon thrownaxe (spec)").itemId(ItemID.DRAGON_THROWNAXE).ids(7521, 1317, 1318, -1, 25, 11, 144, 15).build());

		// javelins.
		projectiles.add(p().itemId(ItemID.BRONZE_JAVELIN).ids(7555, -1, 200, -1, 32, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.IRON_JAVELIN).ids(7555, -1, 201, -1, 32, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.STEEL_JAVELIN).ids(7555, -1, 202, -1, 32, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.MITHRIL_JAVELIN).ids(7555, -1, 203, -1, 32, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.ADAMANT_JAVELIN).ids(7555, -1, 204, -1, 32, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.RUNE_JAVELIN).ids(7555, -1, 205, -1, 32, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.AMETHYST_JAVELIN).ids(7555, -1, 1386, -1, 32, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.DRAGON_JAVELIN).ids(7555, -1, 1301, -1, 32, 11, 144, 15).build());

		projectiles.add(p().itemId(ItemID.BLACK_CHINCHOMPA).ids(7618, -1, 1272, 157, 21, 11, 144, 15).build()); // only has hit gfx when in multicombat area.
		projectiles.add(p().itemId(ItemID.RED_CHINCHOMPA).ids(7618, -1, 909, 157, 21, 11, 144, 15).hitGfx(157, 0).build()); // only has hit gfx when in multicombat area.
		projectiles.add(p().itemId(ItemID.CHINCHOMPA).ids(7618, -1, 908, 157, 21, 11, 144, 15).hitGfx(157, 0).build()); // only has hit gfx when in multicombat area.

		projectiles.add(p().itemId(ItemID.TOKTZXILUL).ids(7558, -1, 442, -1, 32, 11, 144, 15).build());

//		projectiles.add(p().itemId(ItemID.GRAAHK_HEADDRESS).ids(7618, -1, 941, 157, 21, 11, 144, 15, 0).build()); // only has hit gfx when in multicombat area.

		projectiles.add(p().name("Snowball").itemId(ItemID.SNOWBALL).ids(5063, 860, 861, 862, 62, 11, 44, 15).build());
		projectiles.add(p().name("Dragon crossbow spec").itemId(ItemID.DRAGON_CROSSBOW).ids(4230, -1, 698, 157, 41, 11, 144, 5).build());
		projectiles.add(p().name("Dark bow spec (non-dragon arrows)").itemId(ItemID.DARK_BOW).ids(426, 1105, 1101, 1103, 41, 11, 144, 5).build());
		projectiles.add(p().name("Dark bow spec (dragon arrows)").itemId(ItemID.DARK_BOW).ids(426, 1111, 1099, 1100, 41, 11, 144, 5).build());
		projectiles.add(p().name("Seercull").itemId(ItemID.SEERCULL).ids(426, 472, 473, 474, 41, 11, 144, 15).hitGfx(474, 0).build());

		// TODO
		projectiles.add(p().name("Corp sperm 1").sprite(SpriteID.SPELL_WIND_STRIKE).ids(1162, 90, 314, 92, 51, 64, 124, 16).artificial().build());
		projectiles.add(p().name("Corp sperm 2").sprite(SpriteID.SPELL_WIND_STRIKE).ids(1162, 90, 315, 92, 51, 64, 124, 16).artificial().build());
		projectiles.add(p().name("Corp sperm 3").sprite(SpriteID.SPELL_WIND_STRIKE).ids(1162, 90, 316, 92, 51, 64, 124, 16).artificial().build());

		projectiles.add(p().name("Rotten tomato").itemId(ItemID.ROTTEN_TOMATO).ids(5063, 30, 29, 31, 62, 11, 44, 15).build());
		projectiles.add(p().name("Rock").itemId(ItemID.PET_ROCK).ids(5063, 33, 32, -1, 62, 11, 44, 15).build());
		projectiles.add(p().itemId(ItemID.VIAL).ids(7617, 50, 49, -1, 32, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.ENCHANTED_VIAL).ids(7617, 52, 51, -1, 32, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.HOLY_WATER).ids(7617, 193, 192, -1, 32, 11, 144, 15).build());
		projectiles.add(p().itemId(ItemID.NINJA_IMPLING_JAR).ids(7617, 210, 211, 209, 32, 11, 144, 15).hitGfx(209, 0).build());
		projectiles.add(p().name("Dragon breath (large)").sprite(SpriteID.SPELL_FIRE_SURGE).ids(7855, 1464, 54, 1466, 51, 64, 124, 16).build());
		projectiles.add(p().name("Dark Strike").sprite(SpriteID.SPELL_WIND_STRIKE).ids(1162, 194, 195, 196, 51, 64, 124, 16).build());
		projectiles.add(p().name("Tempoross harpoonfish").itemId(ItemID.HARPOONFISH).ids(426, 18, 1837, 3, 41, 11, 144, 15).hitGfx(3, 0).build());

		projectiles.add(p().itemId(ItemID.TUMEKENS_SHADOW).ids(9493, 2125, 2126, 2127, 56, 40, 124, 32).build());
	}

	@Getter(AccessLevel.NONE)
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
		return new ProjectileCast(toCopy.name, toCopy.itemIdIcon, toCopy.spriteIdIcon, toCopy.castAnimation, toCopy.castGfx, toCopy.projectileId, toCopy.hitGfx, toCopy.hitGfxHeight, toCopy.startMovement, toCopy.startHeight, toCopy.height, toCopy.endHeight, toCopy.slope, true);
	}

	public String getName(ItemManager itemManager) {
		return name != null ? name : itemManager.getItemComposition(itemIdIcon).getName() + itemIdIcon;
	}

	public static class ProjectileCastBuilder {
		String name = null;
		int itemIdIcon = -1;
		int spriteIdIcon = -1;

		int castAnimation = -1;
		int castGfx = -1;
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

		public ProjectileCastBuilder projectile(int projectileId, int startMovement, int startHeight, int height, int endHeight, int slope) {
			this.projectileId = projectileId;
			this.hitGfxHeight = endHeight; // Good enough for most spells, only a few have a different value here.
			this.startMovement = startMovement;
			this.startHeight = startHeight;
			this.endHeight = endHeight;
			this.slope = slope;
			return this;
		}

		// TODO ideally remove this once you have the right values.
		// reasonable default.
		public ProjectileCastBuilder ids(int castAnimation, int castGfx, int projectileId, int hitGfx, int startMovement, int startHeight, int endHeight, int slope) {
			return ids(castAnimation, castGfx, projectileId, hitGfx, startMovement, startHeight, -172, endHeight, slope);
		}

		public ProjectileCastBuilder ids(int castAnimation, int castGfx, int projectileId, int hitGfx, int startMovement, int startHeight, int height, int endHeight, int slope) {
			this.castAnimation = castAnimation;
			this.castGfx = castGfx;
			this.projectileId = projectileId;
			this.hitGfx = hitGfx;
			this.hitGfxHeight = endHeight; // Good enough for most spells, only a few have a different value here.
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
			return new ProjectileCast(name, itemIdIcon, spriteIdIcon, castAnimation, castGfx, projectileId, hitGfx, hitGfxHeight, startMovement, startHeight, height, endHeight, slope, artificial);
		}
	}

	public static ProjectileCastBuilder p() {
		return new ProjectileCastBuilder();
	}
}
