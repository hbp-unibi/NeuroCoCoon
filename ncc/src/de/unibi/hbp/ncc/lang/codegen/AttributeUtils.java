package de.unibi.hbp.ncc.lang.codegen;

import com.mxgraph.model.mxICell;
import de.unibi.hbp.ncc.lang.NetworkModule;
import de.unibi.hbp.ncc.lang.NeuronPopulation;

import java.util.Optional;

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

   static boolean definitelyNotEqual (Object valueA, Object valueB) {
      return valueA != null && valueB != null && !valueA.equals(valueB);
   }

}
