package de.unibi.hbp.ncc.lang.props;

import de.unibi.hbp.ncc.lang.DisplayNamed;
import de.unibi.hbp.ncc.lang.LanguageEntity;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import java.awt.Component;

public class EditableEnumProp<E extends Enum<E>> extends SimpleEditableProp<E> {
   private E[] allEnumValues;

   public EditableEnumProp (String propName, Class<E> valueClass, LanguageEntity owner, E value) {
      super(propName, valueClass, owner, value);
      this.allEnumValues = valueClass.getEnumConstants();
   }

   @Override
   public void setValueFromString (String encodedValue) { setValue(Enum.valueOf(getValueClass(), encodedValue)); }

   @Override
   public EditableEnumProp<E> addImpact (Impact impact) {  // to get the more precise co-variant return type
      super.addImpact(impact);
      return this;
   }

   @Override
   public boolean isValid (E proposedValue) {
      return super.isValid(proposedValue) && proposedValue != null;
   }

   static class SmartListCellRenderer extends  DefaultListCellRenderer {
      @Override
      public Component getListCellRendererComponent (JList<?> list, Object value, int index, boolean isSelected,
                                                     boolean cellHasFocus) {
         // System.err.println("Enum Combo Cell Renderer: " + value + " ?" + (value instanceof DisplayNamed));
         return super.getListCellRendererComponent(list,
                                                   value instanceof DisplayNamed
                                                         ? ((DisplayNamed) value).getDisplayName()
                                                         : value,
                                                   index, isSelected, cellHasFocus);
      }
   }

   // TODO support an optional filter to reduce the set of available values (or to disable the other values)
   @Override
   public TableCellEditor getTableCellEditor (JTable table) {
      JComboBox<E> comboBox = new JComboBox<>(allEnumValues);
      comboBox.setRenderer(new SmartListCellRenderer());
      comboBox.setEditable(false);
      // comboBox.setInputVerifier();
      return new DefaultCellEditor(comboBox);
      // FIXME combobox does NOT commit changes on itemChanged (but only on focus lost and maybe ENTER?)

/*
      DefaultCellEditor editor = new DefaultCellEditor(comboBox);
      comboBox.addItemListener(new ItemListener() {
         @Override
         public void itemStateChanged (ItemEvent e) {
            System.err.println("itemStateChanged: " + e.getItem() + ", " + e.getStateChange());
            System.err.println("comboBox: " + comboBox.getSelectedItem());
            System.err.println("cellEditorListeners: " + Arrays.toString(editor.getCellEditorListeners()));
            // comboBox.actionPerformed(new ActionEvent(editor, 0, ""));
            editor.stopCellEditing();  // this is provided inside the internal delegate, but not installed
         }
      });
      return editor;
*/
   }
}
