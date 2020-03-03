package de.unibi.hbp.ncc.lang.codegen;

import com.mxgraph.model.mxICell;
import de.unibi.hbp.ncc.lang.NetworkModule;
import de.unibi.hbp.ncc.lang.NeuronPopulation;

import java.util.Optional;

final class AttributeUtils {
   private AttributeUtils () { }

   static NetworkModule getParentModule (NetworkModule.Port port) {
      return (NetworkModule) port.getCell().getParent().getValue();
   }

   static Integer getNeuronCount (mxICell cell) {
      Object value = cell.getValue();
      if (value instanceof NeuronPopulation)
         return ((NeuronPopulation) value).getNeuronCount();
      else if (value instanceof NetworkModule.Port) {
         NetworkModule.Port port = (NetworkModule.Port) value;
         NetworkModule module = getParentModule(port);
         return module.getPortDimension(port);
      }
      else
         return null;
   }

   static boolean definitelyNotEqual (Object valueA, Object valueB) {
      return valueA != null && valueB != null && !valueA.equals(valueB);
   }

}
