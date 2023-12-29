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
		description = "Include all items when searching for model swaps, which may include new items that runelite does not yet " +
			"know are equippable,<br/> as well as items that are not equippable but do have player models.<br/> These items do" +
			"not have equip slot information, so you will have to right-click the item in the search interface and" +
			"choose an equip slot.<br/> WARNING: This also includes items with no player model."
	)
	default boolean showUnequippableItems()
	{
		return false;
	}

	@ConfigItem(
		keyName = "hideSidePanel",
		name = "Hide side panel",
		description = "",
		position = 1
	)
	default boolean hideSidePanel()
	{
		return false;
	}
}
