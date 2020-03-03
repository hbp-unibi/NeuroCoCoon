package de.unibi.hbp.ncc.lang;

public interface Connectable {
   // TODO use this in graph edge creation checks
   // TODO disallow dangling edges in graph
   boolean isValidConnectionSource ();

   boolean isValidConnectionTarget ();

   Iterable<NeuronConnection> getOutgoingConnections ();
   Iterable<NeuronConnection> getIncomingConnections ();
   default boolean hasOutgoingConnections () { return getOutgoingConnections().iterator().hasNext(); }
   default boolean hasIncomingConnections () { return getIncomingConnections().iterator().hasNext(); }
}
