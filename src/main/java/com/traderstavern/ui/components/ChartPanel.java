package com.traderstavern.ui.components;

import com.traderstavern.model.PriceData;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.OHLCDataset;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

@Slf4j
public class ChartPanel extends BasePanel {
    private final JPanel chartContainer;
    private JFreeChart chart;
    private ChartPanel chartPanel;
    
    public ChartPanel() {
        addHeader("Price Chart");
        
        chartContainer = new JPanel(new BorderLayout());
        chartContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        
        add(chartContainer, BorderLayout.CENTER);
    }
    
    public void updateChart(List<PriceData> priceHistory) {
        if (priceHistory == null || priceHistory.isEmpty()) {
            return;
        }
        
        // Create price dataset
        int n = priceHistory.size();
        Date[] dates = new Date[n];
        double[] highs = new double[n];
        double[] lows = new double[n];
        double[] opens = new double[n];
        double[] closes = new double[n];
        double[] volumes = new double[n];
        
        for (int i = 0; i < n; i++) {
            PriceData price = priceHistory.get(i);
            dates[i] = new Date(price.getHighTimestamp());
            highs[i] = price.getHigh();
            lows[i] = price.getLow();
            opens[i] = price.getHigh(); // Using high as open since we don't have OHLC data
            closes[i] = price.getLow();  // Using low as close
            volumes[i] = Math.abs(price.getHigh() - price.getLow());
        }
        
        OHLCDataset dataset = new DefaultHighLowDataset(
            "Price",
            dates,
            highs,
            lows,
            opens,
            closes,
            volumes
        );
        
        // Create chart
        chart = ChartFactory.createCandlestickChart(
            null,           // No title
            "Time",        // X-axis label
            "Price",       // Y-axis label
            dataset,       // Dataset
            false          // No legend
        );
        
        // Customize chart
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(ColorScheme.DARKER_GRAY_COLOR);
        plot.setDomainGridlinePaint(ColorScheme.DARK_GRAY_COLOR);
        plot.setRangeGridlinePaint(ColorScheme.DARK_GRAY_COLOR);
        
        DateAxis dateAxis = (DateAxis) plot.getDomainAxis();
        dateAxis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));
        dateAxis.setLabelPaint(Color.WHITE);
        dateAxis.setTickLabelPaint(Color.WHITE);
        
        NumberAxis priceAxis = (NumberAxis) plot.getRangeAxis();
        priceAxis.setLabelPaint(Color.WHITE);
        priceAxis.setTickLabelPaint(Color.WHITE);
        
        CandlestickRenderer renderer = (CandlestickRenderer) plot.getRenderer();
        renderer.setUpPaint(Color.GREEN);
        renderer.setDownPaint(Color.RED);
        
        // Create or update chart panel
        if (chartPanel == null) {
            chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(600, 400));
            chartPanel.setMouseWheelEnabled(true);
            chartPanel.setZoomInFactor(0.8);
            chartPanel.setZoomOutFactor(1.2);
            chartContainer.add(chartPanel, BorderLayout.CENTER);
        } else {
            chartPanel.setChart(chart);
        }
        
        chartContainer.revalidate();
        chartContainer.repaint();
    }
    
    @Override
    public void refresh() {
        if (chart != null) {
            chartContainer.revalidate();
            chartContainer.repaint();
        }
    }
}