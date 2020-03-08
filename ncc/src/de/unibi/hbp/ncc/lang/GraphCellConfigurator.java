package de.unibi.hbp.ncc.lang;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.view.mxGraph;

public interface GraphCellConfigurator {

   void configurePlaceholder (mxGraph graph, mxCell placeholderCell);
   // this needs to adjust the cell itself in ways which are not possible with just the interface type mxICell

   void restructureExisting (mxGraph graph, mxICell existingCell);  // this needs to create/destroy children
   void resizeExisting (mxGraph graph, mxICell existingCell);  // this just needs to reposition children
}
