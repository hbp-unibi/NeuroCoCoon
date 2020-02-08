package de.unibi.hbp.ncc.editor.props;

import de.unibi.hbp.ncc.lang.props.EditableProp;

public class StringValueCellEditor extends AbstractValueCellEditor<String> {

   public StringValueCellEditor (EditableProp<String> prop) {
      super(prop);
   }

   @Override
   protected String convertFromString (String s) {
      return s != null ? s : "";
   }
}
