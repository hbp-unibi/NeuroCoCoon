package de.unibi.hbp.ncc.lang;

import com.mxgraph.model.mxICell;
import de.unibi.hbp.ncc.lang.props.StringProp;

public abstract class AnyConnection extends LanguageEntity {
   private Connectable.EdgeKind edgeKind;
   // TODO use the common enum for edge types in Connectable and consolidate all methods into one version with such an enum parameter
   protected final StringProp userLabel;

   protected AnyConnection (Connectable.EdgeKind edgeKind, String userLabel) {
      this.edgeKind = edgeKind;
      this.userLabel = new StringProp("Edge Label", this, userLabel != null ? userLabel : "");
   }

   public Connectable.EdgeKind getEdgeKind () { return edgeKind; }

   // this class does not add userLabel to the editable properties automatically
   // so that subclasses have full control over the position in the inspector

   protected String getUserLabelOrNull () {  // normalizes empty labels to null (property never stores null)
      String value = userLabel.getValue();
      return value != null && value.isEmpty() ? null : value;
   }

   private Object getTerminalValue (boolean source) {
      mxICell edgeCell = getOwningCell();
      if (edgeCell != null) {
         mxICell terminalCell = edgeCell.getTerminal(source);
         if (terminalCell != null)
            return terminalCell.getValue();
      }
      return null;
   }

   public LanguageEntity getSourceEntity () {
      Object sourceValue = getTerminalValue(true);
      return (sourceValue instanceof LanguageEntity) ? (LanguageEntity) sourceValue : null;
   }

   public LanguageEntity getTargetEntity () {
      Object targetValue = getTerminalValue(false);
      return (targetValue instanceof LanguageEntity) ? (LanguageEntity) targetValue : null;
   }

   public NamedEntity getSourceNamedEntity () {
      Object sourceValue = getTerminalValue(true);
      return (sourceValue instanceof NamedEntity) ? (NamedEntity) sourceValue : null;
   }

   public NamedEntity getTargetNamedEntity () {
      Object targetValue = getTerminalValue(false);
      return (targetValue instanceof NamedEntity) ? (NamedEntity) targetValue : null;
   }

   public NeuronPopulation getSourcePopulation () {
      Object sourceValue = getTerminalValue(true);
      return (sourceValue instanceof NeuronPopulation) ? (NeuronPopulation) sourceValue : null;
   }

   public NeuronPopulation getTargetPopulation () {
      Object targetValue = getTerminalValue(false);
      return (targetValue instanceof NeuronPopulation) ? (NeuronPopulation) targetValue : null;
   }

   public NetworkModule.Port getSourceModulePort () {
      Object sourceValue = getTerminalValue(true);
      return (sourceValue instanceof NetworkModule.Port) ? (NetworkModule.Port) sourceValue : null;
   }

   public NetworkModule.Port getTargetModulePort () {
      Object targetValue = getTerminalValue(false);
      return (targetValue instanceof NetworkModule.Port) ? (NetworkModule.Port) targetValue : null;
   }

   public PlotDataSource getTargetPlotDataSource () {
      Object targetValue = getTerminalValue(false);
      return (targetValue instanceof PlotDataSource) ? (PlotDataSource) targetValue : null;
   }

}
