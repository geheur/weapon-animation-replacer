package com.weaponanimationreplacer;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a set of:
 *  Configurable name to describe what this does.
 *  List of weapon slot items that this should be used with. If there are none, it applies always.
 *  List of animation replacements, which each contain an animation type to replace, and animation set to use for replacement, and, optionally, an animation type to use as the replacement (if it is different from the one being replaced).
 */
public class AnimationReplacementRule {
    String name = "";
    boolean modelSwapEnabled = false;
    boolean enabled = true;
    public boolean minimized = false;
    public int modelSwap = -1;
    public List<ItemRestriction> itemRestrictions;
    public List<AnimationReplacement> animationReplacements;

    public AnimationReplacementRule(List<ItemRestriction> itemRestrictions, List<AnimationReplacement> animationReplacements) {
        this.itemRestrictions = itemRestrictions;
        this.animationReplacements = animationReplacements;
    }

    public static AnimationReplacementRule createTemplate(AnimationSet animationSet) {
        List<ItemRestriction> itemRestrictions = new ArrayList<>();
        List<AnimationReplacement> animationReplacements = new ArrayList<>();
//        animationReplacements.add(new AnimationReplacement(animationSet, AnimationType.ALL, null, false));
        AnimationReplacementRule animationReplacementRule = new AnimationReplacementRule(itemRestrictions, animationReplacements);
        animationReplacementRule.name = "New Replacement";
        return animationReplacementRule;
    }

    public boolean appliesToItem(int weaponItemId) {
        return itemRestrictions.stream().filter(r -> r.enabled).count() == 0 || itemRestrictions.stream().filter(r -> r.appliesToItem(weaponItemId)).findAny().isPresent();
    }

    public boolean appliesSpecificallyToItem(int weaponItemId) {
        return itemRestrictions.stream().filter(r -> r.appliesToItem(weaponItemId)).findAny().isPresent();
    }

    public boolean isModelSwapEnabled() {
        return enabled;// && modelSwapEnabled;
    }

    @Data
    public static class ItemRestriction {
        boolean enabled;
        int itemId;

        public ItemRestriction(int itemId) {
            this(itemId, true);
        }

        public ItemRestriction(int itemId, boolean enabled) {
            this.itemId = itemId;
            this.enabled = enabled;
        }

        public boolean appliesToItem(int itemId) {
            return enabled && itemId == this.itemId;
        }
    }

    @Data
    public static class AnimationReplacement implements Comparable<AnimationReplacement> {
        boolean enabled;
        public AnimationSet animationSet;
        public AnimationType animationtypeToReplace;
        public AnimationSet.Animation animationtypeReplacement;

        public AnimationType getAnimationtypeReplacement() {
            return animationtypeReplacement == null ? getAnimationtypeToReplace() : animationtypeReplacement.type;
        }

        public AnimationReplacement(AnimationSet animationSet, AnimationType animationtypeToReplace, AnimationType animationtypeReplacement) {
            this(animationSet, animationtypeToReplace, animationtypeReplacement, true);
        }

        public AnimationReplacement(AnimationSet animationSet, AnimationType animationtypeToReplace, AnimationType animationtypeReplacement, boolean enabled) {
            this.animationSet = animationSet;
            this.animationtypeToReplace = animationtypeToReplace;
            Integer animation = animationSet == null ? -1 : animationSet.getAnimation(animationtypeReplacement, false);
            if (animation == null) animation = -1;
            this.animationtypeReplacement = new AnimationSet.Animation(
                    animationtypeReplacement,
                    animation,
                    null
            );
            this.enabled = enabled;
        }

        @Override
        public int compareTo(AnimationReplacement o) {
            return animationtypeToReplace.compareTo(o.animationtypeToReplace);
        }

        @Override
        public String toString() {
            return (animationSet == null ? null : animationSet.name) + " " + animationtypeToReplace + " " + animationtypeReplacement;
        }

        public boolean isActive() {
            return enabled && animationtypeToReplace != null && animationSet != null;
        }
    }

