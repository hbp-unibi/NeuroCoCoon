/**
 * Copyright (c) 2008, Gaudenz Alder
 */
package de.unibi.hbp.ncc.editor;

import com.mxgraph.swing.handler.mxKeyboardHandler;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxGraphActions;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * @author Administrator
 * 
 */
public class EditorKeyboardHandler extends mxKeyboardHandler {

	/**
	 * 
	 * @param graphComponent
	 */
	public EditorKeyboardHandler(mxGraphComponent graphComponent)
	{
		super(graphComponent);
	}

	/**
	 * Return JTree's input map.
	 */
	protected InputMap getInputMap(int condition) {
		InputMap map = super.getInputMap(condition);

		if (condition == JComponent.WHEN_FOCUSED && map != null) {
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, MENU_SHORTCUT_DOWN_MASK), "new");
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, MENU_SHORTCUT_DOWN_MASK), "open");
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, MENU_SHORTCUT_DOWN_MASK), "save");
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, MENU_SHORTCUT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), "saveAs");
			map.put(KeyStroke.getKeyStroke(KeyEvent.getExtendedKeyCodeForChar('z'), MENU_SHORTCUT_DOWN_MASK), "undo");
			map.put(KeyStroke.getKeyStroke(KeyEvent.getExtendedKeyCodeForChar('z'), MENU_SHORTCUT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), "redo");

			// likely not directly useful in our case
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, MENU_SHORTCUT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), "selectVertices");
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, MENU_SHORTCUT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), "selectEdges");
		}

		return map;
	}

	/**
	 * Return the mapping between JTree's input map and JGraph's actions.
	 */
	protected ActionMap createActionMap() {
		ActionMap map = super.createActionMap();

		map.put("save", new EditorActions.SaveAction(false));
		map.put("saveAs", new EditorActions.SaveAction(true));
		map.put("new", new EditorActions.NewAction());
		map.put("open", new EditorActions.OpenAction());
		map.put("undo", new EditorActions.HistoryAction(true));
		map.put("redo", new EditorActions.HistoryAction(false));
		map.put("selectVertices", mxGraphActions.getSelectVerticesAction());
		map.put("selectEdges", mxGraphActions.getSelectEdgesAction());

		return map;
	}

}
