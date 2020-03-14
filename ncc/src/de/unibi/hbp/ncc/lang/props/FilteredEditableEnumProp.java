package de.unibi.hbp.ncc.lang.props;

import de.unibi.hbp.ncc.lang.LanguageEntity;

import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import java.util.Vector;

public abstract class FilteredEditableEnumProp<E extends Enum<E>> extends EditableEnumProp<E> {

   protected FilteredEditableEnumProp (String propName, Class<E> valueClass, LanguageEntity owner, E value) {
      super(propName, valueClass, owner, value);
   }

   protected abstract boolean isValidFor (E enumValue, LanguageEntity enclosingEntity);

   @Override
   public boolean isValid (E proposedValue) {
      return super.isValid(proposedValue) && isValidFor(proposedValue, getEnclosingEntity());
   }

   public boolean hasValidValue () {  // for semantic checks, entity might have been reconfigured since last setValue() did the validation
      return isValidFor(getValue(), getEnclosingEntity());
   }

   @Override
   public TableCellEditor getTableCellEditor (JTable table) {
      Vector<E> filteredValues = new Vector<>(allEnumValues.length);
      LanguageEntity enclosingEntity = getEnclosingEntity();
      for (E value: allEnumValues)
         if (isValidFor(value, enclosingEntity))
            filteredValues.add(value);
      return configureEditor(new JComboBox<>(filteredValues));
   }
}
