package de.unibi.hbp.ncc.lang.props;

import de.unibi.hbp.ncc.editor.props.IntegerValueCellEditor;
import de.unibi.hbp.ncc.lang.LanguageEntity;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

public class IntegerProp extends SimpleEditableProp<Integer> {

   public IntegerProp (String propName, LanguageEntity owner, Integer value) {
      super(propName, Integer.class, owner, value);
   }

   @Override
   public TableCellEditor getTableCellEditor (JTable table) {
      return new IntegerValueCellEditor(this);
   }
}
