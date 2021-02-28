package com.weaponanimationreplacer;

import lombok.Getter;
import net.runelite.client.plugins.screenmarkers.ScreenMarkerPlugin;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.PluginErrorPanel;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class WeaponAnimationReplacerPluginPanel extends PluginPanel {
    private static final ImageIcon ADD_ICON;
    private static final ImageIcon ADD_HOVER_ICON;

    private static final Color DEFAULT_BORDER_COLOR = Color.GREEN;
    private static final Color DEFAULT_FILL_COLOR = new Color(0, 255, 0, 0);

    private static final int DEFAULT_BORDER_THICKNESS = 3;

    private final JLabel addMarker = new JLabel(ADD_ICON);
    private final JLabel title = new JLabel();
    private final PluginErrorPanel noMarkersPanel = new PluginErrorPanel();
    private final JPanel markerView = new JPanel(new GridBagLayout());

    private final WeaponAnimationReplacerPlugin plugin;

    @Getter
    private Color selectedColor = DEFAULT_BORDER_COLOR;

    @Getter
    private Color selectedFillColor = DEFAULT_FILL_COLOR;

    @Getter
    private int selectedBorderThickness = DEFAULT_BORDER_THICKNESS;

    static
    {
        final BufferedImage addIcon = ImageUtil.loadImageResource(ScreenMarkerPlugin.class, "add_icon.png");
        ADD_ICON = new ImageIcon(addIcon);
        ADD_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(addIcon, 0.53f));
    }

    public WeaponAnimationReplacerPluginPanel(WeaponAnimationReplacerPlugin weaponAnimationReplacerPlugin)
    {
        this.plugin = weaponAnimationReplacerPlugin;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBorder(new EmptyBorder(1, 0, 10, 0));

        title.setText("Animation Replacements");
        title.setBackground(ColorScheme.DARK_GRAY_COLOR);
        title.setForeground(Color.WHITE);

        northPanel.add(title, BorderLayout.WEST);
        northPanel.add(addMarker, BorderLayout.EAST);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

        markerView.setBackground(ColorScheme.DARK_GRAY_COLOR);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;

        noMarkersPanel.setContent("fixme", "fixme");
        noMarkersPanel.setVisible(false);

        markerView.add(noMarkersPanel, constraints);
        constraints.gridy++;

        addMarker.setToolTipText("Add a new animation replacement rule.");
        addMarker.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent mouseEvent)
            {
//                setCreation(true);
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent)
            {
                addMarker.setIcon(ADD_HOVER_ICON);
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent)
            {
                addMarker.setIcon(ADD_ICON);
            }
        });

        centerPanel.add(markerView, BorderLayout.CENTER);

        add(northPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void rebuild()
    {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;

        markerView.removeAll();

        int index = 0;
        for (AnimationReplacementRule animationReplacementRule : ((plugin == null) ? plugin.getAnimationReplacementRules() : plugin.getAnimationReplacementRules()))
        {
            markerView.add(new AnimationReplacementRulePanel(plugin, animationReplacementRule, () -> rebuild(), index++), constraints);
            constraints.gridy++;

            markerView.add(Box.createRigidArea(new Dimension(0, 10)), constraints);
            constraints.gridy++;
        }

        boolean empty = constraints.gridy == 0;
        noMarkersPanel.setVisible(empty);
        title.setVisible(!empty);

        markerView.add(noMarkersPanel, constraints);
        constraints.gridy++;

//        markerView.add(creationPanel, constraints);
//        constraints.gridy++;
//
        repaint();
        revalidate();
    }

    public void deleteNewRule(int index) {

    }

    public void addNewRule(int index) {
    }
}
