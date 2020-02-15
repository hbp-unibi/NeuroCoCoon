package de.unibi.hbp.ncc.lang;

import de.unibi.hbp.ncc.lang.props.EditableNameProp;
import de.unibi.hbp.ncc.lang.props.EditableProp;
import de.unibi.hbp.ncc.lang.serialize.SerializedNeuronConnection;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class NeuronConnection extends LanguageEntity implements Serializable {
   private Namespace<SynapseType> synapseTypeNamespace;
   private EditableNameProp<SynapseType> synapseType;

   private static Namespace<SynapseType> globalSynapseTypeNamespace;

   public static void setGlobalSynapseTypeNamespace (Namespace<SynapseType> ns) { globalSynapseTypeNamespace = ns; }

   protected Object writeReplace() throws ObjectStreamException {
      return new SerializedNeuronConnection(synapseTypeNamespace.getId(), synapseType.getValue().getName());
   }

   @Override
   protected List<EditableProp<?>> addEditableProps (List<EditableProp<?>> list) {
      super.addEditableProps(list);
      list.add(synapseType);
      return list;
   }

   @Override
   protected List<EditableProp<?>> addIndirectEditableProps (List<EditableProp<?>> list) {
      return synapseType.getValue().addExportedEditableProps(list);
   }

   public NeuronConnection (Namespace<SynapseType> synapseTypeNamespace, SynapseType synapseType) {
      super();
      if (synapseTypeNamespace == null)
         synapseTypeNamespace = globalSynapseTypeNamespace;
      this.synapseTypeNamespace = synapseTypeNamespace;
      if (synapseType == null)
         synapseType = globalSynapseTypeNamespace.get("All Default");
      this.synapseType = new EditableNameProp<>("Synapse Type", SynapseType.class, this,
                                                Objects.requireNonNull(synapseType), synapseTypeNamespace);
   }

   public NeuronConnection (SynapseType synapseType) {
      this(null, synapseType);
   }

   public NeuronConnection () {
      this(null, null);
   }

   protected NeuronConnection (NeuronConnection orig) {
      this(orig.synapseTypeNamespace, orig.synapseType.getValue());
   }

   public static final EntityCreator<NeuronConnection> CREATOR = new Creator();

   private static class Creator implements EntityCreator<NeuronConnection> {
      @Override
      public NeuronConnection create () {
         return new NeuronConnection();
      }

      @Override
      public String toString () {  // used by drag&drop tooltips
         return "Neuron Connection";
      }
   }

   @Override
   public String toString () {
      return synapseType.getValue().getSummary();
   }

   // @Override
   public NeuronConnection duplicate () {
      return new NeuronConnection(this);
   }

   public SynapseType getSynapseType () { return synapseType.getValue(); }

   @Override
   public String getCellStyle () {
      return synapseType.getValue().getEdgeStyle();
   }
}
