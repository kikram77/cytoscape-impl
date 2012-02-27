/*
 File: CytoscapeDesktop.java

 Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.internal.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Dictionary;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.cytoscape.application.CyShutdown;
import org.cytoscape.application.events.CyStartEvent;
import org.cytoscape.application.events.CyStartListener;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.application.swing.ToolBarComponent;
import org.cytoscape.application.swing.events.CytoPanelStateChangedListener;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.session.events.SessionSavedEvent;
import org.cytoscape.session.events.SessionSavedListener;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The CytoscapeDesktop is the central Window for working with Cytoscape
 */
public class CytoscapeDesktop extends JFrame implements CySwingApplication, CyStartListener, SessionLoadedListener, SessionSavedListener {

	private final static long serialVersionUID = 1202339866271348L;
	
	private static final String TITLE_PREFIX_STRING ="Session: ";
	
	private static final Dimension DEF_DESKTOP_SIZE = new Dimension(1200, 850);
	private static final int DEF_DIVIDER_LOATION = 450;
	
	private static final String SMALL_ICON = "/images/c16.png";
	private static final int DEVIDER_SIZE = 4;
	
	private static final Logger logger = LoggerFactory.getLogger(CytoscapeDesktop.class);
	
	/**
	 * The network panel that sends out events when a network is selected from
	 * the Tree that it contains.
	 */
	protected NetworkPanel networkPanel;

	/**
	 * The CyMenus object provides access to the all of the menus and toolbars
	 * that will be needed.
	 */
	protected CytoscapeMenus cyMenus;

	/**
	 * The NetworkViewManager can support three types of interfaces.
	 * Tabbed/InternalFrame/ExternalFrame
	 */
	protected NetworkViewManager networkViewManager;
	

	//
	// CytoPanel Variables
	//
	private CytoPanelImp cytoPanelWest;
	private CytoPanelImp cytoPanelEast;
	private CytoPanelImp cytoPanelSouth;
	private CytoPanelImp cytoPanelSouthWest; 

	// Status Bar TODO: Move this to log-swing to avoid cyclic dependency.
	private JPanel main_panel;
	private final CyShutdown shutdown; 
	private final CyEventHelper cyEventHelper;
	private final CyServiceRegistrar registrar;
	private final JToolBar statusToolBar;

