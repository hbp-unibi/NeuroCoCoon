package de.unibi.hbp.ncc.lang;

public class ValueRef<T extends Number> implements ReadOnlyValue<T> {
   private Literal<T> literal;
   private Constant<T> constant;

   public ValueRef (Constant<T> constant) {
      this.constant = constant;
   }

   public ValueRef (Literal<T> literal) {
      this.literal = literal;
   }

   @Override
   public T getValue () {
      return literal != null ? literal.getValue() : constant.getValue();
   }

   public String getPythonRepresentation () {
      if (constant != null)
         return constant.getPythonName();
      else
         return literal.getPythonRepresentation();
   }

}
