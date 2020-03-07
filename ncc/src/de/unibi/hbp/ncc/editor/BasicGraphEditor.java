package de.unibi.hbp.ncc.editor;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.layout.mxEdgeLabelLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.layout.mxOrganicLayout;
import com.mxgraph.layout.mxParallelEdgeLayout;
import com.mxgraph.layout.mxPartitionLayout;
import com.mxgraph.layout.mxStackLayout;
import com.mxgraph.swing.handler.mxKeyboardHandler;
import com.mxgraph.swing.handler.mxRubberband;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.mxGraphOutline;
import com.mxgraph.swing.util.mxMorphing;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxResources;
import com.mxgraph.util.mxUndoManager;
import com.mxgraph.util.mxUndoableEdit;
import com.mxgraph.util.mxUndoableEdit.mxUndoableChange;
import com.mxgraph.view.mxGraph;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.util.List;

public class BasicGraphEditor extends JPanel
{

	private static final long serialVersionUID = -6561623072112577140L;

	/**
	 * Adds required resources for i18n
	 */
	static
	{
		try
		{
			mxResources.add("de/unibi/hbp/ncc/resources/editor");
			// may NOT be an absolute path for some reason
		}
		catch (Exception e)
		{
			// ignore
		}
	}

	protected mxGraphComponent graphComponent;
	protected mxGraphOutline graphOutline;
	protected JTabbedPane libraryPane;
	protected EditorToolBar editorToolBar;
	protected mxUndoManager undoManager;
	protected String appTitle;
	protected JLabel statusBar;
	protected File currentFile;

	/**
	 * Flag indicating whether the current graph has been modified 
	 */
	protected boolean modified = false;

	protected mxRubberband rubberBand;
	protected mxKeyboardHandler keyboardHandler;

	protected mxIEventListener undoHandler = new mxIEventListener() {
		public void invoke(Object source, mxEventObject evt)
		{
			undoManager.undoableEditHappened((mxUndoableEdit) evt.getProperty("edit"));
		}
	};

	protected mxIEventListener changeTracker = (source, evt) -> setModified(true);

	public BasicGraphEditor(String appTitle, mxGraphComponent component) {
		// Stores and updates the frame title
		this.appTitle = appTitle;

		// Stores a reference to the graph and creates the command history
		graphComponent = component;
	}

	public void initialize () {
		final mxGraph graph = graphComponent.getGraph();
		undoManager = createUndoManager();

		// Do not change the scale and translation after files have been loaded
		graph.setResetViewOnRootChange(false);

		// Updates the modified flag if the graph model changes
		graph.getModel().addListener(mxEvent.CHANGE, changeTracker);

		// Adds the command history to the model and view
		graph.getModel().addListener(mxEvent.UNDO, undoHandler);
		graph.getView().addListener(mxEvent.UNDO, undoHandler);

		// Keeps the selection in sync with the command history
		mxIEventListener undoHandler = (source, evt) -> {
			List<mxUndoableChange> changes = ((mxUndoableEdit) evt.getProperty("edit")).getChanges();
			graph.setSelectionCells(graph.getSelectionCellsForChanges(changes));
		};

		undoManager.addListener(mxEvent.UNDO, undoHandler);
		undoManager.addListener(mxEvent.REDO, undoHandler);

		// Creates the graph outline component
		graphOutline = new mxGraphOutline(graphComponent);

		// Creates the library pane that contains the tabs with the palettes
		libraryPane = new JTabbedPane();

		// Creates the inner split pane that contains the library with the
		// palettes and the graph outline on the left side of the window
		JSplitPane inner = new JSplitPane(JSplitPane.VERTICAL_SPLIT, libraryPane, graphOutline);
		inner.setDividerLocation(320);
		inner.setResizeWeight(1);
		inner.setDividerSize(6);
		inner.setBorder(null);

		JTabbedPane inspector = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
		addInspectorTabs(inspector);

		JSplitPane graphAndInspector = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, graphComponent, inspector);
		graphAndInspector.setOneTouchExpandable(true);
		graphAndInspector.setDividerLocation(600);
		graphAndInspector.setDividerSize(6);
		graphAndInspector.setBorder(null);

