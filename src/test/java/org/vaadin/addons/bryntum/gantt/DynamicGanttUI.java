package org.vaadin.addons.bryntum.gantt;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;

/**
 * Exercises the server side API: adding/removing tasks, columns and read only
 * mode after the chart has been rendered.
 */
@Route
public class DynamicGanttUI extends VerticalLayout {

    private final GanttChart gantt = new GanttChart();
    private int counter;

    public DynamicGanttUI() {
        gantt.addTask("Initial task", LocalDate.now(), 2);
        add(new Toolbar());
        addAndExpand(gantt);
        setHeightFull();
    }

    class Toolbar extends HorizontalLayout {
        Toolbar() {
            add(new Button("Add task", e -> {
                Task task = gantt.addTask("Task " + ++counter, LocalDate.now().plusDays(counter), 2);
                gantt.scrollToTask(task);
            }));
            add(new Button("Remove last", e -> {
                var tasks = gantt.getTasks();
                if (!tasks.isEmpty()) {
                    gantt.removeTask(tasks.getLast());
                }
            }));
            add(new Button("Minimal columns", e -> gantt.setColumns(Column.wbs(), Column.name())));
            add(new Button("Zoom to fit", e -> gantt.zoomToFit()));
            add(new Checkbox("Read only", e -> gantt.setReadOnly(e.getValue())));
        }
    }
}
