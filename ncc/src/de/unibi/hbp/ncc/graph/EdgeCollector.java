package de.unibi.hbp.ncc.graph;

import com.mxgraph.model.mxICell;
import de.unibi.hbp.ncc.lang.AnyConnection;
import de.unibi.hbp.ncc.lang.Connectable;
import de.unibi.hbp.ncc.lang.ProbeConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public final class EdgeCollector {

   private EdgeCollector () {}

   @Nullable
   private static List<AnyConnection> getConnectionsAt (Connectable.EdgeKind edgeKind,
                                                        mxICell vertex, boolean vertexIsSource) {
      if (vertex == null)
         return null;
      int edgeCount = vertex.getEdgeCount();
      List<AnyConnection> result = new ArrayList<>(edgeCount);  // over estimates (all vs. incoming/outgoing edges only)
      for (int edgeIndex = 0; edgeIndex < edgeCount; edgeIndex++) {
         mxICell edge = vertex.getEdgeAt(edgeIndex);
         if (vertex.equals(edge.getTerminal(vertexIsSource))) {
            Object edgeValue = edge.getValue();
            if (edgeValue instanceof AnyConnection) {
               AnyConnection con = (AnyConnection) edgeValue;
               if (edgeKind == null || edgeKind == con.getEdgeKind())  // allows edge kind null to mean all edges
                  result.add(con);
            }
         }
      }
      return result.isEmpty() ? null : result;
   }

   @Nullable
   public static List<AnyConnection> getIncomingConnections (Connectable.EdgeKind edgeKind, mxICell vertex) {
      return getConnectionsAt(edgeKind, vertex, false);
   }

   @Nullable
   public static List<AnyConnection> getOutgoingConnections (Connectable.EdgeKind edgeKind, mxICell vertex) {
      return getConnectionsAt(edgeKind, vertex, true);
   }

   @NotNull
   public static Collection<ProbeConnection.DataSeries> getRequiredDataSeries (Connectable connectable) {
      EnumSet<ProbeConnection.DataSeries> union = EnumSet.noneOf(ProbeConnection.DataSeries.class);
      for (ProbeConnection probe: connectable.getIncomingProbes())
         union.add(probe.getDataSeries());
      return union;
   }

   @NotNull
   public static Collection<ProbeConnection.DataSeries> getContributingDataSeries (Connectable connectable) {
      EnumSet<ProbeConnection.DataSeries> union = EnumSet.noneOf(ProbeConnection.DataSeries.class);
      for (ProbeConnection probe: connectable.getOutgoingProbes())
         union.add(probe.getDataSeries());
      return union;
   }
}
