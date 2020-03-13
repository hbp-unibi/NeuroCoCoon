package de.unibi.hbp.ncc.lang.serialize;

import com.mxgraph.io.mxCodec;
import com.mxgraph.io.mxObjectCodec;
import de.unibi.hbp.ncc.lang.LanguageEntity;
import de.unibi.hbp.ncc.lang.NamedEntity;
import de.unibi.hbp.ncc.lang.props.EditableProp;
import de.unibi.hbp.ncc.lang.props.NameProp;
import org.w3c.dom.Node;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LanguageEntityCodec extends mxObjectCodec {

   public LanguageEntityCodec () {
      super(new LinkedHashMap<String, Object>());  // decoding starts with a clone of this empty order preserving hash map
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

   @Override
   public String getName () { return "ncc"; }

   private static final String CLASS_NAME_PSEUDO_PROPERTY_NAME = "_class";
   private static final String PREDEFINED_MARKER_PSEUDO_PROPERTY_NAME = "_predef";
   private static final String REFERENCE_ID_PSEUDO_PROPERTY_NAME = "_refid";

   @Override
   public Object beforeEncode (mxCodec enc, Object obj, Node node) {
      ProgramCodec progEncoder = (ProgramCodec) enc;
      LanguageEntity entity = (LanguageEntity) obj;
      // what to about predefined entities? store them with a special pseudo attribute and remap them to the existing entity based on their name on decode
      Map<String, Object> propValues = new LinkedHashMap<>();  // preserve order of properties in XML
      propValues.put(CLASS_NAME_PSEUDO_PROPERTY_NAME, entity.getClass().getSimpleName());
      String ownRefId = progEncoder.getExistingRefId(entity,
                                                     entity.getOwningCell() == null &&
                                                           entity instanceof NamedEntity);
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
            String refId = progEncoder.getExistingOrCreateFutureRefId((NameProp<?>) prop);
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

   private void processProps (ProgramCodec progDecoder, Iterable<EditableProp<?>> props,
                              Map<String, Object> propValues) {
      for (EditableProp<?> prop: props) {
         String propName = prop.getPropName();
         // System.err.println("editable property: " + propName + " = " + propValues.get(propName));
         if (prop instanceof NameProp<?>) {
            Object propValue = propValues.get(propName);
            // System.err.println("afterDecode: NameProp " + prop.getPropName() + " = " + propValue + ", class=" + propValue.getClass().getName());
            if (propValue instanceof String)
               prop.setRawValue(progDecoder.resolveRememberedRefId((String) propValue));
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
   }

   @Override
   public Object afterDecode (mxCodec dec, Node node, Object obj) {
      ProgramCodec progDecoder = (ProgramCodec) dec;
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
               entity = progDecoder.getGlobalScope().getNeuronTypes().get(entityName);
               break;
            case "SynapseType":
               entity = progDecoder.getGlobalScope().getSynapseTypes().get(entityName);
               break;
            default:
               throw new IllegalArgumentException("predefined <ncc> with unsupported class " + entityClassName);
         }
      }
      else if ("Program".equals(entityClassName)) {
         entity = progDecoder.getProgram();
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
      progDecoder.rememberRefId(ownRefId, entity);
      if (!isPredefined) {
         processProps(progDecoder, entity.getInfluentialEditableProps(), propValues);
         processProps(progDecoder, entity.getNonInfluentialEditableProps(), propValues);
         // assumes that there are no multi-level dependencies, where influential props are only exposed by other influential props
      }
      return entity;
   }
}
