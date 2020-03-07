package de.unibi.hbp.ncc.lang.serialize;

import com.mxgraph.io.mxCodec;
import com.mxgraph.io.mxCodecRegistry;
import com.mxgraph.io.mxObjectCodec;
import de.unibi.hbp.ncc.lang.DataPlot;
import de.unibi.hbp.ncc.lang.LanguageEntity;
import de.unibi.hbp.ncc.lang.NamedEntity;
import de.unibi.hbp.ncc.lang.NeuronConnection;
import de.unibi.hbp.ncc.lang.NeuronType;
import de.unibi.hbp.ncc.lang.PoissonSource;
import de.unibi.hbp.ncc.lang.ProbeConnection;
import de.unibi.hbp.ncc.lang.RegularSpikeSource;
import de.unibi.hbp.ncc.lang.Scope;
import de.unibi.hbp.ncc.lang.StandardPopulation;
import de.unibi.hbp.ncc.lang.SynapseType;
import de.unibi.hbp.ncc.lang.modules.SynfireChain;
import de.unibi.hbp.ncc.lang.modules.WinnerTakeAll;
import de.unibi.hbp.ncc.lang.props.EditableProp;
import de.unibi.hbp.ncc.lang.props.NameProp;
import org.w3c.dom.Node;

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
      rememberedEntities = new HashMap<>();
      rememberedRefIds = new HashMap<>();
      String myName = getName();
      mxCodecRegistry.addPackage("de.unibi.hbp.ncc.lang");
      mxCodecRegistry.setClassNameRemapper(LanguageEntityCodec::remapAllLanguageEntitySubclasses);
      mxCodecRegistry.addAlias("LanguageEntity", myName);
      mxCodecRegistry.addPackage("de.unibi.hbp.ncc.lang.modules");
      // this list must be kep in sync with that cases of the switch in afterDecode
      // TODO use reflection to gather all concrete subclasses of LanguageEntity?
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

   private static String remapAllLanguageEntitySubclasses (Object instance) {
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
         // TODO maybe use reflection to call the (String name) constructor or the parameter-less constructor, if entityName is null
         switch (entityClassName) {
            case "RegularSpikeSource":
               entity = new RegularSpikeSource(entityName);
               break;
            case "PoissonSource":
               entity = new PoissonSource(entityName);
               break;
            case "StandardPopulation":
               entity = new StandardPopulation(entityName);
               break;
            case "NeuronType":
               entity = new NeuronType(entityName);
               break;
            case "SynapseType":
               entity = new SynapseType(entityName);
               break;
            case "NeuronConnection":
               assert entityName == null;
               entity = new NeuronConnection();
               break;
            case "DataPlot":
               entity = new DataPlot(entityName);
               break;
            case "ProbeConnection":
               assert entityName == null;
               entity = new ProbeConnection();
               break;
            case "SynfireChain":
               entity = new SynfireChain(entityName);
               break;
            case "WinnerTakeAll":
               entity = new WinnerTakeAll(entityName);
               break;
            default:
               throw new IllegalArgumentException("normal <ncc> with unsupported class " + entityClassName);
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
