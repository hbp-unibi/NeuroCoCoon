package de.unibi.hbp.ncc.lang.codegen;

import de.unibi.hbp.ncc.lang.LanguageEntity;

import java.awt.Component;

public class ErrorCollector {

   // TODO see WarningAction on how to add markers to cells
   public enum Severity { NOTE, INFO, WARNING, ERROR, FATAL }

   public void record (LanguageEntity responsible, Severity severity, String message) {
      // TODO implement this
   }

   public void recordNote(LanguageEntity responsible, String message) {
      record(responsible, Severity.NOTE, message);
   }

   public void recordInfo (LanguageEntity responsible, String message) {
      record(responsible, Severity.INFO, message);
   }

   public void recordWarning (LanguageEntity responsible, String message) {
      record(responsible, Severity.WARNING, message);
   }

   public void recordError (LanguageEntity responsible, String message) {
      record(responsible, Severity.ERROR, message);
   }

   public void recordFatal (LanguageEntity responsible, Throwable cause) {
      record(responsible, Severity.FATAL, cause.getMessage());
   }

   public boolean hasAnyErrors () {
      return false;  // FIXME implement this
   }

   public Component getDisplayAndNavigationComponent () {
      return null;  // FIXME implement this
   }
}
