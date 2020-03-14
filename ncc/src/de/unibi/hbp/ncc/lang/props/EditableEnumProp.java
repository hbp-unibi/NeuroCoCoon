package de.unibi.hbp.ncc.lang.props;

import de.unibi.hbp.ncc.lang.LanguageEntity;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

public class EditableEnumProp<E extends Enum<E>> extends SimpleEditableProp<E> {
   protected E[] allEnumValues;

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

   protected TableCellEditor configureEditor (JComboBox<E> comboBox) {
      comboBox.setRenderer(new SmartListCellRenderer());
      comboBox.setEditable(false);
      // comboBox.setInputVerifier();
      return new DefaultCellEditor(comboBox);
   }

   // TODO support an optional filter to reduce the set of available values (or to disable the other values)
   @Override
   public TableCellEditor getTableCellEditor (JTable table) {
      return configureEditor(new JComboBox<>(allEnumValues));
   }
}
