
/*
  File: HideUtils.java

  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)

  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies

  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.

  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/

package org.cytoscape.task.internal.hide;


import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_VISIBLE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_VISIBLE;

import java.util.Collection;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;


abstract class HideUtils {

	static void setVisibleNodes(Collection<CyNode> nodes, boolean visible, CyNetworkView view) {
		final CyNetwork net = view.getModel();
		
		for (CyNode n : nodes) {
			if (visible)
				view.getNodeView(n).clearValueLock(NODE_VISIBLE);
			else
				view.getNodeView(n).setLockedValue(NODE_VISIBLE, false);

			for (CyNode n2 : net.getNeighborList(n, CyEdge.Type.ANY)) {
				for (CyEdge e : net.getConnectingEdgeList(n, n2, CyEdge.Type.ANY)) {
					final View<CyEdge> ev = view.getEdgeView(e);
					
					if (visible)
						ev.clearValueLock(EDGE_VISIBLE);
					else
						ev.setLockedValue(EDGE_VISIBLE, false);
				}
			}
		}
	}

	static void setVisibleEdges(Collection<CyEdge> edges, boolean visible, CyNetworkView view) {
		for (CyEdge e : edges) {
			final View<CyEdge> ev = view.getEdgeView(e);
					
			if (visible)
				ev.clearValueLock(EDGE_VISIBLE);
			else
				ev.setLockedValue(EDGE_VISIBLE, false);
		}
	}
}
