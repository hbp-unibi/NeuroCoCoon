package de.unibi.hbp.ncc.lang.serialize;

import de.unibi.hbp.ncc.lang.Namespace;

import java.io.ObjectStreamException;
import java.io.Serializable;

public class SerializedEntityName implements Serializable {
   private String namespaceId;
   private String name;

   public SerializedEntityName (String namespaceId, String name) {
      this.namespaceId = namespaceId;
      this.name = name;
   }

   Object readResolve() throws ObjectStreamException {
      return Namespace.forId(namespaceId).get(name);
   }
}
