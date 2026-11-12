package org.vaadin.addons.bryntum.gantt;

import com.vaadin.flow.component.ComponentEvent;

import java.util.List;

/**
 * Fired when the user has deleted tasks in the browser. The tasks (and
 * dependencies referring to them) are already removed from the
 * {@link GanttChart} when this event is fired.
 */
public class TasksRemovedEvent extends ComponentEvent<GanttChart> {

    private final List<Task> tasks;

    public TasksRemovedEvent(GanttChart source, boolean fromClient, List<Task> tasks) {
        super(source, fromClient);
        this.tasks = List.copyOf(tasks);
    }

    /**
     * @return the removed tasks. If a parent task was removed, only the parent
     * is listed, not its children.
     */
    public List<Task> getTasks() {
        return tasks;
    }
}
