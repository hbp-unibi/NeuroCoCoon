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
import de.unibi.hbp.ncc.editor.EditorMenuBar;
import de.unibi.hbp.ncc.editor.EditorPalette;
import de.unibi.hbp.ncc.editor.EditorToolBar;
import de.unibi.hbp.ncc.editor.EntityConfigurator;
import de.unibi.hbp.ncc.editor.EntityCreator;
import de.unibi.hbp.ncc.editor.TooltipProvider;
import de.unibi.hbp.ncc.editor.props.DetailsEditor;
import de.unibi.hbp.ncc.editor.props.MasterDetailsEditor;
import de.unibi.hbp.ncc.editor.props.Notificator;
import de.unibi.hbp.ncc.lang.AnyConnection;
import de.unibi.hbp.ncc.lang.Connectable;
import de.unibi.hbp.ncc.lang.DataPlot;
import de.unibi.hbp.ncc.lang.DisplayNamed;
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
import de.unibi.hbp.ncc.lang.modules.ModuleExample;
import de.unibi.hbp.ncc.lang.modules.SynfireChain;
import de.unibi.hbp.ncc.lang.modules.WinnerTakeAll;
import org.w3c.dom.Document;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Component;
import java.util.Collection;

public class NeuroCoCoonEditor extends BasicGraphEditor
{

	public static final String VERSION = "0.9.0";

	private ProgramGraphComponent programGraphComponent;
	private JTabbedPane rightHandTabs;
	private DetailsEditor detailsEditor;
	private MasterDetailsEditor<NeuronType> neuronTypeEditor;
	private MasterDetailsEditor<SynapseType> synapseTypeEditor;
	private int resultsTabIndex;

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

		graph.addListener(mxEvent.FLIP_EDGE, (sender, evt) -> {
			ProgramGraph.EdgeStyles style = (ProgramGraph.EdgeStyles) evt.getProperty("style");
			status("Edge style changed to " + style.getDisplayName());
		});

		// Adds some template cells for dropping into the graph
		// System.err.println("NeuroCoCoonEditor.initialize: palette");

