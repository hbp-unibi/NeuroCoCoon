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

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.TransferHandler;
import java.awt.Color;
import java.awt.Dimension;

public class EditorToolBar extends JToolBar
{
	private static final long serialVersionUID = -8015443128436394471L;

	private boolean ignoreZoomChange = false;

	private Action checkAction;
	private JLabel jobStatusDisplay;
	private NmpiClient.Platform currentPlatform;
	private SynapseType currentSynapseType;

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
							JOptionPane.showMessageDialog(editor, ex.getMessage());
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
		add(new JLabel("Synapses:"));
		add(synapseTypeCombo);
		addSeparator();

		checkAction = editor.bind("Check", new CheckAction(), IMAGE_PATH + "check.png");
		add(checkAction);
		add(editor.bind("Run", new RunAction(), IMAGE_PATH + "run.png"));
		final JComboBox<NmpiClient.Platform> platformCombo = new JComboBox<>(NmpiClient.Platform.values());
		platformCombo.setEditable(false);
		platformCombo.setMinimumSize(new Dimension(120, 0));
		platformCombo.setPreferredSize(new Dimension(120, 0));
		platformCombo.setMaximumSize(new Dimension(120, 100));
		currentPlatform = JavaScriptBridge.isWebPlatform() ? NmpiClient.Platform.SPINNAKER : NmpiClient.Platform.NEST;
		platformCombo.setSelectedItem(currentPlatform);
		platformCombo.addActionListener(e -> {
			Object selected = platformCombo.getSelectedItem();
			if (selected instanceof NmpiClient.Platform)
				currentPlatform = (NmpiClient.Platform) selected;
		});
		add(platformCombo);
		// TODO disable unavailable platforms
		/*
		1. Disallow selecting items which you want to be disabled

For this you can use a custom ComboBoxModel, and override its setSelectedItem() method to do nothing if the item to be selected is a disabled one:

class MyComboModel extends DefaultComboBoxModel<String> {
    public MyComboModel() {}
    public MyComboModel(Vector<String> items) {
        super(items);
    }
    @Override
    public void setSelectedItem(Object item) {
        if (item.toString().startsWith("**"))
            return;
        super.setSelectedItem(item);
    };
}
And you can set this new model by passing an instance of it to the JComboBox constructor:

JComboBox<String> cb = new JComboBox<>(new MyComboModel());
2. Display disabled items with different font

For this you have to use a custom ListCellRenderer and in getListCellRendererComponent() method you can configure different visual appearance for disabled and enabled items:

Font f1 = cb.getFont();
Font f2 = new Font("Tahoma", 0, 14);

cb.setRenderer(new DefaultListCellRenderer() {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof JComponent)
            return (JComponent) value;

        boolean itemEnabled = !value.toString().startsWith("**");

        super.getListCellRendererComponent(list, value, index,
                isSelected && itemEnabled, cellHasFocus);

        // Render item as disabled and with different font:
        setEnabled(itemEnabled);
        setFont(itemEnabled ? f1 : f2);

        return this;
    }
});
		 */

		addSeparator();

		final JLabel jobStatus = new JLabel();
		jobStatusDisplay = jobStatus;
		setJobStatus(StatusLevel.PLACEHOLDER, "No Job");
		jobStatus.setForeground(Color.GRAY);
		add(jobStatus);
	}

	public NmpiClient.Platform getCurrentPlatform () { return currentPlatform; }

	public SynapseType getCurrentSynapseType () { return currentSynapseType; }

	enum StatusLevel {
		PLACEHOLDER(Color.GRAY), NEUTRAL(Color.BLACK), GOOD(Color.GREEN.darker()), BAD(Color.RED.darker());

		private Color textColor;

		StatusLevel (Color c) {textColor = c; }

		Color getTextColor () { return textColor; }
	}

	public void setJobStatus (StatusLevel level, String statusText) {
		jobStatusDisplay.setText(statusText);
		jobStatusDisplay.setForeground(level.getTextColor());
	}

	public void setJobStatus (String statusText) {
		setJobStatus(StatusLevel.NEUTRAL, statusText);
	}

	public void setProblemStatus (boolean anyProblems) {
		checkAction.putValue(Action.SMALL_ICON, IMAGE_PATH + (anyProblems ? "problem.png" : "check.png"));
	}
}
