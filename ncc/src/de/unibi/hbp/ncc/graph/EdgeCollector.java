package de.unibi.hbp.ncc.graph;

import com.mxgraph.model.mxICell;
import de.unibi.hbp.ncc.lang.NeuronConnection;
import de.unibi.hbp.ncc.lang.ProbeConnection;
import de.unibi.hbp.ncc.lang.utils.Iterators;

import java.util.ArrayList;
import java.util.List;

public final class EdgeCollector {

   private EdgeCollector () {}

   private static Iterable<NeuronConnection> getSynapsesAt (mxICell vertex, boolean vertexIsSource) {
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

   public static Iterable<NeuronConnection> getIncomingSynapses (mxICell vertex) {
      return getSynapsesAt(vertex, false);
   }

   public static Iterable<NeuronConnection> getOutgoingSynapses (mxICell vertex) {
      return getSynapsesAt(vertex, true);
   }

   private static Iterable<ProbeConnection> getProbesAt (mxICell vertex, boolean vertexIsSource) {
      if (vertex == null)
         return Iterators.emptyIterable();
      int edgeCount = vertex.getEdgeCount();
      List<ProbeConnection> result = new ArrayList<>(edgeCount);  // over estimates (all vs. incoming/outgoing edges only)
      for (int edgeIndex = 0; edgeIndex < edgeCount; edgeIndex++) {
         mxICell edge = vertex.getEdgeAt(edgeIndex);
         if (vertex.equals(edge.getTerminal(vertexIsSource))) {
            Object edgeValue = edge.getValue();
            if (edgeValue instanceof ProbeConnection)
               result.add((ProbeConnection) edgeValue);
         }
      }
      return result;
   }

   public static Iterable<ProbeConnection> getIncomingProbes (mxICell vertex) {
      return getProbesAt(vertex, false);
   }

   public static Iterable<ProbeConnection> getOutgoingProbes (mxICell vertex) {
      return getProbesAt(vertex, true);
   }

   // TODO do we need support for AnyConnection edge collectors?
}
