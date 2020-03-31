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
import de.unibi.hbp.ncc.lang.props.StrictlyPositiveIntegerProp;

import java.util.Collections;
import java.util.List;

public class RobotHeadDirection extends SingleNeuronTypeModule {
   private final IntegerProp numberOfDirections, numberOfNeurons, maxShiftVelocity;
   private final DoubleProp inhibitionWeight, excitationWeight;

   @Override
   protected List<EditableProp<?>> addEditableProps (List<EditableProp<?>> list) {
      super.addEditableProps(list);
      list.add(numberOfDirections);
      list.add(numberOfNeurons);
      list.add(neuronType);
      list.add(inhibitionWeight);
      list.add(excitationWeight);
      list.add(synapseDelay);
      return list;
   }

   @Override
   protected String getGeneratedNamesPrefix () { return "Head Direction"; }

   private static final String DEFAULT_NEURON_TYPE_NAME = "Direction Default";

   public RobotHeadDirection (Namespace<NetworkModule> namespace, String name, NeuronType neuronType,
                        int numberOfDirections, int maxShiftVelocity, int numberOfNeurons,
                        double inhibitionWeight, double excitationWeight,
                        double synapseDelay) {
      super(namespace, name, CREATOR.getResourceFileBaseName(), neuronType, DEFAULT_NEURON_TYPE_NAME, synapseDelay);
      this.numberOfDirections = new StrictlyPositiveIntegerProp("Number of Directions", this, numberOfDirections)
            .addImpact(EditableProp.Impact.CELL_STRUCTURE);
      this.maxShiftVelocity = new StrictlyPositiveIntegerProp("Maximum Shift Distance", this, maxShiftVelocity)
            .addImpact(EditableProp.Impact.CELL_STRUCTURE);
      this.numberOfNeurons = new StrictlyPositiveIntegerProp("Neurons per Population", this, numberOfNeurons);
      this.inhibitionWeight = new NonNegativeDoubleProp("Inhibitory Weight", this, inhibitionWeight);
      this.excitationWeight = new NonNegativeDoubleProp("Excitatory Weight", this, excitationWeight);
   }

   @Override
   protected NeuronType createDefaultNeuronType (Namespace<NeuronType> neuronTypes, String typeName) {
      return new NeuronType(neuronTypes, typeName, NeuronType.NeuronKind.IF_COND_EXP,
                            -70.0, -80.0, -60.0, 0.0, -100.0,
                            5.0, 3.0, 1.0, 10.0, 0.2, 0.0);
   }

   public RobotHeadDirection (Namespace<NetworkModule> namespace, String name) {
      this(namespace, name, null,
           8, 2, 5,
           0.07, 0.015,
           1.0);
   }

   public RobotHeadDirection (String name) { this(getGlobalNamespace(), name); }
   public RobotHeadDirection () { this((String) null); }

   protected RobotHeadDirection (RobotHeadDirection orig) {
      this(orig.moreSpecificNamespace, orig.getCopiedName(), orig.getNeuronType(),
           orig.numberOfDirections.getValue(), orig.maxShiftVelocity.getValue(), orig.numberOfNeurons.getValue(),
           orig.inhibitionWeight.getValue(), orig.excitationWeight.getValue(),
           orig.synapseDelay.getValue());
   }

   private transient List<String> outputPortNames, inputPortAllNames,
         inputPortInitName = Collections.singletonList("init dir"),
         inputPortShiftPosNames, inputPortShiftNegNames;  // caches the lists, if retrieved multiple times

   @Override
   protected List<String> getPortNames (Port.Direction direction) {
      if (direction == Port.Direction.IN) {
         int maxVelocity = this.maxShiftVelocity.getValue();
         inputPortAllNames = concatPortNames(
               inputPortAllNames,
               inputPortInitName,
               inputPortShiftNegNames = buildPortNames(inputPortShiftNegNames, "shift neg", maxVelocity),
               inputPortShiftPosNames = buildPortNames(inputPortShiftPosNames, "shift pos", maxVelocity));
         return inputPortAllNames;
      }
      else if (direction == Port.Direction.OUT) {
         int numDirections = numberOfDirections.getValue();
         if (360 % numDirections == 0)
            return outputPortNames = buildPortNames(outputPortNames, "deg", numDirections,
                                                    n -> n * (360 / numDirections));
         else
            return outputPortNames = buildPortNames(outputPortNames, "dir", numDirections);
      }
      else
         throw new IllegalArgumentException("Unexpected direction: " + direction);
   }

   @Override
   protected boolean getPortIsOptional (Port.Direction direction, int portIndex) {
      return direction == Port.Direction.OUT;  // make all outputs optional
   }

   @Override
   protected int getPortDimension (Port.Direction direction, int portIndex) {
      return numberOfNeurons.getValue();
   }

   @Override
   public LanguageEntity duplicate () { return new RobotHeadDirection(this); }

   public static final EntityCreator<RobotHeadDirection> CREATOR = new Creator();

   private static class Creator extends ModuleInstanceCreator<RobotHeadDirection> {
      @Override
      public RobotHeadDirection create () { return new RobotHeadDirection(); }

      @Override
      public String getResourceFileBaseName () { return "robot_head"; }

      @Override
      public String getIconCaption () { return "Direction"; }

      @Override
      public int getInitialCellWidth () { return 125; }
      // increase default width of 2 * 100 to 2 * 125, because we use somewhat longer port names
   }

   @CodeGenUse
   public int getNumberOfDirections () { return numberOfDirections.getValue(); }
   @CodeGenUse
   public int getMaxShiftVelocity () { return maxShiftVelocity.getValue(); }
   @CodeGenUse
   public int getNumberOfNeuronsPerPopulation () { return numberOfNeurons.getValue(); }
   @CodeGenUse
   public double getInhibitionWeight () { return inhibitionWeight.getValue(); }
   @CodeGenUse
   public double getExcitationWeight () { return excitationWeight.getValue(); }
   @CodeGenUse
   public double getSynapseDelay () { return synapseDelay.getValue(); }

}
