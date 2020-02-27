package de.unibi.hbp.ncc.lang.serialize;

import de.unibi.hbp.ncc.lang.NamedEntity;
import de.unibi.hbp.ncc.lang.Namespace;

import java.io.ObjectStreamException;
import java.io.Serializable;

public class SerializedNamedEntity implements Serializable {
   private Namespace.Id namespaceId;
   private String name;

   public SerializedNamedEntity (NamedEntity entity) {
      this.namespaceId = entity.getNamespaceId();
      this.name = entity.getName();
   }

   Object readResolve() throws ObjectStreamException {
      return Namespace.forId(namespaceId).get(name);
   }
}
