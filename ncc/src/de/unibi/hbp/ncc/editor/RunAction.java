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
import javax.swing.AbstractListModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RunAction extends AbstractAction {

   private static int counter = 42;

   private static final String TOY_CODE =
         "import sys\n" +
               "print('Hello from NeuroCoCoon')\n" +
               "print(sys.version)\n";

   private static final int POLLING_INTERVAL = 5000;  // milliseconds

   private Timer trackJobStatus;
   private CharSequence lastOutput;
   private LocalDateTime lastOutputWhen;

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
         editor.getEditorToolBar().setProblemStatus(pythonCode == null);
         if (pythonCode != null) {
            editor.status(diagnostics.hasAnyWarnings() ? "There were warnings." : "Success!");
            if (diagnostics.hasAnyMessages()) {
               Component display = diagnostics.buildDisplayAndNavigationComponent(program.getGraphComponent());  // TODO should the scroll pane be added by the error collector?
               editor.setResultsTab("Messages", new JScrollPane(display), false);
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
               Component display = diagnostics.buildDisplayAndNavigationComponent(program.getGraphComponent());  // TODO should the scroll pane be added by the error collector?
               editor.setResultsTab("Errors", new JScrollPane(display), true);
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
         System.err.println("checking " + candidate);
         // cannot check for isFile() because virtual envs use symbolic links which are considered !isFile()
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
                  List<ImageIcon> plotImages = null;
                  try {
                     ShellCommandExecutor executor = new ShellCommandExecutor();
                     executor.execute(commandList, tempDir.toFile());
                     // TODO update status line with toolBar.setJobStatus
                     if (executor.failed())
                        SwingUtilities.invokeLater(
                              () -> {
                                 editor.setJobStatus(EditorToolBar.StatusLevel.BAD, "Simulation failed!");
                                 JOptionPane.showMessageDialog(null,
                                                               executor.getOutput(), "NEST Simulation failed",
                                                               JOptionPane.ERROR_MESSAGE);
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
                           plotImages =
                                 dirContent.filter(
                                       dirChild -> Files.isRegularFile(dirChild, LinkOption.NOFOLLOW_LINKS) &&
                                             dirChild.getFileName().toString().toLowerCase().endsWith(".png"))
                                       .map(pngChild -> new ImageIcon(pngChild.toString(),
                                                                      pngChild.getFileName().toString()))
                                       .sorted(Comparator.comparing(ImageIcon::getDescription,
                                                                    Namespace.getSmartNumericOrderComparator()))
                                       .collect(Collectors.toList());
                        }
                     }
                  }
                  catch (IOException | InterruptedException excp) {
                     SwingUtilities.invokeLater(
                           () -> JOptionPane.showMessageDialog(null,
                                                               excp.getMessage(), "Exception in Command",
                                                               JOptionPane.ERROR_MESSAGE));
                  }
                  finally {
                     try {
                        DirTreeDeleter.deleteRecursively(tempDir);
                     }
                     catch (IOException ioe) {
                        SwingUtilities.invokeLater(
                              () -> JOptionPane.showMessageDialog(null,
                                                                  ioe.getMessage(), "Exception while cleaning up",
                                                                  JOptionPane.ERROR_MESSAGE));
                     }
                     final List<ImageIcon> capturedPlotImages = plotImages;
                     SwingUtilities.invokeLater(
                           () -> {
                              finishedJob();
                              if (capturedPlotImages != null && !capturedPlotImages.isEmpty()) {
                                 JList<ImageIcon> plotList = new JList<>(new PlotListModel(capturedPlotImages));
                                 // DefaultListCellRenderer might suffice: it treats icons as a special case
                                 // TODO add downscaling with enlarge on click and description/title as tool tip
                                 editor.setResultsTab("Plots", new JScrollPane(plotList), true);
                              }
                              else
                                 editor.setResultsTab(null, null, false);
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

   private static class PlotListModel extends AbstractListModel<ImageIcon> {
      private List<ImageIcon> fullSize, thumbnails;

      public PlotListModel (List<ImageIcon> fullSize) {
         this.fullSize = fullSize;
         thumbnails = new ArrayList<>(fullSize.size());
         // TODO create thumbnail versions lazily on demand
         // TODO use Image instances instead of ImageIcons? overhead?
         for (ImageIcon fullIcon: fullSize)
            thumbnails.add(new ImageIcon(
//                  fullIcon.getImage().getScaledInstance(160, 90, Image.SCALE_SMOOTH),
                  fullIcon.getImage(),
                  fullIcon.getDescription()));
      }

      public ImageIcon getFullImage (int index) { return fullSize.get(index); }

      @Override
      public int getSize () { return fullSize.size(); }

      @Override
      public ImageIcon getElementAt (int index) {
         // return fullSize.get(index);
         System.err.println("getElementAt(" + index + "): " +
                                  thumbnails.get(index).getIconWidth() + " x " + thumbnails.get(index).getIconHeight());
         return thumbnails.get(index);
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
         trackJobStatus =
               new Timer(POLLING_INTERVAL,
                         e -> {
                            String status = client.getJobStatus(jobId);
                            if ("finished".equals(status)) {
                               editor.setJobStatus(EditorToolBar.StatusLevel.GOOD, "Finished");
                               finishedJob();
                            }
                            else if ("error".equals(status)) {
                               editor.setJobStatus(EditorToolBar.StatusLevel.BAD, "Error!", "Error message");
                               finishedJob();
                               // TODO add error message

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

   private void finishedJob () {
      if (trackJobStatus != null) {
         trackJobStatus.stop();
         trackJobStatus = null;
      }
      setEnabled(true);
   }
}
