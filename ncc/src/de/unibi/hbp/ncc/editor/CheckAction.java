package de.unibi.hbp.ncc.editor;

import de.unibi.hbp.ncc.NeuroCoCoonEditor;
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
   // FIXME implement this

   @Override
   public void actionPerformed (ActionEvent e) {
      // JOptionPane.showMessageDialog(null, "RunAction Source: " + e.getSource());
      BasicGraphEditor editor = EditorActions.getEditor(e);
      if (editor instanceof NeuroCoCoonEditor) {
         editor.status("Checking …");
         NeuroCoCoonEditor neuroCoCoonEditor = (NeuroCoCoonEditor) editor;
         Program program = neuroCoCoonEditor.getProgram();
         StringBuilder pythonCode;
         pythonCode = program.generatePythonCode();
         ErrorCollector diagnostics = program.getLastDiagnostics();
         if (pythonCode != null) {
            editor.status(diagnostics.hasAnyWarnings() ? "There were warnings." : "Success!");
            JFrame frame = new JFrame("Python Code");
            JTextArea textArea = new JTextArea(pythonCode.toString(), 25, 80);
            textArea.setEditable(false);
            frame.add(new JScrollPane(textArea));
            frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
         }
         else
            editor.status("There were errors.");
         if (diagnostics.hasAnyMessages()) {
            JFrame frame = new JFrame("Diagnostics");
            Component display = diagnostics.buildDisplayAndNavigationComponent();
            frame.add(new JScrollPane(display));  // TODO should the scroll pane be added by the error collector?
            frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
         }
      }
   }

}
