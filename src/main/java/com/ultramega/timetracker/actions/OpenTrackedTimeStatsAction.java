package com.ultramega.timetracker.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.ultramega.timetracker.TimeTrackerService;
import com.ultramega.timetracker.display.TimeDisplayDialog;
import org.jetbrains.annotations.NotNull;

public final class OpenTrackedTimeStatsAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        Project project = actionEvent.getProject();
        if (project != null) {
            TimeDisplayDialog dialog = new TimeDisplayDialog(project.getService(TimeTrackerService.class));
            dialog.show();
        }
    }
}
