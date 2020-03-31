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
import de.unibi.hbp.ncc.lang.props.StrictlyPositiveDoubleProp;
import de.unibi.hbp.ncc.lang.props.StrictlyPositiveIntegerProp;

import java.util.List;

public class RetinaGrid extends SingleNeuronTypeModule {
   private final IntegerProp numInputRows, numOutputColumns, numberOfNeurons;
   private final DoubleProp maxExcitationWeight, minExcitationWeight, weightVariance;

   @Override
   protected List<EditableProp<?>> addEditableProps (List<EditableProp<?>> list) {
      super.addEditableProps(list);
      list.add(numInputRows);
      list.add(numOutputColumns);
      list.add(numberOfNeurons);
      list.add(neuronType);
      list.add(maxExcitationWeight);
      list.add(minExcitationWeight);
      list.add(weightVariance);
      list.add(synapseDelay);
      return list;
   }

   @Override
   protected String getGeneratedNamesPrefix () { return "Retina Grid"; }

   private static final String DEFAULT_NEURON_TYPE_NAME = "Retina Default";

   public RetinaGrid (Namespace<NetworkModule> namespace, String name, NeuronType neuronType,
                          int numInputRows, int numOutputColumns, int numberOfNeurons,
                          double maxExcitationWeight, double minExcitationWeight, double weightVariance,
                          double synapseDelay) {
      super(namespace, name, CREATOR.getResourceFileBaseName(), neuronType, DEFAULT_NEURON_TYPE_NAME, synapseDelay);
      this.numInputRows = new StrictlyPositiveIntegerProp("Number of Input Rows", this, numInputRows)
            .addImpact(EditableProp.Impact.CELL_STRUCTURE);
      this.numOutputColumns = new StrictlyPositiveIntegerProp("Number of Output Columns", this, numOutputColumns)
            .addImpact(EditableProp.Impact.CELL_STRUCTURE);
      this.numberOfNeurons = new StrictlyPositiveIntegerProp("Neurons per Population", this, numberOfNeurons);
      this.maxExcitationWeight = new NonNegativeDoubleProp("Excitatory Weight at Center", this, maxExcitationWeight);
      this.minExcitationWeight = new NonNegativeDoubleProp("Excitatory Weight at Border", this, minExcitationWeight);
      this.weightVariance = new StrictlyPositiveDoubleProp("Variance of Weight Distribution", this, weightVariance);
   }

   @Override
   protected NeuronType createDefaultNeuronType (Namespace<NeuronType> neuronTypes, String typeName) {
      return new NeuronType(neuronTypes, typeName, NeuronType.NeuronKind.IF_COND_EXP,
                            -70.0, -80.0, -60.0, 0.0, -100.0,
                            3.0, 3.0, 1.0, 10.0, 0.2, 0.0);
   }

   public RetinaGrid (Namespace<NetworkModule> namespace, String name) {
      this(namespace, name, null,
           3, 3, 5,
           0.09, 0.015, 0.0625,
           1.0);
   }

   public RetinaGrid (String name) { this(getGlobalNamespace(), name); }
   public RetinaGrid () { this((String) null); }

   protected RetinaGrid (RetinaGrid orig) {
      this(orig.moreSpecificNamespace, orig.getCopiedName(), orig.getNeuronType(),
           orig.numInputRows.getValue(), orig.numOutputColumns.getValue(), orig.numberOfNeurons.getValue(),
           orig.maxExcitationWeight.getValue(), orig.minExcitationWeight.getValue(), orig.weightVariance.getValue(),
           orig.synapseDelay.getValue());
   }

   private transient List<String> outputPortNames, inputPortNames;  // caches the lists, if retrieved multiple times

   @Override
   protected List<String> getPortNames (Port.Direction direction) {
      if (direction == Port.Direction.IN)
         return inputPortNames = buildPortNames(inputPortNames, "row", numInputRows.getValue());
      else if (direction == Port.Direction.OUT)
         return outputPortNames = buildPortNames(outputPortNames, "column", numOutputColumns.getValue());
      else
         throw new IllegalArgumentException("Unexpected direction: " + direction);
   }

   @Override
   protected boolean getPortIsOptional (Port.Direction direction, int portIndex) {
      return true;  // unused rows or columns are allowed in general
   }

   @Override
   protected int getPortDimension (Port.Direction direction, int portIndex) {
      return numberOfNeurons.getValue();  // all ports have the same dimensions
   }

   @Override
   public LanguageEntity duplicate () {
      return new RetinaGrid(this);
   }

   public static final EntityCreator<RetinaGrid> CREATOR = new Creator();

   private static class Creator extends ModuleInstanceCreator<RetinaGrid> {
      @Override
      public RetinaGrid create () { return new RetinaGrid(); }

      @Override
      public String getResourceFileBaseName () { return "retina"; }

      @Override
      public String getIconCaption () { return "Retina"; }
   }

   @CodeGenUse
   public int getNumberOfInputRows () { return numInputRows.getValue(); }
   @CodeGenUse
   public int getNumberOfOutputColumns () { return numOutputColumns.getValue(); }
   @CodeGenUse
   public int getNumberOfNeuronsPerPopulation () { return numberOfNeurons.getValue(); }
   @CodeGenUse
   public double getExcitationWeightAtCenter () { return maxExcitationWeight.getValue(); }
   @CodeGenUse
   public double getExcitationWeightAtBorder () { return minExcitationWeight.getValue(); }
   @CodeGenUse
   public double getWeightVariance () { return weightVariance.getValue(); }
   @CodeGenUse
   public double getSynapseDelay () { return synapseDelay.getValue(); }

}
