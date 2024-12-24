package com.ultramega.timetracker.display;

import com.intellij.openapi.ui.DialogWrapper;
import com.ultramega.timetracker.TimeTrackerService;
import com.ultramega.timetracker.utils.Bundle;
import com.ultramega.timetracker.utils.Utils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.concurrent.TimeUnit;

public class SummarizeStatsDialog extends DialogWrapper {
    private final TimeTrackerService timeTrackerService;

    public SummarizeStatsDialog(TimeTrackerService timeTrackerService) {
        super(true);
        this.timeTrackerService = timeTrackerService;

        init();
        setTitle(Bundle.message("display.SummarizeStats.title"));
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JLabel timeLabel = new JLabel(getGradleLabelText("coding.totalTime", timeTrackerService.totalTime));
        JLabel runLabel = new JLabel(getGradleLabelText("run.totalTime", TimeUnit.MILLISECONDS.toSeconds(timeTrackerService.getRunTime())));
        JLabel debugLabel = new JLabel(getGradleLabelText("debug.totalTime", TimeUnit.MILLISECONDS.toSeconds(timeTrackerService.getDebugTime())));
        JLabel idleLabel = new JLabel(getGradleLabelText("idle.totalTime", timeTrackerService.getIdleTime()));

        mainPanel.add(timeLabel);
        mainPanel.add(runLabel);
        mainPanel.add(debugLabel);
        mainPanel.add(idleLabel);

        return mainPanel;
    }

    private String getGradleLabelText(String name, long duration) {
        return String.format(Bundle.message("display.TimeTrackerStats." + name), Utils.convertSecondsToTime(duration));
    }
}
