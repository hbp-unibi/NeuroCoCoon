package de.unibi.hbp.ncc.lang.modules;

import de.unibi.hbp.ncc.editor.EntityCreator;
import de.unibi.hbp.ncc.editor.ModuleInstanceCreator;
import de.unibi.hbp.ncc.lang.LanguageEntity;
import de.unibi.hbp.ncc.lang.Namespace;
import de.unibi.hbp.ncc.lang.NetworkModule;
import de.unibi.hbp.ncc.lang.NeuronType;
import de.unibi.hbp.ncc.lang.ProbeConnection;
import de.unibi.hbp.ncc.lang.props.DoubleProp;
import de.unibi.hbp.ncc.lang.props.EditableNameProp;
import de.unibi.hbp.ncc.lang.props.EditableProp;
import de.unibi.hbp.ncc.lang.props.IntegerProp;
import de.unibi.hbp.ncc.lang.props.NonNegativeDoubleProp;
import de.unibi.hbp.ncc.lang.props.StrictlyPositiveIntegerProp;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class SynfireChain extends NetworkModule {
   private final EditableNameProp<NeuronType> neuronType;
   private final IntegerProp numberOfPopulations, numberOfNeurons;
   private final DoubleProp inhibitionWeight, excitationWeight;
   private final DoubleProp synapseDelay;

   @Override
   protected List<EditableProp<?>> addEditableProps (List<EditableProp<?>> list) {
      super.addEditableProps(list);
      list.add(neuronType);
      list.add(numberOfPopulations);
      list.add(numberOfNeurons);
      list.add(inhibitionWeight);
      list.add(excitationWeight);
      list.add(synapseDelay);
      return list;
   }

   @Override
   protected String getGeneratedNamesPrefix () { return "Synfire Chain"; }

   private static final String DEFAULT_NEURON_TYPE_NAME = "Chain Default";

   public SynfireChain (Namespace<NetworkModule> namespace, String name, NeuronType neuronType,
                        int numberOfPopulations, int numberOfNeurons,
                        double inhibitionWeight, double excitationWeight,
                        double synapseDelay) {
      super(namespace, name, CREATOR.getResourceFileBaseName());
      Namespace<NeuronType> neuronTypes = namespace.getContainingScope().getNeuronTypes();
      if (neuronType == null)
         neuronType = ensureDefaultType(neuronTypes, DEFAULT_NEURON_TYPE_NAME);
      this.neuronType = new EditableNameProp<>("Neuron Type", NeuronType.class, this, neuronType, neuronTypes);
      this.numberOfPopulations = new StrictlyPositiveIntegerProp("Length of Chain", this, numberOfPopulations)
            .addImpact(EditableProp.Impact.CELL_STRUCTURE);
      this.numberOfNeurons = new StrictlyPositiveIntegerProp("Neurons per Population", this, numberOfNeurons);
      this.inhibitionWeight = new NonNegativeDoubleProp("Inhibitory Weight", this, inhibitionWeight);
      this.excitationWeight = new NonNegativeDoubleProp("Excitatory Weight", this, excitationWeight);
      this.synapseDelay = new NonNegativeDoubleProp("Synapse Delay", this, synapseDelay).setUnit("ms");
   }

   public SynfireChain (Namespace<NetworkModule> namespace, String name) {
      this(namespace, name, null,
           3, 5,
           0.03, 0.015,
           1.0);
   }

   public SynfireChain (String name) {
      this(getGlobalNamespace(), name);
   }
   public SynfireChain () { this((String) null); }

   protected SynfireChain (SynfireChain orig) {
      this(orig.moreSpecificNamespace, orig.getCopiedName(), orig.neuronType.getValue(),
           orig.numberOfPopulations.getValue(), orig.numberOfNeurons.getValue(),
           orig.inhibitionWeight.getValue(), orig.excitationWeight.getValue(),
           orig.synapseDelay.getValue());
   }

   private transient List<String> outputPortNames,
         inputPortNames = Collections.singletonList("trigger");  // caches the lists, if retrieved multiple times

   @Override
   protected List<String> getPortNames (Port.Direction direction) {
      if (direction == Port.Direction.IN)
         return inputPortNames;
      else if (direction == Port.Direction.OUT)
         return outputPortNames = getPortNames(outputPortNames, numberOfPopulations.getValue(), "link");
      else
         throw new IllegalArgumentException("Unexpected direction: " + direction);
   }

   @Override
   protected boolean getPortIsOptional (Port.Direction direction, int portIndex) {
      return direction == Port.Direction.IN;  // make all outputs optional
   }

   @Override
   protected int getPortDimension (Port.Direction direction, int portIndex) {
      if (direction == Port.Direction.IN)
         if (0 == portIndex)
            return numberOfNeurons.getValue();
         else
            throw new IndexOutOfBoundsException("port index: " + portIndex);
      else if (direction == Port.Direction.OUT)
         if (0 <= portIndex && portIndex < numberOfPopulations.getValue())
            return numberOfNeurons.getValue();
         else
            throw new IndexOutOfBoundsException("port index: " + portIndex);
      else
         throw new IllegalArgumentException("Unexpected direction: " + direction);
   }

   private static final EnumSet<ProbeConnection.DataSeries> SUPPORTED_DATA_SERIES = EnumSet.of(
         ProbeConnection.DataSeries.SPIKES, ProbeConnection.DataSeries.VOLTAGE);

   @Override
   public Collection<ProbeConnection.DataSeries> validDataSeries () { return SUPPORTED_DATA_SERIES; }

   @Override
   public LanguageEntity duplicate () {
      return new SynfireChain(this);
   }

   public static final EntityCreator<SynfireChain> CREATOR = new Creator();

   private static class Creator extends ModuleInstanceCreator<SynfireChain> {
      @Override
      public SynfireChain create () {
         return new SynfireChain();
      }

      @Override
      public String getResourceFileBaseName () { return "chain"; }

      @Override
      public String getIconCaption () { return "Chain"; }
   }

   public int getNumberOfPopulations () { return numberOfPopulations.getValue(); }
   public int getNumberOfNeuronsPerPopulation () { return numberOfNeurons.getValue(); }
   public NeuronType getNeuronType () { return neuronType.getValue(); }
   public double getInhibitionWeight () { return inhibitionWeight.getValue(); }
   public double getExcitationWeight () { return excitationWeight.getValue(); }
   public double getSynapseDelay () { return synapseDelay.getValue(); }
}
