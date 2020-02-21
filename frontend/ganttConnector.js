import { Gantt, ProjectModel } from './gantt.module.js';

window.Vaadin.Flow.brymtumGanttConnector = {
	    initLazy: function (c, rows, deps) {

	        // Check whether the connector was already initialized 
	        if (c.$connector) {
	            return;
	        }
	        
	        c.$connector =  {};
	        
	        
	    	const project = new ProjectModel({
	    		eventsData : JSON.parse(rows),
	    		dependenciesData : JSON.parse(deps)
	    	});
	    	
	    	c.chart = new Gantt({
	    	    adopt : c,

	    	    project : project,

	    	    columns : [
	    	        { type : 'name', width : 250 },
	    	        { type : 'startdate' },
	    	        { type : 'duration' },
//	    	        { type : 'resourceavatar', width : 120 },
	    	        { type : 'percentdone', showCircle : true, width : 70 },
	    	    ]

	    	});
	    	c.chart.zoomToFit();
	        
	    }
}


//project.load();

