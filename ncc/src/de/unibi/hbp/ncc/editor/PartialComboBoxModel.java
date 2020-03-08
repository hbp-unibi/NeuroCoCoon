package de.unibi.hbp.ncc.editor;

import org.jetbrains.annotations.NotNull;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import java.awt.Component;

public abstract class PartialComboBoxModel<E> extends DefaultComboBoxModel<E> {
   protected PartialComboBoxModel (E[] items) {
      super(items);
   }

   protected abstract boolean isAllowed (@NotNull Object value);  // callers cannot guarantee generic type E anyway

   @Override
   public void setSelectedItem (Object anObject) {
      if (anObject == null || isAllowed(anObject))
         super.setSelectedItem(anObject);
   }

   public JComboBox<E> buildComboBox () {
      JComboBox<E> comboBox = new JComboBox<>(this);
      comboBox.setRenderer(new DefaultListCellRenderer() {
         @Override
         public Component getListCellRendererComponent (JList<?> list, Object value, int index, boolean isSelected,
                                                        boolean cellHasFocus) {
            boolean itemAllowed = value != null && isAllowed(value);
            Component component =  super.getListCellRendererComponent(list, value, index,
                                                                      itemAllowed && isSelected,
                                                                      cellHasFocus);
            // DefaultListCellRenderer returns this (a JLabel) so configure it according to our needs
            setEnabled(itemAllowed);
            return component;
         }
      });
      return comboBox;
   }
}
