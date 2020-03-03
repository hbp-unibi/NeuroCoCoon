package de.unibi.hbp.ncc.lang;

import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import de.unibi.hbp.ncc.graph.AbstractCellsCollector;
import de.unibi.hbp.ncc.lang.props.DoubleProp;
import de.unibi.hbp.ncc.lang.props.EditableEnumProp;
import de.unibi.hbp.ncc.lang.props.EditableProp;
import de.unibi.hbp.ncc.lang.props.NonNegativeDoubleProp;
import de.unibi.hbp.ncc.lang.props.ProbabilityProp;
import de.unibi.hbp.ncc.lang.props.ReadOnlyProp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SynapseType extends NamedEntity {

   public enum ConnectorKind implements DisplayNamed, PythonNamed, PyNNAssociated {
      ALL_TO_ALL("All:All", "allToAll", "all_to_all", "AllToAllConnector"),
      ONE_TO_ONE("One:One", "oneToOne", "one_to_one", "OneToOneConnector"),
      FIXED_PROBABILITY("Probability", "probability", null, "FixedProbabilityConnector");

      private String displayName, edgeStyle, pythonName, pyNNClassName;

      ConnectorKind (String displayName, String edgeStyle, String pythonVarName, String pyNNClassName) {
         assert (pythonVarName == null) == hasKindSpecificProps() : "connectors with specific properties cannot be created and stored globally";
         this.displayName = displayName;
         this.edgeStyle = edgeStyle;
         this.pythonName = pythonVarName;
         this.pyNNClassName = pyNNClassName;
      }

      @Override
      public String getDisplayName () { return displayName; }

      public boolean hasKindSpecificProps () { return this == FIXED_PROBABILITY; }

      void addKindSpecificProps (List<? super EditableProp<?>> list, SynapseType type) {
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

      public String getPyNNClassName () { return pyNNClassName; }

      @Override
      public String getPythonName () {
         return Namespace.buildStaticPythonName("con", pythonName);
      }
   }

   public enum SynapseKind implements DisplayNamed, PyNNAssociated {
      STATIC("Static", "StaticSynapse");

      private String displayName, pyNNClassName;

      SynapseKind (String displayName, String pyNNClassName) {
         this.displayName = displayName;
         this.pyNNClassName = pyNNClassName;
      }

      @Override
      public String getDisplayName () { return displayName; }

      public String getPyNNClassName () { return pyNNClassName; }

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
      addNamePropImpact(EditableProp.Impact.DEPENDENT_CELLS_LABEL);
      this.connectorKind = new EditableEnumProp<>("Connector", ConnectorKind.class, this,
                                                  Objects.requireNonNull(connectorKind))
            .addImpact(EditableProp.Impact.OTHER_PROPS_VISIBILITY)
            .addImpact(EditableProp.Impact.DEPENDENT_CELLS_LABEL)
            .addImpact(EditableProp.Impact.DEPENDENT_CELLS_STYLE);
      this.weight = new DoubleProp("Weight", this, weight)
            .addImpact(EditableProp.Impact.DEPENDENT_CELLS_STYLE);
      this.delay = new NonNegativeDoubleProp("Delay", this, delay).setUnit("ms");
      this.probability = new ProbabilityProp("Probability", this, probability)
            .setPythonName("p_connect")
            .addImpact(EditableProp.Impact.DEPENDENT_CELLS_LABEL);
      this.synapseKind = new EditableEnumProp<>("Synapse Kind", SynapseKind.class, this,
                                                Objects.requireNonNull(synapseKind))
            .addImpact(EditableProp.Impact.OTHER_PROPS_VISIBILITY);
   }

   @Override
   protected String getGeneratedNamesPrefix () { return "Synapse Type"; }

   protected static Namespace<SynapseType> getGlobalNamespace () {
      return NeuronPopulation.getGlobalNamespace().getContainingScope().getSynapseTypes();
   }

   public SynapseType (Namespace<SynapseType> namespace, String name, ConnectorKind kind) {
      this(namespace, name, kind, 1.0, 0.0, 0.5, SynapseKind.STATIC);
   }

   public SynapseType (Namespace<SynapseType> namespace, String name) {  // default for New button in master/detail editor
      this(namespace, null, ConnectorKind.ALL_TO_ALL);
   }

   public SynapseType (String name) {
      this(getGlobalNamespace(), name);
   }
   public SynapseType (Namespace<SynapseType> namespace) { this(namespace, null); }
   public SynapseType () { this((String) null); }

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

   public ConnectorKind getConnectorKind () { return connectorKind.getValue(); }

   public double getWeight () { return weight.getValue(); }
   public double getDelay () { return delay.getValue(); }

   public SynapseKind getSynapseKind () { return synapseKind.getValue(); }

   public Iterable<ReadOnlyProp<?>> getConnectorParameters () {
      List<ReadOnlyProp<?>> result = new ArrayList<>();
      connectorKind.getValue().addKindSpecificProps(result, this);
      return result;
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
