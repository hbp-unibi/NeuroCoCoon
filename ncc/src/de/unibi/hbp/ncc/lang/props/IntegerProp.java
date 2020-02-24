package de.unibi.hbp.ncc.lang.props;

import de.unibi.hbp.ncc.editor.props.IntegerValueCellEditor;
import de.unibi.hbp.ncc.lang.LanguageEntity;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

public class IntegerProp extends SimpleEditableProp<Integer> {

   public IntegerProp (String propName, LanguageEntity owner, Integer value) {
      super(propName, Integer.class, owner, value);
   }

   @Override
   public void setValueFromString (String encodedValue) { setValue(Integer.valueOf(encodedValue)); }

   @Override
   public IntegerProp setImpact (Impact impact) {  // to get the more precise covariant return type
      super.setImpact(impact);
      return this;
   }

   @Override
   public TableCellEditor getTableCellEditor (JTable table) {
      return new IntegerValueCellEditor(this);
   }
}
