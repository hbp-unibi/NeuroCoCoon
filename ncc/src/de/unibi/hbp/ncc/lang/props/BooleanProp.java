package de.unibi.hbp.ncc.lang.props;

import de.unibi.hbp.ncc.lang.LanguageEntity;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

public class BooleanProp extends SimpleEditableProp<Boolean> {

   public BooleanProp (String propName, LanguageEntity owner, Boolean value) {
      super(propName, Boolean.class, owner, value);
   }

   @Override
   public void setValueFromString (String encodedValue) { setValue(Boolean.valueOf(encodedValue)); }

   @Override
   public BooleanProp setPythonName (String pythonName) {  // to get the more precise co-variant return type
      super.setPythonName(pythonName);
      return this;
   }

   @Override
   public BooleanProp addImpact (Impact impact) {  // to get the more precise co-variant return type
      super.addImpact(impact);
      return this;
   }

   @Override
   public TableCellEditor getTableCellEditor (JTable table) {
      return table.getDefaultEditor(Boolean.class);
   }
}
