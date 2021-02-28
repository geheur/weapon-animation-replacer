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

import com.weaponanimationreplacer.AnimationReplacementRule.AnimationType;
import net.runelite.api.GameState;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.FlatTextField;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;

import static com.weaponanimationreplacer.AnimationReplacementRule.AnimationType.ATTACK;

class AnimationReplacementRulePanel extends JPanel
{
	private static final int DEFAULT_FILL_OPACITY = 75;

	private static final Border NAME_BOTTOM_BORDER = new CompoundBorder(
		BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.DARK_GRAY_COLOR),
		BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_COLOR));

//	private static final ImageIcon BORDER_COLOR_ICON;
//	private static final ImageIcon BORDER_COLOR_HOVER_ICON;
//	private static final ImageIcon NO_BORDER_COLOR_ICON;
//	private static final ImageIcon NO_BORDER_COLOR_HOVER_ICON;
//
//	private static final ImageIcon FILL_COLOR_ICON;
//	private static final ImageIcon FILL_COLOR_HOVER_ICON;
//	private static final ImageIcon NO_FILL_COLOR_ICON;
//	private static final ImageIcon NO_FILL_COLOR_HOVER_ICON;
//
//	private static final ImageIcon FULL_OPACITY_ICON;
//	private static final ImageIcon FULL_OPACITY_HOVER_ICON;
//	private static final ImageIcon NO_OPACITY_ICON;
//	private static final ImageIcon NO_OPACITY_HOVER_ICON;
//
//	private static final ImageIcon VISIBLE_ICON;
//	private static final ImageIcon VISIBLE_HOVER_ICON;
//	private static final ImageIcon INVISIBLE_ICON;
//	private static final ImageIcon INVISIBLE_HOVER_ICON;
//
//	private static final ImageIcon DELETE_ICON;
//	private static final ImageIcon DELETE_HOVER_ICON;
//
	private final WeaponAnimationReplacerPlugin plugin;
	private final int index;
	private final AnimationReplacementRule rule;

	private final JLabel borderColorIndicator = new JLabel();
	private final JLabel fillColorIndicator = new JLabel();
	private final JLabel opacityIndicator = new JLabel();
	private final JLabel visibilityLabel = new JLabel();
	private final JLabel deleteLabel = new JLabel();

	private final FlatTextField nameInput = new FlatTextField();
	private final JLabel save = new JLabel("Save");
	private final JLabel cancel = new JLabel("Cancel");
	private final JLabel rename = new JLabel("Rename");

	private boolean visible;

	static
	{
//		final BufferedImage borderImg = ImageUtil.loadImageResource(ScreenMarkerPlugin.class, "border_color_icon.png");
//		final BufferedImage borderImgHover = ImageUtil.luminanceOffset(borderImg, -150);
//		BORDER_COLOR_ICON = new ImageIcon(borderImg);
//		BORDER_COLOR_HOVER_ICON = new ImageIcon(borderImgHover);
//
//		NO_BORDER_COLOR_ICON = new ImageIcon(borderImgHover);
//		NO_BORDER_COLOR_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(borderImgHover, -100));
//
//		final BufferedImage fillImg = ImageUtil.loadImageResource(ScreenMarkerPlugin.class, "fill_color_icon.png");
//		final BufferedImage fillImgHover = ImageUtil.luminanceOffset(fillImg, -150);
//		FILL_COLOR_ICON = new ImageIcon(fillImg);
//		FILL_COLOR_HOVER_ICON = new ImageIcon(fillImgHover);
//
//		NO_FILL_COLOR_ICON = new ImageIcon(fillImgHover);
//		NO_FILL_COLOR_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(fillImgHover, -100));
//
//		final BufferedImage opacityImg = ImageUtil.loadImageResource(ScreenMarkerPlugin.class, "opacity_icon.png");
//		final BufferedImage opacityImgHover = ImageUtil.luminanceOffset(opacityImg, -150);
//		FULL_OPACITY_ICON = new ImageIcon(opacityImg);
//		FULL_OPACITY_HOVER_ICON = new ImageIcon(opacityImgHover);
//
//		NO_OPACITY_ICON = new ImageIcon(opacityImgHover);
//		NO_OPACITY_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(opacityImgHover, -100));
//
//		final BufferedImage visibleImg = ImageUtil.loadImageResource(ScreenMarkerPlugin.class, "visible_icon.png");
//		VISIBLE_ICON = new ImageIcon(visibleImg);
//		VISIBLE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(visibleImg, -100));
//
//		final BufferedImage invisibleImg = ImageUtil.loadImageResource(ScreenMarkerPlugin.class, "invisible_icon.png");
//		INVISIBLE_ICON = new ImageIcon(invisibleImg);
//		INVISIBLE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(invisibleImg, -100));
//
//		final BufferedImage deleteImg = ImageUtil.loadImageResource(ScreenMarkerPlugin.class, "delete_icon.png");
//		DELETE_ICON = new ImageIcon(deleteImg);
//		DELETE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(deleteImg, -100));
	}

	AnimationReplacementRulePanel(WeaponAnimationReplacerPlugin plugin, AnimationReplacementRule animationReplacementRule, Runnable rebuild, int index)
	{
		this.rebuild = rebuild;
		this.plugin = plugin;
		this.rule = animationReplacementRule;
		this.visible = true;
		this.index = index;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARKER_GRAY_COLOR);

		add(createNamePanel(), BorderLayout.NORTH);
		if (!rule.minimized) add(createBottomPanel(plugin), BorderLayout.CENTER);

