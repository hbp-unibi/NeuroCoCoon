package de.unibi.hbp.ncc.lang.props;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import java.util.EnumSet;

public interface EditableProp<T> extends ReadOnlyProp<T> {

   enum Impact {
      OWN_VALUE,
      OTHER_PROPS_VALUES, OTHER_PROPS_VISIBILITY,  // belonging to the same entity
      // EXTERNAL_PROPS_VALUES, EXTERNAL_PROPS_VISIBILITY,  // TODO do we need these?
      CELL_LABEL, CELL_STYLE, CELL_STRUCTURE,
      DEPENDENT_CELLS_STYLE }

   boolean isValid (T proposedValue);
   void setValue (T value);
   default void setRawValue (Object rawValue) {
      System.err.println("setRawValue: " + rawValue + " --> " + getValueClass().getName());
      setValue(getValueClass().cast(rawValue));
   }
   EnumSet<Impact> getChangeImpact ();
   TableCellEditor getTableCellEditor (JTable table);

}
