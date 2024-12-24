package com.ultramega.timetracker.listeners;

import com.intellij.openapi.application.ApplicationActivationListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.IdeFrame;
import com.ultramega.timetracker.TimeTrackerService;
import com.ultramega.timetracker.utils.Status;
import org.jetbrains.annotations.NotNull;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public final class TimeTrackerApplicationListener implements ApplicationActivationListener {
    private boolean focusListenerAdded = false;

    @Override
    public void applicationActivated(@NotNull IdeFrame ideFrame) {
        setStatus(ideFrame, Status.RUNNING);

        if (!focusListenerAdded) {
            ideFrame.getComponent().addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    applicationActivated(ideFrame);
                }

                @Override
                public void focusLost(FocusEvent e) {
                    applicationDeactivated(ideFrame);
                }
            });
            focusListenerAdded = true;
        }
    }

    @Override
    public void applicationDeactivated(@NotNull IdeFrame ideFrame) {
        setStatus(ideFrame, Status.STOPPED);
    }

    private void setStatus(IdeFrame ideFrame, Status status) {
        Project project = ideFrame.getProject();
        if (project != null) {
            project.getService(TimeTrackerService.class).setStatus(status);
        }
    }
}
