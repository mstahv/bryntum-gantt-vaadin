package org.vaadin.addons.bryntum.gantt.it;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import in.virit.mopo.Mopo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.vaadin.addons.bryntum.gantt.BasicGanttUsageUI;
import org.vaadin.addons.bryntum.gantt.DynamicGanttUI;

import java.util.List;


import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Browser tests. Run with "mvn verify". Pass -Dheadful to see the browser.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GanttChartIT {

    @Value("${local.server.port}")
    private int port;

    static Playwright playwright = Playwright.create();

    private Browser browser;
    private Page page;
    private Mopo mopo;

    @BeforeEach
    public void setup() {
        boolean headful = System.getProperty("headful") != null;
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(!headful)
                .setArgs(headful ? List.of("--auto-open-devtools-for-tabs") : List.of()));
        page = browser.newPage();
        page.setDefaultTimeout(30000); // first load may need to wait for Vite
        mopo = new Mopo(page);
    }

    @AfterEach
    public void closeBrowser() {
        page.close();
        browser.close();
    }

    @AfterAll
    public static void closePlaywright() {
        playwright.close();
    }

    private String url(Class<?> view) {
        return "http://localhost:" + port + "/" + view.getSimpleName().toLowerCase();
    }

    @Test
    public void smokeTestAllTestUIs() {
        String rootUrl = "http://localhost:" + port + "/";
        mopo.getViewsReportedByDevMode(browser, rootUrl).forEach(viewName -> {
            page.navigate(rootUrl + viewName);
            page.locator(".b-gantt").first().waitFor();
            mopo.assertNoJsErrors();
        });
    }

    @Test
    public void rendersTasksAndDependencies() {
        page.navigate(url(BasicGanttUsageUI.class));
        Locator gantt = page.locator("vaadin-bryntum-gantt");
        assertThat(gantt.locator(".b-grid-cell", new Locator.LocatorOptions().setHasText("Install Apache")))
                .isVisible();
        assertThat(gantt.locator(".b-gantt-task").first()).isVisible();
        assertThat(gantt.locator(".b-sch-dependency").first()).isAttached();
        mopo.assertNoJsErrors();
    }

    @Test
    public void clientSideChangesAreSynchronizedToServer() {
        page.navigate(url(BasicGanttUsageUI.class));
        page.locator(".b-gantt-task").first().waitFor();

        // Simulate a user edit through the Bryntum API (same path as drag/resize/cell edit)
        page.evaluate("""
                () => {
                    const gantt = document.querySelector('vaadin-bryntum-gantt').gantt;
                    gantt.taskStore.getById('apache').name = 'Install Nginx';
                }""");

        assertThat(page.locator("#change-log")).containsText("Install Nginx");
        mopo.assertNoJsErrors();
    }

    @Test
    public void clientSideRemovalsAreSynchronizedToServer() {
        page.navigate(url(BasicGanttUsageUI.class));
        page.locator(".b-gantt-task").first().waitFor();

        page.evaluate("() => document.querySelector('vaadin-bryntum-gantt').gantt.taskStore.getById('apache').remove()");

        // apache -> ports dependency removed as well
        // and the successor rescheduled by the engine
        assertThat(page.locator("#change-log")).containsText("Configure ports");
        assertThat(page.locator("#remove-log")).hasText("Removed: [Install Apache], dependencies left: 6");
        mopo.assertNoJsErrors();
    }

    @Test
    public void serverSideModifications() {
        page.navigate(url(DynamicGanttUI.class));
        Locator gantt = page.locator("vaadin-bryntum-gantt");
        assertThat(gantt.locator(".b-grid-cell", new Locator.LocatorOptions().setHasText("Initial task")))
                .isVisible();

        page.locator("vaadin-button", new Page.LocatorOptions().setHasText("Add task")).click();
        page.locator("vaadin-button", new Page.LocatorOptions().setHasText("Add task")).click();
        assertThat(gantt.locator(".b-grid-cell", new Locator.LocatorOptions().setHasText("Task 2")))
                .isVisible();

        page.locator("vaadin-button", new Page.LocatorOptions().setHasText("Remove last")).click();
        assertThat(gantt.locator(".b-grid-cell", new Locator.LocatorOptions().setHasText("Task 2")))
                .hasCount(0);
        assertThat(gantt.locator(".b-grid-cell", new Locator.LocatorOptions().setHasText("Task 1")))
                .isVisible();

        page.locator("vaadin-button", new Page.LocatorOptions().setHasText("Minimal columns")).click();
        assertThat(gantt.locator(".b-grid-header", new Locator.LocatorOptions().setHasText("WBS")))
                .isVisible();
        // data survives re-creating the chart
        assertThat(gantt.locator(".b-grid-cell", new Locator.LocatorOptions().setHasText("Task 1")))
                .isVisible();
        mopo.assertNoJsErrors();
    }
}
