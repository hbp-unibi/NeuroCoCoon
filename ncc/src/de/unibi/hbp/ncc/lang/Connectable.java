package de.unibi.hbp.ncc.lang;

public interface Connectable {
   // TODO use this in graph edge creation checks
   // TODO disallow dangling edges in graph
   boolean isValidConnectionSource ();

   boolean isValidConnectionTarget ();
}
