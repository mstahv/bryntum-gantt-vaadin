package org.vaadin.addons.bryntum.gantt.unit;

import org.junit.jupiter.api.Test;
import org.vaadin.addons.bryntum.gantt.Dependency;
import org.vaadin.addons.bryntum.gantt.GanttChart;
import org.vaadin.addons.bryntum.gantt.Task;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class GanttChartTest {

    @Test
    public void taskHierarchy() {
        Task parent = new Task("Parent");
        Task child = parent.addChild("Child", LocalDate.of(2026, 1, 5), 3);
        assertSame(parent, child.getParent());
        assertEquals(3.0, child.getDuration());

        Task other = new Task("Other");
        other.addChild(child);
        assertTrue(parent.getChildren().isEmpty());
        assertSame(other, child.getParent());
    }

    @Test
    public void removingTaskRemovesItsDependencies() {
        GanttChart chart = new GanttChart();
        Task a = chart.addTask("A", LocalDate.of(2026, 1, 5), 1);
        Task b = chart.addTask("B");
        Task b1 = b.addChild("B1", LocalDate.of(2026, 1, 5), 1);
        Task c = chart.addTask("C", LocalDate.of(2026, 1, 5), 1);
        chart.addDependency(a, b1);
        Dependency ac = chart.addDependency(a, c).setType(Dependency.Type.START_TO_START);

        assertEquals(b1, chart.findTask(b1.getId()).orElseThrow());

        chart.removeTask(b);
        assertEquals(2, chart.getTasks().size());
        assertEquals(1, chart.getDependencies().size());
        assertSame(ac, chart.getDependencies().getFirst());
        assertTrue(chart.findTask(b1.getId()).isEmpty());
    }
}
