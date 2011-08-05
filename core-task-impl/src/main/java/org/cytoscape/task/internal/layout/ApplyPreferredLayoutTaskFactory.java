package org.cytoscape.task.internal.layout;


import org.cytoscape.event.CyEventHelper;
import org.cytoscape.property.CyProperty;
import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;

import java.util.Properties; 


public class ApplyPreferredLayoutTaskFactory extends AbstractNetworkViewTaskFactory {
	private final UndoSupport undoSupport;
	private final CyEventHelper eventHelper;
	private final CyLayoutAlgorithmManager layouts;
	private final Properties props;
	
	public ApplyPreferredLayoutTaskFactory(final UndoSupport undoSupport,
	                                       final CyEventHelper eventHelper,
	                                       final CyLayoutAlgorithmManager layouts,
	                                       final CyProperty<Properties> p)
	{
		this.undoSupport = undoSupport;
		this.eventHelper = eventHelper;
		this.layouts     = layouts;
		this.props       = p.getProperties();
	}

	public TaskIterator getTaskIterator() {
		return new TaskIterator(new ApplyPreferredLayoutTask(undoSupport, eventHelper, view,
		                                                     layouts, props));
	}
}
