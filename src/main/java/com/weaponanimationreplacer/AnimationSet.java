package com.weaponanimationreplacer;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.weaponanimationreplacer.Swap.AnimationType;
import static com.weaponanimationreplacer.Swap.AnimationType.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Data;

public class AnimationSet implements Comparable<AnimationSet> {
    public static final List<AnimationSet> animationSets = new ArrayList<>();
    private final AnimationSetType animationSetType;

	public static AnimationSet getAnimationSet(String name) {
		return animationSets.stream().filter(a -> name.equals(a.name)).findAny().orElse(null);
	}

	@Override
	public int compareTo(AnimationSet o) {
//        int typeComparison = animationSetType.compareTo(o.animationSetType);
//        return typeComparison == 0 ? name.compareTo(o.name) : typeComparison;
        return name.compareTo(o.name);
    }

    // used for ordering, so declaration order matters.
    private enum AnimationSetType {
        MELEE_GENERIC, MELEE_SPECIFIC, RANGED, RANGED_FUN, MAGIC, FUN
    }

    static {
        animationSets.add(new AnimationSet("Scythe of Vitur",false, AnimationSetType.MELEE_SPECIFIC,
                     // TODO get actual data.
                     STAND, 8057,
					RUN, 824,
					WALK, 819,
					WALK_BACKWARD, 820,
					SHUFFLE_LEFT, 821,
					SHUFFLE_RIGHT, 822,
					ROTATE, 823,
					ATTACK_SLASH, 8056,
					ATTACK_CRUSH, 8056
        ));

        animationSets.add(new AnimationSet("Nightmare Staff",false, AnimationSetType.MAGIC,
                // TODO get actual data.
                STAND, 4504,
                RUN, 1210,
                WALK, 1205,
                WALK_BACKWARD, 1206,
                SHUFFLE_LEFT, 1207,
                SHUFFLE_RIGHT, 1208,
                ROTATE, 1209,
                DEFEND, 420,
                ATTACK_CRUSH, 4505
        ));

        animationSets.add(new AnimationSet("Inquisitor's mace",false, AnimationSetType.MELEE_SPECIFIC,
                // TODO get actual data.
                STAND, 808,
                RUN, 824,
                WALK, 819,
                WALK_BACKWARD, 820,
                SHUFFLE_LEFT, 821,
                SHUFFLE_RIGHT, 822,
                ROTATE, 823,
                DEFEND, 403,
                ATTACK_CRUSH, 4503
        ));

        animationSets.add(new AnimationSet("Dinh's bulwhark",false, AnimationSetType.MELEE_SPECIFIC,
                     STAND, 7508,
                     WALK, 7510,
                     RUN, 7509,
                     ATTACK_CRUSH, 7511,
                     ATTACK_SPEC, 7512,
                     DEFEND, 7517
        ));

        animationSets.add(new AnimationSet("Dragon hunter lance",false, AnimationSetType.MELEE_SPECIFIC,
                     STAND, 813,
                     WALK, 1205,
                     WALK_BACKWARD, 1206,
                     SHUFFLE_LEFT, 1207,
                     SHUFFLE_RIGHT, 1208,
                     ROTATE, 1209,
                     RUN, 2563,
                     ATTACK_STAB, 8288,
                     ATTACK_SLASH, 8289,
                     ATTACK_CRUSH, 8290
        ));

        animationSets.add(new AnimationSet("Flying Carpet",false, AnimationSetType.FUN,
                STAND, 6936,
                WALK, 6936,
                WALK_BACKWARD, 6936,
                SHUFFLE_LEFT, 6936,
                SHUFFLE_RIGHT, 6936,
                ROTATE, 6936,
                RUN, 6936
        ));

        animationSets.add(new AnimationSet("Levitate",false, AnimationSetType.FUN,
                STAND, 8070,
                WALK, 8070,
                WALK_BACKWARD, 8070,
                SHUFFLE_LEFT, 8070,
                SHUFFLE_RIGHT, 8070,
                ROTATE, 8070,
                RUN, 8070
        ));

        animationSets.add(new AnimationSet("Clueless Scroll",false, AnimationSetType.FUN,
                STAND, 7271,
                RUN, 7273,
                WALK, 7272,
                WALK_BACKWARD, 820,
                SHUFFLE_LEFT, 821,
                SHUFFLE_RIGHT, 822,
                ROTATE, 823,
                ATTACK_CRUSH, 406
        ));

        animationSets.add(new AnimationSet("Godsword (Saradomin)",false, AnimationSetType.MELEE_SPECIFIC,
                STAND, 7053,
                RUN, 7043,
                WALK, 7052,
                WALK_BACKWARD, 7052,
                SHUFFLE_LEFT, 7048,
                SHUFFLE_RIGHT, 7047,
                ROTATE, 7044,
                ATTACK_SLASH, 7045,
                ATTACK_SLASH2, 7055,
                ATTACK_CRUSH, 7054,
                ATTACK_SPEC, 7640,
                DEFEND, 7056
        ));

        animationSets.add(new AnimationSet("Godsword (Bandos)",false, AnimationSetType.MELEE_SPECIFIC,
                STAND, 7053,
                RUN, 7043,
                WALK, 7052,
                WALK_BACKWARD, 7052,
                SHUFFLE_LEFT, 7048,
                SHUFFLE_RIGHT, 7047,
                ROTATE, 7044,
                ATTACK_SLASH, 7045,
                ATTACK_SLASH2, 7055,
                ATTACK_CRUSH, 7054,
                ATTACK_SPEC, 7642,
                DEFEND, 7056
        ));

        animationSets.add(new AnimationSet("Godsword (Armadyl)",false, AnimationSetType.MELEE_SPECIFIC,
                STAND, 7053,
                RUN, 7043,
                WALK, 7052,
                WALK_BACKWARD, 7052,
                SHUFFLE_LEFT, 7048,
                SHUFFLE_RIGHT, 7047,
                ROTATE, 7044,
                ATTACK_SLASH, 7045,
                ATTACK_SLASH2, 7055,
                ATTACK_CRUSH, 7054,
                ATTACK_SPEC, 7644,
                DEFEND, 7056
        ));

        animationSets.add(new AnimationSet("Godsword (Zamorak)",false, AnimationSetType.MELEE_SPECIFIC,
                STAND, 7053,
                RUN, 7043,
                WALK, 7052,
                WALK_BACKWARD, 7052,
                SHUFFLE_LEFT, 7048,
                SHUFFLE_RIGHT, 7047,
                ROTATE, 7044,
                ATTACK_SLASH, 7045,
                ATTACK_SLASH2, 7055,
                ATTACK_CRUSH, 7054,
                ATTACK_SPEC, 7638,
                DEFEND, 7056
        ));

        animationSets.add(new AnimationSet("Boxing gloves", false, AnimationSetType.FUN,
                STAND, 3677,
                RUN, 824,
                WALK, 3680,
                WALK_BACKWARD, 3680,
                SHUFFLE_LEFT, 3680,
                SHUFFLE_RIGHT, 3680,
                ROTATE, 823,
                ATTACK_STAB, 3678,
                ATTACK_SLASH, 3678,
                DEFEND, 3679
        ));

        animationSets.add(new AnimationSet("Hand fan", false, AnimationSetType.FUN,
                STAND, 6297,
                RUN, 7633,
                WALK, 7629,
                WALK_BACKWARD, 7630,
                SHUFFLE_LEFT, 7631,
                SHUFFLE_RIGHT, 7632,
                ROTATE, 6297,
                DEFEND, 424,
                ATTACK_CRUSH, 401
        ));

        animationSets.add(new AnimationSet("Prop sword/candy cane", false, AnimationSetType.FUN,
                STAND, 2911,
                RUN, 824,
                WALK, 819,
                WALK_BACKWARD, 820,
                SHUFFLE_LEFT, 821,
                SHUFFLE_RIGHT, 822,
                ROTATE, 823,
                DEFEND, 378,
                ATTACK_CRUSH, 7328
        ));

        animationSets.add(new AnimationSet("Staff", false, AnimationSetType.MELEE_GENERIC,
                STAND, 813,
                RUN, 1210,
                WALK, 1205,
                WALK_BACKWARD, 1206,
                SHUFFLE_LEFT, 1207,
                SHUFFLE_RIGHT, 1208,
                ROTATE, 1209,
                DEFEND, 420,
                ATTACK_CRUSH, 419
        ));

        animationSets.add(new AnimationSet("Zamorakian hasta", false, AnimationSetType.MELEE_SPECIFIC,
                STAND, 813,
                RUN, 1210,
                WALK, 1205,
                WALK_BACKWARD, 1206,
                SHUFFLE_LEFT, 1207,
                SHUFFLE_RIGHT, 1208,
                ROTATE, 1209,
                ATTACK_STAB, 381,
                DEFEND, 420,
                ATTACK_SPEC, 1064,
                ATTACK_SLASH, 440,
                ATTACK_CRUSH, 393,
                ATTACK_CRUSH2, 419, "crush2 (no offhand)"
        ));

        animationSets.add(new AnimationSet("Falconer's glove", false, AnimationSetType.FUN,
                STAND, 5160,
                RUN, 5168,
                WALK, 5164,
                WALK_BACKWARD, 5165,
                SHUFFLE_LEFT, 5166,
                SHUFFLE_RIGHT, 5167,
                ROTATE, 5161
        ));

        animationSets.add(new AnimationSet("Halberd",false, AnimationSetType.MELEE_GENERIC,
                STAND, 813,
                WALK, 1205,
                WALK_BACKWARD, 1206,
                SHUFFLE_LEFT, 1207,
                SHUFFLE_RIGHT, 1208,
                ROTATE, 1209,
                RUN, 1210,
                ATTACK_STAB, 428,
                ATTACK_SLASH, 440,
                ATTACK_SPEC, 1203,
                DEFEND, 430
        ));

        animationSets.add(new AnimationSet("Cursed banana",false, AnimationSetType.FUN,
                STAND, 4646,
                WALK, 4682,
                WALK_BACKWARD, 6276,
                SHUFFLE_LEFT, 6268,
                SHUFFLE_RIGHT, 6275,
                ROTATE, 823,
                RUN, 6277,
                ATTACK_SLASH, 6278,
                DEFEND, 6279
        ));

        animationSets.add(new AnimationSet("Elder maul",true, AnimationSetType.MELEE_SPECIFIC,
                STAND, 7518,
                RUN, 7519,
                WALK, 7520,
                WALK_BACKWARD, 7520,
                SHUFFLE_LEFT, 7520,
                SHUFFLE_RIGHT, 7520,
                ROTATE, 823,
                ATTACK_CRUSH, 7516,
                DEFEND, 7517
        ));

        animationSets.add(new AnimationSet("Scythe (holiday item)", false, AnimationSetType.FUN,
                STAND, 847,
                RUN, 824,
                WALK, 819,
                WALK_BACKWARD, 820,
                SHUFFLE_LEFT, 821,
                SHUFFLE_RIGHT, 822,
                ROTATE, 823,
                ATTACK_SLASH, 440,
                DEFEND, 435,
                ATTACK_CRUSH, 438
        ));

        animationSets.add(new AnimationSet("2h sword", false, AnimationSetType.MELEE_GENERIC,
                STAND, 2561,
                RUN, 2563,
                WALK, 2562,
                WALK_BACKWARD, 2562,
                SHUFFLE_LEFT, 2562,
                SHUFFLE_RIGHT, 2562,
                ROTATE, 823,
                DEFEND, 410,
                ATTACK_CRUSH, 406,
                ATTACK_SLASH, 407,
                ATTACK_SPEC, 3157 // d2h spec.
        ));

        animationSets.add(new AnimationSet("Birthday balloons", false, AnimationSetType.FUN,
                STAND, 7538,
                RUN, 7540,
                WALK, 7539,
                WALK_BACKWARD, 7539,
                SHUFFLE_LEFT, 821,
                SHUFFLE_RIGHT, 822,
                ROTATE, 823,
                DEFEND, 1834,
                ATTACK_CRUSH, 7541
        ));

        animationSets.add(new AnimationSet("Banner", false, AnimationSetType.FUN,
                STAND, 1421,
                RUN, 1427,
                WALK, 1422,
                WALK_BACKWARD, 1423,
                SHUFFLE_LEFT, 1424,
                SHUFFLE_RIGHT, 1425,
                ROTATE, 1426,
                DEFEND, 1429,
                ATTACK_CRUSH, 1428
        ));

        animationSets.add(new AnimationSet("Dharok's greataxe", false, AnimationSetType.MELEE_SPECIFIC,
                STAND, 2065,
                RUN, 824,
                WALK, 2064,
                WALK_BACKWARD, 2064,
                SHUFFLE_LEFT, 2064,
                SHUFFLE_RIGHT, 2064,
                ROTATE, 823,
                ATTACK_CRUSH, 2066,
                ATTACK_SLASH, 2067,
                DEFEND, 424
        ));

        animationSets.add(new AnimationSet("Hunting knife", false, AnimationSetType.FUN,
                STAND, 2911,
                RUN, 2322,
                WALK, 7327,
                WALK_BACKWARD, 7327,
                SHUFFLE_LEFT, 821,
                SHUFFLE_RIGHT, 822,
                ROTATE, 823,
                ATTACK_CRUSH, 7328,
                DEFEND, 378
        ));

        animationSets.add(new AnimationSet("Giant boulder", false, AnimationSetType.FUN,
                STAND, 4193,
                RUN, 7274,
                WALK, 4194,
                WALK_BACKWARD, 4194,
                SHUFFLE_LEFT, 4194,
                SHUFFLE_RIGHT, 4194,
                ROTATE, 4194,
                ATTACK_CRUSH, 7275,
                DEFEND, 7276
        ));

//        animationSets.add(new AnimationSet("// derivative. Consider not including.", false,
//                2h leagues trophy
//                STAND, 4193,
//                RUN, 7274,
//                WALK, 4194,
//                WALK_BACKWARD, 4194,
//                SHUFFLE_LEFT, 4194,
//                SHUFFLE_RIGHT, 4194,
//                ROTATE, 4194,
//                DEFEND, 403,
//                ATTACK_CRUSH, 401
//        ));
//
        animationSets.add(new AnimationSet("Golden tench", false, AnimationSetType.FUN,
                STAND, 8208,
                RUN, 824,
                WALK, 819,
                WALK_BACKWARD, 820,
                SHUFFLE_LEFT, 821,
                SHUFFLE_RIGHT, 822,
                ROTATE, 823,
                ATTACK_CRUSH, 8209,
                DEFEND, 424
        ));

        animationSets.add(new AnimationSet("unarmed", false, AnimationSetType.MELEE_GENERIC,
                STAND, 808,
                RUN, 824,
                WALK, 819,
                WALK_BACKWARD, 820,
                SHUFFLE_LEFT, 821,
                SHUFFLE_RIGHT, 822,
                ROTATE, 823,
                ATTACK_CRUSH, 422, "punch", // (punch)
                ATTACK_CRUSH2, 423, "crush2 (kick)", // (kick)
                DEFEND, 424
        ));

        animationSets.add(new AnimationSet("shortsword/scim/saeldor", false, AnimationSetType.MELEE_GENERIC,
                STAND, 809,
                RUN, 824,
                WALK, 819,
                WALK_BACKWARD, 820,
                SHUFFLE_LEFT, 821,
                SHUFFLE_RIGHT, 822,
                ROTATE, 823,
                ATTACK_STAB, 386,
                DEFEND, 388,
                ATTACK_SLASH, 390,
                ATTACK_SPEC, 1872
        ));

        animationSets.add(new AnimationSet("Ghrazi rapier", false, AnimationSetType.MELEE_SPECIFIC,
                STAND, 809,
                RUN, 824,
                WALK, 819,
                WALK_BACKWARD, 820,
                SHUFFLE_LEFT, 821,
                SHUFFLE_RIGHT, 822,
                ROTATE, 823,
                ATTACK_STAB, 8145,
                DEFEND, 388,
                ATTACK_SLASH, 390
        ));

        animationSets.add(new AnimationSet("Noose wand", false, AnimationSetType.FUN,
                STAND, 5254,
                RUN, 5253,
                WALK, 5250,
                WALK_BACKWARD, 5251,
                SHUFFLE_LEFT, 1207,
                SHUFFLE_RIGHT, 1208,
                ROTATE, 5252,
                ATTACK_STAB, 428,
                DEFEND, 430,
                ATTACK_SLASH, 440,
                ATTACK_CRUSH, 429,
                DEFEND, 430
        ));

        animationSets.add(new AnimationSet("staff2/wand", false, AnimationSetType.MELEE_GENERIC,
                STAND, 813,
                RUN, 1210,
                WALK, 1205,
                WALK_BACKWARD, 1206,
                SHUFFLE_LEFT, 1207,
                SHUFFLE_RIGHT, 1208,
                ROTATE, 1209,
                ATTACK_CRUSH, 393, // one handed.
                ATTACK_CRUSH, 414, // duplicate of 419
                DEFEND, 415 // duplicate of 420.
        ));

        animationSets.add(new AnimationSet("Magic butterfly net", false, AnimationSetType.FUN,
                STAND, 6604,
                RUN, 6603,
                WALK, 6607,
                WALK_BACKWARD, 6608,
                SHUFFLE_LEFT, 6610,
                SHUFFLE_RIGHT, 6609,
                ROTATE, 6611,
                ATTACK_STAB, 428,
                ATTACK_CRUSH, 429,
                DEFEND, 430,
                ATTACK_SLASH, 440
        ));

        animationSets.add(new AnimationSet("Trident of the swamp", false, AnimationSetType.MAGIC,
                STAND, 813,
                RUN, 1210,
                WALK, 1205,
                WALK_BACKWARD, 1206,
                SHUFFLE_LEFT, 1207,
                SHUFFLE_RIGHT, 1208,
                ROTATE, 1209,
                ATTACK, 1167,
                DEFEND, 420 // unshielded defend??? why don't I have this. It's probably 430 though.
        ));

        animationSets.add(new AnimationSet("Granite maul", false, AnimationSetType.MELEE_SPECIFIC,
                STAND, 1662,
                RUN, 1664,
                WALK, 1663,
                WALK_BACKWARD, 1663,
                SHUFFLE_LEFT, 1663,
                SHUFFLE_RIGHT, 1663,
                ROTATE, 823,
                ATTACK_CRUSH, 1665,
                ATTACK_SPEC, 1667,
                DEFEND, 1666
        ));

        animationSets.add(new AnimationSet("Red salamander", false, AnimationSetType.FUN,
                STAND, 5246,
                RUN, 824,
                WALK, 5245,
                WALK_BACKWARD, 5245,
                SHUFFLE_LEFT, 5245,
                SHUFFLE_RIGHT, 5245,
                ROTATE, 823,
                DEFEND, 388
                // TODO attack animations.
        ));

        animationSets.add(new AnimationSet("Undead chicken", false, AnimationSetType.FUN,
                STAND, 5363,
                RUN, 824,
                WALK, 5364,
                WALK_BACKWARD, 5438,
                SHUFFLE_LEFT, 821,
                SHUFFLE_RIGHT, 822,
                ROTATE, 823,
                ATTACK_CRUSH, 5439,
                DEFEND, 5441
        ));

        animationSets.add(new AnimationSet("Rubber chicken", false, AnimationSetType.FUN,
                STAND, 1832,
                RUN, 824,
                WALK, 1830,
                WALK_BACKWARD, 1830,
                SHUFFLE_LEFT, 821,
                SHUFFLE_RIGHT, 822,
                ROTATE, 823,
                ATTACK_CRUSH, 1833,
                DEFEND, 1834
        ));

        animationSets.add(new AnimationSet("Easter basket", false, AnimationSetType.FUN,
                STAND, 1837,
                RUN, 1836,
                WALK, 1836,
                WALK_BACKWARD, 1836,
                SHUFFLE_LEFT, 1836,
                SHUFFLE_RIGHT, 1836,
                ROTATE, 823,
                ATTACK_CRUSH, 422,
                DEFEND, 424
                // TODO Can't you kick with this?
        ));

        animationSets.add(new AnimationSet("Fixed device", false, AnimationSetType.FUN,
                STAND, 2316,
                RUN, 2322,
                WALK, 2317,
                WALK_BACKWARD, 2318,
                SHUFFLE_LEFT, 2319,
                SHUFFLE_RIGHT, 2320,
                ROTATE, 2321,
                ATTACK_CRUSH, 2323,
                DEFEND, 2324
        ));

        animationSets.add(new AnimationSet("Crystal grail", false, AnimationSetType.FUN,
                STAND, 3040,
                RUN, 824,
                WALK, 3039,
                WALK_BACKWARD, 820,
                SHUFFLE_LEFT, 821,
                SHUFFLE_RIGHT, 822,
                ROTATE, 823,
                ATTACK_CRUSH, 422, "punch", // (punch)
                ATTACK_CRUSH2, 423, "crush2 (kick)", // (kick)
                DEFEND, 424
        ));

        animationSets.add(new AnimationSet("Chinchompa", false, AnimationSetType.RANGED,
                STAND, 3175,
                RUN, 3178,
                WALK, 3177,
                WALK_BACKWARD, 3177,
                SHUFFLE_LEFT, 3177,
                SHUFFLE_RIGHT, 3177,
                ROTATE, 3177,
                DEFEND, 3176,
                ATTACK, 7618
        ));

        animationSets.add(new AnimationSet("Barrelchest anchor", false, AnimationSetType.MELEE_SPECIFIC,
                STAND, 5869,
                RUN, 5868,
                WALK, 5867,
                WALK_BACKWARD, 5867,
                SHUFFLE_LEFT, 5867,
                SHUFFLE_RIGHT, 5867,
                ROTATE, 823,
                ATTACK_CRUSH, 5865, // replaces weapon.
                DEFEND, 5866, // replaces weapon.
                ATTACK_SPEC, 5870 // replaces weapon.
        ));

        animationSets.add(new AnimationSet("Pet rock", false, AnimationSetType.FUN,
                STAND, 6657,
                RUN, 6660,
                WALK, 6658,
                WALK_BACKWARD, 6659,
                SHUFFLE_LEFT, 6662,
                SHUFFLE_RIGHT, 6663,
                ROTATE, 6661,
                ATTACK_CRUSH, 422, "punch", // (punch)
                ATTACK_CRUSH2, 423, "crush2 (kick)", // (kick)
                DEFEND, 424
        ));

        animationSets.add(new AnimationSet("Ballista", false, AnimationSetType.RANGED,
                STAND, 7220,
                RUN, 7221,
                WALK, 7223,
                WALK_BACKWARD, 7223,
                SHUFFLE_LEFT, 7223,
                SHUFFLE_RIGHT, 7223,
                ROTATE, 823,
                DEFEND, 7219,
                ATTACK, 7555,
                ATTACK_SPEC, 7556
        ));

        animationSets.add(new AnimationSet("Ivandis flail", false, AnimationSetType.MELEE_SPECIFIC,
                STAND, 8009,
                RUN, 8016,
                WALK, 8011,
                WALK_BACKWARD, 8012,
                SHUFFLE_LEFT, 8013,
                SHUFFLE_RIGHT, 8014,
                ROTATE, 8015,
                DEFEND, 8017,
                ATTACK_CRUSH, 8010
        ));

		animationSets.add(new AnimationSet("Viggora's chainmace", false, AnimationSetType.MELEE_SPECIFIC,
				STAND, 244,
				RUN, 248,
				WALK, 247,
				WALK_BACKWARD, 247,
				SHUFFLE_LEFT, 247,
				SHUFFLE_RIGHT, 247,
				ROTATE, 823,
				DEFEND, 4177,
				ATTACK_CRUSH, 245
		));

		animationSets.add(new AnimationSet("Skeleton lantern", false, AnimationSetType.FUN,
				STAND, 8521,
				RUN, 8492,
                WALK, 8492,
                WALK_BACKWARD, 8492,
                SHUFFLE_LEFT, 8492,
                SHUFFLE_RIGHT, 8492,
                ROTATE, 8492,
                ATTACK_CRUSH, 422, "punch", // (punch)
                ATTACK_CRUSH2, 423, "crush2 (kick)", // (kick)
                DEFEND, 424
        ));

        animationSets.add(new AnimationSet("Bow", false, AnimationSetType.RANGED,
                STAND, 808,
                RUN, 824,
                WALK, 819,
                WALK_BACKWARD, 820,
                SHUFFLE_LEFT, 821,
                SHUFFLE_RIGHT, 822,
                ROTATE, 823,
                DEFEND, 424,
                ATTACK, 426,
                ATTACK_SPEC, 1074, "spec (msb)"
        ));

        animationSets.add(new AnimationSet("Comp bow", false, AnimationSetType.RANGED,
                STAND, 813,
                RUN, 1210,
                WALK, 1205,
                WALK_BACKWARD, 1206,
                SHUFFLE_LEFT, 1207,
                SHUFFLE_RIGHT, 1208,
                ROTATE, 1209,
                DEFEND, 424,
                ATTACK, 426
        ));

        animationSets.add(new AnimationSet("Crossbow", false, AnimationSetType.RANGED,
                STAND, 4591,
                RUN, 4228,
                WALK, 4226,
                WALK_BACKWARD, 4227,
                SHUFFLE_LEFT, 821,
                SHUFFLE_RIGHT, 822,
                ROTATE, 823,
                DEFEND, 424,
                ATTACK, 7552
        ));

        animationSets.add(new AnimationSet("Dart", false, AnimationSetType.RANGED,
                STAND, 808,
                RUN, 824,
                WALK, 819,
                WALK_BACKWARD, 820,
                SHUFFLE_LEFT, 821,
                SHUFFLE_RIGHT, 822,
                ROTATE, 823,
                DEFEND, 424,
                ATTACK, 7554
        ));

        animationSets.add(new AnimationSet("Toxic blowpipe", false, AnimationSetType.RANGED,
                STAND, 813,
                RUN, 1210,
                WALK, 1205,
                WALK_BACKWARD, 1206,
                SHUFFLE_LEFT, 1207,
                SHUFFLE_RIGHT, 1208,
                ROTATE, 1209,
                DEFEND, 430,
                ATTACK, 5061 // replaces weapon.
        ));

        animationSets.add(new AnimationSet("Thrownaxe", false, AnimationSetType.RANGED,
                STAND, 808,
                RUN, 824,
                WALK, 819,
                WALK_BACKWARD, 820,
                SHUFFLE_LEFT, 821,
                SHUFFLE_RIGHT, 822,
                ROTATE, 823,
                ATTACK_SPEC, 7521,
                ATTACK, 7617,
                DEFEND, 424
        ));

        animationSets.add(new AnimationSet("Knife", false, AnimationSetType.RANGED,
                STAND, 808,
                RUN, 824,
                WALK, 819,
                WALK_BACKWARD, 820,
                SHUFFLE_LEFT, 821,
                SHUFFLE_RIGHT, 822,
                ROTATE, 823,
                // TODO.
                ATTACK, 8194, // dragon knives. replaces weapon.
                ATTACK, 7617, // other knives.
                ATTACK_SPEC, 8291, // replaces weapon.
                DEFEND, 424
        ));

        animationSets.add(new AnimationSet("Guthan's warspear", false, AnimationSetType.MELEE_SPECIFIC,
                STAND, 813,
                RUN, 1210,
                WALK, 1205,
                WALK_BACKWARD, 1206,
                SHUFFLE_LEFT, 1207,
                SHUFFLE_RIGHT, 1208,
                ROTATE, 1209,
                ATTACK_SLASH, 2081,
                ATTACK_STAB, 2080,
                DEFEND, 430,
                ATTACK_CRUSH, 2082
        ));

        animationSets.add(new AnimationSet("Abyssal whip", false, AnimationSetType.MELEE_SPECIFIC,
                STAND, 808,
                RUN, 1661,
                WALK, 1660,
                WALK_BACKWARD, 1660,
                SHUFFLE_LEFT, 1660,
                SHUFFLE_RIGHT, 1660,
                ROTATE, 823,
                DEFEND, 1659,
                ATTACK_SLASH, 1658
        ));

        animationSets.add(new AnimationSet("Dragon mace", false, AnimationSetType.MELEE_GENERIC,
                STAND, 808,
                RUN, 824,
                WALK, 819,
                WALK_BACKWARD, 820,
                SHUFFLE_LEFT, 821,
                SHUFFLE_RIGHT, 822,
                ROTATE, 823,
                DEFEND, 403,
                ATTACK_STAB, 400,
                ATTACK_CRUSH, 401,
                ATTACK_SPEC, 1060
        ));

        animationSets.add(new AnimationSet("Ancient mace", false, AnimationSetType.MELEE_GENERIC,
                STAND, 808,
                RUN, 824,
                WALK, 819,
                WALK_BACKWARD, 820,
                SHUFFLE_LEFT, 821,
                SHUFFLE_RIGHT, 822,
                ROTATE, 823,
                DEFEND, 403,
                ATTACK_STAB, 400,
                ATTACK_CRUSH, 401,
                ATTACK_SPEC, 6147
        ));

        animationSets.add(new AnimationSet("Dragon warhammer", false, AnimationSetType.MELEE_GENERIC,
                STAND, 808,
                RUN, 824,
                WALK, 819,
                WALK_BACKWARD, 820,
                SHUFFLE_LEFT, 821,
                SHUFFLE_RIGHT, 822,
                ROTATE, 823,
                DEFEND, 403,
                ATTACK_STAB, 400,
                ATTACK_CRUSH, 401,
                ATTACK_SPEC, 1378
        ));

        animationSets.add(new AnimationSet("Dragon axe", false, AnimationSetType.MELEE_GENERIC,
                STAND, 808,
                RUN, 824,
                WALK, 819,
                WALK_BACKWARD, 820,
                SHUFFLE_LEFT, 821,
                SHUFFLE_RIGHT, 822,
                ROTATE, 823,
                ATTACK_STAB, 400,
                ATTACK_CRUSH, 401,
                DEFEND, 397,
                ATTACK_SLASH, 395,
                ATTACK_SPEC, 2876
        ));

        animationSets.add(new AnimationSet("Dragon battleaxe", false, AnimationSetType.MELEE_GENERIC,
                STAND, 808,
                RUN, 824,
                WALK, 819,
                WALK_BACKWARD, 820,
                SHUFFLE_LEFT, 821,
                SHUFFLE_RIGHT, 822,
                ROTATE, 823,
                ATTACK_STAB, 400,
                ATTACK_CRUSH, 401,
                DEFEND, 397,
                ATTACK_SLASH, 395,
                ATTACK_SPEC, 1056
        ));

        animationSets.add(new AnimationSet("Spear", false, AnimationSetType.MELEE_GENERIC,
                STAND, 813,
                RUN, 1210,
                WALK, 1205,
                WALK_BACKWARD, 1206,
                SHUFFLE_LEFT, 1207,
                SHUFFLE_RIGHT, 1208,
                ROTATE, 1209,
                // TODO is hasta a different spec animation?
                ATTACK_SPEC, 1064,
                ATTACK_SLASH, 380,
                ATTACK_STAB, 381,
                ATTACK_CRUSH, 382,
                DEFEND, 383
        ));

        animationSets.add(new AnimationSet("Snowball", false, AnimationSetType.RANGED_FUN,
                STAND, 808,
                RUN, 824,
                WALK, 819,
                WALK_BACKWARD, 820,
                SHUFFLE_LEFT, 821,
                SHUFFLE_RIGHT, 822,
                ROTATE, 823,
                DEFEND, 424,
                ATTACK, 5063
        ));

        animationSets.add(new AnimationSet("Sled", false, AnimationSetType.FUN,
                STAND, 1461,
                RUN, 8853,
                WALK, 8854,
                WALK_BACKWARD, 1468,
                SHUFFLE_LEFT, 1468,
                SHUFFLE_RIGHT, 1468,
                ROTATE, 1468
        ));

        animationSets.add(new AnimationSet("Claws", false, AnimationSetType.MELEE_GENERIC,
                STAND, 808,
                RUN, 824,
                WALK, 819,
                WALK_BACKWARD, 820,
                SHUFFLE_LEFT, 821,
                SHUFFLE_RIGHT, 822,
                ROTATE, 823,
                ATTACK_STAB, 1067,
                DEFEND, 424,
                ATTACK_SLASH, 393,
                ATTACK_SPEC, 7514
        ));

        animationSets.add(new AnimationSet("Verac's flail", false, AnimationSetType.MELEE_SPECIFIC,
                STAND, 2061,
                RUN, 824,
                WALK, 2060,
                WALK_BACKWARD, 2060,
                SHUFFLE_LEFT, 2060,
                SHUFFLE_RIGHT, 2060,
                ROTATE, 823,
                ATTACK_CRUSH, 2062,
                DEFEND, 2063
        ));

        animationSets.add(new AnimationSet("Abyssal bludgeon", false, AnimationSetType.MELEE_SPECIFIC,
                STAND, 1652,
                RUN, 2847,
                WALK, 3293,
                WALK_BACKWARD, 3293,
                SHUFFLE_LEFT, 3293,
                SHUFFLE_RIGHT, 3293,
                ROTATE, 823,
                DEFEND, 1666,
                ATTACK_CRUSH, 3298,
                ATTACK_SPEC, 3299
        ));

        animationSets.add(new AnimationSet("Karil's crossbow", false, AnimationSetType.RANGED,
                STAND, 2074,
                RUN, 2077,
                WALK, 2076,
                WALK_BACKWARD, 2076,
                SHUFFLE_LEFT, 2076,
                SHUFFLE_RIGHT, 2076,
                ROTATE, 823,
                DEFEND, 424,
                ATTACK, 2075
        ));

        animationSets.add(new AnimationSet("Abyssal dagger", false, AnimationSetType.MELEE_SPECIFIC,
                STAND, 3296,
                RUN, 824,
                WALK, 819,
                WALK_BACKWARD, 820,
                SHUFFLE_LEFT, 821,
                SHUFFLE_RIGHT, 822,
                ROTATE, 823,
                ATTACK_SLASH, 3294,
                DEFEND, 3295,
                ATTACK_STAB, 3297,
                ATTACK_SPEC, 3300
        ));

        animationSets.add(new AnimationSet("Torag's hammers", false, AnimationSetType.MELEE_SPECIFIC,
                STAND, 808,
                RUN, 824,
                WALK, 819,
                WALK_BACKWARD, 820,
                SHUFFLE_LEFT, 821,
                SHUFFLE_RIGHT, 822,
                ROTATE, 823,
                ATTACK_CRUSH, 2068,
                DEFEND, 424
        ));

        animationSets.add(new AnimationSet("Zamorakian spear", false, AnimationSetType.MELEE_SPECIFIC,
                STAND, 1713,
                RUN, 1707,
                WALK, 1703,
                WALK_BACKWARD, 1704,
                SHUFFLE_LEFT, 1706,
                SHUFFLE_RIGHT, 1705,
                ROTATE, 1702,
                DEFEND, 1709,
                ATTACK_SPEC, 1064,
                ATTACK_CRUSH, 1710,
                ATTACK_STAB, 1711,
                ATTACK_SLASH, 1712
        ));

        animationSets.add(new AnimationSet("Leaf-bladed battleaxe", false, AnimationSetType.MELEE_SPECIFIC,
                STAND, 808,
                RUN, 824,
                WALK, 819,
                WALK_BACKWARD, 820,
                SHUFFLE_LEFT, 821,
                SHUFFLE_RIGHT, 822,
                ROTATE, 823,
                ATTACK_CRUSH, 3852,
                DEFEND, 397,
                ATTACK_SLASH, 7004
        ));

        animationSets.add(new AnimationSet("Dragon sword", false, AnimationSetType.MELEE_GENERIC,
                STAND, 808,
                RUN, 824,
                WALK, 819,
                WALK_BACKWARD, 820,
                SHUFFLE_LEFT, 821,
                SHUFFLE_RIGHT, 822,
                ROTATE, 823,
                ATTACK_STAB, 386,
                DEFEND, 388,
                ATTACK_SLASH, 390,
                ATTACK_SPEC, 7515
        ));

        animationSets.add(new AnimationSet("Arclight", false, AnimationSetType.MELEE_GENERIC,
                STAND, 808,
                RUN, 824,
                WALK, 819,
                WALK_BACKWARD, 820,
                SHUFFLE_LEFT, 821,
                SHUFFLE_RIGHT, 822,
                ROTATE, 823,
                ATTACK_STAB, 386,
                DEFEND, 388,
                ATTACK_SLASH, 390,
                ATTACK_SPEC, 2890
        ));

        animationSets.add(new AnimationSet("Dragon dagger", false, AnimationSetType.MELEE_SPECIFIC,
                STAND, 808,
                RUN, 824,
                WALK, 819,
                WALK_BACKWARD, 820,
                SHUFFLE_LEFT, 821,
                SHUFFLE_RIGHT, 822,
                ROTATE, 823,
                ATTACK_SPEC, 1062,
                ATTACK_STAB, 376,
                ATTACK_SLASH, 377,
                DEFEND, 378
        ));

        Collections.sort(animationSets);
    }

