package de.unibi.hbp.ncc.lang;

import java.util.List;

public class IntegerConstant extends Constant<Integer> implements IntegerValue {

   public IntegerConstant (Namespace<? extends NamedEntity> namespace, String name, Integer value) {
      super(namespace, name, value);
   }

   public IntegerConstant (Namespace<? extends NamedEntity> namespace) {
      super(namespace);
      setValue(0);
   }

   private static List<PropertyDescriptor<? extends LanguageEntity, ?>> entityProperties;

   @Override
   public List<PropertyDescriptor<? extends LanguageEntity, ?>> getEntityProperties () {
      if (entityProperties == null) {
         entityProperties = buildEntityProperties(IntegerConstant.class, Integer.class);
      }
      return entityProperties;
   }

}
