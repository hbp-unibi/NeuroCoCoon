package de.unibi.hbp.ncc.lang;

import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import de.unibi.hbp.ncc.graph.AbstractCellsCollector;
import de.unibi.hbp.ncc.lang.props.DoubleProp;
import de.unibi.hbp.ncc.lang.props.EditableEnumProp;
import de.unibi.hbp.ncc.lang.props.EditableProp;
import de.unibi.hbp.ncc.lang.props.NonNegativeDoubleProp;
import de.unibi.hbp.ncc.lang.props.ProbabilityProp;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public class SynapseType extends NamedEntity {

   public enum ConnectorKind implements DisplayNamed {
      ALL_TO_ALL("All:All", "allToAll"),
      ONE_TO_ONE("One:One", "oneToOne"),
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
         else return "weightZero";
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

   private final Namespace<SynapseType> moreSpecificNamespace;
   private final EditableEnumProp<ConnectorKind> connectorKind;
   private final DoubleProp weight, delay, probability;
   private final EditableEnumProp<SynapseKind> synapseKind;

   public SynapseType (Namespace<SynapseType> namespace, String name, ConnectorKind connectorKind,
                       double weight, double delay, double probability,
                       SynapseKind synapseKind) {
      super(namespace, name);
      moreSpecificNamespace = namespace;
      this.connectorKind = new EditableEnumProp<>("Connector", ConnectorKind.class, this,
                                                  Objects.requireNonNull(connectorKind))
            .setImpact(EnumSet.of(EditableProp.Impact.OTHER_PROPS_VISIBILITY,
                                  EditableProp.Impact.DEPENDENT_CELLS_LABEL,
                                  EditableProp.Impact.DEPENDENT_CELLS_STYLE));
      this.weight = new DoubleProp("Weight", this, weight)
            .setImpact(EditableProp.Impact.DEPENDENT_CELLS_STYLE);
      // FIXME styles in dependent cells (edges) are NOT updated when the sign of the weight is changed (cell collector ok?)
      this.delay = new NonNegativeDoubleProp("Delay", this, delay).setUnit("ms");
      this.probability = new ProbabilityProp("Probability", this, probability)
            .setImpact(EditableProp.Impact.DEPENDENT_CELLS_LABEL);
      this.synapseKind = new EditableEnumProp<>("Synapse Kind", SynapseKind.class, this,
                                                Objects.requireNonNull(synapseKind))
            .setImpact(EditableProp.Impact.OTHER_PROPS_VISIBILITY);
   }

   @Override
   protected String getGeneratedNamesPrefix () { return "Synapse Type"; }

   public SynapseType (Namespace<SynapseType> namespace, String name, ConnectorKind kind) {
      this(namespace, name, kind, 1.0, 0.0, 0.5, SynapseKind.STATIC);
   }

   public SynapseType (Namespace<SynapseType> namespace) {  // default for New button in master/detail editor
      this(namespace, null, ConnectorKind.ALL_TO_ALL);
   }

   public SynapseType (SynapseType orig) {
      this(orig.moreSpecificNamespace, orig.getCopiedName(), orig.connectorKind.getValue(),
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
   public List<mxICell> getDependentCells (mxIGraphModel graphModel) {
      return new AbstractCellsCollector(false, true) {
         @Override
         protected boolean matches (mxICell cell, LanguageEntity entity) {
            if (entity instanceof NeuronConnection)
               System.err.println("getDependentCells: " + cell + ", " + entity + ", " + SynapseType.this + ", " +
                                        ((NeuronConnection) entity).getSynapseType());
            return entity instanceof NeuronConnection &&
                  SynapseType.this.equals(((NeuronConnection) entity).getSynapseType());
         }
      }.getMatchingCells(graphModel);
   }

   public String getSummary () {
      return connectorKind.getValue().getSummary(this);
   }
   // TODO add synapse kind info, if not STATIC

   @Override
   public String getCellStyle () {  // has no visual representation of its own, but NeuronConenction delegates here
      return connectorKind.getValue().getEdgeStyle(this);
   }

   // @Override
   public SynapseType duplicate () {
      return new SynapseType(this);
   }
}
