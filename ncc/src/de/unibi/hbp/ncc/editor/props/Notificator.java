package de.unibi.hbp.ncc.editor.props;

import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import de.unibi.hbp.ncc.lang.GraphCellConfigurator;
import de.unibi.hbp.ncc.lang.LanguageEntity;
import de.unibi.hbp.ncc.lang.props.EditableProp;

import java.util.ArrayList;
import java.util.List;

public class Notificator {
   // we currently support only one graph component
   private mxGraphComponent graphComponent;
   private List<PropChangeListener> propChangeListeners;
   // should not need to notify the master part of a MasterDetailsEditor

   private Notificator () {
      propChangeListeners = new ArrayList<>();
   }

   public void subscribe (mxGraphComponent graphComponent) {
      if (this.graphComponent != null)
         throw new IllegalStateException("only one graph component is currently supported: " + this.graphComponent);
      this.graphComponent = graphComponent;
   }

   public void subscribe (PropChangeListener listener) {
      if (propChangeListeners.contains(listener))
         throw new IllegalStateException("listener already subscribed: " + listener);
      propChangeListeners.add(listener);
   }

   public void unsubscribe (mxGraphComponent graphComponent) {
      if (!graphComponent.equals(this.graphComponent))
         throw new IllegalStateException("graph component is not subscribed: " + graphComponent);
      this.graphComponent = null;
   }

   public void unsubscribe (PropChangeListener listener) {
      if (!propChangeListeners.remove(listener))
         throw new IllegalStateException("listener is not subscribed: " + listener);
      ;
   }

   // position is only forwarded to the source of the notification
   // all other listeners get an unknown position
   public void notifyListeners (PropChangeListener source, EditableProp<?> changed, int positionInSource) {
      // no need to notify the originating editor (may be null to notify all editors)
      LanguageEntity enclosingEntity = changed.getEnclosingEntity();
      if (changed.hasChangeImpact(EditableProp.Impact.OWN_VALUE)) {  // should always be included
         for (PropChangeListener listener: propChangeListeners) {
            listener.propertyChanged(changed,
                                     listener.equals(source)
                                           ? positionInSource
                                           : PropChangeListener.UNKNOWN_POSITION);
         }
      }
      if (changed.hasChangeImpact(EditableProp.Impact.OTHER_PROPS_VISIBILITY)) {
         // this is more general and implies that values of other properties may also have changed
         for (PropChangeListener listener: propChangeListeners)
            listener.otherPropertiesVisibilityChanged(enclosingEntity);
      }
      else if (changed.hasChangeImpact(EditableProp.Impact.OTHER_PROPS_VALUES)) {
         for (PropChangeListener listener: propChangeListeners)
            listener.multiplePropertyValuesChanged(enclosingEntity);
      }
      mxICell cell = enclosingEntity.getOwningCell();
      if (cell != null && changed.hasChangeImpact(EditableProp.Impact.CELL_LABEL)) {
         graphComponent.labelChanged(cell, enclosingEntity, null);  // enclosingEntity == cell.getValue()
      }
      if (cell != null && changed.hasChangeImpact(EditableProp.Impact.CELL_STYLE)) {
         String style = enclosingEntity.getCellStyle();
         if (style != null)
            graphComponent.getGraph().setCellStyle(style, new Object[] { cell });
      }
      if (cell != null && changed.hasChangeImpact(EditableProp.Impact.CELL_STRUCTURE)) {
         mxGraph graph = graphComponent.getGraph();
         graph.getModel().beginUpdate();
         try {
            ((GraphCellConfigurator) enclosingEntity).restructureExisting(graph, cell);
         }
         finally {
            graph.getModel().endUpdate();
         }
      }
      List<mxICell> dependentCells = null;
      if (changed.hasChangeImpact(EditableProp.Impact.DEPENDENT_CELLS_STYLE)) {  // update this before the label (more visually important)
         String style = enclosingEntity.getCellStyle();
         // System.err.println("DEPENDENT_CELLS_STYLE: " + style);
         if (style != null) {
            mxGraph graph = graphComponent.getGraph();
            dependentCells = enclosingEntity.getDependentCells(graph.getModel());
            graph.setCellStyle(style, dependentCells.toArray());
         }
      }
      if (changed.hasChangeImpact(EditableProp.Impact.DEPENDENT_CELLS_LABEL)) {
         mxGraph graph = graphComponent.getGraph();
         if (dependentCells == null)  // might have been computed above already
            dependentCells = enclosingEntity.getDependentCells(graph.getModel());
         for (mxICell dependentCell: dependentCells)
            graphComponent.labelChanged(dependentCell, dependentCell.getValue(), null);
      }
   }

   // for notifications originating outside a PropertyChangeListener (i.e. outside a DetailsEditor
   public void notifyListeners (EditableProp<?> changed) {
      notifyListeners(null, changed, PropChangeListener.UNKNOWN_POSITION);
   }

   public void notifyListeners (LanguageEntity entity) {  // simulates EditableProp.Impact.OTHER_PROPS_VALUES for the given entity
      for (PropChangeListener listener: propChangeListeners)
         listener.multiplePropertyValuesChanged(entity);
   }

   private static final Notificator theInstance = new Notificator();

   public static Notificator getInstance() { return theInstance; }
}
