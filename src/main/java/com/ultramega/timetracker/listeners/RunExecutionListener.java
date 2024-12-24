package com.ultramega.timetracker.listeners;

import com.intellij.execution.ExecutionListener;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.wm.ToolWindowId;
import com.ultramega.timetracker.TimeTrackerService;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// TODO Track gradle tasks
public final class RunExecutionListener implements ExecutionListener {
    private final Map<String, Long> startTimeMap = new ConcurrentHashMap<>();

    @Override
    public void processStarted(@NotNull String executorId, @NotNull ExecutionEnvironment env, @NotNull ProcessHandler handler) {
        startTimeMap.put(executorId, System.currentTimeMillis());
    }

    @Override
    public void processTerminated(@NotNull String executorId, @NotNull ExecutionEnvironment env, @NotNull ProcessHandler handler, int exitCode) {
        Long startTime = startTimeMap.remove(executorId);
        if (startTime == null) return;

        long duration = System.currentTimeMillis() - startTime;
        TimeTrackerService service = env.getProject().getService(TimeTrackerService.class);
        if (executorId.equals(ToolWindowId.RUN)) {
            service.addRunTime(duration);
        } else if (executorId.equals(ToolWindowId.DEBUG)) {
            service.addDebugTime(duration);
        }
    }
}
