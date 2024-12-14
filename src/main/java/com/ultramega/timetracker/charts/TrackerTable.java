package com.ultramega.timetracker.charts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.intellij.ui.table.JBTable;
import com.ultramega.timetracker.utils.Bundle;
import com.ultramega.timetracker.utils.Utils;

public class TrackerTable extends JBTable {
    private final TrackerPieChart pieChart;

    public TrackerTable(TrackerPieChart pieChart) {
        this.pieChart = pieChart;

        DefaultTableModel model = this.getTableModel();
        this.setModel(model);

        this.setBorder(BorderFactory.createLineBorder(this.getGridColor(), 1));

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        sorter.setComparator(1, Utils.compareTimes());

        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(1, SortOrder.DESCENDING));
        sorter.setSortKeys(sortKeys);

        this.setRowSorter(sorter);
    }

    private DefaultTableModel getTableModel() {
        String[] columnNames = { Bundle.message("display.TimeTrackerStats.table.ClassName"), Bundle.message("display.TimeTrackerStats.table.Time") };
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        fillTableModelWithData(model);

        return model;
    }

    private void fillTableModelWithData(DefaultTableModel model) {
        Map<String, Long> pieChartData = pieChart.getPieChartData();
        for (String className : pieChartData.keySet()) {
            long time = pieChartData.get(className);
            String timeString = Utils.convertSecondsToTime(time);

            model.addRow(new Object[] {className, timeString});
        }
    }

    public void updateTable() {
        DefaultTableModel model = (DefaultTableModel) getModel();
        model.setRowCount(0);
        fillTableModelWithData(model);
    }
}
