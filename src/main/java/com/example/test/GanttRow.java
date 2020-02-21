package com.example.test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GanttRow {
//    "id": 1000,
//    "name": "Launch SaaS Product",
//    "percentDone": 50,
//    "startDate": "2019-01-14",
//    "expanded": true,
//    "children": []
	
	private String id = UUID.randomUUID().toString();
	private String name;
	private LocalDate startDate;
	private LocalDate endDate;
	private Integer duration;
	private boolean expanded = true;
	private List<GanttRow> children = new LinkedList<>();
	@JsonIgnore
	private List<Dependency> dependencies = new LinkedList<>();
	private Integer percentDone;
	
	public GanttRow() {
	}
	
	public GanttRow(String name) {
		setName(name);
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public LocalDate getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = LocalDate.parse(startDate);
	}
	public void setStartDate(LocalDate date) {
		this.startDate = date;
	}
	
	public boolean isExpanded() {
		return expanded;
	}
	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}
	public List<GanttRow> getChildren() {
		return children;
	}
	public void setChildren(List<GanttRow> children) {
		this.children = children;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
		if(startDate  != null) {
			endDate = startDate.plusDays(duration);
		}
	}

	public void setPercentDone(int percentDone) {
		this.percentDone = percentDone;
	}
	
	public Integer getPercentDone() {
		return percentDone;
	}

	public void addDependency(GanttRow dependency) {
		dependencies.add(new Dependency(dependency));
	}
	
	public List<Dependency> getDependencies() {
		return dependencies;
	}

	public void setEndDate(String string) {
		setEndDate(LocalDate.parse(string));
	}
	
}