    public String name;
    public boolean useWalkOrRunForShuffleAndWalkBackwards; // TODO do I need this?
    // TODO this doesn't make sense as a multimap for when you use this class as the set of things to actually replace and not as data containing weapon animations.
    public final Multimap<AnimationType, Animation> animations = MultimapBuilder.hashKeys().arrayListValues().build();

    AnimationSet() {
        this.name = "";
        this.animationSetType = AnimationSetType.MELEE_GENERIC;
        this.useWalkOrRunForShuffleAndWalkBackwards = false;
    }

    @Data
    public static final class Animation {
        public final AnimationType type;
        public final int id;
        public final transient String description;

//        public String getDescription() {
//            return description == null ? type.prettyName : description;
//        }
    }

    AnimationSet(String name, boolean useWalkOrRunForShuffleAndWalkBackwards, AnimationSetType animationSetType, Object... data) {
//        if (data.length % 2 != 0) throw new IllegalArgumentException("requires pairs of data, but the number is odd");
        this.name = name;
        this.animationSetType = animationSetType;
        this.useWalkOrRunForShuffleAndWalkBackwards = useWalkOrRunForShuffleAndWalkBackwards;
        for (int i = 0; i < data.length; ) {
            if (!(data[i] instanceof AnimationType)) throw new IllegalArgumentException("first item in pair must be an AnimationType");
            if (data[i + 1] != null && !(data[i + 1] instanceof Integer)) throw new IllegalArgumentException("second item in pair must be an Integer");
            AnimationType type = (AnimationType) data[i];
            Integer animationId = (Integer) data[i + 1];
            if (i + 2 < data.length && data[i + 2] instanceof String) {
                String description = (String) data[i + 2];
                animations.put(type, new Animation(type, animationId, description));
                i += 3;
            } else {
                animations.put(type, new Animation(type, animationId, null));
                i += 2;
            }
        }
    }

