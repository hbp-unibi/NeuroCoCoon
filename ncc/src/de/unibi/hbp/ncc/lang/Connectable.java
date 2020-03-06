package de.unibi.hbp.ncc.lang;

import de.unibi.hbp.ncc.lang.utils.Iterators;

public interface Connectable {
   default boolean isValidSynapseSource () { return false; }
   default boolean isValidSynapseTarget () { return false; };

   default Iterable<NeuronConnection> getOutgoingSynapses () { return Iterators.emptyIterable(); }
   default Iterable<NeuronConnection> getIncomingSynapses () { return Iterators.emptyIterable(); }

   default boolean hasOutgoingSynapses () { return getOutgoingSynapses().iterator().hasNext(); }
   default boolean hasIncomingSynapses () { return getIncomingSynapses().iterator().hasNext(); }

   default boolean isValidProbeSource () { return false; }
   default boolean isValidProbeTarget () { return false; };

   default Iterable<ProbeConnection> getOutgoingProbes () { return Iterators.emptyIterable(); }
   default Iterable<ProbeConnection> getIncomingProbes () { return Iterators.emptyIterable(); }

   default boolean hasOutgoingProbes () { return getOutgoingProbes().iterator().hasNext(); }
   default boolean hasIncomingProbes () { return getIncomingProbes().iterator().hasNext(); }
}
