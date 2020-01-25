package de.unibi.hbp.ncc.lang;

import java.util.List;

public class DoubleLiteral extends Literal<Double> implements DoubleReadOnlyValue {

   public DoubleLiteral (Double value) {
      super(value);
   }

   private static List<PropertyDescriptor<? extends LanguageEntity, ?>> entityProperties;

   @Override
   public List<PropertyDescriptor<? extends LanguageEntity, ?>> getEntityProperties () {
      if (entityProperties == null) {
         entityProperties = buildEntityProperties(DoubleLiteral.class, Double.class);
      }
      return entityProperties;
   }

}