    private Integer getInt(AnimationType type) {
        Collection<Animation> anims = animations.get(type);
        return anims.isEmpty() ? null : anims.iterator().next().id;
    }

	public void copy(AnimationSet currentAnimationSet) {
        animations.putAll(currentAnimationSet.animations);
        this.useWalkOrRunForShuffleAndWalkBackwards = currentAnimationSet.useWalkOrRunForShuffleAndWalkBackwards;
    }

    public void applyReplacement(Swap.AnimationReplacement replacement) {
        replaceAnimations(
                replacement.getAnimationSet(),
                replacement.getAnimationtypeToReplace(),
                replacement.animationtypeReplacement
        );
    }

    private void replaceAnimations(AnimationSet animationSet, AnimationType toReplace, Animation replacement) {
        AnimationType type = replacement == null ? toReplace : replacement.type;
        if (toReplace.children == null || toReplace.children.isEmpty()) {
            animations.removeAll(toReplace);
            if (replacement != null) {
                animations.put(toReplace, replacement);
            } else {
				Integer id = animationSet.getAnimation(type);
				if (id != null) {
                    animations.put(toReplace, new Animation(type, id, null));
                }
            }
        } else {
            for (AnimationType child : toReplace.children) {
                replaceAnimations(animationSet, child, replacement);
            }
        }
    }

