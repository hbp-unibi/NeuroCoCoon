package de.unibi.hbp.ncc.editor;

import de.unibi.hbp.ncc.NeuroCoCoonEditor;
import de.unibi.hbp.ncc.env.NmpiClient;
import de.unibi.hbp.ncc.lang.Program;
import de.unibi.hbp.ncc.lang.codegen.ErrorCollector;
import de.unibi.hbp.ncc.lang.utils.Iterators;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

public class RunAction extends AbstractAction {

   private static int counter = 42;

   private static final String TOY_CODE =
         "import sys\n" +
               "print('Hello from NeuroCoCoon')\n" +
               "print(sys.version)\n";

   private static final int POLLING_INTERVAL = 5000;  // milliseconds

   private Timer trackJobStatus;

   @Override
   public void actionPerformed (ActionEvent e) {
      // JOptionPane.showMessageDialog(null, "RunAction Source: " + e.getSource());
      NeuroCoCoonEditor editor = EditorActions.getEditor(e);
      // JOptionPane.showMessageDialog(null, "RunAction Editor: " + editor);
      if (editor != null && trackJobStatus == null) {
         editor.status("Preparing …");
         Program program = editor.getProgram();
         NmpiClient.Platform targetPlatform = editor.getEditorToolBar().getCurrentPlatform();
         StringBuilder pythonCode;
         pythonCode = program.generatePythonCode(targetPlatform);
         ErrorCollector diagnostics = program.getLastDiagnostics();
         if (pythonCode != null) {
            editor.status(diagnostics.hasAnyWarnings() ? "There were warnings." : "Success!");
            if (diagnostics.hasAnyMessages()) {
               Component display = diagnostics.buildDisplayAndNavigationComponent(program.getGraphComponent());  // TODO should the scroll pane be added by the error collector?
               editor.setResultsTab("Messages", new JScrollPane(display), false);
            }
            if (targetPlatform == NmpiClient.Platform.SOURCE_CODE)
               showGeneratedCode(pythonCode);
            else if (targetPlatform == NmpiClient.Platform.NEST)
               simulateWithNEST(pythonCode, editor.getEditorToolBar());
            else
               simulateWithNMPI(pythonCode, targetPlatform, editor.getEditorToolBar());
         }
         else {
            editor.status("There were errors.");
            if (diagnostics.hasAnyErrors()) {
               Component display = diagnostics.buildDisplayAndNavigationComponent(program.getGraphComponent());  // TODO should the scroll pane be added by the error collector?
               editor.setResultsTab("Errors", new JScrollPane(display), true);
            }
         }
      }
   }

   private void showGeneratedCode (CharSequence sourceCode) {
      JFrame frame = new JFrame("Python Code");
      JTextArea textArea = new JTextArea(sourceCode.toString(), 25, 80);
      textArea.setEditable(false);
      frame.add(new JScrollPane(textArea));
      frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
      frame.pack();
      frame.setLocationRelativeTo(null);
      frame.setVisible(true);
   }

   private static String searchPathFor (String fileName) {
      for (String dirPath: Iterators.split(System.getenv("PATH"), File.pathSeparatorChar)) {
         File candidate = new File(dirPath, fileName);
         if (candidate.isFile() && candidate.canExecute())
            return candidate.getAbsolutePath();
      }
      return null;
   }

   private void simulateWithNEST (CharSequence sourceCode, EditorToolBar toolBar) {
      try {
         Path tempDir = Files.createTempDirectory("ncc");
         Path scriptFile = tempDir.resolve("run.py");
         try (Writer w = Files.newBufferedWriter(scriptFile)) {
            w.append(sourceCode);
         }
      }
      catch (IOException ioe) {
         toolBar.setJobStatus(EditorToolBar.StatusLevel.BAD, "Simulation failed: " + ioe.getMessage());
      }
   }

   /*

public class ExecutableFinder {

  private static string searchPathFor(string fileName) {
    foreach dirPath in System.getenv("PATH").split(File.pathSeparator) {
      File candidate = new File(dirPath, fileName);
      if (candidate.isFile() && candidate.canExecute()) {
        return candidate.getAbsolutePath();
      }
    }
    return null;
  }

  public static string getPathFor(string fileName, string fallbackName, string varName) {
    if (varName.isNotEmpty) {
      string pathVar;
      pathVar = PathMacros.getInstance().getValue(varName);
      // pathVar = MacrosFactory.getGlobal().expandPath("${" + varName + "}");
      if (pathVar.isNotEmpty) { return pathVar; }
      pathVar = System.getenv().get(varName);
      if (pathVar.isNotEmpty) { return pathVar; }
    }
    string extension = SystemInfo.isWindows ? ".exe" : "";
    string path = searchPathFor(fileName + extension);
    if (path.isEmpty && fallbackName.isNotEmpty) {
      path = searchPathFor(fallbackName + extension);
    }
    return path;
  }

}
    */
   private void simulateWithNMPI (CharSequence sourceCode, NmpiClient.Platform targetPlatform,
                                  EditorToolBar toolBar) {

      // JOptionPane.showMessageDialog(null, "RunAction Graph: " + graph);

      // JOptionPane.showMessageDialog(null, "RunAction token: " + JavaScriptBridge.getHBPToken());

      final NmpiClient client = new NmpiClient();
      // JOptionPane.showMessageDialog(null, "RunAction userId: " + client.getUserId());
      long collabId = client.getCollabId();
      // JOptionPane.showMessageDialog(null, "RunAction collabId lastQuery: " + client.lastQuery);
      // JOptionPane.showMessageDialog(null, "RunAction collabId lastResponse: " + client.lastJobResponse);

      // String map = client.getResourceMap("ignored");
      // JOptionPane.showMessageDialog(null, "RunAction map: " + map);

      toolBar.setJobStatus("Submitting …");
      final long jobId = client.submitJob(
            "# submitted " + (counter++) + " at " + new Date().toString() + "\n" +
                  sourceCode.toString(), targetPlatform);
      // JOptionPane.showMessageDialog(null, "RunAction submitJob query: " + client.lastQuery);
      // JOptionPane.showMessageDialog(null, "RunAction submitJob response: " + client.lastJobResponse);
      if (jobId >= 0) {
         toolBar.setJobStatus("Running …");
         trackJobStatus =
               new Timer(POLLING_INTERVAL,
                         e -> {
                            String status = client.getJobStatus(jobId);
                            if ("finished".equals(status)) {
                               toolBar.setJobStatus(EditorToolBar.StatusLevel.GOOD, "Finished");
                               finishedJob();
                            }
                            else if ("error".equals(status)) {
                               toolBar.setJobStatus(EditorToolBar.StatusLevel.BAD, "Error!");
                               finishedJob();

                            }
                            // JOptionPane.showMessageDialog(null, "timer: " + status + "\n" + eventSource);
                         });
         setEnabled(false);
         trackJobStatus.start();
      }
      else
         toolBar.setJobStatus(EditorToolBar.StatusLevel.BAD, "Job submission failed! (" + jobId + ")");

      // JOptionPane.showMessageDialog(null, "RunAction jobInfo: " + client.getJobInfo(jobId).toString(WriterConfig.PRETTY_PRINT));
   }

   private void finishedJob () {
      if (trackJobStatus != null) {
         trackJobStatus.stop();
         trackJobStatus = null;
      }
      setEnabled(true);
   }
}
