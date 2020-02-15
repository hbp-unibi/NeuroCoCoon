package de.unibi.hbp.ncc.editor.props;

import de.unibi.hbp.ncc.lang.LanguageEntity;
import de.unibi.hbp.ncc.lang.props.EditableProp;
import de.unibi.hbp.ncc.lang.props.ReadOnlyProp;

public class VisualMarkers {

   private static final char MARKER_INDIRECT = '\u279d';
   private static final char PUNCTUATION_SPACE = '\u2008';
   private static final char MARKER_READONLY = '\u20e0';
   private static final char MARKER_PREDEFINED = '\u29bf';

   public static String getPropertyMarkers (ReadOnlyProp<?> prop, LanguageEntity directEntity) {
      boolean isReadOnly = !(prop instanceof EditableProp<?>);
      LanguageEntity enclosing = prop.getEnclosingEntity();
      boolean isIndirect = !directEntity.equals(enclosing);
      boolean isPredefined = enclosing.isPredefined();
      StringBuilder sb = new StringBuilder(2);
      if (isIndirect) {
         sb.append(MARKER_INDIRECT);
         if (isReadOnly || isPredefined)
            sb.append(PUNCTUATION_SPACE);
      }
      if (isReadOnly) sb.append(MARKER_READONLY);
      else if (isPredefined) sb.append(MARKER_PREDEFINED);
      return sb.toString();
   }

   public static String getEntityMarkers (LanguageEntity entity) {
      if (entity.isPredefined())
         return String.valueOf(MARKER_PREDEFINED);
      else
         return "";
   }
}