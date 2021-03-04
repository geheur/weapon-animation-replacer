package com.weaponanimationreplacer;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("WeaponAnimationReplacer")
public interface WeaponAnimationReplacerConfig extends Config
{
	@ConfigItem(
		keyName = "showSidePanel",
		name = "Show side panel",
		description = "The message to show to the user when they login"
	)
	default String greeting()
	{
		return "Hello";
	}
}
