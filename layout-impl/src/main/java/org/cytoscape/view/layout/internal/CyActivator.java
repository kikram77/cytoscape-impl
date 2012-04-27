

package org.cytoscape.view.layout.internal;

import java.util.Properties;

import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.layout.internal.algorithms.GridNodeLayout;
import org.osgi.framework.BundleContext;

import static org.cytoscape.work.ServiceProperties.*;


public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {

		CyProperty cyPropertyServiceRef = getService(bc,CyProperty.class,"(cyPropertyName=cytoscape3.props)");
		
		CyLayoutsImpl cyLayouts = new CyLayoutsImpl(cyPropertyServiceRef);
		GridNodeLayout gridNodeLayout = new GridNodeLayout();
		
		registerService(bc,cyLayouts,CyLayoutAlgorithmManager.class, new Properties());

		Properties gridNodeLayoutProps = new Properties();
		gridNodeLayoutProps.setProperty(PREFERRED_MENU,"Layout.Cytoscape Layouts");
		gridNodeLayoutProps.setProperty("preferredTaskManager","menu");
		gridNodeLayoutProps.setProperty(TITLE,gridNodeLayout.toString());
		registerService(bc,gridNodeLayout,CyLayoutAlgorithm.class, gridNodeLayoutProps);

		registerServiceListener(bc,cyLayouts,"addLayout","removeLayout",CyLayoutAlgorithm.class);
	}
}

