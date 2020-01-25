package de.unibi.hbp.ncc.lang;

public interface Value<T> extends ReadOnlyValue<T> {

   void setValue (T value);
}
