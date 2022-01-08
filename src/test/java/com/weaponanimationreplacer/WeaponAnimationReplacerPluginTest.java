package com.weaponanimationreplacer;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class WeaponAnimationReplacerPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(WeaponAnimationReplacerPlugin.class, WeaponAnimationReplacerToolsPlugin.class);
		RuneLite.main(args);
	}
}