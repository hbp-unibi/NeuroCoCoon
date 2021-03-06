/*
 * Copyright (c) 2006-2012, JGraph Ltd */
package de.unibi.hbp.ncc;

import com.mxgraph.io.mxCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxSwingConstants;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxResources;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphSelectionModel;
import de.unibi.hbp.ncc.editor.BasicGraphEditor;
import de.unibi.hbp.ncc.editor.EditorActions;
import de.unibi.hbp.ncc.editor.EditorMenuBar;
import de.unibi.hbp.ncc.editor.EditorPalette;
import de.unibi.hbp.ncc.editor.EditorToolBar;
import de.unibi.hbp.ncc.editor.EntityConfigurator;
import de.unibi.hbp.ncc.editor.EntityCreator;
import de.unibi.hbp.ncc.editor.InspectorController;
import de.unibi.hbp.ncc.editor.TooltipProvider;
import de.unibi.hbp.ncc.editor.props.DetailsEditor;
import de.unibi.hbp.ncc.editor.props.MasterDetailsEditor;
import de.unibi.hbp.ncc.editor.props.Notificator;
import de.unibi.hbp.ncc.editor.props.PropChangeListener;
import de.unibi.hbp.ncc.lang.AnyConnection;
import de.unibi.hbp.ncc.lang.Connectable;
import de.unibi.hbp.ncc.lang.DataPlot;
import de.unibi.hbp.ncc.lang.GraphCellConfigurator;
import de.unibi.hbp.ncc.lang.LanguageEntity;
import de.unibi.hbp.ncc.lang.NamedEntity;
import de.unibi.hbp.ncc.lang.NeuronConnection;
import de.unibi.hbp.ncc.lang.NeuronType;
import de.unibi.hbp.ncc.lang.PoissonSource;
import de.unibi.hbp.ncc.lang.ProbeConnection;
import de.unibi.hbp.ncc.lang.Program;
import de.unibi.hbp.ncc.lang.RegularSpikeSource;
import de.unibi.hbp.ncc.lang.Scope;
import de.unibi.hbp.ncc.lang.StandardPopulation;
import de.unibi.hbp.ncc.lang.SynapseType;
import de.unibi.hbp.ncc.lang.modules.InceptionGroup;
import de.unibi.hbp.ncc.lang.modules.ModuleExample;
import de.unibi.hbp.ncc.lang.modules.RetinaGrid;
import de.unibi.hbp.ncc.lang.modules.RobotHeadDirection;
import de.unibi.hbp.ncc.lang.modules.SynfireChain;
import de.unibi.hbp.ncc.lang.modules.WinnerTakeAll;
import de.unibi.hbp.ncc.lang.props.EditableProp;
import org.w3c.dom.Document;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Collection;

public class NeuroCoCoonEditor extends BasicGraphEditor implements PropChangeListener, InspectorController
{

	public static final String VERSION = "1.0.0-M24";

	private ProgramGraphComponent programGraphComponent;
	private JTabbedPane rightHandTabs;
	private DetailsEditor detailsEditor;
	private MasterDetailsEditor<NeuronType> neuronTypeEditor;
	private MasterDetailsEditor<SynapseType> synapseTypeEditor;
	private int resultsTabIndex;

	private static final boolean ENABLE_GRID_BY_DEFAULT = true;  // controls effect on movement AND visibility

	public NeuroCoCoonEditor ()
	{
		this("NeuroCoCoon Editor", new ProgramGraphComponent(new ProgramGraph(new Program())));
	}

	public NeuroCoCoonEditor (String appTitle, ProgramGraphComponent component) {
		super(appTitle, component);
		programGraphComponent = component;
		Notificator.getInstance().subscribe(component);
	}

	public Program getProgram () { return programGraphComponent.getProgramGraph().getProgram(); }

	public mxIGraphModel getGraphModel () { return programGraphComponent.getGraph().getModel(); }

