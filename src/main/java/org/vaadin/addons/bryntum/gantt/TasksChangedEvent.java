package org.vaadin.addons.bryntum.gantt;

import com.vaadin.flow.component.ComponentEvent;

import java.util.List;

/**
 * Fired when the user has modified tasks in the browser, for example by
 * dragging, resizing or editing them. The server side {@link Task} objects are
 * already updated when this event is fired. A single user action may change
 * multiple tasks, for example parent tasks and successors are re-scheduled by
 * the scheduling engine.
 */
public class TasksChangedEvent extends ComponentEvent<GanttChart> {

    private final List<Task> tasks;

    public TasksChangedEvent(GanttChart source, boolean fromClient, List<Task> tasks) {
        super(source, fromClient);
        this.tasks = List.copyOf(tasks);
    }

    /**
     * @return the tasks that were changed
     */
    public List<Task> getTasks() {
        return tasks;
    }
}
