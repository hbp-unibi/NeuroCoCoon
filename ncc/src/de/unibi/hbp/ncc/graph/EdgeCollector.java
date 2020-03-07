package de.unibi.hbp.ncc.graph;

import com.mxgraph.model.mxICell;
import de.unibi.hbp.ncc.lang.Connectable;
import de.unibi.hbp.ncc.lang.NeuronConnection;
import de.unibi.hbp.ncc.lang.ProbeConnection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public final class EdgeCollector {

   private EdgeCollector () {}

   private static List<NeuronConnection> getSynapsesAt (mxICell vertex, boolean vertexIsSource) {
      if (vertex == null)
         return Collections.emptyList();
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

   public static List<NeuronConnection> getIncomingSynapses (mxICell vertex) {
      return getSynapsesAt(vertex, false);
   }

   public static List<NeuronConnection> getOutgoingSynapses (mxICell vertex) {
      return getSynapsesAt(vertex, true);
   }

   private static List<ProbeConnection> getProbesAt (mxICell vertex, boolean vertexIsSource) {
      if (vertex == null)
         return Collections.emptyList();
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

   public static List<ProbeConnection> getIncomingProbes (mxICell vertex) {
      return getProbesAt(vertex, false);
   }

   public static List<ProbeConnection> getOutgoingProbes (mxICell vertex) {
      return getProbesAt(vertex, true);
   }

   public static Collection<ProbeConnection.DataSeries> getRequiredDataSeries (Connectable connectable) {
      EnumSet<ProbeConnection.DataSeries> union = EnumSet.noneOf(ProbeConnection.DataSeries.class);
      for (ProbeConnection probe: connectable.getIncomingProbes())
         union.add(probe.getDataSeries());
      return union;
   }

   // TODO do we need support for AnyConnection edge collectors?
}
