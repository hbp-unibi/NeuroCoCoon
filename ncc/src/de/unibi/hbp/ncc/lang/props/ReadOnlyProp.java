package de.unibi.hbp.ncc.lang.props;

import de.unibi.hbp.ncc.lang.LanguageEntity;

public interface ReadOnlyProp<T> {
   String getPropName (boolean withUnit);
   default String getPropName () { return getPropName(false); }
   default String getLongPropName () { return getPropName(); }
   default String getShortPropName () { return getPropName(); }
   default String getUnit () { return null; }
   LanguageEntity getEnclosingEntity ();
   T getValue ();
   Class<T> getValueClass ();
}
