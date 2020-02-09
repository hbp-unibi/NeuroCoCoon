package de.unibi.hbp.ncc.lang;

import java.io.ObjectStreamException;
import java.io.Serializable;

public class SerializedEntityName implements Serializable {
   private int namespaceId;
   private String name;

   SerializedEntityName (int namespaceId, String name) {
      this.namespaceId = namespaceId;
      this.name = name;
   }

   Object readResolve() throws ObjectStreamException {
      return Namespace.forId(namespaceId).get(name);
   }
}
