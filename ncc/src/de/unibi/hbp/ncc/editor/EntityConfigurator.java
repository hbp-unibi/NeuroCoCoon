package de.unibi.hbp.ncc.editor;

import com.mxgraph.model.mxICell;
import de.unibi.hbp.ncc.lang.LanguageEntity;

public interface EntityConfigurator {
   void configureEntityAndCell (LanguageEntity entity, mxICell owningCell, EditorToolBar globalState);
   // does not use a generic subtype <E extends LanguageEntity>, because the invocation in addCells cannot supply the generic type info
}
