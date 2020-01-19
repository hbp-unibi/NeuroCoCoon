package de.unibi.hbp.ncc.editor.props;

import de.unibi.hbp.ncc.lang.LanguageEntity;
import de.unibi.hbp.ncc.lang.PropertyDescriptor;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.Color;
import java.awt.Component;
import java.util.List;

public class DetailsEditor {

   private JComponent component;

   public DetailsEditor () {
      TableColumnModel tableColumnModel = new DefaultTableColumnModel();
      TableColumn labelColumn = new TableColumn(0, 150);
      labelColumn.setHeaderValue("Property");
      tableColumnModel.addColumn(labelColumn);
      TableColumn valueColumn = new TableColumn(1);
      valueColumn.setHeaderValue("Value");
      valueColumn.setCellEditor(new CustomTableCellEditor());
      tableColumnModel.addColumn(valueColumn);
      JTable table = new JTable(new CustomTableModel(), tableColumnModel);
      table.setGridColor(Color.LIGHT_GRAY);
      table.setShowGrid(true);
      table.setFillsViewportHeight(true);
      component = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
   }

   public JComponent getComponent () { return component; }

   static class CustomTableCellEditor extends AbstractCellEditor implements TableCellEditor {
      private TableCellEditor editor;

      @Override
      public Object getCellEditorValue() {
         if (editor != null) {
            return editor.getCellEditorValue();
         }
         return null;
      }

      @Override
      public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
         if (value instanceof String) {
            editor = new DefaultCellEditor(new JTextField());
         }
         else if (value instanceof Boolean) {
            editor = new DefaultCellEditor(new JCheckBox());
         }
         else if (value instanceof Number) {
            editor = table.getDefaultEditor(Number.class);
         }
         return editor.getTableCellEditorComponent(table, value, isSelected, row, column);
      }
   }

   static class PropertiesTableModel extends AbstractTableModel {
      private LanguageEntity subject;
      List<PropertyDescriptor<? extends LanguageEntity, ?>> properties;

      PropertiesTableModel (LanguageEntity subject) {
         this.subject = subject;
         properties = subject.getEntityProperties();
      }

      @Override
      public int getRowCount () {
         return properties.size();
      }

      @Override
      public int getColumnCount () {
         return 2;
      }

      @Override
      public Object getValueAt (int rowIndex, int columnIndex) {
         if (columnIndex == 0)
            return properties.get(rowIndex).getPropertyName();
         else
            return properties.get(rowIndex).castAndGetValue(subject);
      }

      @Override
      public void setValueAt (Object value, int rowIndex, int columnIndex) {
         assert columnIndex == 1 : "only value column can be edited";
         properties.get(rowIndex).castBothAndSetValue(subject, value);
         fireTableCellUpdated(rowIndex, columnIndex);
      }

      @Override
      public boolean isCellEditable (int rowIndex, int columnIndex) {
         return columnIndex == 1 && properties.get(rowIndex).castAndIsEditable(subject);
      }

   }

   static class CustomTableModel extends AbstractTableModel {

      private String[][] data = new String[][] {
            { "Label 0", "Value 0" },
            { "Label 1", "Value 1" }
      };

      public CustomTableModel () {
      }

      @Override
      public int getRowCount () {
         return data.length;
      }

      @Override
      public int getColumnCount () {
         return 2;
      }

      @Override
      public Object getValueAt (int rowIndex, int columnIndex) {
         return data[rowIndex][columnIndex];
      }

      @Override
      public void setValueAt (Object aValue, int rowIndex, int columnIndex) {
         data[rowIndex][columnIndex] = aValue.toString();
         fireTableCellUpdated(rowIndex, columnIndex);
      }

      @Override
      public boolean isCellEditable (int rowIndex, int columnIndex) {
         return columnIndex == 1;
      }
   }
}
