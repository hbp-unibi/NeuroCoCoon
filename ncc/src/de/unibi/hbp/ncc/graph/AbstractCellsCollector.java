package de.unibi.hbp.ncc.graph;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxIGraphModel;
import de.unibi.hbp.ncc.lang.LanguageEntity;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCellsCollector {

   protected abstract boolean matches (mxCell cell, LanguageEntity entity);

   // TODO do we need a second predicate for cells with non-LanguageEntity valuse?

   private void addMatches (mxIGraphModel graphModel, mxCell parent, List<mxCell> matches) {
      Object value = parent.getValue();
      if (value instanceof LanguageEntity && matches(parent, (LanguageEntity) value))
         matches.add(parent);
      int count = graphModel.getChildCount(parent);
      for (int i = 0; i < count; i++) {
         Object obj = graphModel.getChildAt(parent, i);
         if (obj instanceof mxCell)
            addMatches(graphModel, (mxCell) obj, matches);
      }
   }

   public List<mxCell> getMatchingCells (mxIGraphModel graphModel) {
      List<mxCell> matchingCells = new ArrayList<>();
      Object root = graphModel.getRoot();
      if (root instanceof mxCell)
         addMatches(graphModel, (mxCell) root, matchingCells);
      return matchingCells;
   }

   private boolean haveMatches (mxIGraphModel graphModel, mxCell parent) {
      Object value = parent.getValue();
      if (value instanceof LanguageEntity && matches(parent, (LanguageEntity) value))
         return true;
      int count = graphModel.getChildCount(parent);
      for (int i = 0; i < count; i++) {
         Object obj = graphModel.getChildAt(parent, i);
         if (obj instanceof mxCell && haveMatches(graphModel, (mxCell) obj))
            return true;
      }
      return false;
   }

   public boolean haveMatchingCells (mxIGraphModel graphModel) {
      List<mxCell> matchingCells = new ArrayList<>();
      Object root = graphModel.getRoot();
      return root instanceof mxCell && haveMatches(graphModel, (mxCell) root);
   }
}
