package com.ultramega.timetracker.display;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

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

public final class TimeDisplayDialog extends DialogWrapper {
    private final TimeTrackerService timeTrackerService;
    private final TrackerXYChart lineChart;
    private final TrackerPieChart pieChart;
    private final JLabel pieChartLabel;
    private final TrackerTable classTable;

    private FilterItems selectedFilter = FilterItems.TWELVE_HOURS;
    private SelectableCharts selectedChart = SelectableCharts.PIE_CHART;
    private boolean showTotalActivity = false;

    public TimeDisplayDialog(TimeTrackerService timeTrackerService) {
        super(true);
        this.timeTrackerService = timeTrackerService;
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

        // Second chart container
        JPanel secondChartPanelContainer = new JPanel(cardLayout);

        secondChartPanelContainer.add(pieChartPanel, SelectableCharts.PIE_CHART.getName());
        secondChartPanelContainer.add(tableScrollPane, SelectableCharts.TABLE.getName());

        // Select Chart Panel
        JPanel selectChartPanel = new JPanel(new BorderLayout());

        // Filter ComboBox
        ComboBox<FilterItems> filterComboBox = new ComboBox<>(FilterItems.values());
        filterComboBox.setSelectedItem(selectedFilter);
        filterComboBox.setRenderer(SimpleListCellRenderer.create("", (filterItems) ->
                Bundle.message("display.TimeTrackerStats.filter." + filterItems.getName())
        ));
        filterComboBox.addActionListener(e -> {
            FilterItems selectedItem = (FilterItems) filterComboBox.getSelectedItem();
            if (selectedItem != null) {
                selectedFilter = selectedItem;
                updateCharts(lineChartPanel, pieChartPanel, selectChartPanel);
            }
        });

        // Total Activity CheckBox
        JCheckBox totalCheckBox = new JCheckBox(Bundle.message("display.TimeTrackerStats.checkbox.TotalActivity"), showTotalActivity);
        totalCheckBox.addActionListener(e -> {
            showTotalActivity = totalCheckBox.isSelected();
            lineChart.getStyler().setCursorZeroString(showTotalActivity ? null : "00:00:00");
            updateCharts(lineChartPanel, pieChartPanel, selectChartPanel);
        });

        // Select Chart ComboBox
        ComboBox<SelectableCharts> selectChartComboBox = new ComboBox<>(SelectableCharts.values());
        selectChartComboBox.setRenderer(SimpleListCellRenderer.create("", (selectableCharts) ->
                Bundle.message("display.TimeTrackerStats.charts." + selectableCharts.getName())
        ));
        selectChartComboBox.addActionListener(e -> {
            SelectableCharts selectedItem = (SelectableCharts) selectChartComboBox.getSelectedItem();
            if (selectedItem != null) {
                selectedChart = selectedItem;
                cardLayout.show(secondChartPanelContainer, selectedChart.getName());
            }
        });
        selectChartPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        selectChartPanel.add(selectChartComboBox, BorderLayout.EAST);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(filterComboBox, BorderLayout.EAST);
        topPanel.add(totalCheckBox, BorderLayout.WEST);

        mainPanel.add(topPanel);
        mainPanel.add(lineChartPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(selectChartPanel);
        mainPanel.add(secondChartPanelContainer);

        return mainPanel;
    }

    // TODO: add live chart update (but it has to be performant, so only update relevant values instead of clearing it every second)
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

    public LocalDateTime getStartLocalDateTime(LocalDateTime now) {
        return switch (selectedFilter) {
            case TWELVE_HOURS -> now.minusHours(12).truncatedTo(ChronoUnit.HOURS);
            case TWENTY_FOUR_HOURS -> now.minusHours(24).truncatedTo(ChronoUnit.HOURS);
            case WEEK -> now.minusWeeks(1).truncatedTo(ChronoUnit.DAYS);
            case MONTH -> now.minusMonths(1).truncatedTo(ChronoUnit.DAYS);
            case YEAR -> now.minusYears(1).with(TemporalAdjusters.firstDayOfMonth()).truncatedTo(ChronoUnit.DAYS);
        };
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
        return selectedFilter;
    }

    public boolean isShowTotalActivity() {
        return showTotalActivity;
    }

    public enum FilterItems {
        TWELVE_HOURS("12hours"),
        TWENTY_FOUR_HOURS("24hours"),
        WEEK("week"),
        MONTH("month"),
        YEAR("year");
        //TOTAL("total");

        private final String name;

        FilterItems(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private enum SelectableCharts {
        PIE_CHART("pie_chart"),
        TABLE("table");

        private final String name;

        SelectableCharts(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
