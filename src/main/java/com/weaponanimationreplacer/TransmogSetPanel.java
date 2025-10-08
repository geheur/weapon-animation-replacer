/*
 * Copyright (c) 2018, Kamiel, <https://github.com/Kamielvf>
 * Copyright (c) 2018, Psikoi <https://github.com/psikoi>
 * Copyright (c) 2017, Adam <Adam@sigterm.info>
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

import com.google.common.primitives.Ints;
import com.weaponanimationreplacer.ChatBoxFilterableSearch.SelectionResult;
import com.weaponanimationreplacer.Constants.IdIconNameAndSlot;
import static com.weaponanimationreplacer.Constants.TriggerItemIds;
import static com.weaponanimationreplacer.Swap.AnimationReplacement;
import com.weaponanimationreplacer.Swap.AnimationType;
import static com.weaponanimationreplacer.Swap.AnimationType.ATTACK;
import com.weaponanimationreplacer.Swap.SoundSwap;
import com.weaponanimationreplacer.WeaponAnimationReplacerPlugin.SearchType;
import static com.weaponanimationreplacer.WeaponAnimationReplacerPlugin.SearchType.MODEL_SWAP;
import static com.weaponanimationreplacer.WeaponAnimationReplacerPlugin.SearchType.SPELL_L;
import static com.weaponanimationreplacer.WeaponAnimationReplacerPlugin.SearchType.SPELL_R;
import static com.weaponanimationreplacer.WeaponAnimationReplacerPlugin.SearchType.TRIGGER_ITEM;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.Player;
import net.runelite.api.Projectile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.api.kit.KitType;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.ColorJButton;
import net.runelite.client.ui.components.FlatTextField;
import net.runelite.client.ui.components.colorpicker.RuneliteColorPicker;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.ImageUtil;

@Slf4j
class TransmogSetPanel extends JPanel
{
	private static final int DEFAULT_FILL_OPACITY = 75;

	private static final Border NAME_BOTTOM_BORDER = new CompoundBorder(
		BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.DARK_GRAY_COLOR),
		BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_COLOR));

	private static final ImageIcon MOVE_RULE_UP_ICON;
	private static final ImageIcon MOVE_RULE_UP_ICON_HOVER;
	private static final ImageIcon MOVE_RULE_DOWN_ICON;
	private static final ImageIcon MOVE_RULE_DOWN_ICON_HOVER;

	private static final ImageIcon DELETE_RULE_ICON;
	private static final ImageIcon DELETE_RULE_ICON_HOVER;

	private static final ImageIcon EDIT_ICON;
	private static final ImageIcon EDIT_ICON_HOVER;

	private static final ImageIcon ADD_ICON;
	private static final ImageIcon ADD_HOVER_ICON;

	private static final ImageIcon GEAR_ICON;
	private static final ImageIcon GEAR_HOVER_ICON;

	private static final ImageIcon VISIBLE_ICON;
	private static final ImageIcon INVISIBLE_ICON;

	private final WeaponAnimationReplacerPlugin plugin;
	private final int index;
	private final TransmogSet transmogSet;

	private final FlatTextField nameInput = new FlatTextField();

	private WeaponAnimationReplacerPluginPanel pluginPanel;

	static
	{
		final BufferedImage upImg = ImageUtil.loadImageResource(TransmogSetPanel.class, "up_small.png");
		MOVE_RULE_UP_ICON = new ImageIcon(upImg);
		MOVE_RULE_UP_ICON_HOVER = new ImageIcon(ImageUtil.luminanceOffset(upImg, -150));

		final BufferedImage downImg = ImageUtil.loadImageResource(TransmogSetPanel.class, "down_small.png");
		MOVE_RULE_DOWN_ICON = new ImageIcon(downImg);
		MOVE_RULE_DOWN_ICON_HOVER = new ImageIcon(ImageUtil.luminanceOffset(downImg, -150));

		final BufferedImage deleteImg = ImageUtil.loadImageResource(TransmogSetPanel.class, "delete.png");
		DELETE_RULE_ICON = new ImageIcon(deleteImg);
		DELETE_RULE_ICON_HOVER = new ImageIcon(ImageUtil.luminanceOffset(deleteImg, -50));

		final BufferedImage editImg = ImageUtil.loadImageResource(TransmogSetPanel.class, "edit.png");
		EDIT_ICON = new ImageIcon(editImg);
		EDIT_ICON_HOVER = new ImageIcon(ImageUtil.luminanceOffset(editImg, -150));

		final BufferedImage addIcon = ImageUtil.loadImageResource(TransmogSetPanel.class, "add_icon.png");
		ADD_ICON = new ImageIcon(addIcon);
		ADD_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(addIcon, 0.53f));

		final BufferedImage gearIcon = ImageUtil.loadImageResource(TransmogSetPanel.class, "gear_icon.png");
		GEAR_ICON = new ImageIcon(gearIcon);
		GEAR_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(gearIcon, 0.53f));

		final BufferedImage visibleIcon = ImageUtil.loadImageResource(TransmogSetPanel.class, "visible_icon.png");
		VISIBLE_ICON = new ImageIcon(visibleIcon);

		final BufferedImage invisibleIcon = ImageUtil.loadImageResource(TransmogSetPanel.class, "invisible_icon.png");
		INVISIBLE_ICON = new ImageIcon(invisibleIcon);
	}

	TransmogSetPanel(WeaponAnimationReplacerPlugin plugin, TransmogSet transmogSet, WeaponAnimationReplacerPluginPanel pluginPanel, int index)
	{
		this.plugin = plugin;
		this.transmogSet = transmogSet;
		this.pluginPanel = pluginPanel;
		this.index = index;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARKER_GRAY_COLOR);

		add(createNamePanel(), BorderLayout.NORTH);
		if (!transmogSet.isMinimized()) add(createBottomPanel(), BorderLayout.CENTER);
	}

	private JPanel createBottomPanel() {
		JPanel bottomContainer = new JPanel();
		bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.Y_AXIS));
		bottomContainer.setBorder(new EmptyBorder(8, 0, 8, 0));
		bottomContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		for (int i = 0; i < transmogSet.getSwaps().size(); i++)
		{
			bottomContainer.add(createSwapPanel(transmogSet.getSwaps().get(i), i, transmogSet.getSwaps().size()));
		}
		JButton addSwapButton = new JButton("add swap");
		addSwapButton.addActionListener(e -> addNewSwap(transmogSet));

		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(addSwapButton);
		bottomContainer.add(p);

		bottomPanel = bottomContainer;
		return bottomContainer;
	}

	private Component createSwapPanel(Swap swap, int index, int total)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		JPanel restrictionAndModelSwapPanel = getRestrictionAndModelSwapPanel();
		restrictionAndModelSwapPanel.add(createSwapOptionsPanel(transmogSet, swap, index != 0, index != total - 1));

		List<Integer> itemRestrictions = swap.getItemRestrictions();
		if (itemRestrictions.isEmpty()) {
			itemRestrictions = Collections.singletonList(-1); // "Any" button.
		}
		for (Integer itemRestriction : itemRestrictions)
		{
			restrictionAndModelSwapPanel.add(createItemRestrictionButton(swap, itemRestriction));
		}

		restrictionAndModelSwapPanel.add(new JLabel("->"));

		List<Integer> modelSwaps = swap.getModelSwaps();
		if (modelSwaps.isEmpty()) {
			modelSwaps = Collections.singletonList(-1); // "None" button.
		}
		for (Integer modelSwap : modelSwaps)
		{
			restrictionAndModelSwapPanel.add(createModelSwapButton(swap, modelSwap));
		}

		panel.add(restrictionAndModelSwapPanel, BorderLayout.NORTH);

		JPanel animationSwapsPanel = new JPanel();
		animationSwapsPanel.setLayout(new BoxLayout(animationSwapsPanel, BoxLayout.Y_AXIS));
		if (!swap.animationReplacements.isEmpty()) {
			for (int i = 0; i < swap.animationReplacements.size(); i++)
			{
				animationSwapsPanel.add(createAnimationReplacementPanel(swap, i, swap.animationReplacements.size()));
			}
		}
		if (!swap.getProjectileSwaps().isEmpty()) {
			for (int i = 0; i < swap.getProjectileSwaps().size(); i++)
			{
				animationSwapsPanel.add(createProjectileSwapPanel(swap, i, swap.getProjectileSwaps().size()));
			}
		}
		if (!swap.getGraphicEffects().isEmpty()) {
			for (int i = 0; i < swap.getGraphicEffects().size(); i++)
			{
				animationSwapsPanel.add(createGraphicsEffectPanel(swap, i, swap.getGraphicEffects().size()));
			}
		}
		if (!swap.getSoundSwaps().isEmpty()) {
			for (int i = 0; i < swap.getSoundSwaps().size(); i++)
			{
				animationSwapsPanel.add(createSoundSwapPanel(swap, i, swap.getSoundSwaps().size()));
			}
		}
		panel.add(animationSwapsPanel, BorderLayout.CENTER);

		return panel;
	}

	// What in the absolute fuck. FlowLayout claims to use multiple rows but apparently it give not a single shit that more than the first row is visible. What the fuck. What a fucking waste of my fucking time. Gaslightning documentation, fuck you.
	/* :MonkaChrist: */
	private JPanel getRestrictionAndModelSwapPanel()
	{
		JPanel restrictionAndModelSwapPanel = new JPanel(new FlowLayout(FlowLayout.LEFT) {
			@Override
			public Dimension minimumLayoutSize(Container target)
			{
				int parentWidth = target.getParent().getWidth();//target.getParent().getWidth();
				if (parentWidth == 0) parentWidth = 215;

				int nmembers = target.getComponentCount();
				int y = 0;
				int x = getInsets().left + getInsets().right + getHgap() * 2;
				for (int i = 0; i < nmembers; i++)
				{
					Component component = target.getComponent(i);
					int newX = (int) (x + component.getPreferredSize().getWidth());
					if (newX > parentWidth) {
						y += 40;
						newX = (int) (component.getPreferredSize().getWidth() + getHgap());
					}
					x = newX + getHgap();
				}
				return new Dimension(super.minimumLayoutSize(target).width, y + 40);
			}

			@Override
			public Dimension preferredLayoutSize(Container target)
			{
				return minimumLayoutSize(target);
			}
		}) {
//			@Override
//			public Dimension getMaximumSize()
//			{
//				return getPreferredSize();
//			}
		};
		return restrictionAndModelSwapPanel;
	}

	private Component createItemRestrictionButton(Swap swap, int initialItemId)
	{
		ItemSelectionButton button = new ItemSelectionButton();

		button.nameWhenEmpty = "Any";
		IdIconNameAndSlot hiddenSlot = TriggerItemIds.getHiddenSlot(initialItemId);
		int displayIconId = initialItemId;
		if (hiddenSlot != null) {
			displayIconId = hiddenSlot.getIconId();
			button.showNotSign = true;
			button.setItem(displayIconId, hiddenSlot.getName());
		} else {
			button.setItem(displayIconId, initialItemId);
		}

		button.addListeners(() -> swap.removeTriggerItem(initialItemId), (result, plugin) -> swap.addTriggerItem(result.itemId, result.slot, plugin), TRIGGER_ITEM, swap);
		return button;
	}

	private Component createModelSwapButton(Swap swap, int initialItemId)
	{
		ItemSelectionButton button = new ItemSelectionButton();
		button.nameWhenEmpty = "None";
		int slotOverride = swap.getSlotOverride(initialItemId);
		if (slotOverride != -1) button.overlayString = KitType.values()[slotOverride].name().toLowerCase();
		if (initialItemId < 0) {
			IdIconNameAndSlot idIconNameAndSlot = Constants.getModelSwap(initialItemId);
			button.showNotSign = idIconNameAndSlot.isShowNotSign();
			button.setItem(idIconNameAndSlot.getIconId(), idIconNameAndSlot.getName());
		} else {
			button.setItem(initialItemId);
		}
		button.addListeners(() -> swap.removeModelSwap(initialItemId), (result, plugin) -> swap.addModelSwap(result.itemId, plugin, result.slot), MODEL_SWAP, swap);
		return button;
	}

	private Component createSpellSwapLButton(ProjectileSwap swap)
	{
		ItemSelectionButton button = new ItemSelectionButton();
		button.nameWhenEmpty = "None";
		button.setSpell(swap.toReplace, swap.toReplaceCustom != null);
		button.addListeners(() -> swap.toReplace = -1, (result, plugin) -> swap.toReplace = result.itemId, SPELL_L, null);
		return button;
	}

	private Component createSpellSwapRButton(ProjectileSwap swap)
	{
		ItemSelectionButton button = new ItemSelectionButton();
		button.nameWhenEmpty = "None";
		button.setSpell(swap.toReplaceWith, swap.toReplaceWithCustom != null);
		button.addListeners(() -> swap.toReplaceWith = -1, (result, plugin) -> {swap.toReplaceWith = result.itemId; swap.toReplaceWithCustom = null;}, SPELL_R, null);
		return button;
	}

	private Component createSwapOptionsPanel(TransmogSet transmogSet, Swap swap, boolean moveUp, boolean moveDown)
	{
		JLabel label = new IconLabelButton(GEAR_ICON, GEAR_HOVER_ICON, () -> {}, "Options");
//		JLabel label = new JLabel(GEAR_ICON);
		final JPopupMenu menu = new JPopupMenu();
		final Color labelForeground = label.getForeground();
		menu.setBorder(new EmptyBorder(5, 5, 5, 5));

		addMenuItem(menu, "Add trigger item", e -> addTriggerItem(swap));
		addMenuItem(menu, "Add model swap", e -> addModelSwap(swap));
		addMenuItem(menu, "Add animation swap", e -> addAnimationReplacement(swap));
		addMenuItem(menu, "Add attack swap", e -> addProjectileSwap(swap));
		addMenuItem(menu, "Add graphic effect", e -> addGraphicEffect(swap));
		addMenuItem(menu, "Add sound swap", e -> addSoundSwap(swap));

		if (moveUp) addMenuItem(menu, "Move up", e -> moveSwap(transmogSet, swap, -1));
		if (moveDown) addMenuItem(menu, "Move down", e -> moveSwap(transmogSet, swap, +1));

		addMenuItem(menu, "Remove swap", e -> removeSwap(transmogSet, swap));

		label.addMouseListener(new MouseAdapter()
		{
			private Color lastForeground;

			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				Component source = (Component) mouseEvent.getSource();
				Point location = MouseInfo.getPointerInfo().getLocation();
				SwingUtilities.convertPointFromScreen(location, source);
				menu.show(source, location.x, location.y);
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				lastForeground = label.getForeground();
				label.setForeground(ColorScheme.BRAND_ORANGE);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				label.setForeground(lastForeground);
			}
		});

		return label;
	}

	private void addNewSwap(TransmogSet transmogSet)
	{
		transmogSet.addNewSwap();
		SwingUtilities.invokeLater(this::rebuild);
	}

	private void removeSwap(TransmogSet transmogSet, Swap swap)
	{
		transmogSet.removeSwap(swap);
		plugin.clientThread.invokeLater(plugin::handleTransmogSetChange);
		SwingUtilities.invokeLater(this::rebuild);
	}

	private void moveSwap(TransmogSet transmogSet, Swap swap, int i)
	{
		transmogSet.moveSwap(swap, i);
		plugin.clientThread.invokeLater(plugin::handleTransmogSetChange);
		SwingUtilities.invokeLater(this::rebuild);
	}

	private void addAnimationReplacement(Swap swap)
	{
		plugin.clientThread.invokeLater(() -> {
			swap.addNewAnimationReplacement();
			plugin.handleTransmogSetChange();
			SwingUtilities.invokeLater(this::rebuild);
		});
	}

	private void addProjectileSwap(Swap swap)
	{
		swap.addNewProjectileSwap();
		plugin.clientThread.invokeLater(plugin::handleTransmogSetChange);
		SwingUtilities.invokeLater(this::rebuild);
	}

	private void addGraphicEffect(Swap swap)
	{
		swap.addNewGraphicEffect();
		plugin.clientThread.invokeLater(plugin::handleTransmogSetChange);
		SwingUtilities.invokeLater(this::rebuild);
	}

	private void addSoundSwap(Swap swap)
	{
		swap.addNewSoundSwap();
		plugin.clientThread.invokeLater(plugin::handleTransmogSetChange);
		SwingUtilities.invokeLater(this::rebuild);
	}

	// TODO threading, memory consistency? on which threads am I doing what. I want swaps to be modified on the client thread only, I think.
	private void addModelSwap(Swap swap)
	{
		plugin.doItemSearch(
			result -> {
				swap.addModelSwap(result.itemId, plugin, result.slot);
				plugin.handleTransmogSetChange();
				SwingUtilities.invokeLater(this::rebuild);
			},
			() -> {},
			MODEL_SWAP,
			swap
		);
	}

	private void addTriggerItem(Swap swap)
	{
		plugin.doItemSearch(
			result -> {
				swap.addTriggerItem(result.itemId, plugin);
				plugin.handleTransmogSetChange();
				SwingUtilities.invokeLater(this::rebuild);
			},
			TRIGGER_ITEM
		);
	}

	private void addMenuItem(JPopupMenu menu, String name, ActionListener actionListener)
	{
		JMenuItem menuItem = new JMenuItem(name);
		menuItem.addActionListener(actionListener);
		menu.add(menuItem);
	}

	public static class IconLabelButton extends JLabel {
		private final Icon icon;
		private final Icon iconMouseovered;
		private Runnable onClick;
		public IconLabelButton(Icon icon, Icon iconMouseovered, Runnable onClick, String tooltip) {
			this.icon = icon;
			this.iconMouseovered = iconMouseovered;
			this.onClick = onClick;
			setIcon(icon);
			setToolTipText(tooltip);

			addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent mouseEvent)
				{
					IconLabelButton.this.onClick.run();
				}

				@Override
				public void mouseEntered(MouseEvent mouseEvent)
				{
					setIcon(iconMouseovered);
				}

				@Override
				public void mouseExited(MouseEvent mouseEvent)
				{
					setIcon(icon);
				}
			});
		}
	}

	private Component bottomPanel;

	private void rebuild() {
		if (bottomPanel != null)
		{
			remove(bottomPanel);
			bottomPanel = null;
		}
		if (!transmogSet.isMinimized()) add(createBottomPanel(), BorderLayout.CENTER);
		pluginPanel.revalidate();
	}

	public class EntryPanel extends JPanel {
		public EntryPanel(boolean checkbox, boolean enabled, boolean x, boolean plus, JPanel panel, Runnable onDelete, Runnable onAdd, Consumer<Boolean> onEnable) {
			this(checkbox, enabled, false, false, false, false, false, x, plus, panel, onDelete, onAdd, onEnable);
		}

		public EntryPanel(boolean checkbox, boolean enabled, boolean minimize, boolean minimized, boolean updown, boolean up, boolean down, boolean x, boolean plus, JPanel panel, Runnable onDelete, Runnable onAdd, Consumer<Boolean> onEnable) {
			setLayout(new BorderLayout());
			setBackground(ColorScheme.DARKER_GRAY_COLOR);
			if (checkbox) {
				JCheckBox enabledCheckbox = new JCheckBox();
//				enabledCheckbox.setToolTipText("Enabled");
				enabledCheckbox.setSelected(enabled);
				enabledCheckbox.setIcon(enabled ? VISIBLE_ICON : INVISIBLE_ICON);
				enabledCheckbox.addActionListener((e) -> {
					plugin.clientThread.invokeLater(() -> {
						onEnable.accept(enabledCheckbox.isSelected());
					});
					enabledCheckbox.setIcon(enabledCheckbox.isSelected() ? VISIBLE_ICON : INVISIBLE_ICON);
				});
				add(enabledCheckbox, BorderLayout.WEST);
			}
			JPanel rightSide = new JPanel();
			rightSide.setLayout(new BoxLayout(rightSide, BoxLayout.X_AXIS));
			rightSide.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			if (minimize) {
				JLabel xButton = makeButton(minimized ? " + " : " - ", () -> {
					plugin.clientThread.invokeLater(() -> {
						transmogSet.setMinimized(!minimized);;
						SwingUtilities.invokeLater(TransmogSetPanel.this::rebuild);
					});
				});
				rightSide.add(xButton);
			}
			if (updown) {
				rightSide.add(new IconLabelButton(MOVE_RULE_UP_ICON, MOVE_RULE_UP_ICON_HOVER, () -> {
					plugin.clientThread.invokeLater(() -> {
						plugin.moveTransmogSet(index, true);
					});
				}, "Move up in list and priority"));
				rightSide.add(new IconLabelButton(MOVE_RULE_DOWN_ICON, MOVE_RULE_DOWN_ICON_HOVER, () -> {
					plugin.clientThread.invokeLater(() -> {
						plugin.moveTransmogSet(index, false);
					});
				}, "Move down in list and priority"));
			}
			if (x) {
				rightSide.add(new IconLabelButton(DELETE_RULE_ICON, DELETE_RULE_ICON_HOVER, onDelete, "Delete"));
			}
			if (plus) {
				rightSide.add(new IconLabelButton(ADD_ICON, ADD_HOVER_ICON, onAdd, "Add another"));
			}
			add(rightSide, BorderLayout.EAST);

			add(panel, BorderLayout.CENTER);
		}

		private JLabel makeButton(String text, Runnable onClick) {
			JLabel label = new JLabel(text);
//			label.setToolTipText("Add a new animation replacement rule.");
			label.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent mouseEvent)
				{
					onClick.run();
				}

//				@Override
//				public void mouseEntered(MouseEvent mouseEvent)
//				{
//					label.setIcon(ADD_HOVER_ICON);
//				}
//
//				@Override
//				public void mouseExited(MouseEvent mouseEvent)
//				{
//					label.setIcon(ADD_ICON);
//				}
			});
			return label;
		}
	}

	private Component createAnimationReplacementPanel(Swap swap, int i, int size) {
		AnimationReplacement animationReplacement = swap.animationReplacements.get(i);

		JPanel animationReplacementPanel = new JPanel();
		animationReplacementPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
		animationReplacementPanel.setLayout(new BoxLayout(animationReplacementPanel, BoxLayout.Y_AXIS));
		animationReplacementPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		if (animationReplacement.auto != -1) {
			JPanel p = new JPanel();
			p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
			JLabel autoLabel = new JLabel("(auto-generated animation swap)");
			p.add(autoLabel);
			animationReplacementPanel.add(p);
		}

		JPanel row1 = new JPanel();
		row1.setLayout(new BoxLayout(row1, BoxLayout.X_AXIS));
		row1.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		row1.add(new JLabel("Replace "));

		JComboBox<AnimationType> animToReplace = new JComboBox<>(AnimationType.comboBoxOrder.toArray(new AnimationType[] {}));
		animToReplace.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				Component rendererComponent = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				setText(value == null ? "<choose>" : ((AnimationType) value).getComboBoxName());
				return rendererComponent;
			}
		});
		animToReplace.setSelectedItem(animationReplacement.animationtypeToReplace);
		animToReplace.setPrototypeDisplayValue(AnimationType.RUN);
		animToReplace.addActionListener((e) -> {
			plugin.clientThread.invokeLater(() -> {
				animationReplacement.animationtypeToReplace = (AnimationType) animToReplace.getSelectedItem();
				animationReplacement.auto = -1;
				if (!ATTACK.appliesTo(animationReplacement.animationtypeToReplace)) {
					animationReplacement.animationtypeReplacement = null;
				}
				plugin.handleTransmogSetChange();
				SwingUtilities.invokeLater(this::rebuild);
			});
		});
		row1.add(animToReplace);
		animationReplacementPanel.add(row1);

		JPanel row2 = new JPanel();
		row2.setLayout(new BoxLayout(row2, BoxLayout.X_AXIS));
		row2.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		row2.add(new JLabel("with"));
		JComboBox<AnimationSet> animationSetToUse = new JComboBox<>(Constants.animationSets.toArray(new AnimationSet[]{}));
		animationSetToUse.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				Component rendererComponent = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				setText(value == null ? "<choose>" : ((AnimationSet) value).getComboBoxName());
				return rendererComponent;
			}
		});
		animationSetToUse.setSelectedItem(animationReplacement.animationSet);
		animationSetToUse.addActionListener((e) -> {
			plugin.clientThread.invokeLater(() -> {
				animationReplacement.animationSet = (AnimationSet) animationSetToUse.getSelectedItem();
				animationReplacement.auto = -1;
				if (ATTACK.appliesTo(animationReplacement.animationtypeToReplace) && animationReplacement.animationSet != null)
				{
					List<AnimationType> actions = animationReplacement.animationSet.getAttackAnimations();
					Optional<AnimationType> match = actions.stream().filter(action -> action == animationReplacement.animationtypeReplacement).findAny();
					if (match.isPresent()) {
						animationReplacement.animationtypeReplacement = match.get();
					} else if (!actions.isEmpty()) {
						animationReplacement.animationtypeReplacement = actions.get(0);
					}
				}
				plugin.handleTransmogSetChange();
				SwingUtilities.invokeLater(this::rebuild);
			});
		});
		row2.add(animationSetToUse);
		animationReplacementPanel.add(row2);

		if (ATTACK.appliesTo(animationReplacement.animationtypeToReplace) && animationReplacement.animationSet != null) {
			JPanel row3 = new JPanel();
			row3.setLayout(new BoxLayout(row3, BoxLayout.X_AXIS));
			row3.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			row3.add(new JLabel("attack animation:"));
			List<AnimationType> actions = animationReplacement.animationSet.getAttackAnimations();
			log.debug("actions is : " + actions);
			JComboBox<AnimationType> attackToUse = new JComboBox<>(actions.toArray(new AnimationType[] {})); // TODO remove indenting?
			// TODO add "automatic" option.
			attackToUse.setRenderer(new DefaultListCellRenderer() {
				@Override
				public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
					Component rendererComponent = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					if (value == null || !actions.contains(value)) {
						setText("<choose>");
					} else {
						setText(AnimationSet.getDescription(animationReplacement.animationSet, (AnimationType) value));
					}
					return rendererComponent;
				}
			});
			attackToUse.setSelectedItem(animationReplacement.animationtypeReplacement);
			// Update the rule to reflect the dropdown. This is relevant if the list of items in the dropdown does not contain the original replacement.
			animationReplacement.animationtypeReplacement = ((AnimationType) attackToUse.getSelectedItem());
			attackToUse.addActionListener((e) -> {
				plugin.clientThread.invokeLater(() -> {
					animationReplacement.animationtypeReplacement = ((AnimationType) attackToUse.getSelectedItem());
					animationReplacement.auto = -1;
					plugin.handleTransmogSetChange();

					plugin.demoAnimation(animationReplacement.animationSet.getAnimation(animationReplacement.animationtypeReplacement));
				});
			});
			row3.add(attackToUse);
