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

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Ints;
import com.google.inject.Inject;
import static com.weaponanimationreplacer.Constants.ARMS_SLOT;
import static com.weaponanimationreplacer.Constants.HAIR_SLOT;
import static com.weaponanimationreplacer.Constants.HiddenSlot;
import com.weaponanimationreplacer.Constants.IdIconNameAndSlot;
import static com.weaponanimationreplacer.Constants.JAW_SLOT;
import static com.weaponanimationreplacer.Constants.NegativeId;
import static com.weaponanimationreplacer.Constants.NegativeIdsMap;
import static com.weaponanimationreplacer.Constants.ShownSlot;
import static com.weaponanimationreplacer.Constants.TriggerItemIds;
import static com.weaponanimationreplacer.Constants.mapNegativeId;
import com.weaponanimationreplacer.WeaponAnimationReplacerPlugin.SearchType;
import static com.weaponanimationreplacer.WeaponAnimationReplacerPlugin.SearchType.MODEL_SWAP;
import static com.weaponanimationreplacer.WeaponAnimationReplacerPlugin.SearchType.SPELL_L;
import static com.weaponanimationreplacer.WeaponAnimationReplacerPlugin.SearchType.SPELL_R;
import static com.weaponanimationreplacer.WeaponAnimationReplacerPlugin.SearchType.TRIGGER_ITEM;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.Value;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import static net.runelite.api.ItemID.*;
import net.runelite.api.MenuEntry;
import net.runelite.api.SpriteID;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuShouldLeftClick;
import net.runelite.api.kit.KitType;
import net.runelite.api.widgets.ItemQuantityMode;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetPositionMode;
import net.runelite.api.widgets.WidgetSizeMode;
import net.runelite.api.widgets.WidgetTextAlignment;
import net.runelite.api.widgets.WidgetType;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
import net.runelite.client.game.chatbox.ChatboxTextInput;
import net.runelite.client.ui.JagexColors;
import net.runelite.client.util.ColorUtil;
import net.runelite.http.api.item.ItemStats;

@Singleton
public class ChatBoxFilterableSearch extends ChatboxTextInput
{
    private static final int ICON_HEIGHT = 32;
    private static final int ICON_WIDTH = 36;
    private static final int PADDING = 6;
    private static final int RESULTS_PER_PAGE = 24;
    private static final int FONT_SIZE = 16;
    private static final int HOVERED_OPACITY = 128;

    private final ChatboxPanelManager chatboxPanelManager;
    private final ItemManager itemManager;
    private final Client client;
    private final WeaponAnimationReplacerConfig config;
	private final WeaponAnimationReplacerPlugin plugin;

	private final List<Integer> results = new ArrayList<>();
	private final List<String> spells = new ArrayList<>();
	private String tooltipText;
    private int index = -1;

    @Getter
    private Consumer<SelectionResult> onItemSelected;

	private Consumer<Integer> onItemMouseOvered;
	private Runnable onItemDeleted;
	@Getter
	private SearchType searchType;

	public void setType(SearchType searchType)
	{
		this.searchType = searchType;
		mode = 0;
		slotToShow = null;
	}

    @Inject
    private ChatBoxFilterableSearch(ChatboxPanelManager chatboxPanelManager, ClientThread clientThread,
                                    ItemManager itemManager, Client client, WeaponAnimationReplacerConfig config,
									WeaponAnimationReplacerPlugin plugin)
    {
        super(chatboxPanelManager, clientThread);
        this.chatboxPanelManager = chatboxPanelManager;
        this.itemManager = itemManager;
        this.client = client;
        this.config = config;
        this.plugin = plugin;

        lines(1);
        prompt("Item Search");
        onChanged(searchString ->
                clientThread.invokeLater(() ->
                {
                    resetPage();
                    update();
                }));
    }

	@Override
	protected void open()
	{
		resetPage();
		super.open();
	}

	private void resetPage()
	{
		page = 0;
		lastPage = -1;
		filteredPageIndexes.clear();
		filterResults();
	}

