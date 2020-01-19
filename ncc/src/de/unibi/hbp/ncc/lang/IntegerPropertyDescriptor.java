package de.unibi.hbp.ncc.lang;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class IntegerPropertyDescriptor<E extends LanguageEntity> extends PropertyDescriptor<E, Integer> {
   IntegerPropertyDescriptor (Class<E> entityType, String propertyName,
                              BiConsumer<E, Integer> setter, Function<E, Integer> getter,
                              Function<Integer, String> simpleValidator,
                              BiFunction<E, Integer, String> contextDependentValidator) {
      super(entityType, propertyName, Integer.class, setter, getter, simpleValidator, contextDependentValidator);
   }

   IntegerPropertyDescriptor (Class<E> entityType, String propertyName,
                              BiConsumer<E, Integer> setter, Function<E, Integer> getter) {
      super(entityType, propertyName, Integer.class, setter, getter);
   }

   IntegerPropertyDescriptor (Class<E> entityType, String propertyName,
                              BiConsumer<E, Integer> setter, Function<E, Integer> getter,
                              Function<Integer, String> simpleValidator) {
      super(entityType, propertyName, Integer.class, setter, getter, simpleValidator);
   }

   IntegerPropertyDescriptor (Class<E> entityType, String propertyName,
                              BiConsumer<E, Integer> setter, Function<E, Integer> getter,
                              BiFunction<E, Integer, String> contextDependentValidator) {
      super(entityType, propertyName, Integer.class, setter, getter, contextDependentValidator);
   }

   IntegerPropertyDescriptor (Class<E> entityType, String propertyName, Function<E, Integer> getter) {
      super(entityType, propertyName, Integer.class, getter);
   }
}
