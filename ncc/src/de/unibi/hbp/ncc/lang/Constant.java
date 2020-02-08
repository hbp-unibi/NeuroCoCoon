package de.unibi.hbp.ncc.lang;

import java.util.ArrayList;
import java.util.List;

public abstract class Constant<T extends Number> extends NamedEntity<Constant<Number>> implements Value<T> {

   private T value;

   public Constant (Namespace<Constant<Number>> namespace, String name, T value) {
      super(namespace, name);
      setValue(value);
   }

   public Constant (Namespace<Constant<Number>> namespace) {
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
}
