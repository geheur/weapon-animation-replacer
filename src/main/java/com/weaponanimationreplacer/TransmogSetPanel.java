/*
 * Copyright (c) 2018, Kamiel, <https://github.com/Kamielvf>
 * Copyright (c) 2018, Psikoi <https://github.com/psikoi>
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

import static com.weaponanimationreplacer.Constants.HiddenSlot;
import static com.weaponanimationreplacer.Constants.NegativeId;
import static com.weaponanimationreplacer.Constants.NegativeIdsMap;
import static com.weaponanimationreplacer.Constants.ShownSlot;
import static com.weaponanimationreplacer.Constants.mapNegativeId;
import com.weaponanimationreplacer.Swap.AnimationType;
import static com.weaponanimationreplacer.Swap.AnimationType.ATTACK;
import com.weaponanimationreplacer.WeaponAnimationReplacerPlugin.ItemSearchType;
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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import net.runelite.api.kit.KitType;
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

	private final Runnable rebuild;

	private final FlatTextField nameInput = new FlatTextField();

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

	TransmogSetPanel(WeaponAnimationReplacerPlugin plugin, TransmogSet transmogSet, Runnable rebuild, int index)
	{
		this.rebuild = rebuild;
		this.plugin = plugin;
		this.transmogSet = transmogSet;
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
		bottomContainer.add(addSwapButton);

		return bottomContainer;
	}

	private Component createSwapPanel(Swap swap, int index, int total)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		JPanel restrictionAndModelSwapPanel = getRestrictionAndModelSwapPanel();
		restrictionAndModelSwapPanel.add(createSwapOptionsPanel(transmogSet, swap, index != 0, index != total - 1));
		for (int i = 0; i < swap.getItemRestrictions().size(); i++)
		{
			restrictionAndModelSwapPanel.add(createItemRestrictionButton(swap, i));
		}
		restrictionAndModelSwapPanel.add(new JLabel("->"));
		for (int i = 0; i < swap.getModelSwaps().size(); i++)
		{
			restrictionAndModelSwapPanel.add(createModelSwapButton(swap, i));
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

	private Component createModelSwapButton(Swap swap, int index)
	{
		ItemSelectionButton weaponIdInput = new ItemSelectionButton(ItemSearchType.MODEL_SWAP);
		weaponIdInput.setItem(swap.getModelSwaps().get(index));
		weaponIdInput.setOnItemChanged((itemId) -> {
			plugin.clientThread.invokeLater(() -> {
				swap.setModelSwap(index, itemId);
				plugin.handleTransmogSetChange();

				SwingUtilities.invokeLater(rebuild::run);
			});
		});

		return weaponIdInput;
	}

	private Component createItemRestrictionButton(Swap swap, int index)
	{
		ItemSelectionButton weaponIdInput = new ItemSelectionButton(ItemSearchType.ITEM_RESTRICTION);
		weaponIdInput.nameWhenEmpty = "Any";
		weaponIdInput.setItem(swap.getItemRestrictions().get(index));
		weaponIdInput.setOnItemChanged((itemId) -> {
			plugin.clientThread.invokeLater(() -> {
				swap.setItemRestriction(index, itemId);
				plugin.handleTransmogSetChange();

				SwingUtilities.invokeLater(rebuild::run);
			});
		});

		return weaponIdInput;
	}

	private Component createSpellSwapLButton(ProjectileSwap projectileSwap)
	{
		ItemSelectionButton weaponIdInput = new ItemSelectionButton(ItemSearchType.SPELL_L);
		weaponIdInput.setSpell(projectileSwap.toReplace);
		weaponIdInput.setOnItemChanged((itemId) -> {
			plugin.clientThread.invokeLater(() -> {
				projectileSwap.toReplace = itemId;
				plugin.handleTransmogSetChange();

				SwingUtilities.invokeLater(rebuild::run);
			});
		});

		return weaponIdInput;
	}

	private Component createSpellSwapRButton(ProjectileSwap projectileSwap)
	{
		ItemSelectionButton weaponIdInput = new ItemSelectionButton(ItemSearchType.SPELL_R);
		weaponIdInput.setSpell(projectileSwap.toReplaceWith);
		weaponIdInput.setOnItemChanged((itemId) -> {
			plugin.clientThread.invokeLater(() -> {
				projectileSwap.toReplaceWith = itemId;
				plugin.handleTransmogSetChange();

				SwingUtilities.invokeLater(rebuild::run);
			});
		});

		return weaponIdInput;
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
		addMenuItem(menu, "Add projectile swap", e -> addProjectileSwap(swap));
		addMenuItem(menu, "Add graphic effect", e -> addGraphicEffect(swap));

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
		SwingUtilities.invokeLater(rebuild::run);
	}

	private void removeSwap(TransmogSet transmogSet, Swap swap)
	{
		transmogSet.removeSwap(swap);
		plugin.clientThread.invokeLater(plugin::handleTransmogSetChange);
		SwingUtilities.invokeLater(rebuild::run);
	}

	private void moveSwap(TransmogSet transmogSet, Swap swap, int i)
	{
		transmogSet.moveSwap(swap, i);
		plugin.clientThread.invokeLater(plugin::handleTransmogSetChange);
		SwingUtilities.invokeLater(rebuild::run);
	}

	private void addAnimationReplacement(Swap swap)
	{
		plugin.clientThread.invokeLater(() -> {
			swap.addNewAnimationReplacement();
			plugin.handleTransmogSetChange();
			SwingUtilities.invokeLater(rebuild::run);
		});
	}

	private void addProjectileSwap(Swap swap)
	{
		swap.addNewProjectileSwap();
		plugin.clientThread.invokeLater(plugin::handleTransmogSetChange);
		SwingUtilities.invokeLater(rebuild::run);
	}

	private void addGraphicEffect(Swap swap)
	{
		swap.addNewGraphicEffect();
		plugin.clientThread.invokeLater(plugin::handleTransmogSetChange);
		SwingUtilities.invokeLater(rebuild::run);
	}

	// TODO threading, memory consistency? on which threads am I doing what. I want swaps to be modified on the client thread only, I think.
	private void addModelSwap(Swap swap)
	{
		plugin.doItemSearch(ItemSearchType.MODEL_SWAP, null, itemId -> {
			swap.addNewModelSwap(itemId);
			plugin.handleTransmogSetChange();
			SwingUtilities.invokeLater(rebuild::run);
		});
	}

	private void addTriggerItem(Swap swap)
	{
		plugin.doItemSearch(ItemSearchType.ITEM_RESTRICTION, null, itemId -> {
			swap.addNewTriggerItem(itemId);
			plugin.handleTransmogSetChange();
			SwingUtilities.invokeLater(rebuild::run);
		});
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
						SwingUtilities.invokeLater(rebuild::run);
					});
				});
				rightSide.add(xButton);
			}
			if (updown) {
			    rightSide.add(new IconLabelButton(MOVE_RULE_UP_ICON, MOVE_RULE_UP_ICON_HOVER, () -> {
					plugin.clientThread.invokeLater(() -> {
						plugin.moveTransmogSet(index, true);
						SwingUtilities.invokeLater(rebuild::run);
						plugin.handleTransmogSetChange();
					});
				}, "Move up in list and priority"));
				rightSide.add(new IconLabelButton(MOVE_RULE_DOWN_ICON, MOVE_RULE_DOWN_ICON_HOVER, () -> {
					plugin.clientThread.invokeLater(() -> {
						plugin.moveTransmogSet(index, false);
						SwingUtilities.invokeLater(rebuild::run);
						plugin.handleTransmogSetChange();
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
		Swap.AnimationReplacement animationReplacement = swap.animationReplacements.get(i);

	    JPanel animationReplacementPanel = new JPanel();
	    animationReplacementPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
	    animationReplacementPanel.setLayout(new BoxLayout(animationReplacementPanel, BoxLayout.Y_AXIS));
		animationReplacementPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

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
				if (!ATTACK.appliesTo(animationReplacement.animationtypeToReplace)) {
					animationReplacement.animationtypeReplacement = null;
				}
				plugin.handleTransmogSetChange();
				SwingUtilities.invokeLater(rebuild::run);
			});
		});
		row1.add(animToReplace);
		animationReplacementPanel.add(row1);

		JPanel row2 = new JPanel();
		row2.setLayout(new BoxLayout(row2, BoxLayout.X_AXIS));
		row2.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		row2.add(new JLabel("with"));
		JComboBox<AnimationSet> animationSetToUse = new JComboBox<>(AnimationSet.animationSets.toArray(new AnimationSet[]{}));
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
				if (ATTACK.appliesTo(animationReplacement.animationtypeToReplace) && animationReplacement.animationSet != null)
				{
					List<AnimationSet.Animation> actions = animationReplacement.animationSet.animations.entrySet().stream()
						.filter(t -> ATTACK.appliesTo(t.getKey()))
						.map(entry -> entry.getValue())
						.collect(Collectors.toList());
					AnimationType currentAttackStyle = animationReplacement.animationtypeReplacement == null ? null : animationReplacement.animationtypeReplacement.type;
					Optional<AnimationSet.Animation> match = actions.stream().filter(action -> action.type.equals(currentAttackStyle)).findAny();
					if (match.isPresent()) {
						animationReplacement.animationtypeReplacement = match.get();
					} else if (!actions.isEmpty()) {
						animationReplacement.animationtypeReplacement = actions.get(0);
					}
				}
				plugin.handleTransmogSetChange();
				SwingUtilities.invokeLater(rebuild::run);
			});
		});
		row2.add(animationSetToUse);
		animationReplacementPanel.add(row2);

		if (ATTACK.appliesTo(animationReplacement.animationtypeToReplace) && animationReplacement.animationSet != null) {
			JPanel row3 = new JPanel();
			row3.setLayout(new BoxLayout(row3, BoxLayout.X_AXIS));
			row3.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			row3.add(new JLabel("attack animation:"));
			List<AnimationSet.Animation> actions = animationReplacement.animationSet.animations.entrySet().stream()
					.filter(t -> ATTACK.appliesTo(t.getKey()))
					.map(e -> e.getValue())
					.collect(Collectors.toList());
			log.debug("actions is : " + actions);
			JComboBox<AnimationSet.Animation> attackToUse = new JComboBox<>(actions.toArray(new AnimationSet.Animation[] {})); // TODO remove indenting?
            // TODO add "automatic" option.
			attackToUse.setRenderer(new DefaultListCellRenderer() {
				@Override
				public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
					Component rendererComponent = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					if (value == null || !actions.contains(value)) {
						setText("<choose>");
					} else {
						AnimationSet.Animation animation = (AnimationSet.Animation) value;
						String comboBoxName = animation.type.getComboBoxName();
						setText(animation.description != null ? animation.description : comboBoxName);
					}
					return rendererComponent;
				}
			});
			attackToUse.setSelectedItem(animationReplacement.animationtypeReplacement);
			// Update the rule to reflect the dropdown. This is relevant if the list of items in the dropdown does not contain the original replacement.
			animationReplacement.animationtypeReplacement = ((AnimationSet.Animation) attackToUse.getSelectedItem());
			attackToUse.addActionListener((e) -> {
				plugin.clientThread.invokeLater(() -> {
					animationReplacement.animationtypeReplacement = ((AnimationSet.Animation) attackToUse.getSelectedItem());
					plugin.handleTransmogSetChange();

					Integer animation = animationReplacement.animationtypeReplacement.id;
					if (animation != null) {
					    plugin.demoAnimation(animation);
					}
				});
			});
			row3.add(attackToUse);
//			animToReplace.setPrototypeDisplayValue("___");
			animationReplacementPanel.add(row3);
		}

		return new EntryPanel(false, true, true, i == size - 1, animationReplacementPanel, () -> {
			swap.animationReplacements.remove(i);
			plugin.clientThread.invoke(plugin::handleTransmogSetChange);
			SwingUtilities.invokeLater(() -> rebuild.run());
		}, () -> {
			plugin.clientThread.invokeLater(() -> {
				swap.addNewAnimationReplacement();
				plugin.handleTransmogSetChange();
				SwingUtilities.invokeLater(() -> rebuild.run());
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
		row1.add(new JLabel("Projectile "));

		JPanel projectileSwapPanel = getRestrictionAndModelSwapPanel();
		projectileSwapPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		projectileSwapPanel.add(createSpellSwapLButton(projectileSwap));
		projectileSwapPanel.add(new JLabel("->"));
		projectileSwapPanel.add(createSpellSwapRButton(projectileSwap));
		row1.add(projectileSwapPanel);
		animationReplacementPanel.add(row1);

		return new EntryPanel(false, true, true, i == size - 1, animationReplacementPanel, () -> {
			swap.getProjectileSwaps().remove(i);
			plugin.clientThread.invoke(plugin::handleTransmogSetChange);
			SwingUtilities.invokeLater(() -> rebuild.run());
		}, () -> {
			plugin.clientThread.invokeLater(() -> {
				swap.addNewProjectileSwap();
				plugin.handleTransmogSetChange();
				SwingUtilities.invokeLater(() -> rebuild.run());
			});
		}, (enabled) -> {
			plugin.clientThread.invoke(plugin::handleTransmogSetChange);
		});
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
				SwingUtilities.invokeLater(rebuild::run);
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
						SwingUtilities.invokeLater(rebuild::run);
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
			SwingUtilities.invokeLater(rebuild::run);
		}, () -> {
			plugin.clientThread.invokeLater(() -> {
				swap.addNewGraphicEffect();
				plugin.handleTransmogSetChange();
				SwingUtilities.invokeLater(rebuild::run);
			});
		}, (enabled) -> {
		});
	}

	public class ItemSelectionButton extends JButton {
		String nameWhenEmpty = "None";
		public ItemSelectionButton(ItemSearchType type)
		{
			setBackground(ColorScheme.LIGHT_GRAY_COLOR);
			setPreferredSize(new Dimension(35, 35));
			setMaximumSize(new Dimension(35, 35));
			setMinimumSize(new Dimension( 30, 30));
			addActionListener((e) -> {
//				if (plugin.client.getGameState() == GameState.LOGGED_IN) setItemInternal(-1);
				plugin.doItemSearch(type, this, (itemId) -> {
					setItemInternal(itemId);
				});
			});
		}
		private IntConsumer f;
		public void setOnItemChanged(IntConsumer f) {
			this.f = f;
		}
		private void setItemInternal(int itemId) {
			f.accept(itemId);
			setItem(itemId);
		}
		public void setItem(int itemId) {
			if (itemId == -1)
			{
				setIcon(null);
				setText(nameWhenEmpty);
				setBorder(null);
			} else if (itemId < 0) {
				NegativeId negativeId = mapNegativeId(itemId);
				if (negativeId.type == NegativeIdsMap.HIDE_SLOT) {
					plugin.clientThread.invoke(() -> {
						BufferedImage itemImage = plugin.getItemImage(HiddenSlot.values()[negativeId.id].iconIdToShow);
						BufferedImage bankFillerImage = plugin.getItemImage(ItemID.BANK_FILLER);
						SwingUtilities.invokeLater(() -> {
							BufferedImage copy = new BufferedImage(itemImage.getWidth(), itemImage.getHeight(), itemImage.getType());
							Graphics2D graphics = (Graphics2D) copy.getGraphics();
							graphics.drawImage(itemImage, 0, 0, null);
							AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
							graphics.setComposite(ac);
							graphics.drawImage(bankFillerImage, 0, 0/*, (int) (bankFillerImage.getHeight() * 1.5), (int) (bankFillerImage.getWidth() * 1.5)*/, null);
							setIcon(new ImageIcon(copy));
						});
					});
					setText(null);
					SwingUtilities.invokeLater(() -> {
						setToolTipText("hide " + KitType.values()[negativeId.id]);
					});
				}
				else if (negativeId.type == NegativeIdsMap.SHOW_SLOT) {
					AsyncBufferedImage itemImage = (AsyncBufferedImage) plugin.getItemImage(ShownSlot.values()[negativeId.id].iconIdToShow);
					itemImage.addTo(this);
					setText(null);
					SwingUtilities.invokeLater(() -> {
						setToolTipText("show " + KitType.values()[negativeId.id]);
					});
				}
			} else {
				setIcon(new ImageIcon(plugin.getItemImage(itemId)));
				setText(null);

				plugin.clientThread.invoke(() -> {
					String name = itemName(itemId);
					SwingUtilities.invokeLater(() -> {
						setToolTipText(name);
					});
				});
			}
		}

		public void setSpell(int spellIndex)
		{
			if (spellIndex == -1)
			{
				setIcon(null);
				setText(nameWhenEmpty);
				setBorder(null);
			} else {
				if (spellIndex >= ProjectileCast.projectiles.size()) return;

				ProjectileCast projectileCast = ProjectileCast.projectiles.get(spellIndex);
				setText(null);

				plugin.clientThread.invoke(() -> {
					setIcon(new ImageIcon(plugin.getSpellImage(projectileCast)));
					String name = projectileCast.getName(plugin.itemManager);
					SwingUtilities.invokeLater(() ->
					{
						setToolTipText(name);
					});
				});
			}
		}
	}

	private String itemName(int itemId) {
	    if (!plugin.clientLoaded) return null;
		return plugin.itemManager.getItemComposition(itemId).getName();
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
//		nameActions.add(rename, BorderLayout.CENTER);

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
						SwingUtilities.invokeLater(rebuild::run);
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
			plugin.deleteTransmogSet(index);
		}, () -> {
			plugin.clientThread.invokeLater(() -> {
				plugin.addNewTransmogSet(index + 1);
				plugin.handleTransmogSetChange();
			});
		}, (b) -> {
			plugin.clientThread.invokeLater(() -> {
				transmogSet.setEnabled(b);
				plugin.handleTransmogSetChange();
			});
		});
	}

}
