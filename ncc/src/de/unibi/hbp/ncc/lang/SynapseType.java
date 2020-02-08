package de.unibi.hbp.ncc.lang;

import de.unibi.hbp.ncc.lang.props.DoubleProp;
import de.unibi.hbp.ncc.lang.props.EditableEnumProp;
import de.unibi.hbp.ncc.lang.props.EditableProp;

import java.util.EnumSet;
import java.util.List;

public class SynapseType extends NamedEntity<SynapseType> {

   public enum SynapseKind implements DisplayNamed {
      ALL_TO_ALL("All:All", "straight;strokeWidth=4"),
      ONE_TO_ONE("One:One", "straight"),
      FIXED_PROBABILITY("Probability", "straight;dashed=1");

      private String displayName, edgeStyle;

      SynapseKind (String displayName, String edgeStyle) {
         this.displayName = displayName;
         this.edgeStyle = edgeStyle;
      }

      @Override
      public String getDisplayName () { return displayName; }

      void addKindSpecificProps (List<EditableProp<?>> list, SynapseType type) {
         if (this == FIXED_PROBABILITY)
            list.add(type.probability);
      }

      String getSummary (SynapseType type) {
         if (this == FIXED_PROBABILITY)
            return "p = " + type.probability.getValue();
         else
            return displayName;
      }

      String getEdgeStyle (SynapseType type) {
         return edgeStyle;
      }
   }

   private EditableEnumProp<SynapseKind> synapseKind;
   private DoubleProp weight, probability;

   public SynapseType (Namespace<SynapseType> namespace, String name, SynapseKind synapseKind,
                      double weight, double probability) {
      super(namespace, name);
      this.synapseKind = new EditableEnumProp<>("Synapse Kind", SynapseKind.class, this, synapseKind)
            .setImpact(EnumSet.of(EditableProp.Impact.OTHER_PROPS_VISIBILITY));
      this.weight = new DoubleProp("Weight", this, weight);
      this.probability = new DoubleProp("Probability", this, probability) {
         @Override
         public boolean isValid (Double proposedValue) {
            return 0 <= proposedValue && proposedValue <= 1.0;
         }
      };
   }

   public SynapseType (Namespace<SynapseType> namespace, String name, SynapseKind kind) {
      this(namespace, name, kind, 1.0, 0.5);
   }

   public SynapseType (SynapseType orig) {
      this(orig.getNamespace(), orig.getCopiedName(), orig.synapseKind.getValue(),
           orig.weight.getValue(), orig.probability.getValue());
   }

   @Override
   protected List<EditableProp<?>> addEditableProps (List<EditableProp<?>> list) {
      super.addEditableProps(list);
      list.add(synapseKind);
      list.add(weight);
      synapseKind.getValue().addKindSpecificProps(list, this);
      return list;
   }

   protected List<EditableProp<?>> addExportedEditableProps (List<EditableProp<?>> list) {
      list.add(synapseKind);
      list.add(weight);
      synapseKind.getValue().addKindSpecificProps(list, this);
      return list;
   }

   public String getSummary () {
      return synapseKind.getValue().getSummary(this);
   }

   public String getEdgeStyle () { return synapseKind.getValue().getEdgeStyle(this); }

   // @Override
   public SynapseType duplicate () {
      return new SynapseType(this);
   }
}
