/*
 * Copyright (c) 2020, Hydrox6 <ikada@protonmail.ch>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.weaponanimationreplacer;

import static com.weaponanimationreplacer.Constants.ARMS_SLOT;
import static com.weaponanimationreplacer.Constants.HAIR_SLOT;
import static com.weaponanimationreplacer.Constants.HEAD_SLOT;
import static com.weaponanimationreplacer.Constants.HIDES_HAIR;
import static com.weaponanimationreplacer.Constants.HIDES_JAW;
import static com.weaponanimationreplacer.Constants.JAW_SLOT;
import static com.weaponanimationreplacer.Constants.SHOWS_ARMS;
import static com.weaponanimationreplacer.Constants.TORSO_SLOT;
import static com.weaponanimationreplacer.WeaponAnimationReplacerPlugin.GROUP_NAME;
import java.util.Arrays;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.PlayerComposition;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.RuneScapeProfileChanged;

@Singleton
@Slf4j
public class TransmogrificationManager
{
	public static final int SHOW_SLOT = -1;

	@Inject private Client client;
	@Inject private ConfigManager configManager;
	@Inject private WeaponAnimationReplacerPlugin plugin;

    private int[] currentActualState;
    private int transmogHash = 0;

	private int baseArmsKit = -1;
	private int baseHairKit = -1;
	private int baseJawKit = -1;

	public void startUp()
	{
		onRuneScapeProfileChanged(new RuneScapeProfileChanged());
	}

    public void shutDown()
    {
        removeTransmog();
        currentActualState = null;
    }

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged e) {
		Integer baseArmsKit = configManager.getRSProfileConfiguration(GROUP_NAME, "baseArmsKit", Integer.class);
		this.baseArmsKit = baseArmsKit != null ? baseArmsKit : -1;
		Integer baseHairKit = configManager.getRSProfileConfiguration(GROUP_NAME, "baseHairKit", Integer.class);
		this.baseHairKit = baseHairKit != null ? baseHairKit : -1;
		Integer baseJawKit = configManager.getRSProfileConfiguration(GROUP_NAME, "baseJawKit", Integer.class);
		this.baseJawKit = baseJawKit != null ? baseJawKit : -1;
	}

	/**
     * To be called when the kits are force updated by Jagex code
     */
    public void reapplyTransmog()
    {
		final int currentHash = Arrays.hashCode(client.getLocalPlayer().getPlayerComposition().getEquipmentIds());
		if (currentHash == transmogHash)
		{
			return;
		}

		currentActualState = null;
		changeTransmog();
    }

	void changeTransmog()
    {
        if (client.getGameState() != GameState.LOGGED_IN || client.getLocalPlayer() == null)
        {
            return;
        }

        Player player = client.getLocalPlayer();
        int[] kits = player.getPlayerComposition().getEquipmentIds();
		if (currentActualState != null)
		{
			// restore the player to their actual state.
			System.arraycopy(currentActualState, 0, kits, 0, kits.length);
		}
		else
		{
			storeState(kits);
		}

		Integer[] swaps = plugin.getApplicableModelSwaps();

		// show slots.
		Integer arms = swaps[ARMS_SLOT];
		if (arms != null && arms == SHOW_SLOT) {
        	swaps[ARMS_SLOT] = getBaseArms() - 512;
		}
		Integer hair = swaps[HAIR_SLOT];
		if (hair != null && hair == SHOW_SLOT) {
			swaps[HAIR_SLOT] = getBaseHair() - 512;
		}
		Integer jaw = swaps[JAW_SLOT];
		if (jaw != null && jaw == SHOW_SLOT) {
			swaps[JAW_SLOT] = getBaseJaw() - 512;
		}

		// auto-apply arms/hair/jaw.
		Integer torso = swaps[TORSO_SLOT];
		if (torso != null)
		{
			if (swaps[ARMS_SLOT] == null)
			{
				swaps[ARMS_SLOT] = SHOWS_ARMS.contains(torso) ? getBaseArms() - 512 : 0;
			}
		}
		Integer head = swaps[HEAD_SLOT];
		if (head != null)
		{
			if (swaps[HAIR_SLOT] == null)
			{
				swaps[HAIR_SLOT] = !HIDES_HAIR.contains(head) ? getBaseHair() - 512 : 0;
			}
			if (swaps[JAW_SLOT] == null && kits[JAW_SLOT] <= 512) // Do not replace people's blue icons.
			{
				swaps[JAW_SLOT] = !HIDES_JAW.contains(head) ? getBaseJaw() - 512 : 0;
			}
		}

		for (int i = 0; i < swaps.length; i++)
		{
			if (swaps[i] != null) {
				kits[i] = swaps[i] + 512;
			}
		}

		player.getPlayerComposition().setHash();

		transmogHash = Arrays.hashCode(kits);
    }

	private void storeState(int[] kits)
	{
		int arms = kits[ARMS_SLOT];
		if (arms != 0 && arms != baseArmsKit) {
			baseArmsKit = arms;
			configManager.setRSProfileConfiguration(GROUP_NAME, "baseArmsKit", baseArmsKit);
		}
		int hair = kits[HAIR_SLOT];
		if (hair != 0 && hair != baseHairKit) {
			baseHairKit = hair;
			configManager.setRSProfileConfiguration(GROUP_NAME, "baseHairKit", baseHairKit);
		}
		int jaw = kits[JAW_SLOT];
		if (jaw != 0 && jaw != baseJawKit) {
			baseJawKit = jaw;
			configManager.setRSProfileConfiguration(GROUP_NAME, "baseJawKit", baseJawKit);
		}
		currentActualState = kits.clone();
	}

	void removeTransmog()
    {
        if (currentActualState == null)
        {
            return;
        }
        PlayerComposition comp = client.getLocalPlayer().getPlayerComposition();
        int[] kits = comp.getEquipmentIds();
        System.arraycopy(currentActualState, 0, kits, 0, kits.length);
        comp.setHash();
    }

	private static final int DEFAULT_MALE_ARMS = 256 + 28;
	private static final int DEFAULT_FEMALE_ARMS = 256 + 64;
	private static final int DEFAULT_MALE_HAIR = 256 + 0;
	private static final int DEFAULT_FEMALE_HAIR = 256 + 45;
	private static final int DEFAULT_MALE_JAW = 256 + 14;

	private int getBaseJaw()
	{
		// Female characters don't use the jaw slot in their base model.
		if (client.getLocalPlayer().getPlayerComposition().getGender() == 1 /* female */) return 0;
		return getBaseModel(baseJawKit, 0, DEFAULT_MALE_JAW);
	}

	private int getBaseHair()
	{
		return getBaseModel(baseHairKit, DEFAULT_FEMALE_HAIR, DEFAULT_MALE_HAIR);
	}

	private int getBaseArms()
	{
		return getBaseModel(baseArmsKit, DEFAULT_FEMALE_ARMS, DEFAULT_MALE_ARMS);
	}

	private int getBaseModel(int baseKit, int defaultFemaleModel, int defaultMaleModel)
	{
		return baseKit != -1 ?
			baseKit :
			(client.getLocalPlayer().getPlayerComposition().getGender() == 1 /* female */ ? defaultFemaleModel : defaultMaleModel);
	}
}
