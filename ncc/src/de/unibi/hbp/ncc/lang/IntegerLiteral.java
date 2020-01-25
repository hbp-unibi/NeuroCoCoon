package de.unibi.hbp.ncc.lang;

import java.util.List;

public class IntegerLiteral extends Literal<Integer> implements IntegerReadOnlyValue {

   public IntegerLiteral (Integer value) {
      super(value);
   }

   private static List<PropertyDescriptor<? extends LanguageEntity, ?>> entityProperties;

   @Override
   public List<PropertyDescriptor<? extends LanguageEntity, ?>> getEntityProperties () {
      if (entityProperties == null) {
         entityProperties = buildEntityProperties(IntegerLiteral.class, Integer.class);
      }
      return entityProperties;
   }

}
