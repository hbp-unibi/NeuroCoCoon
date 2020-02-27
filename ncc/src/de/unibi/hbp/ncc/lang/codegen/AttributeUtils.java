package de.unibi.hbp.ncc.lang.codegen;

import com.mxgraph.model.mxICell;
import de.unibi.hbp.ncc.lang.NetworkModule;
import de.unibi.hbp.ncc.lang.NeuronPopulation;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
final class AttributeUtils {
   private AttributeUtils () { }

   static Optional<Integer> getNeuronCount (mxICell cell) {
      Object value = cell.getValue();
      if (value instanceof NeuronPopulation)
         return Optional.of(((NeuronPopulation) value).getNeuronCount());
      else if (value instanceof NetworkModule.Port) {
         NetworkModule.Port port = (NetworkModule.Port) value;
         NetworkModule module = (NetworkModule) cell.getParent().getValue();
         return Optional.of(module.getPortDimension(port));
      }
      else
         return Optional.empty();
   }

   static <T> boolean definitelyNotEqual (Optional<T> valueA, Optional<T> valueB) {
      return valueA.isPresent() && valueB.isPresent() && !valueA.get().equals(valueB.get());
   }

}
