package com.example.test;

public class Dependency {
	
	private GanttRow dependency;
	private Integer lag;
	
	public Dependency(GanttRow dependency) {
		this.dependency = dependency;
	}
	
	public GanttRow getDependency() {
		return dependency;
	}
	public void setDependency(GanttRow dependency) {
		this.dependency = dependency;
	}
	public Integer getLag() {
		return lag;
	}
	public void setLag(Integer lag) {
		this.lag = lag;
	}

}