		// Creates the outer split pane that contains the inner split pane and
		// the graph component on the right side of the window
		JSplitPane outer = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inner, graphAndInspector);
		outer.setOneTouchExpandable(true);
		outer.setDividerLocation(200);
		outer.setDividerSize(6);
		outer.setBorder(null);

		// Creates the status bar
		statusBar = createStatusBar();

		// Display some useful information about repaint events
		// installRepaintListener();

		// Puts everything together
		setLayout(new BorderLayout());
		add(outer, BorderLayout.CENTER);
		add(statusBar, BorderLayout.SOUTH);
		editorToolBar = installToolBar();

		// Installs rubberband selection and handling for some special
		// keystrokes such as F2, Control-C, -V, X, A etc.
		installHandlers();
		installListeners();
		updateTitle();
	}

	protected void addInspectorTabs (JTabbedPane inspector) {}

	protected mxUndoManager createUndoManager()
	{
		return new mxUndoManager();
	}

	protected void installHandlers()
	{
		rubberBand = new mxRubberband(graphComponent);
		keyboardHandler = new EditorKeyboardHandler(graphComponent);
	}

	protected EditorToolBar installToolBar()
	{
		EditorToolBar toolBar = new EditorToolBar(this, JToolBar.HORIZONTAL);
		add(toolBar, BorderLayout.NORTH);
		return toolBar;
	}

	protected JLabel createStatusBar()
	{
		JLabel statusBar = new JLabel(mxResources.get("ready"));
		statusBar.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

		return statusBar;
	}

	public EditorPalette insertPalette(String title)
	{
		final EditorPalette palette = new EditorPalette();
		final JScrollPane scrollPane = new JScrollPane(palette);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		libraryPane.add(title, scrollPane);

		// Updates the widths of the palettes if the container size changes
		libraryPane.addComponentListener(new ComponentAdapter()
		{
			public void componentResized(ComponentEvent e)
			{
				int w = scrollPane.getWidth() - scrollPane.getVerticalScrollBar().getWidth();
				palette.setPreferredWidth(w);
			}
		});

		return palette;
	}

	protected void mouseWheelMoved(MouseWheelEvent e)
	{
		if (e.getWheelRotation() < 0)
			graphComponent.zoomIn();
		else
			graphComponent.zoomOut();

		status(mxResources.get("scale") + ": " + (int) (100 * graphComponent.getGraph().getView().getScale()) + "%");
	}

	private abstract static class PopupMenuHandler extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e)
		{
			// Handles context menu on the Mac where the trigger is on mousepressed
			mouseReleased(e);
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			if (e.isPopupTrigger())
				showPopupMenu(e);
		}

		protected abstract void showPopupMenu (MouseEvent e);
	}

	private class OutlinePopupMenuHandler extends PopupMenuHandler {
		@Override
		protected void showPopupMenu (MouseEvent e) {
			Point pt = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), graphComponent);
			JCheckBoxMenuItem item = new JCheckBoxMenuItem(mxResources.get("magnifyPage"));
			item.setSelected(graphOutline.isFitPage());
			item.addActionListener(actionEvent -> {
				graphOutline.setFitPage(!graphOutline.isFitPage());
				graphOutline.repaint();
			});

			JCheckBoxMenuItem item2 = new JCheckBoxMenuItem(mxResources.get("showLabels"));
			item2.setSelected(graphOutline.isDrawLabels());

			item2.addActionListener(actionEvent -> {
				graphOutline.setDrawLabels(!graphOutline.isDrawLabels());
				graphOutline.repaint();
			});

			JCheckBoxMenuItem item3 = new JCheckBoxMenuItem(mxResources.get("buffering"));
			item3.setSelected(graphOutline.isTripleBuffered());
			item3.addActionListener(actionEvent -> {
				graphOutline.setTripleBuffered(!graphOutline.isTripleBuffered());
				graphOutline.repaint();
			});

			JPopupMenu menu = new JPopupMenu();
			menu.add(item);
			menu.add(item2);
			menu.add(item3);
			menu.show(graphComponent, pt.x, pt.y);

			e.consume();
		}
	}

	private class GraphPopupMenuHandler extends PopupMenuHandler {
		@Override
		protected void showPopupMenu (MouseEvent e) {
			Point pt = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), graphComponent);
			EditorPopupMenu menu = new EditorPopupMenu(BasicGraphEditor.this);
			menu.show(graphComponent, pt.x, pt.y);

			e.consume();
		}
	}

	protected void installListeners()
	{
		// Installs mouse wheel listener for zooming
		MouseWheelListener wheelTracker = e -> {
			if (e.getSource() instanceof mxGraphOutline || e.isControlDown())
				BasicGraphEditor.this.mouseWheelMoved(e);
		};

		// Handles mouse wheel events in the outline and graph component
		graphOutline.addMouseWheelListener(wheelTracker);
		graphComponent.addMouseWheelListener(wheelTracker);

		// Installs the popup menus in the outline and the graph
		graphOutline.addMouseListener(new OutlinePopupMenuHandler());
		graphComponent.getGraphControl().addMouseListener(new GraphPopupMenuHandler());
	}

	public void setCurrentFile(File file)
	{
		File oldValue = currentFile;
		currentFile = file;
		if (oldValue != file)
		{
			firePropertyChange("currentFile", oldValue, file);
			updateTitle();
		}
	}

	public File getCurrentFile()
	{
		return currentFile;
	}

	/**
	 * 
	 * @param modified
	 */
	public void setModified (boolean modified)
	{
		boolean oldValue = this.modified;
		this.modified = modified;

		if (oldValue != modified)
		{
			firePropertyChange("modified", oldValue, modified);
			updateTitle();
		}
	}

	/**
	 * 
	 * @return whether or not the current graph has been modified
	 */
	public boolean isModified() { return modified; }

	public mxGraphComponent getGraphComponent() { return graphComponent; }

	public mxGraphOutline getGraphOutline() { return graphOutline; }
	
	public JTabbedPane getLibraryPane() { return libraryPane; }

	public mxUndoManager getUndoManager() { return undoManager; }

	/**
	 * 
	 * @param name
	 * @param action
	 * @return a new Action bound to the specified string name
	 */
	public Action bind(String name, final Action action)
	{
		return bind(name, action, null);
	}

	/**
	 * 
	 * @param name
	 * @param action
	 * @return a new Action bound to the specified string name and icon
	 */
	@SuppressWarnings("serial")
	public Action bind(String name, final Action action, String iconUrl) {
		// System.err.println("BasicGraphEditor.bind: " + name + ", " + iconUrl);
		AbstractAction newAction = new AbstractAction(name, (iconUrl != null)
				? new ImageIcon(BasicGraphEditor.class.getResource(iconUrl))
				: null) {
			public void actionPerformed(ActionEvent e) {
				action.actionPerformed(new ActionEvent(getGraphComponent(), e.getID(), e.getActionCommand()));
			}
		};
		
		newAction.putValue(Action.SHORT_DESCRIPTION, action.getValue(Action.SHORT_DESCRIPTION));
		
		return newAction;
	}

	public void status(String msg) {
		statusBar.setText(msg);
	}

	public void updateTitle()
	{
		JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);

		if (frame != null)
		{
			String title = (currentFile != null) ? currentFile.getAbsolutePath() : mxResources.get("newNetwork");
			if (modified) title += "*";
			frame.setTitle(title + " - " + appTitle);
		}
	}

	public void about()
	{
		JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);

		if (frame != null)
		{
			EditorAboutFrame about = new EditorAboutFrame(frame);
			about.setModal(true);

			// Centers inside the application frame
			int x = frame.getX() + (frame.getWidth() - about.getWidth()) / 2;
			int y = frame.getY() + (frame.getHeight() - about.getHeight()) / 2;
			about.setLocation(x, y);

			// Shows the modal dialog and waits
			about.setVisible(true);
		}
	}

	public void exit()
	{
		JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);

		if (frame != null)
			frame.dispose();
	}

	public void setLookAndFeel(String clazz)
	{
		JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);

		if (frame != null)
		{
			try
			{
				UIManager.setLookAndFeel(clazz);
				SwingUtilities.updateComponentTreeUI(frame);

				// Needs to assign the key bindings again
				keyboardHandler = new EditorKeyboardHandler(graphComponent);
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
			}
		}
	}

	public JFrame createFrame(JMenuBar menuBar, int width, int height)
	{
		JFrame frame = new JFrame();
		frame.getContentPane().add(this);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setJMenuBar(menuBar);
		frame.setSize(width, height);

		// Updates the frame title
		updateTitle();

		return frame;
	}

	/**
	 * Creates an action that executes the specified layout.
	 * 
	 * @param key Key to be used for getting the label from mxResources and also
	 * to create the layout instance for the commercial graph editor example.
	 * @return an action that executes the specified layout
	 */
	@SuppressWarnings("serial")
	public Action graphLayout(final String key, boolean animate)
	{
		final mxIGraphLayout layout = createLayout(key, animate);

		if (layout != null)
		{
			return new AbstractAction(mxResources.get(key))
			{
				public void actionPerformed(ActionEvent e)
				{
					final mxGraph graph = graphComponent.getGraph();
					Object cell = graph.getSelectionCell();

					if (cell == null || graph.getModel().getChildCount(cell) == 0)
					{
						cell = graph.getDefaultParent();
					}

					graph.getModel().beginUpdate();
					try
					{
						long t0 = System.currentTimeMillis();
						layout.execute(cell);
						status("Layout: " + (System.currentTimeMillis() - t0) + " ms");
					}
					finally
					{
						mxMorphing morph = new mxMorphing(graphComponent, 20, 1.2, 20);
						morph.addListener(mxEvent.DONE, (sender, evt) -> graph.getModel().endUpdate());
						morph.startAnimation();
					}

				}

			};
		}
		else
		{
			return new AbstractAction(mxResources.get(key))
			{
				public void actionPerformed(ActionEvent e)
				{
					JOptionPane.showMessageDialog(graphComponent, mxResources.get("noLayout"));
				}
			};
		}
	}

	/**
	 * Creates a layout instance for the given identifier.
	 */
	protected mxIGraphLayout createLayout(String ident, boolean animate)
	{
		mxIGraphLayout layout = null;

		if (ident != null)
		{
			mxGraph graph = graphComponent.getGraph();

			switch (ident) {
				case "verticalHierarchical":
					layout = new mxHierarchicalLayout(graph);
					break;
				case "horizontalHierarchical":
					layout = new mxHierarchicalLayout(graph, JLabel.WEST);
					break;
				case "verticalTree":
					layout = new mxCompactTreeLayout(graph, false);
					break;
				case "horizontalTree":
					layout = new mxCompactTreeLayout(graph, true);
					break;
				case "parallelEdges":
					layout = new mxParallelEdgeLayout(graph);
					break;
				case "placeEdgeLabels":
					layout = new mxEdgeLabelLayout(graph);
					break;
				case "organicLayout":
					layout = new mxOrganicLayout(graph);
					break;
			}
			switch (ident) {
				case "verticalPartition":
					layout = new mxPartitionLayout(graph, false) {
						/**
						 * Overrides the empty implementation to return the size of the
						 * graph control.
						 */
						public mxRectangle getContainerSize () {
							return graphComponent.getLayoutAreaSize();
						}
					};
					break;
				case "horizontalPartition":
					layout = new mxPartitionLayout(graph, true) {
						/**
						 * Overrides the empty implementation to return the size of the
						 * graph control.
						 */
						public mxRectangle getContainerSize () {
							return graphComponent.getLayoutAreaSize();
						}
					};
					break;
				case "verticalStack":
					layout = new mxStackLayout(graph, false) {
						/**
						 * Overrides the empty implementation to return the size of the
						 * graph control.
						 */
						public mxRectangle getContainerSize () {
							return graphComponent.getLayoutAreaSize();
						}
					};
					break;
				case "horizontalStack":
					layout = new mxStackLayout(graph, true) {
						/**
						 * Overrides the empty implementation to return the size of the
						 * graph control.
						 */
						public mxRectangle getContainerSize () {
							return graphComponent.getLayoutAreaSize();
						}
					};
					break;
				case "circleLayout":
					layout = new mxCircleLayout(graph);
					break;
			}
		}

		return layout;
	}

	public EditorToolBar getEditorToolBar () { return editorToolBar; }
}
