package de.unibi.hbp.ncc.lang;

import java.io.ObjectStreamException;
import java.io.Serializable;

public class EntityName implements Serializable {
   private int namespaceId;
   private String name;

   public EntityName (int namespaceId, String name) {
      this.namespaceId = namespaceId;
      this.name = name;
   }

   public String getName () {
      return name;
   }

   Object readResolve() throws ObjectStreamException {
      return Namespace.forId(namespaceId).get(name);
   }
}
