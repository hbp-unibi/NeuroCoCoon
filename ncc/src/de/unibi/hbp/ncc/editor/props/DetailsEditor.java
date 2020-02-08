package de.unibi.hbp.ncc.editor.props;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import de.unibi.hbp.ncc.lang.DisplayNamed;
import de.unibi.hbp.ncc.lang.LanguageEntity;
import de.unibi.hbp.ncc.lang.props.EditableProp;
import de.unibi.hbp.ncc.lang.props.ReadOnlyProp;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.Color;
import java.awt.Component;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public class DetailsEditor {

   private PropsTableModel tableModel;
   private JComponent component;

   public DetailsEditor () {
      tableModel = new PropsTableModel();
      TableColumnModel tableColumnModel = new DefaultTableColumnModel();
      TableColumn labelColumn = new TableColumn(0, 150);
      labelColumn.setHeaderValue("Property");
      labelColumn.setCellRenderer(new PropNameCellRenderer(tableModel));
      tableColumnModel.addColumn(labelColumn);
      TableColumn valueColumn = new TableColumn(1);
      valueColumn.setHeaderValue("Value");
      valueColumn.setCellRenderer(new PropValueCellRenderer());
      valueColumn.setCellEditor(new PropValueCellEditor(tableModel));
      tableColumnModel.addColumn(valueColumn);
      JTable table = new JTable(tableModel, tableColumnModel);
      table.setGridColor(Color.LIGHT_GRAY);
      table.setShowGrid(true);
      table.setFillsViewportHeight(true);
      component = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
   }

   public void setSubject (mxGraphComponent graphComponent, mxCell cell, LanguageEntity subject) {
      tableModel.setSubject(graphComponent, cell, subject);
   }

   public JComponent getComponent () { return component; }

   static class PropNameCellRenderer extends DefaultTableCellRenderer {
      private PropsTableModel tableModel;

      PropNameCellRenderer (PropsTableModel tableModel) { this.tableModel = tableModel; }

      @Override
      public Component getTableCellRendererComponent (JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                      int row, int column) {
         Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
         component.setForeground(column == 0 && tableModel.isIndirectProp(row) ? Color.BLUE : Color.BLACK);
         // component.setFont();
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
      private mxCell cell;
      private LanguageEntity subject;
      List<ReadOnlyProp<?>> readOnlyProps;
      List<EditableProp<?>> editableProps;

      PropsTableModel () {
         subject = null;
         readOnlyProps = Collections.emptyList();
         editableProps = Collections.emptyList();
      }

      void setSubject (mxGraphComponent graphComponent, mxCell cell, LanguageEntity subject) {
         this.graphComponent = graphComponent;
         this.cell = cell;
         if (!Objects.equals(this.subject, subject))
            updateSubject(subject);
      }

      private void updateSubject (LanguageEntity subject) {
         this.subject = subject;
         if (subject != null) {
            readOnlyProps = subject.getReadOnlyProps();
            editableProps = subject.getDirectAndIndirectEditableProps();
         }
         else {
            readOnlyProps = Collections.emptyList();
            editableProps = Collections.emptyList();
         }
         // fireTableStructureChanged();
         fireTableDataChanged();
      }

      boolean isIndirectProp (int rowIndex) {
         return !subject.equals(getAnyPropForRow(rowIndex).getParentEntity());
      }

      void notifyCell (EditableProp.Impact impact) {
         if (impact == EditableProp.Impact.CELL_LABEL)
            graphComponent.labelChanged(cell, subject, null);
         else if (impact == EditableProp.Impact.CELL_APPEARANCE)
            graphComponent.refresh();  // FIXME this is too coarse grained
         // TODO handle other impact levels
      }

      @Override
      public int getRowCount () {
         return readOnlyProps.size() + editableProps.size();
      }

      @Override
      public int getColumnCount () {
         return 2;
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
         if (columnIndex == 0)
            return getAnyPropForRow(rowIndex).getPropName(true);
         else
            return getAnyPropForRow(rowIndex).getValue();
      }

      @Override
      public void setValueAt (Object value, int rowIndex, int columnIndex) {
         assert columnIndex == 1 : "only value column can be edited";
         if (value != null) {
            EditableProp<?> prop = getPropForRow(rowIndex);
            prop.setRawValue(value);
            fireTableCellUpdated(rowIndex, columnIndex);
            EnumSet<EditableProp.Impact> impact = prop.getChangeImpact();
            if (impact.contains(EditableProp.Impact.CELL_LABEL))
               notifyCell(EditableProp.Impact.CELL_LABEL);
            // FIXME handle in between cases
            if (impact.contains(EditableProp.Impact.OTHER_PROPS_VISIBILITY))
               updateSubject(subject);  // force set of visible table rows to change
         }
      }

      @Override
      public boolean isCellEditable (int rowIndex, int columnIndex) {
         return columnIndex == 1 && rowIndex >= readOnlyProps.size() &&
               !getAnyPropForRow(rowIndex).getParentEntity().isPredefined();
      }

   }
}
