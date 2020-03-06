package de.unibi.hbp.ncc.editor;

import de.unibi.hbp.ncc.NeuroCoCoonEditor;
import de.unibi.hbp.ncc.env.NmpiClient;
import de.unibi.hbp.ncc.lang.Program;
import de.unibi.hbp.ncc.lang.codegen.ErrorCollector;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import java.awt.Component;
import java.awt.event.ActionEvent;

public class CheckAction extends AbstractAction {

   @Override
   public void actionPerformed (ActionEvent e) {
      // JOptionPane.showMessageDialog(null, "RunAction Source: " + e.getSource());
      NeuroCoCoonEditor editor = EditorActions.getEditor(e);
      if (editor!= null) {
         editor.status("Checking …");
         Program program = editor.getProgram();
         NmpiClient.Platform targetPlatform = editor.getEditorToolBar().getCurrentPlatform();
         ErrorCollector diagnostics = program.checkProgram(targetPlatform);
         editor.status(diagnostics.hasAnyWarnings()
                             ? "There were warnings."
                             : (diagnostics.hasAnyErrors() ? "There were errors." : "Success!"));
         if (diagnostics.hasAnyMessages()) {
            Component display = diagnostics.buildDisplayAndNavigationComponent(program.getGraphComponent());  // TODO should the scroll pane be added by the error collector?
            editor.setResultsTab("Problems", new JScrollPane(display), true);
         }
      }
   }

}
