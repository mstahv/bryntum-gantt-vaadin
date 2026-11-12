import { Gantt } from '@bryntum/gantt-community';
import { DateHelper } from '@bryntum/core-community';
// Register columns and features of the underlying products (e.g. "percent" column)
import '@bryntum/grid-community';
import '@bryntum/scheduler-community';

import '@bryntum/core-community/fontawesome/css/fontawesome.css';
import '@bryntum/core-community/fontawesome/css/solid.css';
import '@bryntum/gantt-community/gantt.css';
import '@bryntum/gantt-community/svalbard.css';

/** Task fields synchronized back to the server when the user edits the chart. */
const formatDate = date => date ? DateHelper.format(date, 'YYYY-MM-DD') : null;

const toServerTask = task => ({
    id : String(task.id),
    name : task.name,
    startDate : formatDate(task.startDate),
    endDate : formatDate(task.endDate),
    duration : task.duration,
    percentDone : task.percentDone
});

/**
 * Thin custom element wrapping Bryntum Gantt for the Vaadin GanttChart Java
 * component. The Java side owns the data and pushes it with init()/setData(),
 * edits made in the browser are reported back with "tasks-changed" and
 * "tasks-removed" events.
 */
class VaadinBryntumGantt extends HTMLElement {

    constructor() {
        super();
        this._tasks = [];
        this._dependencies = [];
        this._config = {};
        this._pendingChanges = new Set();
    }

    connectedCallback() {
        this.style.display = 'flex';
        this.style.flexDirection = 'column';
        if (this._initialized && !this.gantt) {
            this._createGantt();
        }
    }

    disconnectedCallback() {
        // Vaadin may detach and re-attach the same element (e.g. moving it in
        // the layout), so keep the current state and re-create on connect.
        if (this.gantt) {
            this._tasks = this.gantt.taskStore.toJSON();
            this._dependencies = this.gantt.dependencyStore.toJSON();
            this._destroyGantt();
        }
    }

    _destroyGantt() {
        this._listening = false;
        this._pendingChanges.clear();
        this.gantt?.destroy();
        this.gantt = null;
    }

    /**
     * (Re)creates the Gantt with given configuration and data.
     * Called from the server side.
     */
    init(config, tasks, dependencies) {
        this._config = config || {};
        this._tasks = tasks || [];
        this._dependencies = dependencies || [];
        this._initialized = true;
        this._destroyGantt();
        if (this.isConnected) {
            this._createGantt();
        }
    }

    /** Replaces all tasks and dependencies. Called from the server side. */
    async setData(tasks, dependencies) {
        this._tasks = tasks || [];
        this._dependencies = dependencies || [];
        if (this.gantt) {
            this._listening = false;
            await this.gantt.project.loadInlineData({
                tasksData : this._tasks,
                dependenciesData : this._dependencies
            });
            await this._afterLoad();
        }
    }

    zoomToFit() {
        this.gantt?.zoomToFit();
    }

    scrollToTask(id) {
        const task = this.gantt?.taskStore.getById(id);
        if (task) {
            this.gantt.scrollTaskIntoView(task, { highlight : true, animate : true });
        }
    }

    async _createGantt() {
        const c = this._config;
        // The server side model is the source of truth. Edits to existing tasks
        // and removals are synchronized, structural changes (adding, linking,
        // re-parenting) are only supported via the Java API for now.
        const features = {
            taskMenu : {
                items : {
                    add : false,
                    convertToMilestone : false,
                    linkTasks : false,
                    unlinkTasks : false
                }
            },
            dependencies : { allowCreate : false },
            taskDragCreate : false,
            rowReorder : false
        };
        const gantt = this.gantt = new Gantt({
            appendTo : this,
            flex : '1 1 0',
            readOnly : Boolean(c.readOnly),
            columns : c.columns,
            features,
            project : {
                tasks : this._tasks,
                dependencies : this._dependencies
            }
        });

        gantt.taskStore.on({
            update : ({ record }) => this._queueChange(record),
            remove : ({ records, isMove, isCollapse }) => this._onRemove(records, isMove || isCollapse),
            thisObj : this
        });
        await this._afterLoad();
    }

    async _afterLoad() {
        const gantt = this.gantt;
        if (!gantt) {
            return;
        }
        // Wait until the scheduling engine has calculated the initial values,
        // updates before that are not user changes.
        await gantt.project.commitAsync();
        if (gantt !== this.gantt) {
            return;
        }
        this._pendingChanges.clear();
        this._listening = true;
        if (this._config.zoomToFit !== false) {
            // zooming needs the final size of the time axis
            requestAnimationFrame(() => this.gantt === gantt && gantt.zoomToFit({ leftMargin : 20, rightMargin : 40 }));
        }
    }

    _onRemove(records, ignore) {
        if (!this._listening || ignore) {
            return;
        }
        const ids = records.filter(r => !r.isPhantom).map(r => String(r.id));
        if (ids.length) {
            this.dispatchEvent(new CustomEvent('tasks-removed', { detail : { ids } }));
        }
    }

    _queueChange(record) {
        if (!this._listening || record.isRoot || record.isPhantom || record.isDestroyed) {
            return;
        }
        this._pendingChanges.add(record);
        if (!this._flushScheduled) {
            this._flushScheduled = true;
            // A single user gesture may update many tasks (parents, successors),
            // let the engine settle and send all of them in one event.
            this.gantt.project.commitAsync().finally(() => {
                this._flushScheduled = false;
                const tasks = [...this._pendingChanges].filter(t => !t.isDestroyed).map(toServerTask);
                this._pendingChanges.clear();
                if (tasks.length) {
                    this.dispatchEvent(new CustomEvent('tasks-changed', { detail : { tasks } }));
                }
            });
        }
    }
}

customElements.define('vaadin-bryntum-gantt', VaadinBryntumGantt);
