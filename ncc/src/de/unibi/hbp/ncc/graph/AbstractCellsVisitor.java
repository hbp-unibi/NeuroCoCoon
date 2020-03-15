package de.unibi.hbp.ncc.graph;

import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import de.unibi.hbp.ncc.lang.LanguageEntity;
import de.unibi.hbp.ncc.lang.NetworkModule;

import java.util.function.BiConsumer;

public abstract class AbstractCellsVisitor {
   private final boolean visitEdges;
   private boolean cancelled;

   protected AbstractCellsVisitor (boolean visitEdges) {
      this.visitEdges = visitEdges;
   }

   protected void cancelVisit () { cancelled = true; }
   protected boolean isCancelled () { return cancelled; }

   protected void beginGraph (mxICell root) { }
   protected void endGraph (mxICell root) { }
   protected void beginEntityVertex (mxICell vertex, LanguageEntity entity) { }
   protected void beginPortVertex (mxICell vertex, NetworkModule.Port port) { }
   protected void beginOtherVertex (mxICell vertex, Object value) { }
   protected void endVertex (mxICell vertex) { }

   protected void visitEdge (mxICell edge, mxICell source, mxICell target, LanguageEntity entity, boolean isIncoming) {
      if (source == null && target == null)
         visitUnconnectedEdge(edge, entity);
      else if (source == null || target == null)
         visitDanglingEdge(edge, source == null ? target : source, entity, isIncoming);
      else if (source.equals(target))
         visitLoopEdge(edge, source, entity);
      else if (isIncoming)
         visitIncomingEdge(edge, source, target, entity);
      else
         visitOutgoingEdge(edge, source, target, entity);
   }

   protected void visitOtherEdge (mxICell edge, mxICell source, mxICell target, Object value) { }

   protected void visitLoopEdge (mxICell edge, mxICell sourceAndTarget, LanguageEntity entity) { }
   protected void visitDanglingEdge (mxICell edge, mxICell soleConnectedCell, LanguageEntity entity, boolean isIncoming) { }
   protected void visitUnconnectedEdge (mxICell edge, LanguageEntity entity) { }

   protected void visitNormalEdge (mxICell edge, mxICell source, mxICell target, LanguageEntity entity) { }

   protected void visitIncomingEdge (mxICell edge, mxICell source, mxICell target, LanguageEntity entity) {
      visitNormalEdge(edge, source, target, entity);
   }

   protected void visitOutgoingEdge (mxICell edge, mxICell source, mxICell target, LanguageEntity entity) {
      visitNormalEdge(edge, source, target, entity);
   }

   // looks like edges have to be visited separately
   // visits edges only once: from their source node, dangling edges without source node are visited from their target
   // completely detached edges are visited separately (in the end)

   private static boolean isUnconnectedEdge (mxICell cell) {
      return cell.isEdge() && cell.getTerminal(true) == null && cell.getTerminal(false) == null;
   }

   private void beginVertexCell (mxICell vertexCell) {
      assert vertexCell.isVertex();
      Object value = vertexCell.getValue();
      if (value instanceof LanguageEntity)
         beginEntityVertex(vertexCell, (LanguageEntity) value);
      else if (value instanceof NetworkModule.Port)
         beginPortVertex(vertexCell, (NetworkModule.Port) value);
      else
         beginOtherVertex(vertexCell, value);
   }

   private void visitEdgeCell (mxICell edgeCell, boolean isIncoming) {
      assert edgeCell.isEdge();
      Object value = edgeCell.getValue();
      mxICell source = edgeCell.getTerminal(true);
      mxICell target = edgeCell.getTerminal(false);
      if (value instanceof LanguageEntity)
         visitEdge(edgeCell, source, target, (LanguageEntity) value, isIncoming);
      else
         visitOtherEdge(edgeCell, source, target, value);
   }

   private void visitSubgraph (mxICell parent) {
      if (parent.isVertex()) {
         if (visitEdges) {
            int edgeCount = parent.getEdgeCount();
            for (int i = 0; i < edgeCount && !cancelled; i++) {
               mxICell edge = parent.getEdgeAt(i);
               mxICell target = edge.getTerminal(false);
               // visit only incoming edges
               if (parent.equals(target))
                  visitEdgeCell(edge, true);
            }
         }
         beginVertexCell(parent);
      }
      // always visit children, even for non-vertex cells (like the root of a graph)
      int count = parent.getChildCount();
      for (int i = 0; i < count && !cancelled; i++)
         visitSubgraph(parent.getChildAt(i));
      if (parent.isVertex()) {
         if (visitEdges) {
            int edgeCount = parent.getEdgeCount();
            for (int i = 0; i < edgeCount && !cancelled; i++) {
               mxICell edge = parent.getEdgeAt(i);
               mxICell source = edge.getTerminal(true);
               // visit only outgoing edges
               if (parent.equals(source))
                  visitEdgeCell(edge, false);
            }
         }
         endVertex(parent);
      }
      else if (visitEdges && isUnconnectedEdge(parent))
         visitEdgeCell(parent, false);
   }

   public void visitGraph (mxIGraphModel graphModel) {
      Object root = graphModel.getRoot();
      // System.err.println("visitGraph: root = " + root);
      if (root instanceof mxICell) {
         mxICell rootCell = (mxICell) root;
         beginGraph(rootCell);
         visitSubgraph(rootCell);
         endGraph(rootCell);
      }
   }

   public static void simpleVisitGraph (mxIGraphModel graphModel, boolean visitVertices, boolean visitEdges,
                                 BiConsumer<mxICell, LanguageEntity> visit) {
      new AbstractCellsCollector(visitVertices, visitEdges) {
         @Override
         protected boolean matches (mxICell cell, LanguageEntity entity) {
            visit.accept(cell, entity);
            return false;
         }
      }.countMatchingCells(graphModel);
   }

   public static void simpleVisitGraph (mxIGraphModel graphModel, BiConsumer<mxICell, LanguageEntity> visit) {
      simpleVisitGraph(graphModel, true, true, visit);
   }

}
