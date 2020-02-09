package de.unibi.hbp.ncc.editor.props;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.Objects;

public class ComboBoxModelAdapter<E> extends AbstractListModel<E> implements ComboBoxModel<E> {

   private ListModel<E> delegate;
   private Object selectedObject;

   public ComboBoxModelAdapter (ListModel<E> delegate) {
      this.delegate = delegate;
      delegate.addListDataListener(new ListDataListener() {
         @Override
         public void intervalAdded (ListDataEvent e) {
            // nothing to do, selection is still present
         }

         @Override
         public void intervalRemoved (ListDataEvent e) {
            updateSelection();  // selection might have been removed
         }

         @Override
         public void contentsChanged (ListDataEvent e) {
            updateSelection();  // selection might have been changed
         }

         private void updateSelection () {
            if (selectedObject != null) {
               int size = delegate.getSize();
               for (int index = 0; index <= size; index++)
                  if (selectedObject.equals(delegate.getElementAt(index)))
                     return;
               if (size > 0)
                  setSelectedItem(delegate.getElementAt(0));
               else
                  setSelectedItem(null);
            }
         }
      });
   }

   @Override
   public void setSelectedItem(Object anObject) {
      if (!Objects.equals(anObject, selectedObject)) {
         selectedObject = anObject;
         fireContentsChanged(this, -1, -1);
      }
   }

   @Override
   public Object getSelectedItem() { return selectedObject; }

   @Override
   public int getSize () {
      return delegate.getSize();
   }

   @Override
   public E getElementAt (int index) {
      return delegate.getElementAt(index);
   }

   @Override
   public void addListDataListener (ListDataListener l) {
      super.addListDataListener(l);
      delegate.addListDataListener(l);
   }

   @Override
   public void removeListDataListener (ListDataListener l) {
      delegate.removeListDataListener(l);
      super.removeListDataListener(l);
   }
}
