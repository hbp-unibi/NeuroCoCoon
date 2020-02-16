package de.unibi.hbp.ncc.editor.props;

import de.unibi.hbp.ncc.lang.props.ReadOnlyProp;

public interface PropPerRowModel {
   int getRowCount ();
   ReadOnlyProp<?> getPropForRow (int rowIndex);
}
