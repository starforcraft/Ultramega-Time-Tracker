package com.ultramega.timetracker.charts;

import com.intellij.icons.AllIcons;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBFont;
import com.ultramega.timetracker.display.TimeDisplayDialog;
import com.ultramega.timetracker.utils.Utils;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.style.Styler;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public final class TrackerPieChart extends PieChart {
    private final TimeDisplayDialog displayDialog;

    public TrackerPieChart(TimeDisplayDialog displayDialog) {
        super(new PieChartBuilder()
            .width(800)
            .height(600)
            .theme(Styler.ChartTheme.GGPlot2)
        );
        this.displayDialog = displayDialog;

        this.getStyler().setLegendVisible(false);
        this.getStyler().setLabelsDistance(1.15);
        this.getStyler().setPlotContentSize(.7);
        this.getStyler().setStartAngleInDegrees(90);
        this.getStyler().setChartBackgroundColor(Utils.TRANSPARENT);
        this.getStyler().setPlotBackgroundColor(Utils.TRANSPARENT);
        this.getStyler().setCombineSmallSlices(true);
        this.getStyler().setInfoIcon(AllIcons.General.ShowInfos);
        //TODO Find better colors
        //this.getStyler().setSeriesColors(new BaseSeriesColors().getSeriesColors());

        this.getStyler().setLabelsFont(JBFont.regular());
        this.getStyler().setLabelsFontColor(JBColor.foreground());
        this.getStyler().setLabelsFontColorAutomaticEnabled(false);

        this.getStyler().setToolTipsEnabled(true);
        this.getStyler().setCustomCursorDataFormattingFunction(v -> Utils.convertSecondsToTime(v.longValue()));

        // Add data to chart
        Map<String, Long> pieChartData = getPieChartData();
        pieChartData.forEach(this::addSeries);
    }

    public Map<String, Long> getPieChartData() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startLocalDate = Utils.getStartLocalDateTime(now, displayDialog.getSelectedFilter());
        Date startDate = Utils.convertLocalDateTimeToDate(startLocalDate);

        Map<String, Long> pieChartData = new HashMap<>();
        Map<String, HashMap<String, Long>> classTimeData = displayDialog.getTimeTrackerService().getClassTimeData();

        classTimeData.forEach((key, classData) -> {
            Date entryDate = Utils.convertStringToDate(key);
            if (entryDate.after(startDate)) {
                classData.forEach((className, time) ->
                    pieChartData.merge(className, time, Long::sum)
                );
            }
        });

        return pieChartData;
    }

    public void updateSeries() {
        this.clearSeries();
        getPieChartData().forEach(this::addSeries);
    }
}
