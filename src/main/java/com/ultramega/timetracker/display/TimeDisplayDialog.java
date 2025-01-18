package com.ultramega.timetracker.display;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.ui.UIBundle;
import com.intellij.ui.components.JBScrollPane;
import com.ultramega.timetracker.TimeTrackerService;
import com.ultramega.timetracker.charts.TrackerPieChart;
import com.ultramega.timetracker.charts.TrackerTable;
import com.ultramega.timetracker.charts.TrackerXYChart;
import com.ultramega.timetracker.utils.Bundle;
import org.knowm.xchart.XChartPanel;

import javax.swing.*;
import java.awt.*;

public final class TimeDisplayDialog extends DialogWrapper {
    private final TimeTrackerService timeTrackerService;
    private final TimeDialogSelectableOptions selectableOptions;
    private final TrackerXYChart lineChart;
    private final TrackerPieChart pieChart;
    private final JLabel pieChartLabel;
    private final TrackerTable classTable;

    public TimeDisplayDialog(TimeTrackerService timeTrackerService) {
        super(true);
        this.timeTrackerService = timeTrackerService;
        this.selectableOptions = timeTrackerService.getDialogSelectableOptions();
        this.lineChart = new TrackerXYChart(this);
        this.pieChart = new TrackerPieChart(this);
        this.pieChartLabel = new JLabel(UIBundle.message("message.nothingToShow"));
        this.pieChartLabel.setVisible(pieChart.getSeriesAmount() == 0);
        this.classTable = new TrackerTable(pieChart);

        init();
        setTitle(Bundle.message("display.TimeTrackerStats.title"));
    }

    @Override
    protected JComponent createCenterPanel() {
        CardLayout cardLayout = new CardLayout();
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Line Chart Panel
        JPanel lineChartPanel = new XChartPanel<>(lineChart);
        lineChartPanel.setOpaque(false);

        // Table Scroll Pane
        JBScrollPane tableScrollPane = new JBScrollPane(classTable);
        tableScrollPane.setPreferredSize(new Dimension(800, 600));

        // Pie Chart Panel
        JPanel pieChartPanel = new XChartPanel<>(pieChart);
        pieChartPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        pieChartPanel.add(pieChartLabel, gbc);

        pieChartPanel.setOpaque(false);

        // Second Chart Container
        JPanel secondChartPanelContainer = new JPanel(cardLayout);

        secondChartPanelContainer.add(pieChartPanel, SelectableCharts.PIE_CHART.getName());
        secondChartPanelContainer.add(tableScrollPane, SelectableCharts.TABLE.getName());

        // Select Chart Panel
        JPanel selectChartPanel = new JPanel(new BorderLayout());

        // Total Activity CheckBox
        JCheckBox totalCheckBox = new JCheckBox(Bundle.message("display.TimeTrackerStats.checkbox.totalActivity"), selectableOptions.showTotalActivity);
        totalCheckBox.addActionListener(e -> {
            selectableOptions.showTotalActivity = totalCheckBox.isSelected();
            lineChart.getStyler().setCursorZeroString(selectableOptions.showTotalActivity ? null : "00:00:00");
            updateCharts(lineChartPanel, pieChartPanel, selectChartPanel);
        });

        // Summarize Stats Button
        JPanel summarizeStatsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        JButton summarizeStatsButton = new JButton(Bundle.message("display.TimeTrackerStats.button.summarizeStats"));
        summarizeStatsButton.addActionListener(e -> {
            SummarizeStatsDialog dialog = new SummarizeStatsDialog(timeTrackerService);
            dialog.show();
        });
        summarizeStatsPanel.add(summarizeStatsButton);

        // TODO: add dropdown to deselect and select tracked classes (filter options)

        // Filter Time ComboBox
        ComboBox<FilterItems> filterTimeComboBox = new ComboBox<>(FilterItems.values());
        filterTimeComboBox.setSelectedItem(selectableOptions.selectedFilter);
        filterTimeComboBox.setRenderer(SimpleListCellRenderer.create("", (filterItems) ->
                Bundle.message("display.TimeTrackerStats.filter.time." + filterItems.getName())
        ));
        filterTimeComboBox.addActionListener(e -> {
            FilterItems selectedItem = (FilterItems) filterTimeComboBox.getSelectedItem();
            if (selectedItem != null) {
                selectableOptions.selectedFilter = selectedItem;
                updateCharts(lineChartPanel, pieChartPanel, selectChartPanel);
            }
        });

        // Select Chart ComboBox
        ComboBox<SelectableCharts> selectChartComboBox = new ComboBox<>(SelectableCharts.values());
        selectChartComboBox.setRenderer(SimpleListCellRenderer.create("", (selectableCharts) ->
                Bundle.message("display.TimeTrackerStats.charts." + selectableCharts.getName())
        ));
        selectChartComboBox.addActionListener(e -> {
            SelectableCharts selectedItem = (SelectableCharts) selectChartComboBox.getSelectedItem();
            if (selectedItem != null) {
                selectableOptions.selectedChart = selectedItem;
                cardLayout.show(secondChartPanelContainer, selectableOptions.selectedChart.getName());
            }
        });
        selectChartPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        selectChartPanel.add(selectChartComboBox, BorderLayout.EAST);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(totalCheckBox, BorderLayout.WEST);
        topPanel.add(summarizeStatsPanel, BorderLayout.CENTER);
        topPanel.add(filterTimeComboBox, BorderLayout.EAST);

        mainPanel.add(topPanel);
        mainPanel.add(lineChartPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(selectChartPanel);
        mainPanel.add(secondChartPanelContainer);

        return mainPanel;
    }

    // TODO add live chart update (but it has to be performant, so only update relevant values instead of clearing it every second)
    private void updateCharts(JPanel lineChartPanel, JPanel pieChartPanel, JPanel selectChartPanel) {
        SwingUtilities.invokeLater(() -> {
            updateLineChart(lineChartPanel);
            updatePieChart(pieChartPanel);
            updateClassTable(selectChartPanel);
        });
    }

    private void updateLineChart(JPanel lineChartPanel) {
        lineChart.updateLineChart();

        lineChartPanel.revalidate();
        lineChartPanel.repaint();
    }

    private void updateClassTable(JPanel selectChartPanel) {
        classTable.updateTable();

        selectChartPanel.revalidate();
        selectChartPanel.repaint();
    }

    private void updatePieChart(JPanel pieChartPanel) {
        pieChart.updateSeries();

        pieChartLabel.setVisible(pieChart.getSeriesAmount() == 0);

        pieChartPanel.revalidate();
        pieChartPanel.repaint();
    }

    @Override
    protected Action[] createActions() {
        // Remove OK and Cancel Buttons
        return new Action[0];
    }

    public TimeTrackerService getTimeTrackerService() {
        return timeTrackerService;
    }

    public FilterItems getSelectedFilter() {
        return selectableOptions.selectedFilter;
    }

    public boolean isShowTotalActivity() {
        return selectableOptions.showTotalActivity;
    }
}
