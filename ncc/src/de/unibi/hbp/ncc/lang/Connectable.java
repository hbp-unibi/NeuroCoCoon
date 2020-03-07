package de.unibi.hbp.ncc.lang;

import de.unibi.hbp.ncc.lang.utils.Iterators;

public interface Connectable {
   default boolean isValidSynapseSource () { return false; }
   default boolean isValidSynapseTarget () { return false; };

   default Iterable<NeuronConnection> getOutgoingSynapses () { return Iterators.emptyIterable(); }
   default Iterable<NeuronConnection> getIncomingSynapses () { return Iterators.emptyIterable(); }

   default boolean hasAnyOutgoingSynapses () { return getOutgoingSynapses().iterator().hasNext(); }
   default boolean hasAnyIncomingSynapses () { return getIncomingSynapses().iterator().hasNext(); }

   default boolean isValidProbeSource () { return false; }
   default boolean isValidProbeTarget () { return false; };

   default Iterable<ProbeConnection> getOutgoingProbes () { return Iterators.emptyIterable(); }
   default Iterable<ProbeConnection> getIncomingProbes () { return Iterators.emptyIterable(); }

   default boolean hasAnyOutgoingProbes () { return getOutgoingProbes().iterator().hasNext(); }
   default boolean hasAnyIncomingProbes () { return getIncomingProbes().iterator().hasNext(); }
}
