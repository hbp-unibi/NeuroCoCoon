package de.unibi.hbp.ncc.editor;

import com.mxgraph.swing.util.mxGraphActions;
import com.mxgraph.util.mxResources;
import de.unibi.hbp.ncc.editor.EditorActions.HistoryAction;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.TransferHandler;

public class EditorPopupMenu extends JPopupMenu
{

	private static final long serialVersionUID = -3132749140550242191L;

	public EditorPopupMenu(BasicGraphEditor editor)
	{
		boolean selected = !editor.getGraphComponent().getGraph().isSelectionEmpty();

		add(editor.bind(mxResources.get("undo"), new HistoryAction(true),
						"/de/unibi/hbp/ncc/editor/images/undo.gif"));

		addSeparator();

		add(editor.bind(mxResources.get("cut"), TransferHandler.getCutAction(),
						"/de/unibi/hbp/ncc/editor/images/cut.gif")).setEnabled(selected);
		add(editor.bind(mxResources.get("copy"), TransferHandler.getCopyAction(),
						"/de/unibi/hbp/ncc/editor/images/copy.gif")).setEnabled(selected);
		add(editor.bind(mxResources.get("paste"), TransferHandler.getPasteAction(),
						"/de/unibi/hbp/ncc/editor/images/paste.gif"));

		addSeparator();

		add(
				editor.bind(mxResources.get("delete"), mxGraphActions.getDeleteAction(),
							"/de/unibi/hbp/ncc/editor/images/delete.gif")).setEnabled(selected);

		addSeparator();

		/*
		// Creates the format menu
		JMenu menu = (JMenu) add(new JMenu(mxResources.get("format")));

		EditorMenuBar.populateFormatMenu(menu, editor);
		 */
		// Creates the shape menu
		JMenu menu = (JMenu) add(new JMenu(mxResources.get("shape")));

		EditorMenuBar.populateShapeMenu(menu, editor);

		addSeparator();

//		add(editor.bind(mxResources.get("edit"), mxGraphActions.getEditAction())).setEnabled(selected);

		addSeparator();

		add(editor.bind(mxResources.get("selectVertices"), mxGraphActions.getSelectVerticesAction()));
		add(editor.bind(mxResources.get("selectEdges"), mxGraphActions.getSelectEdgesAction()));

		addSeparator();

		add(editor.bind(mxResources.get("selectAll"), mxGraphActions.getSelectAllAction()));
	}

}
