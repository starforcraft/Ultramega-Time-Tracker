package com.ultramega.timetracker.widgets;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.widget.StatusBarEditorBasedWidgetFactory;
import com.ultramega.timetracker.TimeTrackerService;
import com.ultramega.timetracker.utils.Bundle;
import kotlinx.coroutines.CoroutineScope;
import org.jetbrains.annotations.NotNull;

public final class TimeTrackerWidgetFactory extends StatusBarEditorBasedWidgetFactory {
    @Override
    public @NotNull String getId() {
        return TimeTrackerWidget.ID;
    }

    @Override
    public @NotNull String getDisplayName() {
        return Bundle.message("status.bar.ultramega_time_tracker.widget.name");
    }

    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project, @NotNull CoroutineScope scope) {
        return project.getService(TimeTrackerService.class).getWidget(scope);
    }
}
