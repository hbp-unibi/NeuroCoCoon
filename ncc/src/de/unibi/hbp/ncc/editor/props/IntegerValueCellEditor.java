package de.unibi.hbp.ncc.editor.props;

import de.unibi.hbp.ncc.lang.props.EditableProp;

import javax.swing.JTextField;

public class IntegerValueCellEditor extends AbstractValueCellEditor<Integer> {

   public IntegerValueCellEditor (EditableProp<Integer> prop) {
      super(prop, JTextField.RIGHT);
   }

   @Override
   protected Integer convertFromString (String s) {
      try {
         return Integer.valueOf(s);
      }
      catch (NumberFormatException nfe) {
         return null;
      }
   }
}
