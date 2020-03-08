package de.unibi.hbp.ncc.lang.codegen;

import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import de.unibi.hbp.ncc.editor.BasicGraphEditor;
import de.unibi.hbp.ncc.lang.DisplayNamed;
import de.unibi.hbp.ncc.lang.LanguageEntity;
import org.stringtemplate.v4.STErrorListener;
import org.stringtemplate.v4.misc.STMessage;

import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ErrorCollector implements STErrorListener {

   public enum Severity implements DisplayNamed {
      NOTE("Note"), INFO("Info"),
      WARNING("Warning"), ERROR("Error"),
      FATAL("Fatal");

      private String displayName;
      private ImageIcon icon;

      Severity (String displayName) {
         this.displayName = displayName;
         this.icon = new ImageIcon(BasicGraphEditor.class.getResource("images/" + displayName.toLowerCase() + ".png"));
      }

      public ImageIcon getIcon () { return icon; }

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
      graphComponent.clearCellOverlays(); // seems to visit edges, too (and clear their warning overlays)
/*
      AbstractCellsVisitor.simpleVisitGraph(graphComponent.getGraph().getModel(),
                                            (cell, entity) -> graphComponent.removeCellOverlays(cell));
*/
   }

   public void setMinimumLevel (Severity minimumLevel) { this.minimumLevel = minimumLevel; }

   public void record (LanguageEntity responsible, Severity severity, String message) {
      if (minimumLevel.compareTo(severity) > 0)  // ignore everything below the threshold level
         return;
      entries.add(new Entry(severity, responsible, message));
      if (responsible != null) {
         mxICell cell = responsible.getOwningCell();
         if (cell != null && Severity.WARNING.compareTo(severity) <= 0) {  // no visual markers for NOTEs and INFOs
            graphComponent.setCellWarning(cell, message, severity.getIcon());
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

   public void recordFatal (LanguageEntity responsible, String message) {
      record(responsible, Severity.FATAL, message);
   }

   public void recordFatal (LanguageEntity responsible, Throwable cause) {
      recordFatal(responsible, cause.getMessage());
   }

   @Override
   public void compileTimeError (STMessage msg) {
      recordError(null, msg.toString());
   }

   @Override
   public void runTimeError (STMessage msg) { recordError(null, msg.toString()); }

   @Override
   public void IOError (STMessage msg) {
      recordFatal(null, msg.toString());
   }

   @Override
   public void internalError (STMessage msg) {
      recordFatal(null, msg.toString());
   }

   public boolean hasAnyMessages () { return !entries.isEmpty(); }

   public boolean hasAnyErrors () { return anyErrors; }

   public boolean hasAnyWarnings () { return anyWarnings | anyErrors; }

   private static final int ICON_CELL_SIZE = 28;  // icon itself is 24x24

   public Component buildComponent (mxGraphComponent graphComponent) {
      TableColumnModel columnModel = new DefaultTableColumnModel();
      columnModel.setColumnSelectionAllowed(false);
      TableColumn severityColumn = new TableColumn(0, ICON_CELL_SIZE);
      // severityColumn.setHeaderValue("Level");
      severityColumn.setMaxWidth(ICON_CELL_SIZE);
      severityColumn.setCellRenderer(new IconCellRenderer());
      columnModel.addColumn(severityColumn);
      TableColumn entityColumn = new TableColumn(1);
      entityColumn.setHeaderValue("Affected");
      entityColumn.setCellRenderer(new FullTextToolTipRenderer());
      columnModel.addColumn(entityColumn);
      TableColumn messageColumn = new TableColumn(2);
      messageColumn.setHeaderValue("Message");
      messageColumn.setCellRenderer(new FullTextToolTipRenderer());
      columnModel.addColumn(messageColumn);
      JTable errorTable = new JTable(new ErrorTableModel(), columnModel);
      errorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      errorTable.setRowSelectionAllowed(true);
      errorTable.setGridColor(Color.LIGHT_GRAY);
      errorTable.setRowHeight(ICON_CELL_SIZE);
      errorTable.setShowGrid(true);
      errorTable.setFillsViewportHeight(true);
      errorTable.getSelectionModel().addListSelectionListener(
            e -> {
               int selectedRow = errorTable.getSelectedRow();
               if (selectedRow >= 0) {
                  LanguageEntity responsible = (LanguageEntity) errorTable.getValueAt(selectedRow, 1);
                  if (responsible != null) {
                     mxICell cell = responsible.getOwningCell();
                     if (cell != null) {
                        graphComponent.scrollCellToVisible(cell);
                        graphComponent.getGraph().setSelectionCell(cell);
                     }
                  }
               }
            }
      );
      return new JScrollPane(errorTable);
   }

   private static class FullTextToolTipRenderer extends DefaultTableCellRenderer {
      @Override
      protected void setValue (Object value) {
         super.setValue(value);
         setToolTipText(
               value instanceof DisplayNamed
                     ? ((DisplayNamed) value).getLongDisplayName()
                     : value != null ? value.toString() : null);
      }
   }

   private static class IconCellRenderer extends DefaultTableCellRenderer {
      @Override
      protected void setValue (Object value) {
         setHorizontalAlignment(CENTER);
         setVerticalAlignment(CENTER);
         setIcon(((Severity) value).getIcon());
         setToolTipText(((DisplayNamed) value).getDisplayName());
      }
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
