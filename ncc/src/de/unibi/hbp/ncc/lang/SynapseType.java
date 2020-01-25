package de.unibi.hbp.ncc.lang;

import java.util.ArrayList;
import java.util.List;

public class SynapseType extends NamedEntity {

   public SynapseType (Namespace<SynapseType> namespace, String name) {
      super(namespace, name);
   }

   private static List<PropertyDescriptor<? extends LanguageEntity, ?>> entityProperties;

   @Override
   public List<PropertyDescriptor<? extends LanguageEntity, ?>> getEntityProperties () {
      if (entityProperties == null) {
         List<PropertyDescriptor<? extends LanguageEntity, ?>> list =
               new ArrayList<>(super.getEntityProperties());
         entityProperties = list;
      }
      return entityProperties;
   }
}
