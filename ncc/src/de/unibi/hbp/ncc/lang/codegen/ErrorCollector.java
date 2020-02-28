package de.unibi.hbp.ncc.lang.codegen;

import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import de.unibi.hbp.ncc.editor.BasicGraphEditor;
import de.unibi.hbp.ncc.editor.props.MasterDetailsEditor;
import de.unibi.hbp.ncc.graph.AbstractCellsVisitor;
import de.unibi.hbp.ncc.lang.DisplayNamed;
import de.unibi.hbp.ncc.lang.LanguageEntity;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

      @Override
      public String toString () { return getDisplayName(); }
   }

   private static class Entry {
      Severity level;
      LanguageEntity responsible;
      String message;

      Entry (Severity level, LanguageEntity responsible, String message) {
         this.level = level;
         this.responsible = responsible;
         this.message = Objects.requireNonNull(message);
         assert !message.isEmpty();  // mxGraph overlays do NOT accept empty strings
      }
   }

   private mxGraphComponent graphComponent;
   private List<Entry> entries;
   private Severity minimumLevel;
   private boolean anyWarnings, anyErrors;

   public ErrorCollector (mxGraphComponent graphComponent) {
      this.graphComponent = graphComponent;
      entries = new ArrayList<>();
      minimumLevel = Severity.NOTE;
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

   public void setMinimumLevel (Severity minimumLevel) { this.minimumLevel = minimumLevel; }

   private static final ImageIcon WARNING_ICON =
         new ImageIcon(BasicGraphEditor.class.getResource("images/warning.png"));

   public void record (LanguageEntity responsible, Severity severity, String message) {
      if (minimumLevel.compareTo(severity) > 0)  // ignore everything below the threshold level
         return;
      entries.add(new Entry(severity, responsible, message));
      if (responsible != null) {
         mxICell cell = responsible.getOwningCell();
         if (cell != null && Severity.WARNING.compareTo(severity) <= 0) {  // no visual markers for NOTEs and INFOs
            if (severity == Severity.WARNING)
               graphComponent.setCellWarning(cell, message, WARNING_ICON);
            else
               graphComponent.setCellWarning(cell, message);  // use the default mxGraph icon (also called WARNING)
         }
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

   public boolean hasAnyMessages () { return !entries.isEmpty(); }

   public boolean hasAnyErrors () { return anyErrors; }

   public boolean hasAnyWarnings () { return anyWarnings | anyErrors; }

   public Component buildDisplayAndNavigationComponent () {
      TableColumnModel columnModel = new DefaultTableColumnModel();
      columnModel.setColumnSelectionAllowed(false);
      TableColumn severityColumn = new TableColumn(0, 45);
      severityColumn.setHeaderValue("Level");
      severityColumn.setMaxWidth(45);
      columnModel.addColumn(severityColumn);
      TableColumn entityColumn = new TableColumn(1);
      entityColumn.setHeaderValue("Affected");
      columnModel.addColumn(entityColumn);
      TableColumn messageColumn = new TableColumn(2);
      messageColumn.setHeaderValue("Message");
      columnModel.addColumn(messageColumn);
      JTable errorTable = new JTable(new ErrorTableModel(), columnModel);
      errorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      errorTable.setRowSelectionAllowed(true);
      errorTable.setGridColor(Color.LIGHT_GRAY);
      errorTable.setShowGrid(true);
      errorTable.setFillsViewportHeight(true);
      // FIXME implement click navigation to graph node
      return errorTable;
   }

   private class ErrorTableModel extends AbstractTableModel {
      @Override
      public int getRowCount () { return entries.size(); }

      @Override
      public int getColumnCount () { return 3; }

      @Override
      public Object getValueAt (int rowIndex, int columnIndex) {
         if (columnIndex == 0)
            return entries.get(rowIndex).level;
         else if (columnIndex == 1)
            return entries.get(rowIndex).responsible;
         else
            return entries.get(rowIndex).message;
      }
   }
}
