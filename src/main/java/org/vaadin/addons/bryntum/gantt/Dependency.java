package org.vaadin.addons.bryntum.gantt;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * A dependency (link) between two tasks in a {@link GanttChart}. Create
 * instances with {@link GanttChart#addDependency(Task, Task)}.
 */
public class Dependency implements Serializable {

    /**
     * The type of the dependency, defines which ends of the tasks are linked.
     */
    public enum Type {
        /** The successor can start once the predecessor has started. */
        START_TO_START(0),
        /** The successor can end once the predecessor has started. */
        START_TO_END(1),
        /** The successor can start once the predecessor has ended (the default). */
        END_TO_START(2),
        /** The successor can end once the predecessor has ended. */
        END_TO_END(3);

        private final int bryntumValue;

        Type(int bryntumValue) {
            this.bryntumValue = bryntumValue;
        }

        int getBryntumValue() {
            return bryntumValue;
        }
    }

    private final String id = UUID.randomUUID().toString();
    private final Task from;
    private final Task to;
    private Type type = Type.END_TO_START;
    private double lag;

    /**
     * @param from the predecessor task
     * @param to   the successor task
     */
    public Dependency(Task from, Task to) {
        this.from = Objects.requireNonNull(from);
        this.to = Objects.requireNonNull(to);
    }

    public String getId() {
        return id;
    }

    /**
     * @return the predecessor task
     */
    public Task getFrom() {
        return from;
    }

    /**
     * @return the successor task
     */
    public Task getTo() {
        return to;
    }

    public Type getType() {
        return type;
    }

    public Dependency setType(Type type) {
        this.type = Objects.requireNonNull(type);
        return this;
    }

    /**
     * @return the lag in days
     */
    public double getLag() {
        return lag;
    }

    /**
     * @param lagInDays the lag (or lead if negative) in days
     * @return this, for chaining
     */
    public Dependency setLag(double lagInDays) {
        this.lag = lagInDays;
        return this;
    }
}