		basicPalette.addTemplate(RegularSpikeSource.CREATOR);
		basicPalette.addTemplate(PoissonSource.CREATOR);
		basicPalette.addTemplate(StandardPopulation.CREATOR);
		basicPalette.addEdgeTemplate(NeuronConnection.CREATOR);
		basicPalette.addTemplate(DataPlot.CREATOR);
		basicPalette.addEdgeTemplate(ProbeConnection.CREATOR);
		modulesPalette.addTemplate(SynfireChain.CREATOR);
		modulesPalette.addTemplate(WinnerTakeAll.CREATOR);
		modulesPalette.addTemplate("Retina",
								   new ImageIcon(NeuroCoCoonEditor.class.getResource("editor/images/lang/retina.png")),
								   "module",
								   100, 60, ModuleExample.CREATOR);
		modulesPalette.addTemplate("Inception",
								   new ImageIcon(NeuroCoCoonEditor.class.getResource("editor/images/lang/inception.png")),
								   "module",
								   100, 60, ModuleExample.CREATOR);
		modulesPalette.addTemplate("Direction",
								   new ImageIcon(NeuroCoCoonEditor.class.getResource("editor/images/lang/robot_head.png")),
								   "module",
								   100, 60, ModuleExample.CREATOR);
		modulesPalette.addTemplate("Generic",
								   new ImageIcon(NeuroCoCoonEditor.class.getResource("editor/images/lang/module.png")),
								   "module",
								   100, 60, ModuleExample.CREATOR);
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
		setJobStatus(EditorToolBar.StatusLevel.PLACEHOLDER, "No Job", "");
	}


		public static class ProgramGraphComponent extends mxGraphComponent
	{
		private final ProgramGraph programGraph;

		public ProgramGraphComponent (ProgramGraph graph)
		{
			super(graph);
			programGraph = graph;
			graph.getProgram().setGraphComponent(this);

			// Sets switches typically used in an editor
			setPageVisible(false);
			// setGridVisible(false);
			// menu item for grid is based on gridEnabled property of graph
			setToolTips(true);
			getConnectionHandler().setCreateTarget(false);
			// Stops editing after enter has been pressed instead of adding a newline to the current editing value
			setEnterStopsCellEditing(true);  // FIXME this does NOT help with our Enter inspector problems


			// Loads the default stylesheet from an external file
			mxCodec codec = new mxCodec();
			Document doc = mxUtils.loadDocument(NeuroCoCoonEditor.class.getResource("resources/default-style.xml").toString());
			if (doc != null)
				codec.decode(doc.getDocumentElement(), graph.getStylesheet());

			// Sets the background to white
			getViewport().setOpaque(true);
			getViewport().setBackground(Color.WHITE);
		}

		public ProgramGraph getProgramGraph () { return programGraph; }

		// TODO allow dragging of edges onto edges (and populations onto populations) to set the synapse type (or neuron type)
		/* *
		 * Overrides drop behaviour to set the cell style if the target
		 * is not a valid drop target and the cells are of the same
		 * type (eg. both vertices or both edges). 
		 */
		/*
		public Object[] importCells(Object[] cells, double dx, double dy,
				Object target, Point location)
		{
			if (target == null && cells.length == 1 && location != null)
			{
				target = getCellAt(location.x, location.y);

				if (target instanceof mxICell && cells[0] instanceof mxICell)
				{
					mxICell targetCell = (mxICell) target;
					mxICell dropCell = (mxICell) cells[0];

					if (targetCell.isVertex() == dropCell.isVertex()
							|| targetCell.isEdge() == dropCell.isEdge())
					{
						mxIGraphModel model = graph.getModel();
						model.setStyle(target, model.getStyle(cells[0]));
						graph.setSelectionCell(target);

						return null;
					}
				}
			}

			return super.importCells(cells, dx, dy, target, location);
		}
		 */

	}

	/**
	 * A graph that creates new edges from a given template edge.
	 */
	public static class ProgramGraph extends mxGraph {
		private final Program program;
		private EditorToolBar toolBar;

		/**
		 * Custom graph that defines the alternate edge style to be used when
		 * the middle control point of edges is double clicked (flipped).
		 */
		public ProgramGraph (Program program) {
			this.program = program;
			setGridEnabled(false);
			setAllowDanglingEdges(true);  // otherwise drag&drop of edge template effectively not be used
			setMultigraph(false);
//			setAlternateEdgeStyle("edgeStyle=mxEdgeStyle.ElbowConnector;elbow=vertical");  // not used by our overridden version flipEdge

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
					if (obj instanceof mxCell) {
						mxCell cell = (mxCell) obj;
						Object value = cell.getValue();
						if (value instanceof GraphCellConfigurator)
							((GraphCellConfigurator) value).resizeExisting(this, cell);
					}
				}
			});

		}

		void setToolBar (EditorToolBar toolBar) { this.toolBar = toolBar; }

		public Program getProgram () { return program; }

		// @Override
		public Object[] cloneCells_deactivated (Object[] cells, boolean allowInvalidEdges) {
			Object[] clones =  super.cloneCells(cells, allowInvalidEdges);
			int index = 0;
			for (Object clone: clones) {
				if (clone instanceof mxCell) {
					mxCell cell = (mxCell) clone;
					Object value = cell.getValue();
					LanguageEntity duplicatedValue = null;
					if (value instanceof EntityCreator) {
						duplicatedValue = ((EntityCreator<?>) value).create();
					}
					else if (value instanceof LanguageEntity && ((mxCell) cells[index]).getParent() != null)
						// TODO duplicate() the value only, if original cell is part of the graph
						duplicatedValue = ((LanguageEntity) value).duplicate();
					if (duplicatedValue != null) {
						cell.setValue(duplicatedValue);
						duplicatedValue.setOwningCell(cell);
					}
				}
				index += 1;
			}
			return clones;
		}

		@Override
		public void cellsAdded (Object[] cells, Object parent, Integer index, Object source, Object target,
								boolean absolute, boolean constrain) {
			for (Object obj: cells) {
				if (obj instanceof mxCell) {
					mxCell cell = (mxCell) obj;
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
						mxCell owner = entityValue.getOwningCell();
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


		private enum EdgeStyles implements DisplayNamed {
			DEFAULT("Orthogonal", ""),
			ELBOW_VERTICAL("Elbow, vertical", ";edgeStyle=elbowEdgeStyle;elbow=vertical"),
			ELBOW_HORIZONTAL("Elbow, horizontal", ";edgeStyle=elbowEdgeStyle;elbow=horizontal"),
			RELATION("Relation", ";edgeStyle=entityRelationEdgeStyle"),
			TOP_TO_BOTTOM("Top-to-bottom", ";edgeStyle=topToBottomEdgeStyle"),
			SIDE_TO_SIDE("Side-to-side", ";edgeStyle=sideToSideEdgeStyle");
			// ";edgeStyle=segmentEdgeStyle",  // segment edges seem to allow additional control points,
			// but how to edit them is unclear and toggling to some other edge style seems to leave garbage control points behind
			// ";edgeStyle=loopEdgeStyle"  // this is applied automatically for loops?

			private String displayName, edgeStyle;

			EdgeStyles (String displayName, String edgeStyle) {
				this.displayName = displayName;
				this.edgeStyle = edgeStyle;
			}

			static EdgeStyles getNextStyle (String cellStyle) {
				int startPos = cellStyle.indexOf(";edgeStyle");
				String oldEdgeStyle;
				if (startPos < 0) // DEFAULT
					oldEdgeStyle = "";
				else
					oldEdgeStyle = cellStyle.substring(startPos);
				boolean exitNextRound = false;
				for (EdgeStyles style: values())
					if (exitNextRound)
						return style;
					else if (style.edgeStyle.equals(oldEdgeStyle))
						exitNextRound = true;
				if (exitNextRound)  // last style in list --> default (first style)
					return DEFAULT;
				throw new IllegalStateException("unknown edge style in " + cellStyle);
			}

			String updateCellStyle (String cellStyle) {
				int startPos = cellStyle.indexOf(";edgeStyle");
				if (startPos >= 0)
					return cellStyle.substring(0, startPos) + edgeStyle;
				else
					return cellStyle + edgeStyle;
			}

			@Override
			public String getDisplayName () { return displayName; }
		}

		@Override
		public Object flipEdge (Object edge) {
			if (edge != null) {
				model.beginUpdate();
				try {
					String cellStyle = model.getStyle(edge);
					EdgeStyles nextStyle = EdgeStyles.getNextStyle(cellStyle);
					// System.err.println("flipEdge: current = " + cellStyle);
					// System.err.println("flipEdge: next = " + nextStyle);
					model.setStyle(edge, nextStyle.updateCellStyle(cellStyle));
					// Removes all existing control points
					resetEdge(edge);
					fireEvent(new mxEventObject(mxEvent.FLIP_EDGE, "edge", edge, "style", nextStyle));
				}
				finally {
					model.endUpdate();
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
					return (con.isValidSynapseSource() || con.isValidProbeSource()) && super.isValidSource(cell);
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
					return (con.isValidSynapseTarget() || con.isValidProbeTarget()) && super.isValidSource(cell);
				}
			}
			return super.isValidSource(cell);  // only the isValidSource method implements the general (direction-agnostic) checks
		}

		@Override
		public boolean isValidConnection (Object edge, Object source, Object target) {
			if (super.isValidConnection(edge, source, target)) {
				if (edge instanceof mxICell && source instanceof mxICell && target instanceof mxICell) {
					Object edgeValue = ((mxICell) edge).getValue();
					Object sourceValue = ((mxICell) source).getValue();
					Object targetValue = ((mxICell) target).getValue();
					if (edgeValue instanceof AnyConnection &&
							sourceValue instanceof Connectable && targetValue instanceof Connectable) {
						Connectable sourceCon = (Connectable) sourceValue;
						Connectable targetCon = (Connectable) targetValue;
						if (edgeValue instanceof ProbeConnection)
							return sourceCon.isValidProbeSource() && targetCon.isValidProbeTarget();
						else
							return sourceCon.isValidSynapseSource() && targetCon.isValidSynapseTarget();
					}
				}
				return true;
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
					if (sourceCon.isValidProbeSource() && !sourceCon.isValidSynapseSource()) {
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

	public static void main (String[] args) {
		// FIXME use SwingUtilities.invokeLater?
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception excp) {
			excp.printStackTrace(System.err);
		}

		mxSwingConstants.SHADOW_COLOR = Color.LIGHT_GRAY;
		mxConstants.W3C_SHADOWCOLOR = "#D3D3D3";

		try {  // CheerpJ Debugging
			NeuroCoCoonEditor editor = new NeuroCoCoonEditor();
			editor.initialize();  // moved from constructor so that subclass object fields have been initialized already
			JFrame frame = editor.createFrame(new EditorMenuBar(editor), 1200, 800);
			frame.setVisible(true);
		}
		catch (Throwable t) {
			t.printStackTrace(System.err);
		}
	}
}
