package de.unibi.hbp.ncc.editor.props;

import com.mxgraph.swing.mxGraphComponent;
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
import java.util.List;
import java.util.Objects;

public class DetailsEditor {
   private final PropsTableModel tableModel;
   private final JTable table;
   private final JComponent component;

   private static final String SMART_NAV_PREV = "smartNavPrev";
   private static final String SMART_NAV_NEXT = "smartNavNext";

   public DetailsEditor () {
      tableModel = new PropsTableModel();
      Notificator.getInstance().subscribe(tableModel);
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
      // FIXME TAB and SHIFT-TAB and SHIFT-ENTER work, but ENTER alone is somehow consumed?
      ActionMap actions = table.getActionMap();
      actions.put(SMART_NAV_NEXT, new SmartNavigationAction(+1));
      actions.put(SMART_NAV_PREV, new SmartNavigationAction(-1));
      InputMap whenAncestor = table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
      whenAncestor.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), SMART_NAV_NEXT);
      whenAncestor.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK), SMART_NAV_PREV);
      whenAncestor.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), SMART_NAV_NEXT);
      whenAncestor.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK), SMART_NAV_PREV);
      table.setGridColor(Color.LIGHT_GRAY);
      table.setShowGrid(true);
      table.setFillsViewportHeight(true);
      component = new JScrollPane(table);  // both scrollbars as needed
   }

   private class SmartNavigationAction extends AbstractAction {
      private int rowDelta;

      public SmartNavigationAction (int rowDelta) {
         super();
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
         EditableProp<?> prop = tableModel.getEditablePropForRow(row);
         editor = prop.getTableCellEditor(table);
         return editor.getTableCellEditorComponent(table, value, isSelected, row, column);
      }
   }

   static class PropsTableModel extends AbstractTableModel
         implements PropPerRowModel, PropChangeListener {
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

      @Override
      public void propertyChanged (EditableProp<?> changed, int position) {
         if (position != PropChangeListener.UNKNOWN_POSITION)
            fireTableCellUpdated(position, 2);
         else {
            int rowIndex = PropModelSearch.findPosition(this, changed);
            if (rowIndex != PropChangeListener.UNKNOWN_POSITION)
               fireTableCellUpdated(rowIndex, 2);
         }
      }

      @Override
      public void multiplePropertyValuesChanged (LanguageEntity affected) {
         if (affected.equals(subject) || PropModelSearch.haveRowForEntity(this, affected))
            fireTableDataChanged();
      }

      @Override
      public void otherPropertiesVisibilityChanged (LanguageEntity affected) {
         if (affected.equals(subject) || PropModelSearch.haveRowForEntity(this, affected))
            updateSubject(subject);  // force reconstruction of table structure
      }

      boolean isIndirectProp (int rowIndex) { return !subject.equals(getPropForRow(rowIndex).getEnclosingEntity()); }

      @Override
      public int getRowCount () {
         return readOnlyProps.size() + editableProps.size();
      }

      @Override
      public int getColumnCount () {
         return 3;
      }
      @Override
      public ReadOnlyProp<?> getPropForRow (int rowIndex) {
         if (rowIndex < readOnlyProps.size())
            return readOnlyProps.get(rowIndex);
         else
            return editableProps.get(rowIndex - readOnlyProps.size());
      }

      private EditableProp<?> getEditablePropForRow (int rowIndex) {
         assert rowIndex >= readOnlyProps.size() : "value of read-only prop cannot be edited";
         return editableProps.get(rowIndex - readOnlyProps.size());
      }

      @Override
      public Object getValueAt (int rowIndex, int columnIndex) {
         if (columnIndex == 0) {
            ReadOnlyProp<?> prop = getPropForRow(rowIndex);
            return VisualMarkers.getPropertyMarkers(prop, subject);
         }
         else if (columnIndex == 1)
            return getPropForRow(rowIndex).getPropName(true);
         else
            return getPropForRow(rowIndex).getValue();
      }

      @Override
      public void setValueAt (Object value, int rowIndex, int columnIndex) {
         assert columnIndex == 2 : "only value column can be edited";
         if (value != null) {
            EditableProp<?> prop = getEditablePropForRow(rowIndex);
            prop.setRawValue(value);
            Notificator.getInstance().notify(this, prop, rowIndex);
         }
      }

      @Override
      public boolean isCellEditable (int rowIndex, int columnIndex) {
         return columnIndex == 2 && rowIndex >= readOnlyProps.size() &&
               !getPropForRow(rowIndex).getEnclosingEntity().isPredefined();
      }
   }
}
