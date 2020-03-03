package de.unibi.hbp.ncc.lang;

import de.unibi.hbp.ncc.editor.TooltipProvider;

public interface PyNNAssociated extends TooltipProvider {

   String getPyNNClassName ();  // without any module prefixes like "sim."
}
