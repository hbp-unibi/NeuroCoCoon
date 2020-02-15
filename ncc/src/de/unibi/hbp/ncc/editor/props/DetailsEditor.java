package de.unibi.hbp.ncc.editor.props;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import de.unibi.hbp.ncc.lang.DisplayNamed;
import de.unibi.hbp.ncc.lang.LanguageEntity;
import de.unibi.hbp.ncc.lang.props.EditableProp;
import de.unibi.hbp.ncc.lang.props.ReadOnlyProp;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.JTextComponent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public class DetailsEditor {
   private PropsTableModel tableModel;
   private JTable table;
   private JComponent component;

   public DetailsEditor () {
      tableModel = new PropsTableModel();
      TableColumnModel tableColumnModel = new DefaultTableColumnModel();
      TableColumn markerColumn = new TableColumn(0, 30);
      // no header text
      markerColumn.setMaxWidth(30);
      tableColumnModel.addColumn(markerColumn);
      TableColumn labelColumn = new TableColumn(1, 150);
      labelColumn.setHeaderValue("Property");
      labelColumn.setCellRenderer(new PropNameCellRenderer(tableModel));
      tableColumnModel.addColumn(labelColumn);
      TableColumn valueColumn = new TableColumn(2);
      valueColumn.setHeaderValue("Value");
      valueColumn.setCellRenderer(new PropValueCellRenderer());
      valueColumn.setCellEditor(new PropValueCellEditor(tableModel));
      tableColumnModel.addColumn(valueColumn);
      table = new JTable(tableModel, tableColumnModel);
      // replace action for ENTER, since next row would be selected automatically
      ActionMap actions = table.getActionMap();
      actions.put("smartNavNext", new SmartNavigationAction("smartNavNext", +1));
      actions.put("smartNavPrev", new SmartNavigationAction("smartNavPrev", -1));
      InputMap whenAncestor = table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
      whenAncestor.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "smartNavNext");
      whenAncestor.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK), "smartNavPrev");
      whenAncestor.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "smartNavNext");
      whenAncestor.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK), "smartNavPrev");
      table.setGridColor(Color.LIGHT_GRAY);
      table.setShowGrid(true);
      table.setFillsViewportHeight(true);
      component = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
   }

   private class SmartNavigationAction extends AbstractAction {
      private int rowDelta;

      public SmartNavigationAction (String name, int rowDelta) {
         super(name);
         this.rowDelta = rowDelta;
      }

      public void actionPerformed(ActionEvent e) {
         int row, col;
         if (table.isEditing()) {
            row = table.getEditingRow();
            col = table.getEditingColumn();
            if (!table.getCellEditor().stopCellEditing())  // store user input, if it is valid
               return;
            row += rowDelta;
            if (row < 0 || row >= tableModel.getRowCount())
               return;
            table.changeSelection(row, col, false, false);
         }
         else {
            row = table.getSelectedRow();
            col = table.getSelectedColumn();
         }
         if (table.editCellAt(row, col)) {
            Component editor = table.getEditorComponent();
            editor.requestFocusInWindow();
            if (editor instanceof JTextComponent)
               ((JTextComponent) editor).selectAll();
         }
      }
   }

   public void setSubject (mxGraphComponent graphComponent, LanguageEntity subject) {
      tableModel.setSubject(graphComponent, subject);
   }

   public void setSubject (LanguageEntity subject) {  // for entities without a visual representation
      tableModel.setSubject(null, subject);
   }

   public JComponent getComponent () { return component; }

   static class PropNameCellRenderer extends DefaultTableCellRenderer {
      private PropsTableModel tableModel;

      PropNameCellRenderer (PropsTableModel tableModel) { this.tableModel = tableModel; }

      private static Font normalCellFont, italicsCellFont;

      @Override
      public Component getTableCellRendererComponent (JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                      int row, int column) {
         Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
         if (normalCellFont == null) {
            normalCellFont = component.getFont();
            italicsCellFont = new Font(normalCellFont.getName(), Font.ITALIC, normalCellFont.getSize());
         }
         component.setFont(column == 1 && tableModel.isIndirectProp(row) ? italicsCellFont : normalCellFont);
         // TODO use component.setFont(); with italics instead of blue
         return component;
      }
   }

   static class PropValueCellRenderer extends DefaultTableCellRenderer {

      @Override
      public Component getTableCellRendererComponent (JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                      int row, int column) {
         // System.err.println("Prop Value Cell Renderer: " + value + " ?" + (value instanceof DisplayNamed));
         return super.getTableCellRendererComponent(table,
                                                    value instanceof DisplayNamed ? ((DisplayNamed) value).getDisplayName() : value,
                                                    isSelected, hasFocus, row, column);
      }
   }

   static class PropValueCellEditor extends AbstractCellEditor implements TableCellEditor {
      private PropsTableModel tableModel;
      private TableCellEditor editor;

      PropValueCellEditor (PropsTableModel tableModel) {
         this.tableModel = tableModel;
      }

      @Override
      public Object getCellEditorValue () {
         if (editor != null) {
            return editor.getCellEditorValue();
         }
         return null;
      }

      @Override
      public Component getTableCellEditorComponent (JTable table, Object value, boolean isSelected, int row, int column) {
         EditableProp<?> prop = tableModel.getPropForRow(row);
         editor = prop.getTableCellEditor(table);
         return editor.getTableCellEditorComponent(table, value, isSelected, row, column);
      }
   }

   static class PropsTableModel extends AbstractTableModel {
      private mxGraphComponent graphComponent;
      private LanguageEntity subject;
      List<ReadOnlyProp<?>> readOnlyProps;
      List<EditableProp<?>> editableProps;

      PropsTableModel () {
         subject = null;
         readOnlyProps = Collections.emptyList();
         editableProps = Collections.emptyList();
      }

      void setSubject (mxGraphComponent graphComponent, LanguageEntity subject) {
         this.graphComponent = graphComponent;
         if (!Objects.equals(this.subject, subject))
            updateSubject(subject);
      }

      private void updateSubject (LanguageEntity subject) {
         this.subject = subject;
         if (subject != null) {
            readOnlyProps = subject.getDirectAndIndirectReadOnlyProps();
            editableProps = subject.getDirectAndIndirectEditableProps();
         }
         else {
            readOnlyProps = Collections.emptyList();
            editableProps = Collections.emptyList();
         }
         // fireTableStructureChanged();
         fireTableDataChanged();
      }

      boolean isIndirectProp (int rowIndex) { return !subject.equals(getAnyPropForRow(rowIndex).getEnclosingEntity()); }

      @Override
      public int getRowCount () {
         return readOnlyProps.size() + editableProps.size();
      }

      @Override
      public int getColumnCount () {
         return 3;
      }

      private ReadOnlyProp<?> getAnyPropForRow (int rowIndex) {
         if (rowIndex < readOnlyProps.size())
            return readOnlyProps.get(rowIndex);
         else
            return editableProps.get(rowIndex - readOnlyProps.size());
      }

      private EditableProp<?> getPropForRow (int rowIndex) {
         assert rowIndex >= readOnlyProps.size() : "value of read-only prop cannot be edited";
         return editableProps.get(rowIndex - readOnlyProps.size());
      }

      @Override
      public Object getValueAt (int rowIndex, int columnIndex) {
         if (columnIndex == 0){
            ReadOnlyProp<?> prop = getAnyPropForRow(rowIndex);
            return VisualMarkers.getPropertyMarkers(prop, subject);
         }
         if (columnIndex == 1)
            return getAnyPropForRow(rowIndex).getPropName(true);
         else
            return getAnyPropForRow(rowIndex).getValue();
      }

      @Override
      public void setValueAt (Object value, int rowIndex, int columnIndex) {
         assert columnIndex == 2 : "only value column can be edited";
         if (value != null) {
            EditableProp<?> prop = getPropForRow(rowIndex);
            prop.setRawValue(value);
            fireTableCellUpdated(rowIndex, columnIndex);
            EnumSet<EditableProp.Impact> impact = prop.getChangeImpact();
            LanguageEntity parent = prop.getEnclosingEntity();
            mxCell cell = parent.getOwningCell();
            if (cell != null && impact.contains(EditableProp.Impact.CELL_LABEL)) {
               graphComponent.labelChanged(cell, subject, null);
            }
            if (cell != null && impact.contains(EditableProp.Impact.CELL_STYLE)) {
               String style = parent.getCellStyle();
               if (style != null)
                  graphComponent.getGraph().setCellStyle(style, new Object[] { cell });
            }
            if (impact.contains(EditableProp.Impact.DEPENDENT_CELLS_STYLE)) {
               String style = parent.getCellStyle();
               if (style != null) {
                  mxGraph graph = graphComponent.getGraph();
                  List<mxCell> dependentCells = parent.getDependentCells(graph.getModel());
                  graph.setCellStyle(style, dependentCells.toArray());
               }
            }
            // FIXME handle in between cases
            if (impact.contains(EditableProp.Impact.OTHER_PROPS_VISIBILITY))
               updateSubject(subject);  // force set of visible table rows to change
         }
      }

      @Override
      public boolean isCellEditable (int rowIndex, int columnIndex) {
         return columnIndex == 2 && rowIndex >= readOnlyProps.size() &&
               !getAnyPropForRow(rowIndex).getEnclosingEntity().isPredefined();
      }

   }
}
