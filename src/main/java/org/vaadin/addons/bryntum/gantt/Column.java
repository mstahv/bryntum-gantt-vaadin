package org.vaadin.addons.bryntum.gantt;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

/**
 * A column definition for the grid part (left side) of the {@link GanttChart}.
 * The {@code type} refers to a Bryntum column type, e.g. "name", "startdate",
 * "enddate", "duration", "wbs", "sequence", "milestone" or "note". See Bryntum
 * Gantt documentation for all available column types and their options.
 *
 * @param type  the Bryntum column type
 * @param field the task field shown in the column, or null to use the default
 *              of the column type
 * @param text  the header text, or null to use the default of the column type
 * @param width the width in pixels, or null to use the default
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Column(String type, String field, String text, Integer width) implements Serializable {

    public static Column of(String type) {
        return new Column(type, null, null, null);
    }

    public static Column name() {
        return of("name").withWidth(250);
    }

    public static Column wbs() {
        return of("wbs");
    }

    public static Column startDate() {
        return of("startdate");
    }

    public static Column endDate() {
        return of("enddate");
    }

    public static Column duration() {
        return of("duration");
    }

    /**
     * @return a column showing the completion percentage of tasks as a bar
     */
    public static Column percentDone() {
        return new Column("percent", "percentDone", "% Done", 100);
    }

    public Column withText(String text) {
        return new Column(type, field, text, width);
    }

    public Column withWidth(Integer width) {
        return new Column(type, field, text, width);
    }

    public Column withField(String field) {
        return new Column(type, field, text, width);
    }
}
