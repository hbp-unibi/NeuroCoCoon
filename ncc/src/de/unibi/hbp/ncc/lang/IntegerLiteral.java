package de.unibi.hbp.ncc.lang;

import java.util.List;

public class IntegerLiteral extends Literal<Integer> implements IntegerReadOnlyValue {

   public IntegerLiteral (Integer value) {
      super(value);
   }

}
