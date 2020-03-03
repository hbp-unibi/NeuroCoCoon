package de.unibi.hbp.ncc.graph;

import com.mxgraph.model.mxICell;
import de.unibi.hbp.ncc.lang.NeuronConnection;
import de.unibi.hbp.ncc.lang.utils.Iterators;

import java.util.ArrayList;
import java.util.List;

public final class EdgeCollector {

   private EdgeCollector () {}

   private static Iterable<NeuronConnection> getConnections (mxICell vertex, boolean vertexIsSource) {
      if (vertex == null)
         return Iterators.emptyIterable();
      int edgeCount = vertex.getEdgeCount();
      List<NeuronConnection> result = new ArrayList<>(edgeCount);  // over estimates (all vs. incoming/outgoing edges only)
      for (int edgeIndex = 0; edgeIndex < edgeCount; edgeIndex++) {
         mxICell edge = vertex.getEdgeAt(edgeIndex);
         if (vertex.equals(edge.getTerminal(vertexIsSource))) {
            Object edgeValue = edge.getValue();
            if (edgeValue instanceof NeuronConnection)
               result.add((NeuronConnection) edgeValue);
         }
      }
      return result;
   }

   public static Iterable<NeuronConnection> getIncomingConnections (mxICell vertex) {
      return getConnections(vertex, false);
   }

   public static Iterable<NeuronConnection> getOutgoingConnections (mxICell vertex) {
      return getConnections(vertex, true);
   }

}
