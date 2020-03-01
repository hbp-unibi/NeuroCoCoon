package de.unibi.hbp.ncc.lang.props;

import de.unibi.hbp.ncc.lang.DisplayNamed;
import de.unibi.hbp.ncc.lang.LanguageEntity;
import de.unibi.hbp.ncc.lang.Namespace;
import de.unibi.hbp.ncc.lang.PythonNamed;

public interface ReadOnlyProp<T> extends DisplayNamed, PythonNamed {
   String getPropName (boolean withUnit);
   default String getPropName () { return getPropName(false); }
   default String getLongPropName () { return getPropName(); }
   default String getShortPropName () { return getPropName(); }
   default String getUnit () { return null; }
   LanguageEntity getEnclosingEntity ();
   T getValue ();

   default String getValueEncodedAsString () { return getValue().toString(); }
   Class<T> getValueClass ();

   @Override
   default String getDisplayName () { return getPropName(); }

   @Override
   default String getPythonName () { return Namespace.buildUnadornedPythonName(getPropName()); }

   @Override
   default String getLongDisplayName () { return getLongPropName(); }

   @Override
   default String getShortDisplayName () { return getShortPropName(); }

   @Override
   default String getTooltip () {
      return "Property " + getPropName(true);
   }
}
