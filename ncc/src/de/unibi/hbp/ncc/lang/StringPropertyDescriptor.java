package de.unibi.hbp.ncc.lang;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class StringPropertyDescriptor<E extends LanguageEntity> extends PropertyDescriptor<E, String> {
   StringPropertyDescriptor (Class<E> entityType, String propertyName,
                             BiConsumer<E, String> setter, Function<E, String> getter,
                             Function<String, String> simpleValidator,
                             BiFunction<E, String, String> contextDependentValidator) {
      super(entityType, propertyName, String.class, setter, getter, simpleValidator, contextDependentValidator);
   }

   StringPropertyDescriptor (Class<E> entityType, String propertyName,
                             BiConsumer<E, String> setter, Function<E, String> getter) {
      super(entityType, propertyName, String.class, setter, getter);
   }

   StringPropertyDescriptor (Class<E> entityType, String propertyName,
                             BiConsumer<E, String> setter, Function<E, String> getter,
                             Function<String, String> simpleValidator) {
      super(entityType, propertyName, String.class, setter, getter, simpleValidator);
   }

   StringPropertyDescriptor (Class<E> entityType, String propertyName,
                             BiConsumer<E, String> setter, Function<E, String> getter,
                             BiFunction<E, String, String> contextDependentValidator) {
      super(entityType, propertyName, String.class, setter, getter, contextDependentValidator);
   }

   StringPropertyDescriptor (Class<E> entityType, String propertyName, Function<E, String> getter) {
      super(entityType, propertyName, String.class, getter);
   }
}
