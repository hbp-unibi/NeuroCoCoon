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
import de.unibi.hbp.ncc.lang.props.NonNegativeIntegerProp;
import de.unibi.hbp.ncc.lang.props.ProbabilityProp;
import de.unibi.hbp.ncc.lang.props.StrictlyPositiveIntegerProp;

import java.util.Collections;
import java.util.List;

public class InceptionGroup extends SingleNeuronTypeModule {
   private final IntegerProp minStages, maxStages, numberOfNeurons;
   private final DoubleProp excitationWeight, connectionProbability;

   @Override
   protected List<EditableProp<?>> addEditableProps (List<EditableProp<?>> list) {
      super.addEditableProps(list);
      list.add(minStages);
      list.add(maxStages);
      list.add(numberOfNeurons);
      list.add(neuronType);
      list.add(connectionProbability);
      list.add(excitationWeight);
      list.add(synapseDelay);
      return list;
   }

   @Override
   protected String getGeneratedNamesPrefix () { return "Inception Group"; }

   private static final String DEFAULT_NEURON_TYPE_NAME = "Inception Default";

   public InceptionGroup (Namespace<NetworkModule> namespace, String name, NeuronType neuronType,
                          int minStages, int maxStages, int numberOfNeurons,
                          double connectionProbability, double excitationWeight,
                          double synapseDelay) {
      super(namespace, name, CREATOR.getResourceFileBaseName(), neuronType, DEFAULT_NEURON_TYPE_NAME, synapseDelay);
      this.minStages = new NonNegativeIntegerProp("Minimum Number of Stages", this, minStages)
            .addImpact(EditableProp.Impact.CELL_STRUCTURE);
      this.maxStages = new NonNegativeIntegerProp("Maximum Number of Stages", this, maxStages)
            .addImpact(EditableProp.Impact.CELL_STRUCTURE);
      this.numberOfNeurons = new StrictlyPositiveIntegerProp("Neurons per Population", this, numberOfNeurons);
      this.connectionProbability = new ProbabilityProp("Connection Probability", this, connectionProbability);
      this.excitationWeight = new NonNegativeDoubleProp("Excitatory Weight", this, excitationWeight);
   }

   @Override
   protected NeuronType createDefaultNeuronType (Namespace<NeuronType> neuronTypes, String typeName) {
      return new NeuronType(neuronTypes, typeName, NeuronType.NeuronKind.IF_COND_EXP,
                            -70.0, -80.0, -60.0, 0.0, -100.0,
                            3.0, 3.0, 1.0, 10.0, 0.2, 0.0);
   }

   public InceptionGroup (Namespace<NetworkModule> namespace, String name) {
      this(namespace, name, null,
           1, 3, 5,
           0.5, 0.015,
           1.0);
   }

   public InceptionGroup (String name) { this(getGlobalNamespace(), name); }
   public InceptionGroup () { this((String) null); }

   protected InceptionGroup (InceptionGroup orig) {
      this(orig.moreSpecificNamespace, orig.getCopiedName(), orig.getNeuronType(),
           orig.minStages.getValue(), orig.maxStages.getValue(), orig.numberOfNeurons.getValue(),
           orig.connectionProbability.getValue(), orig.excitationWeight.getValue(),
           orig.synapseDelay.getValue());
   }

   private transient List<String> outputPortNames,
         inputPortNames = Collections.singletonList("input");  // caches the lists, if retrieved multiple times

   private static final List<String> COMBINED_OUTPUT = Collections.singletonList("group output");

   @Override
   protected List<String> getPortNames (Port.Direction direction) {
      if (direction == Port.Direction.IN)
         return inputPortNames;
      else if (direction == Port.Direction.OUT)
         return outputPortNames = buildPortNames(outputPortNames, COMBINED_OUTPUT,
                                                 "branch", minStages.getValue(), maxStages.getValue(),
                                                 Collections.emptyList());
      else
         throw new IllegalArgumentException("Unexpected direction: " + direction);
   }

   @Override
   protected boolean getPortIsOptional (Port.Direction direction, int portIndex) {
      return direction == Port.Direction.OUT && portIndex > 0;  // make all individual branch outputs optional
   }

   private int computeNumberOfBranches () {
      return Math.max(maxStages.getValue() - minStages.getValue() + 1, 0);
   }

   @Override
   protected int getPortDimension (Port.Direction direction, int portIndex) {
      if (direction == Port.Direction.IN)
         if (0 == portIndex)
            return numberOfNeurons.getValue();
         else
            throw new IndexOutOfBoundsException("port index: " + portIndex);
      else if (direction == Port.Direction.OUT)
         if (portIndex == 0)
            return numberOfNeurons.getValue() * computeNumberOfBranches();
         else if (1 <= portIndex && portIndex <= computeNumberOfBranches())
            return numberOfNeurons.getValue();
         else
            throw new IndexOutOfBoundsException("port index: " + portIndex);
      else
         throw new IllegalArgumentException("Unexpected direction: " + direction);
   }

   @Override
   public LanguageEntity duplicate () {
      return new InceptionGroup(this);
   }

   public static final EntityCreator<InceptionGroup> CREATOR = new Creator();

   private static class Creator extends ModuleInstanceCreator<InceptionGroup> {
      @Override
      public InceptionGroup create () { return new InceptionGroup(); }

      @Override
      public String getResourceFileBaseName () { return "inception"; }

      @Override
      public String getIconCaption () { return "Inception"; }
   }

   @CodeGenUse
   public int getMinimumNumberOfStages () { return minStages.getValue(); }
   @CodeGenUse
   public int getMaximumNumberOfStages () { return maxStages.getValue(); }
   @CodeGenUse
   public int getNumberOfNeuronsPerPopulation () { return numberOfNeurons.getValue(); }
   @CodeGenUse
   public double getConnectionProbability () { return connectionProbability.getValue(); }
   @CodeGenUse
   public double getExcitationWeight () { return excitationWeight.getValue(); }
   @CodeGenUse
   public double getSynapseDelay () { return synapseDelay.getValue(); }

}
