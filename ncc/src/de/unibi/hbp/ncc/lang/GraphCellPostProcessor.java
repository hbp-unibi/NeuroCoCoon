package de.unibi.hbp.ncc.lang;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;

public interface GraphCellPostProcessor {

   void adjustAndAdoptGraphCell (mxGraph graph, Object parent, mxCell placeholderCell);
}
