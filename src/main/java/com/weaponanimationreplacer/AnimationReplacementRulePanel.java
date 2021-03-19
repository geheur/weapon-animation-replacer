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
import net.runelite.client.plugins.screenmarkers.ScreenMarkerPlugin;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.FlatTextField;
import net.runelite.client.util.ImageUtil;

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
import java.awt.image.BufferedImage;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
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

	private final WeaponAnimationReplacerPlugin plugin;
	private final int index;
	private final AnimationReplacementRule rule;

	private final FlatTextField nameInput = new FlatTextField();

	private boolean visible;

	static
	{
		final BufferedImage upImg = ImageUtil.loadImageResource(AnimationReplacementRulePanel.class, "up_small.png");
		MOVE_RULE_UP_ICON = new ImageIcon(upImg);
		MOVE_RULE_UP_ICON_HOVER = new ImageIcon(ImageUtil.luminanceOffset(upImg, -150));

		final BufferedImage downImg = ImageUtil.loadImageResource(AnimationReplacementRulePanel.class, "down_small.png");
		MOVE_RULE_DOWN_ICON = new ImageIcon(downImg);
		MOVE_RULE_DOWN_ICON_HOVER = new ImageIcon(ImageUtil.luminanceOffset(downImg, -150));

		final BufferedImage deleteImg = ImageUtil.loadImageResource(AnimationReplacementRulePanel.class, "delete.png");
		DELETE_RULE_ICON = new ImageIcon(deleteImg);
		DELETE_RULE_ICON_HOVER = new ImageIcon(ImageUtil.luminanceOffset(deleteImg, -50));

		final BufferedImage editImg = ImageUtil.loadImageResource(AnimationReplacementRulePanel.class, "edit.png");
		EDIT_ICON = new ImageIcon(editImg);
		EDIT_ICON_HOVER = new ImageIcon(ImageUtil.luminanceOffset(editImg, -150));

		final BufferedImage addIcon = ImageUtil.loadImageResource(ScreenMarkerPlugin.class, "add_icon.png");
		ADD_ICON = new ImageIcon(addIcon);
		ADD_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(addIcon, 0.53f));
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
	}

	private JPanel createBottomPanel(WeaponAnimationReplacerPlugin plugin) {
		JPanel bottomContainer = new JPanel();
		bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.Y_AXIS));
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
				enabledCheckbox.setToolTipText("Enabled");
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
			rightSide.setBackground(ColorScheme.DARKER_GRAY_COLOR);
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
			    rightSide.add(new IconLabelButton(MOVE_RULE_UP_ICON, MOVE_RULE_UP_ICON_HOVER, () -> {
					plugin.clientThread.invokeLater(() -> {
						plugin.moveRule(index, true);
						SwingUtilities.invokeLater(rebuild::run);
						plugin.updateAnimationsAndTransmog();
					});
				}, "Move up in list and priority"));
				rightSide.add(new IconLabelButton(MOVE_RULE_DOWN_ICON, MOVE_RULE_DOWN_ICON_HOVER, () -> {
					plugin.clientThread.invokeLater(() -> {
						plugin.moveRule(index, false);
						SwingUtilities.invokeLater(rebuild::run);
						plugin.updateAnimationsAndTransmog();
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
				if (!ATTACK.appliesTo(animationReplacement.animationtypeToReplace)) {
					animationReplacement.animationtypeReplacement = null;
				}
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
			List<AnimationSet.Animation> actions = animationReplacement.animationSet.animations.entries().stream()
					.filter(t -> ATTACK.appliesTo(t.getKey()))
					.map(e -> e.getValue())
					.collect(Collectors.toList());
			System.out.println("actions is : " + actions);
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
					plugin.updateAnimationsAndTransmog();

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
		nameWrapper.add(rename, BorderLayout.EAST);
		return new EntryPanel(true, rule.enabled, false, rule.minimized, true, true, true, true, false, nameWrapper, () -> {
			plugin.deleteRule(index);
			plugin.updateAnimationsAndTransmog();
		}, () -> {
			plugin.addNewRule(index + 1);
			plugin.updateAnimationsAndTransmog();
		}, (b) -> {
			rule.enabled = b;
			plugin.updateAnimationsAndTransmog();
		});
	}

}
