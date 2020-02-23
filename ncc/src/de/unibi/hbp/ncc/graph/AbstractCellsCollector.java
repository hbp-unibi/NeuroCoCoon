package de.unibi.hbp.ncc.graph;

import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import de.unibi.hbp.ncc.lang.LanguageEntity;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCellsCollector {
   private boolean checkVertices, checkEdges;

   protected AbstractCellsCollector (boolean checkVertices, boolean checkEdges) {
      this.checkVertices = checkVertices;
      this.checkEdges = checkEdges;
   }

   protected abstract boolean matches (mxICell cell, LanguageEntity entity);

   protected boolean matchesOtherValue (mxICell cell, Object value) {
      return false;
   }

   // looks like edges have to be visited separately
   // TODO visit edges only once: from their source node, unless there is no source node

   private void addOneMatch (mxICell cell, List<mxICell> matches) {
      Object value = cell.getValue();
      if (value instanceof LanguageEntity && matches(cell, (LanguageEntity) value) ||
            matchesOtherValue(cell, value))
         matches.add(cell);
   }

   private void addMatches (mxICell parent, List<mxICell> matches) {
      if (checkVertices && parent.isVertex())
         addOneMatch(parent, matches);
      int count = parent.getChildCount();
      for (int i = 0; i < count; i++)
            addMatches(parent.getChildAt(i), matches);
      if (checkEdges) {
         int edgeCount = parent.getEdgeCount();
         for (int i = 0; i < edgeCount; i++)
            addOneMatch(parent.getEdgeAt(i), matches);
      }
   }

   public List<mxICell> getMatchingCells (mxIGraphModel graphModel) {
      List<mxICell> matchingCells = new ArrayList<>();
      Object root = graphModel.getRoot();
      if (root instanceof mxICell)
         addMatches((mxICell) root, matchingCells);
      return matchingCells;
   }

   private boolean haveOneMatch (mxICell cell) {
      Object value = cell.getValue();
      return value instanceof LanguageEntity && matches(cell, (LanguageEntity) value) ||
            matchesOtherValue(cell, value);
   }

   private boolean haveMatches (mxICell parent) {
      if (checkVertices && parent.isVertex() && haveOneMatch(parent))
         return true;
      int count = parent.getChildCount();
      for (int i = 0; i < count; i++)
         if (haveMatches(parent.getChildAt(i)))
            return true;
      if (checkEdges) {
         int edgeCount = parent.getEdgeCount();
         for (int i = 0; i < edgeCount; i++)
            if (haveOneMatch(parent.getEdgeAt(i)))
               return true;
      }
      return false;
   }

   public boolean haveMatchingCells (mxIGraphModel graphModel) {
      Object root = graphModel.getRoot();
      return root instanceof mxICell && haveMatches((mxICell) root);
   }

   private void printOneMatch (mxICell cell, StringBuilder matches, int indent) {
      Object value = cell.getValue();
      if (value instanceof LanguageEntity && matches(cell, (LanguageEntity) value) ||
            matchesOtherValue(cell, value)) {
         for (int i = 0; i < indent; i++)
            matches.append('*');
         matches.append(' ')
               .append(cell.isEdge() ? "edge " : (cell.isVertex() ? "vertex " : "??? "))
               .append("id=").append(cell.getId()).append(", value=").append(value)
               .append(", parent=").append(cell.getParent())
               .append('\n');
      }
   }

   private void printMatches (mxICell parent, StringBuilder matches, int indent) {
      if (checkVertices && parent.isVertex())
         printOneMatch(parent, matches, indent);
      int count = parent.getChildCount();
      for (int i = 0; i < count; i++)
         printMatches(parent.getChildAt(i), matches, indent + 1);
      if (checkEdges) {
         int edgeCount = parent.getEdgeCount();
         for (int i = 0; i < edgeCount; i++)
            printOneMatch(parent.getEdgeAt(i), matches, indent);
      }
   }

   public StringBuilder printMatchingCells (mxIGraphModel graphModel) {
      StringBuilder matchingCells = new StringBuilder();
      Object root = graphModel.getRoot();
      if (root instanceof mxICell)
         printMatches((mxICell) root, matchingCells, 1);
      return matchingCells;
   }

}
