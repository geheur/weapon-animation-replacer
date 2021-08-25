/*
 * Copyright (c) 2019, Ron Young <https://github.com/raiyni>
 * All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
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

import com.google.common.primitives.Ints;
import com.google.inject.Inject;
import static com.weaponanimationreplacer.Constants.HiddenSlot;
import static com.weaponanimationreplacer.Constants.NegativeId;
import static com.weaponanimationreplacer.Constants.NegativeIdsMap;
import static com.weaponanimationreplacer.Constants.ShownSlot;
import static com.weaponanimationreplacer.Constants.mapNegativeId;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.Value;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemID;
import net.runelite.api.kit.KitType;
import net.runelite.api.widgets.ItemQuantityMode;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetPositionMode;
import net.runelite.api.widgets.WidgetSizeMode;
import net.runelite.api.widgets.WidgetTextAlignment;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
import net.runelite.client.game.chatbox.ChatboxTextInput;
import net.runelite.client.ui.JagexColors;
import net.runelite.http.api.item.ItemStats;

@Singleton
public class ChatBoxFilterableSearch extends ChatboxTextInput
{
    private static final int ICON_HEIGHT = 32;
    private static final int ICON_WIDTH = 36;
    private static final int PADDING = 6;
    private static final int MAX_RESULTS = 24;
    private static final int FONT_SIZE = 16;
    private static final int HOVERED_OPACITY = 128;

    private final ChatboxPanelManager chatboxPanelManager;
    private final ItemManager itemManager;
    private final Client client;

    private final Map<Integer, ItemComposition> results = new LinkedHashMap<>();
    private String tooltipText;
    private int index = -1;

    @Getter
    private Consumer<Integer> onItemSelected;

    @Value
    private static class ItemIcon
    {
        private final int modelId;
        private final short[] colorsToReplace;
        private final short[] texturesToReplace;
    }

    @Inject
    private ChatBoxFilterableSearch(ChatboxPanelManager chatboxPanelManager, ClientThread clientThread,
                                    ItemManager itemManager, Client client)
    {
        super(chatboxPanelManager, clientThread);
        this.chatboxPanelManager = chatboxPanelManager;
        this.itemManager = itemManager;
        this.client = client;

        lines(1);
        prompt("Item Search");
        onChanged(searchString ->
                clientThread.invokeLater(() ->
                {
                    filterResults();
                    update();
                }));
    }

    @Override
    protected void update()
    {
        Widget container = chatboxPanelManager.getContainerWidget();
        container.deleteAllChildren();

//		addPromptWidget(container);

		buildEdit(0, 5 + FONT_SIZE, container.getWidth(), FONT_SIZE);

		addSeparator(container);

		Widget deleteItemWidget = createDeleteItemWidget(container);
		Widget itemSearchWidget = createHideSlotWidget(container, "Items", 0, 10, 50);
		Widget hideSlotWidget = createHideSlotWidget(container, "Hide/Show slot", 1, 60, 80);

		int x = PADDING;
		int y = PADDING * 3;
		int idx = 0;
		if (mode == 0) // items
		{
			for (ItemComposition itemComposition : results.values())
			{
				addItemWidget(itemComposition.getId(), itemComposition.getName(), container, x, y, () ->
				{
					onItemSelected.accept(itemComposition.getId());
					chatboxPanelManager.close();
				}, idx);

				x += ICON_WIDTH + PADDING;
				if (x + ICON_WIDTH >= container.getWidth())
				{
					y += ICON_HEIGHT + PADDING;
					x = PADDING;
				}

				++idx;
			}
		} else if (mode == 1) { // hide slots.
			List<Integer> iconIds = new ArrayList<>();
			List<String> names = new ArrayList<>();
			List<Integer> hideSlotIds = new ArrayList<>();
			for (HiddenSlot hiddenSlot : HiddenSlot.values())
			{
				iconIds.add(hiddenSlot.iconIdToShow);
				names.add(hiddenSlot.actionName);
				hideSlotIds.add(mapNegativeId(new NegativeId(NegativeIdsMap.HIDE_SLOT, hiddenSlot.ordinal())));
			}

			iconIds.add(ShownSlot.ARMS.iconIdToShow); names.add("Show arms"); hideSlotIds.add(mapNegativeId(new NegativeId(NegativeIdsMap.SHOW_SLOT, KitType.ARMS.getIndex())));
			iconIds.add(ShownSlot.HAIR.iconIdToShow); names.add("Show hair"); hideSlotIds.add(mapNegativeId(new NegativeId(NegativeIdsMap.SHOW_SLOT, KitType.HAIR.getIndex())));
			iconIds.add(ShownSlot.JAW.iconIdToShow); names.add("Show jaw"); hideSlotIds.add(mapNegativeId(new NegativeId(NegativeIdsMap.SHOW_SLOT, KitType.JAW.getIndex())));
			for (int i = 0; i < iconIds.size(); i++)
			{
				final int finalI = i;
				addItemWidget(iconIds.get(i), names.get(i), container, x, y, () ->
				{
					onItemSelected.accept(hideSlotIds.get(finalI));
					chatboxPanelManager.close();
				}, idx);

				x += ICON_WIDTH + PADDING;
				if (x + ICON_WIDTH >= container.getWidth())
				{
					y += ICON_HEIGHT + PADDING;
					x = PADDING;
				}

				++idx;
			}
		}
	}

	private void addItemWidget(int id, String name,  Widget container, int x, int y, Runnable runnable, int idx)
	{
		Widget item = container.createChild(-1, WidgetType.GRAPHIC);
		item.setXPositionMode(WidgetPositionMode.ABSOLUTE_LEFT);
		item.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
		item.setOriginalX(x);
		item.setOriginalY(y + FONT_SIZE * 2);
		item.setOriginalHeight(ICON_HEIGHT);
		item.setOriginalWidth(ICON_WIDTH);
		item.setName(JagexColors.MENU_TARGET_TAG + name);
		item.setItemId(id);
		item.setItemQuantity(10000);
		item.setItemQuantityMode(ItemQuantityMode.NEVER);
		item.setBorderType(1);
		item.setAction(0, tooltipText);
		item.setHasListener(true);

		if (index == idx)
		{
			item.setOpacity(HOVERED_OPACITY);
		}
		else
		{
			item.setOnMouseOverListener((JavaScriptCallback) ev -> item.setOpacity(HOVERED_OPACITY));
			item.setOnMouseLeaveListener((JavaScriptCallback) ev -> item.setOpacity(0));
		}

		item.setOnOpListener((JavaScriptCallback) ev -> runnable.run());
		item.revalidate();
	}

	private void addSeparator(Widget container)
	{
		Widget separator = container.createChild(-1, WidgetType.LINE);
		separator.setOriginalX(0);
		separator.setOriginalY(8 + (FONT_SIZE * 2));
		separator.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER);
		separator.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
		separator.setOriginalHeight(0);
		separator.setOriginalWidth(16);
		separator.setWidthMode(WidgetSizeMode.MINUS);
		separator.setTextColor(0x666666);
		separator.revalidate();
	}

	private void addPromptWidget(Widget container)
	{
		Widget promptWidget = container.createChild(-1, WidgetType.TEXT);
		promptWidget.setText(getPrompt());
		promptWidget.setTextColor(0x800000);
		promptWidget.setFontId(getFontID());
		promptWidget.setOriginalX(0);
		promptWidget.setOriginalY(5);
		promptWidget.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER);
		promptWidget.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
		promptWidget.setOriginalHeight(FONT_SIZE);
		promptWidget.setXTextAlignment(WidgetTextAlignment.CENTER);
		promptWidget.setYTextAlignment(WidgetTextAlignment.CENTER);
		promptWidget.setWidthMode(WidgetSizeMode.MINUS);
		promptWidget.revalidate();
	}

	private Widget createDeleteItemWidget(Widget container)
	{
		Widget item = container.createChild(-1, WidgetType.GRAPHIC);
		item.setXPositionMode(WidgetPositionMode.ABSOLUTE_LEFT);
		item.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
		item.setOriginalX(470);
		item.setOriginalY(5);
		item.setOriginalHeight(ICON_HEIGHT);
		item.setOriginalWidth(ICON_WIDTH);
		ItemComposition itemComposition = itemManager.getItemComposition(ItemID.BANK_FILLER);
		item.setName("delete");
		item.setItemId(itemComposition.getId());
		item.setItemQuantity(10000);
		item.setItemQuantityMode(ItemQuantityMode.NEVER);
		item.setBorderType(1);
		item.setAction(0, tooltipText);
		item.setHasListener(true);

		item.setOnMouseOverListener((JavaScriptCallback) ev -> item.setOpacity(HOVERED_OPACITY));
		item.setOnMouseLeaveListener((JavaScriptCallback) ev -> item.setOpacity(0));

		item.setOnOpListener((JavaScriptCallback) ev ->
		{
			{
				onItemSelected.accept(itemComposition.getId());
			}

			chatboxPanelManager.close();
		});

		item.revalidate();

		return item;
	}

	private int mode = 0; // 0 items, 1 hide slots.

	private Widget createHideSlotWidget(Widget container, String name, int modeToSwitchTo, int x, int width)
	{
		Widget item = container.createChild(-1, WidgetType.TEXT);
		item.setTextColor(mode == modeToSwitchTo ? 0xffaa0000 : 0xff000000);
		item.setText(name);
		item.setFontId(getFontID());
		item.setXPositionMode(WidgetPositionMode.ABSOLUTE_LEFT);
		item.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
		item.setOriginalX(x);
		item.setOriginalY(5);
		item.setOriginalHeight(ICON_HEIGHT);
		item.setOriginalWidth(width);
//		ItemComposition itemComposition = itemManager.getItemComposition(ItemID.BANK_FILLER);
		item.setName(name);
//		item.setItemId(itemComposition.getId());
//		item.setItemQuantity(10000);
//		item.setItemQuantityMode(ItemQuantityMode.NEVER);
		item.setBorderType(1);
		item.setAction(0, tooltipText);
		item.setHasListener(true);

		item.setOnMouseOverListener((JavaScriptCallback) ev -> item.setTextColor(mode == modeToSwitchTo ? 0xffaa0000 : 0xff666666));
		item.setOnMouseLeaveListener((JavaScriptCallback) ev -> item.setTextColor(mode == modeToSwitchTo ? 0xffaa0000 : 0xff000000));

		item.setOnOpListener((JavaScriptCallback) ev ->
		{
			mode = modeToSwitchTo;
			update();
		});

		item.revalidate();

		return item;
	}

	@Override
	public void keyPressed(KeyEvent ev)
	{
		if (!chatboxPanelManager.shouldTakeInput())
		{
			return;
		}

		switch (ev.getKeyCode())
		{
			case KeyEvent.VK_ENTER:
				ev.consume();
				if (index > -1)
				{
					if (onItemSelected != null)
					{
						onItemSelected.accept(results.keySet().toArray(new Integer[results.size()])[index]);
                    }

                    chatboxPanelManager.close();
                }
                break;
            case KeyEvent.VK_TAB:
            case KeyEvent.VK_RIGHT:
                ev.consume();
                if (!results.isEmpty())
                {
                    index++;
                    if (index >= results.size())
                    {
                        index = 0;
                    }
                    clientThread.invokeLater(this::update);
                }
                break;
            case KeyEvent.VK_LEFT:
                ev.consume();
                if (!results.isEmpty())
                {
                    index--;
                    if (index < 0)
                    {
                        index = results.size() - 1;
                    }
                    clientThread.invokeLater(this::update);
                }
                break;
            case KeyEvent.VK_UP:
                ev.consume();
                if (results.size() >= (MAX_RESULTS / 2))
                {
                    index -= MAX_RESULTS / 2;
                    if (index < 0)
                    {
                        if (results.size() == MAX_RESULTS)
                        {
                            index += results.size();
                        }
                        else
                        {
                            index += MAX_RESULTS;
                        }
                        index = Ints.constrainToRange(index, 0, results.size() - 1);
                    }

                    clientThread.invokeLater(this::update);
                }
                break;
            case KeyEvent.VK_DOWN:
                ev.consume();
                if (results.size() >= (MAX_RESULTS / 2))
                {
                    index += MAX_RESULTS / 2;
                    if (index >= MAX_RESULTS)
                    {
                        if (results.size() == MAX_RESULTS)
                        {
                            index -= results.size();
                        }
                        else
                        {
                            index -= MAX_RESULTS;
                        }
                        index = Ints.constrainToRange(index, 0, results.size() - 1);
                    }

                    clientThread.invokeLater(this::update);
                }
                break;
            default:
                super.keyPressed(ev);
        }
    }

    @Override
    protected void close()
    {
        // Clear search string when closed
        value("");
        results.clear();
        index = -1;
        mode = 0;
        super.close();
    }

    @Override
    @Deprecated
    public ChatboxTextInput onDone(Consumer<String> onDone)
    {
        throw new UnsupportedOperationException();
    }

    private void filterResults()
    {
        results.clear();
        index = -1;

        String search = getValue().toLowerCase();
        if (search.isEmpty())
        {
            return;
        }

        Set<ItemIcon> itemIcons = new HashSet<>();
        // For finding members items in f2p.
		Integer integer = -1;
		try
		{
			integer = Integer.valueOf(search);
		} catch (NumberFormatException e) {
			// that's fine.
		}
		for (int i = 0; i < client.getItemCount() && results.size() < MAX_RESULTS; i++)
        {
			ItemComposition itemComposition = itemManager.getItemComposition(itemManager.canonicalize(i));
            ItemStats itemStats = itemManager.getItemStats(itemComposition.getId(), false);
            if (Constants.equippableItemsNotMarkedAsEquipment.containsKey(i)) {
            	// don't need to check anything else.
			}
            else if (itemStats == null || !itemStats.isEquipable())
            {
                continue;
            } else {
				int slot = itemStats.getEquipment().getSlot();
				if (slot == EquipmentInventorySlot.RING.getSlotIdx() || slot == EquipmentInventorySlot.AMMO.getSlotIdx()) {
					continue;
				}
			}

			String name = itemComposition.getName().toLowerCase();
			if ((name.contains(search) || i == integer) && !results.containsKey(itemComposition.getId()))
            {
                ItemIcon itemIcon = new ItemIcon(itemComposition.getInventoryModel(),
                        itemComposition.getColorToReplaceWith(), itemComposition.getTextureToReplaceWith());

                itemIcons.add(itemIcon);
                results.put(itemComposition.getId(), itemComposition);
            }
        }
    }

    public ChatBoxFilterableSearch onItemSelected(Consumer<Integer> onItemSelected)
    {
        this.onItemSelected = onItemSelected;
        return this;
    }

    public ChatBoxFilterableSearch tooltipText(final String text)
    {
        tooltipText = text;
        return this;
    }
}
