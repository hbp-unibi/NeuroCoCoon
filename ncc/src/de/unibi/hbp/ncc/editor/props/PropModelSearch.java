package de.unibi.hbp.ncc.editor.props;

import de.unibi.hbp.ncc.lang.LanguageEntity;
import de.unibi.hbp.ncc.lang.props.EditableProp;
import de.unibi.hbp.ncc.lang.props.ReadOnlyProp;

import javax.swing.ListModel;

final public class PropModelSearch {

   private PropModelSearch () { }  // static helper methods only

   // probably only needed for editable properties but also works for read only properties
   public static PropPerRowModel asPropModel (final ListModel<? extends ReadOnlyProp<?>> listModel) {
      return new PropPerRowModel() {
         @Override
         public int getRowCount () {
            return listModel.getSize();
         }

         @Override
         public ReadOnlyProp<?> getPropForRow (int rowIndex) {
            return listModel.getElementAt(rowIndex);
         }
      };
   }

   public static <E extends LanguageEntity> int findPosition (ListModel<E> listModel, E entity) {
      int rowCount = listModel.getSize();
      for (int rowIndex = 0; rowIndex < rowCount; rowIndex++)
         if (entity.equals(listModel.getElementAt(rowIndex)))
            return rowIndex;
      return PropChangeListener.UNKNOWN_POSITION;
   }

   public static int findPosition (PropPerRowModel propModel, EditableProp<?> prop) {
      int rowCount = propModel.getRowCount();
      for (int rowIndex = 0; rowIndex < rowCount; rowIndex++)
         if (prop.equals(propModel.getPropForRow(rowIndex)))
            return rowIndex;
      return PropChangeListener.UNKNOWN_POSITION;
   }

   public static boolean haveRowForEntity (PropPerRowModel propModel, LanguageEntity entity) {
      int rowCount = propModel.getRowCount();
      for (int rowIndex = 0; rowIndex < rowCount; rowIndex++)
         if (entity.equals(propModel.getPropForRow(rowIndex).getEnclosingEntity()))
            return true;
      return false;
   }
}
