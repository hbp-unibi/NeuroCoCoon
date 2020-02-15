package de.unibi.hbp.ncc.lang;

import de.unibi.hbp.ncc.lang.props.EditableNameProp;
import de.unibi.hbp.ncc.lang.props.EditableProp;

import java.util.List;
import java.util.Objects;

public class StandardPopulation extends NeuronPopulation {
   private EditableNameProp<NeuronType> neuronType;

   @Override
   protected List<EditableProp<?>> addEditableProps (List<EditableProp<?>> list) {
      super.addEditableProps(list);
      list.add(neuronType);
      return list;
   }

   @Override
   protected List<EditableProp<?>> addIndirectEditableProps (List<EditableProp<?>> list) {
      return neuronType.getValue().addExportedEditableProps(list);
   }

   public StandardPopulation (Namespace<NeuronPopulation> namespace, String name, NeuronType neuronType, int neuronCount) {
      super(namespace, name, neuronCount);
      Namespace<NeuronType> neuronTypes = getGlobalNamespace().getContainingScope().getNeuronTypes();
      if (neuronType == null)
         neuronType = neuronTypes.get("Default");
      this.neuronType = new EditableNameProp<NeuronType>("Neuron Type", NeuronType.class, this,
                                                         Objects.requireNonNull(neuronType), neuronTypes);
   }

   public StandardPopulation (Namespace<NeuronPopulation> namespace) {
      this(namespace, null, null, 1);
   }

   public StandardPopulation () {
      this(getGlobalNamespace());
   }

   protected StandardPopulation (StandardPopulation orig) {
      this(orig.getNamespace(), orig.getCopiedName(), orig.neuronType.getValue(),
           orig.getNeuronCountProp().getValue());
   }

   public static final EntityCreator<StandardPopulation> CREATOR = new StandardPopulation.Creator();

   private static class Creator implements EntityCreator<StandardPopulation> {
      @Override
      public StandardPopulation create () {
         return new StandardPopulation();
      }

      @Override
      public String toString () {  // used by drag&drop tooltips
         return "Standard Population";
      }
   }

   @Override
   public NeuronPopulation duplicate () {
      return new StandardPopulation(this);
   }

   public boolean isValidConnectionTarget () { return true; }

}
