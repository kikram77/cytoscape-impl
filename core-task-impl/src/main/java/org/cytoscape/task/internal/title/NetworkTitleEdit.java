package org.cytoscape.task.internal.title;


import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.util.swing.CyAbstractEdit;


/** An undoable edit that will undo and redo renaming of a network. */ 
final class NetworkTitleEdit extends CyAbstractEdit {
	private final CyNetwork network;
	private String previousTitle;

	NetworkTitleEdit(final CyNetwork network, final String previousTitle) {
		super("Rename Title");

		this.network = network;
		this.previousTitle = previousTitle;
	}

	public void redo() {
		super.redo();

		final String savedTitle = network.getCyRow().get(CyTableEntry.NAME, String.class);
		network.getCyRow().set(CyTableEntry.NAME, previousTitle);
		previousTitle = savedTitle;
	}

	public void undo() {
		super.undo();

		final String savedTitle = network.getCyRow().get(CyTableEntry.NAME, String.class);
		network.getCyRow().set(CyTableEntry.NAME, previousTitle);
		previousTitle = savedTitle;
	}
}
