package com.weaponanimationreplacer;

import com.weaponanimationreplacer.AnimationReplacementRule.AnimationType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.weaponanimationreplacer.AnimationReplacementRule.AnimationType.*;

public class AnimationSet {
    public static final List<AnimationSet> animationSets = new ArrayList<>();

    static {
        animationSets.add(new AnimationSet("default",false
        ));

        animationSets.add(new AnimationSet("unarmed",false,
                     STAND, 808,
                     WALK, 819,
                     WALK_BACKWARD, 820,
                     SHUFFLE_LEFT, 821,
                     SHUFFLE_RIGHT, 822,
                     ROTATE, 823,
                     RUN, 824,
                     DEFEND, 424
        ));

        animationSets.add(new AnimationSet("halberd",false,
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

        animationSets.add(new AnimationSet("dart",false,
                STAND, 808,
                WALK, 819,
                WALK_BACKWARD, 820,
                SHUFFLE_LEFT, 821,
                SHUFFLE_RIGHT, 822,
                ROTATE, 823,
                RUN, 824,
                ATTACK_STAB, 7554,
                ATTACK_SLASH, 440,
                ATTACK_SPEC, 1203,
                DEFEND, 424
        ));

        animationSets.add(new AnimationSet("blowpipe",false,
                STAND, 808,
                WALK, 819,
                WALK_BACKWARD, 820,
                SHUFFLE_LEFT, 821,
                SHUFFLE_RIGHT, 822,
                ROTATE, 823,
                RUN, 824,
                ATTACK_STAB, 5061,
                ATTACK_SLASH, 440,
                ATTACK_SPEC, 1203,
                DEFEND, 424
        ));

        animationSets.add(new AnimationSet("bow",false,
                STAND, 808,
                WALK, 819,
                WALK_BACKWARD, 820,
                SHUFFLE_LEFT, 821,
                SHUFFLE_RIGHT, 822,
                ROTATE, 823,
                RUN, 824,
                ATTACK_STAB, 426,
                ATTACK_SLASH, 440,
                ATTACK_SPEC, 1203,
                DEFEND, 424
        ));

        animationSets.add(new AnimationSet("xbow",false,
                STAND, 808,
                WALK, 819,
                WALK_BACKWARD, 820,
                SHUFFLE_LEFT, 821,
                SHUFFLE_RIGHT, 822,
                ROTATE, 823,
                RUN, 824,
                ATTACK_STAB, 7552,
                ATTACK_SLASH, 440,
                ATTACK_SPEC, 1203,
                DEFEND, 424
        ));

        animationSets.add(new AnimationSet("snowball",false,
                STAND, 808,
                WALK, 819,
                WALK_BACKWARD, 820,
                SHUFFLE_LEFT, 821,
                SHUFFLE_RIGHT, 822,
                ROTATE, 823,
                RUN, 824,
                ATTACK_STAB, 5063,
                ATTACK_SLASH, 440,
                ATTACK_SPEC, 1203,
                DEFEND, 424
        ));

        animationSets.add(new AnimationSet("banana",false,
                     STAND, 4646,
                     WALK, 4682,
                     WALK_BACKWARD, 6276,
                     SHUFFLE_LEFT, 6275,
                     SHUFFLE_RIGHT, 6268,
                     RUN, 6277,
                     ATTACK_SLASH, 6278,
                     DEFEND, 6279
        ));

        animationSets.add(new AnimationSet("chalice",false,
                     STAND, 3040,
                     WALK, 3039
        ));

        animationSets.add(new AnimationSet("dharok",true,
                     STAND, 2065,
                     WALK, 2064
        ));

        animationSets.add(new AnimationSet("whip",false,
                     WALK, 1660,
                     RUN, 1661,
                     ATTACK_SLASH, 1658,
                     DEFEND, 1659
        ));

        animationSets.add(new AnimationSet("rapier",false,
                     STAND, 809,
                     DEFEND, 388,
                     ATTACK_STAB, 8145,
                     ATTACK_SLASH, 390
        ));

        animationSets.add(new AnimationSet("ballista",true,
                     STAND, 7220,
                     WALK, 7223,
                     RUN, 7221,
                     DEFEND, 7219
                     // 7222 is the attack animation
        ));

        animationSets.add(new AnimationSet("anchor",true,
                     STAND, 5869,
                     WALK, 5867,
                     RUN, 5868
        ));

        animationSets.add(new AnimationSet("basket",false,
                     STAND, 1837,
                     WALK, 1836,
                     WALK_BACKWARD, 1836,
                     SHUFFLE_LEFT, 1836,
                     SHUFFLE_RIGHT, 1836,
                     RUN, 1836
        ));

        animationSets.add(new AnimationSet("trophy",true,
                     STAND, 4193,
                     WALK, 4194,
                     ROTATE, 4194,
                     RUN, 7274,
                     DEFEND, 403
        ));

        animationSets.add(new AnimationSet("spooky",true,
                     STAND, 8521,
                     WALK, 8492,
                     ROTATE, 8492,
                     RUN, 8492
        ));

        animationSets.add(new AnimationSet("eventrpg",false,
                     STAND, 2316,
                     WALK, 2317,
                     WALK_BACKWARD, 2318,
                     SHUFFLE_LEFT, 2319,
                     SHUFFLE_RIGHT, 2320,
                     ROTATE, 2321,
                     RUN, 2322
        ));

        animationSets.add(new AnimationSet("noosewand",false,
                     STAND, 5254,
                     WALK, 5250,
                     WALK_BACKWARD, 1206,
                     SHUFFLE_LEFT, 1207,
                     SHUFFLE_RIGHT, 1208,
                     ROTATE, 5252,
                     RUN, 5253
        ));

        animationSets.add(new AnimationSet("crossbow",false,
                     STAND, 4591,
                     WALK, 4226,
                     RUN, 4228
        ));

        animationSets.add(new AnimationSet("gadderhammer",true,
                     STAND, 1662,
                     WALK, 1663,
                     RUN, 1664,
                ATTACK_CRUSH, 1665
        ));

        animationSets.add(new AnimationSet("ratpole",false,
                     STAND, 1421,
                     WALK, 1422,
                     WALK_BACKWARD, 1423,
                     SHUFFLE_LEFT, 1424,
                     SHUFFLE_RIGHT, 1425,
                     ROTATE, 1426,
                     RUN, 1427
        ));

        animationSets.add(new AnimationSet("chin",true,
                     STAND, 3175,
                     WALK, 3177,
                     ROTATE, 3177,
                     RUN, 3178
        ));

        animationSets.add(new AnimationSet("scythe",false,
                     STAND, 8057,
                     ATTACK_SLASH, 8056,
                     ATTACK_CRUSH, 8056
        ));

        animationSets.add(new AnimationSet("claws",false,
                     STAND, 8057,
                     ATTACK_SLASH, 393,
                     ATTACK_STAB, 1067,
                     ATTACK_SPEC, 7514
        ));

        animationSets.add(new AnimationSet("maul",true,
                     STAND, 7518,
                     WALK, 7520,
                     RUN, 7519,
                     ATTACK_CRUSH, 7516,
                     DEFEND, 7517
        ));

        animationSets.add(new AnimationSet("bulwhark",false,
                     STAND, 7508,
                     WALK, 7510,
                     RUN, 7509,
                     ATTACK_CRUSH, 7511,
                     ATTACK_SPEC, 7512,
                     DEFEND, 7517
        ));

        animationSets.add(new AnimationSet("dhl",false,
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

        animationSets.add(new AnimationSet("godsword",false,
                     // TODO what is 7055?
                     // What is 7046?
                     STAND, 7053,
                     WALK, 7052,
                     // TODO needs a walk backwards animation.
                     SHUFFLE_LEFT, 7047,
                     SHUFFLE_RIGHT, 7048,
                     RUN, 7043,
                     ATTACK_SLASH, 7045,
                     ATTACK_CRUSH, 7054,
                     DEFEND, 7056
        ));

        animationSets.add(new AnimationSet("boxing gloves", false,
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

        animationSets.add(new AnimationSet("Hand fan", false,
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

        animationSets.add(new AnimationSet("Prop sword", false,
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

        animationSets.add(new AnimationSet("staff", false,
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

        animationSets.add(new AnimationSet("hasta", false,
                STAND, 813,
                RUN, 1210,
                WALK, 1205,
                WALK_BACKWARD, 1206,
                SHUFFLE_LEFT, 1207,
                SHUFFLE_RIGHT, 1208,
                ROTATE, 1209,
                ATTACK_STAB, 381,
                DEFEND, 420,
                ATTACK_SLASH, 440,
                ATTACK_CRUSH, 419
        ));

        animationSets.add(new AnimationSet("hasta (with offhand)", false,
                STAND, 813,
                RUN, 1210,
                WALK, 1205,
                WALK_BACKWARD, 1206,
                SHUFFLE_LEFT, 1207,
                SHUFFLE_RIGHT, 1208,
                ROTATE, 1209,
                ATTACK_STAB, 381,
                DEFEND, 420,
                ATTACK_SLASH, 440,
                ATTACK_CRUSH, 393
        ));

        animationSets.add(new AnimationSet("Falconer's glove", false,
                STAND, 5160,
                RUN, 5168,
                WALK, 5164,
                WALK_BACKWARD, 5165,
                SHUFFLE_LEFT, 5166,
                SHUFFLE_RIGHT, 5167,
                ROTATE, 5161
        ));
    }

    public String name;
    public boolean useWalkOrRunForShuffleAndWalkBackwards; // TODO do I need this?
    public final Map<AnimationType, Integer> animations = new HashMap<>();

    AnimationSet() {
        this.name = "";
        this.useWalkOrRunForShuffleAndWalkBackwards = false;
    }

    AnimationSet(String name, boolean useWalkOrRunForShuffleAndWalkBackwards, Object... data) {
        if (data.length % 2 != 0) throw new IllegalArgumentException("requires pairs of data, but the number is odd");
        this.name = name;
        this.useWalkOrRunForShuffleAndWalkBackwards = useWalkOrRunForShuffleAndWalkBackwards;
        for (int i = 0; i < data.length; i += 2) {
            if (!(data[i] instanceof AnimationType)) throw new IllegalArgumentException("first item in pair must be an AnimationType");
            if (data[i + 1] != null && !(data[i + 1] instanceof Integer)) throw new IllegalArgumentException("second item in pair must be an Integer");
            AnimationType type = (AnimationType) data[i];
            Integer animationId = (Integer) data[i + 1];
            animations.put(type, animationId);
        }
    }

    Integer getStand() {
        Integer anim = animations.get(STAND);
        return anim; // TODO stop returning defaults.
//        return (anim == null) ? 808 : anim;
    }

    Integer getWalk() {
        Integer anim = animations.get(WALK);
        return anim;
//        return (anim == null) ? 819 : anim;
    }

    Integer getWalkBackwards(boolean isRunning) {
        if (useWalkOrRunForShuffleAndWalkBackwards) {
            if (animations.get(RUN) == null) {
                return getWalk();
            }
            return isRunning ? getRun() : getWalk();
        }
        return animations.get(WALK_BACKWARD);
//        return (animations.get(WALK_BACKWARD) == null) ? 820 : animations.get(WALK_BACKWARD);
    }

    Integer getShuffleLeft(boolean isRunning) {
        if (useWalkOrRunForShuffleAndWalkBackwards) {
            if (animations.get(RUN) == null) {
                return getWalk();
            }
            return isRunning ? getRun() : getWalk();
        }
        return animations.get(SHUFFLE_LEFT);
//        return (animations.get(SHUFFLE_LEFT) == null) ? 821 : animations.get(SHUFFLE_LEFT);
    }

    Integer getShuffleRight(boolean isRunning) {
        if (useWalkOrRunForShuffleAndWalkBackwards) {
            if (animations.get(RUN) == null) {
                return getWalk();
            }
            return isRunning ? getRun() : getWalk();
        }
        return animations.get(SHUFFLE_RIGHT);
//        return (animations.get(SHUFFLE_RIGHT) == null) ? 822 : animations.get(SHUFFLE_RIGHT);
    }

    Integer getRotate() {
        Integer anim = animations.get(ROTATE);
        return anim;
//        return (anim == null) ? 823 : anim;
    }

    Integer getRun() {
        Integer anim = animations.get(RUN);
        return anim;
//        return (anim == null) ? 824 : anim;
    }

    Integer getDefend() {
        return animations.get(DEFEND);
//        return (anim == null) ? 424 : anim;
    }

    Integer getAttack() {
        List<Integer> attacks = animations.entrySet().stream().filter(entry -> ATTACK.appliesTo(entry.getKey())).map(entry -> entry.getValue()).collect(Collectors.toList());
        if (attacks.isEmpty()) {
            return null;
        }
        return attacks.get(0);
    }

    Integer getAttack(AnimationType type) {
        Integer animation = animations.get(type); // TODO lol, this works for any animation type.
        return (animation != null) ? animation : null;
    }

    public void copy(AnimationSet currentAnimationSet) {
        animations.putAll(currentAnimationSet.animations);
        this.useWalkOrRunForShuffleAndWalkBackwards = currentAnimationSet.useWalkOrRunForShuffleAndWalkBackwards;
    }

    public void applyReplacement(AnimationReplacementRule.AnimationReplacement replacement) {
        replaceAnimations(replacement.getAnimationSet(), replacement.getAnimationtypeToReplace(), replacement.getAnimationtypeReplacement());
    }

    public void replaceAnimations(AnimationSet animationSet, AnimationType toReplace, AnimationType replacement) {
        if (toReplace.children == null || toReplace.children.isEmpty()) {
            animations.put(toReplace, animationSet.animations.get(replacement));
        } else {
            for (AnimationType child : toReplace.children) {
                replaceAnimations(animationSet, child, replacement.children.isEmpty() ? replacement : child);
            }
        }
    }

    public Integer getAnimation(AnimationType type, boolean isRunning) {
        switch (type) {
            case STAND:
                return getStand();
            case WALK:
                return getWalk();
            case WALK_BACKWARD:
                return getWalkBackwards(false);
            case SHUFFLE_LEFT:
                return getShuffleLeft(isRunning);
            case SHUFFLE_RIGHT:
                return getShuffleRight(isRunning);
            case ROTATE:
                return getRotate();
            case RUN:
                return getRun();
            case DEFEND:
                return getDefend();
            case ATTACK_STAB:
            case ATTACK_SLASH:
            case ATTACK_CRUSH:
            case ATTACK_SPEC:
                Integer attack = animations.get(type);
                System.out.println("1");
                if (attack != null) return attack;
            case ATTACK:
                System.out.println("1.1");
                List<Integer> attacks = animations.entrySet().stream()
                        .filter(entry -> ATTACK.appliesTo(entry.getKey()) && entry.getValue() != null) // TODO why is the value sometimes null?
                        .map(entry -> entry.getValue()).collect(Collectors.toList());
                System.out.println("attacks is " + attacks);
                if (attacks.isEmpty()) {
                    System.out.println("2");
                    return null;
                } else {
                    System.out.println("3");
                    return attacks.get(0);
                }
        }
        return null;
    }

    public AnimationType getType(int animationId) {
        for (Map.Entry<AnimationType, Integer> animationTypeIntegerEntry : animations.entrySet()) {
            Integer value = animationTypeIntegerEntry.getValue();
            if (value != null && value == animationId) {
                return animationTypeIntegerEntry.getKey();
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
