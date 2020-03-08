package de.unibi.hbp.ncc.lang.serialize;

import com.mxgraph.io.mxCodec;
import com.mxgraph.io.mxObjectCodec;
import de.unibi.hbp.ncc.lang.LanguageEntity;
import de.unibi.hbp.ncc.lang.NamedEntity;
import de.unibi.hbp.ncc.lang.Scope;
import de.unibi.hbp.ncc.lang.props.EditableProp;
import de.unibi.hbp.ncc.lang.props.NameProp;
import org.w3c.dom.Node;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LanguageEntityCodec extends mxObjectCodec {
   private Scope globalScope;  // for resolving predefined entities
   private Map<LanguageEntity, Integer> rememberedEntities;
   private Map<String, LanguageEntity> rememberedRefIds;

   public LanguageEntityCodec (Scope global) {
      super(new LinkedHashMap<String, Object>());  // decoding starts with a clone of this empty order preserving hash map
      globalScope = global;
      rememberedEntities = new HashMap<>();  // FIXME these two fields really belong into a mxCodec subclass
      rememberedRefIds = new HashMap<>();  // this will break, if we ever create more than one Program instance (simultaneously)
      // this list must be kep in sync with the cases of the switch in afterDecode
      /*
      mxCodecRegistry.addAlias("RegularSpikeSource", myName);
      mxCodecRegistry.addAlias("PoissonSource", myName);
      mxCodecRegistry.addAlias("StandardPopulation", myName);
      mxCodecRegistry.addAlias("NeuronType", myName);
      mxCodecRegistry.addAlias("SynapseType", myName);
      mxCodecRegistry.addAlias("NeuronConnection", myName);
      mxCodecRegistry.addAlias("DataPlot", myName);
      mxCodecRegistry.addAlias("ProbeConnection", myName);

      mxCodecRegistry.addPackage("de.unibi.hbp.ncc.lang.modules");
      mxCodecRegistry.addAlias("SynfireChain", myName);
      mxCodecRegistry.addAlias("WinnerTakeAll", myName);
      */
   }

   public static String remapAllLanguageEntitySubclasses (Object instance) {
      return instance instanceof LanguageEntity ? "LanguageEntity" : null;
   }

   public void announceEncodeDecodeDone (boolean encodeDone) {
      if (encodeDone)
         rememberedEntities.clear();
      else
         rememberedRefIds.clear();
      // after loading the LanguageEntities need to be re-attached to their owning cells
      // this is handled by the ModulePortCodec() which needs to traverse the full graph model anyway
   }

   @Override
   public String getName () { return "ncc"; }

   private static final String REFERENCE_ID_PREFIX = "ncc_";

   private String getExistingOrCreateFutureRefId (NameProp<?> refProp) {
      NamedEntity targetEntity = refProp.getTargetEntity();
      Integer id = rememberedEntities.putIfAbsent(targetEntity, rememberedEntities.size() + 1);
      if (id != null)
         return REFERENCE_ID_PREFIX + id;
      else
         return null;
   }

   private String getExistingRefId (LanguageEntity entity) {
      Integer id = rememberedEntities.get(entity);
      if (id != null)
         return REFERENCE_ID_PREFIX + id;
      else
         return null;
   }

   private static final String CLASS_NAME_PSEUDO_PROPERTY_NAME = "_class";
   private static final String PREDEFINED_MARKER_PSEUDO_PROPERTY_NAME = "_predef";
   private static final String REFERENCE_ID_PSEUDO_PROPERTY_NAME = "_refid";

   @Override
   public Object beforeEncode (mxCodec enc, Object obj, Node node) {
      LanguageEntity entity = (LanguageEntity) obj;
      // what to about predefined entities? store them with a special pseudo attribute and remap them to the existing entity based on their name on decode
      Map<String, Object> propValues = new LinkedHashMap<>();  // preserve order of properties in XML
      propValues.put(CLASS_NAME_PSEUDO_PROPERTY_NAME, entity.getClass().getSimpleName());
      String ownRefId = getExistingRefId(entity);
      if (ownRefId != null)
         propValues.put(REFERENCE_ID_PSEUDO_PROPERTY_NAME, ownRefId);
      if (entity.isPredefined()) {
         assert entity instanceof NamedEntity : "predefined entities are resolved by name";
         propValues.put(NamedEntity.NAME_PROPERTY_NAME, ((NamedEntity) entity).getName());
         propValues.put(PREDEFINED_MARKER_PSEUDO_PROPERTY_NAME, "yes");  // only existence matters, values is don't care
         return propValues;  // further details in other properties would be ignored anyway on load
      }
      for (EditableProp<?> prop: entity.getEditableProps()) { // TODO any need to store (and reload) read-only properties?
         if (prop instanceof NameProp<?>) {
            String refId = getExistingOrCreateFutureRefId((NameProp<?>) prop);
            if (refId == null)
               propValues.put(prop.getPropName(), prop.getValue());
            else
               propValues.put(prop.getPropName(), refId);
         }
         else
            propValues.put(prop.getPropName(), prop.getValueEncodedAsString());
      }
      return propValues;
   }

   private Map<String, Constructor<?>> cachedConstructors;

   private Constructor<?> getConstructor (String simpleClassName, boolean haveEntityName) {
      if (cachedConstructors == null)
         cachedConstructors = new HashMap<>();
      Constructor<?> entityConstructor = cachedConstructors.get(simpleClassName);
      if (entityConstructor == null) {
         Class<?> entityClass;
         try {
            entityClass = Class.forName("de.unibi.hbp.ncc.lang." + simpleClassName);
         }
         catch (ClassNotFoundException cnf1) {
            try {
               entityClass = Class.forName("de.unibi.hbp.ncc.lang.modules." + simpleClassName);
            }
            catch (ClassNotFoundException cnf2) {
               throw new IllegalArgumentException("normal <ncc> with unsupported class " + simpleClassName);
            }
         }
         try {
            if (haveEntityName)
               entityConstructor = entityClass.getConstructor(String.class);
            else
               entityConstructor = entityClass.getConstructor();
         }
         catch (NoSuchMethodException nsm) {
            throw new IllegalArgumentException("class " + simpleClassName + " has no suitable constructor");
         }
         cachedConstructors.put(simpleClassName, entityConstructor);
      }
      return entityConstructor;
   }

   @Override
   public Object afterDecode (mxCodec dec, Node node, Object obj) {
      @SuppressWarnings("unchecked")
      Map<String, Object> propValues = (Map<String, Object>) obj;
      // System.err.println("afterDecode: propValues = " + propValues);
      String entityName = (String) propValues.get(NamedEntity.NAME_PROPERTY_NAME);
      String entityClassName = (String) propValues.get(CLASS_NAME_PSEUDO_PROPERTY_NAME);
      LanguageEntity entity;
      boolean isPredefined = propValues.containsKey(PREDEFINED_MARKER_PSEUDO_PROPERTY_NAME);
      if (isPredefined) {
         // if we get (many) more classes with predefined entities, we could try to generalize this via reflection (class method?) somehow
         switch (entityClassName) {
            case "NeuronType":
               entity = globalScope.getNeuronTypes().get(entityName);
               break;
            case "SynapseType":
               entity = globalScope.getSynapseTypes().get(entityName);
               break;
            default:
               throw new IllegalArgumentException("predefined <ncc> with unsupported class " + entityClassName);
         }
      }
      else {
         Constructor<?> entityConstructor = getConstructor(entityClassName, entityName != null);
         try {
            if (entityName != null)
               entity = (NamedEntity) entityConstructor.newInstance(entityName);
            else {
               entity = (LanguageEntity) entityConstructor.newInstance();
               if (entity instanceof NamedEntity)
                  throw new IllegalStateException("no encoded name for named entity " + entityClassName);
            }
         }
         catch (ReflectiveOperationException roe) {
            throw new RuntimeException("failed to invoke constructor for " + entityClassName, roe);
         }
      }
      String ownRefId = (String) propValues.get(REFERENCE_ID_PSEUDO_PROPERTY_NAME);
      if (ownRefId != null)
         rememberedRefIds.put(ownRefId, entity);
      if (isPredefined)
         return entity;
      for (EditableProp<?> prop: entity.getEditableProps()) {
         String propName = prop.getPropName();
         // System.err.println("editable property: " + propName + " = " + propValues.get(propName));
         if (prop instanceof NameProp<?>) {
            Object propValue = propValues.get(propName);
            if (propValue instanceof String)
               prop.setRawValue(rememberedRefIds.get(propValue));
            else
               prop.setRawValue(propValue);
               // System.err.println("got prop value from XML: " + propName + "=" + propValue);
         }
         else {
            String encodedValue;
            // formally, Name needs to only be skipped, if (entity instanceof NamedEntity), but we never will use Name for anything else
            if (!NamedEntity.NAME_PROPERTY_NAME.equals(propName) && (encodedValue = (String) propValues.get(propName)) != null)
               prop.setValueFromString(encodedValue);
         }
      }
      // System.err.println("afterDecode: " + node + ", " + obj + ", " + entityName);
      return entity;
   }
}
