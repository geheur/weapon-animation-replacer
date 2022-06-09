package com.weaponanimationreplacer;

import com.google.common.collect.ImmutableSet;
import static com.weaponanimationreplacer.Swap.AnimationType.ROTATE;
import static com.weaponanimationreplacer.Swap.AnimationType.SHUFFLE_LEFT;
import static com.weaponanimationreplacer.Swap.AnimationType.SHUFFLE_RIGHT;
import static com.weaponanimationreplacer.Swap.AnimationType.STAND;
import static com.weaponanimationreplacer.Swap.AnimationType.WALK_BACKWARD;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Actor;
import net.runelite.api.ItemID;
import net.runelite.api.kit.KitType;
import static net.runelite.api.kit.KitType.SHIELD;
import static net.runelite.api.kit.KitType.WEAPON;

public class Constants
{
	public static final Set<Integer> JAW_SLOT = ImmutableSet.of(10556, 10557, 10558, 10559, 10567, 20802, 22308, 22309, 22310, 22311, 22312, 22313, 22314, 22315, 22337, 22338, 22339, 22340, 22341, 22342, 22343, 22344, 22345, 22346, 22347, 22348, 22349, 22721, 22722, 22723, 22724, 22725, 22726, 22727, 22728, 22729, 22730, 23460, 23461, 23462, 23463, 23464, 23465, 23466, 23467, 23468, 23469, 23470, 23471, 23472, 23473, 23474, 23475, 23476, 23477, 23478, 23479, 23480, 23481, 23482, 23483, 23484, 23485, 23486, 25228, 25229, 25230, 25231, 25232, 25233, 25234, 25235, 25236, 25237, 25238, 25239, 25240, 25241, 25242, 25243, 25212, 25213, 25214, 25215, 25216, 25217, 25218, 25219, 25220, 25221, 25222, 25223, 25224, 25225, 25226, 25227);

	// key is item id, value is the slot id it should go in.
	public static final Map<Integer, Integer> OVERRIDE_EQUIPPABILITY_OR_SLOT = new HashMap<>();
	static {
		loadEquippableItemsNotMarkedAsEquippable();
	}

	public static void loadEquippableItemsNotMarkedAsEquippable()
	{
		OVERRIDE_EQUIPPABILITY_OR_SLOT.clear();

		addUnequippable(1963, WEAPON); // banana (right-handed)
		addUnequippable(3177, SHIELD); // banana (left-handed)
		addUnequippable(26649, SHIELD); // skis (I want them in the shield slot not the weapon slot. They are marked as equippable though).
		addUnequippable(27021, WEAPON); // Colossal blade

		// leagues 3 items.
		addUnequippable(26503, WEAPON); // Shattered relics bronze trophy
		addUnequippable(26505, WEAPON); // Shattered relics iron trophy
		addUnequippable(26507, WEAPON); // Shattered relics steel trophy
		addUnequippable(26509, WEAPON); // Shattered relics mithril trophy
	}

	private static void addUnequippable(int itemId, KitType kitType) {
		OVERRIDE_EQUIPPABILITY_OR_SLOT.put(itemId, kitType.getIndex());
	}

	public static final Set<Integer> showSleeves = new HashSet<>();

	public static Constants.NegativeId mapNegativeId(int id) {
		if (id == -1) return new Constants.NegativeId(Constants.NegativeIdsMap.NULL, -1);
		else if (id <= -2 && id > -2 - KitType.values().length) {
			return new Constants.NegativeId(Constants.NegativeIdsMap.HIDE_SLOT, id * -1 - 2);
		}
		else if (id == -14) {
			return new Constants.NegativeId(Constants.NegativeIdsMap.SHOW_SLOT, KitType.ARMS.getIndex());
		}
		else if (id == -15) {
			return new Constants.NegativeId(Constants.NegativeIdsMap.SHOW_SLOT, KitType.HAIR.getIndex());
		}
		else if (id == -16) {
			return new Constants.NegativeId(Constants.NegativeIdsMap.SHOW_SLOT, KitType.JAW.getIndex());
		}
		throw new IllegalArgumentException();
	}

	public static int mapNegativeId(Constants.NegativeId id) {
		if (id.type == Constants.NegativeIdsMap.NULL) return -1;
		else if (id.type == Constants.NegativeIdsMap.HIDE_SLOT) {
			return -2 - id.id;
		}
		else if (id.type == Constants.NegativeIdsMap.SHOW_SLOT) {
			if (id.id == KitType.ARMS.getIndex()) {
				return -14;
			}
			else if (id.id == KitType.HAIR.getIndex()) {
				return -15;
			}
			else if (id.id == KitType.JAW.getIndex()) {
				return -16;
			}
		}
		throw new IllegalArgumentException();
	}

