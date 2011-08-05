package org.cytoscape.task.internal.quickstart.subnetworkbuilder;

import java.util.Set;

import javax.swing.JOptionPane;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubnetworkBuilderTask extends AbstractTask {

	private static final Logger logger = LoggerFactory.getLogger(SubnetworkBuilderTask.class);

	private static final String USE_EXISTING_NETWORK = "Use existing network as interactome";
	private static final String LOAD_NEW_NETWORK = "Load all known interactions for a model organism";

	@Tunable(description = "Create subnetwork from interactome")
	public ListSingleSelection<String> selection = new ListSingleSelection<String>(LOAD_NEW_NETWORK,
			USE_EXISTING_NETWORK);

	private final CyNetworkManager networkManager;
	private final SubnetworkBuilderUtil util;
	private final SubnetworkBuilderState state;

	public SubnetworkBuilderTask(final CyNetworkManager networkManager, final SubnetworkBuilderUtil util) {
		this.networkManager = networkManager;
		this.util = util;
		this.state = new SubnetworkBuilderState();
	}

	@Override
	public void run(TaskMonitor monitor) throws Exception {

		final String selected = selection.getSelectedValue();

		//insertTasksAfterCurrentTask(new SearchRelatedGenesTask(util, state));

		if (selected == LOAD_NEW_NETWORK) {
			insertTasksAfterCurrentTask(util.getWebServiceImportTask());
		} else if (selected == USE_EXISTING_NETWORK) {
			final Set<CyNetwork> networks = networkManager.getNetworkSet();
			if (networks.size() == 0) {
				// No network is available. Need to load new one.
				JOptionPane.showMessageDialog(null, "No network is available.  Need to load a new one.",
						"No network found", JOptionPane.ERROR_MESSAGE);
				insertTasksAfterCurrentTask(util.getWebServiceImportTask());
			}
		}

	}
}
