package de.unibi.hbp.ncc.lang.codegen;

import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import de.unibi.hbp.ncc.graph.AbstractCellsVisitor;
import de.unibi.hbp.ncc.lang.DisplayNamed;
import de.unibi.hbp.ncc.lang.LanguageEntity;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

public class ErrorCollector {

   // TODO see WarningAction on how to add markers to cells
   public enum Severity implements DisplayNamed {
      NOTE("Note"), INFO("Info"),
      WARNING("Warning"), ERROR("Error"),
      FATAL("Fatal");

      private String displayName;

      Severity (String displayName) { this.displayName = displayName; }

      @Override
      public String getDisplayName () { return displayName; }
   }

   private static class Entry {
      Severity level;
      LanguageEntity responsible;
      String message;

      Entry (Severity level, LanguageEntity responsible, String message) {
         this.level = level;
         this.responsible = responsible;
         this.message = message;
      }
   }

   private mxGraphComponent graphComponent;
   private List<Entry> entries;
   private boolean anyWarnings, anyErrors;

   public ErrorCollector (mxGraphComponent graphComponent) {
      this.graphComponent = graphComponent;
      entries = new ArrayList<>();
      anyWarnings = anyErrors = false;
   }

   public void reset () {
      entries.clear();
      anyWarnings = anyErrors = false;
      graphComponent.clearCellOverlays(); // does NOT visit edges and clear their warning overlays
/*
      AbstractCellsVisitor.simpleVisitGraph(graphComponent.getGraph().getModel(),
                                            (cell, entity) -> graphComponent.removeCellOverlays(cell));
*/
   }

   public void record (LanguageEntity responsible, Severity severity, String message) {
      entries.add(new Entry(severity, responsible, message));
      if (responsible != null) {
         mxICell cell = responsible.getOwningCell();
         if (cell != null)
            graphComponent.setCellWarning(cell, message);
         // TODO use different warning icons for different levels of severity?
         // TODO collect multiple messages per cell or at least let the most severe (and recent) message win?
      }
      anyErrors |= Severity.ERROR.compareTo(severity) <= 0;
      anyWarnings |= Severity.WARNING == severity;
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

   public boolean hasAnyErrors () { return anyErrors; }

   public boolean hasAnyWarnings () { return anyWarnings | anyErrors; }

   public Component getDisplayAndNavigationComponent () {
      return null;  // FIXME implement this
   }
}
