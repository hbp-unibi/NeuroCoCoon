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
import java.util.EnumSet;

public class EditableEnumProp<E extends Enum<E>> extends SimpleEditableProp<E> {
   private E[] allEnumValues;

   public EditableEnumProp (String propName, Class<E> valueClass, LanguageEntity owner, E value) {
      super(propName, valueClass, owner, value);
      this.allEnumValues = valueClass.getEnumConstants();
   }

   @Override
   public EditableEnumProp<E> setImpact (EnumSet<Impact> impactSet) {  // to get the more precise co-variant return type
      super.setImpact(impactSet);
      return this;
   }

   @Override
   public EditableEnumProp<E> setImpact (Impact impact) {
      super.setImpact(impact);
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
                                                   value instanceof DisplayNamed ? ((DisplayNamed) value).getDisplayName() : value,
                                                   index, isSelected, cellHasFocus);
      }
   }

   @Override
   public TableCellEditor getTableCellEditor (JTable table) {
      JComboBox<E> comboBox = new JComboBox<>(allEnumValues);
      comboBox.setRenderer(new SmartListCellRenderer());
      comboBox.setEditable(false);
      // comboBox.setInputVerifier();
      return new DefaultCellEditor(comboBox);
   }
}
