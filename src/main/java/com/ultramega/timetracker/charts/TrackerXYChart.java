package com.ultramega.timetracker.charts;

import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBFont;
import com.ultramega.timetracker.display.TimeDisplayDialog;
import com.ultramega.timetracker.utils.Bundle;
import com.ultramega.timetracker.utils.Utils;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.*;

public class TrackerXYChart extends XYChart {
    private final TimeDisplayDialog displayDialog;

    public TrackerXYChart(TimeDisplayDialog displayDialog) {
        super(new XYChartBuilder()
            .width(800)
            .height(600)
            .xAxisTitle("Date")
            .yAxisTitle("Time")
        );
        this.displayDialog = displayDialog;

        this.getStyler().setLegendVisible(false);
        this.getStyler().setAxisTitlesVisible(false);
        this.getStyler().setChartBackgroundColor(new Color(0, 0, 0, 0));
        this.getStyler().setPlotBackgroundColor(new Color(0, 0, 0, 0));
        this.getStyler().setPlotGridLinesColor(JBColor.foreground());

        this.getStyler().setAxisTickLabelsFont(JBFont.regular());
        this.getStyler().setAxisTickLabelsColor(JBColor.foreground());
        this.getStyler().setyAxisTickLabelsFormattingFunction(y -> Utils.convertSecondsToTime(y.longValue()));

        this.getStyler().setCursorEnabled(true);
        this.getStyler().setCursorLineWidth(2.0F);
        this.getStyler().setCursorColor(JBColor.foreground());
        this.getStyler().setCustomCursorYDataFormattingFunction(y -> Utils.convertSecondsToTime(y.longValue()));
        this.getStyler().setCursorZeroString(displayDialog.isShowTotalActivity() ? null : "00:00:00");
        this.getStyler().setCursorOrder(Utils.compareTimes());

        fillLineChartData();
    }

    private void fillLineChartData() {
        if (displayDialog.isShowTotalActivity()) {
            LineChartData data = getLineChartData(null);
            this.addSeries(Bundle.message("display.TimeTrackerStats.checkbox.TotalActivity"), data.xData, data.yData);
        } else {
            Set<String> processedClasses = new HashSet<>();

            displayDialog.getTimeTrackerService().getClassTimeData().forEach((dateKey, classData) -> {
                classData.forEach((className, time) -> {
                    if (processedClasses.add(className)) {
                        LineChartData data = getLineChartData(className);
                        boolean onlyZeroData = data.yData.stream().allMatch(value -> value == 0);
                        if (!onlyZeroData) {
                            this.addSeries(className, data.xData, data.yData);
                        }
                    }
                });
            });
        }
    }

    private LineChartData getLineChartData(String className) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startLocalDate = displayDialog.getStartLocalDateTime(now);
        Date startDate = Utils.convertLocalDateTimeToDate(startLocalDate);

        Map<Date, Long> timeData = fillTimeData(startDate, className);
        fillMissingTimePeriods(timeData, startLocalDate, now);

        List<Date> xData = new ArrayList<>();
        List<Long> yData = new ArrayList<>();

        timeData.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                xData.add(entry.getKey());
                yData.add(entry.getValue());
            });

        return new LineChartData(xData, yData);
    }

    private Map<Date, Long> fillTimeData(Date startDate, String className) {
        Map<Date, Long> data = new HashMap<>();

        displayDialog.getTimeTrackerService().getClassTimeData().forEach((dateKey, classData) -> {
            Date entryDate = Utils.convertStringToDate(dateKey);
            Date normalizedDate = normalizeDate(entryDate);

            if (normalizedDate.after(startDate) || normalizedDate.equals(startDate)) {
                classData.forEach((keyClassName, totalTime) -> {
                    if (className == null || keyClassName.equals(className)) {
                        data.merge(normalizedDate, totalTime, Long::sum);
                    }
                });
            }
        });

        return data;
    }

    private Date normalizeDate(Date date) {
        if (displayDialog.getSelectedFilter() == TimeDisplayDialog.FilterItems.WEEK || displayDialog.getSelectedFilter() == TimeDisplayDialog.FilterItems.MONTH || displayDialog.getSelectedFilter() == TimeDisplayDialog.FilterItems.YEAR) {
            LocalDateTime localDateTime = Utils.convertDateToLocalDateTime(date);
            if (displayDialog.getSelectedFilter() == TimeDisplayDialog.FilterItems.YEAR) {
                localDateTime = localDateTime.withDayOfMonth(1);
            }
            return Utils.convertLocalDateTimeToDate(localDateTime.truncatedTo(ChronoUnit.DAYS));
        }
        return date;
    }

    private void fillMissingTimePeriods(Map<Date, Long> data, LocalDateTime startLocalDate, LocalDateTime endLocalDate) {
        LocalDateTime cursor = startLocalDate;

        while (cursor.isBefore(endLocalDate)) {
            data.putIfAbsent(Utils.convertLocalDateTimeToDate(cursor), 0L);

            cursor = advanceCursor(cursor);
        }
    }

    public LocalDateTime advanceCursor(LocalDateTime cursor) {
        return switch (displayDialog.getSelectedFilter()) {
            case TWELVE_HOURS, TWENTY_FOUR_HOURS -> cursor.plusHours(1);
            case WEEK, MONTH -> cursor.plusDays(1);
            case YEAR -> cursor.plusMonths(1);
        };
    }

    public void updateLineChart() {
        this.clearSeries();
        fillLineChartData();
    }

    private record LineChartData(List<Date> xData, List<Long> yData) {
    }
}
