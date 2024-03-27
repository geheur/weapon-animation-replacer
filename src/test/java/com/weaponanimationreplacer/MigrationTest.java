package com.weaponanimationreplacer;

import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class MigrationTest
{

	@Inject WeaponAnimationReplacerPlugin plugin;

	@Test
	public void testMigration() {
		String config = "[{\"name\":\"Monkey run\",\"modelSwapEnabled\":false,\"enabled\":false,\"minimized\":false,\"modelSwap\":-1,\"itemRestrictions\":[],\"animationReplacements\":[{\"enabled\":true,\"animationSet\":\"Cursed banana\",\"animationtypeToReplace\":\"ALL\"}]},{\"name\":\"Drunk stagger\",\"modelSwapEnabled\":false,\"enabled\":false,\"minimized\":false,\"modelSwap\":-1,\"itemRestrictions\":[],\"animationReplacements\":[{\"enabled\":true,\"animationSet\":\"Crystal grail\",\"animationtypeToReplace\":\"ALL\"}]},{\"name\":\"Elder scythe\",\"modelSwapEnabled\":false,\"enabled\":true,\"minimized\":false,\"modelSwap\":22325,\"itemRestrictions\":[{\"enabled\":true,\"itemId\":22324},{\"enabled\":true,\"itemId\":4151},{\"enabled\":true,\"itemId\":12006},{\"enabled\":true,\"itemId\":4587},{\"enabled\":true,\"itemId\":24551}],\"animationReplacements\":[{\"enabled\":true,\"animationSet\":\"Elder maul\",\"animationtypeToReplace\":\"ALL\"},{\"enabled\":true,\"animationSet\":\"Scythe of Vitur\",\"animationtypeToReplace\":\"ATTACK\",\"animationtypeReplacement\":{\"type\":\"ATTACK_SLASH\",\"id\":8056}}]},{\"name\":\"Scythe\",\"modelSwapEnabled\":false,\"enabled\":false,\"minimized\":false,\"modelSwap\":22325,\"itemRestrictions\":[{\"enabled\":true,\"itemId\":12006},{\"enabled\":true,\"itemId\":4587},{\"enabled\":true,\"itemId\":4151},{\"enabled\":true,\"itemId\":22324},{\"enabled\":true,\"itemId\":24551}],\"animationReplacements\":[{\"enabled\":true,\"animationSet\":\"Scythe of Vitur\",\"animationtypeToReplace\":\"ALL\"}]},{\"name\":\"Shoulder halberd\",\"modelSwapEnabled\":false,\"enabled\":true,\"minimized\":false,\"modelSwap\":-1,\"itemRestrictions\":[{\"enabled\":true,\"itemId\":3204},{\"enabled\":true,\"itemId\":23987}],\"animationReplacements\":[{\"enabled\":true,\"animationSet\":\"Dharok\\\\u0027s greataxe\",\"animationtypeToReplace\":\"STAND_PLUS_MOVEMENT\"}]},{\"name\":\"Saeldor Slash\",\"modelSwapEnabled\":false,\"enabled\":true,\"minimized\":false,\"modelSwap\":-1,\"itemRestrictions\":[{\"enabled\":true,\"itemId\":24551},{\"enabled\":true,\"itemId\":23995},{\"enabled\":true,\"itemId\":23997}],\"animationReplacements\":[{\"enabled\":true,\"animationSet\":\"Inquisitor\\\\u0027s mace\",\"animationtypeToReplace\":\"ATTACK_SLASH\",\"animationtypeReplacement\":{\"type\":\"ATTACK_CRUSH\",\"id\":4503}}]},{\"name\":\"Magic secateurs\",\"modelSwapEnabled\":false,\"enabled\":true,\"minimized\":false,\"modelSwap\":22370,\"itemRestrictions\":[{\"enabled\":true,\"itemId\":7409}],\"animationReplacements\":[{\"enabled\":true,\"animationSet\":\"Staff\",\"animationtypeToReplace\":\"ALL\"}]},{\"name\":\"Master staff\",\"modelSwapEnabled\":false,\"enabled\":true,\"minimized\":false,\"modelSwap\":24424,\"itemRestrictions\":[{\"enabled\":true,\"itemId\":6914}],\"animationReplacements\":[{\"enabled\":true,\"animationSet\":\"Nightmare Staff\",\"animationtypeToReplace\":\"ALL\"}]},{\"name\":\"Trident Sanguinesti\",\"modelSwapEnabled\":false,\"enabled\":true,\"minimized\":false,\"modelSwap\":22323,\"itemRestrictions\":[{\"enabled\":true,\"itemId\":12899}],\"animationReplacements\":[]}]";
		List<TransmogSet> migratedTransmogSets = plugin.migrate(config);
		System.out.println("migrated transmog sets: " + migratedTransmogSets);
		String expectedConfigString = "[{\"name\":\"Monkey run\",\"enabled\":true,\"minimized\":false,\"swaps\":[{\"itemRestrictions\":[-1],\"modelSwaps\":[-1],\"animationReplacements\":[{\"animationSet\":\"Cursed banana\",\"animationtypeToReplace\":\"ALL\"}],\"graphicEffects\":[]}]},{\"name\":\"Drunk stagger\",\"enabled\":true,\"minimized\":false,\"swaps\":[{\"itemRestrictions\":[-1],\"modelSwaps\":[-1],\"animationReplacements\":[{\"animationSet\":\"Crystal grail\",\"animationtypeToReplace\":\"ALL\"}],\"graphicEffects\":[]}]},{\"name\":\"Elder scythe\",\"enabled\":true,\"minimized\":false,\"swaps\":[{\"itemRestrictions\":[22324,4151,12006,4587,24551],\"modelSwaps\":[22325],\"animationReplacements\":[{\"animationSet\":\"Elder maul\",\"animationtypeToReplace\":\"ALL\"},{\"animationSet\":\"Scythe of Vitur\",\"animationtypeToReplace\":\"ATTACK\",\"animationtypeReplacement\":{\"type\":\"ATTACK_SLASH\",\"id\":8056}}],\"graphicEffects\":[]}]},{\"name\":\"Scythe\",\"enabled\":true,\"minimized\":false,\"swaps\":[{\"itemRestrictions\":[12006,4587,4151,22324,24551],\"modelSwaps\":[22325],\"animationReplacements\":[{\"animationSet\":\"Scythe of Vitur\",\"animationtypeToReplace\":\"ALL\"}],\"graphicEffects\":[]}]},{\"name\":\"Shoulder halberd\",\"enabled\":true,\"minimized\":false,\"swaps\":[{\"itemRestrictions\":[3204,23987],\"modelSwaps\":[-1],\"animationReplacements\":[{\"animationSet\":\"2h sword\",\"animationtypeToReplace\":\"STAND_PLUS_MOVEMENT\"}],\"graphicEffects\":[]}]},{\"name\":\"Saeldor Slash\",\"enabled\":true,\"minimized\":false,\"swaps\":[{\"itemRestrictions\":[24551,23995,23997],\"modelSwaps\":[-1],\"animationReplacements\":[{\"animationSet\":\"2h sword\",\"animationtypeToReplace\":\"ATTACK_SLASH\",\"animationtypeReplacement\":{\"type\":\"ATTACK_CRUSH\",\"id\":4503}}],\"graphicEffects\":[]}]},{\"name\":\"Magic secateurs\",\"enabled\":true,\"minimized\":false,\"swaps\":[{\"itemRestrictions\":[7409],\"modelSwaps\":[22370],\"animationReplacements\":[{\"animationSet\":\"Staff\",\"animationtypeToReplace\":\"ALL\"}],\"graphicEffects\":[]}]},{\"name\":\"Master staff\",\"enabled\":true,\"minimized\":false,\"swaps\":[{\"itemRestrictions\":[6914],\"modelSwaps\":[24424],\"animationReplacements\":[{\"animationSet\":\"Nightmare Staff\",\"animationtypeToReplace\":\"ALL\"}],\"graphicEffects\":[]}]},{\"name\":\"Trident Sanguinesti\",\"enabled\":true,\"minimized\":false,\"swaps\":[{\"itemRestrictions\":[12899],\"modelSwaps\":[22323],\"animationReplacements\":[],\"graphicEffects\":[]}]}]";
		List<TransmogSet> expectedTransmogSets = plugin.getGson().fromJson(expectedConfigString, new TypeToken<ArrayList<TransmogSet>>() {}.getType());

		assertEquals(migratedTransmogSets, expectedTransmogSets);
	}

	@Test
	public void testMigrationEdgeCases() {
		String config = "[{\"name\":\"New Replacement\",\"modelSwapEnabled\":false,\"enabled\":true,\"minimized\":false,\"modelSwap\":-1,\"itemRestrictions\":[],\"animationReplacements\":[]},{\"name\":\"New Replacement\",\"modelSwapEnabled\":false,\"enabled\":true,\"minimized\":false,\"modelSwap\":-1,\"itemRestrictions\":[{\"enabled\":true,\"itemId\":-1}],\"animationReplacements\":[]},{\"name\":\"New Replacement\",\"modelSwapEnabled\":false,\"enabled\":true,\"minimized\":false,\"modelSwap\":-1,\"itemRestrictions\":[],\"animationReplacements\":[{\"enabled\":true,\"animationSet\":\"Dragon dagger\",\"animationtypeToReplace\":\"ATTACK\",\"animationtypeReplacement\":{\"type\":\"ATTACK_SPEC\",\"id\":1062}},{\"enabled\":true,\"animationSet\":\"2h sword\",\"animationtypeToReplace\":\"STAND_PLUS_MOVEMENT\"}]},{\"name\":\"New Replacement\",\"modelSwapEnabled\":false,\"enabled\":true,\"minimized\":false,\"modelSwap\":-1,\"itemRestrictions\":[],\"animationReplacements\":[{\"enabled\":true,\"animationSet\":\"Abyssal whip\",\"animationtypeToReplace\":\"STAND_PLUS_MOVEMENT\"}]},{\"name\":\"New Replacement\",\"modelSwapEnabled\":false,\"enabled\":true,\"minimized\":false,\"modelSwap\":-1,\"itemRestrictions\":[],\"animationReplacements\":[{\"enabled\":true,\"animationtypeReplacement\":{\"id\":-1}}]},{\"name\":\"New Replacement\",\"modelSwapEnabled\":false,\"enabled\":true,\"minimized\":false,\"modelSwap\":-1,\"itemRestrictions\":[{\"enabled\":true,\"itemId\":12389},{\"enabled\":true,\"itemId\":23332}],\"animationReplacements\":[]},{\"name\":\"New Replacement\",\"modelSwapEnabled\":false,\"enabled\":true,\"minimized\":false,\"modelSwap\":23334,\"itemRestrictions\":[{\"enabled\":true,\"itemId\":-1}],\"animationReplacements\":[]},{\"name\":\"New Replacement\",\"modelSwapEnabled\":false,\"enabled\":true,\"minimized\":false,\"modelSwap\":-1,\"itemRestrictions\":[{\"enabled\":true,\"itemId\":1333}],\"animationReplacements\":[]}]";
		List<TransmogSet> migratedTransmogSets = plugin.migrate(config);
		System.out.println("migrated transmog sets: " + migratedTransmogSets);
		String expectedConfigString = "[{\"name\":\"New Replacement\",\"enabled\":true,\"minimized\":false,\"swaps\":[{\"itemRestrictions\":[-1],\"modelSwaps\":[-1],\"animationReplacements\":[],\"graphicEffects\":[]}]},{\"name\":\"New Replacement\",\"enabled\":true,\"minimized\":false,\"swaps\":[{\"itemRestrictions\":[-1],\"modelSwaps\":[-1],\"animationReplacements\":[],\"graphicEffects\":[]}]},{\"name\":\"New Replacement\",\"enabled\":true,\"minimized\":false,\"swaps\":[{\"itemRestrictions\":[-1],\"modelSwaps\":[-1],\"animationReplacements\":[{\"animationSet\":\"Dragon dagger\",\"animationtypeToReplace\":\"ATTACK\",\"animationtypeReplacement\":{\"type\":\"ATTACK_SPEC\",\"id\":1062}},{\"animationSet\":\"2h sword\",\"animationtypeToReplace\":\"STAND_PLUS_MOVEMENT\"}],\"graphicEffects\":[]}]},{\"name\":\"New Replacement\",\"enabled\":true,\"minimized\":false,\"swaps\":[{\"itemRestrictions\":[-1],\"modelSwaps\":[-1],\"animationReplacements\":[{\"animationSet\":\"Abyssal whip\",\"animationtypeToReplace\":\"STAND_PLUS_MOVEMENT\"}],\"graphicEffects\":[]}]},{\"name\":\"New Replacement\",\"enabled\":true,\"minimized\":false,\"swaps\":[{\"itemRestrictions\":[-1],\"modelSwaps\":[-1],\"animationReplacements\":[{\"animationtypeReplacement\":{\"id\":-1}}],\"graphicEffects\":[]}]},{\"name\":\"New Replacement\",\"enabled\":true,\"minimized\":false,\"swaps\":[{\"itemRestrictions\":[12389,23332],\"modelSwaps\":[-1],\"animationReplacements\":[],\"graphicEffects\":[]}]},{\"name\":\"New Replacement\",\"enabled\":true,\"minimized\":false,\"swaps\":[{\"itemRestrictions\":[-1],\"modelSwaps\":[23334],\"animationReplacements\":[],\"graphicEffects\":[]}]},{\"name\":\"New Replacement\",\"enabled\":true,\"minimized\":false,\"swaps\":[{\"itemRestrictions\":[1333],\"modelSwaps\":[-1],\"animationReplacements\":[],\"graphicEffects\":[]}]}]";
		List<TransmogSet> expectedTransmogSets = plugin.getGson().fromJson(expectedConfigString, new TypeToken<ArrayList<TransmogSet>>() {}.getType());

		assertEquals(migratedTransmogSets, expectedTransmogSets);
	}

	@Test
	public void testRenameAnimationSet() {
		for (Map.Entry<String, String> stringStringEntry : WeaponAnimationReplacerPlugin.renames.entrySet())
		{
			boolean found = false;
			for (AnimationSet animationSet : AnimationSet.animationSets) {
				if (animationSet.name.equals(stringStringEntry.getValue())) {
					found = true;
					break;
				}
			}
			assertTrue("couldn't find " + stringStringEntry.getValue(), found);
		}
		String config = plugin.getGson().toJson(Collections.singletonList(new TransmogSet(Collections.singletonList(new Swap(Collections.emptyList(), Collections.emptyList(), Collections.singletonList(new Swap.AnimationReplacement(new AnimationSet("Godsword", AnimationSet.AnimationSetType.MELEE_SPECIFIC, false, new int[Swap.AnimationType.values().length]), Swap.AnimationType.ALL, null)), Collections.emptyList(), Collections.emptyList(), Collections.emptyList())))));
		System.out.println(config);
		List<TransmogSet> transmogSets = plugin.getGson().fromJson(config, new TypeToken<ArrayList<TransmogSet>>() {}.getType());
		assertEquals("Godsword (Armadyl)", transmogSets.get(0).getSwaps().get(0).animationReplacements.get(0).animationSet.name);
	}

}
