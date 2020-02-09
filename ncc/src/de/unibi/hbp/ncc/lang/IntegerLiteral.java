package de.unibi.hbp.ncc.lang;

public class IntegerLiteral extends Literal<Integer> implements IntegerReadOnlyValue {

   public IntegerLiteral (Integer value) {
      super(value);
   }

   @Override
   public LanguageEntity duplicate () {
      throw new UnsupportedOperationException();
      // FIXME implement this or remove the class
   }
}
