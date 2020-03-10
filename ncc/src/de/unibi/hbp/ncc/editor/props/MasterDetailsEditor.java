package de.unibi.hbp.ncc.editor.props;

import de.unibi.hbp.ncc.NeuroCoCoonEditor;
import de.unibi.hbp.ncc.lang.LanguageEntity;
import de.unibi.hbp.ncc.lang.NamedEntity;
import de.unibi.hbp.ncc.lang.Namespace;
import de.unibi.hbp.ncc.lang.props.EditableProp;

import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.function.Function;

public class MasterDetailsEditor<E extends NamedEntity> {
   private final ListModel<E> listModel;
   private final JTable masterTable;
   private final DetailsEditor detailsEditor;
   private final JComponent component;

   public MasterDetailsEditor (Namespace<E> namespace, Function<Namespace<E>, E> entityCreator,
                               NeuroCoCoonEditor editor) {
      listModel = namespace.getListModel();
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      TableColumnModel columnModel = new DefaultTableColumnModel();
      columnModel.setColumnSelectionAllowed(false);
      TableColumn markerColumn = new TableColumn(0, 30);
      // no header text
      markerColumn.setMaxWidth(30);
      columnModel.addColumn(markerColumn);
      TableColumn nameColumn = new TableColumn(1);
      nameColumn.setHeaderValue("Name");
      nameColumn.setCellEditor(new SelectAllCellEditor());
      columnModel.addColumn(nameColumn);
      MasterTableModel masterModel = new MasterTableModel();
      masterTable = new JTable(masterModel, columnModel);
      masterTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      masterTable.setRowSelectionAllowed(true);
      masterTable.setGridColor(Color.LIGHT_GRAY);
      masterTable.setShowGrid(true);
      masterTable.setFillsViewportHeight(true);
      JScrollPane scrollPane = new JScrollPane(masterTable,
                                               ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                                               ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      panel.add(scrollPane);
      component = panel;
      JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
      final JButton addButton = new JButton("New");
      addButton.addActionListener(e -> {
         E entity = entityCreator.apply(namespace);
         if (entity != null)
            setSelectedEntity(entity);
         else  // in rare cases creation may fail, e.g., DataPlot requires at least one NeuronPopulation
            JOptionPane.showMessageDialog(component,
                                          "Could not create new " + namespace.getDescription() + "!\n" +
                                                "\nPrecondition not satisfied.",
                                          "Creation failed!", JOptionPane.ERROR_MESSAGE);
      });
      final JButton duplicateButton = new JButton("Duplicate");
      duplicateButton.addActionListener(e -> {
         E entity = getSelectedEntity();
         if (entity != null)
            setSelectedEntityRaw(entity.duplicate());
         // or preserve the current selection for further duplicates instead
      });
      final JButton deleteButton = new JButton("Delete");
      deleteButton.addActionListener(e -> {
         E candidateEntity = getSelectedEntity();
         if (candidateEntity != null && !namespace.remove(candidateEntity, editor.getGraphModel()))
            JOptionPane.showMessageDialog(component,
                                          "Could not delete " + namespace.getDescription() +
                                                "'" + candidateEntity.getName() + "'" + "!\n" +
                                                "References to this entity still exist.",
                                          "Deletion failed!", JOptionPane.ERROR_MESSAGE);
      });
      duplicateButton.setEnabled(false);  // configure buttons for initial state: nothing selected
      deleteButton.setEnabled(false);
      buttonBar.add(addButton);
      buttonBar.add(duplicateButton);
      buttonBar.add(deleteButton);
      panel.add(buttonBar);
      detailsEditor = new DetailsEditor();
      panel.add(detailsEditor.getComponent());
      masterTable.getSelectionModel().addListSelectionListener(e -> {
         E entity = getSelectedEntity();
         detailsEditor.setSubject(entity);
         duplicateButton.setEnabled(entity != null);
         deleteButton.setEnabled(entity != null && !entity.isPredefined());
      });
   }

   private E getSelectedEntity () {
      int selectedRow = masterTable.getSelectedRow();
      if (0 <= selectedRow && selectedRow < listModel.getSize())
         return listModel.getElementAt(selectedRow);
      else
         return null;
   }

   @SuppressWarnings("unchecked")  // TODO maybe generify DetailsEditor with <E extends LanguageEntity>, too
   private void setSelectedEntityRaw (LanguageEntity entity) {
      setSelectedEntity((E) entity);
   }

   private void setSelectedEntity (E entity) {
      if (entity == null)
         return;
      int selectedRow = PropModelSearch.findPosition(listModel, entity);
      if (selectedRow != PropChangeListener.UNKNOWN_POSITION)
         masterTable.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
      else
         masterTable.clearSelection();
   }

   public JComponent getComponent () {
      return component;
   }

   private EditableProp<String> getNamePropForRow (int rowIndex) {
      return listModel.getElementAt(rowIndex).getNameProp();
   }

   class MasterTableModel extends AbstractTableModel {

      public MasterTableModel () {
         listModel.addListDataListener(new ListDataListener() {

            private void restoreSelectedEntity () {
               if (!masterTable.getSelectionModel().isSelectionEmpty()) {
                  setSelectedEntityRaw(detailsEditor.getSubject());
               }
            }

            @Override
            public void intervalAdded (ListDataEvent e) {
               fireTableRowsInserted(e.getIndex0(), e.getIndex1());
               restoreSelectedEntity();  // could be limited to cases, where selectedIndex >= e.getIndex0()
            }

            @Override
            public void intervalRemoved (ListDataEvent e) {
               fireTableRowsDeleted(e.getIndex0(), e.getIndex1());
               restoreSelectedEntity();  // could be limited to cases, where selectedIndex >= e.getIndex0()
            }

            @Override
            public void contentsChanged (ListDataEvent e) {
               // should also suffice to just update all name column cells in the range, but that cannot be done in bulk
               fireTableRowsUpdated(e.getIndex0(), e.getIndex1());
               restoreSelectedEntity();  // could be limited to cases, where e.getIndex0() <= selectedIndex <= e.getIndex1()
            }
         });
      }

      @Override
      public int getRowCount () {
         return listModel.getSize();
      }

      @Override
      public int getColumnCount () {
         return 2;
      }

      @Override
      public Object getValueAt (int rowIndex, int columnIndex) {
         if (columnIndex == 0) {
            return VisualMarkers.getEntityMarkers(listModel.getElementAt(rowIndex));
         }
         else
            return listModel.getElementAt(rowIndex).getName();
      }

      @Override
      public void setValueAt (Object value, int rowIndex, int columnIndex) {
         assert columnIndex == 1 : "only name column can be edited";
         if (value != null) {
            EditableProp<String> nameProp = getNamePropForRow(rowIndex);
            nameProp.setRawValue(value);
            // fireTableCellUpdated(rowIndex, columnIndex);
            Notificator.getInstance().notify(nameProp);
         }
      }

      @Override
      public boolean isCellEditable (int rowIndex, int columnIndex) {
         return columnIndex == 1 &&
               !listModel.getElementAt(rowIndex).isPredefined();
      }
   }

}
