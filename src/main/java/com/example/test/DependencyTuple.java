package com.example.test;

import java.util.UUID;

public class DependencyTuple {
	private String id = UUID.randomUUID().toString();
	private String fromTask;
	private String toTask;
	private Integer lag;
	
	public DependencyTuple(GanttRow from, Dependency to) {
		fromTask = from.getId();
		toTask = to.getDependency().getId();
		setLag(to.getLag());
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getFromTask() {
		return fromTask;
	}
	public void setFromTask(String fromTask) {
		this.fromTask = fromTask;
	}
	public String getToTask() {
		return toTask;
	}
	public void setToTask(String toTask) {
		this.toTask = toTask;
	}

	public Integer getLag() {
		return lag;
	}

	public void setLag(Integer lag) {
		this.lag = lag;
	}
	
}
