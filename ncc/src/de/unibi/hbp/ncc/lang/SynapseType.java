package de.unibi.hbp.ncc.lang;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxIGraphModel;
import de.unibi.hbp.ncc.lang.props.DoubleProp;
import de.unibi.hbp.ncc.lang.props.EditableEnumProp;
import de.unibi.hbp.ncc.lang.props.EditableProp;
import de.unibi.hbp.ncc.lang.props.NonNegativeDoubleProp;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public class SynapseType extends NamedEntity<SynapseType> {

   public enum ConnectorKind implements DisplayNamed {
      ALL_TO_ALL("All:All", "alltoall"),
      ONE_TO_ONE("One:One", "onetoone"),
      FIXED_PROBABILITY("Probability", "probability");

      private String displayName, edgeStyle;

      ConnectorKind (String displayName, String edgeStyle) {
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
         // TODO add weight or delay to text, if "unusual"
      }

      String getEdgeStyle (SynapseType type) {

         double weight = type.weight.getValue();
         if (weight < 0)
            return edgeStyle + ";endArrow=oval";
         else if (weight > 0)
            return edgeStyle;
         else return "weightzero";
      }
   }

   public enum SynapseKind implements DisplayNamed {
      STATIC("Static");

      private String displayName;

      SynapseKind (String displayName) { this.displayName = displayName; }

      @Override
      public String getDisplayName () { return displayName; }

      void addKindSpecificProps (List<EditableProp<?>> list, SynapseType type) {
         // nothing currently
      }
   }

   private EditableEnumProp<ConnectorKind> connectorKind;
   private DoubleProp weight, delay, probability;
   private EditableEnumProp<SynapseKind> synapseKind;

   public SynapseType (Namespace<SynapseType> namespace, String name, ConnectorKind connectorKind,
                      double weight, double delay, double probability,
                       SynapseKind synapseKind) {
      super(namespace, name);
      this.connectorKind = new EditableEnumProp<>("Connector", ConnectorKind.class, this,
                                                  Objects.requireNonNull(connectorKind))
            .setImpact(EnumSet.of(EditableProp.Impact.OTHER_PROPS_VISIBILITY));
      this.weight = new DoubleProp("Weight", this, weight)
            .setImpact(EnumSet.of(EditableProp.Impact.DEPENDENT_CELLS_STYLE));
      this.delay = new NonNegativeDoubleProp("Delay", this, delay).setUnit("ms");
      this.probability = new NonNegativeDoubleProp("Probability", this, probability) {
         @Override
         public boolean isValid (Double proposedValue) {
            return super.isValid(proposedValue) && proposedValue <= 1.0;
         }
      };
      this.synapseKind = new EditableEnumProp<>("Synapse Kind", SynapseKind.class, this,
                                                  Objects.requireNonNull(synapseKind))
            .setImpact(EnumSet.of(EditableProp.Impact.OTHER_PROPS_VISIBILITY));
   }

   public SynapseType (Namespace<SynapseType> namespace, String name, ConnectorKind kind) {
      this(namespace, name, kind, 1.0, 0.0, 0.5, SynapseKind.STATIC);
   }

   public SynapseType (SynapseType orig) {
      this(orig.getNamespace(), orig.getCopiedName(), orig.connectorKind.getValue(),
           orig.weight.getValue(), orig.delay.getValue(), orig.probability.getValue(),
           orig.synapseKind.getValue());
   }

   @Override
   protected List<EditableProp<?>> addEditableProps (List<EditableProp<?>> list) {
      super.addEditableProps(list);
      list.add(connectorKind);
      list.add(weight);
      list.add(delay);
      list.add(synapseKind);
      connectorKind.getValue().addKindSpecificProps(list, this);
      synapseKind.getValue().addKindSpecificProps(list, this);
      return list;
   }

   protected List<EditableProp<?>> addExportedEditableProps (List<EditableProp<?>> list) {
      list.add(connectorKind);
      list.add(weight);
      connectorKind.getValue().addKindSpecificProps(list, this);
      return list;
   }

   @Override
   public List<mxCell> getDependentCells (mxIGraphModel graphModel) {
      List<mxCell> dependentEdges = new ArrayList<>();
      Object root = graphModel.getRoot();
      int count = graphModel.getChildCount(root);
      for (int i = 0; i < count; i++) {
         Object obj = graphModel.getChildAt(root, i);
         if (obj instanceof mxCell) {
            mxCell cell = (mxCell) obj;
            Object value = cell.getValue();
            if (value instanceof NeuronConnection) {
               NeuronConnection connection = (NeuronConnection) value;
               if (this.equals(connection.getSynapseType()))
                  dependentEdges.add(cell);
            }
         }
      }
      return dependentEdges;  // TODO add impact level for multiple dependent cells
   }

   public String getSummary () {
      return connectorKind.getValue().getSummary(this);
   }
   // TODO add synapse kind info, if not STATIC

   public String getEdgeStyle () { return connectorKind.getValue().getEdgeStyle(this); }

   // @Override
   public SynapseType duplicate () {
      return new SynapseType(this);
   }
}
