package de.unibi.hbp.ncc.editor.props;

import de.unibi.hbp.ncc.editor.TooltipProvider;
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
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
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
import java.util.EventObject;
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
      TableColumn labelColumn = new TableColumn(1, 100);
      labelColumn.setHeaderValue("Property");
      labelColumn.setCellRenderer(new PropNameCellRenderer(tableModel));
      tableColumnModel.addColumn(labelColumn);
      TableColumn valueColumn = new TableColumn(2, 150);
      valueColumn.setHeaderValue("Value");
      valueColumn.setCellRenderer(new PropValueCellRenderer());
      valueColumn.setCellEditor(new PropValueCellEditor(tableModel));
      tableColumnModel.addColumn(valueColumn);
      table = new JTable(tableModel, tableColumnModel);
      // replace action for ENTER, since next row would be selected automatically
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
            // if (editor instanceof JTextComponent) ((JTextComponent) editor).selectAll();
            // TODO should no longer be necessary? (SelectAllEditor does this hopefully)
         }
      }
   }

   public void setSubject (LanguageEntity subject) {
      if (!Objects.equals(subject, tableModel.getSubject())) {
         if (table.isEditing()) {
            TableCellEditor editor = table.getCellEditor();
            if (editor != null && !editor.stopCellEditing())
               editor.cancelCellEditing();
         }
         tableModel.updateSubject(subject);
      }
   }

   LanguageEntity getSubject () { return tableModel.subject; }  // so that MasterDetailsEditor can re-establish its selection in the master table

   public void startEditing () {
      if (table.editCellAt(0, 2)) {  // first row, value column
         Component editor = table.getEditorComponent();
         editor.requestFocusInWindow();
      }
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
         // returns this (a JLabel subclass instance)
         if (normalCellFont == null) {
            normalCellFont = getFont();
            italicsCellFont = new Font(normalCellFont.getName(), Font.ITALIC, normalCellFont.getSize());
         }
         setFont(column == 1 && tableModel.isIndirectProp(row) ? italicsCellFont : normalCellFont);
         if (value instanceof TooltipProvider)
            setToolTipText(((TooltipProvider) value).getTooltip());
         else
            setToolTipText(value.toString());
         return component;
      }
   }

   static class PropValueCellRenderer extends DefaultTableCellRenderer {

      @Override
      public Component getTableCellRendererComponent (JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                      int row, int column) {
         // System.err.println("Prop Value Cell Renderer: " + value + ", class=" + value.getClass().getName());
         if (value instanceof Boolean)
            return table.getDefaultRenderer(Boolean.class).getTableCellRendererComponent(table, value, isSelected,
                                                                                         hasFocus, row, column);
         setHorizontalAlignment(value instanceof Number ? RIGHT : LEFT);
         return super.getTableCellRendererComponent(table,
                                                    value instanceof DisplayNamed
                                                          ? ((DisplayNamed) value).getDisplayName()
                                                          : value,
                                                    isSelected, hasFocus, row, column);
      }
   }

   static class PropValueCellEditor extends AbstractCellEditor
         implements TableCellEditor, CellEditorListener {
      private PropsTableModel tableModel;
      private TableCellEditor editor;

      PropValueCellEditor (PropsTableModel tableModel) {
         this.tableModel = tableModel;
      }

      @Override
      public Object getCellEditorValue () {
         return editor != null ? editor.getCellEditorValue() : null;
      }

      @Override
      public boolean isCellEditable (EventObject e) {
//         System.err.println("PropValueCellEditor.isCellEditable");
         return true;
         // do NOT delegate this to the (old) editor: might require a double-click,
         // even if the not yet installed new editor would not
         // the result is that editing starts with a single click always, which is even desirable
         // return editor != null ? editor.isCellEditable(e) : super.isCellEditable(e);
      }

      @Override
      public boolean shouldSelectCell (EventObject e) {
//         System.err.println("PropValueCellEditor.shouldSelectCell");
         return true;
         // do NOT delegate this to the (old) editor: new editor is not installed yet
         // return editor != null ? editor.shouldSelectCell(e) : super.shouldSelectCell(e);
      }

      @Override
      public boolean stopCellEditing () {
//         System.err.println("PropValueCellEditor.stopCellEditing");
         return editor != null ? editor.stopCellEditing() : super.stopCellEditing();
         // TODO seems to be called twice in many situations: caused by focusLost listener?
      }

      @Override
      public void cancelCellEditing () {
//         System.err.println("PropValueCellEditor.cancelCellEditing");
         if (editor != null)
            editor.cancelCellEditing();
         else
            super.cancelCellEditing();
      }

      @Override
      public void editingStopped (ChangeEvent e) {
//         System.err.println("PropValueCellEditor.editingStopped");
         fireEditingStopped();  // propagate event from auxiliary editor to our listeners
      }

      @Override
      public void editingCanceled (ChangeEvent e) {
//         System.err.println("PropValueCellEditor.editingCanceled");
         fireEditingCanceled();  // propagate event from auxiliary editor to our listeners
      }

      @Override
      public Component getTableCellEditorComponent (JTable table, Object value, boolean isSelected, int row, int column) {
//         System.err.println("PropValueCellEditor.getTableCellEditorComponent: old editor = " + editor);
         EditableProp<?> prop = tableModel.getEditablePropForRow(row);
         if (editor != null)
            editor.removeCellEditorListener(this);
         editor = prop.getTableCellEditor(table);
         editor.addCellEditorListener(this);
//         System.err.println("PropValueCellEditor.getTableCellEditorComponent: new editor = " + editor);
         return editor.getTableCellEditorComponent(table, value, isSelected, row, column);
      }

      // should we listen for setCellEditor(null) property change in the JTable and de-register as  a listener earlier?
   }

   static class PropsTableModel extends AbstractTableModel
         implements PropPerRowModel, PropChangeListener {
      LanguageEntity subject;
      List<ReadOnlyProp<?>> readOnlyProps;
      List<EditableProp<?>> editableProps;

      PropsTableModel () {
         subject = null;
         readOnlyProps = Collections.emptyList();
         editableProps = Collections.emptyList();
      }

      LanguageEntity getSubject () { return subject; }

      void updateSubject (LanguageEntity subject) {
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
            Notificator.getInstance().notifyListeners(this, prop, rowIndex);
         }
      }

      @Override
      public boolean isCellEditable (int rowIndex, int columnIndex) {
         return columnIndex == 2 && rowIndex >= readOnlyProps.size() &&
               !getPropForRow(rowIndex).getEnclosingEntity().isPredefined();
      }
   }
}
