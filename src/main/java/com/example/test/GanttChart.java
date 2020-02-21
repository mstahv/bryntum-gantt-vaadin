package com.example.test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.function.SerializableConsumer;

@JsModule(value = "./gantt.module.js")
@JavaScript("./ganttConnector.js")
@CssImport("./gantt.default.css")
public class GanttChart extends Div {
	
	private static ObjectMapper mapper = new ObjectMapper(); 
	static {
		mapper.registerModule(new JavaTimeModule());
	    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}

	private List<GanttRow> rows = new ArrayList<>();


	public GanttChart() {
		setId(UUID.randomUUID().toString());
	}

	public GanttRow addRow(String string) {
		GanttRow ganttRow = new GanttRow(string);
		rows.add(ganttRow);
		return ganttRow;
	}
	
    void runBeforeClientResponse(SerializableConsumer<UI> command) {
        getElement().getNode().runWhenAttached(ui -> ui
                .beforeClientResponse(this, context -> command.accept(ui)));
    }
    
    private List<DependencyTuple> calculateDeps() {
    	LinkedList<DependencyTuple> deps = new LinkedList<>();
    	for (GanttRow row : rows) {
    		collectDeps(deps, row);
		}
    	return deps;
    }

    private void collectDeps(LinkedList<DependencyTuple> deps, GanttRow row) {
    	row.getDependencies().forEach(dep -> deps.add(new DependencyTuple(row, dep)));;
    	row.getChildren().forEach(c -> collectDeps(deps, c));
	}

	@Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        initConnector();
    }

    private void initConnector() {
        runBeforeClientResponse(ui -> {
			try {
				ui.getPage().executeJs(
				        "window.Vaadin.Flow.brymtumGanttConnector.initLazy($0, $1, $2)",
				        getElement(), mapper.writeValueAsString(rows), mapper.writeValueAsString(calculateDeps()));
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		});
    }

}
