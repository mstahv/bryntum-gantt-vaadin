package org.vaadin.addons.bryntum.gantt;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.shared.Registration;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A Vaadin component wrapping <a href="https://bryntum.com/products/gantt/">Bryntum
 * Gantt</a> (the MIT licensed Community tier).
 * <p>
 * The server side {@link Task} and {@link Dependency} objects are the source of
 * truth. Changes the user makes to existing tasks in the browser (dragging,
 * resizing, editing) are synchronized back to the server side objects and
 * reported with {@link #addTasksChangedListener(ComponentEventListener)}.
 * Tasks deleted in the browser are removed from the server side model and
 * reported with {@link #addTasksRemovedListener(ComponentEventListener)}.
 * Adding tasks and dependencies is done via the Java API.
 * <p>
 * The component needs a defined size, by default it is 100% wide and 400px high.
 */
@Tag("vaadin-bryntum-gantt")
@NpmPackage(value = "@bryntum/gantt-community", version = GanttChart.BRYNTUM_VERSION)
@NpmPackage(value = "@bryntum/core-community", version = GanttChart.BRYNTUM_VERSION)
@NpmPackage(value = "@bryntum/grid-community", version = GanttChart.BRYNTUM_VERSION)
@NpmPackage(value = "@bryntum/scheduler-community", version = GanttChart.BRYNTUM_VERSION)
@JsModule("./bryntum-gantt/vaadin-bryntum-gantt.js")
public class GanttChart extends Component implements HasSize, HasStyle {

    /**
     * The version of Bryntum Gantt used by this integration.
     */
    public static final String BRYNTUM_VERSION = "8.0.1";

    private final List<Task> tasks = new ArrayList<>();
    private final List<Dependency> dependencies = new ArrayList<>();
    private List<Column> columns = List.of(Column.name(), Column.startDate(), Column.duration(),
            Column.percentDone());
    private boolean readOnly;
    private boolean zoomToFitOnLoad = true;

    private boolean initPending;
    private boolean dataPending;
    private boolean updateScheduled;

    public GanttChart() {
        setWidthFull();
        setHeight("400px");
        getElement().addEventListener("tasks-changed", this::onTasksChanged)
                .addEventData("event.detail");
        getElement().addEventListener("tasks-removed", this::onTasksRemoved)
                .addEventData("event.detail");
    }

    /**
     * @return an unmodifiable view of the top level tasks
     */
    public List<Task> getTasks() {
        return List.copyOf(tasks);
    }

    /**
     * Replaces all tasks. Dependencies are cleared as well.
     *
     * @param tasks the new top level tasks
     */
    public void setTasks(Collection<Task> tasks) {
        this.tasks.clear();
        this.tasks.addAll(tasks);
        this.dependencies.clear();
        refresh();
    }

    /**
     * Adds a top level task.
     *
     * @param task the task to add
     * @return the added task
     */
    public Task addTask(Task task) {
        tasks.add(Objects.requireNonNull(task));
        refresh();
        return task;
    }

    /**
     * Creates and adds a top level task.
     *
     * @param name the name of the task
     * @return the created task
     */
    public Task addTask(String name) {
        return addTask(new Task(name));
    }

    /**
     * Creates and adds a top level task.
     *
     * @param name           the name of the task
     * @param startDate      the start date
     * @param durationInDays the duration in days
     * @return the created task
     */
    public Task addTask(String name, LocalDate startDate, Number durationInDays) {
        return addTask(new Task(name, startDate, durationInDays));
    }

    /**
     * Removes a task (top level or nested) and dependencies referring to it or
     * its children.
     *
     * @param task the task to remove
     */
    public void removeTask(Task task) {
        detach(task);
        refresh();
    }

    private void detach(Task task) {
        if (task.getParent() != null) {
            task.getParent().removeChild(task);
        } else {
            tasks.remove(task);
        }
        Set<Task> removed = new HashSet<>();
        collect(task, removed::add);
        dependencies.removeIf(d -> removed.contains(d.getFrom()) || removed.contains(d.getTo()));
    }

    /**
     * Finds a task (top level or nested) by its id.
     *
     * @param id the task id
     * @return the task, if found
     */
    public Optional<Task> findTask(String id) {
        return Optional.ofNullable(taskIndex().get(id));
    }

    /**
     * Adds a "finish-to-start" dependency between two tasks.
     *
     * @param from the predecessor
     * @param to   the successor
     * @return the created dependency, which can be further configured
     */
    public Dependency addDependency(Task from, Task to) {
        Dependency dependency = new Dependency(from, to);
        dependencies.add(dependency);
        refresh();
        return dependency;
    }

    public void removeDependency(Dependency dependency) {
        dependencies.remove(dependency);
        refresh();
    }

    /**
     * @return an unmodifiable view of the dependencies
     */
    public List<Dependency> getDependencies() {
        return List.copyOf(dependencies);
    }

    /**
     * Sets the columns shown in the grid part of the chart.
     *
     * @param columns the columns
     */
    public void setColumns(Column... columns) {
        this.columns = List.of(columns);
        reinit();
    }

    public List<Column> getColumns() {
        return columns;
    }

    /**
     * @param readOnly true to prevent the user from modifying the chart
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        reinit();
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * @param zoomToFitOnLoad true (the default) to zoom the time axis to fit
     *                        all tasks when data is loaded
     */
    public void setZoomToFitOnLoad(boolean zoomToFitOnLoad) {
        this.zoomToFitOnLoad = zoomToFitOnLoad;
        reinit();
    }

    public boolean isZoomToFitOnLoad() {
        return zoomToFitOnLoad;
    }

    /**
     * Zooms the time axis to fit all tasks.
     */
    public void zoomToFit() {
        getElement().callJsFunction("zoomToFit");
    }

    /**
     * Scrolls the given task into view and highlights it.
     *
     * @param task the task
     */
    public void scrollToTask(Task task) {
        getElement().callJsFunction("scrollToTask", task.getId());
    }

    /**
     * Sends the current state of tasks and dependencies to the browser. Call
     * this after modifying {@link Task} or {@link Dependency} objects directly.
     */
    public void refresh() {
        dataPending = true;
        scheduleUpdate();
    }

    /**
     * Adds a listener notified when the user modifies tasks in the browser.
     *
     * @param listener the listener
     * @return a registration for removing the listener
     */
    public Registration addTasksChangedListener(ComponentEventListener<TasksChangedEvent> listener) {
        return addListener(TasksChangedEvent.class, listener);
    }

    /**
     * Adds a listener notified when the user deletes tasks in the browser.
     *
     * @param listener the listener
     * @return a registration for removing the listener
     */
    public Registration addTasksRemovedListener(ComponentEventListener<TasksRemovedEvent> listener) {
        return addListener(TasksRemovedEvent.class, listener);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // client side element is fresh after (re)attach
        updateScheduled = false;
        reinit();
    }

    private void reinit() {
        initPending = true;
        scheduleUpdate();
    }

    private void scheduleUpdate() {
        if (updateScheduled) {
            return;
        }
        updateScheduled = true;
        getElement().getNode().runWhenAttached(ui -> ui.beforeClientResponse(this, ctx -> {
            updateScheduled = false;
            if (initPending) {
                getElement().callJsFunction("init",
                        new Config(columns, readOnly, zoomToFitOnLoad), taskData(), dependencyData());
            } else if (dataPending) {
                getElement().callJsFunction("setData", taskData(), dependencyData());
            }
            initPending = false;
            dataPending = false;
        }));
    }

    private void onTasksChanged(DomEvent event) {
        List<TaskChange> changes = event.getEventData(TasksChangedPayload.class).event().detail().tasks();
        Map<String, Task> index = taskIndex();
        List<Task> changed = new ArrayList<>();
        for (TaskChange change : changes) {
            Task task = index.get(change.id());
            if (task != null) {
                task.setName(change.name());
                task.setStartDate(change.startDate());
                task.setEndDate(change.endDate());
                task.setDuration(change.duration());
                task.setPercentDone(change.percentDone());
                changed.add(task);
            }
        }
        if (!changed.isEmpty()) {
            fireEvent(new TasksChangedEvent(this, true, changed));
        }
    }

    private void onTasksRemoved(DomEvent event) {
        List<String> ids = event.getEventData(TasksRemovedPayload.class).event().detail().ids();
        Map<String, Task> index = taskIndex();
        List<Task> removed = ids.stream().map(index::get).filter(Objects::nonNull).toList();
        // the client is already up to date, no refresh needed
        removed.forEach(this::detach);
        if (!removed.isEmpty()) {
            fireEvent(new TasksRemovedEvent(this, true, removed));
        }
    }

    private Map<String, Task> taskIndex() {
        Map<String, Task> index = new HashMap<>();
        tasks.forEach(t -> collect(t, task -> index.put(task.getId(), task)));
        return index;
    }

    private static void collect(Task task, Consumer<Task> consumer) {
        consumer.accept(task);
        task.getChildren().forEach(c -> collect(c, consumer));
    }

    List<TaskData> taskData() {
        return tasks.stream().map(TaskData::of).toList();
    }

    List<DependencyData> dependencyData() {
        return dependencies.stream().map(DependencyData::of).toList();
    }

    // Data transfer types for the client side, field names match Bryntum models

    record Config(List<Column> columns, boolean readOnly, boolean zoomToFit) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record TaskData(String id, String name, String startDate, String endDate, Double duration,
                    Double percentDone, boolean expanded, boolean manuallyScheduled,
                    List<TaskData> children) {
        static TaskData of(Task t) {
            return new TaskData(t.getId(), t.getName(), str(t.getStartDate()), str(t.getEndDate()),
                    t.getDuration(), t.getPercentDone(), t.isExpanded(), t.isManuallyScheduled(),
                    // Bryntum treats a task with (even empty) children array as a parent
                    t.getChildren().isEmpty() ? null : t.getChildren().stream().map(TaskData::of).toList());
        }

        private static String str(LocalDate date) {
            return date == null ? null : date.toString();
        }
    }

    record DependencyData(String id, String fromTask, String toTask, int type, double lag, String lagUnit) {
        static DependencyData of(Dependency d) {
            return new DependencyData(d.getId(), d.getFrom().getId(), d.getTo().getId(),
                    d.getType().getBryntumValue(), d.getLag(), "day");
        }
    }

    record TaskChange(String id, String name, LocalDate startDate, LocalDate endDate, Double duration,
                      Double percentDone) {
    }

    record TasksChangedPayload(EventPart event) {
    }

    record EventPart(Detail detail) {
    }

    record Detail(List<TaskChange> tasks) {
    }

    record TasksRemovedPayload(RemovedEventPart event) {
    }

    record RemovedEventPart(RemovedDetail detail) {
    }

    record RemovedDetail(List<String> ids) {
    }
}
