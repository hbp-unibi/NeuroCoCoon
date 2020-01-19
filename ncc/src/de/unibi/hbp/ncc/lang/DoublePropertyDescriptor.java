package de.unibi.hbp.ncc.lang;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DoublePropertyDescriptor<E extends LanguageEntity> extends PropertyDescriptor<E, Double> {
   DoublePropertyDescriptor (Class<E> entityType, String propertyName,
                             BiConsumer<E, Double> setter, Function<E, Double> getter,
                             Function<Double, String> simpleValidator,
                             BiFunction<E, Double, String> contextDependentValidator) {
      super(entityType, propertyName, Double.class, setter, getter, simpleValidator, contextDependentValidator);
   }

   DoublePropertyDescriptor (Class<E> entityType, String propertyName,
                             BiConsumer<E, Double> setter, Function<E, Double> getter) {
      super(entityType, propertyName, Double.class, setter, getter);
   }

   DoublePropertyDescriptor (Class<E> entityType, String propertyName,
                             BiConsumer<E, Double> setter, Function<E, Double> getter,
                             Function<Double, String> simpleValidator) {
      super(entityType, propertyName, Double.class, setter, getter, simpleValidator);
   }

   DoublePropertyDescriptor (Class<E> entityType, String propertyName,
                             BiConsumer<E, Double> setter, Function<E, Double> getter,
                             BiFunction<E, Double, String> contextDependentValidator) {
      super(entityType, propertyName, Double.class, setter, getter, contextDependentValidator);
   }

   DoublePropertyDescriptor (Class<E> entityType, String propertyName, Function<E, Double> getter) {
      super(entityType, propertyName, Double.class, getter);
   }
}
