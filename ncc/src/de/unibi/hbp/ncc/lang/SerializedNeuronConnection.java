package de.unibi.hbp.ncc.lang;

import java.io.ObjectStreamException;
import java.io.Serializable;

public class SerializedNeuronConnection implements Serializable {
   private int namespaceId;
   private String name;

   SerializedNeuronConnection (int namespaceId, String name) {
      this.namespaceId = namespaceId;
      this.name = name;
   }

   Object readResolve() throws ObjectStreamException {
      Namespace<SynapseType> namespace = (Namespace<SynapseType>) Namespace.forId(namespaceId);
      return new NeuronConnection(namespace, namespace.get(name));
   }
}
