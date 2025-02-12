package com.traderstavern.ui.components;

import com.traderstavern.analysis.TechnicalIndicators;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import java.awt.*;

@Slf4j
public class TechnicalOverlayPanel extends BasePanel {
    private final JPanel indicatorsPanel;
    private final JLabel macdLabel;
    private final JLabel rsiLabel;
    private final JLabel rocLabel;
    private final JLabel volumeLabel;
    private final JLabel trendLabel;
    
    public TechnicalOverlayPanel() {
        addHeader("Technical Analysis");
        
        indicatorsPanel = createContentPanel();
        indicatorsPanel.setLayout(new GridLayout(5, 2, 5, 5));
        
        // Create labels
        macdLabel = createValueLabel("MACD:");
        rsiLabel = createValueLabel("RSI:");
        rocLabel = createValueLabel("ROC:");
        volumeLabel = createValueLabel("Volume:");
        trendLabel = createValueLabel("Trend:");
        
        // Add components
        indicatorsPanel.add(createLabel("MACD:"));
        indicatorsPanel.add(macdLabel);
        indicatorsPanel.add(createLabel("RSI:"));
        indicatorsPanel.add(rsiLabel);
        indicatorsPanel.add(createLabel("ROC:"));
        indicatorsPanel.add(rocLabel);
        indicatorsPanel.add(createLabel("Volume:"));
        indicatorsPanel.add(volumeLabel);
        indicatorsPanel.add(createLabel("Trend:"));
        indicatorsPanel.add(trendLabel);
        
        add(indicatorsPanel, BorderLayout.CENTER);
    }
    
    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(FontManager.getRunescapeBoldFont());
        return label;
    }
    
    public void updateIndicators(TechnicalIndicators indicators) {
        if (indicators == null) {
            clearIndicators();
            return;
        }
        
        SwingUtilities.invokeLater(() -> {
            macdLabel.setText(String.format("%.2f", indicators.getMacd().getValue()));
            rsiLabel.setText(String.format("%.1f", indicators.getRsi().getValue()));
            rocLabel.setText(String.format("%.2f%%", indicators.getRoc().getValue()));
            volumeLabel.setText(formatVolume(indicators.getVolumeMetrics().getCurrentVolume()));
            trendLabel.setText(indicators.getTrendMetrics().getTrend().name());
            
            updateColors(indicators);
            revalidate();
            repaint();
        });
    }
    
    private void updateColors(TechnicalIndicators indicators) {
        // MACD color
        macdLabel.setForeground(indicators.getMacd().getValue() > 0 ? 
            ColorScheme.PROGRESS_COMPLETE_COLOR : ColorScheme.PROGRESS_ERROR_COLOR);
        
        // RSI color
        double rsi = indicators.getRsi().getValue();
        if (rsi > 70) {
            rsiLabel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
        } else if (rsi < 30) {
            rsiLabel.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
        } else {
            rsiLabel.setForeground(Color.WHITE);
        }
        
        // ROC color
        rocLabel.setForeground(indicators.getRoc().getValue() > 0 ?
            ColorScheme.PROGRESS_COMPLETE_COLOR : ColorScheme.PROGRESS_ERROR_COLOR);
            
        // Volume color
        volumeLabel.setForeground(indicators.getVolumeMetrics().isHighVolume() ?
            ColorScheme.PROGRESS_COMPLETE_COLOR : Color.WHITE);
            
        // Trend color
        switch (indicators.getTrendMetrics().getTrend()) {
            case UPTREND:
                trendLabel.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
                break;
            case DOWNTREND:
                trendLabel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
                break;
            default:
                trendLabel.setForeground(Color.WHITE);
        }
    }
    
    private void clearIndicators() {
        SwingUtilities.invokeLater(() -> {
            macdLabel.setText("-");
            rsiLabel.setText("-");
            rocLabel.setText("-");
            volumeLabel.setText("-");
            trendLabel.setText("-");
            
            macdLabel.setForeground(Color.WHITE);
            rsiLabel.setForeground(Color.WHITE);
            rocLabel.setForeground(Color.WHITE);
            volumeLabel.setForeground(Color.WHITE);
            trendLabel.setForeground(Color.WHITE);
            
            revalidate();
            repaint();
        });
    }
    
    private String formatVolume(long volume) {
        if (volume >= 1_000_000) {
            return String.format("%.1fM", volume / 1_000_000.0);
        } else if (volume >= 1_000) {
            return String.format("%.1fK", volume / 1_000.0);
        }
        return String.valueOf(volume);
    }
    
    @Override
    public void refresh() {
        revalidate();
        repaint();
    }
}