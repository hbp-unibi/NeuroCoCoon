package de.unibi.hbp.ncc.lang;

import java.util.List;

public class DoubleConstant extends Constant<Double> implements DoubleValue {

   public DoubleConstant (Namespace<? extends NamedEntity> namespace, String name, Double value) {
      super(namespace, name, value);
   }

   public DoubleConstant (Namespace<? extends NamedEntity> namespace) {
      super(namespace);
      setValue(0.0);
   }

   private static List<PropertyDescriptor<? extends LanguageEntity, ?>> entityProperties;

   @Override
   public List<PropertyDescriptor<? extends LanguageEntity, ?>> getEntityProperties () {
      if (entityProperties == null) {
         entityProperties = buildEntityProperties(DoubleConstant.class, Double.class);
      }
      return entityProperties;
   }

}