	@Override
	public void initialize () {
		// System.err.println("NeuroCoCoonEditor.initialize: enter");
		super.initialize();

		programGraphComponent.getProgramGraph().setToolBar(getEditorToolBar());

		final mxGraph graph = graphComponent.getGraph();

		// Creates the shapes palette
		EditorPalette basicPalette = insertPalette(mxResources.get("basic"));
		EditorPalette modulesPalette = insertPalette(mxResources.get("modules"));
		// EditorPalette symbolsPalette = insertPalette(mxResources.get("symbols"));

		detailsEditor.setSubject(getProgram());
		graph.getSelectionModel().addListener(mxEvent.CHANGE, (sender, evt) -> {
			mxGraphSelectionModel model = (mxGraphSelectionModel) sender;
			if (model.isEmpty())
				detailsEditor.setSubject(getProgram());
			else {
				Collection<?> added = (Collection<?>) evt.getProperty("removed");  // yes "added" and "removed" are swapped, see notice in mxGraphSelectionModel
				if (added != null && !added.isEmpty()) {
					Object tmp = added.iterator().next();
					// System.err.println("selected: " + tmp);
					if (tmp instanceof mxCell) {
						Object value = ((mxCell) tmp).getValue();
						if (value instanceof LanguageEntity)
							detailsEditor.setSubject((LanguageEntity) value);
					}
				}
			}
		});

		programGraphComponent.setInspector(this);

		graph.addListener(mxEvent.FLIP_EDGE, (sender, evt) -> {
			AnyConnection.RoutingStyle style = (AnyConnection.RoutingStyle) evt.getProperty("style");
			status("Routing style changed to " + style.getDisplayName());
		});

		// Adds some template cells for dropping into the graph
		// System.err.println("NeuroCoCoonEditor.initialize: palette");

		basicPalette.addTemplate(RegularSpikeSource.CREATOR);
		basicPalette.addTemplate(PoissonSource.CREATOR);
		basicPalette.addTemplate(StandardPopulation.CREATOR);
		basicPalette.addEdgeTemplate(NeuronConnection.CREATOR);
		basicPalette.addTemplate(DataPlot.CREATOR);
		basicPalette.addEdgeTemplate(ProbeConnection.CREATOR);

		// TODO add back support for assemblies, slices (a special case of a view) and associated dependency edges
/*
		basicPalette.addTemplate("Assembly",
								   new ImageIcon(NeuroCoCoonEditor.class.getResource("editor/images/lang/assembly.png")),
								   "assembly",
								   100, 60, ModuleExample.CREATOR);
		basicPalette.addTemplate("Slice",
								   new ImageIcon(NeuroCoCoonEditor.class.getResource("editor/images/lang/dummy.png")),
								   "slice",
								   100, 100, ModuleExample.CREATOR);
*/
		// basicPalette.addEdgeTemplate(DependencyConnection.CREATOR);

		modulesPalette.addTemplate(SynfireChain.CREATOR);
		modulesPalette.addTemplate(WinnerTakeAll.CREATOR);
		modulesPalette.addTemplate(RetinaGrid.CREATOR);
		modulesPalette.addTemplate(InceptionGroup.CREATOR);
		modulesPalette.addTemplate(RobotHeadDirection.CREATOR);
		modulesPalette.addTemplate(ModuleExample.CREATOR);
		Notificator.getInstance().subscribe(this);  // to get notified of non-visual (in the graph) property changes
	}

	@Override
	public void propertyChanged (EditableProp<?> changed, int position) { setModified(true); }

	@Override
	public void multiplePropertyValuesChanged (LanguageEntity affected) { setModified(true); }

	@Override
	public void otherPropertiesVisibilityChanged (LanguageEntity affected) { setModified(true); }

	@Override
	public void edit (LanguageEntity subject) {
		detailsEditor.setSubject(subject);
		rightHandTabs.setSelectedIndex(0);  // reveal the inspector
		if (!subject.isPredefined())
			detailsEditor.startEditing();
	}

