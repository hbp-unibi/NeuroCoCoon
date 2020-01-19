package de.unibi.hbp.ncc.lang;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class PropertyDescriptor<E, V> {
   private final String propertyName;
   private final Class<V> valueType;
   private final BiConsumer<E, V> setter;  // may be null for read-only properties
   private final Function<E, V> getter;
   private final Function<V, String> simpleValidator;
   private final BiFunction<E, V, String> contextDependentValidator;
   private Collection<V> enumerationValues;  // for types with a finite set of values
   private Supplier<Collection<V>> simpleEnumerator;
   private Function<E, Collection<V>> contextDependentEnumerator;
   private Predicate<E> editablePredicate;

   PropertyDescriptor (String propertyName, Class<V> valueType,
                       BiConsumer<E, V> setter, Function<E, V> getter,
                       Function<V, String> simpleValidator, BiFunction<E, V, String> contextDependentValidator) {
      this.propertyName = propertyName;
      this.valueType = valueType;
      this.setter = setter;
      this.getter = Objects.requireNonNull(getter);
      this.simpleValidator = simpleValidator;
      this.contextDependentValidator = contextDependentValidator;
   }

   PropertyDescriptor (String propertyName, Class<V> valueType,
                       BiConsumer<E, V> setter, Function<E, V> getter) {  // no validation (beyond enumeration values)
      this(propertyName, valueType, setter, getter, null, null);
   }

   PropertyDescriptor (String propertyName, Class<V> valueType,
                       BiConsumer<E, V> setter, Function<E, V> getter,
                       Function<V, String> simpleValidator) {
      this(propertyName, valueType, setter, getter, simpleValidator, null);
   }

   PropertyDescriptor (String propertyName, Class<V> valueType,
                       BiConsumer<E, V> setter, Function<E, V> getter,
                       BiFunction<E, V, String> contextDependentValidator) {
      this(propertyName, valueType, setter, getter, null, contextDependentValidator);
   }

   PropertyDescriptor (String propertyName, Class<V> valueType, Function<E, V> getter) {
      this(propertyName, valueType, null, getter, null, null);
   }

   PropertyDescriptor<E, V> addEnumerationValues (Collection<V> enumerationValues) {
      assert this.enumerationValues == null && this.simpleEnumerator == null && this.contextDependentEnumerator == null : "set up only one enumerator once";
      this.enumerationValues = enumerationValues;
      return this;
   }

   @SafeVarargs
   final PropertyDescriptor<E, V> addEnumerationValues (V... enumerationValues) {
      assert this.enumerationValues == null && this.simpleEnumerator == null && this.contextDependentEnumerator == null : "set up only one enumerator once";
      this.enumerationValues = Arrays.asList(enumerationValues);
      return this;
   }

   PropertyDescriptor<E, V> addValueEnumerator (Supplier<Collection<V>> simpleEnumerator) {
      assert this.enumerationValues == null && this.simpleEnumerator == null && this.contextDependentEnumerator == null : "set up only one enumerator once";
      this.simpleEnumerator = simpleEnumerator;
      return this;
   }

   PropertyDescriptor<E, V> addValueEnumerator (Function<E, Collection<V>> contextDependentEnumerator) {
      assert this.enumerationValues == null && this.simpleEnumerator == null && this.contextDependentEnumerator == null : "set up only one enumerator once";
      this.contextDependentEnumerator = contextDependentEnumerator;
      return this;
   }

   PropertyDescriptor<E, V> addEditablePredicate (Predicate<E> editablePredicate) {
      assert this.editablePredicate == null : "set up an editable predicate only once";
      this.editablePredicate = editablePredicate;
      return this;
   }

   private Collection<V> getEnumerationValuesOrNull (E entity) {
      if (contextDependentEnumerator != null)
         return contextDependentEnumerator.apply(entity);
      else if (simpleEnumerator != null)
         return simpleEnumerator.get();
      else if (enumerationValues != null)
         return enumerationValues;
      else
         return null;
   }

   public Collection<V> getEnumerationValues (E entity) {
      Collection<V> validValues = getEnumerationValuesOrNull(entity);
      if (validValues != null)
         return validValues;
      else
         return Collections.emptyList();
   }

   public String getPropertyName () {
      return propertyName;
   }

   public boolean isReadOnly () {
      return setter == null;
   }

   public String validate (E entity, V value) {
      String errorMessage;
      if (simpleValidator != null && (errorMessage = simpleValidator.apply(value)) != null)
         return errorMessage;
      if (contextDependentValidator != null && (errorMessage = contextDependentValidator.apply(entity, value)) != null)
         return errorMessage;
      Collection<V> validValues = getEnumerationValuesOrNull(entity);
      if (validValues != null && !validValues.contains(value))
         return "List of valid values does not include " + value;
      return null;
   }

   public boolean isValid (E entity, V value) {
      return validate(entity, value) == null;
   }

   public boolean isEditable (E entity) {
      return !isReadOnly() && (editablePredicate == null || editablePredicate.test(entity));
   }

   public void setValue (E entity, V value) {
      if (isReadOnly())
         throw new LanguageException("property " + propertyName + " is read-only");
      if (!isEditable(entity))
         throw new LanguageException("property " + propertyName + " is not editable for " + entity);
      if (isValid(entity, value))
         setter.accept(entity, value);
      else
         throw new LanguageException("value " + value + " is invalid for property " + propertyName);
   }

   public V getValue (E entity) {
      return getter.apply(entity);
   }

}
