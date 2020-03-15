package de.unibi.hbp.ncc.lang;

import de.unibi.hbp.ncc.lang.utils.Iterators;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Connectable {

   enum EdgeKind implements DisplayNamed {
      SYNAPSE("Synapse"), PROBE("Probe"), DEPENDENCY("Dependency");
      // SLICING("Slice"), COMPOSITION("Composition");  // use one edge kind for both abd distinguish in display text only

      private String displayName;

      EdgeKind (String displayName) { this.displayName = displayName; }

      @Override
      public String getDisplayName () { return displayName; }
   }
   // TODO provide dependency edge methods (dependency for population sub-view (and possibly assemblies [smart name comparator order?!])

   default boolean isValidSource (EdgeKind edgeKind) { return false; }
   default boolean isValidTarget (EdgeKind edgeKind) { return false; };

   default boolean isAnyValidSource () {
      for (EdgeKind edgeKind: EdgeKind.values())
         if (isValidSource(edgeKind))
            return true;
      return false;
   }

   default boolean isAnyValidTarget () {
      for (EdgeKind edgeKind: EdgeKind.values())
         if (isValidTarget(edgeKind))
            return true;
      return false;
   };

   // the next two methods must be implemented by each Connectable class
   @Nullable
   Iterable<AnyConnection> getOutgoingEdgesImpl (EdgeKind edgeKind);  // these may return null to indicate "none"

   @Nullable
   Iterable<AnyConnection> getIncomingEdgesImpl (EdgeKind edgeKind);

   // these are the nice, for-loop suited variants
   @NotNull
   default Iterable<AnyConnection> getOutgoingEdges (EdgeKind edgeKind) {
      Iterable<AnyConnection> iterable = getOutgoingEdgesImpl(edgeKind);
      return iterable != null ? iterable : Iterators.emptyIterable();
   }

   @NotNull
   default Iterable<AnyConnection> getIncomingEdges (EdgeKind edgeKind) {
      Iterable<AnyConnection> iterable = getIncomingEdgesImpl(edgeKind);
      return iterable != null ? iterable : Iterators.emptyIterable();
   }

   default boolean hasAnyOutgoingEdges (EdgeKind edgeKind) {
      Iterable<AnyConnection> iterable = getOutgoingEdgesImpl(edgeKind);
      return iterable != null && iterable.iterator().hasNext();
   }

   default boolean hasAnyIncomingEdges (EdgeKind edgeKind) {
      Iterable<AnyConnection> iterable = getIncomingEdgesImpl(edgeKind);
      return iterable != null && iterable.iterator().hasNext();
   }

   default boolean hasAnyOutgoingSynapses () { return hasAnyOutgoingEdges(EdgeKind.SYNAPSE); }
   default boolean hasAnyIncomingSynapses () { return hasAnyIncomingEdges(EdgeKind.SYNAPSE); }
   default boolean hasAnyOutgoingProbes () { return hasAnyOutgoingEdges(EdgeKind.PROBE); }
   default boolean hasAnyIncomingProbes () { return hasAnyIncomingEdges(EdgeKind.PROBE); }

   @NotNull
   default Iterable<NeuronConnection> getOutgoingSynapses () {
      Iterable<AnyConnection> iterable = getOutgoingEdgesImpl(EdgeKind.SYNAPSE);
      if (iterable != null)
         return Iterators.map(iterable, con -> (NeuronConnection) con);
      else
         return Iterators.emptyIterable();
   }

   @NotNull
   default Iterable<NeuronConnection> getIncomingSynapses () {
      Iterable<AnyConnection> iterable = getIncomingEdgesImpl(EdgeKind.SYNAPSE);
      if (iterable != null)
         return Iterators.map(iterable, con -> (NeuronConnection) con);
      else
         return Iterators.emptyIterable();
   }

   @NotNull
   default Iterable<ProbeConnection> getOutgoingProbes () {
      Iterable<AnyConnection> iterable = getOutgoingEdgesImpl(EdgeKind.PROBE);
      if (iterable != null)
         return Iterators.map(iterable, con -> (ProbeConnection) con);
      else
         return Iterators.emptyIterable();
   }

   @NotNull
   default Iterable<ProbeConnection> getIncomingProbes () {
      Iterable<AnyConnection> iterable = getIncomingEdgesImpl(EdgeKind.PROBE);
      if (iterable != null)
         return Iterators.map(iterable, con -> (ProbeConnection) con);
      else
         return Iterators.emptyIterable();
   }

}
