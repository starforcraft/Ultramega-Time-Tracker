package com.ultramega.timetracker;

import com.intellij.openapi.application.ApplicationActivationListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.IdeFrame;
import com.ultramega.timetracker.utils.Status;
import org.jetbrains.annotations.NotNull;

public final class TimeTrackerApplicationListener implements ApplicationActivationListener {
    private TimeTrackerService timeTrackerService;

    @Override
    public void applicationActivated(@NotNull IdeFrame ideFrame) {
        setStatus(ideFrame, Status.RUNNING);
    }

    @Override
    public void applicationDeactivated(@NotNull IdeFrame ideFrame) {
        setStatus(ideFrame, Status.STOPPED);
    }

    private void setStatus(IdeFrame ideFrame, Status status) {
        if (timeTrackerService == null) {
            Project project = ideFrame.getProject();
            if (project != null) {
                this.timeTrackerService = project.getService(TimeTrackerService.class);
            }
        }
        if (timeTrackerService != null) {
            this.timeTrackerService.setStatus(status);
        }
    }
}
