/*
 * Copyright (c) 2006-2012, JGraph Ltd */
package de.unibi.hbp.ncc;

import com.mxgraph.io.mxCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxSwingConstants;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxResources;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphSelectionModel;
import de.unibi.hbp.ncc.editor.BasicGraphEditor;
import de.unibi.hbp.ncc.editor.EditorMenuBar;
import de.unibi.hbp.ncc.editor.EditorPalette;
import de.unibi.hbp.ncc.editor.EditorToolBar;
import de.unibi.hbp.ncc.editor.TooltipProvider;
import de.unibi.hbp.ncc.editor.props.DetailsEditor;
import de.unibi.hbp.ncc.editor.props.MasterDetailsEditor;
import de.unibi.hbp.ncc.editor.props.Notificator;
import de.unibi.hbp.ncc.lang.Connectable;
import de.unibi.hbp.ncc.lang.DataPlot;
import de.unibi.hbp.ncc.editor.EntityCreator;
import de.unibi.hbp.ncc.lang.GraphCellConfigurator;
import de.unibi.hbp.ncc.lang.LanguageEntity;
import de.unibi.hbp.ncc.lang.modules.ModuleExample;
import de.unibi.hbp.ncc.lang.NeuronConnection;
import de.unibi.hbp.ncc.lang.NeuronType;
import de.unibi.hbp.ncc.lang.PoissonSource;
import de.unibi.hbp.ncc.lang.Program;
import de.unibi.hbp.ncc.lang.RegularSpikeSource;
import de.unibi.hbp.ncc.lang.Scope;
import de.unibi.hbp.ncc.lang.StandardPopulation;
import de.unibi.hbp.ncc.lang.SynapseType;
import de.unibi.hbp.ncc.lang.modules.SynfireChain;
import de.unibi.hbp.ncc.lang.modules.WinnerTakeAll;
import org.w3c.dom.Document;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import java.awt.Color;
import java.util.Collection;

public class NeuroCoCoonEditor extends BasicGraphEditor
{

	public static final String VERSION = "0.9.0";

	private ProgramGraphComponent programGraphComponent;
	private DetailsEditor detailsEditor;
	private MasterDetailsEditor<NeuronType> neuronTypeEditor;
	private MasterDetailsEditor<SynapseType> synapseTypeEditor;
	private MasterDetailsEditor<DataPlot> dataPlotEditor;

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

		// Adds some template cells for dropping into the graph
		// System.err.println("NeuroCoCoonEditor.initialize: palette");

		basicPalette.addTemplate(RegularSpikeSource.CREATOR);
		basicPalette.addTemplate(PoissonSource.CREATOR);
		basicPalette.addTemplate(StandardPopulation.CREATOR);
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

	@Override
	protected void addInspectorTabs (JTabbedPane inspector) {
		// System.err.println("NeuroCoCoonEditor.addInspectorTabs: enter");
		detailsEditor = new DetailsEditor();
		inspector.addTab("Inspector", detailsEditor.getComponent());
		Scope global = programGraphComponent.getProgramGraph().getProgram().getGlobalScope();
		neuronTypeEditor = new MasterDetailsEditor<>(global.getNeuronTypes(), NeuronType::new, this);
		inspector.addTab("Neurons", neuronTypeEditor.getComponent());
		synapseTypeEditor = new MasterDetailsEditor<>(global.getSynapseTypes(), SynapseType::new, this);
		inspector.addTab("Synapses", synapseTypeEditor.getComponent());
		dataPlotEditor = new MasterDetailsEditor<>(global.getDataPlots(), DataPlot::new, this);
		inspector.addTab("Plots", dataPlotEditor.getComponent());
		super.addInspectorTabs(inspector);
	}

	public static class ProgramGraphComponent extends mxGraphComponent
	{
		private final ProgramGraph programGraph;

		public ProgramGraphComponent (ProgramGraph graph)
		{
			super(graph);
			programGraph = graph;

			// Sets switches typically used in an editor
			setPageVisible(false);
			// setGridVisible(false);
			// menu item for grid is based on gridEnabled property of graph
			setToolTips(true);
			getConnectionHandler().setCreateTarget(false);

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
	public static class ProgramGraph extends mxGraph
	{
		private final Program program;
		private EditorToolBar toolBar;

		/**
		 * Custom graph that defines the alternate edge style to be used when
		 * the middle control point of edges is double clicked (flipped).
		 */
		public ProgramGraph (Program program)
		{
			program.setGraphModel(this.getModel());
			this.program = program;
			setGridEnabled(false);
			setAllowDanglingEdges(false);
			setMultigraph(false);
//			setAlternateEdgeStyle("edgeStyle=mxEdgeStyle.ElbowConnector;elbow=vertical");

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
					}
					else if (value instanceof LanguageEntity)
						duplicatedValue = ((LanguageEntity) value).duplicate();
					if (duplicatedValue != null) {
						cell.setValue(duplicatedValue);
						duplicatedValue.setOwningCell(cell);
					}
					// TODO do something similar with the (transitive) children of the cell?
				}
			}
			super.cellsAdded(cells, parent, index, source, target, absolute, constrain);
		}

		// Ports are not used as terminals for edges, they are
		// only used to compute the graphical connection point
		@Override
		public boolean isPort(Object cell)
		{
			mxGeometry geo = getCellGeometry(cell);
			return (geo != null) && geo.isRelative();
		}

		// can we get a red border when dragging to invalid target nodes?
		// not easily, com.mxgraph.swing.handler.mxCellMarker.getCell override in mxConnectionHandler.java
		// deliberately skips the red highlight, for validation errors with empty message strings
		// would need to override getEdgeValidationError to return some message for !isValidConnection to change this
		@Override
		public boolean isValidSource (Object cell) {
			if (cell instanceof mxICell) {
				Object value = ((mxICell) cell).getValue();
				if (value instanceof Connectable)
					return ((Connectable) value).isValidConnectionSource() && super.isValidSource(cell);
			}
			return super.isValidSource(cell);
		}

		@Override
		public boolean isValidTarget (Object cell) {
			if (cell instanceof mxICell) {
				Object value = ((mxICell) cell).getValue();
				if (value instanceof Connectable)
					return ((Connectable) value).isValidConnectionTarget() && super.isValidSource(cell);
			}
			return super.isValidSource(cell);  // only the isValidSource method implements the general (direction-agnostic) checks
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

			SynapseType synapseType = toolBar.getCurrentSynapseType();

			mxCell edge = (mxCell) super.createEdge(parent, id, value, source, target, synapseType.getEdgeStyle());
			edge.setValue(new NeuronConnection(synapseType));
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
