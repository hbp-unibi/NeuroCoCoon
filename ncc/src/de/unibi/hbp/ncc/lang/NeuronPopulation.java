package de.unibi.hbp.ncc.lang;

import de.unibi.hbp.ncc.lang.props.EditableNameProp;
import de.unibi.hbp.ncc.lang.props.EditableProp;
import de.unibi.hbp.ncc.lang.props.IntegerProp;

import java.util.List;
import java.util.Objects;

public class NeuronPopulation extends NamedEntity<NeuronPopulation> {
   private EditableNameProp<NeuronType> neuronType;
   private IntegerProp neuronCount;

   private static Namespace<NeuronPopulation> globalNamespace;

   public static void setGlobalNamespace (Namespace<NeuronPopulation> ns) { globalNamespace = ns; }

   @Override
   protected List<EditableProp<?>> addEditableProps (List<EditableProp<?>> list) {
      super.addEditableProps(list);
      list.add(neuronType);
      list.add(neuronCount);
      return list;
   }

   @Override
   protected List<EditableProp<?>> addIndirectEditableProps (List<EditableProp<?>> list) {
      return neuronType.getValue().addExportedEditableProps(list);
   }

   public NeuronPopulation (Namespace<NeuronPopulation> namespace, String name, NeuronType neuronType, int neuronCount) {
      super(namespace, name);
      Namespace<NeuronType> neuronTypes = globalNamespace.getContainingScope().getNeuronTypes();
      if (neuronType == null)
         neuronType = neuronTypes.get("Default");
      this.neuronType = new EditableNameProp<NeuronType>("Neuron Type", NeuronType.class, this,
                                                         Objects.requireNonNull(neuronType), neuronTypes);
      this.neuronCount = new IntegerProp("Neuron Count", this, neuronCount) {
         @Override
         public boolean isValid (Integer proposedValue) {
            return proposedValue >= 1;
         }
      };
   }

   public NeuronPopulation (Namespace<NeuronPopulation> namespace) {
      this(namespace, null, null, 1);
   }

   public NeuronPopulation () {
      this(globalNamespace);
   }

   protected NeuronPopulation (NeuronPopulation orig) {
      this(orig.getNamespace(), orig.getCopiedName(), orig.neuronType.getValue(), orig.neuronCount.getValue());
   }

   public static final EntityCreator<NeuronPopulation> CREATOR = new Creator();

   private static class Creator implements EntityCreator<NeuronPopulation> {
      @Override
      public NeuronPopulation create () {
         return new NeuronPopulation();
      }

      @Override
      public String toString () {  // used by drag&drop tooltips
         return "Neuron Population";
      }
   }

   public int getNeuronCount () {
      return neuronCount.getValue();
   }

   public void setNeuronCount (int neuronCount) {
      this.neuronCount.setValue(neuronCount);
   }

   // @Override
   public NeuronPopulation duplicate () {
      return new NeuronPopulation(this);
   }
}
