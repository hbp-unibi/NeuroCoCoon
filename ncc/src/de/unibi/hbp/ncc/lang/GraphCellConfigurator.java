package de.unibi.hbp.ncc.lang;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;

public interface GraphCellConfigurator {

   void configurePlaceholder (mxGraph graph, mxCell placeholderCell);
   void restructureExisting (mxGraph graph, mxCell existingCell);  // this needs to create/destroy children
   void resizeExisting (mxGraph graph, mxCell existingCell);  // this just needs to reposition children
}
