package de.unibi.hbp.ncc.lang;

public class IntegerConstant extends Constant<Integer> implements IntegerValue {

   public IntegerConstant (Namespace<Constant<Number>> namespace, String name, Integer value) {
      super(namespace, name, value);
   }

   public IntegerConstant (Namespace<Constant<Number>> namespace) {
      super(namespace);
      setValue(0);
   }

   @Override
   public LanguageEntity duplicate () {
      throw new UnsupportedOperationException();
      // FIXME implement this or remove the class
   }
}