	/**
	 * Creates a new CytoscapeDesktop object.
	 */
	public CytoscapeDesktop(CytoscapeMenus cyMenus, NetworkViewManager networkViewManager, NetworkPanel networkPanel,
			CyShutdown shut, CyEventHelper eh, CyServiceRegistrar registrar, DialogTaskManager taskManager) {
		super(TITLE_PREFIX_STRING + "New Session");

		this.cyMenus = cyMenus;
		this.networkViewManager = networkViewManager;
		this.networkPanel = networkPanel;
		this.shutdown = shut;
		this.cyEventHelper = eh;
		this.registrar = registrar;
		
		taskManager.setExecutionContext(this);

		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource(SMALL_ICON)));

		main_panel = new JPanel();
		main_panel.setLayout(new BorderLayout());

		// create the CytoscapeDesktop
		final BiModalJSplitPane masterPane = setupCytoPanels(networkPanel, networkViewManager);

		main_panel.add(masterPane, BorderLayout.CENTER);
		main_panel.add(cyMenus.getJToolBar(), BorderLayout.NORTH);

		statusToolBar = new JToolBar();
		main_panel.add(statusToolBar, BorderLayout.SOUTH);

		setJMenuBar(cyMenus.getJMenuBar());

		// update look and feel
		try {
			String laf = null;
			if (System.getProperty("os.name").startsWith("Mac OS X") ||
			    System.getProperty("os.name").startsWith("Windows"))
				laf = UIManager.getSystemLookAndFeelClassName();
			else {
				// Use Nimbus on Unix systems
				laf = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
			}

			logger.debug("setting look and feel to: " + laf);
			UIManager.setLookAndFeel(laf);
			SwingUtilities.updateComponentTreeUI(this);
		} catch (Exception e) { /* not really a problem if this fails */ }

		//don't automatically close window. Let shutdown.exit(returnVal)
		//handle this
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent we) {
					shutdown.exit(0);
				}
			});

		// Prepare to show the desktop...
		setContentPane(main_panel);
		pack();
		setSize(DEF_DESKTOP_SIZE);
		
		// Defines default divider location for the network panel to JDesktop
		masterPane.setDividerLocation(400);
		
		// Move it to the center
		this.setLocationRelativeTo(null);

		// ...but don't actually show it!!!!
		// Once the system has fully started the JFrame will be set to 
		// visible by the StartupMostlyFinished class, found elsewhere.
	}

	/**
	 * Create the CytoPanels UI.
	 *
	 * @param networkPanel
	 *            to load on left side of right bimodal.
	 * @param networkViewManager
	 *            to load on left side (CytoPanel West).
	 * @return BiModalJSplitPane Object.
	 */
	private BiModalJSplitPane setupCytoPanels(NetworkPanel networkPanel,
	                                            NetworkViewManager networkViewManager) {
		// bimodals that our Cytopanels Live within
		final BiModalJSplitPane topRightPane = createTopRightPane(networkViewManager);
		final BiModalJSplitPane rightPane = createRightPane(topRightPane);
		final BiModalJSplitPane masterPane = createMasterPane(networkPanel, rightPane);
		createBottomLeft();

		return masterPane;
	}

	/**
	 * Creates the TopRight Pane.
	 *
	 * @param networkViewManager
	 *            to load on left side of top right bimodal.
	 * @return BiModalJSplitPane Object.
	 */
	private BiModalJSplitPane createTopRightPane(NetworkViewManager networkViewManager) {
		// create cytopanel with tabs along the top
		cytoPanelEast = new CytoPanelImp(CytoPanelName.EAST, JTabbedPane.TOP, CytoPanelState.HIDE, cyEventHelper);

		// determine proper network view manager component
		Component networkViewComp = (Component) networkViewManager.getDesktopPane();

		// create the split pane - we show this on startup
		BiModalJSplitPane splitPane = new BiModalJSplitPane(this, JSplitPane.HORIZONTAL_SPLIT,
		                                                    BiModalJSplitPane.MODE_HIDE_SPLIT,
		                                                    networkViewComp, cytoPanelEast);

		// set the cytopanelcontainer
		cytoPanelEast.setCytoPanelContainer(splitPane);

		// set the resize weight - left component gets extra space
		splitPane.setResizeWeight(1.0);

		// outta here
		return splitPane;
	}

	/**
	 * Creates the Right Panel.
	 *
	 * @param topRightPane
	 *            TopRightPane Object.
	 * @return BiModalJSplitPane Object
	 */
	private BiModalJSplitPane createRightPane(BiModalJSplitPane topRightPane) {
		// create cytopanel with tabs along the bottom
		cytoPanelSouth = new CytoPanelImp(CytoPanelName.SOUTH, JTabbedPane.BOTTOM,
		                                  CytoPanelState.DOCK, cyEventHelper);

		// create the split pane - hidden by default
		BiModalJSplitPane splitPane = new BiModalJSplitPane(this, JSplitPane.VERTICAL_SPLIT,
		                                                    BiModalJSplitPane.MODE_HIDE_SPLIT,
		                                                    topRightPane, cytoPanelSouth);

		// set the cytopanel container
		cytoPanelSouth.setCytoPanelContainer(splitPane);

		splitPane.setDividerSize(DEVIDER_SIZE);
		splitPane.setDividerLocation(DEF_DIVIDER_LOATION);

		// set resize weight - top component gets all the extra space.
		splitPane.setResizeWeight(1.0);

		// outta here
		return splitPane;
	}

	private void createBottomLeft() {

		// create cytopanel with tabs along the top for manual layout
		cytoPanelSouthWest = new CytoPanelImp(CytoPanelName.SOUTH_WEST,
						      JTabbedPane.TOP,
						      CytoPanelState.HIDE, cyEventHelper);

        final BiModalJSplitPane split = new BiModalJSplitPane(this, JSplitPane.VERTICAL_SPLIT,
                                      BiModalJSplitPane.MODE_HIDE_SPLIT, new JPanel(),
                                      cytoPanelSouthWest);
        split.setResizeWeight(0);
        cytoPanelSouthWest.setCytoPanelContainer(split);
        cytoPanelSouthWest.setMinimumSize(new Dimension(180, 330));
        cytoPanelSouthWest.setMaximumSize(new Dimension(180, 330));
        cytoPanelSouthWest.setPreferredSize(new Dimension(180, 330));

        split.setDividerSize(DEVIDER_SIZE);

		ToolCytoPanelListener t = new ToolCytoPanelListener( split, cytoPanelWest, 
		                                                     cytoPanelSouthWest );
		registrar.registerService(t,CytoPanelStateChangedListener.class,new Properties());
	}

	/**
	 * Creates the Master Split Pane.
	 *
	 * @param networkPanel
	 *            to load on left side of CytoPanel (cytoPanelWest).
	 * @param rightPane
	 *            BiModalJSplitPane Object.
	 * @return BiModalJSplitPane Object.
	 */
	private BiModalJSplitPane createMasterPane(NetworkPanel networkPanel,
	                                             BiModalJSplitPane rightPane) {
		// create cytopanel with tabs along the top
		cytoPanelWest = new CytoPanelImp(CytoPanelName.WEST, JTabbedPane.TOP, CytoPanelState.DOCK, cyEventHelper);

		// add the network panel to our tab
		String tab1Name = new String("Network");
		cytoPanelWest.add(tab1Name, new ImageIcon(getClass().getResource("/images/class_hi.gif")),
		                  networkPanel, "Cytoscape Network List");

		// create the split pane - hidden by default
		BiModalJSplitPane splitPane = new BiModalJSplitPane(this, JSplitPane.HORIZONTAL_SPLIT,
		                                                    BiModalJSplitPane.MODE_SHOW_SPLIT,
		                                                    cytoPanelWest, rightPane);

		splitPane.setDividerSize(DEVIDER_SIZE);

		// set the cytopanel container
		cytoPanelWest.setCytoPanelContainer(splitPane);

		// outta here
		return splitPane;
	}

	NetworkViewManager getNetworkViewManager() {
		return networkViewManager;
	}

	public void addAction(CyAction action, Dictionary props) {
		cyMenus.addAction(action);
	}

	public void addAction(CyAction action) {
		cyMenus.addAction(action);
	}

	public void removeAction(CyAction action, Dictionary props) {
		cyMenus.removeAction(action);
	}

	public void removeAction(CyAction action) {
		cyMenus.removeAction(action);
	}

	public JMenu getJMenu(String name) {
		return cyMenus.getJMenu(name);
	}

	public JMenuBar getJMenuBar() {
		return cyMenus.getJMenuBar();
	}

	public JToolBar getJToolBar() {
		return cyMenus.getJToolBar();
	}

	public JFrame getJFrame() {
		return this;
	}

	public CytoPanel getCytoPanel(final CytoPanelName compassDirection) {
		return getCytoPanelInternal(compassDirection);
	}

	private CytoPanelImp getCytoPanelInternal(final CytoPanelName compassDirection) {
		// return appropriate cytoPanel based on compass direction
		switch (compassDirection) {
		case SOUTH:
			return cytoPanelSouth;
		case EAST:
			return cytoPanelEast;
		case WEST:
			return cytoPanelWest;
		case SOUTH_WEST:
			return cytoPanelSouthWest;
		}

		// houston we have a problem
		throw new IllegalArgumentException("Illegal Argument:  " + compassDirection
		                                   + ".  Must be one of:  {SOUTH,EAST,WEST,SOUTH_WEST}.");
	}

	public void addCytoPanelComponent(CytoPanelComponent cp, Dictionary props) {
		CytoPanelImp impl = getCytoPanelInternal(cp.getCytoPanelName());
		impl.add(cp);
	}

	public void removeCytoPanelComponent(CytoPanelComponent cp, Dictionary props) {
		CytoPanelImp impl = getCytoPanelInternal(cp.getCytoPanelName());
		impl.remove(cp);
	}

	public JToolBar getStatusToolBar() {
		return statusToolBar;
	}

	public void addToolBarComponent(ToolBarComponent tp, Dictionary props) {
		((CytoscapeToolBar)cyMenus.getJToolBar()).addToolBarComponent(tp);
	}

	public void removeToolBarComponent(ToolBarComponent tp, Dictionary props) {
		((CytoscapeToolBar)cyMenus.getJToolBar()).removeToolBarComponent(tp);		
	}
	
	// handle CytoscapeStartEvent
	@Override
	public void handleEvent(CyStartEvent e) {
		this.setVisible(true);
		this.toFront();
	}

	@Override
	public void handleEvent(SessionLoadedEvent e) {
		// Update window title
		final String sessionName = e.getLoadedFileName();		
		this.setTitle(TITLE_PREFIX_STRING + sessionName);
	}

	@Override
	public void handleEvent(SessionSavedEvent e) {
		// Update window title
		final String sessionName = e.getSavedFileName();
		this.setTitle(TITLE_PREFIX_STRING + sessionName);
	}
}