	private final Component NO_RESULTS_PLACEHOLDER = new JLabel("No results.", SwingConstants.CENTER);
	private static final String DEFAULT_RESULTS_TAB_TITLE = "Results";

	@Override
	protected void addInspectorTabs (JTabbedPane inspector) {
		// System.err.println("NeuroCoCoonEditor.addInspectorTabs: enter");
		rightHandTabs = inspector;
		detailsEditor = new DetailsEditor();
		inspector.addTab("Inspector", detailsEditor.getComponent());
		Scope global = programGraphComponent.getProgramGraph().getProgram().getGlobalScope();
		neuronTypeEditor = new MasterDetailsEditor<>(global.getNeuronTypes(), NeuronType::new, this);
		inspector.addTab("Neurons", neuronTypeEditor.getComponent());
		synapseTypeEditor = new MasterDetailsEditor<>(global.getSynapseTypes(), SynapseType::new, this);
		inspector.addTab("Synapses", synapseTypeEditor.getComponent());
		// dataPlotEditor = new MasterDetailsEditor<>(global.getDataPlots(), DataPlot::new, this);
		resultsTabIndex = inspector.getTabCount();
		inspector.addTab(DEFAULT_RESULTS_TAB_TITLE, NO_RESULTS_PLACEHOLDER);
		// inspector.setEnabledAt(resultsTabIndex, false);
		super.addInspectorTabs(inspector);
	}

	public void setResultsTab (String tabTitle, Component content, boolean activateTab) {
		if (tabTitle == null) tabTitle = DEFAULT_RESULTS_TAB_TITLE;
		boolean enableTab = content != null;
		if (content == null) content = NO_RESULTS_PLACEHOLDER;
		rightHandTabs.setTitleAt(resultsTabIndex, tabTitle);
		rightHandTabs.setComponentAt(resultsTabIndex, content);
		// rightHandTabs.setEnabledAt(resultsTabIndex, enableTab);
		// disabling only means that clicks on the tab header are ignored
		// there is no visible difference for a disabled tab header
		if (activateTab && enableTab)
			rightHandTabs.setSelectedIndex(resultsTabIndex);
	}

	public void clearResultsTab () {
		setResultsTab(null, null, false);
	}

	public void setJobStatus (EditorToolBar.StatusLevel level, String topSummary, String longerBottomText) {
		getEditorToolBar().setJobStatusInToolBar(level, topSummary);
		status(longerBottomText);
	}

	public void setJobStatus (EditorToolBar.StatusLevel level, String shortSummary) {
		setJobStatus(level, shortSummary, shortSummary);
	}

	public void setJobStatus (String topSummary, String longerBottomText) {
		setJobStatus(EditorToolBar.StatusLevel.NEUTRAL, topSummary, longerBottomText);
	}

	public void setJobStatus (String shortSummary) {
		setJobStatus(EditorToolBar.StatusLevel.NEUTRAL, shortSummary, shortSummary);
	}

	public void clearJobStatus () {
		setJobStatus(EditorToolBar.StatusLevel.PLACEHOLDER, "No Job", "Ready.");
		// with empty text the status bar disappears completely
	}


	public static class ProgramGraphComponent extends mxGraphComponent {
		private final ProgramGraph programGraph;
		private InspectorController inspector;

