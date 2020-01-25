package de.unibi.hbp.ncc.lang;

import java.util.ArrayList;
import java.util.List;

public abstract class Literal<T> extends LanguageEntity implements ReadOnlyValue<T> {

   private final T value;

   public Literal (T value) {
      this.value = value;
   }

   @Override
   public T getValue () {
      return value;
   }

   protected List<PropertyDescriptor<? extends LanguageEntity, ?>> buildEntityProperties (
         Class<? extends Literal<T>> entityType, Class<T> valueType) {
      PropertyDescriptor<? extends Literal<T>, T> valueProperty =
            new PropertyDescriptor<>(entityType, "Value", valueType, Literal<T>::getValue);
      List<PropertyDescriptor<? extends LanguageEntity, ?>> list = new ArrayList<>();
      list.add(valueProperty);
      return list;
   }

   public String getPythonRepresentation () {
      return value.toString();  // works for numbers, but would need to be overridden to e.g. quote and escape strings
   }

}
