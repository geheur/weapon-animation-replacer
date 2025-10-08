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
import com.weaponanimationreplacer.WeaponAnimationReplacerPlugin.PlayerData;
import java.util.Arrays;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.PlayerComposition;
import static net.runelite.api.PlayerComposition.ITEM_OFFSET;
import net.runelite.client.config.ConfigManager;

@Singleton
@Slf4j
public class TransmogrificationManager
{
	public static final int SHOW_SLOT = -1;

	@Inject private Client client;
	@Inject private ConfigManager configManager;
	@Inject private WeaponAnimationReplacerPlugin plugin;

	public void startUp()
	{
	}

    public void shutDown()
    {
	}

	/**
     * To be called when the kits are force updated by Jagex code
     */
    public void reapplyTransmog(PlayerData data)
    {
		final int currentHash = Arrays.hashCode(client.getLocalPlayer().getPlayerComposition().getEquipmentIds());
		if (currentHash == data.transmogHash)
		{
			return;
		}

		data.currentActualState = null;
		changeTransmog(data);
    }

	void changeTransmog(PlayerData data)
    {
        if (client.getGameState() != GameState.LOGGED_IN || client.getLocalPlayer() == null)
        {
            return;
        }

        Player player = data.player;
        int[] kits = player.getPlayerComposition().getEquipmentIds();
		if (data.currentActualState != null)
		{
			// restore the player to their actual state.
			System.arraycopy(data.currentActualState, 0, kits, 0, kits.length);
		}
		else
		{
			storeState(kits, data);
		}

		Integer[] swaps = plugin.getApplicableModelSwaps(data);

		// show slots.
		Integer arms = swaps[ARMS_SLOT];
		if (arms != null && arms == SHOW_SLOT) {
        	swaps[ARMS_SLOT] = getBaseArms(data) - ITEM_OFFSET;
		}
		Integer hair = swaps[HAIR_SLOT];
		if (hair != null && hair == SHOW_SLOT) {
			swaps[HAIR_SLOT] = getBaseHair(data) - ITEM_OFFSET;
		}
		Integer jaw = swaps[JAW_SLOT];
		if (jaw != null && jaw == SHOW_SLOT) {
			swaps[JAW_SLOT] = getBaseJaw(data) - ITEM_OFFSET;
		}

		// auto-apply arms/hair/jaw.
		Integer torso = swaps[TORSO_SLOT];
		if (torso != null)
		{
			if (swaps[ARMS_SLOT] == null)
			{
				swaps[ARMS_SLOT] = SHOWS_ARMS.contains(torso) ? getBaseArms(data) - ITEM_OFFSET : 0;
			}
		}
		Integer head = swaps[HEAD_SLOT];
		if (head != null)
		{
			if (swaps[HAIR_SLOT] == null)
			{
				swaps[HAIR_SLOT] = !HIDES_HAIR.contains(head) ? getBaseHair(data) - ITEM_OFFSET : 0;
			}
			if (swaps[JAW_SLOT] == null && kits[JAW_SLOT] <= ITEM_OFFSET) // Do not replace people's blue icons.
			{
				swaps[JAW_SLOT] = !HIDES_JAW.contains(head) ? getBaseJaw(data) - ITEM_OFFSET : 0;
			}
		}

		for (int i = 0; i < swaps.length; i++)
		{
			if (swaps[i] != null) {
				kits[i] = swaps[i] + ITEM_OFFSET;
			}
		}

		player.getPlayerComposition().setHash();

		data.transmogHash = Arrays.hashCode(kits);
    }

	private void storeState(int[] kits, PlayerData data)
	{
		int arms = kits[ARMS_SLOT];
		if (arms != 0 && arms != data.baseArmsKit) {
			data.baseArmsKit = arms;
			if (data.player == client.getLocalPlayer()) configManager.setRSProfileConfiguration(GROUP_NAME, "baseArmsKit", data.baseArmsKit);
		}
		int hair = kits[HAIR_SLOT];
		if (hair != 0 && hair != data.baseHairKit) {
			data.baseHairKit = hair;
			if (data.player == client.getLocalPlayer()) configManager.setRSProfileConfiguration(GROUP_NAME, "baseHairKit", data.baseHairKit);
		}
		int jaw = kits[JAW_SLOT];
		if (jaw != 0 && jaw != data.baseJawKit) {
			data.baseJawKit = jaw;
			if (data.player == client.getLocalPlayer()) configManager.setRSProfileConfiguration(GROUP_NAME, "baseJawKit", data.baseJawKit);
		}
		data.currentActualState = kits.clone();
	}

	void removeTransmog(PlayerData data)
    {
        if (data.currentActualState == null)
        {
            return;
        }
        PlayerComposition comp = data.player.getPlayerComposition();
        int[] kits = comp.getEquipmentIds();
        System.arraycopy(data.currentActualState, 0, kits, 0, kits.length);
        comp.setHash();
    }

	private static final int DEFAULT_MALE_ARMS = 256 + 28;
	private static final int DEFAULT_FEMALE_ARMS = 256 + 64;
	private static final int DEFAULT_MALE_HAIR = 256 + 0;
	private static final int DEFAULT_FEMALE_HAIR = 256 + 45;
	private static final int DEFAULT_MALE_JAW = 256 + 14;
	private static final int DEFAULT_FEMALE_JAW = 552;

	private int getBaseJaw(PlayerData data)
	{
		return getBaseModel(data.baseJawKit, DEFAULT_FEMALE_JAW, DEFAULT_MALE_JAW);
	}

	private int getBaseHair(PlayerData data)
	{
		return getBaseModel(data.baseHairKit, DEFAULT_FEMALE_HAIR, DEFAULT_MALE_HAIR);
	}

	private int getBaseArms(PlayerData data)
	{
		return getBaseModel(data.baseArmsKit, DEFAULT_FEMALE_ARMS, DEFAULT_MALE_ARMS);
	}

	private int getBaseModel(int baseKit, int defaultFemaleModel, int defaultMaleModel)
	{
		return baseKit != -1 ?
			baseKit :
			(client.getLocalPlayer().getPlayerComposition().getGender() == 1 /* female */ ? defaultFemaleModel : defaultMaleModel);
	}
}
