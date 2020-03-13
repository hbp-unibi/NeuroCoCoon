package de.unibi.hbp.ncc.editor;

import com.mxgraph.util.mxResources;
import de.unibi.hbp.ncc.NeuroCoCoonEditor;

import javax.swing.JOptionPane;

public final class Dialogs {

   private Dialogs () { }

   // philosophy: editor argument should not normally be null, but don't crash if it is

   public static boolean confirm (NeuroCoCoonEditor editor, String question, String title) {
      return JOptionPane.showConfirmDialog(editor, question, title,
                                           JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                                           editor != null ? editor.getAppIcon() : null) == JOptionPane.YES_OPTION;
   }

   public static boolean confirm (NeuroCoCoonEditor editor, String question) {
      return confirm(editor, question, mxResources.get("confirmation"));
   }

   public static void error (NeuroCoCoonEditor editor, String message, String title) {
      JOptionPane.showMessageDialog(editor, message, title, JOptionPane.ERROR_MESSAGE,
                                    editor != null ? editor.getAppIcon() : null);

   }
   public static void error (NeuroCoCoonEditor editor, String message) {
      error(editor, message, mxResources.get("error"));
   }

   public static void error (NeuroCoCoonEditor editor, Throwable exception, String title) {
      error(editor, exception.toString(), title);
   }

   public static void error (NeuroCoCoonEditor editor, Throwable exception) {
      error(editor, exception, mxResources.get("error"));
   }

   // TODO replace remaining usages of JOptionPane for input dialogs with additional helper functions
}
