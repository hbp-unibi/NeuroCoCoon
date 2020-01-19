package de.unibi.hbp.ncc.lang;

import java.util.ArrayList;
import java.util.List;

public class NeuronType extends NamedEntity<NeuronType> {

   public NeuronType (Namespace<NeuronType> namespace, String name) {
      super(namespace, name);
   }

   protected final static List<PropertyDescriptor<? extends LanguageEntity, ?>> entityProperties =
         addEntityProperties(new ArrayList<>(NamedEntity.entityProperties));

   protected static List<PropertyDescriptor<? extends LanguageEntity, ?>> addEntityProperties (List<PropertyDescriptor<? extends LanguageEntity, ?>> superProps) {
      return superProps;
   }

   @Override
   public List<PropertyDescriptor<? extends LanguageEntity, ?>> getEntityProperties () {
      return entityProperties;
   }
}
