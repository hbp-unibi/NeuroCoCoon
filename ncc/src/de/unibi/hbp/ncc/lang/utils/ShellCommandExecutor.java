package de.unibi.hbp.ncc.lang.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class ShellCommandExecutor {
   private int exitCode;
   private boolean raisedException;
   private StringBuilder commandOutput;

   public ShellCommandExecutor () { }

   public void execute (List<String> commandList, File workingDirectory)
         throws IOException, InterruptedException {
      Process process = new ProcessBuilder(commandList)
            .redirectErrorStream(true)
            .directory(workingDirectory)
            .start();
      StringBuilder sb = new StringBuilder();
      raisedException = false;
      try (BufferedReader cmdOutput = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
         String line;
         while ((line = cmdOutput.readLine()) != null)
            sb.append(line).append('\n');
      } catch (IOException ioe) {
         raisedException = true;
         sb.append('\n').append(ioe).append('\n');
      }
      exitCode = process.waitFor();
      if (exitCode != 0 || raisedException)
         sb.append("Exit Code: ").append(exitCode);
      commandOutput = sb;
   }

   public boolean succeeded () {
      return exitCode == 0 && !raisedException;
   }

   public boolean failed () {
      return exitCode != 0 || raisedException;
   }

   public boolean haveOutput () {
      return commandOutput != null && commandOutput.length() > 0;
   }

   public CharSequence getOutput () {
      return commandOutput == null ? "" : commandOutput;
   }

   public CharSequence getOutputTail (int lines) {
      return getOutput();
   }
   // TODO provide a truncated to the last few lines version of the output for the error alert

}
