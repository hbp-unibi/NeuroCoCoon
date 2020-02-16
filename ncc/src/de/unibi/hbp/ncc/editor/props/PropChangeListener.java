package de.unibi.hbp.ncc.editor.props;

import de.unibi.hbp.ncc.lang.LanguageEntity;
import de.unibi.hbp.ncc.lang.props.EditableProp;

public interface PropChangeListener {

   int UNKNOWN_POSITION = -1;

   void propertyChanged (EditableProp<?> changed, int position);

   void multiplePropertyValuesChanged (LanguageEntity affected);
   void otherPropertiesVisibilityChanged (LanguageEntity affected);  // could probably default to multiplePropertyValuesChanged

   // void externalPropertyValuesChanged ();  // TODO do we need this?
   // void externalPropertiesVisibilityChanged ();  // TODO do we need this?
}
