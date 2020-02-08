package de.unibi.hbp.ncc.editor.props;

import de.unibi.hbp.ncc.lang.props.EditableProp;

import javax.swing.JTextField;

public class DoubleValueCellEditor extends AbstractValueCellEditor<Double> {

   public DoubleValueCellEditor (EditableProp<Double> prop) {
      super(prop, JTextField.RIGHT);
   }

   @Override
   protected Double convertFromString (String s) {
      try {
         return Double.valueOf(s);
      }
      catch (NumberFormatException nfe) {
         return null;
      }
   }
}
