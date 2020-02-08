package de.unibi.hbp.ncc.lang.props;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import java.util.EnumSet;

public interface EditableProp<T> extends ReadOnlyProp<T> {

   enum Impact { OWN_VALUE, OTHER_PROPS_VALUES, OTHER_PROPS_VISIBILITY, CELL_LABEL, CELL_APPEARANCE, CELL_SIZE }

   boolean isValid (T proposedValue);
   void setValue (T value);
   default void setRawValue (Object rawValue) {
      // System.err.println("setRawValue: " + rawValue + " --> " + getValueClass().getName());
      setValue(getValueClass().cast(rawValue));
   }
   EnumSet<Impact> getChangeImpact ();
   TableCellEditor getTableCellEditor (JTable table);

}
