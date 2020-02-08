package de.unibi.hbp.ncc.editor;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class CheckAction extends AbstractAction {
   // FIXME implement this


   @Override
   public void actionPerformed (ActionEvent e) {
      // JOptionPane.showMessageDialog(null, "RunAction Source: " + e.getSource());
      BasicGraphEditor editor = EditorActions.getEditor(e);
      if (editor != null) {
         final EditorToolBar toolBar = editor.getEditorToolBar();
         toolBar.setJobStatus(EditorToolBar.StatusLevel.BAD, "CheckAction not implemented yet!");
      }
   }

}
