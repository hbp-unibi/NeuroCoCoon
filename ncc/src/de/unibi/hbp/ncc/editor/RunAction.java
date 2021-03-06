package de.unibi.hbp.ncc.editor;

import de.unibi.hbp.ncc.NeuroCoCoonEditor;
import de.unibi.hbp.ncc.env.NmpiClient;
import de.unibi.hbp.ncc.lang.Namespace;
import de.unibi.hbp.ncc.lang.Program;
import de.unibi.hbp.ncc.lang.codegen.ErrorCollector;
import de.unibi.hbp.ncc.lang.utils.DirTreeDeleter;
import de.unibi.hbp.ncc.lang.utils.Iterators;
import de.unibi.hbp.ncc.lang.utils.ShellCommandExecutor;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RunAction extends AbstractAction {

   private static int counter = 567;  // some distinctive, easy to recognize value

   private static final String TOY_CODE =
         "import sys\n" +
               "print('Hello from NeuroCoCoon')\n" +
               "print(sys.version)\n";

   private static final int POLLING_INTERVAL = 5000;  // milliseconds

   private Action wrapperAction;  // toolbar button is bound to this auxiliary action created by editor.bind()
   private Timer trackJobStatus;
   private NmpiClient.JobInfo lastJobInfo;
   private CharSequence lastOutput;
   private LocalDateTime lastOutputWhen, runningSince;

   public Action setWrapperAction (Action wrapperAction) {
      this.wrapperAction = wrapperAction;
      return wrapperAction;
   }

   @Override
   public void setEnabled (boolean newValue) {
      super.setEnabled(newValue);  // disable ourselves as usual
      wrapperAction.setEnabled(newValue);  // and inform the toolbar button via the wrapper action
   }

   @Override
   public void actionPerformed (ActionEvent e) {
      // JOptionPane.showMessageDialog(null, "RunAction Source: " + e.getSource());
      NeuroCoCoonEditor editor = EditorActions.getEditor(e);
      // JOptionPane.showMessageDialog(null, "RunAction Editor: " + editor);
      if (editor != null && trackJobStatus == null) {
         editor.status("Preparing …");
         Program program = editor.getProgram();
         NmpiClient.Platform targetPlatform = editor.getEditorToolBar().getCurrentPlatform();
         // System.err.println("RunAction: modifiers = " + e.getModifiers());
         // modifiers seems to alway be 0
         if ((e.getModifiers() & InputEvent.ALT_DOWN_MASK) != 0)
            targetPlatform = NmpiClient.Platform.SOURCE_CODE;  // temporary switch to view source code only
         StringBuilder pythonCode;
         pythonCode = program.generatePythonCode(targetPlatform);
         ErrorCollector diagnostics = program.getLastDiagnostics();
         editor.getEditorToolBar().setProblemStatus(pythonCode == null);
         if (pythonCode != null) {
            editor.status(diagnostics.hasAnyWarnings() ? "There were warnings." : "Success!");
            if (diagnostics.hasAnyMessages()) {
               Component display = diagnostics.buildComponent(program.getGraphComponent());
               editor.setResultsTab("Messages", display, false);
            }
            if (targetPlatform == NmpiClient.Platform.SOURCE_CODE)
               showLongText("Python Code", pythonCode);
            else if (targetPlatform == NmpiClient.Platform.NEST)
               simulateWithNEST(pythonCode, editor);
            else
               simulateWithNMPI(pythonCode, targetPlatform, editor);
         }
         else {
            editor.status("There were errors.");
            if (diagnostics.hasAnyErrors()) {
               Component display = diagnostics.buildComponent(program.getGraphComponent());
               editor.setResultsTab("Errors", display, true);
            }
         }
      }
   }

   public boolean showLastOutput () {
      if (lastOutput == null)
         return false;
      showLongText("Last Simulation Output: " + lastOutputWhen, lastOutput);
      return true;
   }

   private void showLongText (String windowTitle, CharSequence longText) {
      JFrame frame = new JFrame(windowTitle);
      JTextArea textArea = new JTextArea(longText.toString(), 25, 80);
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
         // can still check for isFile() although virtual envs use symbolic links to files which seem to be considered isFile()
         if (candidate.isFile() && candidate.canExecute())
            return candidate.getAbsolutePath();
      }
      return null;
   }

   private transient String cachedPython3Path;

   private void simulateWithNEST (CharSequence sourceCode, NeuroCoCoonEditor editor) {
      String python = cachedPython3Path;
      if (python == null) {
         python = System.getProperty("de.unibi.hbp.ncc.python3");
         if (python == null)
            python = System.getProperty("python3");  // more convenient shorthand form
         if (python == null)
            python = searchPathFor("python3");
         if (python == null)
            python = searchPathFor("python3.exe");
         if (python == null) {
            editor.setJobStatus(EditorToolBar.StatusLevel.BAD, "Python missing!",
                                "python3[.exe] not found on command path!");
            return;
         }
         else {
            System.err.println("Using Python 3 at " + python);
         }
         cachedPython3Path = python;
      }
      editor.setJobStatus("Running …");
      try {
         Path tempDir = Files.createTempDirectory("ncc");
         Path scriptFile = tempDir.resolve("run.py");
         try (Writer w = Files.newBufferedWriter(scriptFile)) {
            w.append(sourceCode);
         }
         List<String> commandList = Arrays.asList(python, "run.py", "nest");
         Thread worker =
               new Thread( () -> {
                  PlotsPanel plotsPanel = null;
                  try {
                     ShellCommandExecutor executor = new ShellCommandExecutor();
                     executor.execute(commandList, tempDir.toFile());
                     if (executor.failed())
                        SwingUtilities.invokeLater(
                              () -> {
                                 editor.setJobStatus(EditorToolBar.StatusLevel.BAD, "Simulation failed!");
                                 Dialogs.error(editor, executor.getOutputTail(300).toString(),
                                               "NEST Simulation failed");
                              });
                     else
                        SwingUtilities.invokeLater(
                              () -> editor.setJobStatus(EditorToolBar.StatusLevel.GOOD, "Simulation completed."));
                     if (executor.haveOutput()) {
                        lastOutput = executor.getOutput();
                        lastOutputWhen = LocalDateTime.now();
                     }
                     else {
                        lastOutput = null;
                        lastOutputWhen = null;
                     }
                     if (!executor.failed()) {
                        try (Stream<Path> dirContent = Files.list(tempDir)) {
                           List<File> plotImageFiles =
                                 dirContent.filter(
                                       dirChild -> Files.isRegularFile(dirChild, LinkOption.NOFOLLOW_LINKS) &&
                                             dirChild.getFileName().toString().toLowerCase().endsWith(".png"))
                                       .map(Path::toFile)
                                       .sorted(Comparator.comparing(File::getName,
                                                                    Namespace.getSmartNumericOrderComparator()))
                                       .collect(Collectors.toList());
                           plotsPanel = new PlotsPanel(plotImageFiles,
                                                       editor.getProgram().getGlobalScope().getDataPlots());
                        }
                     }
                  }
                  catch (IOException | InterruptedException excp) {
                     SwingUtilities.invokeLater(
                           () -> Dialogs.error(editor, excp, "Exception in external command"));
                  }
                  finally {
                     // TODO provide a way to preserve the temp directory intact and print its path to stderr instead
                     try {
                        DirTreeDeleter.deleteRecursively(tempDir);
                     }
                     catch (IOException ioe) {
                        SwingUtilities.invokeLater(
                              () -> Dialogs.error(editor, ioe, "Exception while cleaning up"));
                     }
                     final PlotsPanel capturedPlotsPanel = plotsPanel;
                     SwingUtilities.invokeLater(
                           () -> {
                              finishedJob(null);
                              if (capturedPlotsPanel != null && !capturedPlotsPanel.isEmpty()) {
                                 editor.setResultsTab("Plots",
                                                      capturedPlotsPanel.buildComponent(),
                                                      true);
                              }
                              else
                                 editor.clearResultsTab();
                           });
                  }
               });
         setEnabled(false);
         worker.start();
      }
      catch (IOException ioe) {
         editor.setJobStatus(EditorToolBar.StatusLevel.BAD, "I/O Exception!", ioe.getMessage());
      }
   }

   private void simulateWithNMPI (CharSequence sourceCode, NmpiClient.Platform targetPlatform,
                                  NeuroCoCoonEditor editor) {

      // JOptionPane.showMessageDialog(null, "RunAction Graph: " + graph);

      // JOptionPane.showMessageDialog(null, "RunAction token: " + JavaScriptBridge.getHBPToken());

      final NmpiClient client = new NmpiClient();
      // JOptionPane.showMessageDialog(null, "RunAction userId: " + client.getUserId());
      long collabId = client.getCollabId();
      // JOptionPane.showMessageDialog(null, "RunAction collabId lastQuery: " + client.lastQuery);
      // JOptionPane.showMessageDialog(null, "RunAction collabId lastResponse: " + client.lastJobResponse);

      // String map = client.getResourceMap("ignored");
      // JOptionPane.showMessageDialog(null, "RunAction map: " + map);

      editor.setJobStatus("Submitting …");
      final long jobId = client.submitJob(
            "# submitted " + (counter++) + " at " + new Date().toString() + "\n" +
                  sourceCode.toString(), targetPlatform);
      // JOptionPane.showMessageDialog(null, "RunAction submitJob query: " + client.lastQuery);
      // JOptionPane.showMessageDialog(null, "RunAction submitJob response: " + client.lastJobResponse);
      if (jobId >= 0) {
         editor.setJobStatus("Running …");
         runningSince = LocalDateTime.now();
         trackJobStatus =
               new Timer(POLLING_INTERVAL,
                         e -> {
                            NmpiClient.JobInfo jobInfo = client.getJobInfo(jobId);
                            String status = jobInfo.getJobStatus();
                            if ("finished".equals(status)) {
                               editor.setJobStatus(EditorToolBar.StatusLevel.GOOD,
                                                   durationMessage("Finished after %d:%02d", runningSince));
                               for (String url: jobInfo.getOutputURLs())
                                  System.err.println("output URL: " + url);
                               finishedJob(jobInfo);
                               Thread worker = new Thread( () -> {
                                  List<String> plotImageURLs = Iterators.asList(
                                        Iterators.filter(jobInfo.getOutputURLs(),
                                                         url -> url.toLowerCase().endsWith(".png")));
                                  plotImageURLs.sort(Namespace.getSmartNumericOrderComparator());
                                  // compares the full URLs, but should all have the same prefix before the file name
                                  PlotsPanel plotsPanel = new PlotsPanel(plotImageURLs,
                                                                         editor.getProgram().getGlobalScope().getDataPlots(),
                                                                         true);
                                  SwingUtilities.invokeLater(
                                        () -> {
                                           if (!plotsPanel.isEmpty()) {
                                              editor.setResultsTab("Plots", plotsPanel.buildComponent(),
                                                                   true);
                                           }
                                           else
                                              editor.clearResultsTab();
                                        });

                               });
                               worker.start();
                            }
                            else if ("error".equals(status)) {
                               String jobLog = client.getJobLog(jobId);
                               editor.setJobStatus(EditorToolBar.StatusLevel.BAD,
                                                   durationMessage("Error after %d:%02d!", runningSince),
                                                   "Log: " + jobLog);
                               finishedJob(jobInfo);
                            }
                            else {
                               editor.setJobStatus(durationMessage("Running for %d:%02d …", runningSince));
                            }
                            // JOptionPane.showMessageDialog(null, "timer: " + status + "\n" + eventSource);
                         });
         setEnabled(false);
         trackJobStatus.start();
      }
      else
         editor.setJobStatus(EditorToolBar.StatusLevel.BAD, "Submission failed!",
                             "Could not submit job with id " + jobId);

      // JOptionPane.showMessageDialog(null, "RunAction jobInfo: " + client.getJobInfo(jobId).toString(WriterConfig.PRETTY_PRINT));
   }

   private static String durationMessage (String format, LocalDateTime since) {
      long seconds = Duration.between(since, LocalDateTime.now()).getSeconds();
      return String.format(format, seconds / 60, seconds % 60);
   }

   private void finishedJob (NmpiClient.JobInfo jobInfo) {
      if (trackJobStatus != null) {
         trackJobStatus.stop();
         trackJobStatus = null;
      }
      setEnabled(true);
   }
}
