package de.unibi.hbp.ncc.lang.serialize;

import de.unibi.hbp.ncc.lang.AnyConnection;
import de.unibi.hbp.ncc.lang.Namespace;
import de.unibi.hbp.ncc.lang.NeuronConnection;
import de.unibi.hbp.ncc.lang.SynapseType;

import java.io.ObjectStreamException;
import java.io.Serializable;

public class SerializedNeuronConnection extends SerializedAnyConnection implements Serializable {
   private Namespace.Id synapseTypeNamespaceId;
   private String synapseTypeName;

   public SerializedNeuronConnection (SynapseType synapseType, String userLabel, AnyConnection.RoutingStyle routingStyle) {
      super(userLabel, routingStyle);
      this.synapseTypeNamespaceId = synapseType.getNamespaceId();
      this.synapseTypeName = synapseType.getName();
   }

   Object readResolve() throws ObjectStreamException {
      @SuppressWarnings("unchecked")
      Namespace<SynapseType> namespace = (Namespace<SynapseType>) Namespace.forId(synapseTypeNamespaceId);
      return new NeuronConnection(namespace, namespace.get(synapseTypeName), userLabel, routingStyle);
   }
}
