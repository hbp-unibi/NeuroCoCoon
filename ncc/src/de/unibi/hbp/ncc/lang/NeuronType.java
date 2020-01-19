package de.unibi.hbp.ncc.lang;

import java.util.ArrayList;
import java.util.List;

public class NeuronType extends NamedEntity {

   public NeuronType (Namespace<NeuronType> namespace, String name) {
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
