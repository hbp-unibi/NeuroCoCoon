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

   private void addOneMatch (mxICell cell, List<mxICell> matches) {
      Object value = cell.getValue();
      if (value instanceof LanguageEntity ? matches(cell, (LanguageEntity) value) : matchesOtherValue(cell, value))
         matches.add(cell);
   }

   private void addMatches (mxICell parent, List<mxICell> matches) {
      if (checkVertices && parent.isVertex() || checkEdges && parent.isEdge())
         addOneMatch(parent, matches);
      int count = parent.getChildCount();
      for (int i = 0; i < count; i++)
         addMatches(parent.getChildAt(i), matches);
   }

   public List<mxICell> getMatchingCells (mxIGraphModel graphModel) {
      List<mxICell> matchingCells = new ArrayList<>();
      Object root = graphModel.getRoot();
      if (root instanceof mxICell)
         addMatches((mxICell) root, matchingCells);
      return matchingCells;
   }

   private int matchCounter;

   private void countOneMatch (mxICell cell) {
      Object value = cell.getValue();
      if (value instanceof LanguageEntity ? matches(cell, (LanguageEntity) value) : matchesOtherValue(cell, value))
         matchCounter += 1;
   }

   private void countMatches (mxICell parent) {
      if (checkVertices && parent.isVertex() || checkEdges && parent.isEdge())
         countOneMatch(parent);
      int count = parent.getChildCount();
      for (int i = 0; i < count; i++)
         countMatches(parent.getChildAt(i));
   }

   public int countMatchingCells (mxIGraphModel graphModel) {
      matchCounter = 0;
      Object root = graphModel.getRoot();
      if (root instanceof mxICell)
         countMatches((mxICell) root);
      return matchCounter;
   }

   private boolean haveOneMatch (mxICell cell) {
      Object value = cell.getValue();
      return value instanceof LanguageEntity ? matches(cell, (LanguageEntity) value) : matchesOtherValue(cell, value);
   }

   private boolean haveMatches (mxICell parent) {
      if ((checkVertices && parent.isVertex() || checkEdges && parent.isEdge()) && haveOneMatch(parent))
         return true;
      int count = parent.getChildCount();
      for (int i = 0; i < count; i++)
         if (haveMatches(parent.getChildAt(i)))
            return true;
      return false;
   }

   public boolean haveMatchingCells (mxIGraphModel graphModel) {
      Object root = graphModel.getRoot();
      return root instanceof mxICell && haveMatches((mxICell) root);
   }

   private void printOneMatch (mxICell cell, StringBuilder matches, int indent) {
      Object value = cell.getValue();
      if (value instanceof LanguageEntity ? matches(cell, (LanguageEntity) value) : matchesOtherValue(cell, value)) {
         for (int i = 0; i < indent; i++)
            matches.append('*');
         matches.append(' ')
               .append(cell.isEdge() ? "edge " : (cell.isVertex() ? "vertex " : "??? "))
               .append("id=").append(cell.getId()).append(", value=").append(value)
               .append(", parent=").append(cell.getParent())
               .append('\n');
         if (cell.isEdge()) {
            matches.append("\tsource=").append(cell.getTerminal(true))
                  .append(", target=").append(cell.getTerminal(false))
                  .append('\n');
         }
      }
   }

   private void printMatches (mxICell parent, StringBuilder matches, int indent) {
      if (checkVertices && parent.isVertex() || checkEdges && parent.isEdge())
         printOneMatch(parent, matches, indent);
      int count = parent.getChildCount();
      for (int i = 0; i < count; i++)
         printMatches(parent.getChildAt(i), matches, indent + 1);
   }

   public StringBuilder printMatchingCells (mxIGraphModel graphModel) {
      StringBuilder matchingCells = new StringBuilder();
      Object root = graphModel.getRoot();
      if (root instanceof mxICell)
         printMatches((mxICell) root, matchingCells, 1);
      return matchingCells;
   }

}
