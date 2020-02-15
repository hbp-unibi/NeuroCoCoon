package de.unibi.hbp.ncc.editor;

import de.unibi.hbp.ncc.NeuroCoCoonEditor;
import de.unibi.hbp.ncc.lang.Program;

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
         StringBuilder pythonCode = program.generatePythonCode();
         if (pythonCode != null) {
            editor.status("Success!");
            JFrame frame = new JFrame("Python Code");
            JTextArea textArea = new JTextArea(pythonCode.toString(), 25, 80);
            textArea.setEditable(false);
            frame.add(new JScrollPane(textArea));
            frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
         }
         else {
            editor.status("There were errors.");
            JFrame frame = new JFrame("Diagnostics");
            Component display = program.getLastDiagnostics().getDisplayAndNavigationComponent();
            frame.add(new JScrollPane(display));
            frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
         }
      }
   }

}
