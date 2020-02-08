package de.unibi.hbp.ncc.lang;

import java.util.List;

public class DoubleConstant extends Constant<Double> implements DoubleValue {

   public DoubleConstant (Namespace<Constant<Number>> namespace, String name, Double value) {
      super(namespace, name, value);
   }

   public DoubleConstant (Namespace<Constant<Number>> namespace) {
      super(namespace);
      setValue(0.0);
   }

}
