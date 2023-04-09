package com.weaponanimationreplacer;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(WeaponAnimationReplacerPlugin.GROUP_NAME)
public interface WeaponAnimationReplacerConfig extends Config
{
	@ConfigItem(
		keyName = "showUnequippableItems",
		name = "Search all items",
		description = "Include all items when searching for model swaps, including new items that runelite does not yet " +
			"know are equippable, as well as items that are not equippable but do have player models, such as the spade " +
			"used in the digging animation. These items do not have equip slot information, so you will have to " +
			"right-click the item in the search interface and choose an equip slot. WARNING: This also includes items " +
			"with no inventory model."
	)
	default boolean showUnequippableItems()
	{
		return false;
	}
}
