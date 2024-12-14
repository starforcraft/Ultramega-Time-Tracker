package com.ultramega.timetracker.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.KeepPopupOnPerform;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.ultramega.timetracker.TimeTrackerService;
import org.jetbrains.annotations.NotNull;

public final class ShowTodayTimeAction extends DumbAwareAction {
    ShowTodayTimeAction() {
        getTemplatePresentation().setKeepPopupOnPerform(KeepPopupOnPerform.Always);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        Project project = actionEvent.getProject();
        if (project != null) {
            TimeTrackerService service = project.getService(TimeTrackerService.class);
            service.changeShowTodayTimeOption();
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);

        Presentation presentation = e.getPresentation();
        Project project = e.getProject();
        if (project == null) return;

        TimeTrackerService service = project.getService(TimeTrackerService.class);

        presentation.setIcon(service.getShowTodayTimeOption() ? AllIcons.Actions.Checked : null);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
