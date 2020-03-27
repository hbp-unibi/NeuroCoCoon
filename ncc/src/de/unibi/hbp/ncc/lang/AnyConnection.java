package de.unibi.hbp.ncc.lang;

import com.mxgraph.model.mxICell;
import de.unibi.hbp.ncc.editor.props.Notificator;
import de.unibi.hbp.ncc.lang.props.EditableEnumProp;
import de.unibi.hbp.ncc.lang.props.EditableProp;
import de.unibi.hbp.ncc.lang.props.StringProp;

public abstract class AnyConnection extends LanguageEntity {
   private Connectable.EdgeKind edgeKind;
   protected final StringProp userLabel;
   protected final EditableEnumProp<RoutingStyle> routingStyle;

   public enum RoutingStyle implements DisplayNamed {
      DEFAULT("Orthogonal", ""),
      ELBOW_VERTICAL("Elbow, vertical", "edgeStyle=elbowEdgeStyle;elbow=vertical"),
      ELBOW_HORIZONTAL("Elbow, horizontal", "edgeStyle=elbowEdgeStyle;elbow=horizontal"),
      RELATION("Relation", "edgeStyle=entityRelationEdgeStyle"),
      TOP_TO_BOTTOM("Top-to-bottom", "edgeStyle=topToBottomEdgeStyle"),
      SIDE_TO_SIDE("Side-to-side", "edgeStyle=sideToSideEdgeStyle");
      // "edgeStyle=segmentEdgeStyle",  // segment edges seem to allow additional control points,
      // but how to edit them is unclear and toggling to some other edge style seems to leave garbage control points behind
      // "edgeStyle=loopEdgeStyle"  // this is applied automatically for loops?

      private String displayName, edgeStyle;

      RoutingStyle (String displayName, String edgeStyle) {
         this.displayName = displayName;
         this.edgeStyle = edgeStyle;
      }

      String getEdgeStyle () { return edgeStyle; }

      RoutingStyle getNextStyle () {
         int nextOrd = ordinal() + 1;
         RoutingStyle[] allStyles = values();
         if (nextOrd < allStyles.length)
            return allStyles[nextOrd];
         else
            return allStyles[0];
      }

      @Override
      public String getDisplayName () { return displayName; }
   }

   protected AnyConnection (Connectable.EdgeKind edgeKind, String userLabel, RoutingStyle routingStyle) {
      this.edgeKind = edgeKind;
      this.userLabel = new StringProp("Edge Label", this, userLabel != null ? userLabel : "")
            .addImpact(EditableProp.Impact.CELL_LABEL);
      this.routingStyle = new EditableEnumProp<>("Routing", RoutingStyle.class, this,
                                                 routingStyle != null ? routingStyle : RoutingStyle.DEFAULT)
            .addImpact(EditableProp.Impact.CELL_STYLE);
   }

   public Connectable.EdgeKind getEdgeKind () { return edgeKind; }

   // this class does not add userLabel and routingStyle to the editable properties automatically
   // so that subclasses have full control over the position in the inspector

   protected String getUserLabelOrNull () {  // normalizes empty labels to null (property never stores null)
      String value = userLabel.getValue();
      return value != null && value.isEmpty() ? null : value;
   }

   protected abstract String getEdgeStyle ();

   // for handling edge clicks in the graph view
   public void cycleRoutingStyle () {
      routingStyle.setValue(routingStyle.getValue().getNextStyle());
      Notificator.getInstance().notifyListeners(routingStyle);
   }

   public RoutingStyle getRoutingStyle () { return routingStyle.getValue(); }

   @Override
   public String getCellStyle () {
      return joinCellStyles(getEdgeStyle(), routingStyle.getValue().getEdgeStyle());
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
