package de.unibi.hbp.ncc.lang;

import de.unibi.hbp.ncc.editor.TooltipProvider;

public interface DisplayNamed extends TooltipProvider {

   String getDisplayName ();
   default String getLongDisplayName () { return getDisplayName(); }
   default String getShortDisplayName () { return getDisplayName(); }

   @Override
   default String getTooltip () { return getLongDisplayName(); }
}