	public Integer getAnimation(AnimationType type) {
		switch (type) {
			case STAND:
			case WALK:
			case ROTATE:
			case RUN:
			case DEFEND:
			case WALK_BACKWARD:
			case SHUFFLE_LEFT:
			case SHUFFLE_RIGHT:
				return getInt(type);
			default:
				if (ATTACK.appliesTo(type)) {
					if (type != ATTACK)
					{
						Integer attack = getInt(type);
						if (attack != null) return attack;
					}

					List<Integer> attacks = animations.entries().stream()
						.filter(entry -> ATTACK.appliesTo(entry.getKey()) && entry.getValue() != null) // TODO why is the value sometimes null?
						.map(entry -> entry.getValue().id).collect(Collectors.toList());
					if (attacks.isEmpty()) {
						return null;
					} else {
						return attacks.get(0);
					}
				}
		}
		return null;
	}

	public Integer getAnimation(AnimationType type, boolean isRunning) {
		switch (type) {
			case WALK_BACKWARD:
			case SHUFFLE_LEFT:
			case SHUFFLE_RIGHT:
				if (useWalkOrRunForShuffleAndWalkBackwards) {
					if (getInt(RUN) == null) {
						return getInt(WALK);
					}
					return (type != WALK_BACKWARD && isRunning) ? getInt(RUN) : getInt(WALK);
				}
				return getInt(type);
			default:
				return getAnimation(type);
        }
    }

    public AnimationType getType(int animationId) {
        for (Map.Entry<AnimationType, Animation> entry : animations.entries()) {
            Animation animation = entry.getValue();
            if (animation != null && animation.id == animationId) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name + " " + animations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnimationSet that = (AnimationSet) o;
        return useWalkOrRunForShuffleAndWalkBackwards == that.useWalkOrRunForShuffleAndWalkBackwards && Objects.equals(name, that.name) && Objects.equals(animations, that.animations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, useWalkOrRunForShuffleAndWalkBackwards, animations);
    }

    public String getComboBoxName() {
        return name;
    }
}
