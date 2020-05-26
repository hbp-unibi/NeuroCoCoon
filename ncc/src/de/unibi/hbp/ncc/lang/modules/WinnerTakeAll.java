package de.unibi.hbp.ncc.lang.modules;

import de.unibi.hbp.ncc.editor.EntityCreator;
import de.unibi.hbp.ncc.editor.ModuleInstanceCreator;
import de.unibi.hbp.ncc.lang.LanguageEntity;
import de.unibi.hbp.ncc.lang.Namespace;
import de.unibi.hbp.ncc.lang.NetworkModule;
import de.unibi.hbp.ncc.lang.NeuronType;
import de.unibi.hbp.ncc.lang.codegen.CodeGenUse;
import de.unibi.hbp.ncc.lang.props.DoubleProp;
import de.unibi.hbp.ncc.lang.props.EditableProp;
import de.unibi.hbp.ncc.lang.props.IntegerProp;
import de.unibi.hbp.ncc.lang.props.NonNegativeDoubleProp;
import de.unibi.hbp.ncc.lang.props.ProbabilityProp;
import de.unibi.hbp.ncc.lang.props.StrictlyPositiveIntegerProp;

import java.util.List;

public class WinnerTakeAll extends SingleNeuronTypeModule {
   private final IntegerProp numberOfPopulations, numberOfNeurons;
   private final DoubleProp noiseWeight, inhibitionWeight, excitationWeight;
   private final DoubleProp noiseRate, noiseProbability, inhibitionProbability, excitationProbability;

   @Override
   protected List<EditableProp<?>> addEditableProps (List<EditableProp<?>> list) {
      super.addEditableProps(list);
      list.add(numberOfPopulations);
      list.add(numberOfNeurons);
      list.add(neuronType);
      list.add(noiseWeight);
      list.add(inhibitionWeight);
      list.add(excitationWeight);
      list.add(noiseRate);
      list.add(noiseProbability);
      list.add(inhibitionProbability);
      list.add(excitationProbability);
      list.add(synapseDelay);
      return list;
   }

   @Override
   protected String getGeneratedNamesPrefix () { return "Winner Take All"; }

   private static final String DEFAULT_NEURON_TYPE_NAME = "WTA Default";

   public WinnerTakeAll (Namespace<NetworkModule> namespace, String name, NeuronType neuronType,
                         int numberOfPopulations, int numberOfNeurons,
                         double noiseWeight, double inhibitionWeight, double excitationWeight,
                         double noiseRate, double noiseProbability,
                         double inhibitionProbability, double excitationProbability,
                         double synapseDelay) {
      super(namespace, name, CREATOR.getResourceFileBaseName(), neuronType, DEFAULT_NEURON_TYPE_NAME, synapseDelay);
      this.numberOfPopulations = new StrictlyPositiveIntegerProp("Number of Outcomes", this, numberOfPopulations)
            .addImpact(EditableProp.Impact.CELL_STRUCTURE);
      this.numberOfNeurons = new StrictlyPositiveIntegerProp("Neurons per Population", this, numberOfNeurons);
      this.noiseWeight = new NonNegativeDoubleProp("Noise Weight", this, noiseWeight);
      this.inhibitionWeight = new NonNegativeDoubleProp("Inhibitory Weight", this, inhibitionWeight);
      this.excitationWeight = new NonNegativeDoubleProp("Excitatory Weight", this, excitationWeight);
      this.noiseRate = new NonNegativeDoubleProp("Rate", this, noiseRate).setUnit("Hz");  // zero means no noise
      this.noiseProbability = new ProbabilityProp("Noise Probability", this, noiseProbability);
      this.inhibitionProbability = new ProbabilityProp("Inhibition Probability", this, inhibitionProbability);
      this.excitationProbability = new ProbabilityProp("Excitation Probability", this, excitationProbability);
   }

