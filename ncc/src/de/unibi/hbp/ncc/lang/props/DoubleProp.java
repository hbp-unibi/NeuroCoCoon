package de.unibi.hbp.ncc.lang.props;

import de.unibi.hbp.ncc.editor.props.DoubleValueCellEditor;
import de.unibi.hbp.ncc.lang.LanguageEntity;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import java.util.EnumSet;

public class DoubleProp extends SimpleEditableProp<Double> {

   public DoubleProp (String propName, LanguageEntity owner, Double value) {
      super(propName, Double.class, owner, value);
   }

   @Override
   public void setValueFromString (String encodedValue) { setValue(Double.valueOf(encodedValue)); }

   @Override
   public DoubleProp setUnit (String unit) {  // to get the more precise co-variant return type
      super.setUnit(unit);
      return this;
   }

   @Override
   public DoubleProp setImpact (EnumSet<Impact> impactSet) {  // to get the more precise co-variant return type
      super.setImpact(impactSet);
      return this;
   }

   @Override
   public DoubleProp setImpact (Impact impact) {  // to get the more precise co-variant return type
      super.setImpact(impact);
      return this;
   }

   @Override
   public TableCellEditor getTableCellEditor (JTable table) {
      return new DoubleValueCellEditor(this);
   }
}
