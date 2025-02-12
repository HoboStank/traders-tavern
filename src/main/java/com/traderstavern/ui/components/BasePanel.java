package com.traderstavern.ui.components;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public abstract class BasePanel extends JPanel {
    protected static final int CONTENT_PADDING = 10;
    
    public BasePanel() {
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setBorder(new EmptyBorder(CONTENT_PADDING, CONTENT_PADDING, CONTENT_PADDING, CONTENT_PADDING));
    }
    
    protected void addHeader(String text) {
        JLabel header = new JLabel(text);
        header.setForeground(Color.WHITE);
        header.setBorder(new EmptyBorder(0, 0, CONTENT_PADDING, 0));
        add(header, BorderLayout.NORTH);
    }
    
    protected JPanel createContentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        return panel;
    }
    
    protected JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        return label;
    }
    
    protected JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        button.setForeground(Color.WHITE);
        return button;
    }
    
    protected JComboBox<?> createComboBox() {
        JComboBox<?> comboBox = new JComboBox<>();
        comboBox.setForeground(Color.WHITE);
        comboBox.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        return comboBox;
    }
    
    protected JCheckBox createCheckBox(String text) {
        JCheckBox checkBox = new JCheckBox(text);
        checkBox.setForeground(Color.WHITE);
        checkBox.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        return checkBox;
    }
    
    public abstract void refresh();
}