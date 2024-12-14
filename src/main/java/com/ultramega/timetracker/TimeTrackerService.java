package com.ultramega.timetracker;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.concurrency.EdtExecutorService;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.ultramega.timetracker.actions.ForceStopCountingListener;
import com.ultramega.timetracker.utils.Status;
import com.ultramega.timetracker.utils.Utils;
import com.ultramega.timetracker.widgets.TimeTrackerWidget;
import kotlinx.coroutines.CoroutineScope;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service(Service.Level.PROJECT)
@State(name = "UltramegaTimeTracker", storages = @Storage("ultramega-time-tracker.xml"))
public final class TimeTrackerService implements PersistentStateComponent<TimeTrackerData> {
    private static final long TICK_DELAY = 1;
    private static final TimeUnit TICK_DELAY_UNIT = TimeUnit.SECONDS;

    private final TimeTrackerData timeTrackerData = new TimeTrackerData();
    private final Project project;

    public long totalTime;
    public boolean forceStopCounting = false;

    private TimeTrackerWidget widget;
    private ScheduledFuture<?> ticker;
    private Status status;

    public TimeTrackerService(@NotNull Project project) {
        this.project = project;
        setStatus(Status.RUNNING);
        checkTodayTime();
    }

    private synchronized void tick() {
        addTotalTime();
    }

    public void setStatus(Status status) {
        this.status = status;

        if (status == Status.STOPPED) {
            if (this.ticker != null) {
                // Since we cancel the task and only count in seconds, the actual time will be off by a few milliseconds each time you exit the application
                // And I currently don't think it's worth the effort and extra computing power to take this into account ¯\_(ツ)_/¯
                this.ticker.cancel(true);
                this.ticker = null;
            }
        } else {
            this.ticker = EdtExecutorService.getScheduledExecutorInstance().scheduleWithFixedDelay(this::tick, TICK_DELAY, TICK_DELAY, TICK_DELAY_UNIT);
        }
    }

    public TimeTrackerWidget getWidget(CoroutineScope scope) {
        if (widget == null) {
            this.widget = new TimeTrackerWidget(project, scope);
        }
        return widget;
    }

    @Override
    public void loadState(@NotNull TimeTrackerData timeTrackerData) {
        XmlSerializerUtil.copyBean(timeTrackerData, this.timeTrackerData);

        // Calculate total time after loading
        for (String date : timeTrackerData.classTimeData.keySet()) {
            HashMap<String, Long> classData = timeTrackerData.classTimeData.get(date);
            for (String className : classData.keySet()) {
                long time = classData.get(className);
                this.totalTime += time;
            }
        }
    }

    @Override
    public TimeTrackerData getState() {
        return timeTrackerData;
    }

    private void addTotalTime() {
        if (forceStopCounting) return;

        checkTodayTime();

        this.totalTime += TICK_DELAY;
        this.timeTrackerData.todayTime += TICK_DELAY;

        VirtualFile openedFile = Utils.getOpenedFile(project);
        if (openedFile != null) {
            String currentTime = Utils.getCurrentTimeAsString();
            String fileName = openedFile.getName();
            Map<String, Long> classTimeData = timeTrackerData.classTimeData.computeIfAbsent(currentTime, k -> new HashMap<>());

            classTimeData.put(fileName, classTimeData.getOrDefault(fileName, 0L) + TICK_DELAY);
        } else {
            this.timeTrackerData.idleTime += TICK_DELAY;
        }

        this.widget.update();
    }

    private void checkTodayTime() {
        LocalDateTime oldTodayDateTime = timeTrackerData.todayDateTime;
        LocalDateTime todayDateTime = LocalDate.now().atStartOfDay();
        if (oldTodayDateTime == null || !oldTodayDateTime.isEqual(todayDateTime)) {
            this.timeTrackerData.todayTime = 0;
            this.timeTrackerData.todayDateTime = LocalDate.now().atStartOfDay();
        }
    }

    public long getWidgetTime() {
        return getShowTodayTimeOption() ? getTodayTime() : getTotalTime();
    }

    private long getTotalTime() {
        return totalTime;
    }

    private long getTodayTime() {
        return timeTrackerData.todayTime;
    }

    public void changeShowTodayTimeOption() {
        timeTrackerData.showTodayTimeOption = !timeTrackerData.showTodayTimeOption;
    }

    public boolean getShowTodayTimeOption() {
        return timeTrackerData.showTodayTimeOption;
    }

    public HashMap<String, HashMap<String, Long>> getClassTimeData() {
        return timeTrackerData.classTimeData;
    }

    public Status getStatus() {
        return status;
    }

    public void fireForceStopCountingChange() {
        ForceStopCountingListener publisher = ApplicationManager.getApplication().getMessageBus()
                .syncPublisher(ForceStopCountingListener.FORCE_STOP_COUNTING_CHANGES);
        publisher.propertyChanged();
    }
}
