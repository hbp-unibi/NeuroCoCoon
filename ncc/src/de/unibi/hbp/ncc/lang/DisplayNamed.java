package de.unibi.hbp.ncc.lang;

public interface DisplayNamed {

   String getDisplayName ();
   default String getLongDisplayName () { return getDisplayName(); }
}
