package de.unibi.hbp.ncc.lang.props;

import de.unibi.hbp.ncc.editor.props.StringValueCellEditor;
import de.unibi.hbp.ncc.lang.LanguageEntity;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

public class StringProp extends SimpleEditableProp<String> {

   public StringProp (String propName, LanguageEntity owner, String value) {
      super(propName, String.class, owner, value);
   }

   @Override
   public void setValueFromString (String encodedValue) { setValue(encodedValue); }

   @Override
   public TableCellEditor getTableCellEditor (JTable table) { return new StringValueCellEditor(this); }
}
