package de.unibi.hbp.ncc.editor;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxGraphActions;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxResources;
import com.mxgraph.view.mxGraphView;
import de.unibi.hbp.ncc.NeuroCoCoonEditor;
import de.unibi.hbp.ncc.editor.EditorActions.HistoryAction;
import de.unibi.hbp.ncc.editor.EditorActions.NewAction;
import de.unibi.hbp.ncc.editor.EditorActions.OpenAction;
import de.unibi.hbp.ncc.editor.EditorActions.SaveAction;
import de.unibi.hbp.ncc.editor.props.ComboBoxModelAdapter;
import de.unibi.hbp.ncc.env.JavaScriptBridge;
import de.unibi.hbp.ncc.env.NmpiClient;
import de.unibi.hbp.ncc.lang.Namespace;
import de.unibi.hbp.ncc.lang.SynapseType;
import org.jetbrains.annotations.NotNull;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.TransferHandler;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class EditorToolBar extends JToolBar
{
	private static final long serialVersionUID = -8015443128436394471L;

	private boolean ignoreZoomChange = false;

	private final Action checkAction;
	private final JLabel jobStatusDisplay;
	private NmpiClient.Platform currentPlatform;
	private SynapseType currentSynapseType;
	private JComboBox<SynapseType> synapseTypeComboBox;

	private static final String IMAGE_PATH = "images/"; // "/de/unibi/hbp/ncc/editor/images/";  // "../images/";

	public EditorToolBar(final BasicGraphEditor editor, int orientation)
	{
		super(orientation);
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(3, 3, 3, 3), getBorder()));
		setFloatable(false);

		add(editor.bind("New", new NewAction(), IMAGE_PATH + "new.gif"));
		add(editor.bind("Open", new OpenAction(), IMAGE_PATH + "open.gif"));
		add(editor.bind("Save", new SaveAction(false), IMAGE_PATH + "save.gif"));

		addSeparator();

		add(editor.bind("Cut", TransferHandler.getCutAction(), IMAGE_PATH + "cut.gif"));
		add(editor.bind("Copy", TransferHandler.getCopyAction(), IMAGE_PATH + "copy.gif"));
		add(editor.bind("Paste", TransferHandler.getPasteAction(), IMAGE_PATH + "paste.gif"));

		addSeparator();

		add(editor.bind("Delete", mxGraphActions.getDeleteAction(), IMAGE_PATH + "delete.gif"));

		addSeparator();

		add(editor.bind("Undo", new HistoryAction(true), IMAGE_PATH + "undo.gif"));
		add(editor.bind("Redo", new HistoryAction(false), IMAGE_PATH + "redo.gif"));

		addSeparator();

		final mxGraphView view = editor.getGraphComponent().getGraph()
				.getView();
		final JComboBox<String> zoomCombo = new JComboBox<>(new String[] {
				"400%", "200%", "150%", "100%", "75%", "50%",
				/* mxResources.get("height"), mxResources.get("width"), */
				mxResources.get("actualSize") });
		zoomCombo.setEditable(true);
		zoomCombo.setMinimumSize(new Dimension(75, 0));
		zoomCombo.setPreferredSize(new Dimension(75, 0));
		zoomCombo.setMaximumSize(new Dimension(75, 100));
		zoomCombo.setMaximumRowCount(9);
		add(zoomCombo);

		// Sets the zoom in the zoom combo the current value
		mxIEventListener scaleTracker = new mxIEventListener() {
			public void invoke(Object sender, mxEventObject evt)
			{
				ignoreZoomChange = true;
				try {
					zoomCombo.setSelectedItem((int) Math.round(100 * view.getScale()) + "%");
				}
				finally {
					ignoreZoomChange = false;
				}
			}
		};

		// Installs the scale tracker to update the value in the combo box
		// if the zoom is changed from outside the combo box
		view.getGraph().getView().addListener(mxEvent.SCALE, scaleTracker);
		view.getGraph().getView().addListener(mxEvent.SCALE_AND_TRANSLATE, scaleTracker);

		// Invokes once to sync with the actual zoom value
		scaleTracker.invoke(null, null);

		zoomCombo.addActionListener(e -> {
			mxGraphComponent graphComponent = editor.getGraphComponent();

			// zoomCombo is changed when the scale is changed in the diagram
			// but the change is ignored here
			if (!ignoreZoomChange)
			{
				Object selected = zoomCombo.getSelectedItem();
				if (selected != null) {
					String zoom = selected.toString();

/*
					if (zoom.equals(mxResources.get("height"))) {
//						graphComponent.setPageVisible(true);
						graphComponent.setZoomPolicy(mxGraphComponent.ZOOM_POLICY_HEIGHT);
					}
					else if (zoom.equals(mxResources.get("width"))) {
//						graphComponent.setPageVisible(true);
						graphComponent.setZoomPolicy(mxGraphComponent.ZOOM_POLICY_WIDTH);
					}
					else
*/
					if (zoom.equals(mxResources.get("actualSize"))) {
						graphComponent.zoomActual();
					}
					else {
						try {
							zoom = zoom.replace("%", "");
							double scale = Math.min(16, Math.max(0.01, Double.parseDouble(zoom) / 100));
							graphComponent.zoomTo(scale, graphComponent.isCenterZoom());
						}
						catch (NumberFormatException ex) {
							Dialogs.error(editor instanceof NeuroCoCoonEditor ? (NeuroCoCoonEditor) editor : null, ex);
						}
					}
				}
			}
		});
		addSeparator();

		NeuroCoCoonEditor.ProgramGraph programGraph = (NeuroCoCoonEditor.ProgramGraph) editor.getGraphComponent().getGraph();
		Namespace<SynapseType> synapseTypes = programGraph.getProgram().getGlobalScope().getSynapseTypes();

		final JComboBox<SynapseType> synapseTypeCombo =
				new JComboBox<>(new ComboBoxModelAdapter<>(synapseTypes.getListModel()));
		synapseTypeCombo.setEditable(false);
		synapseTypeCombo.setMinimumSize(new Dimension(120, 0));
		synapseTypeCombo.setPreferredSize(new Dimension(180, 0));
		synapseTypeCombo.setMaximumSize(new Dimension(240, 100));
		currentSynapseType = synapseTypeCombo.getModel().getElementAt(0);
		synapseTypeCombo.setSelectedItem(currentSynapseType);
		synapseTypeCombo.addActionListener(e -> {
			Object selected = synapseTypeCombo.getSelectedItem();
			if (selected instanceof SynapseType)
				currentSynapseType = (SynapseType) selected;
			// TODO should we change all currently selected edges to this type? and update this combobox to the type of a selected edge?
		});
		synapseTypeComboBox = synapseTypeCombo;
		add(new JLabel("New Synapses:"));
		add(synapseTypeCombo);
		addSeparator();

		checkAction = editor.bind("Check", new CheckAction(), IMAGE_PATH + "check.png");
		add(checkAction);
		RunAction runAction = new RunAction();
		add(runAction.setWrapperAction(editor.bind("Run", runAction, IMAGE_PATH + "run.png")));  // FIXME disabled just for testing
		final JComboBox<NmpiClient.Platform> platformCombo =
				new PartialComboBoxModel<NmpiClient.Platform>(NmpiClient.Platform.values()) {
					@Override
					protected boolean isAllowed (@NotNull Object value) {
						return value instanceof NmpiClient.Platform &&
								(JavaScriptBridge.isWebPlatform()
								? ((NmpiClient.Platform) value).worksInWebApp()
								: ((NmpiClient.Platform) value).worksOutsideWebApp());
					}
				}.buildComboBox();
		platformCombo.setEditable(false);
		platformCombo.setMinimumSize(new Dimension(100, 0));
		platformCombo.setPreferredSize(new Dimension(140, 0));
		platformCombo.setMaximumSize(new Dimension(140, 100));
		currentPlatform = JavaScriptBridge.isWebPlatform() ? NmpiClient.Platform.SPINNAKER : NmpiClient.Platform.NEST;
		platformCombo.setSelectedItem(currentPlatform);
		platformCombo.addActionListener(e -> {
			Object selected = platformCombo.getSelectedItem();
			if (selected instanceof NmpiClient.Platform)
				currentPlatform = (NmpiClient.Platform) selected;
		});
		add(platformCombo);

		addSeparator();

		final JLabel jobStatus = new JLabel();
		jobStatusDisplay = jobStatus;
		setJobStatusInToolBar(StatusLevel.PLACEHOLDER, "No Job");
		jobStatus.setForeground(Color.GRAY);
		jobStatus.addMouseListener(
				new MouseAdapter() {
					@Override
					public void mouseClicked (MouseEvent e) {
						if (!runAction.showLastOutput())
							getToolkit().beep();
					}
				}
		);
		add(jobStatus);
	}

	public NmpiClient.Platform getCurrentPlatform () { return currentPlatform; }

	public SynapseType getCurrentSynapseType () { return currentSynapseType; }

	public void setCurrentSynapseType (SynapseType synapseType) {
		currentSynapseType = synapseType;
		synapseTypeComboBox.setSelectedItem(synapseType);
	}

	public enum StatusLevel {
		PLACEHOLDER(Color.GRAY), NEUTRAL(Color.BLACK),
		GOOD(new Color(0x008800)), BAD(new Color(0x880000));

		private Color textColor;

		StatusLevel (Color c) {textColor = c; }

		Color getTextColor () { return textColor; }
	}

	public void setJobStatusInToolBar (StatusLevel level, String statusText) {
		jobStatusDisplay.setText(statusText);
		jobStatusDisplay.setForeground(level.getTextColor());
	}

	private static ImageIcon loadIcon (String fileName) {
		return new ImageIcon(EditorToolBar.class.getResource(IMAGE_PATH + fileName));
	}

	private final static ImageIcon CHECK_MARK_ICON = loadIcon("check.png");
	private final static ImageIcon PROBLEM_ICON = loadIcon("problem.png");

	public void setProblemStatus (boolean anyProblems) {
		checkAction.putValue(Action.SMALL_ICON, anyProblems ? PROBLEM_ICON : CHECK_MARK_ICON);
	}
}
