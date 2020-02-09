package de.unibi.hbp.ncc.lang;

public class DoubleLiteral extends Literal<Double> implements DoubleReadOnlyValue {

   public DoubleLiteral (Double value) {
      super(value);
   }

   @Override
   public LanguageEntity duplicate () {
      throw new UnsupportedOperationException();
      // FIXME implement this or remove the class
   }

}