//		updateVisibility();
//		updateFill();
//		updateBorder();
//		updateBorder();

	}

	private JPanel createBottomPanel(WeaponAnimationReplacerPlugin plugin) {
		JPanel bottomContainer = new JPanel();
		bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.Y_AXIS));
//		bottomContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
		bottomContainer.setBorder(new EmptyBorder(8, 0, 8, 0));
		bottomContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		if (!rule.itemRestrictions.isEmpty()) {
			int index = 0;
			for (AnimationReplacementRule.ItemRestriction itemRestriction : rule.itemRestrictions) {
				bottomContainer.add(createItemRestrictionPanel(itemRestriction, index++, rule.itemRestrictions.size()));
			}
		} else {
			JButton button = new JButton("Enable only for specific item(s)");
			button.addActionListener((e) -> {
				rule.itemRestrictions.add(new AnimationReplacementRule.ItemRestriction(-1));
				plugin.updateAnimationsAndTransmog();
				SwingUtilities.invokeLater(() -> rebuild.run());
			});
			Box  b = Box.createHorizontalBox();
			b.add(button);
			b.add(Box.createHorizontalGlue());
			bottomContainer.add(b);
		}

		bottomContainer.add(createModelSwapPanel(rule.modelSwap));

		if (!rule.animationReplacements.isEmpty()) {
			int index = 0;
			for (AnimationReplacementRule.AnimationReplacement animationReplacement : rule.animationReplacements) {
				bottomContainer.add(createAnimationReplacementPanel(animationReplacement, index, rule.animationReplacements.size()));

				if (index != rule.animationReplacements.size() - 1) bottomContainer.add(Box.createRigidArea(new Dimension(0, 10)));

				index++;
			}
		} else {
			JButton button = new JButton("add animation swap");
			button.addActionListener((e) -> {
			    addAnimationReplacement();
			});
			Box  b = Box.createHorizontalBox();
			b.add(button);
			b.add(Box.createHorizontalGlue());
			bottomContainer.add(b);
		}

		return bottomContainer;
	}

	private final Runnable rebuild;

	public class EntryPanel extends JPanel {
		public EntryPanel(boolean checkbox, boolean enabled, boolean x, boolean plus, JPanel panel, Runnable onDelete, Runnable onAdd, Consumer<Boolean> onEnable) {
			this(checkbox, enabled, false, false, false, false, false, x, plus, panel, onDelete, onAdd, onEnable);
        }

		public EntryPanel(boolean checkbox, boolean enabled, boolean minimize, boolean minimized, boolean updown, boolean up, boolean down, boolean x, boolean plus, JPanel panel, Runnable onDelete, Runnable onAdd, Consumer<Boolean> onEnable) {
			setLayout(new BorderLayout());
			if (checkbox) {
				JCheckBox enabledCheckbox = new JCheckBox();
				enabledCheckbox.setSelected(enabled);
				enabledCheckbox.addActionListener((e) -> {
					plugin.clientThread.invokeLater(() -> {
						onEnable.accept(enabledCheckbox.isSelected());
					});
				});
				add(enabledCheckbox, BorderLayout.WEST);
			}
			JPanel rightSide = new JPanel();
			rightSide.setLayout(new BoxLayout(rightSide, BoxLayout.X_AXIS));
			if (minimize) {
				JLabel xButton = makeButton(minimized ? " + " : " - ", () -> {
					plugin.clientThread.invokeLater(() -> {
					    rule.minimized = !minimized;
						SwingUtilities.invokeLater(rebuild::run);
					});
				});
				rightSide.add(xButton);
			}
			if (updown) {
				if (up) {
					JLabel xButton = makeButton(" ^ ", () -> {
						plugin.clientThread.invokeLater(() -> {
							plugin.moveRule(index, true);
							SwingUtilities.invokeLater(rebuild::run);
							plugin.updateAnimationsAndTransmog();
						});
					});
					rightSide.add(xButton);
				}
				if (down) {
					JLabel xButton = makeButton(" v ", () -> {
						plugin.clientThread.invokeLater(() -> {
							plugin.moveRule(index, false);
							SwingUtilities.invokeLater(rebuild::run);
							plugin.updateAnimationsAndTransmog();
						});
					});
					rightSide.add(xButton);
				}
			}
			if (x) {
				JLabel xButton = makeButton(" x ", () -> {
					plugin.clientThread.invokeLater(() -> {
						onDelete.run();
					});
				});
				rightSide.add(xButton);
			}
			if (plus) {
				JLabel plusButton = makeButton(" + ", () -> {
					plugin.clientThread.invokeLater(() -> {
						onAdd.run();
					});
				});
				rightSide.add(plusButton);
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

	private Component createAnimationReplacementPanel(AnimationReplacementRule.AnimationReplacement animationReplacement, int i, int size) {
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
				plugin.updateAnimationsAndTransmog();
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
				plugin.updateAnimationsAndTransmog();
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
			JComboBox<AnimationType> attackToUse = new JComboBox<>(animationReplacement.animationSet.animations.keySet().stream().filter(t -> ATTACK.appliesTo(t)).collect(Collectors.toList()).toArray(new AnimationType[] {})); // TODO remove indenting?
            // TODO add "automatic" option.
			attackToUse.setRenderer(new DefaultListCellRenderer() {
				@Override
				public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
					Component rendererComponent = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					setText(value == null ? "<choose>" : ((AnimationType) value).getComboBoxName());
					return rendererComponent;
				}
			});
			attackToUse.setSelectedItem(animationReplacement.animationtypeReplacement);
			attackToUse.addActionListener((e) -> {
				plugin.clientThread.invokeLater(() -> {
					animationReplacement.animationtypeReplacement = (AnimationType) attackToUse.getSelectedItem();
//					plugin.clientThread.invoke(() -> {
//					});
					plugin.updateAnimationsAndTransmog();

					Integer animation = animationReplacement.animationSet.getAnimation(animationReplacement.animationtypeReplacement, false);
					if (animation != null) {
					    plugin.demoAnimation(animation);
					}
				});
			});
			row3.add(attackToUse);
//			animToReplace.setPrototypeDisplayValue("___");
			animationReplacementPanel.add(row3);
		}

		return new EntryPanel(!animationReplacement.enabled, animationReplacement.enabled, true, i == size - 1, animationReplacementPanel, () -> {
			rule.animationReplacements.remove(i);
			plugin.updateAnimationsAndTransmog();
			SwingUtilities.invokeLater(() -> rebuild.run());
		}, () -> {
			addAnimationReplacement();
		}, (enabled) -> {
			animationReplacement.enabled = enabled;
			plugin.updateAnimationsAndTransmog();
		});
	}

	private void addAnimationReplacement() {
	    plugin.clientThread.invokeLater(() -> {
			rule.animationReplacements.add(new AnimationReplacementRule.AnimationReplacement(null, null, null));
			plugin.updateAnimationsAndTransmog();
			SwingUtilities.invokeLater(() -> rebuild.run());
		});
	}

	public class ItemSelectionButton extends JButton {
		{
			addActionListener((e) -> {
				if (plugin.client.getGameState() == GameState.LOGGED_IN) setItemInternal(-1);
				plugin.doItemSearch(this, (itemId) -> {
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
			if (itemId == -1) {
				setIcon(null);
				setText("None");
			} else {
				setIcon(new ImageIcon(plugin.getItemImage(itemId)));
				setText(null);
			}

			plugin.clientThread.invoke(() -> {
				String name = itemName(itemId);
				SwingUtilities.invokeLater(() -> {
					setToolTipText(name);
				});
			});
		}
	}

	private Component createModelSwapPanel(int modelSwap) {
		JPanel modelSwapPanel = new JPanel();
		modelSwapPanel.setLayout(new BoxLayout(modelSwapPanel, BoxLayout.X_AXIS));
		modelSwapPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JLabel text1 = new JLabel("model swap:");
		modelSwapPanel.add(text1);

		ItemSelectionButton weaponIdInput = new ItemSelectionButton();
		weaponIdInput.setItem(rule.modelSwap);
		weaponIdInput.setOnItemChanged((itemId) -> {
			rule.modelSwap = Integer.valueOf(itemId);
			plugin.updateAnimationsAndTransmog();
		});
		modelSwapPanel.add(weaponIdInput);
		return new EntryPanel(false, rule.modelSwapEnabled, false, false, modelSwapPanel, () -> {
		}, () -> {
		}, (b) -> {
			rule.modelSwapEnabled = b;
			plugin.updateAnimationsAndTransmog();
		});
	}

	private Component createItemRestrictionPanel(AnimationReplacementRule.ItemRestriction itemRestriction, int index, int totalRestrictions) {
		JPanel itemRestrictionPanel = new JPanel();
		itemRestrictionPanel.setLayout(new BoxLayout(itemRestrictionPanel, BoxLayout.X_AXIS));
		itemRestrictionPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JLabel text1 = new JLabel("Use with item:");
		itemRestrictionPanel.add(text1);

		ItemSelectionButton weaponIdInput = new ItemSelectionButton();
		weaponIdInput.setItem(itemRestriction.itemId);
		weaponIdInput.setOnItemChanged((itemId) -> {
			itemRestriction.itemId = Integer.valueOf(itemId);
			plugin.updateAnimationsAndTransmog();
		});
		itemRestrictionPanel.add(weaponIdInput);
		return new EntryPanel(!itemRestriction.enabled, itemRestriction.enabled, true, index == totalRestrictions - 1, itemRestrictionPanel, () -> {
			rule.itemRestrictions.remove(index);
			plugin.updateAnimationsAndTransmog();
			SwingUtilities.invokeLater(() -> rebuild.run());
		}, () -> {
			rule.itemRestrictions.add(new AnimationReplacementRule.ItemRestriction(-1));
			plugin.updateAnimationsAndTransmog();
			SwingUtilities.invokeLater(() -> rebuild.run());
		}, (b) -> {
			itemRestriction.enabled = b;
			plugin.updateAnimationsAndTransmog();
		});
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

		save.setVisible(false);
		save.setFont(FontManager.getRunescapeSmallFont());
		save.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
		save.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
//				save();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				save.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR.darker());
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				save.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
			}
		});

		cancel.setVisible(false);
		cancel.setFont(FontManager.getRunescapeSmallFont());
		cancel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
		cancel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
//				cancel();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				cancel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR.darker());
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				cancel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
			}
		});

		rename.setFont(FontManager.getRunescapeSmallFont());
		rename.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker());
		rename.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				nameInput.setEditable(true);
				rename.setVisible(false);
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

//		nameActions.add(save, BorderLayout.EAST);
//		nameActions.add(cancel, BorderLayout.WEST);
		nameActions.add(rename, BorderLayout.CENTER);

		nameInput.setText(rule.name);
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
						rule.name = nameInput.getText();
						plugin.saveRules();
					});
				}
				else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
				    nameInput.setText(rule.name);
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
					rule.name = nameInput.getText();
					plugin.saveRules();
				});
				nameInput.setEditable(false);
				plugin.clientUI.requestFocus(); // Necessary to avoid dotted line outline on next thing in the interface.
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
						rule.minimized = !rule.minimized;
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
		nameWrapper.add(nameActions, BorderLayout.EAST);
		return new EntryPanel(true, rule.enabled, false, rule.minimized, true, true, true, true, false, nameWrapper, () -> {
			plugin.deleteNewRule(index);
			plugin.updateAnimationsAndTransmog();
		}, () -> {
			plugin.addNewRule(index);
			plugin.updateAnimationsAndTransmog();
		}, (b) -> {
			rule.enabled = b;
			plugin.updateAnimationsAndTransmog();
		});
	}

}
