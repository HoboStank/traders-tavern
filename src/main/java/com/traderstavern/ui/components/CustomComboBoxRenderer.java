package com.traderstavern.ui.components;

import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CustomComboBoxRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(
            JList<?> list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
        
        JLabel renderer = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        
        // Set colors
        if (isSelected) {
            renderer.setBackground(ColorScheme.DARKER_GRAY_COLOR.brighter());
            renderer.setForeground(Color.WHITE);
        } else {
            renderer.setBackground(ColorScheme.DARKER_GRAY_COLOR);
            renderer.setForeground(Color.LIGHT_GRAY);
        }
        
        // Add padding
        renderer.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        return renderer;
    }
}