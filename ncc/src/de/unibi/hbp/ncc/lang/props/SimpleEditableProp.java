package de.unibi.hbp.ncc.lang.props;

import de.unibi.hbp.ncc.lang.LanguageEntity;

import java.util.EnumSet;
import java.util.Objects;

public abstract class SimpleEditableProp<T> implements EditableProp<T> {
   private String propName, unit;
   private Class<T> valueClass;
   private EnumSet<Impact> impactSet;
   private LanguageEntity owner;
   private T value;

   private final static EnumSet<Impact> DEFAULT_IMPACT = EnumSet.of(Impact.OWN_VALUE);

   public SimpleEditableProp (String propName, Class<T> valueClass, LanguageEntity owner, T value) {
      this.propName = Objects.requireNonNull(propName);
      this.valueClass = Objects.requireNonNull(valueClass);
      // this.impact = DEFAULT_IMPACT;  // null means may still be set
      this.owner = Objects.requireNonNull(owner);
      setValueInternal(value);
   }

   public SimpleEditableProp<T> setUnit (String unit) {
      assert this.unit == null : "may only be set once at creation time";
      this.unit = Objects.requireNonNull(unit);
      return this;
   }

   // TODO provide an addImpact method instead (e.g. SynapseType needs to augment the impact of its name property)

   public SimpleEditableProp<T> addImpact (Impact impact) {
      assert impact != Impact.OWN_VALUE : "impact on own value is assumed by default anyway";
      if (impactSet == null)
         impactSet = EnumSet.copyOf(DEFAULT_IMPACT);
      impactSet.add(impact);
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

   public final void setValueInternal (T value) {
      // should never be overridden
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
   public LanguageEntity getEnclosingEntity () {
      return owner;
   }

   @Override
   public T getValue () {
      return value;
   }

   @Override
   public Class<T> getValueClass () { return valueClass; }

   @Override
   public boolean hasChangeImpact (Impact impact) {
      return (impactSet != null ? impactSet : DEFAULT_IMPACT).contains(impact);
   }
}
