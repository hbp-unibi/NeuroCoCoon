package de.unibi.hbp.ncc.lang.modules;

import de.unibi.hbp.ncc.lang.Namespace;
import de.unibi.hbp.ncc.lang.NetworkModule;
import de.unibi.hbp.ncc.lang.NeuronType;
import de.unibi.hbp.ncc.lang.ProbeConnection;
import de.unibi.hbp.ncc.lang.Program;
import de.unibi.hbp.ncc.lang.codegen.CodeGenUse;
import de.unibi.hbp.ncc.lang.codegen.ErrorCollector;
import de.unibi.hbp.ncc.lang.props.DoubleProp;
import de.unibi.hbp.ncc.lang.props.EditableNameProp;
import de.unibi.hbp.ncc.lang.props.NonNegativeDoubleProp;

import java.util.Collection;
import java.util.Collections;

public abstract class SingleNeuronTypeModule extends NetworkModule {
   protected final EditableNameProp<NeuronType> neuronType;
   protected final DoubleProp synapseDelay;


   // this class does not add neuronType and synapseDelay to the editable properties automatically
   // so that subclasses have full control over the position in the inspector

   protected SingleNeuronTypeModule (Namespace<NetworkModule> namespace, String name, String resourceFileBaseName,
                                     NeuronType neuronType, String defaultNeuronTypeName, double synapseDelay) {
      super(namespace, name, resourceFileBaseName);
      Namespace<NeuronType> neuronTypes = namespace.getContainingScope().getNeuronTypes();
      if (neuronType == null) {
         neuronType = neuronTypes.get(defaultNeuronTypeName);
         if (neuronType == null)
            neuronType = createDefaultNeuronType(neuronTypes, defaultNeuronTypeName);
      }
      this.neuronType = new EditableNameProp<>("Neuron Type", NeuronType.class, this, neuronType, neuronTypes);
      this.synapseDelay = new NonNegativeDoubleProp("Synapse Delay", this, synapseDelay).setUnit("ms");
   }

   @Override
   protected Collection<ProbeConnection.DataSeries> getPortDataSeries (Port.Direction direction, int portIndex) {
      return direction == Port.Direction.OUT
            ? getNeuronType().getSupportedDataSeries()
            : Collections.emptySet();
   }

   @Override
   protected boolean getPortIsConductanceBased (Port.Direction direction, int portIndex) {
      return getNeuronType().isConductanceBased();
   }

   @CodeGenUse
   public NeuronType getNeuronType () { return neuronType.getValue(); }

   protected abstract NeuronType createDefaultNeuronType (Namespace<NeuronType> neuronTypes, String typeName);

   @CodeGenUse
   public double getSynapseDelay () { return synapseDelay.getValue(); }

   @Override
   public void checkStaticSemantics (Program program, ErrorCollector diagnostics) {
      super.checkStaticSemantics(program, diagnostics);
      if (synapseDelay.getValue() < program.getTimeStep())
         diagnostics.recordWarning(this, "Internal synapse delay is smaller than time step.");
   }
}