   @Override
   protected NeuronType createDefaultNeuronType (Namespace<NeuronType> neuronTypes, String typeName) {
      return new NeuronType(neuronTypes, typeName, NeuronType.NeuronKind.IF_COND_EXP,
                            -70.0, -80.0, -60.0, 0.0, -100.0,
                            5.0, 3.0, 1.0, 10.0, 0.2, 0.0);
   }

   public WinnerTakeAll (Namespace<NetworkModule> namespace, String name) {
      this(namespace, name, null,
           5, 5,
           0.01, 0.006, 0.006,
           20.0, 0.7, 0.5, 0.7,
           1.0);
   }

   public WinnerTakeAll (String name) {
      this(getGlobalNamespace(), name);
   }
   public WinnerTakeAll () { this((String) null); }

   protected WinnerTakeAll (WinnerTakeAll orig) {
      this(orig.moreSpecificNamespace, orig.getCopiedName(), orig.getNeuronType(),
           orig.numberOfPopulations.getValue(), orig.numberOfNeurons.getValue(),
           orig.noiseWeight.getValue(), orig.inhibitionWeight.getValue(), orig.excitationWeight.getValue(),
           orig.noiseRate.getValue(), orig.noiseProbability.getValue(),
           orig.inhibitionProbability.getValue(), orig.excitationProbability.getValue(),
           orig.synapseDelay.getValue());
   }

   private transient List<String> outputPortNames, inputPortNames;  // caches the lists, if retrieved multiple times

   @Override
   protected List<String> getPortNames (Port.Direction direction) {
      if (direction == Port.Direction.IN)
         return inputPortNames = buildPortNames(inputPortNames, "stimulus", numberOfPopulations.getValue());
      else if (direction == Port.Direction.OUT)
         return outputPortNames = buildPortNames(outputPortNames, "out", numberOfPopulations.getValue());
      else
         throw new IllegalArgumentException("Unexpected direction: " + direction);
   }

   @Override
   protected boolean getPortIsOptional (Port.Direction direction, int portIndex) {
      return true;  // make everything optional
   }

   @Override
   protected int getPortDimension (Port.Direction direction, int portIndex) {
      if (direction == Port.Direction.IN || direction == Port.Direction.OUT)
         if (0 <= portIndex && portIndex < numberOfPopulations.getValue())
            return numberOfNeurons.getValue();
         else
            throw new IndexOutOfBoundsException("port index: " + portIndex);
      else
         throw new IllegalArgumentException("Unexpected direction: " + direction);
   }

   @Override
   public LanguageEntity duplicate () {
      return new WinnerTakeAll(this);
   }

   public static final EntityCreator<WinnerTakeAll> CREATOR = new Creator();

   private static class Creator extends ModuleInstanceCreator<WinnerTakeAll> {
      @Override
      public WinnerTakeAll create () {
         return new WinnerTakeAll();
      }

      @Override
      public String getResourceFileBaseName () { return "winner"; }

      @Override
      public String getIconCaption () { return "Winner"; }
   }

   @CodeGenUse
   public int getNumberOfPopulations () { return numberOfPopulations.getValue(); }
   @CodeGenUse
   public int getNumberOfNeuronsPerPopulation () { return numberOfNeurons.getValue(); }

   @CodeGenUse
   public double getNoiseWeight () { return noiseWeight.getValue(); }
   @CodeGenUse
   public double getInhibitionWeight () { return inhibitionWeight.getValue(); }
   @CodeGenUse
   public double getExcitationWeight () { return excitationWeight.getValue(); }
   @CodeGenUse
   public double getNoiseRate () { return noiseRate.getValue(); }
   @CodeGenUse
   public double getNoiseProbability () { return noiseProbability.getValue(); }
   @CodeGenUse
   public double getInhibitionProbability () { return inhibitionProbability.getValue(); }
   @CodeGenUse
   public double getExcitationProbability () { return excitationProbability.getValue(); }
   @CodeGenUse
   public double getSynapseDelay () { return synapseDelay.getValue(); }
}
