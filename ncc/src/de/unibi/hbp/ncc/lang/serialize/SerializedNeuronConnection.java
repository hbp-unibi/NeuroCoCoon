package de.unibi.hbp.ncc.lang.serialize;

import de.unibi.hbp.ncc.lang.Namespace;
import de.unibi.hbp.ncc.lang.NeuronConnection;
import de.unibi.hbp.ncc.lang.SynapseType;

import java.io.ObjectStreamException;
import java.io.Serializable;

public class SerializedNeuronConnection implements Serializable {
   private String namespaceId;
   private String name;

   public SerializedNeuronConnection (String namespaceId, String name) {
      this.namespaceId = namespaceId;
      this.name = name;
   }

   Object readResolve() throws ObjectStreamException {
      @SuppressWarnings("unchecked")
      Namespace<SynapseType> namespace = (Namespace<SynapseType>) Namespace.forId(namespaceId);
      return new NeuronConnection(namespace, namespace.get(name));
   }
}
