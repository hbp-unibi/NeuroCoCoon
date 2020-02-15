/*
 * Copyright (c) 2006-2012, JGraph Ltd */
package de.unibi.hbp.ncc;

import com.mxgraph.io.mxCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxSwingConstants;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxResources;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphSelectionModel;
import de.unibi.hbp.ncc.editor.BasicGraphEditor;
import de.unibi.hbp.ncc.editor.EditorMenuBar;
import de.unibi.hbp.ncc.editor.EditorPalette;
import de.unibi.hbp.ncc.editor.EditorToolBar;
import de.unibi.hbp.ncc.editor.props.DetailsEditor;
import de.unibi.hbp.ncc.editor.props.MasterDetailsEditor;
import de.unibi.hbp.ncc.lang.EntityCreator;
import de.unibi.hbp.ncc.lang.LanguageEntity;
import de.unibi.hbp.ncc.lang.NeuronConnection;
import de.unibi.hbp.ncc.lang.NeuronPopulation;
import de.unibi.hbp.ncc.lang.NeuronType;
import de.unibi.hbp.ncc.lang.PoissonSource;
import de.unibi.hbp.ncc.lang.Program;
import de.unibi.hbp.ncc.lang.RegularSpikeSource;
import de.unibi.hbp.ncc.lang.Scope;
import de.unibi.hbp.ncc.lang.StandardPopulation;
import de.unibi.hbp.ncc.lang.SynapseType;
import org.w3c.dom.Document;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import java.awt.Color;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.List;

public class NeuroCoCoonEditor extends BasicGraphEditor
{

	public static final String VERSION = "0.9.0";

	/**
	 * Holds the shared number formatter.
	 * 
	 * @see NumberFormat#getInstance()
	 */
	public static final NumberFormat numberFormat = NumberFormat.getInstance();

	private ProgramGraphComponent programGraphComponent;
	private DetailsEditor detailsEditor;
	private MasterDetailsEditor<NeuronType> neuronTypeEditor;
	private MasterDetailsEditor<SynapseType> synapseTypeEditor;

	public NeuroCoCoonEditor ()
	{
		this("NeuroCoCoon Editor", new ProgramGraphComponent(new ProgramGraph(new Program())));
	}

	public NeuroCoCoonEditor (String appTitle, ProgramGraphComponent component) {
		super(appTitle, component);
		programGraphComponent = component;
	}

	public Program getProgram () { return programGraphComponent.getProgramGraph().getProgram(); }