	@Override
    protected void update()
    {
        Widget container = chatboxPanelManager.getContainerWidget();
        container.deleteAllChildren();

//		addPromptWidget(container);

		buildEdit(0, 5 + FONT_SIZE, container.getWidth(), FONT_SIZE);

		addSeparator(container);

		createCloseInterfaceWidget(container);
		createDeleteItemWidget(container);
		createPageButtons(container);
		if (searchType == MODEL_SWAP) {
			createHideSlotWidget(container, "Items:", 0, 10, 40);
			createHideSlotWidget(container, " (" + (slotToShow == null ? "all" : slotToShow.name().toLowerCase()) + ")", 0, 50, 70);
			createHideSlotWidget(container, "Hide/Show", 1, 110, 80);
		}

		int x = PADDING;
		int y = PADDING * 3;
		int idx = 0;
		if (searchType == TRIGGER_ITEM || searchType == MODEL_SWAP)
		{
			if (mode == 0) // items
			{
				if (results.size() == 0)
				{
					addText(container, getValue().isEmpty() ? "Type to search items." : "No results.", 0xff000000, 170, 50);
				}
				else
				{
					for (Integer itemId : results)
					{
						ItemComposition itemComposition = itemManager.getItemComposition(itemId);
						int iconId = Constants.getIconId(itemId);
						String name = Constants.getName(itemId, itemComposition.getName());
						if (searchType == TRIGGER_ITEM) {
							IdIconNameAndSlot hiddenSlot = TriggerItemIds.getHiddenSlot(itemId);
							if (hiddenSlot != null) {
								iconId = hiddenSlot.getIconId();
								name = hiddenSlot.getName();
							}
						}
						addItemWidgetItem(
							itemId,
							iconId,
							name + (plugin.debug ? " (" + itemId + ")" : ""),
							container,
							x,
							y,
							se -> {
								// Applies for worn items with no slot, e.g. new items that runelite's wiki scraper hasn't picked up yet.
								if (searchType == TRIGGER_ITEM) {
									Integer slot = triggerItemSlots.get(itemId);
									if (slot != null && slot != plugin.getWikiScrapeSlot(itemId)) {
										itemSelected(itemId, slot);
										return;
									}
								}
								itemSelected(itemId);
							}, idx
						);

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
			else if (mode == 1)
			{ // hide slots.
				List<Integer> iconIds = new ArrayList<>();
				List<String> names = new ArrayList<>();
				List<Integer> hideSlotIds = new ArrayList<>();
				for (HiddenSlot hiddenSlot : HiddenSlot.values())
				{
					iconIds.add(hiddenSlot.iconIdToShow);
					names.add(hiddenSlot.actionName);
					hideSlotIds.add(mapNegativeId(new NegativeId(NegativeIdsMap.HIDE_SLOT, hiddenSlot.ordinal())));
				}

				iconIds.add(ShownSlot.ARMS.iconIdToShow);
				names.add("Show arms");
				hideSlotIds.add(mapNegativeId(new NegativeId(NegativeIdsMap.SHOW_SLOT, ARMS_SLOT)));
				iconIds.add(ShownSlot.HAIR.iconIdToShow);
				names.add("Show hair");
				hideSlotIds.add(mapNegativeId(new NegativeId(NegativeIdsMap.SHOW_SLOT, HAIR_SLOT)));
				iconIds.add(ShownSlot.JAW.iconIdToShow);
				names.add("Show jaw");
				hideSlotIds.add(mapNegativeId(new NegativeId(NegativeIdsMap.SHOW_SLOT, JAW_SLOT)));
				for (int i = 0; i < iconIds.size(); i++)
				{
					final int finalI = i;
					addItemWidgetItem(
						hideSlotIds.get(i),
						iconIds.get(i),
						names.get(i),
						container,
						x,
						y,
						se -> itemSelected(hideSlotIds.get(finalI)), idx
					);

					x += ICON_WIDTH + PADDING;
					if (x + ICON_WIDTH >= container.getWidth())
					{
						y += ICON_HEIGHT + PADDING;
						x = PADDING;
					}

					++idx;
				}
			}
		} else { // spell
			for (String spell : spells)
			{
				for (ProjectileCast projectile : Constants.projectiles)
				{
					if (!projectile.getName(itemManager).equals(spell)) continue;

					addItemWidget(projectile.getId(), projectile.getItemIdIcon(), projectile.getSpriteIdIcon(), projectile.getName(itemManager), container, x, y, se ->
					{
						itemSelected(projectile.getId());
						chatboxPanelManager.close();
					}, idx);

					x += ICON_WIDTH + PADDING;
					if (x + ICON_WIDTH >= container.getWidth())
					{
						y += ICON_HEIGHT + PADDING;
						x = PADDING;
					}

					++idx;
					break;
				}
			}
		}
	}

	private void createPageButtons(Widget container)
	{
		if (page != lastPage)
		{
			Widget rightArrow = container.createChild(-1, WidgetType.GRAPHIC);
			rightArrow.setSpriteId(SpriteID.FORWARD_ARROW_BUTTON_SMALL);
			rightArrow.setXPositionMode(WidgetPositionMode.ABSOLUTE_RIGHT);
			rightArrow.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
			rightArrow.setOriginalX(90);
			rightArrow.setOriginalY(5);
			rightArrow.setOriginalHeight(20);
			rightArrow.setOriginalWidth(20);
			rightArrow.setBorderType(1);
			rightArrow.setAction(0, tooltipText);
			rightArrow.setHasListener(true);
			rightArrow.setOnMouseOverListener((JavaScriptCallback) ev -> rightArrow.setOpacity(HOVERED_OPACITY));
			rightArrow.setOnMouseLeaveListener((JavaScriptCallback) ev -> rightArrow.setOpacity(0));
			rightArrow.setOnOpListener((JavaScriptCallback) ev -> {
				clientThread.invoke(() -> {
					page++;
					filterResults();
					update();
				});
			});
			rightArrow.revalidate();
		}

		if (lastPage != 0) {
			Widget leftArrow = container.createChild(-1, WidgetType.TEXT);
			leftArrow.setText("" + (page + 1));
			leftArrow.setTextColor(0x000000);
			leftArrow.setFontId(getFontID());
			leftArrow.setXPositionMode(WidgetPositionMode.ABSOLUTE_RIGHT);
			leftArrow.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
			leftArrow.setOriginalX(109 + ((page + 1 >= 10) ? 0 : -5));
			leftArrow.setOriginalY(5);
			leftArrow.setOriginalHeight(20);
			leftArrow.setOriginalWidth(20);
			leftArrow.setBorderType(1);
			leftArrow.setAction(0, tooltipText);
			leftArrow.revalidate();
		}

		if (page != 0)
		{
			Widget leftArrow = container.createChild(-1, WidgetType.GRAPHIC);
			leftArrow.setSpriteId(SpriteID.BACK_ARROW_BUTTON_SMALL);
			leftArrow.setXPositionMode(WidgetPositionMode.ABSOLUTE_RIGHT);
			leftArrow.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
			leftArrow.setOriginalX(130);
			leftArrow.setOriginalY(5);
			leftArrow.setOriginalHeight(20);
			leftArrow.setOriginalWidth(20);
			leftArrow.setBorderType(1);
			leftArrow.setAction(0, tooltipText);
			leftArrow.setHasListener(true);
			leftArrow.setOnMouseOverListener((JavaScriptCallback) ev -> leftArrow.setOpacity(HOVERED_OPACITY));
			leftArrow.setOnMouseLeaveListener((JavaScriptCallback) ev -> leftArrow.setOpacity(0));
			leftArrow.setOnOpListener((JavaScriptCallback) ev -> {
				clientThread.invoke(() -> {
					page--;
					filterResults();
					update();
				});
			});
			leftArrow.revalidate();
		}
	}

	private void addText(Widget container, String text, int textColor, int x, int y)
	{
		Widget item = container.createChild(-1, WidgetType.TEXT);
		item.setTextColor(textColor);
		item.setText(text);
		item.setFontId(getFontID());
		item.setXPositionMode(WidgetPositionMode.ABSOLUTE_LEFT);
		item.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
		item.setOriginalX(x);
		item.setOriginalY(y);
		item.setOriginalHeight(40);
		item.setOriginalWidth(1000);
		item.setBorderType(1);
		item.revalidate();
	}

	@Value
	public static final class SelectionResult {
		public final int itemId;
		public final int slot;
	}

	private void itemSelected(int itemId)
	{
		itemSelected(itemId, -1);
	}

	private void itemSelected(int itemId, int slot)
	{
		if (onItemSelected != null) onItemSelected.accept(new SelectionResult(itemId, slot));
	}

	private void addItemWidgetSprite(int id, int spriteId, String name, Widget container, int x, int y, JavaScriptCallback onOpListener, int idx)
	{
		addItemWidget(id, -1, spriteId, name, container, x, y, onOpListener, idx);
	}

	private void addItemWidgetItem(int id, int iconId, String name, Widget container, int x, int y, JavaScriptCallback onOpListener, int idx)
	{
		addItemWidget(id, iconId, -1, name, container, x, y, onOpListener, idx);
	}

	private void addItemWidget(int id, int iconId, int spriteId, String name, Widget container, int x, int y, JavaScriptCallback onOpListener, int idx)
	{
		Widget item = container.createChild(-1, WidgetType.GRAPHIC);
		item.setXPositionMode(WidgetPositionMode.ABSOLUTE_LEFT);
		item.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
		item.setOriginalX(x);
		item.setOriginalY(y + FONT_SIZE * 2);
		item.setOriginalHeight(ICON_HEIGHT);
		item.setOriginalWidth(ICON_WIDTH);
		item.setName(JagexColors.MENU_TARGET_TAG + name);
		if (iconId != -1) item.setItemId(iconId);
		else if (spriteId != -1) item.setSpriteId(spriteId);
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
			item.setOnMouseOverListener((JavaScriptCallback) ev -> {
				item.setOpacity(HOVERED_OPACITY);
				if (onItemMouseOvered != null) onItemMouseOvered.accept(id);
			});
			item.setOnMouseLeaveListener((JavaScriptCallback) ev -> {
				item.setOpacity(0);
				if (onItemMouseOvered != null) onItemMouseOvered.accept(-1);
			});
		}

		item.setOnOpListener(onOpListener);
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
		item.setOriginalX(430);
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

		item.setOnOpListener((JavaScriptCallback) ev -> {
			onItemDeleted.run();
			chatboxPanelManager.close();
		});

		item.revalidate();

		return item;
	}

	private Widget createCloseInterfaceWidget(Widget container)
	{
		Widget item = container.createChild(-1, WidgetType.TEXT);
		item.setTextColor(0xff000000);
		item.setFontId(getFontID());
		item.setText("X");
		item.setName("Close (Esc");
		item.setXPositionMode(WidgetPositionMode.ABSOLUTE_LEFT);
		item.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
		item.setOriginalX(470); // Further left than it should be to prevent scrolling up the chat.
		item.setOriginalY(2);
		item.setOriginalHeight(ICON_HEIGHT);
		item.setOriginalWidth(15);
		item.setBorderType(1);
		item.setAction(0, tooltipText);
		item.setHasListener(true);

		item.setOnMouseOverListener((JavaScriptCallback) ev -> item.setOpacity(HOVERED_OPACITY));
		item.setOnMouseLeaveListener((JavaScriptCallback) ev -> item.setOpacity(0));

		item.setOnOpListener((JavaScriptCallback) ev -> {
			chatboxPanelManager.close();
		});

		item.revalidate();

		return item;
	}

	@Getter
	private int mode = 0; // 0 items, 1 hide slots.
	/** Slot to filter when showing model swaps. Null indicates to show all items. */
	private KitType slotToShow = null;

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
					itemSelected(results.get(index));
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
                if (results.size() >= (RESULTS_PER_PAGE / 2))
                {
                    index -= RESULTS_PER_PAGE / 2;
                    if (index < 0)
                    {
                        if (results.size() == RESULTS_PER_PAGE)
                        {
                            index += results.size();
                        }
                        else
                        {
                            index += RESULTS_PER_PAGE;
                        }
                        index = Ints.constrainToRange(index, 0, results.size() - 1);
                    }

                    clientThread.invokeLater(this::update);
                }
                break;
            case KeyEvent.VK_DOWN:
                ev.consume();
                if (results.size() >= (RESULTS_PER_PAGE / 2))
                {
                    index += RESULTS_PER_PAGE / 2;
                    if (index >= RESULTS_PER_PAGE)
                    {
                        if (results.size() == RESULTS_PER_PAGE)
                        {
                            index -= results.size();
                        }
                        else
                        {
                            index -= RESULTS_PER_PAGE;
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
    	if (onItemMouseOvered != null) onItemMouseOvered.accept(-1);

        // Clear search string when closed
        value("");
        results.clear();
        spells.clear();
        index = -1;
        mode = 0;
        searchType = null;
        super.close();
    }

    @Override
    @Deprecated
    public ChatboxTextInput onDone(Consumer<String> onDone)
    {
        throw new UnsupportedOperationException();
    }

    private int page = 0;
	private int lastPage = -1;
	/** For faster searches on pages past the first. */
	private Map<Integer, Integer> filteredPageIndexes = new HashMap<>();

	private Map<Integer, Integer> triggerItemSlots = new HashMap<>();

    private void filterResults()
    {
        results.clear();
        spells.clear();
        index = -1;

        String search = getValue().toLowerCase();
		if (search.isEmpty() && (!(mode == 0 && slotToShow != null)))
		{
        	if (searchType == TRIGGER_ITEM) {
        		// Add equipped items to the list for easy access.
				ItemContainer itemContainer = client.getItemContainer(InventoryID.EQUIPMENT);
				Item[] items = itemContainer == null ? new Item[0] : itemContainer.getItems();
				triggerItemSlots.clear();
				for (int i = 0; i < items.length; i++)
				{
					if (i == EquipmentInventorySlot.RING.getSlotIdx() || i == EquipmentInventorySlot.AMMO.getSlotIdx()) continue;

					int itemId = items[i].getId();
					if (itemId == -1) {
						itemId = -1_000_000 - KitType.values()[i].getIndex();
					} else {
						itemId = itemManager.canonicalize(items[i].getId());
					}

					ItemComposition itemComposition = itemManager.getItemComposition(itemId);
					triggerItemSlots.put(itemComposition.getId(), i);
					results.add(itemId);
				}
				lastPage = 0; // Do not show page change arrows.
				return;
			} else if (searchType == MODEL_SWAP) {
				lastPage = 0; // Do not show page change arrows.
				return;
			} else if (searchType == SPELL_L) {

			} else if (searchType == SPELL_R) {

			}
        }

        // For searching by item id.
		Integer integer = -1;
		try
		{
			integer = Integer.valueOf(search);
		} catch (NumberFormatException e) {
			// that's fine.
		}

		if (searchType == TRIGGER_ITEM || searchType == MODEL_SWAP)
		{
			int start = filteredPageIndexes.getOrDefault(page - 1, 0);
			boolean showUnequippableItems = searchType == MODEL_SWAP && config.showUnequippableItems();
			if (searchType == TRIGGER_ITEM) {
				for (IdIconNameAndSlot hiddenSlot : TriggerItemIds.EMPTY_SLOTS)
				{
					if (hiddenSlot.getName().contains(search)) {
						if (results.size() == RESULTS_PER_PAGE)
						{
							filteredPageIndexes.put(page, hiddenSlot.getIconId());
							return; // skip the lastPage setting, since there is at least 1 item on the next page.
						}
						results.add(hiddenSlot.getIconId());
					}
				}
			}
			for (int itemId = start; itemId < client.getItemCount(); itemId++)
			{
				ItemComposition itemComposition = getItemCompositionIfUsable(itemId, showUnequippableItems);
				if (itemComposition == null) continue;

				if (!matchesSlotFilter(itemId)) continue;

				String name = Constants.getName(itemId, itemComposition.getName()).toLowerCase();
				if (itemId == integer || name.contains(search))
				{
					if (results.size() == RESULTS_PER_PAGE)
					{
						filteredPageIndexes.put(page, itemId);
						return; // skip the lastPage setting, since there is at least 1 item on the next page.
					}
					results.add(itemComposition.getId());
				}
			}
			// We ran out of items to search.
			lastPage = page;
		} else { // is spell.
			int start = filteredPageIndexes.getOrDefault(page - 1, 0);
			for (int projectileIndex = start; projectileIndex < Constants.projectiles.size(); projectileIndex++)
			{
				ProjectileCast projectile = Constants.projectiles.get(projectileIndex);
				if (searchType == SPELL_L && projectile.isArtificial()) continue;
				String projectileName = projectile.getName(itemManager);
				if (projectileName.toLowerCase().contains(search) && !spells.contains(projectileName))
				{
					if (spells.size() == RESULTS_PER_PAGE)
					{
						filteredPageIndexes.put(page, projectileIndex);
						return; // skip the lastPage setting, since there is at least 1 item on the next page.
					}
					spells.add(projectileName);
				}
			}
			// We ran out of items to search.
			lastPage = page;
		}
    }

	private boolean matchesSlotFilter(int itemId)
	{
		boolean matchesSlotFilter = false;
		if (slotToShow == null) {
			matchesSlotFilter = true;
		} else {
			Integer slot = plugin.getSlotForNonNegativeModelId(itemId);
			if (slot != null && slot <= 11 && KitType.values()[slot] == slotToShow) {
				matchesSlotFilter = true;
			}
		}
		return matchesSlotFilter;
	}

	private ItemComposition getItemCompositionIfUsable(int i, boolean showUnequippableItems)
	{
		ItemComposition itemComposition = itemManager.getItemComposition(i);

		// skip notes, placeholders, and weight-reducing item equipped version.
		if (itemComposition.getNote() != -1 || itemComposition.getPlaceholderTemplateId() != -1 || WEIGHT_REDUCING_ITEMS.get(i) != null)
		{
			return null;
		}

		Integer slotOverride = Constants.SLOT_OVERRIDES.get(i);
		if (slotOverride != null)
		{
			return slotOverride == -1 ? null : itemComposition;
		}

		ItemStats itemStats = itemManager.getItemStats(itemComposition.getId(), false);
		if (!showUnequippableItems)
		{
			if (itemStats == null || !itemStats.isEquipable())
			{
				return null;
			}
			int slot = itemStats.getEquipment().getSlot();
			if (slot == EquipmentInventorySlot.RING.getSlotIdx() || slot == EquipmentInventorySlot.AMMO.getSlotIdx())
			{
				return null;
			}
		}
		return itemComposition;
	}

	@Subscribe
	public void onMenuShouldLeftClick(MenuShouldLeftClick e) {
		for (MenuEntry menuEntry : client.getMenuEntries())
		{
			Widget widget = menuEntry.getWidget();
			if (widget == null || widget.getId() != WidgetUtil.packComponentId(162, 37))
			{
				continue;
			}

			// items that do not have a default/known equip slot should require the user to select a slot, so force the
			// right-click menu open.
			if (getSearchType() == MODEL_SWAP && getMode() == 0 && widget.getItemId() != -1 && !menuEntry.getTarget().equals("delete") && plugin.getMySlot(widget.getItemId()) == null)
			{
				e.setForceRightClick(true);
				return;
			}
			if (widget.getText().startsWith(" ("))
			{
				e.setForceRightClick(true);
				return;
			}
		}
	}

	@Subscribe
	public void onMenuOpened(MenuOpened e) {
    	// there is a limit of 10 actions on a widget which is less than we need, so add menu entries here instead.
		for (MenuEntry menuEntry : e.getMenuEntries())
		{
			Widget widget = menuEntry.getWidget();
			if (widget == null || widget.getId() != WidgetUtil.packComponentId(162, 37)) continue;

			if (widget.getText().startsWith(" ("))
			{
				client.createMenuEntry(1).setOption("Show all").onClick(me -> {
					mode = 0;
					slotToShow = null;
					resetPage();
					update();
				});
				for (EquipmentInventorySlot slot : EquipmentInventorySlot.values())
				{
					if (slot == EquipmentInventorySlot.AMMO || slot == EquipmentInventorySlot.RING) continue;

					client.createMenuEntry(1).setOption("Show only").setTarget(ColorUtil.wrapWithColorTag(slot.name().toLowerCase(), new Color(0xff9040))).onClick(me -> {
						mode = 0;
						slotToShow = KitType.values()[slot.getSlotIdx()];
						resetPage();
						update();
					});
				}
				MenuEntry[] newMenuEntries = Arrays.stream(client.getMenuEntries()).filter(me -> !me.getOption().equals(tooltipText)).toArray(i -> new MenuEntry[i]);
				client.setMenuEntries(newMenuEntries);
				return;
			}
			else if (getSearchType() == MODEL_SWAP && getMode() == 0 && widget.getItemId() != -1)
			{
				// If the item has no default/known equip slot, remove the default option since there is no
				// default. I cannot just not add this option to the widget because then there would be no menu options and
				// neither menushouldleftclick nor this one will get called.
				if (!menuEntry.getTarget().equals("delete") && plugin.getMySlot(widget.getItemId()) == null)
				{
					MenuEntry[] newMenuEntries = Arrays.stream(client.getMenuEntries()).filter(me -> !me.getOption().equals(tooltipText)).toArray(i -> new MenuEntry[i]);
					client.setMenuEntries(newMenuEntries);
				}

				ItemComposition itemComposition = itemManager.getItemComposition(widget.getItemId());
				for (KitType value : KitType.values())
				{
					client.createMenuEntry(1)
						.setTarget(ColorUtil.wrapWithColorTag(itemComposition.getName(), new Color(0xff9040)))
						.setOption(value.name())
						.onClick(me -> {
							itemSelected(widget.getItemId(), value.ordinal());
						})
					;
				}
				return;
			}
		}
	}

	public ChatBoxFilterableSearch onItemSelected(Consumer<SelectionResult> onItemSelected)
    {
        this.onItemSelected = onItemSelected;
        return this;
    }

	public ChatBoxFilterableSearch onItemMouseOvered(Consumer<Integer> onItemMouseOvered)
	{
		this.onItemMouseOvered = onItemMouseOvered;
		return this;
	}

	public ChatBoxFilterableSearch onItemDeleted(Runnable onItemDeleted)
	{
		this.onItemDeleted = onItemDeleted;
		return this;
	}

	public ChatBoxFilterableSearch tooltipText(final String text)
    {
        tooltipText = text;
        return this;
    }

    // Copied from ItemManager.
	private static final ImmutableMap<Integer, Integer> WEIGHT_REDUCING_ITEMS = ImmutableMap.<Integer, Integer>builder().
		put(BOOTS_OF_LIGHTNESS_89, BOOTS_OF_LIGHTNESS).
		put(PENANCE_GLOVES_10554, PENANCE_GLOVES).

		put(GRACEFUL_HOOD_11851, GRACEFUL_HOOD).
		put(GRACEFUL_CAPE_11853, GRACEFUL_CAPE).
		put(GRACEFUL_TOP_11855, GRACEFUL_TOP).
		put(GRACEFUL_LEGS_11857, GRACEFUL_LEGS).
		put(GRACEFUL_GLOVES_11859, GRACEFUL_GLOVES).
		put(GRACEFUL_BOOTS_11861, GRACEFUL_BOOTS).
		put(GRACEFUL_HOOD_13580, GRACEFUL_HOOD_13579).
		put(GRACEFUL_CAPE_13582, GRACEFUL_CAPE_13581).
		put(GRACEFUL_TOP_13584, GRACEFUL_TOP_13583).
		put(GRACEFUL_LEGS_13586, GRACEFUL_LEGS_13585).
		put(GRACEFUL_GLOVES_13588, GRACEFUL_GLOVES_13587).
		put(GRACEFUL_BOOTS_13590, GRACEFUL_BOOTS_13589).
		put(GRACEFUL_HOOD_13592, GRACEFUL_HOOD_13591).
		put(GRACEFUL_CAPE_13594, GRACEFUL_CAPE_13593).
		put(GRACEFUL_TOP_13596, GRACEFUL_TOP_13595).
		put(GRACEFUL_LEGS_13598, GRACEFUL_LEGS_13597).
		put(GRACEFUL_GLOVES_13600, GRACEFUL_GLOVES_13599).
		put(GRACEFUL_BOOTS_13602, GRACEFUL_BOOTS_13601).
		put(GRACEFUL_HOOD_13604, GRACEFUL_HOOD_13603).
		put(GRACEFUL_CAPE_13606, GRACEFUL_CAPE_13605).
		put(GRACEFUL_TOP_13608, GRACEFUL_TOP_13607).
		put(GRACEFUL_LEGS_13610, GRACEFUL_LEGS_13609).
		put(GRACEFUL_GLOVES_13612, GRACEFUL_GLOVES_13611).
		put(GRACEFUL_BOOTS_13614, GRACEFUL_BOOTS_13613).
		put(GRACEFUL_HOOD_13616, GRACEFUL_HOOD_13615).
		put(GRACEFUL_CAPE_13618, GRACEFUL_CAPE_13617).
		put(GRACEFUL_TOP_13620, GRACEFUL_TOP_13619).
		put(GRACEFUL_LEGS_13622, GRACEFUL_LEGS_13621).
		put(GRACEFUL_GLOVES_13624, GRACEFUL_GLOVES_13623).
		put(GRACEFUL_BOOTS_13626, GRACEFUL_BOOTS_13625).
		put(GRACEFUL_HOOD_13628, GRACEFUL_HOOD_13627).
		put(GRACEFUL_CAPE_13630, GRACEFUL_CAPE_13629).
		put(GRACEFUL_TOP_13632, GRACEFUL_TOP_13631).
		put(GRACEFUL_LEGS_13634, GRACEFUL_LEGS_13633).
		put(GRACEFUL_GLOVES_13636, GRACEFUL_GLOVES_13635).
		put(GRACEFUL_BOOTS_13638, GRACEFUL_BOOTS_13637).
		put(GRACEFUL_HOOD_13668, GRACEFUL_HOOD_13667).
		put(GRACEFUL_CAPE_13670, GRACEFUL_CAPE_13669).
		put(GRACEFUL_TOP_13672, GRACEFUL_TOP_13671).
		put(GRACEFUL_LEGS_13674, GRACEFUL_LEGS_13673).
		put(GRACEFUL_GLOVES_13676, GRACEFUL_GLOVES_13675).
		put(GRACEFUL_BOOTS_13678, GRACEFUL_BOOTS_13677).
		put(GRACEFUL_HOOD_21063, GRACEFUL_HOOD_21061).
		put(GRACEFUL_CAPE_21066, GRACEFUL_CAPE_21064).
		put(GRACEFUL_TOP_21069, GRACEFUL_TOP_21067).
		put(GRACEFUL_LEGS_21072, GRACEFUL_LEGS_21070).
		put(GRACEFUL_GLOVES_21075, GRACEFUL_GLOVES_21073).
		put(GRACEFUL_BOOTS_21078, GRACEFUL_BOOTS_21076).
		put(GRACEFUL_HOOD_24745, GRACEFUL_HOOD_24743).
		put(GRACEFUL_CAPE_24748, GRACEFUL_CAPE_24746).
		put(GRACEFUL_TOP_24751, GRACEFUL_TOP_24749).
		put(GRACEFUL_LEGS_24754, GRACEFUL_LEGS_24752).
		put(GRACEFUL_GLOVES_24757, GRACEFUL_GLOVES_24755).
		put(GRACEFUL_BOOTS_24760, GRACEFUL_BOOTS_24758).
		put(GRACEFUL_HOOD_25071, GRACEFUL_HOOD_25069).
		put(GRACEFUL_CAPE_25074, GRACEFUL_CAPE_25072).
		put(GRACEFUL_TOP_25077, GRACEFUL_TOP_25075).
		put(GRACEFUL_LEGS_25080, GRACEFUL_LEGS_25078).
		put(GRACEFUL_GLOVES_25083, GRACEFUL_GLOVES_25081).
		put(GRACEFUL_BOOTS_25086, GRACEFUL_BOOTS_25084).
		put(GRACEFUL_HOOD_27446, GRACEFUL_HOOD_27444).
		put(GRACEFUL_CAPE_27449, GRACEFUL_CAPE_27447).
		put(GRACEFUL_TOP_27452, GRACEFUL_TOP_27450).
		put(GRACEFUL_LEGS_27455, GRACEFUL_LEGS_27453).
		put(GRACEFUL_GLOVES_27458, GRACEFUL_GLOVES_27456).
		put(GRACEFUL_BOOTS_27461, GRACEFUL_BOOTS_27459).

		put(MAX_CAPE_13342, MAX_CAPE).

		put(SPOTTED_CAPE_10073, SPOTTED_CAPE).
		put(SPOTTIER_CAPE_10074, SPOTTIER_CAPE).

		put(AGILITY_CAPET_13341, AGILITY_CAPET).
		put(AGILITY_CAPE_13340, AGILITY_CAPE).
		build();
}
