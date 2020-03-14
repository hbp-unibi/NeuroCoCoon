package de.unibi.hbp.ncc.lang.serialize;

public abstract class SerializedAnyConnection {
   protected String userLabel;

   protected SerializedAnyConnection (String userLabel) {
      this.userLabel = userLabel;
   }
}