	@Override
	public void initialize () {
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
							detailsEditor.setSubject(graphComponent, (LanguageEntity) value);
					}
				}
			}
		});

		// Adds some template cells for dropping into the graph

		basicPalette.addTemplate("Spikes",
								 new ImageIcon(NeuroCoCoonEditor.class.getResource("images/lang/spikesource.png")),
								 "spikesource",
								 100, 100, RegularSpikeSource.CREATOR);
		basicPalette.addTemplate("Poisson",
								 new ImageIcon(NeuroCoCoonEditor.class.getResource("images/lang/poissonsource.png")),
								 "poissonsource",
								 100, 80, PoissonSource.CREATOR);
		basicPalette.addTemplate("Population",
								 new ImageIcon(NeuroCoCoonEditor.class.getResource("images/lang/population.png")),
								 "population",
								 100, 60, StandardPopulation.CREATOR);
	}

	@Override
	protected void addInspectorTabs (JTabbedPane inspector) {
		detailsEditor = new DetailsEditor();
		inspector.addTab("Inspector", detailsEditor.getComponent());
		Scope global = programGraphComponent.getProgramGraph().getProgram().getGlobalScope();
		neuronTypeEditor = new MasterDetailsEditor<>(global.getNeuronTypes(),
													 ns -> new NeuronType(ns, null));
		inspector.addTab("Neurons", neuronTypeEditor.getComponent());
		synapseTypeEditor = new MasterDetailsEditor<>(global.getSynapseTypes(),
													  ns -> new SynapseType(ns, null, SynapseType.ConnectorKind.ALL_TO_ALL));
		inspector.addTab("Synapses", synapseTypeEditor.getComponent());
		inspector.addTab("Plots", new JLabel("Not implemented yet"));
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
			setGridVisible(false);
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
			setAlternateEdgeStyle("edgeStyle=mxEdgeStyle.ElbowConnector;elbow=vertical");
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
					if (value instanceof EntityCreator)
						duplicatedValue = ((EntityCreator<?>) value).create();
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

		/*
		@Override
		public Object[] cloneCells (Object[] cells, boolean allowInvalidEdges) {
			Object[] clones =  super.cloneCells(cells, allowInvalidEdges);
			for (Object clone: clones) {
				if (clone instanceof mxCell) {
					mxCell cell = (mxCell) clone;
					Object value = cell.getValue();
					if (value instanceof EntityCreator)
						value = ((EntityCreator<?>) value).create();
					else if (value instanceof NeuronPopulation)  // FIXME do this for all NamedEntities (or even LanguageEntities)
						value = ((NeuronPopulation) value).duplicate();
					cell.setValue(value);
					// TODO do something similar with the (transitive) children of the cell?
				}
			}
			return clones;
		}
*/

		/**
		 * Prints out some useful information about the cell in the tooltip.
		 */
		public String getToolTipForCell(Object cell)
		{
			StringBuilder tip = new StringBuilder("<html>");
			mxGeometry geo = getModel().getGeometry(cell);
			mxCellState state = getView().getState(cell);

			// tip += "token=" + JavaScriptBridge.getHBPToken() + "<br>";

			if (getModel().isEdge(cell))
			{
				tip.append("points={");

				if (geo != null)
				{
					List<mxPoint> points = geo.getPoints();

					if (points != null)
					{
						for (mxPoint point: points) {
							tip.append("[x=").append(numberFormat.format(point.getX()))
									.append(",y=").append(numberFormat.format(point.getY()))
									.append("],");
						}
						tip.deleteCharAt(tip.length() - 1);
					}
				}

				tip.append("}<br>")
						.append("absPoints={");

				if (state != null)
				{

					for (int i = 0; i < state.getAbsolutePointCount(); i++)
					{
						mxPoint point = state.getAbsolutePoint(i);
						tip.append("[x=").append(numberFormat.format(point.getX()))
								.append(",y=").append(numberFormat.format(point.getY()))
								.append("],");
					}
					tip.deleteCharAt(tip.length() - 1);
				}

				tip.append("}");
			}
			else
			{
				tip.append("geo=[");

				if (geo != null)
				{
					tip.append("x=").append(numberFormat.format(geo.getX()))
							.append(",y=").append(numberFormat.format(geo.getY()))
							.append(",width=").append(numberFormat.format(geo.getWidth()))
							.append(",height=").append(numberFormat.format(geo.getHeight()));
				}

				tip.append("]<br>")
						.append("state=[");

				if (state != null)
				{
					tip.append("x=").append(numberFormat.format(state.getX()))
							.append(",y=").append(numberFormat.format(state.getY()))
							.append(",width=").append(numberFormat.format(state.getWidth()))
							.append(",height=").append(numberFormat.format(state.getHeight()));
				}

				tip.append("]");
			}

			mxPoint trans = getView().getTranslate();

			tip.append("<br>scale=").append(numberFormat.format(getView().getScale()))
					.append(", translate=[x=").append(numberFormat.format(trans.getX()))
					.append(",y=").append(numberFormat.format(trans.getY())).append("]");
			tip.append("</html>");

			return tip.toString();
		}

		/**
		 * Overrides the method to use the currently selected edge template for
		 * new edges.
		 */
		public Object createEdge(Object parent, String id, Object value,
								 Object source, Object target, String style)
		{

			SynapseType synapseType = toolBar.getCurrentSynapseType();

			mxCell edge = (mxCell) super.createEdge(parent, id, value, source, target, synapseType.getEdgeStyle());
			edge.setValue(new NeuronConnection(synapseType));
			return edge;
		}


	}

	public static void main(String[] args)
	{
		// FIXME use SwingUtilities.invokeLater?
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}

		mxSwingConstants.SHADOW_COLOR = Color.LIGHT_GRAY;
		mxConstants.W3C_SHADOWCOLOR = "#D3D3D3";

		NeuroCoCoonEditor editor = new NeuroCoCoonEditor();
		editor.initialize();  // moved from constructor so that subclass object fields have been initialized already
		JFrame frame = editor.createFrame(new EditorMenuBar(editor), 1200, 800);
		frame.setVisible(true);
	}
}
