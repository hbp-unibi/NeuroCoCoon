/*
 * Copyright (c) 2001-2012, JGraph Ltd
 */
package de.unibi.hbp.ncc.editor;

import com.mxgraph.analysis.mxDistanceCostFunction;
import com.mxgraph.analysis.mxGraphAnalysis;
import com.mxgraph.canvas.mxICanvas;
import com.mxgraph.canvas.mxSvgCanvas;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.handler.mxConnectionHandler;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.mxGraphOutline;
import com.mxgraph.swing.util.mxGraphActions;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxCellRenderer.CanvasFactory;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxDomUtils;
import com.mxgraph.util.mxResources;
import com.mxgraph.util.mxUtils;
import com.mxgraph.util.mxXmlUtils;
import com.mxgraph.util.png.mxPngEncodeParam;
import com.mxgraph.util.png.mxPngImageEncoder;
import com.mxgraph.util.png.mxPngTextDecoder;
import com.mxgraph.view.mxGraph;
import de.unibi.hbp.ncc.NeuroCoCoonEditor;
import de.unibi.hbp.ncc.env.JavaScriptBridge;
import de.unibi.hbp.ncc.lang.NetworkModule;
import de.unibi.hbp.ncc.lang.serialize.ProgramCodec;
import org.w3c.dom.Document;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class EditorActions {

	public static NeuroCoCoonEditor getEditor (ActionEvent e) {
		if (e.getSource() instanceof Component) {
			Component component = (Component) e.getSource();

			while (component != null && !(component instanceof NeuroCoCoonEditor))
				component = component.getParent();

			return (NeuroCoCoonEditor) component;
		}
		return null;
	}

	@SuppressWarnings("serial")
	public static class ToggleRulersItem extends JCheckBoxMenuItem {

		public ToggleRulersItem (final BasicGraphEditor editor, String name) {
			super(name);
			setSelected(editor.getGraphComponent().getColumnHeader() != null);

			addActionListener(e -> {
				mxGraphComponent graphComponent = editor.getGraphComponent();

				if (graphComponent.getColumnHeader() != null) {
					graphComponent.setColumnHeader(null);
					graphComponent.setRowHeader(null);
				}
				else {
					graphComponent.setColumnHeaderView(new EditorRuler(graphComponent, EditorRuler.ORIENTATION_HORIZONTAL));
					graphComponent.setRowHeaderView(new EditorRuler(graphComponent, EditorRuler.ORIENTATION_VERTICAL));
				}
			});
		}
	}

	@SuppressWarnings("serial")
	public static class ToggleGridItem extends JCheckBoxMenuItem {

		public ToggleGridItem (final BasicGraphEditor editor, String name) {
			super(name);
			setSelected(editor.getGraphComponent().getGraph().isGridEnabled());

			addActionListener(e -> {
				mxGraphComponent graphComponent = editor.getGraphComponent();
				mxGraph graph = graphComponent.getGraph();
				boolean enabled = !graph.isGridEnabled();

				graph.setGridEnabled(enabled);
				graphComponent.setGridVisible(enabled);
				graphComponent.repaint();
				setSelected(enabled);
			});
		}
	}

	@SuppressWarnings("serial")
	public static class ToggleOutlineItem extends JCheckBoxMenuItem {

		public ToggleOutlineItem (final BasicGraphEditor editor, String name) {
			super(name);
			setSelected(editor.getGraphOutline().isVisible());

			addActionListener(e -> {
				final mxGraphOutline outline = editor.getGraphOutline();
				outline.setVisible(!outline.isVisible());
				outline.revalidate();

				SwingUtilities.invokeLater(() -> {
					if (outline.getParent() instanceof JSplitPane) {
						if (outline.isVisible()) {
							((JSplitPane) outline.getParent()).setDividerLocation(editor.getHeight() - 300);
							((JSplitPane) outline.getParent()).setDividerSize(6);
						}
						else {
							((JSplitPane) outline.getParent()).setDividerSize(0);
						}
					}
				});
			});
		}
	}

	private static boolean confirmIfModified (NeuroCoCoonEditor editor) {
		return editor != null && (!editor.isModified() || Dialogs.confirm(editor, mxResources.get("loseChanges")));
	}

	@SuppressWarnings("serial")
	public static class ExitAction extends AbstractAction {

		public void actionPerformed (ActionEvent e) {
			NeuroCoCoonEditor editor = getEditor(e);
			if (confirmIfModified(editor)) {
				editor.exit();
				System.exit(0);
			}
		}
	}

	@SuppressWarnings("serial")
	public static class GridStyleAction extends AbstractAction {

		protected int style;

		public GridStyleAction (int style) {
			this.style = style;
		}

		public void actionPerformed (ActionEvent e) {
			if (e.getSource() instanceof mxGraphComponent) {
				mxGraphComponent graphComponent = (mxGraphComponent) e.getSource();
				graphComponent.setGridStyle(style);
				graphComponent.repaint();
			}
		}
	}

	@SuppressWarnings("serial")
	public static class GridColorAction extends AbstractAction {

		public void actionPerformed (ActionEvent e) {
			if (e.getSource() instanceof mxGraphComponent) {
				mxGraphComponent graphComponent = (mxGraphComponent) e.getSource();
				Color newColor = JColorChooser.showDialog(graphComponent,
						mxResources.get("gridColor"), graphComponent.getGridColor());

				if (newColor != null) {
					graphComponent.setGridColor(newColor);
					graphComponent.repaint();
				}
			}
		}
	}

	@SuppressWarnings("serial")
	public static class ScaleAction extends AbstractAction {

		protected double scale;

		public ScaleAction (double scale)
		{
			this.scale = scale;
		}

		public void actionPerformed (ActionEvent e) {
			if (e.getSource() instanceof mxGraphComponent) {
				mxGraphComponent graphComponent = (mxGraphComponent) e.getSource();
				double scale = this.scale;

				if (scale == 0) {
					NeuroCoCoonEditor editor = getEditor(e);
					String value = (String) JOptionPane.showInputDialog(
							graphComponent, mxResources.get("value"),
							mxResources.get("scale") + " (%)",
							JOptionPane.PLAIN_MESSAGE,
							editor != null ? editor.getAppIcon() : null,
							null, "");

					if (value != null)
						scale = Double.parseDouble(value.replace("%", "")) / 100;
				}

				if (scale > 0)
					graphComponent.zoomTo(scale, graphComponent.isCenterZoom());
			}
		}
	}

	private static final String WEB_APP_HOME_DIR = "/files";

	private static String getHomeDirectory () {
		if (JavaScriptBridge.isWebPlatform())
			return WEB_APP_HOME_DIR;
		else
			return System.getProperty("user.dir");
	}

	@SuppressWarnings("serial")
	public static class SaveAction extends AbstractAction {

		protected boolean showDialog;

		protected static String lastDir = null;  // hack to make all Save/Save As actions share the same idea of the current directory
		// this is still different from the current directory for Open

		public SaveAction (boolean showDialog) {
			this.showDialog = showDialog;
		}

		/**
		 * Saves XML+PNG format.
		 */
		protected void saveXmlPng (NeuroCoCoonEditor editor, String filename, Color bg) throws IOException {
			mxGraphComponent graphComponent = editor.getGraphComponent();
			mxGraph graph = graphComponent.getGraph();

			// Creates the image for the PNG file
			BufferedImage image = mxCellRenderer.createBufferedImage(
					graph, null, 1, bg, graphComponent.isAntiAlias(), null, graphComponent.getCanvas());

			if (image != null) {
				// Creates the URL-encoded XML data
				ProgramCodec codec = new ProgramCodec(editor.getProgram(), true);
				String xml = URLEncoder.encode(mxXmlUtils.getXml(codec.encode(graph.getModel())), "UTF-8");
				codec.announceDone();
				mxPngEncodeParam param = mxPngEncodeParam.getDefaultEncodeParam(image);
				param.setCompressedText(new String[] { "mxGraphModel", xml });

				// Saves as a PNG file
				try (FileOutputStream outputStream = new FileOutputStream(new File(filename))) {
					mxPngImageEncoder encoder = new mxPngImageEncoder(outputStream, param);

					encoder.encode(image);
					editor.setModified(false);
					editor.setCurrentFile(new File(filename));
				}
			}
		}

		public void actionPerformed (ActionEvent e) {
			NeuroCoCoonEditor editor = getEditor(e);

			if (editor != null) {
				mxGraphComponent graphComponent = editor.getGraphComponent();
				mxGraph graph = graphComponent.getGraph();
				FileFilter selectedFilter = null;
				DefaultFileFilter nccFilter =
						new DefaultFileFilter(".ncc",
											  "NeuroCoCoon " + mxResources.get("file") + " (.ncc)");
				DefaultFileFilter xmlPngFilter =
						new DefaultFileFilter(".png",
											  "PNG+XML " + mxResources.get("file") + " (.png)");
				String filename = null;
				boolean dialogShown = false;

				if (showDialog || editor.getCurrentFile() == null) {
					String wd;

					if (lastDir != null)
						wd = lastDir;
					else if (editor.getCurrentFile() != null)
						wd = editor.getCurrentFile().getParent();
					else
						wd = getHomeDirectory();

					JFileChooser fc = new JFileChooser(wd);

					// Adds the default file format
					fc.addChoosableFileFilter(nccFilter);

					// Adds special vector graphics formats and HTML
					fc.addChoosableFileFilter(xmlPngFilter);
					fc.addChoosableFileFilter(
							new DefaultFileFilter(".svg",
												  "SVG " + mxResources.get("file") + " (.svg)"));

					// Adds a filter for each supported image format
					// Finds all distinct extensions
					Set<String> formats = new TreeSet<>();

					for (String imageFormat: ImageIO.getReaderFormatNames())
						formats.add(imageFormat.toLowerCase());


					for (String ext: formats)
						fc.addChoosableFileFilter(
								new DefaultFileFilter("." + ext,
													  ext.toUpperCase() + " " + mxResources.get("file") +
															  " (." + ext + ")"));

					// Adds filter that accepts all supported image formats
					fc.addChoosableFileFilter(new DefaultFileFilter.ImageFileFilter(mxResources.get("allImages")));
					fc.setFileFilter(nccFilter);
					int rc = fc.showDialog(null, mxResources.get("save"));
					dialogShown = true;

					if (rc != JFileChooser.APPROVE_OPTION)
						return;
					lastDir = fc.getSelectedFile().getParent();

					filename = fc.getSelectedFile().getAbsolutePath();
					selectedFilter = fc.getFileFilter();

					if (selectedFilter instanceof DefaultFileFilter) {
						String ext = ((DefaultFileFilter) selectedFilter).getExtension();
						if (!filename.toLowerCase().endsWith(ext))
							filename += ext;
					}

					if (new File(filename).exists()
							&& !Dialogs.confirm(editor, mxResources.get("overwriteExistingFile")))
						return;
				}
				else
					filename = editor.getCurrentFile().getAbsolutePath();

				try {
					String ext = filename.substring(filename.lastIndexOf('.') + 1);

					if (ext.equalsIgnoreCase("svg")) {
						mxSvgCanvas canvas =
								(mxSvgCanvas) mxCellRenderer.drawCells(
										graph, null, 1, null,
										new CanvasFactory() {
											public mxICanvas createCanvas (int width, int height) {
												mxSvgCanvas canvas = new mxSvgCanvas(mxDomUtils.createSvgDocument(width, height));
												canvas.setEmbedded(true);

												return canvas;
											}
										});

						mxUtils.writeFile(mxXmlUtils.getXml(canvas.getDocument()), filename);
					}
					else if (ext.equalsIgnoreCase("ncc") || ext.equalsIgnoreCase("xml")) {
						ProgramCodec codec = new ProgramCodec(editor.getProgram(), true);
						String xml = mxXmlUtils.getXml(codec.encode(graph.getModel()));
						codec.announceDone();

						mxUtils.writeFile(xml, filename);

						editor.setModified(false);
						editor.setCurrentFile(new File(filename));
					}
					else {
						Color bg = null;

						if ((!ext.equalsIgnoreCase("gif") && !ext.equalsIgnoreCase("png")) ||
								!Dialogs.confirm(editor, mxResources.get("transparentBackground")))
							bg = graphComponent.getBackground();

						if (selectedFilter == xmlPngFilter ||
								(editor.getCurrentFile() != null && ext.equalsIgnoreCase("png") && !dialogShown))
							saveXmlPng(editor, filename, bg);
						else {
							BufferedImage image = mxCellRenderer
									.createBufferedImage(graph, null, 1, bg,
											graphComponent.isAntiAlias(), null, graphComponent.getCanvas());

							if (image != null)
								ImageIO.write(image, ext, new File(filename));
							else
								Dialogs.error(editor, mxResources.get("noImageData"));
						}
					}
				}
				catch (Throwable ex) {
					ex.printStackTrace(System.err);
					Dialogs.error(editor, ex);
				}
			}
		}
	}

	@SuppressWarnings("serial")
	public static class SelectShortestPathAction extends AbstractAction {

		protected boolean directed;

		public SelectShortestPathAction (boolean directed)
		{
			this.directed = directed;
		}

		public void actionPerformed (ActionEvent e) {
			if (e.getSource() instanceof mxGraphComponent) {
				mxGraphComponent graphComponent = (mxGraphComponent) e.getSource();
				mxGraph graph = graphComponent.getGraph();
				mxIGraphModel model = graph.getModel();

				Object source = null;
				Object target = null;

				Object[] cells = graph.getSelectionCells();

				for (Object cell: cells) {
					if (model.isVertex(cell)) {
						if (source == null)
							source = cell;
						else {
							target = cell;
							break;
						}
					}
				}

				if (source != null && target != null) {
					int steps = graph.getChildEdges(graph.getDefaultParent()).length;
					Object[] path = mxGraphAnalysis.getInstance().getShortestPath(graph, source, target,
																				  new mxDistanceCostFunction(), steps, directed);
					graph.setSelectionCells(path);
				}
				else
					Dialogs.error(getEditor(e), mxResources.get("noSourceAndTargetSelected"));
			}
		}
	}

	@SuppressWarnings("serial")
	public static class SelectSpanningTreeAction extends AbstractAction {

		protected boolean directed;

		public SelectSpanningTreeAction (boolean directed)
		{
			this.directed = directed;
		}

		public void actionPerformed (ActionEvent e) {
			if (e.getSource() instanceof mxGraphComponent) {
				mxGraphComponent graphComponent = (mxGraphComponent) e.getSource();
				mxGraph graph = graphComponent.getGraph();
				mxIGraphModel model = graph.getModel();

				Object parent = graph.getDefaultParent();
				Object[] cells = graph.getSelectionCells();

				for (Object cell: cells) {
					if (model.getChildCount(cell) > 0) {
						parent = cell;
						break;
					}
				}

				Object[] v = graph.getChildVertices(parent);
				Object[] mst = mxGraphAnalysis.getInstance().getMinimumSpanningTree(graph, v, new mxDistanceCostFunction(), directed);
				graph.setSelectionCells(mst);
			}
		}
	}

	@SuppressWarnings("serial")
	public static class ToggleConnectModeAction extends AbstractAction {

		public void actionPerformed (ActionEvent e) {
			if (e.getSource() instanceof mxGraphComponent) {
				mxGraphComponent graphComponent = (mxGraphComponent) e.getSource();
				mxConnectionHandler handler = graphComponent.getConnectionHandler();
				handler.setHandleEnabled(!handler.isHandleEnabled());
			}
		}
	}

	@SuppressWarnings("serial")
	public static class ToggleCreateTargetItem extends JCheckBoxMenuItem {

		public ToggleCreateTargetItem (final BasicGraphEditor editor, String name) {
			super(name);
			setSelected(true);

			addActionListener(e -> {
				mxGraphComponent graphComponent = editor.getGraphComponent();

				if (graphComponent != null) {
					mxConnectionHandler handler = graphComponent.getConnectionHandler();
					handler.setCreateTarget(!handler.isCreateTarget());
					setSelected(handler.isCreateTarget());
				}
			});
		}
	}

	@SuppressWarnings("serial")
	public static class PromptPropertyAction extends AbstractAction {

		protected Object target;

		protected String fieldName, message;

		public PromptPropertyAction(Object target, String message) {
			this(target, message, message);
		}

		public PromptPropertyAction(Object target, String message, String fieldName) {
			this.target = target;
			this.message = message;
			this.fieldName = fieldName;
		}

		public void actionPerformed(ActionEvent e) {
			if (e.getSource() instanceof Component) {
				NeuroCoCoonEditor editor = getEditor(e);
				try {
					Method getter = target.getClass().getMethod("get" + fieldName);
					Object current = getter.invoke(target);

					// TODO: Support other atomic types
					if (current instanceof Integer) {
						Method setter = target.getClass().getMethod("set" + fieldName, int.class);

						String value = (String) JOptionPane.showInputDialog(
								(Component) e.getSource(), "Value", message,
								JOptionPane.PLAIN_MESSAGE,
								editor != null ? editor.getAppIcon() : null, null, current);

						if (value != null)
							setter.invoke(target, Integer.parseInt(value));
					}
				}
				catch (NumberFormatException ex) {
					Dialogs.error(editor, "Invalid number!");
				}
				catch (ReflectiveOperationException ex) {
					ex.printStackTrace(System.err);
				}
			}

			// Repaints the graph component
			if (e.getSource() instanceof mxGraphComponent) {
				mxGraphComponent graphComponent = (mxGraphComponent) e.getSource();
				graphComponent.repaint();
			}
		}
	}

	@SuppressWarnings("serial")
	public static class TogglePropertyItem extends JCheckBoxMenuItem {

		public TogglePropertyItem(Object target, String name, String fieldName)
		{
			this(target, name, fieldName, false);
		}

		public TogglePropertyItem(Object target, String name, String fieldName, boolean refresh) {
			this(target, name, fieldName, refresh, null);
		}

		public TogglePropertyItem(final Object target, String name, final String fieldName, final boolean refresh,
								  ActionListener listener) {
			super(name);

			// Since action listeners are processed last to first we add the given
			// listener here which means it will be processed after the one below
			if (listener != null)
				addActionListener(listener);

			addActionListener(e -> execute(target, fieldName, refresh));

			PropertyChangeListener propertyChangeListener = evt -> {
				if (evt.getPropertyName().equalsIgnoreCase(fieldName))
					update(target, fieldName);
			};

			if (target instanceof mxGraphComponent)
				((mxGraphComponent) target).addPropertyChangeListener(propertyChangeListener);
			else if (target instanceof mxGraph)
				((mxGraph) target).addPropertyChangeListener(propertyChangeListener);

			update(target, fieldName);
		}

		public void update (Object target, String fieldName) {
			if (target != null && fieldName != null) {
				try {
					Method getter = target.getClass().getMethod("is" + fieldName);

					if (getter != null) {
						Object current = getter.invoke(target);

						if (current instanceof Boolean)
							setSelected((Boolean) current);
					}
				}
				catch (Exception e) {
					// ignore
				}
			}
		}

		public void execute(Object target, String fieldName, boolean refresh)
		{
			if (target != null && fieldName != null) {
				try {
					Method getter = target.getClass().getMethod("is" + fieldName);
					Method setter = target.getClass().getMethod("set" + fieldName, boolean.class);

					Object current = getter.invoke(target);

					if (current instanceof Boolean) {
						boolean value = !(Boolean) current;
						setter.invoke(target, value);
						setSelected(value);
					}

					if (refresh) {
						mxGraph graph = null;

						if (target instanceof mxGraph)
							graph = (mxGraph) target;
						else if (target instanceof mxGraphComponent)
							graph = ((mxGraphComponent) target).getGraph();

						if (graph != null)
							graph.refresh();
					}
				}
				catch (Exception e) {
					// ignore
				}
			}
		}
	}

	@SuppressWarnings("serial")
	public static class HistoryAction extends AbstractAction {

		protected boolean undo;

		public HistoryAction (boolean undo)
		{
			this.undo = undo;
		}

		public void actionPerformed (ActionEvent e) {
			BasicGraphEditor editor = getEditor(e);

			if (editor != null) {
				if (undo)
					editor.getUndoManager().undo();
				else
					editor.getUndoManager().redo();
			}
		}
	}

	public static class FlipModuleAction extends AbstractAction {

		public void actionPerformed (ActionEvent e) {

			mxGraph graph = mxGraphActions.getGraph(e);

			if (graph != null && !graph.isSelectionEmpty()) {
				Object[] cells = graph.getSelectionCells();
				if (cells != null && cells.length > 0) {
					graph.getModel().beginUpdate();
					try {
						for (Object obj: cells) {
							if (obj instanceof mxCell) {
								mxCell cell = (mxCell) obj;
								Object value = cell.getValue();
								if (value instanceof NetworkModule) {
									NetworkModule module = (NetworkModule) value;
									module.toggleLayoutOrientation(graph, cell);
								}
							}
						}
					}
					finally {
						graph.getModel().endUpdate();
					}
				}
			}
			else
				Dialogs.error(getEditor(e), mxResources.get("noCellSelected"));
		}
	}

	@SuppressWarnings("serial")
	public static class NewAction extends AbstractAction {

		public void actionPerformed (ActionEvent e) {
			NeuroCoCoonEditor editor = getEditor(e);

			if (confirmIfModified(editor)) {
				mxGraph graph = editor.getGraphComponent().getGraph();

				graph.getSelectionModel().clear();
				editor.getProgram().clear(editor.getEditorToolBar());
				mxCell root = new mxCell();
				root.insert(new mxCell());
				graph.getModel().setRoot(root);

				editor.clearResultsTab();
				editor.clearJobStatus();
				editor.setModified(false);
				editor.setCurrentFile(null);
				editor.getGraphComponent().zoomAndCenter();
			}
		}
	}

	private static final OpenAction openAction = new OpenAction();

	public static OpenAction getOpenAction () { return openAction; }

	@SuppressWarnings("serial")
	public static class OpenAction extends AbstractAction {
		protected String lastDir;

		private OpenAction () { }  // prevent creation of multiple instances with diverging lastDir values

		protected void resetEditor (NeuroCoCoonEditor editor) {
			editor.clearResultsTab();
			editor.clearJobStatus();
			editor.setModified(false);
			editor.getUndoManager().clear();
			editor.getGraphComponent().zoomAndCenter();
		}

		/**
		 * Reads XML+PNG format.
		 */
		protected void openXmlPng(NeuroCoCoonEditor editor, File file) throws IOException {
			Map<String, String> text = mxPngTextDecoder.decodeCompressedText(new FileInputStream(file));

			if (text != null) {
				String value = text.get("mxGraphModel");

				if (value != null) {
					Document document = mxXmlUtils.parseXml(URLDecoder.decode(value, "UTF-8"));
					if (document != null) {
						editor.getGraphComponent().getGraph().getSelectionModel().clear();
						editor.getProgram().clear(editor.getEditorToolBar());
						ProgramCodec codec = new ProgramCodec(editor.getProgram(), document, false);
						codec.decode(document.getDocumentElement(), editor.getGraphModel());
						codec.announceDone();
						editor.setCurrentFile(file);
						resetEditor(editor);

						return;
					}
				}
			}
			Dialogs.error(editor, mxResources.get("imageContainsNoDiagramData"));
		}

		public void openFile (NeuroCoCoonEditor editor, File file) {
			lastDir = file.getParent();

			try {
				String absPath = file.getAbsolutePath();
				if (absPath.toLowerCase().endsWith(".png"))
					openXmlPng(editor, file);
				else {
					Document document = mxXmlUtils.parseXml(mxUtils.readFile(absPath));
					if (document != null) {
						editor.getProgram().clear(editor.getEditorToolBar());
						ProgramCodec codec = new ProgramCodec(editor.getProgram(), document, false);
						codec.decode(document.getDocumentElement(), editor.getGraphModel());
						codec.announceDone();
						editor.setCurrentFile(file);

						resetEditor(editor);
					}
					else
						throw new IOException("Could not parse XML document!");
				}
			}
			catch (IOException ex) {
				ex.printStackTrace(System.err);
				Dialogs.error(editor, ex);
			}
		}

		public void actionPerformed (ActionEvent e) {
			NeuroCoCoonEditor editor = getEditor(e);

			if (confirmIfModified(editor)) {
				mxGraph graph = editor.getGraphComponent().getGraph();

				if (graph != null) {
					String wd = (lastDir != null) ? lastDir : getHomeDirectory();

					JFileChooser fc = new JFileChooser(wd);

					// Adds file filter for supported file format
					DefaultFileFilter nccFilter =
							new DefaultFileFilter(".ncc",
												  mxResources.get("allSupportedFormats") + " (.ncc, .png)") {

								public boolean accept (File file) {
									return super.accept(file) || file.getName().toLowerCase().endsWith(".png");
								}
							};
					fc.addChoosableFileFilter(nccFilter);

					fc.addChoosableFileFilter(new DefaultFileFilter(".ncc",
																	"NeuroCoCoon " + mxResources.get("file") + " (.ncc)"));
					fc.addChoosableFileFilter(new DefaultFileFilter(".png",
																	"PNG+XML " + mxResources.get("file") + " (.png)"));

					fc.setFileFilter(nccFilter);

					int rc = fc.showDialog(null, mxResources.get("openFile"));

					if (rc == JFileChooser.APPROVE_OPTION) {
						openFile(editor, fc.getSelectedFile());
					}
				}
			}
		}
	}

	@SuppressWarnings("serial")
	public static class SetLabelPositionAction extends AbstractAction {

		protected String labelPosition, alignment;

		public SetLabelPositionAction (String labelPosition, String alignment) {
			this.labelPosition = labelPosition;
			this.alignment = alignment;
		}

		public void actionPerformed (ActionEvent e) {
			mxGraph graph = mxGraphActions.getGraph(e);

			if (graph != null && !graph.isSelectionEmpty()) {
				graph.getModel().beginUpdate();
				try {
					// Checks the orientation of the alignment to use the correct constants
					if (labelPosition.equals(mxConstants.ALIGN_LEFT)
							|| labelPosition.equals(mxConstants.ALIGN_CENTER)
							|| labelPosition.equals(mxConstants.ALIGN_RIGHT)) {
						graph.setCellStyles(mxConstants.STYLE_LABEL_POSITION, labelPosition);
						graph.setCellStyles(mxConstants.STYLE_ALIGN, alignment);
					}
					else {
						graph.setCellStyles(mxConstants.STYLE_VERTICAL_LABEL_POSITION, labelPosition);
						graph.setCellStyles(mxConstants.STYLE_VERTICAL_ALIGN, alignment);
					}
				}
				finally {
					graph.getModel().endUpdate();
				}
			}
		}
	}

	@SuppressWarnings("serial")
	public static class AlignCellsAction extends AbstractAction {

		protected String align;

		public AlignCellsAction (String align)
		{
			this.align = align;
		}

		public void actionPerformed( ActionEvent e) {
			mxGraph graph = mxGraphActions.getGraph(e);

			if (graph != null && !graph.isSelectionEmpty())
				graph.alignCells(align);
		}
	}

	@SuppressWarnings("serial")
	public static class AutosizeAction extends AbstractAction {

		public void actionPerformed (ActionEvent e) {
			mxGraph graph = mxGraphActions.getGraph(e);

			if (graph != null && !graph.isSelectionEmpty()) {
				Object[] cells = graph.getSelectionCells();
				mxIGraphModel model = graph.getModel();

				model.beginUpdate();
				try {
					for (Object cell: cells)
						graph.updateCellSize(cell);
				}
				finally {
					model.endUpdate();
				}
			}
		}
	}

	@SuppressWarnings("serial")
	public static class BackgroundAction extends AbstractAction {

		public void actionPerformed (ActionEvent e) {
			if (e.getSource() instanceof mxGraphComponent) {
				mxGraphComponent graphComponent = (mxGraphComponent) e.getSource();
				Color newColor = JColorChooser.showDialog(graphComponent, mxResources.get("background"), null);

				if (newColor != null) {
					graphComponent.getViewport().setOpaque(true);
					graphComponent.getViewport().setBackground(newColor);
				}

				// Forces a repaint of the outline
				graphComponent.getGraph().repaint();
			}
		}
	}

}
