package de.unibi.hbp.ncc.lang.serialize;

import de.unibi.hbp.ncc.lang.Namespace;
import de.unibi.hbp.ncc.lang.NeuronConnection;
import de.unibi.hbp.ncc.lang.SynapseType;

import java.io.ObjectStreamException;
import java.io.Serializable;

public class SerializedNeuronConnection implements Serializable {
   private Namespace.Id namespaceId;
   private String name;

   public SerializedNeuronConnection (SynapseType synapseType) {
      this.namespaceId = synapseType.getNamespaceId();
      this.name = synapseType.getName();
   }

   Object readResolve() throws ObjectStreamException {
      @SuppressWarnings("unchecked")
      Namespace<SynapseType> namespace = (Namespace<SynapseType>) Namespace.forId(namespaceId);
      return new NeuronConnection(namespace, namespace.get(name));
   }
}
