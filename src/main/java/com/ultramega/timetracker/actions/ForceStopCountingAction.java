package com.ultramega.timetracker.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.ultramega.timetracker.TimeTrackerService;
import com.ultramega.timetracker.utils.Bundle;
import org.jetbrains.annotations.NotNull;

public final class ForceStopCountingAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        Project project = actionEvent.getProject();
        if (project != null) {
            TimeTrackerService service = project.getService(TimeTrackerService.class);
            service.forceStopCounting = !service.forceStopCounting;
            service.fireForceStopCountingChange();
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);

        Presentation presentation = e.getPresentation();
        Project project = e.getProject();
        if (project == null) return;

        TimeTrackerService service = project.getService(TimeTrackerService.class);

        if (service.forceStopCounting) {
            presentation.setText(Bundle.message("action.ResumeCounting.text"));
            presentation.setIcon(AllIcons.Debugger.ThreadRunning);
        } else {
            presentation.setText(Bundle.message("action.ForceStopCounting.text"));
            presentation.setIcon(AllIcons.Run.Stop);
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
