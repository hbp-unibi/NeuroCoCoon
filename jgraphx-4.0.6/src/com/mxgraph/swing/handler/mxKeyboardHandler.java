/**
 * Copyright (c) 2008, Gaudenz Alder
 */
package com.mxgraph.swing.handler;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxGraphActions;
import com.mxgraph.util.mxUtils;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * @author Administrator
 * 
 */
public class mxKeyboardHandler
{

	/**
	 * 
	 * @param graphComponent
	 */
	public mxKeyboardHandler(mxGraphComponent graphComponent)
	{
		installKeyboardActions(graphComponent);
	}

	/**
	 * Invoked as part from the boilerplate install block.
	 */
	protected void installKeyboardActions(mxGraphComponent graphComponent)
	{
		InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		SwingUtilities.replaceUIInputMap(graphComponent, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, inputMap);

		inputMap = getInputMap(JComponent.WHEN_FOCUSED);
		SwingUtilities.replaceUIInputMap(graphComponent, JComponent.WHEN_FOCUSED, inputMap);
		SwingUtilities.replaceUIActionMap(graphComponent, createActionMap());
	}

	protected static final int MENU_SHORTCUT_DOWN_MASK = mxUtils.IS_MAC ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK;

	/**
	 * Return JTree's input map.
	 */
	protected InputMap getInputMap(int condition)
	{
		InputMap map = null;

		if (condition == JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
		{
			map = (InputMap) UIManager.get("ScrollPane.ancestorInputMap");
		}
		else if (condition == JComponent.WHEN_FOCUSED)
		{
			map = new InputMap();

			// map.put(KeyStroke.getKeyStroke("F2"), "edit");
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "delete");
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "selectParent");
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, 0), "selectParent");
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "selectChild");
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, 0), "selectChild");
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "selectNext");
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, 0), "selectNext");
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "selectPrevious");
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, 0), "selectPrevious");
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), "enterGroup");
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), "exitGroup");
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), "home");
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, MENU_SHORTCUT_DOWN_MASK), "expand");
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, MENU_SHORTCUT_DOWN_MASK), "expand");
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, MENU_SHORTCUT_DOWN_MASK), "collapse");
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, MENU_SHORTCUT_DOWN_MASK), "collapse");
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, MENU_SHORTCUT_DOWN_MASK), "selectAll");
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, MENU_SHORTCUT_DOWN_MASK), "selectNone");
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, MENU_SHORTCUT_DOWN_MASK), "cut");
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_CUT, 0), "cut");
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, MENU_SHORTCUT_DOWN_MASK), "copy");
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_COPY, 0), "copy");
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, MENU_SHORTCUT_DOWN_MASK), "paste");
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_PASTE, 0), "paste");
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, MENU_SHORTCUT_DOWN_MASK), "group");
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, MENU_SHORTCUT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), "ungroup");
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, MENU_SHORTCUT_DOWN_MASK), "zoomIn");
			map.put(KeyStroke.getKeyStroke(KeyEvent.getExtendedKeyCodeForChar('+'), MENU_SHORTCUT_DOWN_MASK), "zoomIn");
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, MENU_SHORTCUT_DOWN_MASK), "zoomOut");
			map.put(KeyStroke.getKeyStroke(KeyEvent.getExtendedKeyCodeForChar('-'), MENU_SHORTCUT_DOWN_MASK), "zoomOut");
		}

		return map;
	}

	/**
	 * Return the mapping between JTree's input map and JGraph's actions.
	 */
	protected ActionMap createActionMap()
	{
		ActionMap map = (ActionMap) UIManager.get("ScrollPane.actionMap");

		// map.put("edit", mxGraphActions.getEditAction());
		map.put("delete", mxGraphActions.getDeleteAction());
		map.put("home", mxGraphActions.getHomeAction());
		map.put("enterGroup", mxGraphActions.getEnterGroupAction());
		map.put("exitGroup", mxGraphActions.getExitGroupAction());
		map.put("collapse", mxGraphActions.getCollapseAction());
		map.put("expand", mxGraphActions.getExpandAction());
		map.put("toBack", mxGraphActions.getToBackAction());
		map.put("toFront", mxGraphActions.getToFrontAction());
		map.put("selectNone", mxGraphActions.getSelectNoneAction());
		map.put("selectAll", mxGraphActions.getSelectAllAction());
		map.put("selectNext", mxGraphActions.getSelectNextAction());
		map.put("selectPrevious", mxGraphActions.getSelectPreviousAction());
		map.put("selectParent", mxGraphActions.getSelectParentAction());
		map.put("selectChild", mxGraphActions.getSelectChildAction());
		map.put("cut", TransferHandler.getCutAction());
		map.put("copy", TransferHandler.getCopyAction());
		map.put("paste", TransferHandler.getPasteAction());
		map.put("group", mxGraphActions.getGroupAction());
		map.put("ungroup", mxGraphActions.getUngroupAction());
		map.put("zoomIn", mxGraphActions.getZoomInAction());
		map.put("zoomOut", mxGraphActions.getZoomOutAction());

		return map;
	}

}
