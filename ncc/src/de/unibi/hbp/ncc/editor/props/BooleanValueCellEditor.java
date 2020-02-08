package de.unibi.hbp.ncc.editor.props;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;

public class BooleanValueCellEditor extends DefaultCellEditor {

   public BooleanValueCellEditor () {
      super(new JCheckBox());
      ((JCheckBox) getComponent()).setHorizontalAlignment(JCheckBox.CENTER);
   }
}