	@RequiredArgsConstructor
	public enum HiddenSlot {
		HEAD(KitType.HEAD, ItemID.IRON_MED_HELM, "Hide helm"),
		CAPE(KitType.CAPE, 3779, "Hide cape"),
		AMULET(KitType.AMULET, 1796, "Hide amulet"),
		WEAPON(KitType.WEAPON, ItemID.SHADOW_SWORD, "Hide weapon"),
		TORSO(KitType.TORSO, ItemID.GHOSTLY_ROBE, "Hide torso"),
		SHIELD(KitType.SHIELD, ItemID.IRON_SQ_SHIELD, "Hide off hand"),
		ARMS(KitType.ARMS, ItemID.EXPEDITIOUS_BRACELET, "Hide sleeves"),
		LEGS(KitType.LEGS, ItemID.GHOSTLY_ROBE_6108, "Hide legs"),
		HAIR(KitType.HAIR, 2421, "Hide hair"),
		HANDS(KitType.HANDS, 21736, "Hide hands"),
		BOOTS(KitType.BOOTS, ItemID.IRON_BOOTS, "Hide boots"),
		JAW(KitType.JAW, 4593, "Hide jaw"),
		;

		final KitType kitType;
		final int iconIdToShow;
		public final String actionName;
	}

	@RequiredArgsConstructor
	public enum ShownSlot {
		HEAD(KitType.HEAD, ItemID.IRON_MED_HELM),
		CAPE(KitType.CAPE, 3779),
		AMULET(KitType.AMULET, 1796),
		WEAPON(KitType.WEAPON, ItemID.SHADOW_SWORD),
		TORSO(KitType.TORSO, ItemID.GHOSTLY_ROBE),
		SHIELD(KitType.SHIELD, ItemID.IRON_SQ_SHIELD),
		ARMS(KitType.ARMS, ItemID.GOLD_BRACELET),
		LEGS(KitType.LEGS, ItemID.GHOSTLY_ROBE_6108),
		HAIR(KitType.HAIR, 2419),
		HANDS(KitType.HANDS, 21736),
		BOOTS(KitType.BOOTS, ItemID.IRON_BOOTS),
		JAW(KitType.JAW, ItemID.BASILISK_JAW),
		;

		final KitType kitType;
		final int iconIdToShow;
	}

	public enum ActorAnimation
	{
		IDLE(Actor::getIdlePoseAnimation, Actor::setIdlePoseAnimation, STAND),
		IDLE_ROTATE_LEFT(Actor::getIdleRotateLeft, Actor::setIdleRotateLeft, ROTATE),
		IDLE_ROTATE_RIGHT(Actor::getIdleRotateRight, Actor::setIdleRotateRight, ROTATE),
		WALK(Actor::getWalkAnimation, Actor::setWalkAnimation, Swap.AnimationType.WALK),
		WALK_ROTATE_180(Actor::getWalkRotate180, Actor::setWalkRotate180, WALK_BACKWARD),
		WALK_ROTATE_LEFT(Actor::getWalkRotateLeft, Actor::setWalkRotateLeft, SHUFFLE_LEFT),
		WALK_ROTATE_RIGHT(Actor::getWalkRotateRight, Actor::setWalkRotateRight, SHUFFLE_RIGHT),
		RUN(Actor::getRunAnimation, Actor::setRunAnimation, Swap.AnimationType.RUN),
		;

		interface AnimationGetter
		{
			int getAnimation(Actor a);
		}

		interface AnimationSetter
		{
			void setAnimation(Actor a, int animationId);
		}

		@Getter
		private final Swap.AnimationType type;
		private final AnimationGetter animationGetter;
		private final AnimationSetter animationSetter;

		ActorAnimation(AnimationGetter animationGetter, AnimationSetter animationSetter, Swap.AnimationType type)
		{
			this.type = type;
			this.animationGetter = animationGetter;
			this.animationSetter = animationSetter;
		}

		public int getAnimation(Actor actor)
		{
			return animationGetter.getAnimation(actor);
		}

		public void setAnimation(Actor actor, int animationId)
		{
			animationSetter.setAnimation(actor, animationId);
		}
	}

	public enum NegativeIdsMap {
		NULL,
		HIDE_SLOT,
		SHOW_SLOT,
//		SLEEVES,
//		HAIR,
//		JAW,
		;
	}

	@RequiredArgsConstructor
	public static final class NegativeId {
		public final NegativeIdsMap type;
		public final int id;
	}
}