		public ProgramGraphComponent (ProgramGraph graph) {
			super(graph);
			programGraph = graph;
			graph.getProgram().setGraphComponent(this);

			// Sets switches typically used in an editor
			setPageVisible(false);
			setDragEnabled(false);  // disables many smart drag options and we do not support import from external sources anyway
			setGridVisible(ENABLE_GRID_BY_DEFAULT);  // menu item for grid is based on gridEnabled property of graph
			setToolTips(true);
			getConnectionHandler().setCreateTarget(false);  // no meaningful default target to create when pulling out an edge
			// setPanning(true);  // is enabled by default
			// Stops editing after enter has been pressed instead of adding a newline to the current editing value
			setEnterStopsCellEditing(true);  // we allow no editable multi-line labels


			// Loads the default stylesheet from an external file
			mxCodec codec = new mxCodec();
			Document doc = mxUtils.loadDocument(NeuroCoCoonEditor.class.getResource("resources/ncc-style.xml").toString());
			if (doc != null)
				codec.decode(doc.getDocumentElement(), graph.getStylesheet());

			// Sets the background to white
			getViewport().setOpaque(true);
			getViewport().setBackground(Color.WHITE);
			graphControl.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked (MouseEvent e) {
					if (isEnabled() && inspector != null) {
						if (!e.isConsumed() && isEditEvent(e)) {
							Object cell = getCellAt(e.getX(), e.getY(), false);

							Object value;
							if (cell instanceof mxICell &&
									(value = ((mxICell) cell).getValue()) instanceof LanguageEntity) {
								LanguageEntity entity = (LanguageEntity) value;
								inspector.edit(entity);
								e.consume();
							}
						}
					}
				}

			});
		}

		void setInspector (InspectorController inspector) { this.inspector = inspector; }

		public ProgramGraph getProgramGraph () { return programGraph; }

		// TODO allow dragging of neuron/synapse styles onto vertices/edges to set the type
	}

	/**
	 * A graph that creates new edges from a given template edge.
	 */
	public static class ProgramGraph extends mxGraph {
		private final Program program;
		private EditorToolBar toolBar;

		public ProgramGraph (Program program) {
			this.program = program;
			setGridEnabled(ENABLE_GRID_BY_DEFAULT);
			// setGridSize(10);  // default is 10
			// we use standard node sizes like 100 or 60 pixels for populations
			// module ports are 20 pixels with the connection point in the middle offset by 10 pixels
			setAllowDanglingEdges(true);  // otherwise drag&drop of edge template is effectively unusable
			setDropEnabled(false);  // consequences unclear, seems not to harm; palette can still be used as a source
			setMultigraph(false);

			addListener(mxEvent.CELLS_ADDED, (sender, evt ) -> {
				Object[] cells = (Object[]) evt.getProperty("cells");
				for (Object obj: cells) {
					if (obj instanceof mxCell) {
						mxCell cell = (mxCell) obj;
						Object value = cell.getValue();
						if (value instanceof GraphCellConfigurator)
							((GraphCellConfigurator) value).configurePlaceholder(this, cell);
					}
				}
			});

			addListener(mxEvent.CELLS_RESIZED, (sender, evt ) -> {
				Object[] cells = (Object[]) evt.getProperty("cells");
				for (Object obj: cells) {
					if (obj instanceof mxICell) {
						mxICell cell = (mxICell) obj;
						Object value = cell.getValue();
						if (value instanceof GraphCellConfigurator)
							((GraphCellConfigurator) value).resizeExisting(this, cell);
					}
				}
			});

		}

		void setToolBar (EditorToolBar toolBar) { this.toolBar = toolBar; }

		public Program getProgram () { return program; }

		@Override
		public void cellsAdded (Object[] cells, Object parent, Integer index, Object source, Object target,
								boolean absolute, boolean constrain) {
			for (Object obj: cells) {
				if (obj instanceof mxICell) {
					mxICell cell = (mxICell) obj;
					Object value = cell.getValue();
					LanguageEntity duplicatedValue = null;
					if (value instanceof EntityCreator) {
						duplicatedValue = ((EntityCreator<?>) value).create();
						if (value instanceof EntityConfigurator) {
							((EntityConfigurator) value).configureEntityAndCell(duplicatedValue, cell, toolBar);
						}
					}
					else if (value instanceof LanguageEntity) {
						LanguageEntity entityValue = (LanguageEntity) value;
						mxICell owner = entityValue.getOwningCell();
						if (owner != cell && owner != null && owner.getParent() != null)
							// reference has been cloned to another cell and both are part of a graph
							duplicatedValue = entityValue.duplicate();
						else if (owner != cell)
							duplicatedValue = entityValue;  // adopt existing value from previous owner cell outside graph
					}
					if (duplicatedValue != null) {
						cell.setValue(duplicatedValue);
						duplicatedValue.setOwningCell(cell);
					}
					// TODO do something similar with the (transitive) children of the cell?
				}
			}
			super.cellsAdded(cells, parent, index, source, target, absolute, constrain);
		}

		@Override
		public Object flipEdge (Object edge) {
			if (edge instanceof mxICell) {
				Object value = ((mxICell) edge).getValue();
				if (value instanceof AnyConnection) {
					AnyConnection connection = (AnyConnection) value;
					connection.cycleRoutingStyle();
					fireEvent(new mxEventObject(mxEvent.FLIP_EDGE, "edge", edge,
												"style", connection.getRoutingStyle()));
				}
			}

			return edge;
		}

		@Override
		public Object[] removeCells (Object[] cells, boolean includeEdges) {
			cells = super.removeCells(cells, includeEdges);
			for (Object obj: cells) {
				if (obj instanceof mxCell) {
					mxCell cell = (mxCell) obj;
					Object value = cell.getValue();
					if (value instanceof NamedEntity) {
						NamedEntity entityValue = (NamedEntity) value;
						if (!entityValue.delete(getModel()))
							throw new IllegalStateException("Could not delete referenced named entity " + entityValue);
					}
					if (value instanceof LanguageEntity)
						((LanguageEntity) value).setOwningCell(null);
				}
			}
			return cells;
		}

/* We want our network module ports to appear as terminals at edges!
   Thus, they are NOT "ports" in the mxGraph sense.
		// Ports are not used as terminals for edges, they are
		// only used to compute the graphical connection point
		@Override
		public boolean isPort(Object cell) {
			mxGeometry geo = getCellGeometry(cell);
			return (geo != null) && geo.isRelative();
		}
*/
		// can we get a red border when dragging to invalid target nodes?
		// not easily, com.mxgraph.swing.handler.mxCellMarker.getCell override in mxConnectionHandler.java
		// deliberately skips the red highlight, for validation errors with empty message strings
		// would need to override getEdgeValidationError to return some message for !isValidConnection to change this
		@Override
		public boolean isValidSource (Object cell) {
			if (cell instanceof mxICell) {
				Object value = ((mxICell) cell).getValue();
				if (value instanceof Connectable) {
					Connectable con = (Connectable) value;
					return con.isAnyValidSource() && super.isValidSource(cell);
				}
			}
			return super.isValidSource(cell);
		}

		@Override
		public boolean isValidTarget (Object cell) {
			if (cell instanceof mxICell) {
				Object value = ((mxICell) cell).getValue();
				if (value instanceof Connectable) {
					Connectable con = (Connectable) value;
					return con.isAnyValidTarget() && super.isValidSource(cell);
				}
			}
			return super.isValidSource(cell);  // only the isValidSource method implements the general (direction-agnostic) checks
		}

		@Override
		public boolean isValidConnection (Object edge, Object source, Object target) {
			// System.err.println("isValidConnection: " + edge + ", source=" + source + ", target=" + target);
			if (super.isValidConnection(edge, source, target)) {
				if (edge instanceof mxICell) {
					Object edgeValue = ((mxICell) edge).getValue();
					if (edgeValue instanceof AnyConnection) {
						AnyConnection con = (AnyConnection) edgeValue;
						if (source instanceof mxICell) {
							Object sourceValue = ((mxICell) source).getValue();
							if (sourceValue instanceof Connectable &&
									!((Connectable) sourceValue).isValidSource(con.getEdgeKind()))
								return false;
						}
						if (target instanceof mxICell) {
							Object targetValue = ((mxICell) target).getValue();
							if (targetValue instanceof Connectable &&
									!((Connectable) targetValue).isValidTarget(con.getEdgeKind()))
								return false;
						}
					}
				}
				return true;  // allow any unexpected edge connections not related to our language constructs (there should be none)
			}
			else
				return false;
		}

		@Override
		public String getToolTipForCell (Object cell) {
			if (cell instanceof mxICell) {
				Object value = ((mxICell) cell).getValue();
				if (value instanceof TooltipProvider)
					return ((TooltipProvider) value).getTooltip();
				// else return "Style=" + ((mxICell) cell).getStyle();
			}
			return null;
		}

		/**
		 * Overrides the method to use the currently selected synapse type for
		 * new edges.
		 */
		@Override
		public Object createEdge(Object parent, String id, Object value,
								 Object source, Object target, String style) {

			if (source instanceof mxICell) {
				Object sourceValue = ((mxICell) source).getValue();
				if (sourceValue instanceof Connectable) {
					Connectable sourceCon = (Connectable) sourceValue;
					if (sourceCon.isValidSource(Connectable.EdgeKind.PROBE)) {  // data plots are the only valid probe sources (and are not valid as anys other kind of source)
						ProbeConnection probeConnection = new ProbeConnection();
						mxCell edge = (mxCell) super.createEdge(parent, id, probeConnection, source, target,
																ProbeConnection.CREATOR.getCellStyle());
						probeConnection.setOwningCell(edge);
						return edge;
					}
				}
			}
			SynapseType synapseType = toolBar.getCurrentSynapseType();
			NeuronConnection neuronConnection = new NeuronConnection(synapseType);
			mxCell edge = (mxCell) super.createEdge(parent, id, neuronConnection, source, target,
													synapseType.getCellStyle());
			neuronConnection.setOwningCell(edge);
			return edge;
		}

	}

	private static final ImageIcon APP_ICON =
			new ImageIcon(NeuroCoCoonEditor.class.getResource("editor/images/appicon.png"));

	public ImageIcon getAppIcon () { return APP_ICON; }

	@Override
	public JFrame createFrame (JMenuBar menuBar, int width, int height) {
		JFrame frame = super.createFrame(menuBar, width, height);
		frame.setIconImage(APP_ICON.getImage());
		return frame;
	}

	public static void main (String[] args) {
		final File open;
		if (args.length == 0)
			open = null;
		else {
			open = new File(args[0]).getAbsoluteFile();
			if (!open.isFile()) {
				System.err.println("Cannot open document " + open);
				System.exit(10);
			}
			if (args.length > 1)
				System.err.println("Only a single file can be opened from the command line.");
		}
		SwingUtilities.invokeLater(() -> {
			createAndShowGUI(open);
		});
	}

	public static void createAndShowGUI (File open) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (ReflectiveOperationException | UnsupportedLookAndFeelException ex) {
			ex.printStackTrace(System.err);
		}

		// example, how to force a certain platform-independent look&feel
/*
		try {
			for (UIManager.LookAndFeelInfo laf: UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(laf.getName())) {
					UIManager.setLookAndFeel(laf.getClassName());
				}
			}
		}
		catch (ReflectiveOperationException | UnsupportedLookAndFeelException ex) {
			ex.printStackTrace(System.err);
		}
*/

		mxSwingConstants.SHADOW_COLOR = Color.LIGHT_GRAY;
		mxConstants.W3C_SHADOWCOLOR = "#D3D3D3";

		try {  // CheerpJ Debugging
			NeuroCoCoonEditor editor = new NeuroCoCoonEditor();
			editor.initialize();  // moved from constructor so that subclass object fields have been initialized already
			JFrame frame = editor.createFrame(new EditorMenuBar(editor), 1200, 800);
			frame.setVisible(true);
			if (open != null)
				EditorActions.getOpenAction().openFile(editor, open);
		}
		catch (Throwable t) {
			t.printStackTrace(System.err);
		}
	}
}
