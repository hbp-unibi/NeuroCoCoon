package de.unibi.hbp.ncc.lang.codegen;

import com.mxgraph.model.mxICell;
import de.unibi.hbp.ncc.lang.DisplayNamed;
import de.unibi.hbp.ncc.lang.NetworkModule;
import de.unibi.hbp.ncc.lang.NeuronPopulation;
import de.unibi.hbp.ncc.lang.StandardPopulation;

final class AttributeUtils {
   private AttributeUtils () { }

   static Integer getNeuronCount (mxICell cell) {
      Object value = cell.getValue();
      if (value instanceof NeuronPopulation)
         return ((NeuronPopulation) value).getNeuronCount();
      else if (value instanceof NetworkModule.Port) {
         NetworkModule.Port port = (NetworkModule.Port) value;
         NetworkModule module = port.getOwningModule();
         return module.getPortDimension(port);
      }
      else
         return null;
   }

   static Boolean isConductanceBased (mxICell cell) {
      Object value = cell.getValue();
      if (value instanceof StandardPopulation)
         return ((StandardPopulation) value).getNeuronType().isConductanceBased();
      // TODO what about RegularSpikeSource and PoissonSource?
      else if (value instanceof NetworkModule.Port) {
         NetworkModule.Port port = (NetworkModule.Port) value;
         NetworkModule module = port.getOwningModule();
         return module.isConductanceBasedPort(port);
      }
      else
         return null;
   }

   static boolean definitelyNotEqual (Object valueA, Object valueB) {
      return valueA != null && valueB != null && !valueA.equals(valueB);
   }

   static String display (Object any) {
      if (any instanceof DisplayNamed)
         return ((DisplayNamed) any).getDisplayName();
      else if (any == null)
         return "<null>";
      else
         return any.toString();
   }
}
