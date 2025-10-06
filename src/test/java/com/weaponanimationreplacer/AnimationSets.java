package com.weaponanimationreplacer;

import static com.weaponanimationreplacer.Swap.AnimationType.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimationSets
{
	static final Map<Integer, String> descriptions = new HashMap<>();
	private static final List<AnimationSet> animationSets = new ArrayList<>();

	static List<AnimationSet> getAnimationSets()
	{
		animationSets.clear();

		new AnimationSetBuilder("Scythe of Vitur")
			.poseAnims(8057, 823, 819, 820, 821, 822, 824)
			.put(ATTACK_SLASH, 8056)
			.put(ATTACK_CRUSH, 8056)
			.build();
		new AnimationSetBuilder("Nightmare Staff")
			.poseAnims(4504, 1209, 1205, 1206, 1207, 1208, 1210)
			.put(ATTACK_CRUSH, 4505)
			.put(DEFEND, 420)
			.build();
		new AnimationSetBuilder("Inquisitor's mace")
			.poseAnims(808, 823, 819, 820, 821, 822, 824)
			.put(ATTACK_CRUSH, 4503)
			.put(DEFEND, 403)
			.build();
		new AnimationSetBuilder("Dinh's bulwhark")
			.poseAnims(7508, 823, 7510, 7510, 7510, 7510, 7509)
			.put(ATTACK_CRUSH, 7511)
			.put(ATTACK_SPEC, 7512)
			.put(DEFEND, 7517)
			.build();
		new AnimationSetBuilder("Dragon hunter lance")
			.poseAnims(813, 1209, 1205, 1206, 1207, 1208, 2563)
			.put(ATTACK_SLASH, 8289)
			.put(ATTACK_CRUSH, 8290)
			.put(ATTACK_STAB, 8288)
			.build();
		new AnimationSetBuilder("Flying Carpet")
			.poseAnims(6936, 6936, 6936, 6936, 6936, 6936, 6936)
			.doNotReplace()
			.build();
		new AnimationSetBuilder("Levitate")
			.poseAnims(8070, 8070, 8070, 8070, 8070, 8070, 8070)
			.doNotReplace()
			.build();
		new AnimationSetBuilder("Clueless Scroll")
			.poseAnims(7271, 823, 7272, 820, 821, 822, 7273)
			.put(ATTACK_CRUSH, 406)
			.build();
		new AnimationSetBuilder("Godsword (Saradomin)")
			.poseAnims(7053, 7044, 7052, 7052, 7048, 7047, 7043)
			.put(ATTACK_SLASH, 7045)
			.put(ATTACK_CRUSH, 7054)
			.put(ATTACK_SLASH2, 7055)
			.put(ATTACK_SPEC, 7640)
			.put(DEFEND, 7056)
			.build();
		new AnimationSetBuilder("Godsword (Bandos)")
			.copy("Godsword (Saradomin)")
			.put(ATTACK_SPEC, 7642)
			.build();
		new AnimationSetBuilder("Godsword (Armadyl)")
			.copy("Godsword (Saradomin)")
			.put(ATTACK_SPEC, 7644)
			.build();
		new AnimationSetBuilder("Godsword (Zamorak)")
			.copy("Godsword (Saradomin)")
			.put(ATTACK_SPEC, 7638)
			.build();
		new AnimationSetBuilder("Godsword (Ancient)")
			.copy("Godsword (Saradomin)")
			.put(ATTACK_SPEC, 9171)
			.build();
		new AnimationSetBuilder("Godsword (Ancient, alternative spec)")
			.copy("Godsword (Saradomin)")
			.put(ATTACK_SPEC, 9173)
			.build();
		new AnimationSetBuilder("Saradomin sword") // all guesses
			.copy("Godsword (Saradomin)")
			.put(ATTACK_SPEC, 1132)
			.build();
		new AnimationSetBuilder("Boxing gloves")
			.poseAnims(3677, 823, 3680, 3680, 3680, 3680, 824)
			.put(ATTACK_SLASH, 3678)
			.put(ATTACK_STAB, 3678)
			.put(DEFEND, 3679)
			.build();
		new AnimationSetBuilder("Hand fan")
			.poseAnims(6297, 6297, 7629, 7630, 7631, 7632, 7633)
			.put(ATTACK_CRUSH, 401)
			.put(DEFEND, 424)
			.build();
		new AnimationSetBuilder("Prop sword/candy cane")
			.poseAnims(2911, 823, 819, 820, 821, 822, 824)
			.put(ATTACK_CRUSH, 7328)
			.put(DEFEND, 378)
			.build();
		new AnimationSetBuilder("Staff")
			.poseAnims(813, 1209, 1205, 1206, 1207, 1208, 1210)
			.put(ATTACK_CRUSH, 419) // without shield. 393 is with shield.
			.put(DEFEND, 420)
			.build();
		new AnimationSetBuilder("Staff2/Wand")
			.poseAnims(813, 1209, 1205, 1206, 1207, 1208, 1210)
			.put(ATTACK_CRUSH, 414) // without shield. 393 is with shield.
			.put(DEFEND, 415)
			.build();
		new AnimationSetBuilder("Zamorakian hasta")
			.poseAnims(813, 1209, 1205, 1206, 1207, 1208, 1210)
			.put(DEFEND, 420)
			.put(ATTACK_SPEC, 1064)
			.put(ATTACK_SLASH, 440)
			.put(ATTACK_CRUSH, 393)
			.put(ATTACK_CRUSH2, 419, "crush2 (no offhand)")
			.put(ATTACK_STAB, 381)
			.build();
		new AnimationSetBuilder("Falconer's glove")
			.poseAnims(5160, 5161, 5164, 5165, 5166, 5167, 5168)
			.doNotReplace()
			.build();
		new AnimationSetBuilder("Halberd")
			.poseAnims(813, 1209, 1205, 1206, 1207, 1208, 1210)
			.put(ATTACK_SLASH, 440)
			.put(ATTACK_STAB, 428)
			.put(ATTACK_SPEC, 1203)
			.put(DEFEND, 430)
			.build();
		new AnimationSetBuilder("Cursed banana")
			.poseAnims(4646, 823, 4682, 6276, 6268, 6275, 6277)
			.put(ATTACK_CRUSH, 6278)
			.put(DEFEND, 6279)
			.build();
		new AnimationSetBuilder("Crab")
			.copy("Cursed banana")
			.put(STAND, 10051)
			.build();
		new AnimationSetBuilder("Elder maul")
			.poseAnims(7518, 823, 7520, 7520, 7520, 7520, 7519)
			.put(ATTACK_CRUSH, 7516)
			.put(DEFEND, 7517)
			.build();
		new AnimationSetBuilder("Scythe (holiday item)")
			.poseAnims(847, 823, 819, 820, 821, 822, 824)
			.put(ATTACK_SLASH, 440)
			.put(ATTACK_CRUSH, 438)
			.put(DEFEND, 435)
			.build();
		new AnimationSetBuilder("2h sword")
			.poseAnims(2561, 823, 2562, 2562, 2562, 2562, 2563)
			.put(ATTACK_SLASH, 407)
			.put(ATTACK_CRUSH, 406)
			.put(DEFEND, 410)
			.put(ATTACK_SPEC, 3157)
			.build();
		new AnimationSetBuilder("Birthday balloons")
			.poseAnims(7538, 823, 7539, 7539, 821, 822, 7540)
			.put(ATTACK_CRUSH, 7541)
			.put(DEFEND, 1834)
			.build();
		new AnimationSetBuilder("Banner")
			.poseAnims(1421, 1426, 1422, 1423, 1424, 1425, 1427)
			.put(ATTACK_CRUSH, 1428)
			.put(DEFEND, 1429)
			.build();
		new AnimationSetBuilder("Dharok's greataxe")
			.poseAnims(2065, 823, 2064, 2064, 2064, 2064, 11466)
			.put(ATTACK_SLASH, 2067)
			.put(ATTACK_CRUSH, 2066)
			.put(DEFEND, 424)
			.build();
		new AnimationSetBuilder("Dharok's greataxe (old run)")
			.copy("Dharok's greataxe")
			.put(RUN, 824)
			.build();
		new AnimationSetBuilder("Hunting knife")
			.poseAnims(2911, 823, 7327, 7327, 821, 822, 2322)
			.put(ATTACK_CRUSH, 7328)
			.put(DEFEND, 378)
			.build();
		new AnimationSetBuilder("Giant boulder")
			.poseAnims(4193, 4194, 4194, 4194, 4194, 4194, 7274)
			.put(ATTACK_CRUSH, 7275)
			.put(DEFEND, 7276)
			.build();
		new AnimationSetBuilder("Golden tench")
			.poseAnims(8208, 823, 819, 820, 821, 822, 824)
			.put(ATTACK_CRUSH, 8209)
			.put(DEFEND, 424)
			.build();
		new AnimationSetBuilder("Unarmed")
			.poseAnims(808, 823, 819, 820, 821, 822, 824)
			.put(ATTACK_CRUSH, 422, "punch")
			.put(ATTACK_CRUSH2, 423, "crush2 (kick)")
			.put(DEFEND, 424)
			.build();
		new AnimationSetBuilder("Dragon scimitar")
			.poseAnims(808, 823, 819, 820, 821, 822, 824)
			.put(ATTACK_SLASH, 390)
			.put(ATTACK_STAB, 386)
			.put(DEFEND, 388)
			.put(ATTACK_SPEC, 1872)
			.build();
		new AnimationSetBuilder("Dragon longsword/Saeldor")
			.poseAnims(809, 823, 819, 820, 821, 822, 824)
			.put(ATTACK_SLASH, 390)
			.put(ATTACK_STAB, 386)
			.put(DEFEND, 388)
			.put(ATTACK_SPEC, 1058)
			.build();
		new AnimationSetBuilder("Ghrazi rapier")
			.poseAnims(809, 823, 819, 820, 821, 822, 824)
			.put(ATTACK_SLASH, 390)
			.put(ATTACK_STAB, 8145)
			.put(DEFEND, 388)
			.build();
		new AnimationSetBuilder("Noose wand")
			.poseAnims(5254, 5252, 5250, 5251, 1207, 1208, 5253)
			.put(ATTACK_SLASH, 440)
			.put(ATTACK_CRUSH, 429)
			.put(ATTACK_STAB, 428)
			.put(DEFEND, 430)
			.build();
		new AnimationSetBuilder("Magic butterfly net")
			.poseAnims(6604, 6611, 6607, 6608, 6610, 6609, 6603)
			.put(ATTACK_SLASH, 440)
			.put(ATTACK_CRUSH, 429)
			.put(ATTACK_STAB, 428)
			.put(DEFEND, 430)
			.build();
		new AnimationSetBuilder("Trident of the swamp")
			.poseAnims(813, 1209, 1205, 1206, 1207, 1208, 1210)
			.put(ATTACK, 1167)
			.put(DEFEND, 420)
			.build();
		new AnimationSetBuilder("Eye of ayak")
			.poseAnims(813, 1209, 1205, 1206, 1207, 1208, 1210)
			.put(ATTACK, 12397)
			.put(DEFEND, 420) // guess
			.build();
		new AnimationSetBuilder("Granite maul")
			.poseAnims(1662, 823, 1663, 1663, 1663, 1663, 1664)
			.put(ATTACK_CRUSH, 1665)
			.put(ATTACK_SPEC, 1667)
			.put(DEFEND, 1666)
			.build();
		new AnimationSetBuilder("Red salamander")
			.poseAnims(5246, 823, 5245, 5245, 5245, 5245, 824)
			.put(DEFEND, 388)
			.build();
		new AnimationSetBuilder("Undead chicken")
			.poseAnims(5363, 823, 5364, 5438, 821, 822, 824)
			.put(ATTACK_CRUSH, 5439)
			.put(DEFEND, 5441)
			.build();
		new AnimationSetBuilder("Rubber chicken")
			.poseAnims(1832, 823, 1830, 1830, 821, 822, 824)
			.put(ATTACK_CRUSH, 1833)
			.put(DEFEND, 1834)
			.build();
		new AnimationSetBuilder("Easter basket")
			.poseAnims(1837, 823, 1836, 1836, 1836, 1836, 1836)
			.put(ATTACK_CRUSH, 422)
			.put(DEFEND, 424)
			.build();
		new AnimationSetBuilder("Fixed device")
			.poseAnims(2316, 2321, 2317, 2318, 2319, 2320, 2322)
			.put(ATTACK_CRUSH, 2323)
			.put(DEFEND, 2324)
			.build();
		new AnimationSetBuilder("Crystal grail")
			.poseAnims(3040, 823, 3039, 820, 821, 822, 824)
			.put(ATTACK_CRUSH, 422, "punch")
			.put(ATTACK_CRUSH2, 423, "crush2 (kick)")
			.put(DEFEND, 424)
			.build();
		new AnimationSetBuilder("Drunk")
			.poseAnims(2770, 823, 2769, 820, 821, 822, 824)
			.build();
		new AnimationSetBuilder("Stealing")
			.poseAnims(10684, 10683, 10682, 10683, 10683, 10683, 824)
			.build();
		new AnimationSetBuilder("Chinchompa")
			.poseAnims(3175, 3177, 3177, 3177, 3177, 3177, 3178)
			.put(ATTACK, 7618)
			.put(DEFEND, 3176)
			.build();
		new AnimationSetBuilder("Barrelchest anchor")
			.poseAnims(5869, 823, 5867, 5867, 5867, 5867, 5868)
			.put(ATTACK_CRUSH, 5865)
			.put(DEFEND, 5866)
			.put(ATTACK_SPEC, 5870)
			.build();
		new AnimationSetBuilder("Pet rock")
			.poseAnims(6657, 6661, 6658, 6659, 6662, 6663, 6660)
			.put(ATTACK_CRUSH, 422, "punch")
			.put(ATTACK_CRUSH2, 423, "crush2 (kick)")
			.put(DEFEND, 424)
			.build();
		new AnimationSetBuilder("Ballista")
			.poseAnims(7220, 823, 7223, 7223, 7223, 7223, 7221)
			.put(ATTACK, 7555)
			.put(DEFEND, 7219)
			.put(ATTACK_SPEC, 7556)
			.build();
		new AnimationSetBuilder("Ivandis flail")
			.poseAnims(8009, 8015, 8011, 8012, 8013, 8014, 8016)
			.put(ATTACK_CRUSH, 8010)
			.put(DEFEND, 8017)
			.build();
		new AnimationSetBuilder("Viggora's chainmace")
			.poseAnims(244, 823, 247, 247, 247, 247, 248)
			.put(ATTACK_CRUSH, 245)
			.put(DEFEND, 4177)
			.build();
		new AnimationSetBuilder("Skeleton lantern")
			.poseAnims(8521, 8492, 8492, 8492, 8492, 8492, 8492)
			.put(ATTACK_CRUSH, 422, "punch")
			.put(ATTACK_CRUSH2, 423, "crush2 (kick)")
			.put(DEFEND, 424)
			.build();
		new AnimationSetBuilder("Bow")
			.poseAnims(808, 823, 819, 820, 821, 822, 824)
			.put(ATTACK, 426)
			.put(DEFEND, 424)
			.put(ATTACK_SPEC, 1074, "spec (msb)")
			.build();
		new AnimationSetBuilder("Comp bow")
			.poseAnims(813, 1209, 1205, 1206, 1207, 1208, 1210)
			.put(ATTACK, 426)
			.put(DEFEND, 424)
			.build();
		new AnimationSetBuilder("Crossbow")
			.poseAnims(4591, 823, 4226, 4227, 821, 822, 4228)
			.put(ATTACK, 7552)
			.put(DEFEND, 424)
			.build();
		new AnimationSetBuilder("Zaryte crossbow")
			.poseAnims(4591, 823, 4226, 4227, 821, 822, 4228)
			.put(ATTACK, 9168)
			.put(DEFEND, 424)
			.put(ATTACK_SPEC, 9168)
			// 9166 looks identical but isn't used as either the regular attack or the spec.
			.build();
		new AnimationSetBuilder("Unknown (arms out)")
			.poseAnims(9050, 9050, 9051, 9054, 9052, 9053, 9051)
			.doNotReplace()
			.build();
		new AnimationSetBuilder("Dart")
			.poseAnims(808, 823, 819, 820, 821, 822, 824)
			.put(ATTACK, 7554)
			.put(DEFEND, 424)
			.build();
		new AnimationSetBuilder("Toxic blowpipe")
			.poseAnims(813, 1209, 1205, 1206, 1207, 1208, 1210)
			.put(ATTACK, 5061)
			.put(DEFEND, 430)
			.build();
		new AnimationSetBuilder("Blazing blowpipe")
			.copy("Toxic blowpipe")
			.put(ATTACK, 10656)
			.build();
		new AnimationSetBuilder("Thrownaxe")
			.poseAnims(808, 823, 819, 820, 821, 822, 824)
			.put(ATTACK, 7617)
			.put(ATTACK_SPEC, 7521)
			.put(DEFEND, 424)
			.build();
		new AnimationSetBuilder("Knife (non-dragon)")
			.poseAnims(808, 823, 819, 820, 821, 822, 824)
			.put(ATTACK, 7617)
			.put(ATTACK_SPEC, 8291)
			.put(DEFEND, 424)
			.build();
		new AnimationSetBuilder("Dragon knife")
			.poseAnims(808, 823, 819, 820, 821, 822, 824)
			.put(ATTACK, 8194)
			.put(ATTACK_SPEC, 8291)
			.put(DEFEND, 424)
			.build();
		new AnimationSetBuilder("Dragon knife (poisoned)")
			.poseAnims(808, 823, 819, 820, 821, 822, 824)
			.put(ATTACK, 8195)
			.put(ATTACK_SPEC, 8292)
			.put(DEFEND, 424)
			.build();
		new AnimationSetBuilder("Guthan's warspear")
			.poseAnims(813, 1209, 1205, 1206, 1207, 1208, 1210)
			.put(ATTACK_SLASH, 2081)
			.put(ATTACK_CRUSH, 2082)
			.put(ATTACK_STAB, 2080)
			.put(DEFEND, 430)
			.build();
		new AnimationSetBuilder("Abyssal whip")
			.poseAnims(808, 823, 1660, 1660, 1660, 1660, 1661)
			.put(ATTACK_SLASH, 1658)
			.put(DEFEND, 1659)
			.build();
		new AnimationSetBuilder("Sarachnis cudgel")
			.poseAnims(809, 823, 819, 820, 821, 822, 824)
			.put(ATTACK_CRUSH, 401)
			.put(ATTACK_STAB, 400)
			.put(DEFEND, 403)
			.build();
		new AnimationSetBuilder("Dragon mace")
			.poseAnims(808, 823, 819, 820, 821, 822, 824)
			.put(ATTACK_CRUSH, 401)
			.put(ATTACK_STAB, 400)
			.put(DEFEND, 403)
			.put(ATTACK_SPEC, 1060)
			.build();
		new AnimationSetBuilder("Ancient mace")
			.copy("Dragon mace")
			.put(ATTACK_SPEC, 6147)
			.build();
		new AnimationSetBuilder("Dragon warhammer")
			.copy("Dragon mace")
			.put(ATTACK_SPEC, 1378)
			.build();
		new AnimationSetBuilder("Dragon axe")
			.poseAnims(808, 823, 819, 820, 821, 822, 824)
			.put(ATTACK_SLASH, 395)
			.put(ATTACK_CRUSH, 401)
			.put(ATTACK_STAB, 400)
			.put(DEFEND, 397)
			.put(ATTACK_SPEC, 2876)
			.build();
		new AnimationSetBuilder("Dragon battleaxe")
			.copy("Dragon axe")
			.put(ATTACK_SPEC, 1056)
			.build();
		new AnimationSetBuilder("Spear")
			.poseAnims(813, 1209, 1205, 1206, 1207, 1208, 1210)
			.put(ATTACK_SLASH, 380)
			.put(ATTACK_CRUSH, 382)
			.put(ATTACK_STAB, 381)
			.put(ATTACK_SPEC, 1064)
			.put(DEFEND, 383)
			.build();
		new AnimationSetBuilder("Snowball")
			.poseAnims(808, 823, 819, 820, 821, 822, 824)
			.put(ATTACK, 5063)
			.put(DEFEND, 424)
			.build();
		new AnimationSetBuilder("Sled")
			.poseAnims(1461, 1468, 8854, 1468, 1468, 1468, 8853)
			.doNotReplace()
			.build();
		new AnimationSetBuilder("Claws")
			.poseAnims(808, 823, 819, 820, 821, 822, 824)
			.put(ATTACK_SLASH, 393)
			.put(ATTACK_STAB, 1067)
			.put(DEFEND, 424)
			.put(ATTACK_SPEC, 7514)
			.build();
		new AnimationSetBuilder("Verac's flail")
			.poseAnims(2061, 823, 2060, 2060, 2060, 2060, 824)
			.put(ATTACK_CRUSH, 2062)
			.put(DEFEND, 2063)
			.build();
		new AnimationSetBuilder("Abyssal bludgeon")
			.poseAnims(1652, 823, 3293, 3293, 3293, 3293, 2847)
			.put(ATTACK_CRUSH, 3298)
			.put(DEFEND, 1666)
			.put(ATTACK_SPEC, 3299)
			.build();
		new AnimationSetBuilder("Karil's crossbow")
			.poseAnims(2074, 823, 2076, 2076, 2076, 2076, 2077)
			.put(ATTACK, 2075)
			.put(DEFEND, 424)
			.build();
		new AnimationSetBuilder("Abyssal dagger")
			.poseAnims(3296, 823, 819, 820, 821, 822, 824)
			.put(ATTACK_SLASH, 3294)
			.put(ATTACK_STAB, 3297)
			.put(DEFEND, 3295)
			.put(ATTACK_SPEC, 3300)
			.build();
		new AnimationSetBuilder("Torag's hammers")
			.poseAnims(808, 823, 819, 820, 821, 822, 824)
			.put(ATTACK_CRUSH, 2068)
			.put(DEFEND, 424)
			.build();
		new AnimationSetBuilder("Dual Macauhuitl")
			.copy("Torag's hammers")
			.put(ATTACK_CRUSH, 10989)
			.put(ATTACK_SPEC, 10990)
			.build();
		new AnimationSetBuilder("Zamorakian spear")
			.poseAnims(1713, 1702, 1703, 1704, 1706, 1705, 1707)
			.put(ATTACK_SLASH, 1712)
			.put(ATTACK_CRUSH, 1710)
			.put(ATTACK_STAB, 1711)
			.put(DEFEND, 1709)
			.put(ATTACK_SPEC, 1064)
			.build();
		new AnimationSetBuilder("Leaf-bladed battleaxe")
			.poseAnims(808, 823, 819, 820, 821, 822, 824)
			.put(ATTACK_SLASH, 7004)
			.put(ATTACK_CRUSH, 3852)
			.put(DEFEND, 397)
			.build();
		new AnimationSetBuilder("Dragon sword")
			.poseAnims(808, 823, 819, 820, 821, 822, 824)
			.put(ATTACK_SLASH, 390)
			.put(ATTACK_STAB, 386)
			.put(DEFEND, 388)
			.put(ATTACK_SPEC, 7515)
			.build();
		new AnimationSetBuilder("Arclight")
			.poseAnims(808, 823, 819, 820, 821, 822, 824)
			.put(ATTACK_SLASH, 390)
			.put(ATTACK_STAB, 386)
			.put(DEFEND, 388)
			.put(ATTACK_SPEC, 2890)
			.build();
		new AnimationSetBuilder("Dragon dagger")
			.poseAnims(808, 823, 819, 820, 821, 822, 824)
			.put(ATTACK_SLASH, 377)
			.put(ATTACK_STAB, 376)
			.put(ATTACK_SPEC, 1062)
			.put(DEFEND, 378)
			.build();
		new AnimationSetBuilder("Clan vexillum")
			.poseAnims(9018, 7044, 9017, 9017, 9021, 9020, 9019)
			.put(ATTACK_SLASH, 7045)
			.put(ATTACK_CRUSH, 7054)
			.put(ATTACK_STAB, 7046)
			.put(DEFEND, 7056)
			.build();
		new AnimationSetBuilder("Tightrope")
			.poseAnims(763, 762, 762, 762, 762, 762, 762)
			.doNotReplace()
			.build();
		new AnimationSetBuilder("Monkey bars")
			.poseAnims(745, 745, 744, 745, 745, 745, 744)
			.doNotReplace()
			.build();
		new AnimationSetBuilder("Swimming")
			.poseAnims(773, 773, 772, 772, 772, 772, 772)
			.doNotReplace()
			.build();
		new AnimationSetBuilder("Swimming (drowning)")
			.poseAnims(765, 765, 772, 772, 772, 772, 772)
			.doNotReplace()
			.build();
		new AnimationSetBuilder("Swimming (underwater)")
			.poseAnims(3418, 3415, 3415, 3415, 3415, 3415, 7703)
			.doNotReplace()
			.build();
		new AnimationSetBuilder("Swimming (trident)")
			.poseAnims(6998, 6998, 6996, 6996, 6996, 6996, 6995)
			.put(ATTACK, 6997)
			.doNotReplace()
			.build();
		new AnimationSetBuilder("Transparent")
			.poseAnims(15, 15, 13, 13, 13, 13, 13)
			.put(ATTACK_SLASH, 391)
			.put(DEFEND, 389)
			.build();
		new AnimationSetBuilder("Crawling")
			.poseAnims(845, 845, 844, 844, 844, 844, 1440)
			.doNotReplace()
			.build();
		new AnimationSetBuilder("Leaping")
			.movement(1603)
			.doNotReplace()
			.build();
		new AnimationSetBuilder("Skipping")
			.movement(3854)
			.doNotReplace()
			.build();
		new AnimationSetBuilder("Zombie")
			.standMovement(6113, 6112)
			.doNotReplace()
			.build();
		new AnimationSetBuilder("Hands on hips")
			.standMovement(6393, 6395)
			.doNotReplace()
			.build();
		new AnimationSetBuilder("Hands behind back")
			.standMovement(6389, 6388)
			.doNotReplace()
			.build();
		new AnimationSetBuilder("Sandstorm")
			.standMovement(6379, 6378)
			.doNotReplace()
			.build();
		new AnimationSetBuilder("Hunched over")
			.standMovement(6469, 6468)
			.doNotReplace()
			.build();
		new AnimationSetBuilder("Posh walk")
			.standMovement(6487, 6486)
			.doNotReplace()
			.build();
		new AnimationSetBuilder("Posh walk 2")
			.standMovement(6927, 6928)
			.doNotReplace()
			.build();
		new AnimationSetBuilder("Looking at hands")
			.standMovement(6075, 6076)
			.doNotReplace()
			.build();
		new AnimationSetBuilder("Chicken")
			.standMovement(6397, -1)
			.doNotReplace()
			.build();
		new AnimationSetBuilder("Skis")
			.poseAnims(9341, 9343, 9342, 9345, 9343, 9344, 9346)
			.put(ATTACK_CRUSH, 9347)
			.put(DEFEND, 9348)
			.build();
		new AnimationSetBuilder("Colossal Blade")
			.poseAnims(9460, 10055, 9461, 9461, 10055, 10054, 9459)
			.put(ATTACK_CRUSH, 7516)
			.put(ATTACK_SLASH, 7516)
			.put(DEFEND, 7517)
			.build();
		new AnimationSetBuilder("Tumeken's Shadow")
			.poseAnims(9494, 1702, 1703, 1704, 1706, 1705, 1707)
			.put(ATTACK, 9493)
			.put(DEFEND, 1709) // Not sure, haven't seen this one in-game.
			.build();
		new AnimationSetBuilder("Osmumten's Fang")
			.poseAnims(809, 823, 819, 820, 821, 822, 824) // not sure
			.put(ATTACK_STAB, 9471)
			.put(ATTACK_SLASH, 390)
			.put(ATTACK_SPEC, 11222)
			.put(DEFEND, 4177)
			.build();
		new AnimationSetBuilder("Osmumten's Fang (old spec)")
			.copy("Osmumten's Fang")
			.put(ATTACK_SPEC, 6118) // Used to be 9544 but for some reason jagex changed it. gfx 2124
			.build();
		new AnimationSetBuilder("Voidwaker")
			.copy("Dragon sword")
			.put(ATTACK_SPEC, 11275)
			.build();
		new AnimationSetBuilder("Voidwaker (old spec)")
			.copy("Voidwaker")
			.put(ATTACK_SPEC, 1378)
			.build();
		new AnimationSetBuilder("Venator Bow")
			.poseAnims(9857, 9863, 9859, 9859, 9861, 9862, 9860) // unconfirmed
			.put(ATTACK, 9858) // unconfirmed
			.put(DEFEND, 424) // unconfirmed
			.build();
		new AnimationSetBuilder("Mystic cards")
			.poseAnims(9847, 823, 9849, 820, 9851, 9852, 9850)
			.put(ATTACK, 9848)
			.put(DEFEND, 9853)
			.build();
		new AnimationSetBuilder("Tzhaar-ket-om")
			.poseAnims(2065, 823, 2064, 2064, 2064, 2064, 824)
			.put(ATTACK, 2661)
			.put(DEFEND, 1661)
			.build();
		new AnimationSetBuilder("Toktz-xil-ul (obsidian ring)")
			.poseAnims(808, 823, 819, 820, 821, 822, 824)
			.put(ATTACK, 7558)
			.put(DEFEND, 424)
			.build();
		new AnimationSetBuilder("Thammaron's sceptre")
			.poseAnims(813, 1205, 1205, 1206, 1207, 1208, 1210)
			.put(ATTACK, 419) // not sure
			.put(DEFEND, 420) // not sure
			.build();
		new AnimationSetBuilder("2h axe")
			.poseAnims(10075, 1205, 10076, 10076, 10076, 10076, 10077)
			.put(ATTACK, 10079)
			.put(ATTACK_SPEC, 10078)
			.put(DEFEND, 10080)
			.build();
		new AnimationSetBuilder("2h axe (old)")
			.standMovement(10598, 10599)
			.put(Swap.AnimationType.RUN, 10600)
			.put(ATTACK, 10602)
			.put(ATTACK_SPEC, 10601)
			.put(DEFEND, 10603)
			.build();
		new AnimationSetBuilder("Soulreaper axe")
			.poseAnims(10169, 823, 10170, 10170, 10170, 10170, 824)
			.put(ATTACK_SLASH, 10172)
			.put(ATTACK_CRUSH, 10171)
			.put(ATTACK_SPEC, 10173)
			.put(DEFEND, 424)
			.build();
		new AnimationSetBuilder("Warped sceptre")
			.copy("Staff")
			.put(ATTACK, 10501)
			.build();

		return animationSets;
	}

	public static AnimationSet getAnimationSet(String name) {
		return animationSets.stream().filter(a -> name.equals(a.name)).findAny().orElse(null);
	}

	private static class AnimationSetBuilder {
		public int[] animations = new int[Swap.AnimationType.values().length];
		private String name;
		// This flag should be true for animations that are not the result of equipped weapons, such as the animations when the player crosses a tightrope. This animation is implemented as a pose animation but it should not be replaced by the player's current animation override.
		private boolean doNotReplace = false;

		{
			Arrays.fill(animations, -1);
		}

		public AnimationSetBuilder(String name) {
			this.name = name;
		}

		public AnimationSetBuilder doNotReplace() {
			doNotReplace = true;
			return this;
		}

		public AnimationSetBuilder put(Swap.AnimationType type, int id) {
			return put(type, id, null);
		}

		public AnimationSetBuilder put(Swap.AnimationType type, int id, String description) {
			if (description != null) {
				descriptions.put(id, description);
			}
			if (id != -1) animations[type.ordinal()] = id;
			return this;
		}

		public AnimationSetBuilder poseAnims(int stand, int rotate, int walk, int walkBackwards, int shuffleLeft, int shuffleRight, int run) {
			put(STAND, stand);
			put(ROTATE, rotate);
			movement(walk, walkBackwards, shuffleLeft, shuffleRight, run);
			return this;
		}

		public AnimationSetBuilder movement(int walk, int walkBackwards, int shuffleLeft, int shuffleRight, int run) {
			put(Swap.AnimationType.WALK, walk);
			put(WALK_BACKWARD, walkBackwards);
			put(SHUFFLE_LEFT, shuffleLeft);
			put(SHUFFLE_RIGHT, shuffleRight);
			put(Swap.AnimationType.RUN, run);
			return this;
		}

		public AnimationSetBuilder movement(int all) {
			return this.movement(all, all, all, all, all);
		}

		public void build() {
			if (doNotReplace) {
				int animation = animations[STAND.ordinal()];
//				if (animation > 0) doNotReplaceIdles.add(animation);
			}
			animationSets.add(new AnimationSet(name, doNotReplace, animations));
		}

		public AnimationSetBuilder standMovement(int stand, int movement)
		{
			put(STAND, stand);
			put(ROTATE, stand);
			movement(movement);
			return this;
		}

		public AnimationSetBuilder copy(String setToCopy)
		{
			AnimationSet animationSet = getAnimationSet(setToCopy);
			this.animations = Arrays.copyOf(animationSet.animations, animationSet.animations.length);
			return this;
		}
	}

}
