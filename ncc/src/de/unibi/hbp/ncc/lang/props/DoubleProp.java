package de.unibi.hbp.ncc.lang.props;

import de.unibi.hbp.ncc.editor.props.DoubleValueCellEditor;
import de.unibi.hbp.ncc.editor.props.IntegerValueCellEditor;
import de.unibi.hbp.ncc.lang.LanguageEntity;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

public class DoubleProp extends SimpleEditableProp<Double> {

   public DoubleProp (String propName, LanguageEntity owner, Double value) {
      super(propName, Double.class, owner, value);
   }

   @Override
   public DoubleProp setUnit (String unit) {  // to get the more precise co-variant return type
      super.setUnit(unit);
      return this;
   }

   @Override
   public TableCellEditor getTableCellEditor (JTable table) {
      return new DoubleValueCellEditor(this);
   }
}
