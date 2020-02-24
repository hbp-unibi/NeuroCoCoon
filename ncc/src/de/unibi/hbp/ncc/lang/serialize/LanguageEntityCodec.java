package de.unibi.hbp.ncc.lang.serialize;

import com.mxgraph.io.mxCodec;
import com.mxgraph.io.mxCodecRegistry;
import com.mxgraph.io.mxObjectCodec;
import de.unibi.hbp.ncc.lang.LanguageEntity;
import de.unibi.hbp.ncc.lang.RegularSpikeSource;
import de.unibi.hbp.ncc.lang.props.EditableProp;
import org.w3c.dom.Node;

import java.util.LinkedHashMap;
import java.util.Map;

public class LanguageEntityCodec extends mxObjectCodec {

   public LanguageEntityCodec () {
      super(new LinkedHashMap<String, Object>());  // decoding starts with a clone of this empty order preserving hash map

      String myName = getName();
      mxCodecRegistry.addPackage("de.unibi.hbp.ncc.lang");
      // this list must be kep in sync with tht cases of the switch in afterDecode
      mxCodecRegistry.addAlias("RegularSpikeSource", myName);
   }

   @Override
   public String getName () {
      return "nccle";
   }

   @Override
   public Object beforeEncode (mxCodec enc, Object obj, Node node) {
      LanguageEntity entity = (LanguageEntity) obj;
      if (entity.isPredefined())
         return null;  // skip predefined entities
      Map<String, String> propValues = new LinkedHashMap<>();  // preserve order of properties in XML
      propValues.put("class", entity.getClass().getSimpleName());
      for (EditableProp<?> prop: entity.getEditableProps())  // TODO any need to store (and reload) read-only properties?
         propValues.put(prop.getPropName(), prop.getValueEncodedAsString());
      // TODO what to do about references (NameProps)?
      return propValues;
   }

   @Override
   public Object afterDecode (mxCodec dec, Node node, Object obj) {
      @SuppressWarnings("unchecked")
      Map<String, String> propValues = (Map<String, String>) obj;
      // System.err.println("afterDecode: propValues = " + propValues);
      String entityName = propValues.get("Name");
      LanguageEntity entity;
      switch (propValues.get("class")) {
         case "RegularSpikeSource":
            entity = new RegularSpikeSource(entityName);
            break;
         default:
            throw new IllegalArgumentException("<nccle> with unsupported class " + propValues.get("class"));
      }
      for (EditableProp<?> prop: entity.getEditableProps()) {
         String propName = prop.getPropName();
         // System.err.println("editable property: " + propName + " = " + propValues.get(propName));
         String encodedValue;
         // formally, Name needs to only be skipped, if (entity instanceof NamedEntity), but we never will use Name for anything else
         if (!"Name".equals(propName) && (encodedValue = propValues.get(propName)) != null)
            prop.setValueFromString(encodedValue);
      }
      // System.err.println("afterDecode: " + node + ", " + obj + ", " + entityName);
      return entity;
   }
}
