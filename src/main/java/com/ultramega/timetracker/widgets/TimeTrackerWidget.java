package com.ultramega.timetracker.widgets;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedStatusBarPopup;
import com.intellij.util.messages.MessageBusConnection;
import com.ultramega.timetracker.TimeTrackerService;
import com.ultramega.timetracker.actions.ForceStopCountingListener;
import com.ultramega.timetracker.utils.Bundle;
import com.ultramega.timetracker.utils.Utils;
import kotlinx.coroutines.CoroutineScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TimeTrackerWidget extends EditorBasedStatusBarPopup {
    public static final String ID = "com.ultramega.timetracker";

    private final TimeTrackerService timeTrackerService;

    public TimeTrackerWidget(@NotNull Project project, @NotNull CoroutineScope scope) {
        super(project, false, scope);
        this.timeTrackerService = project.getService(TimeTrackerService.class);
    }

    @Override
    protected @NotNull WidgetState getWidgetState(@Nullable VirtualFile file) {
        if (timeTrackerService.forceStopCounting) {
            return WidgetState.NO_CHANGE;
        }

        String toolTipText = Bundle.message("status.bar.ultramega_time_tracker.widget.name");
        return new WidgetState(toolTipText, Utils.convertSecondsToTime(timeTrackerService.getWidgetTime()), true);
    }

    // Use updateComponent instead of update to ensure the widget refreshes correctly when a dialog is opened.
    public void updateComponent() {
        super.updateComponent(getWidgetState(null));
    }

    @Override
    protected void registerCustomListeners(@NotNull MessageBusConnection connection) {
        connection.subscribe(ForceStopCountingListener.FORCE_STOP_COUNTING_CHANGES, (ForceStopCountingListener) this::update);
    }

    @Override
    protected @Nullable ListPopup createPopup(@NotNull DataContext context) {
        AnAction group = ActionManager.getInstance().getAction("UltramegaTimeTracker");
        if (!(group instanceof ActionGroup)) {
            return null;
        }

        return JBPopupFactory.getInstance().createActionGroupPopup(
                Bundle.message("status.bar.ultramega_time_tracker.widget.name"),
                (ActionGroup)group,
                context,
                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                false
        );
    }

    @Override
    protected @NotNull StatusBarWidget createInstance(@NotNull Project project) {
        return project.getService(TimeTrackerService.class).getWidget(getScope());
    }

    @Override
    public @NotNull String ID() {
        return ID;
    }
}