    /**
     * Ordered by specificity.
     *
     * TODO this is kinda gross - combination categories at the same logical level as animations.
     */
    public enum AnimationType {
        // Order matters - it is used to determine specificity (more specific enums earlier in the list).
        STAND, WALK, RUN, WALK_BACKWARD, SHUFFLE_LEFT, SHUFFLE_RIGHT, ROTATE, ATTACK_STAB("Stab"),
        ATTACK_SLASH("Slash"), ATTACK_CRUSH("Crush"), ATTACK_SPEC("Special"), DEFEND,
        ATTACK_CRUSH2("Crush2"),

//        MAGIC_LOW_LEVEL_SPELL_UNARMED, MAGIC_LOW_LEVEL_SPELL_STAFF, // strike,bolt,blast spells.
//        MAGIC_UNARMED_WAVE_SPELL, MAGIC_STAFF_WAVE_SPELL,
//        MAGIC_GOD_SPELL,
//        MAGIC_CRUMBLE_UNDEAD_UNARMED, MAGIC_CRUMBLE_UNDEAD_STAFF,
//        MAGIC_CONFUSE_UNARMED, MAGIC_CONFUSE_STAFF,
//        MAGIC_WEAKEN_UNARMED, MAGIC_WEAKEN_STAFF,
//        MAGIC_CURSE_VULNERABILITY_UNARMED, MAGIC_CURSE_VULNERABILITY_STAFF,
//        MAGIC_ENFEEBLE_UNARMED, MAGIC_ENFEEBLE_STAFF,
//        MAGIC_STUN_UNARMED, MAGIC_STUN_STAFF,
//        MAGIC_BIND_UNARMED, MAGIC_BIND_STAFF,
//        MAGIC_ANCIENT_SINGLE_TARGET,
//        MAGIC_ANCIENT_MULTI_TARGET,

        ATTACK(ATTACK_STAB, ATTACK_SLASH, ATTACK_CRUSH, ATTACK_CRUSH2, ATTACK_SPEC),
        MAGIC(),
        MOVEMENT(WALK, RUN, WALK_BACKWARD, SHUFFLE_LEFT, SHUFFLE_RIGHT, ROTATE),
        STANCE(STAND, MOVEMENT),
        ALL(ATTACK, STANCE);

        public static final List<AnimationType> comboBoxOrder = new ArrayList<>();
        static {
            comboBoxOrder.add(ALL);
            comboBoxOrder.add(STANCE);
            comboBoxOrder.add(STAND);
            comboBoxOrder.add(MOVEMENT);
            addItems(MOVEMENT, comboBoxOrder);
            comboBoxOrder.add(ATTACK);
            addItems(ATTACK, comboBoxOrder);
        }

        private static void addItems(AnimationType animationType, List<AnimationType> comboBoxOrder) {
            animationType.children.forEach(comboBoxOrder::add);
        }

        public final List<AnimationType> children;
        public final String prettyName;

        AnimationType(AnimationType... children) {
            this(null, children);
        }

        AnimationType(String prettyName, AnimationType... children) {
            String s = toString().replaceAll("_", " ");
            this.prettyName = prettyName != null ? prettyName : s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase();
            this.children = Arrays.asList(children);
        }

        public boolean appliesTo(AnimationType type) {
            return this == type || (children != null && children.stream().anyMatch(child -> child.appliesTo(type)));
        }

        public boolean hasChild(AnimationType type) {
            return (children != null && children.stream().anyMatch(child -> child.appliesTo(type)));
        }

        public String getComboBoxName() {
            int depth = 0;
            if (ATTACK.hasChild(this)) {
                depth = 2;
            } else if (MOVEMENT.hasChild(this)) {
                depth = 3;
            } else if (STANCE.hasChild(this)) {
                depth = 3;
            } else if (ALL.hasChild(this)) {
                depth = 1;
            }
            String s = prettyName;
            for (int i = 0; i < depth; i++) {
                s = " " + s;
            }
            return s;
        }
    }

    @Override
    public String toString() {
        return enabled + " " + itemRestrictions + " " + modelSwap + " " + modelSwapEnabled + " " + animationReplacements;
    }
}