//			animToReplace.setPrototypeDisplayValue("___");
			animationReplacementPanel.add(row3);
		}

		return new EntryPanel(false, true, true, i == size - 1, animationReplacementPanel, () -> {
			swap.animationReplacements.remove(i);
			plugin.clientThread.invoke(plugin::handleTransmogSetChange);
			SwingUtilities.invokeLater(this::rebuild);
		}, () -> {
			plugin.clientThread.invokeLater(() -> {
				swap.addNewAnimationReplacement();
				plugin.handleTransmogSetChange();
				SwingUtilities.invokeLater(this::rebuild);
			});
		}, (enabled) -> {
			plugin.clientThread.invoke(plugin::handleTransmogSetChange);
		});
	}

	private Component createProjectileSwapPanel(Swap swap, int i, int size)
	{
		ProjectileSwap projectileSwap = swap.getProjectileSwaps().get(i);

		JPanel animationReplacementPanel = new JPanel();
		animationReplacementPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
		animationReplacementPanel.setLayout(new BoxLayout(animationReplacementPanel, BoxLayout.Y_AXIS));
		animationReplacementPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel row1 = new JPanel();
		row1.setLayout(new BoxLayout(row1, BoxLayout.X_AXIS));
		row1.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel projectileSwapPanel = getRestrictionAndModelSwapPanel();
		projectileSwapPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		projectileSwapPanel.add(createSpellSwapLButton(projectileSwap));
		projectileSwapPanel.add(createSpellEditPanel(swap, i, false));
		projectileSwapPanel.add(new JLabel("->"));
		projectileSwapPanel.add(createSpellSwapRButton(projectileSwap));
		projectileSwapPanel.add(createSpellEditPanel(swap, i, true));
		row1.add(projectileSwapPanel);
		animationReplacementPanel.add(row1);
		if (pluginPanel.currentPsSwap == swap && pluginPanel.currentPsIndex == i) {
			JPanel row = createProjectileEditPanel(projectileSwap, pluginPanel.currentPsRhs);
			animationReplacementPanel.add(row);
		}

		return new EntryPanel(false, true, true, i == size - 1, animationReplacementPanel, () -> {
			swap.getProjectileSwaps().remove(i);
			plugin.clientThread.invoke(plugin::handleTransmogSetChange);
			SwingUtilities.invokeLater(this::rebuild);
		}, () -> {
			plugin.clientThread.invokeLater(() -> {
				swap.addNewProjectileSwap();
				plugin.handleTransmogSetChange();
				SwingUtilities.invokeLater(this::rebuild);
			});
		}, (enabled) -> {
			plugin.clientThread.invoke(plugin::handleTransmogSetChange);
		});
	}

	private JPanel createProjectileEditPanel(ProjectileSwap ps, boolean rhs)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		ProjectileCast defaultValue = rhs ? ps.getToReplaceWith() : ps.getToReplace();
		if (defaultValue == null) defaultValue = ProjectileCast.p().build();

		createProjectileEditPanelRow("anim id", ce -> {
			ps.createCustomIfNull(rhs);
			ProjectileCast pc = ps.getCustom(rhs);
			pc.setCastAnimation((int) ((JSpinner) ce.getSource()).getValue());
			plugin.saveTransmogSets();
			plugin.demoCast(pc);
		}, defaultValue.castAnimation, panel);
		createProjectileEditPanelRow("cast gfx", ce -> {
			ps.createCustomIfNull(rhs);
			ProjectileCast pc = ps.getCustom(rhs);
			pc.setCastGfx((int) ((JSpinner) ce.getSource()).getValue());
			plugin.saveTransmogSets();
			plugin.demoCast(pc);
		}, defaultValue.castGfx, panel);
		if (rhs) createProjectileEditPanelRow("cast gfx height", ce -> {
			ps.createCustomIfNull(rhs);
			ProjectileCast pc = ps.getCustom(rhs);
			pc.setCastGfxHeight((int) ((JSpinner) ce.getSource()).getValue());
			plugin.saveTransmogSets();
			plugin.demoCast(pc);
		}, defaultValue.castGfxHeight, panel);
		createProjectileEditPanelRow("hit gfx", ce -> {
			ps.createCustomIfNull(rhs);
			ProjectileCast pc = ps.getCustom(rhs);
			pc.setHitGfx((int) ((JSpinner) ce.getSource()).getValue());
			plugin.saveTransmogSets();
			plugin.client.getLocalPlayer().createSpotAnim("demo".hashCode(), pc.hitGfx, 0, 0);
		}, defaultValue.hitGfx, panel);
		if (rhs) createProjectileEditPanelRow("hit gfx height", ce -> {
			ps.createCustomIfNull(rhs);
			ProjectileCast pc = ps.getCustom(rhs);
			pc.setHitGfxHeight((int) ((JSpinner) ce.getSource()).getValue());
			plugin.saveTransmogSets();
			plugin.client.getLocalPlayer().createSpotAnim("demo".hashCode(), pc.hitGfxHeight, 0, 0);
		}, defaultValue.hitGfxHeight, panel);
		createProjectileEditPanelRow("projectile id", ce -> {
			ps.createCustomIfNull(rhs);
			ProjectileCast pc = ps.getCustom(rhs);
			pc.setProjectileId((int) ((JSpinner) ce.getSource()).getValue());
			plugin.saveTransmogSets();
			plugin.demoCast(pc);
		}, defaultValue.projectileId, panel);
		if (rhs) createProjectileEditPanelRow("arc", ce -> {
			ps.createCustomIfNull(rhs);
			ProjectileCast pc = ps.getCustom(rhs);
			pc.setSlope((int) ((JSpinner) ce.getSource()).getValue());
			plugin.saveTransmogSets();
			plugin.demoCast(pc);
		}, defaultValue.slope, panel, -64, 64);
		if (rhs) createProjectileEditPanelRow("delay", ce -> {
			ps.createCustomIfNull(rhs);
			ProjectileCast pc = ps.getCustom(rhs);
			pc.setStartMovement((int) ((JSpinner) ce.getSource()).getValue());
			plugin.saveTransmogSets();
			plugin.demoCast(pc);
		}, defaultValue.startMovement, panel);
		if (rhs) createProjectileEditPanelRow("start offset", ce -> {
			ps.createCustomIfNull(rhs);
			ProjectileCast pc = ps.getCustom(rhs);
			pc.setStartPos((int) ((JSpinner) ce.getSource()).getValue());
			plugin.saveTransmogSets();
			plugin.demoCast(pc);
		}, defaultValue.startPos, panel);
		if (rhs) createProjectileEditPanelRow("start height", ce -> {
			ps.createCustomIfNull(rhs);
			ProjectileCast pc = ps.getCustom(rhs);
			pc.setStartHeight((int) ((JSpinner) ce.getSource()).getValue());
			plugin.saveTransmogSets();
			plugin.demoCast(pc);
		}, defaultValue.startHeight, panel, Integer.MIN_VALUE, Integer.MAX_VALUE);
		if (rhs) createProjectileEditPanelRow("end height", ce -> {
			ps.createCustomIfNull(rhs);
			ProjectileCast pc = ps.getCustom(rhs);
			pc.setEndHeight((int) ((JSpinner) ce.getSource()).getValue());
			plugin.saveTransmogSets();
			plugin.demoCast(pc);
		}, defaultValue.endHeight, panel);
		JButton demo = new JButton("demo");
		demo.addActionListener(al -> plugin.demoCast(rhs ? ps.getToReplaceWith() : ps.getToReplace()));
		panel.add(demo);
		JButton projectileIdsButton = new JButton("id finder");
		projectileIdsButton.addActionListener(al -> {
			new ProjectileIdsFrame().setVisible(true);
		});
		panel.add(projectileIdsButton);

		return panel;
	}

	class ProjectileIdsFrame extends JFrame {
		private JPanel panel;

		private Set<Projectile> lastTickProjectiles = new HashSet<>();

		private boolean debug = false;

		public ProjectileIdsFrame() {
			super("Ids your character is doing.");
			setSize(800, 500);
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowClosing(WindowEvent e)
				{
					plugin.eventBus.unregister(this);
				}
			});
			panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			plugin.eventBus.register(this);
			plugin.registerProjectileIdsFrame(this);
			JPanel squeezePanel = new JPanel(new BorderLayout());
			squeezePanel.add(panel, BorderLayout.NORTH);
			add(squeezePanel);
		}

		@Value
		private class PCwI {
			ProjectileCast pc;
			int i;
		}

		private final List<PCwI> liveProjectiles = new ArrayList<>();
		private final List<PCwI> finishedProjectiles = new ArrayList<>();

		private boolean paused = false;

		public void spell()
		{
			if (paused) return;
			Set<Projectile> thisTickProjectiles = new HashSet<>();
			Projectile projectile = null;
			Client client = plugin.client;
			Player player = client.getLocalPlayer();
			for (Projectile p : client.getProjectiles()) {
				if (!lastTickProjectiles.contains(p)) {
					if (client.getGameCycle() > p.getStartCycle()) continue; // skip already seen projectiles.

					// This is the player's actual location which is what projectiles use as their start position. Player#getX, #getSceneX, etc., do not work here.
					final WorldPoint playerPos = player.getWorldLocation();
					if (playerPos == null) continue;
					final LocalPoint playerPosLocal = LocalPoint.fromWorld(client, playerPos);
					if (playerPosLocal == null) continue;

					if (p.getX1() != playerPosLocal.getX() || p.getY1() != playerPosLocal.getY()) continue;
					projectile = p;
					break;
				}
				thisTickProjectiles.add(p);
			}
			lastTickProjectiles = thisTickProjectiles;

			Actor interacting = player.getInteracting();
			ProjectileCast.ProjectileCastBuilder builder = new ProjectileCast.ProjectileCastBuilder();
			int castAnimation = player.getAnimation();
			int castGfx = player.getGraphic();
			int castGfxHeight = player.getGraphicHeight();
			builder.cast(castAnimation, castGfx, castGfxHeight);
			if (interacting != null) {
				int hitGfx = interacting.getGraphic();
				int hitGfxHeight = interacting.getGraphicHeight();
				builder.hit(hitGfx, hitGfxHeight);
			}
			if (projectile != null) {
				int startMovement = projectile.getStartCycle() - client.getGameCycle();
				int startPos = projectile.getStartPos();
				int startHeight = projectile.getStartHeight();
				int endHeight = projectile.getEndHeight();
				int slope = projectile.getSlope();
				builder.projectile(projectile.getId(), startMovement, startPos, startHeight, endHeight, slope);
			}
			builder.artificial();
			liveProjectiles.add(new PCwI(builder.build(), projectile != null ? projectile.getEndCycle() : client.getGameCycle() + 100));
		}

		@Subscribe
		public void onClientTick(ClientTick e) {
			boolean redraw = false;

			outer:
			for (int j = 0; j < liveProjectiles.size(); j++)
			{
				PCwI liveProjectile = liveProjectiles.get(j);
				if (liveProjectile.i > plugin.client.getGameCycle()) continue;
				liveProjectiles.remove(j);
				j--;
				finishedProjectiles.add(new PCwI(liveProjectile.pc, 0));
				for (int i = 0; i < finishedProjectiles.size(); i++)
				{
					PCwI finishedProjectile = finishedProjectiles.get(i);
					if (finishedProjectile.pc.equals(liveProjectile.pc)) {
						PCwI remove = finishedProjectiles.remove(i);
						finishedProjectiles.add(new PCwI(remove.pc, remove.i + 1));
						redraw = true;
						continue outer;
					}
				}
			}

			if (redraw)
			{
				redraw();
			}
		}

		private void redraw()
		{
			SwingUtilities.invokeLater(() -> {
				panel.removeAll();

				JPanel row = new JPanel();
				row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
				JCheckBox pause = new JCheckBox("pause", paused);
				pause.addItemListener(ce -> {
					paused = ((JCheckBox) ce.getSource()).isSelected();
				});
				row.add(pause);
				JCheckBox debugCheckbox = new JCheckBox("author use", debug);
				debugCheckbox.addItemListener(ce -> {
					debug = ((JCheckBox) ce.getSource()).isSelected();
					redraw();
				});
				row.add(debugCheckbox);
				panel.add(row);

				for (PCwI projectile : finishedProjectiles)
				{
					JPanel panel1 = new JPanel();
					panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));
					JButton demo = new JButton("demo");
					ProjectileCast pc = projectile.pc;
					demo.addActionListener(al -> plugin.demoCast(pc));
					panel1.add(demo);
					JButton use = new JButton("use");
					use.addActionListener(al -> {
						if (pluginPanel.currentPsSwap == null || pluginPanel.currentPsIndex == -1)
						{
							panel.add(new JLabel("not editing a projectile swap"));
							panel.revalidate();
							panel.repaint();
							return;
						}
						ProjectileSwap projectileSwap = pluginPanel.currentPsSwap.getProjectileSwaps().get(pluginPanel.currentPsIndex);
						ProjectileCast replacement = pluginPanel.currentPsRhs ? projectileSwap.toReplaceWithCustom : projectileSwap.toReplaceCustom;
						projectileSwap.createCustomIfNull(pluginPanel.currentPsRhs);
						replacement.castAnimation = pc.castAnimation;
						replacement.castGfx = pc.castGfx;
						replacement.castGfxHeight = pc.castGfxHeight;
						replacement.hitGfx = pc.hitGfx;
						replacement.hitGfxHeight = pc.hitGfxHeight;
						replacement.projectileId = pc.projectileId;
						replacement.startMovement = pc.startMovement;
						replacement.startPos = pc.startPos;
						replacement.startHeight = pc.startHeight;
						replacement.endHeight = pc.endHeight;
						replacement.slope = pc.slope;
						pluginPanel.rebuild();
					});
					panel1.add(use);
					String s;
					if (debug) {
						s = //".name(\"" + lastSpellCastName + "\")" +
								".cast(" + pc.castAnimation + ", " + (pc.castGfx != -1 ? (pc.castGfx + ", " + pc.castGfxHeight) : "-1, -1") + ")" +
								(pc.hitGfx != -1 ?
									".hitGfx(" + pc.hitGfx + ", " + pc.hitGfxHeight + ")"
									: "") +
								(pc.projectileId != -1 ?
									".projectile(" + pc.projectileId + ", " + pc.startMovement + ", " + pc.startPos + ", " + pc.startHeight + ", " + pc.endHeight + ", " + pc.slope + ")"
									: "") +
								"";
					} else {
						s = "a" + pc.castAnimation + " cg" + pc.castGfx + " cgh" + pc.castGfxHeight +
							" hg" + pc.hitGfx + " hgh" + pc.hitGfxHeight +
							" pid" + pc.projectileId + " a" + pc.slope + " d" + pc.startMovement + " o" + pc.startPos + " sh" + pc.startHeight + " eh" + pc.endHeight
						;
					}
					JTextField label = new JTextField(s + " (seen " + projectile.i + " times)");
					label.setEditable(false);
					panel1.add(label);

					panel.add(panel1);
				}
				panel.revalidate();
				panel.repaint();
			});
		}
	}

	private void createProjectileEditPanelRow(String labelName, ChangeListener cl, int initialValue, JPanel panel)
	{
		createProjectileEditPanelRow(labelName, cl, initialValue, panel, -1, Integer.MAX_VALUE);
	}

	private void createProjectileEditPanelRow(String labelName, ChangeListener cl, int initialValue, JPanel panel, int min, int max)
	{
		JPanel row = new JPanel();
		row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
		JLabel label = new JLabel(labelName);
		row.add(label);
		JSpinner input = createIntSpinner(initialValue, cl, min, max);
		row.add(input);
		panel.add(row);
	}

	// Copied from runelite's ConfigPanel class.
	private JSpinner createIntSpinner(int value, ChangeListener onChange, int min, int max)
	{
		// Config may previously have been out of range
		value = Ints.constrainToRange(value, min, max);

		SpinnerModel model = new SpinnerNumberModel(value, min, max, 1);
		JSpinner spinner = new JSpinner(model);
		Component editor = spinner.getEditor();
		JFormattedTextField spinnerTextField = ((JSpinner.DefaultEditor) editor).getTextField();
		int SPINNER_FIELD_WIDTH = 6;
		spinnerTextField.setColumns(SPINNER_FIELD_WIDTH);
		spinner.addChangeListener(onChange);

//		Units units = cid.getUnits();
//		if (units != null)
//		{
//			spinnerTextField.setFormatterFactory(new UnitFormatterFactory(units));
//		}
//
		return spinner;
	}

	private Component createSpellEditPanel(Swap swap, int index, boolean rhs)
	{
		JButton button = new JButton("", EDIT_ICON);
		button.addActionListener(e -> {
			if (pluginPanel.currentPsSwap == swap && pluginPanel.currentPsIndex == index && pluginPanel.currentPsRhs == rhs) {
				pluginPanel.currentPsSwap = null;
				pluginPanel.currentPsIndex = -1;
			} else {
				pluginPanel.currentPsSwap = swap;
				pluginPanel.currentPsIndex = index;
				pluginPanel.currentPsRhs = rhs;
			}
			rebuild();
		});
		return button;
	}

	private Component createGraphicsEffectPanel(Swap swap, int i, int size)
	{
		GraphicEffect graphicEffect = swap.getGraphicEffects().get(i);

		JPanel animationReplacementPanel = new JPanel();
		animationReplacementPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
		animationReplacementPanel.setLayout(new BoxLayout(animationReplacementPanel, BoxLayout.Y_AXIS));
		animationReplacementPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel row1 = new JPanel();
		row1.setLayout(new BoxLayout(row1, BoxLayout.X_AXIS));
		row1.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		row1.add(new JLabel("Effect: "));

		JComboBox<GraphicEffect.Type> graphicEffectTypeComboBox = new JComboBox<>(GraphicEffect.Type.values());
		graphicEffectTypeComboBox.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				Component rendererComponent = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				setText(value == null ? "<choose>" : ((GraphicEffect.Type) value).name());
				return rendererComponent;
			}
		});
		graphicEffectTypeComboBox.setSelectedItem(graphicEffect.type);
		graphicEffectTypeComboBox.setPrototypeDisplayValue(GraphicEffect.Type.SCYTHE_SWING);
		graphicEffectTypeComboBox.addActionListener((e) -> {
			plugin.clientThread.invokeLater(() -> {
				graphicEffect.type = (GraphicEffect.Type) graphicEffectTypeComboBox.getSelectedItem();
				plugin.handleTransmogSetChange();
				SwingUtilities.invokeLater(this::rebuild);
			});
		});
		row1.add(graphicEffectTypeComboBox);
		animationReplacementPanel.add(row1);

		if (graphicEffect.type == GraphicEffect.Type.SCYTHE_SWING) {
			JPanel row2 = new JPanel();
			row2.setLayout(new BoxLayout(row2, BoxLayout.X_AXIS));
			row2.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			row2.add(new JLabel("Color:"));

			Color existing = graphicEffect.color;

			ColorJButton colorPickerBtn;

			boolean alphaHidden = true;

			if (existing == null)
			{
				colorPickerBtn = new ColorJButton("Pick a color", Color.BLACK);
			}
			else
			{
				String colorHex = "#" + (alphaHidden ? ColorUtil.colorToHexCode(existing) : ColorUtil.colorToAlphaHexCode(existing)).toUpperCase();
				colorPickerBtn = new ColorJButton(colorHex, existing);
			}

			colorPickerBtn.setFocusable(false);
			colorPickerBtn.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mouseClicked(MouseEvent e)
				{
					RuneliteColorPicker colorPicker = plugin.colorPickerManager.create(
						SwingUtilities.windowForComponent(TransmogSetPanel.this),
						colorPickerBtn.getColor(),
						"scythe swing color",
						alphaHidden);
					colorPicker.setLocation(getLocationOnScreen());
					colorPicker.setOnColorChange(c ->
					{
						graphicEffect.color = c;
						colorPickerBtn.setColor(c);
						colorPickerBtn.setText("#" + (alphaHidden ? ColorUtil.colorToHexCode(c) : ColorUtil.colorToAlphaHexCode(c)).toUpperCase());
					});
					colorPicker.setOnClose(c -> plugin.clientThread.invokeLater(() -> {
						graphicEffect.color = c;
						plugin.handleTransmogSetChange();
						SwingUtilities.invokeLater(TransmogSetPanel.this::rebuild);
					}));
					colorPicker.setVisible(true);
				}
			});
			row2.add(colorPickerBtn);
			animationReplacementPanel.add(row2);
		}

		return new EntryPanel(false, false, true, i == size - 1, animationReplacementPanel, () -> {
			swap.getGraphicEffects().remove(i);
			plugin.clientThread.invoke(plugin::handleTransmogSetChange);
			SwingUtilities.invokeLater(this::rebuild);
		}, () -> {
			plugin.clientThread.invokeLater(() -> {
				swap.addNewGraphicEffect();
				plugin.handleTransmogSetChange();
				SwingUtilities.invokeLater(this::rebuild);
			});
		}, (enabled) -> {
		});
	}

	private Component createSoundSwapPanel(Swap swap, int i, int size)
	{
		SoundSwap soundSwap = swap.getSoundSwaps().get(i);
		JPanel animationReplacementPanel = new JPanel();
		animationReplacementPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
		animationReplacementPanel.setLayout(new BoxLayout(animationReplacementPanel, BoxLayout.Y_AXIS));
		animationReplacementPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		createProjectileEditPanelRow("Replace sound", ce -> {
			int value = (int) ((JSpinner) ce.getSource()).getValue();
			soundSwap.setToReplace(value);
			plugin.saveTransmogSets();
			plugin.clientThread.invoke(() -> plugin.client.playSoundEffect(value));
		}, soundSwap.getToReplace(), animationReplacementPanel);

		createProjectileEditPanelRow("with", ce -> {
			int value = (int) ((JSpinner) ce.getSource()).getValue();
			soundSwap.setToReplaceWith(value);
			plugin.saveTransmogSets();
			plugin.clientThread.invoke(() -> plugin.client.playSoundEffect(value));
		}, soundSwap.getToReplaceWith(), animationReplacementPanel);

		return new EntryPanel(false, false, true, i == size - 1, animationReplacementPanel, () -> {
			swap.getSoundSwaps().remove(i);
			plugin.clientThread.invoke(plugin::handleTransmogSetChange);
			SwingUtilities.invokeLater(this::rebuild);
		}, () -> {
			plugin.clientThread.invokeLater(() -> {
				swap.addNewSoundSwap();
				plugin.handleTransmogSetChange();
				SwingUtilities.invokeLater(this::rebuild);
			});
		}, (enabled) -> {
		});

	}

	/** Should really use a builder. atm you have to call setitem/setspell and addListeners last, after other fields are set. */
	public class ItemSelectionButton extends JButton {
		String nameWhenEmpty = "None";
		String overlayString = null;
		boolean showNotSign = false;
		public ItemSelectionButton()
		{
			setBackground(ColorScheme.LIGHT_GRAY_COLOR);
			setPreferredSize(new Dimension(35, 35));
			setMaximumSize(new Dimension(35, 35));
			setMinimumSize(new Dimension( 30, 30));
		}
		public void setItem(int itemId, int tooltipId) {
			setItemInternal(itemId, tooltipId, null);
		}
		public void setItem(int itemId, String tooltip) {
			setItemInternal(itemId, -1, tooltip);
		}
		public void setItem(int itemId) {
			setItem(itemId, itemId);
		}
		private void setItemInternal(int itemId, int tooltipItemId, String tooltip) {
			if (itemId == -1)
			{
				setIcon(null);
				setText(nameWhenEmpty);
				setBorder(null);
			} else {
				setText(null);
				plugin.clientThread.invoke(() -> {
					AsyncBufferedImage itemImage = plugin.getItemImage(Constants.getIconId(itemId));
					BufferedImage bankFillerImage = showNotSign ? plugin.getItemImage(ItemID.BANK_FILLER) : null;
					String tooltipString = tooltip == null ? plugin.itemDisplayName(tooltipItemId) : tooltip;
					Runnable processImage = () -> {
						SwingUtilities.invokeLater(() -> {
							if (!showNotSign && overlayString == null) {
								setIcon(new ImageIcon(itemImage));
							} else {
								BufferedImage copy = new BufferedImage(itemImage.getWidth(), itemImage.getHeight(), itemImage.getType());
								Graphics2D graphics = (Graphics2D) copy.getGraphics();
								graphics.drawImage(itemImage, 0, 0, null);
								if (showNotSign)
								{
									AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
									graphics.setComposite(ac);
									graphics.drawImage(bankFillerImage, 0, 0/*, (int) (bankFillerImage.getHeight() * 1.5), (int) (bankFillerImage.getWidth() * 1.5)*/, null);
								}
								if (overlayString != null)
								{
									graphics.drawString(overlayString, 0, 32);
								}
								setIcon(new ImageIcon(copy));
							}

							if (tooltipString != null) {
								setToolTipText(tooltipString);
							}
						});
					};
					// Yes I might end up running it twice, this stupid asyncbufferedimage doesn't let you know if it's loaded and won't run listeners once it's already been loaded.
					itemImage.onLoaded(processImage);
					processImage.run();
				});
			}
		}

		public void addListeners(Runnable onRemove, BiConsumer<SelectionResult, WeaponAnimationReplacerPlugin> onAdd, SearchType type, Swap swap) {
			Runnable deleteItem = () ->
				plugin.clientThread.invoke(() -> {
						onRemove.run();
						plugin.handleTransmogSetChange();
						SwingUtilities.invokeLater(TransmogSetPanel.this::rebuild);
					}
				);
			Runnable addItem = () -> {
				plugin.doItemSearch(
					result -> {
						onAdd.accept(result, plugin);
						plugin.handleTransmogSetChange();
						SwingUtilities.invokeLater(TransmogSetPanel.this::rebuild);
					},
					deleteItem,
					type,
					swap
				);
			};
			this.addActionListener(e -> ((e.getModifiers() & InputEvent.CTRL_MASK) > 0 ? deleteItem : addItem).run());
			JPopupMenu rightClickMenu = new JPopupMenu();
			JMenuItem addItemsMenuItem = new JMenuItem("Add more items");
			addItemsMenuItem.addActionListener(e -> addItem.run());
			rightClickMenu.add(addItemsMenuItem);
			JMenuItem removeItemMenuItem = new JMenuItem("Remove (ctrl-click)");
			removeItemMenuItem.addActionListener(e -> deleteItem.run());
			rightClickMenu.add(removeItemMenuItem);
			this.setComponentPopupMenu(rightClickMenu);
		}

		public void setSpell(int spellId, boolean custom)
		{
			if (custom) overlayString = "Custom";
			if (spellId == -1)
			{
				setIcon(null);
				if (custom) {
					setItem(ItemID.FEATHER, "Custom");
				}
				setText(nameWhenEmpty);
				setBorder(null);
			} else {
				if (spellId >= Constants.projectilesById.length) {
					return;
				}
				ProjectileCast projectileCast = Constants.projectilesById[spellId];
				setText(null);

				plugin.clientThread.invoke(() -> {
					BufferedImage spellImage = plugin.getSpellImage(projectileCast);
					setIcon(new ImageIcon(spellImage));
					String name = projectileCast.getName(plugin.itemManager);
					SwingUtilities.invokeLater(() ->
					{
						if (overlayString != null)
						{
							// fill entire button, they're about 32x32. This makes space for more text.
							BufferedImage copy = new BufferedImage(32, 32, spellImage.getType());
							Graphics2D graphics = (Graphics2D) copy.getGraphics();
							graphics.drawImage(spellImage, (32 - spellImage.getWidth()) / 2, (32 - spellImage.getHeight()) / 2, null);
							graphics.drawString(overlayString, 0, 32);
							setIcon(new ImageIcon(copy));
						} else {
							setIcon(new ImageIcon(spellImage));
						}
						setToolTipText(name);
					});
				});
			}
		}
	}

	private JPanel createNamePanel() {
		JPanel nameWrapper = new JPanel(new BorderLayout());
		nameWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		nameWrapper.setBorder(NAME_BOTTOM_BORDER);

		JPanel nameActions = new JPanel(new BorderLayout(2, 0));
		nameActions.setBorder(new EmptyBorder(-1, 0, 0, 8));
		nameActions.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		IconLabelButton rename;
		rename = new IconLabelButton(EDIT_ICON, EDIT_ICON_HOVER, () -> {}, "Edit name");
		rename.onClick = () -> {
			nameInput.setEditable(true);
			rename.setVisible(false);
			nameInput.getTextField().requestFocus();
			nameInput.getTextField().selectAll();
		};
		rename.setFont(FontManager.getRunescapeSmallFont());
		rename.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker());
		//nameActions.add(rename, BorderLayout.CENTER);

		nameInput.setText(transmogSet.getName());
		nameInput.setBorder(null);
		nameInput.setEditable(false);
		nameInput.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		nameInput.setPreferredSize(new Dimension(-1, 24));
		nameInput.getTextField().setForeground(Color.WHITE);
		nameInput.getTextField().setBorder(new EmptyBorder(-1, 8, 0, 0));
		nameInput.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				// Get the new value of the field, so you don't miss the last letter.
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					SwingUtilities.invokeLater(() -> {
						transmogSet.setName(nameInput.getText());
						plugin.saveTransmogSets();
					});
				}
				else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					nameInput.setText(transmogSet.getName());
				} else {
					return;
				}

				nameInput.setEditable(false);
				plugin.clientUI.requestFocus(); // Necessary to avoid dotted line outline on next thing in the interface.
				rename.setVisible(true);
			}
		});
		nameInput.getTextField().addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				SwingUtilities.invokeLater(() -> {
					transmogSet.setName(nameInput.getText());
					plugin.saveTransmogSets();
				});
				nameInput.setEditable(false);
				rename.setVisible(true);
			}
		});
		nameInput.getTextField().addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				if (!nameInput.getTextField().isEditable()) {
					plugin.clientThread.invokeLater(() -> {
						transmogSet.setMinimized(!transmogSet.isMinimized());
						SwingUtilities.invokeLater(TransmogSetPanel.this::rebuild);
					});
				}
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				rename.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker().darker());
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				rename.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker());
			}
		});

		nameWrapper.add(nameInput, BorderLayout.CENTER);
		nameWrapper.add(rename, BorderLayout.EAST);
		return new EntryPanel(true, transmogSet.isEnabled(), false, transmogSet.isMinimized(), true, true, true, true, false, nameWrapper, () -> {
			int delete = JOptionPane.showConfirmDialog(pluginPanel,
				"Are you sure you want to delete that?",
				"Delete?", JOptionPane.OK_CANCEL_OPTION);
			if (delete != JOptionPane.YES_OPTION) return;

			plugin.clientThread.invokeLater(() -> {
				plugin.deleteTransmogSet(index);
			});
		}, () -> {
			plugin.clientThread.invokeLater(() -> {
				plugin.addNewTransmogSet(index + 1);
			});
		}, (b) -> {
			plugin.clientThread.invokeLater(() -> {
				transmogSet.setEnabled(b);
				plugin.handleTransmogSetChange();
			});
		});
	}

}