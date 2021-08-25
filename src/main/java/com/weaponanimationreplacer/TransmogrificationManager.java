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

import java.util.Arrays;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.PlayerComposition;
import net.runelite.api.kit.KitType;

@Singleton
@Slf4j
public class TransmogrificationManager
{
    @Inject
    private Client client;

    @Inject
    WeaponAnimationReplacerPlugin plugin;

    @Getter
    private int[] currentActualState;

    @Getter
    private int transmogHash = 0;

    public void shutDown()
    {
        removeTransmog();
        currentActualState = null;
    }

    /**
     * To be called when the kits are force updated by Jagex code
     */
    public void reapplyTransmog()
    {
		currentActualState = null;
		applyTransmog();
    }

    public static int baseArmsKit = -1;
	public static int baseHairKit = -1;
	public static int baseJawKit = -1;

	void applyTransmog()
    {
        if (client.getGameState() != GameState.LOGGED_IN || client.getLocalPlayer() == null)
        {
            return;
        }

        Player player = client.getLocalPlayer();
        int[] kits = player.getPlayerComposition().getEquipmentIds();
        if (currentActualState != null)
        {
			System.arraycopy(currentActualState, 0, kits, 0, kits.length);
		}
        else
		{
			if (kits[KitType.ARMS.getIndex()] != 0) {
				baseArmsKit = kits[KitType.ARMS.getIndex()];
			}
			if (kits[KitType.HAIR.getIndex()] != 0) {
				baseHairKit = kits[KitType.HAIR.getIndex()];
			}
			if (kits[KitType.JAW.getIndex()] != 0) {
				baseJawKit = kits[KitType.JAW.getIndex()];
			}
			currentActualState = kits.clone();
		}

		Map<Integer, Integer> applicableSwaps = plugin.getApplicableModelSwaps();
        log.debug(applicableSwaps.size() + " rules " + applicableSwaps);

		for (Map.Entry<Integer, Integer> swap : applicableSwaps.entrySet())
		{
			kits[swap.getKey()] = swap.getValue() + 512;
		}

		player.getPlayerComposition().setHash();

		transmogHash = Arrays.hashCode(kits);
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
}
