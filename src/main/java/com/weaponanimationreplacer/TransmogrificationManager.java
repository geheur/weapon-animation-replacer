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
import lombok.Setter;
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

//    @Getter
//    private List<TransmogPreset> presets = initialisePresetStorage();
//
    @Inject
    WeaponAnimationReplacerPlugin plugin;

    @Setter
    private int[] emptyState;

    @Getter
    private int[] currentActualState;

    @Getter
    private int transmogHash = 0;

    public void shutDown()
    {
//        save();
        removeTransmog();
        currentActualState = null;
        emptyState = null;
//        presets = initialisePresetStorage();
    }

    /**
     * To be called when the kits are force updated by Jagex code
     */
    public void reapplyTransmog()
    {
        clearUserActualState();

//        if (config.transmogActive())
//        {
            applyTransmog();
//        }
    }

    public void clearUserStates()
    {
        currentActualState = null;
        emptyState = null;
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

//        if (!isDefaultStateSet())
//        {
//            hintDefaultState();
//            return;
//        }
//
//        TransmogPreset preset = getCurrentPreset();
//
        Player player = client.getLocalPlayer();
        int[] kits = player.getPlayerComposition().getEquipmentIds();
        System.out.print("kit is:");
        for (int kit : kits) {

            System.out.print(" " + kit);
        }
        System.out.println();
        if (currentActualState == null)
        {
            currentActualState = kits.clone();
        }
        List<AnimationReplacementRule> applicableAnimationReplacementRules = plugin.getApplicableAnimationReplacementRules();
        System.out.println(applicableAnimationReplacementRules.size() + " rules " + applicableAnimationReplacementRules);
        List<AnimationReplacementRule> collect = applicableAnimationReplacementRules.stream().filter(rule -> rule.isModelSwapEnabled()).collect(Collectors.toList());
        if (collect.isEmpty() || collect.get(collect.size() - 1).modelSwap == -1) {
            System.out.println("removing transmog");
            removeTransmog();
        } else {
            AnimationReplacementRule animationReplacementRule = collect.get(collect.size() - 1);
            System.out.println("model is " + animationReplacementRule.modelSwap);
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

    void saveCurrent()
    {
        currentActualState = client.getLocalPlayer().getPlayerComposition().getEquipmentIds().clone();
    }

//    public void updateDefault(int opClicked)
//    {
//        if (plugin.isEmptyEquipment() || opClicked == 2)
//        {
//            chatMessageManager.queue(QueuedMessage.builder()
//                    .type(ChatMessageType.ENGINE)
//                    .value("Saved your default outfit")
//                    .build());
//            emptyState = client.getLocalPlayer().getPlayerComposition().getEquipmentIds();
//            config.saveDefault(emptyState);
//            getUIManager().getSaveDefaultStateButton().setIconSprite(115);
//            getUIManager().getBlockerBox().setHidden(true);
//        }
//        else
//        {
//            chatMessageManager.queue(QueuedMessage.builder()
//                    .type(ChatMessageType.ENGINE)
//                    .value("<col=dd0000>Remove your armour before setting a default state.</col> Right click to override.")
//                    .build());
//        }
//    }
//
//    /**
//     * Get a preset by index, handling the fact that the preset list has the -1 offset
//     */
//    public TransmogPreset getPreset(int index)
//    {
//        return presets.get(index - 1);
//    }
//
//    /**
//     * Set a preset by index, handling the fact that the preset list has the -1 offset
//     */
//    public void setPreset(int index, TransmogPreset preset)
//    {
//        presets.set(index - 1, preset);
//    }
//
//    /**
//     * Helper function to copy the current preset to another index
//     */
//    public void copyCurrentPresetTo(int index)
//    {
//        setPreset(index, getCurrentPreset());
//    }
//
    public boolean isDefaultStateSet()
    {
        return emptyState != null && emptyState.length > 0;
    }

//    public void save()
//    {
//        config.savePresets();
//    }
//
//    private UIManager getUIManager()
//    {
//        return uiManager.get();
//    }
//
//    void loadData()
//    {
//        config.loadDefault();
//        config.loadPresets();
//        clientThread.invoke(() -> presets.stream().filter(Objects::nonNull).forEach(e -> e.loadNames(itemManager)));
//    }
//
    public void hintDefaultState()
    {
//        notifier.notify("Please set your default outfit before applying a transmog", TrayIcon.MessageType.WARNING);
//        chatMessageManager.queue(QueuedMessage.builder()
//                .type(ChatMessageType.ENGINE)
//                .value("<col=dd0000>Please set your default outfit before applying a transmog</col>")
//                .build());
    }
}
