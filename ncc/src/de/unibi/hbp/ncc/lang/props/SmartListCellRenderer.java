package de.unibi.hbp.ncc.lang.props;

import de.unibi.hbp.ncc.lang.DisplayNamed;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import java.awt.Component;

class SmartListCellRenderer extends DefaultListCellRenderer {
   @Override
   public Component getListCellRendererComponent (JList<?> list, Object value, int index, boolean isSelected,
                                                  boolean cellHasFocus) {
      return super.getListCellRendererComponent(list,
                                                value instanceof DisplayNamed
                                                      ? ((DisplayNamed) value).getDisplayName()
                                                      : value,
                                                index, isSelected, cellHasFocus);
   }
}
