package org.vaadin.addons.bryntum.gantt;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * A task (row) in a {@link GanttChart}. Tasks can be nested to form a
 * hierarchy, a task with children is a "summary task" whose dates are
 * calculated from its children by the Bryntum scheduling engine.
 * <p>
 * Typically you define either {@link #setStartDate(LocalDate) start date} and
 * {@link #setDuration(Number) duration} or start and
 * {@link #setEndDate(LocalDate) end date}, the scheduling engine calculates
 * the rest. Note that Bryntum end dates are exclusive: a one day task starting
 * on 2026-01-05 ends on 2026-01-06.
 * <p>
 * After modifying tasks of a chart that is already shown, call
 * {@link GanttChart#refresh()} to update the client side.
 */
public class Task implements Serializable {

    private String id = UUID.randomUUID().toString();
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double duration;
    private Double percentDone;
    private boolean expanded = true;
    private boolean manuallyScheduled;
    private Task parent;
    private final List<Task> children = new ArrayList<>();

    public Task() {
    }

    public Task(String name) {
        this.name = name;
    }

    public Task(String name, LocalDate startDate, Number durationInDays) {
        this(name);
        this.startDate = startDate;
        setDuration(durationInDays);
    }

    /**
     * @return the identifier of the task, a random UUID by default
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the identifier for the task. Must be unique within a chart.
     *
     * @param id the identifier
     */
    public void setId(String id) {
        this.id = Objects.requireNonNull(id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    /**
     * @return the (exclusive) end date of the task
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * @param endDate the (exclusive) end date of the task
     */
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    /**
     * @return the duration of the task in days, or null if not defined
     */
    public Double getDuration() {
        return duration;
    }

    /**
     * @param durationInDays the duration of the task in days, null to let the
     *                       scheduling engine calculate it
     */
    public void setDuration(Number durationInDays) {
        this.duration = durationInDays == null ? null : durationInDays.doubleValue();
    }

    /**
     * @return the completion percentage (0-100), or null if not defined
     */
    public Double getPercentDone() {
        return percentDone;
    }

    /**
     * @param percentDone the completion percentage (0-100)
     */
    public void setPercentDone(Number percentDone) {
        this.percentDone = percentDone == null ? null : percentDone.doubleValue();
    }

    public boolean isExpanded() {
        return expanded;
    }

    /**
     * @param expanded true if the children of this task should be initially
     *                 visible
     */
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public boolean isManuallyScheduled() {
        return manuallyScheduled;
    }

    /**
     * @param manuallyScheduled true if the scheduling engine should not move
     *                          this task based on its dependencies or children
     */
    public void setManuallyScheduled(boolean manuallyScheduled) {
        this.manuallyScheduled = manuallyScheduled;
    }

    /**
     * @return the parent task, or null if this is a top level task
     */
    public Task getParent() {
        return parent;
    }

    /**
     * @return an unmodifiable view of the child tasks
     */
    public List<Task> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /**
     * Adds a child task.
     *
     * @param child the child to add
     * @return the added child
     */
    public Task addChild(Task child) {
        if (child.parent != null) {
            child.parent.removeChild(child);
        }
        child.parent = this;
        children.add(child);
        return child;
    }

    /**
     * Creates and adds a child task with the given name.
     *
     * @param name the name of the new task
     * @return the created task
     */
    public Task addChild(String name) {
        return addChild(new Task(name));
    }

    /**
     * Creates and adds a child task.
     *
     * @param name           the name of the new task
     * @param startDate      the start date of the new task
     * @param durationInDays the duration of the new task in days
     * @return the created task
     */
    public Task addChild(String name, LocalDate startDate, Number durationInDays) {
        return addChild(new Task(name, startDate, durationInDays));
    }

    public void removeChild(Task child) {
        if (children.remove(child)) {
            child.parent = null;
        }
    }

    @Override
    public String toString() {
        return "Task{id='" + id + "', name='" + name + "', startDate=" + startDate
                + ", endDate=" + endDate + ", duration=" + duration + "}";
    }
}
