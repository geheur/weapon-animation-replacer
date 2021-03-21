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

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.PlayerComposition;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.game.ItemManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
@Slf4j
public class TransmogrificationManager
{
    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private Notifier notifier;

    @Inject
    private ItemManager itemManager;

    @Inject
    private ChatMessageManager chatMessageManager;

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
        clearUserActualState();
        applyTransmog();
    }

    public void clearUserActualState()
    {
        currentActualState = null;
    }

    void applyTransmog()
    {
        if (client.getGameState() != GameState.LOGGED_IN || client.getLocalPlayer() == null)
        {
            return;
        }

        Player player = client.getLocalPlayer();
        int[] kits = player.getPlayerComposition().getEquipmentIds();
        if (currentActualState == null)
        {
            currentActualState = kits.clone();
        }
        List<AnimationReplacementRule> applicableAnimationReplacementRules = plugin.getApplicableAnimationReplacementRules();
        log.debug(applicableAnimationReplacementRules.size() + " rules " + applicableAnimationReplacementRules);
        List<AnimationReplacementRule> collect = applicableAnimationReplacementRules.stream().filter(rule -> rule.isModelSwapEnabled()).collect(Collectors.toList());
        if (collect.isEmpty() || collect.get(collect.size() - 1).modelSwap == -1) {
//            log.debug("removing transmog");
            removeTransmog();
        } else {
            AnimationReplacementRule animationReplacementRule = collect.get(collect.size() - 1);
//            System.out.println("model is " + animationReplacementRule.modelSwap);
            kits[3] = animationReplacementRule.modelSwap + 512;
            player.getPlayerComposition().setHash();
        }
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
