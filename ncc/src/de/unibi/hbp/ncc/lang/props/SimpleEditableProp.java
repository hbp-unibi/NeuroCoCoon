package de.unibi.hbp.ncc.lang.props;

import de.unibi.hbp.ncc.lang.LanguageEntity;

import java.util.EnumSet;
import java.util.Objects;

public abstract class SimpleEditableProp<T> implements EditableProp<T> {
   private String propName, unit;
   private Class<T> valueClass;
   private EnumSet<Impact> impact;
   private LanguageEntity owner;
   private T value;

   private final static EnumSet<Impact> DEFAULT_IMPACT = EnumSet.of(Impact.OWN_VALUE);

   public SimpleEditableProp (String propName, Class<T> valueClass, LanguageEntity owner, T value) {
      this.propName = Objects.requireNonNull(propName);
      this.valueClass = Objects.requireNonNull(valueClass);
      this.impact = DEFAULT_IMPACT;
      this.owner = Objects.requireNonNull(owner);
      setValue(value);
   }

   public SimpleEditableProp<T> setUnit (String unit) {
      assert this.unit == null : "may only be set once at creation time";
      this.unit = Objects.requireNonNull(unit);
      return this;
   }

   public SimpleEditableProp<T> setImpact (EnumSet<Impact> impact) {
      assert this.impact == null : "may only be set once at creation time";
      assert !impact.isEmpty() : "impact must not be empty";
      this.impact = Objects.requireNonNull(impact);
      return this;
   }

   @Override
   public boolean isValid (T proposedValue) {
      return proposedValue != null;
   }

   @Override
   public void setValue (T value) {
      if (!isValid(value))
         throw new IllegalArgumentException("property " + getPropName() + " at " + owner + ": invalid value " + value);
      this.value = value;
   }

   public String getPropName (boolean withUnit) {
      if (withUnit && unit != null && !unit.isEmpty())
         return propName + " (" + unit + ")";
      else
         return propName;
   }

   @Override
   public LanguageEntity getParentEntity () {
      return owner;
   }

   @Override
   public T getValue () {
      return value;
   }

   @Override
   public Class<T> getValueClass () { return valueClass; }

   @Override
   public EnumSet<Impact> getChangeImpact () { return impact; }

}
