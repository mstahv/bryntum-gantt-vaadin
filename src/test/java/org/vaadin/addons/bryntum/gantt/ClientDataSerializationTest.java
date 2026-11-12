package org.vaadin.addons.bryntum.gantt;

import com.vaadin.flow.internal.JacksonCodec;
import com.vaadin.flow.internal.JacksonUtils;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies the JSON sent to Bryntum, using the same codec Vaadin uses for
 * callJsFunction parameters.
 */
public class ClientDataSerializationTest {

    @Test
    public void tasksAndDependenciesAreSerializedInBryntumFormat() {
        GanttChart chart = new GanttChart();
        Task parent = chart.addTask("Parent");
        Task child = parent.addChild("Child", LocalDate.of(2026, 1, 5), 3);
        Task other = chart.addTask("Other", LocalDate.of(2026, 1, 8), 1);
        chart.addDependency(child, other).setLag(2).setType(Dependency.Type.END_TO_END);

        JsonNode tasks = JacksonCodec.encodeWithTypeInfo(chart.taskData());
        JsonNode p = tasks.get(0);
        assertEquals(parent.getId(), p.get("id").asString());
        assertFalse(p.has("startDate"), "nulls should be left out");
        JsonNode c = p.get("children").get(0);
        assertEquals("2026-01-05", c.get("startDate").asString());
        assertEquals(3.0, c.get("duration").asDouble());
        assertFalse(c.has("children"), "leaf must not have children array");

        JsonNode dep = JacksonCodec.encodeWithTypeInfo(chart.dependencyData()).get(0);
        assertEquals(child.getId(), dep.get("fromTask").asString());
        assertEquals(other.getId(), dep.get("toTask").asString());
        assertEquals(3, dep.get("type").asInt());
        assertEquals(2.0, dep.get("lag").asDouble());

        JsonNode columns = JacksonCodec.encodeWithTypeInfo(chart.getColumns());
        assertEquals("name", columns.get(0).get("type").asString());
        assertFalse(columns.get(0).has("text"));
    }

    @Test
    public void taskChangesFromClientAreDecoded() {
        String json = """
                {"event":{"detail":{"tasks":[{"id":"x","name":"Renamed","startDate":"2026-02-01",
                "endDate":"2026-02-04","duration":3,"percentDone":10.5}]}}}""";
        GanttChart.TasksChangedPayload payload = JacksonUtils.readToObject(
                JacksonUtils.readTree(json), GanttChart.TasksChangedPayload.class);
        List<GanttChart.TaskChange> changes = payload.event().detail().tasks();
        assertEquals(LocalDate.of(2026, 2, 4), changes.getFirst().endDate());
        assertEquals(10.5, changes.getFirst().percentDone());
    }
}
