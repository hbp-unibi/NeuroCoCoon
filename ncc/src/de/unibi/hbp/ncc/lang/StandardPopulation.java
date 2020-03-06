package de.unibi.hbp.ncc.lang;

import de.unibi.hbp.ncc.editor.EntityCreator;
import de.unibi.hbp.ncc.lang.props.EditableNameProp;
import de.unibi.hbp.ncc.lang.props.EditableProp;

import java.util.Collection;
import java.util.EnumSet;
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

   @Override
   protected String getGeneratedNamesPrefix () { return "Population"; }

   public StandardPopulation (Namespace<NeuronPopulation> namespace, String name, NeuronType neuronType, int neuronCount) {
      super(namespace, name, neuronCount);
      Namespace<NeuronType> neuronTypes = namespace.getContainingScope().getNeuronTypes();
      if (neuronType == null)
         neuronType = neuronTypes.get("Default");
      this.neuronType = new EditableNameProp<>("Neuron Type", NeuronType.class, this,
                                               Objects.requireNonNull(neuronType), neuronTypes);
   }

   public StandardPopulation (Namespace<NeuronPopulation> namespace, String name) {
      this(namespace, name, null, 1);
   }

   public StandardPopulation (String name) {
      this(getGlobalNamespace(), name);
   }
   public StandardPopulation (Namespace<NeuronPopulation> namespace) {
      this(namespace, null);
   }
   public StandardPopulation () { this((String) null); }

   protected StandardPopulation (StandardPopulation orig) {
      this(orig.moreSpecificNamespace, orig.getCopiedName(), orig.neuronType.getValue(),
           orig.getNeuronCountProp().getValue());
   }

   @Override
   public Collection<ProbeConnection.DataSeries> validDataSeries () {
      return neuronType.getValue().getSupportedDataSeries();
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

      @Override
      public String getResourceFileBaseName () { return "population"; }

      @Override
      public String getIconCaption () { return "Population"; }

      @Override
      public String getCellStyle () { return "population"; }

      @Override
      public int getInitialCellHeight () { return 60; }
   }

   @Override
   public NeuronPopulation duplicate () {
      return new StandardPopulation(this);
   }

   public boolean isValidSynapseTarget () { return true; }

   public NeuronType getNeuronType () { return neuronType.getValue(); }

}
