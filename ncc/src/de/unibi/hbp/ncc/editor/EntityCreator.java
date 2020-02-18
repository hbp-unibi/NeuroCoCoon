package de.unibi.hbp.ncc.editor;

import de.unibi.hbp.ncc.lang.LanguageEntity;

import java.io.Serializable;

public interface EntityCreator<E extends LanguageEntity> extends Serializable, TooltipProvider {
   E create ();
   String getIconFileName ();
   String getIconCaption ();
   String getCellStyle ();
   default int getInitialCellWidth () { return 100; }
   int getInitialCellHeight ();
   @Override
   default String getTooltip () { return toString(); }  // entity creators need toString to return their drag&drop tooltip anyway
}
