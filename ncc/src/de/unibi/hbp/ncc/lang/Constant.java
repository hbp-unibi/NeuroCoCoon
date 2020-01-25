package de.unibi.hbp.ncc.lang;

import java.util.ArrayList;
import java.util.List;

public abstract class Constant<T> extends NamedEntity implements Value<T> {

   private T value;

   public Constant (Namespace<? extends NamedEntity> namespace, String name, T value) {
      super(namespace, name);
      setValue(value);
   }

   public Constant (Namespace<? extends NamedEntity> namespace) {
      super(namespace);
   }

   @Override
   public void setValue (T value) {
      this.value = value;
   }

   @Override
   public T getValue () {
      return value;
   }

   protected List<PropertyDescriptor<? extends LanguageEntity, ?>> buildEntityProperties (
         Class<? extends Constant<T>> entityType, Class<T> valueType) {
      PropertyDescriptor<? extends Constant<T>, T> valueProperty =
            new PropertyDescriptor<>(entityType, "Value", valueType,
                                     Constant<T>::setValue, Constant<T>::getValue);
      List<PropertyDescriptor<? extends LanguageEntity, ?>> list = new ArrayList<>(super.getEntityProperties());
      list.add(valueProperty);
      return list;
   }

}
