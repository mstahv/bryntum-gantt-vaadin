package org.vaadin.addons.bryntum.gantt;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;

/**
 * Usage example, also used by browser tests. Edit tasks in the chart and see
 * the changes reported on the server side.
 */
@Route
public class BasicGanttUsageUI extends VerticalLayout {

    public BasicGanttUsageUI() {
        GanttChart gantt = new SaasLaunchChart();
        Paragraph log = new Paragraph("Drag, resize or edit tasks...");
        log.setId("change-log");
        gantt.addTasksChangedListener(e -> log.setText("Changed: " + e.getTasks().stream()
                .map(t -> t.getName() + " " + t.getStartDate() + " - " + t.getEndDate())
                .toList()));
        Paragraph removeLog = new Paragraph();
        removeLog.setId("remove-log");
        gantt.addTasksRemovedListener(e -> removeLog.setText("Removed: " + e.getTasks().stream()
                .map(Task::getName).toList() + ", dependencies left: " + gantt.getDependencies().size()));

        add(new H1("Bryntum Gantt for Vaadin"), log, removeLog);
        addAndExpand(gantt);
        setHeightFull();
    }

    static class SaasLaunchChart extends GanttChart {
        SaasLaunchChart() {
            LocalDate today = LocalDate.now();
            Task launch = addTask("Launch SaaS Product");
            launch.setId("launch");

            Task webServer = launch.addChild("Setup web server");
            Task apache = webServer.addChild("Install Apache", today, 3);
            apache.setId("apache");
            apache.setPercentDone(50);
            Task firewall = webServer.addChild("Configure firewall", today, 3);
            firewall.setPercentDone(50);
            Task loadBalancer = webServer.addChild("Setup load balancer", today, 3);
            Task ports = webServer.addChild("Configure ports", today.plusDays(2), 2);
            Task tests = webServer.addChild("Run tests", today.plusDays(5), 2);

            Task website = launch.addChild("Website");
            Task design = website.addChild("Design", today, 5);
            Task implement = website.addChild("Implementation", today, 7);
            Task release = launch.addChild("Release", today, 0);

            addDependency(apache, ports).setLag(1);
            addDependency(firewall, tests);
            addDependency(loadBalancer, tests);
            addDependency(ports, tests);
            addDependency(design, implement);
            addDependency(tests, release);
            addDependency(implement, release);
        }
    }
}
