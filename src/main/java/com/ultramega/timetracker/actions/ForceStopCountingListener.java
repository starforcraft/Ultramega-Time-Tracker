package com.ultramega.timetracker.actions;

import com.intellij.util.messages.Topic;

public interface ForceStopCountingListener {
    @Topic.AppLevel
    Topic<ForceStopCountingListener> FORCE_STOP_COUNTING_CHANGES = new Topic<>(ForceStopCountingListener.class, Topic.BroadcastDirection.TO_DIRECT_CHILDREN);

    void propertyChanged();
}
