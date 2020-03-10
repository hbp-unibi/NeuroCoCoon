package de.unibi.hbp.ncc.lang.serialize;

import com.mxgraph.io.mxCodec;
import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxDomUtils;
import de.unibi.hbp.ncc.lang.LanguageEntity;
import de.unibi.hbp.ncc.lang.NamedEntity;
import de.unibi.hbp.ncc.lang.Namespace;
import de.unibi.hbp.ncc.lang.NetworkModule;
import de.unibi.hbp.ncc.lang.Program;
import de.unibi.hbp.ncc.lang.Scope;
import de.unibi.hbp.ncc.lang.props.NameProp;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ProgramCodec extends mxCodec {
   private final Program program;
   private final boolean willEncode;
   private Map<LanguageEntity, Integer> rememberedEntities;
   private Map<String, LanguageEntity> rememberedRefIds;

   public ProgramCodec (Program program, boolean willEncode) {
      this(program, mxDomUtils.createDocument(), willEncode);
   }

   private void rememberUserDefinedEntities (List<NamedEntity> list, Namespace<? extends NamedEntity> namespace) {
      for (NamedEntity entity: namespace)
         if (!entity.isPredefined())
            list.add(entity);
   }

   public ProgramCodec (Program program, Document document, boolean willEncode) {
      super(document);
      this.program = program;
      this.willEncode = willEncode;
      if (willEncode) {  // could check consistent usage encode vs. decode, but will crash anyway, if wrong map is used
         rememberedEntities = new HashMap<>();
         // attach a temporary list of unreferenced (from the graph) language entities that must be preserved
         List<NamedEntity> unvisualEntities = new ArrayList<>();
         rememberUserDefinedEntities(unvisualEntities, program.getGlobalScope().getNeuronTypes());
         rememberUserDefinedEntities(unvisualEntities, program.getGlobalScope().getSynapseTypes());
         mxICell root = (mxICell) program.getGraphModel().getRoot();
         assert root.getValue() == null : "root node should nt carry any value by default";
         root.setValue(unvisualEntities);
      }
      else
         rememberedRefIds = new HashMap<>();
   }

   private void rewriteEncodedPorts (NetworkModule enclosingModule, mxICell cell) {
      Object value = cell.getValue();
      if (value instanceof ModulePortCodec.EncodedPort) {
         ModulePortCodec.EncodedPort encodedPort = (ModulePortCodec.EncodedPort) value;
         NetworkModule.Port port = enclosingModule.adoptCellForPort(cell, encodedPort.isInput(), encodedPort.getName());
         // we would normally do the following to announce the change of label for the port cell:
         // program.getGraphComponent().labelChanged(cell, port, null);
         // but our EncodedPort happens to have the same toString()-based label representation as the real Port instance
         // so we cheat and do not force an update now
      }
      else if (value instanceof LanguageEntity) {
         ((LanguageEntity) value).setOwningCell(cell);
         if (value instanceof NetworkModule) {
            enclosingModule = (NetworkModule) value;
            enclosingModule.reviveAfterLoading(cell);
         }
      }
      int count = cell.getChildCount();
      for (int i = 0; i < count; i++)
         rewriteEncodedPorts(enclosingModule, cell.getChildAt(i));
   }

   // important to forget the temporarily assigned XML _refid attribute <--> LanguageEntity mapping
   public void announceDone () {
      mxICell root = (mxICell) program.getGraphModel().getRoot();
      if (willEncode)
         rememberedEntities = null;
      else {  // decoded from XML
         rememberedRefIds = null;
         rewriteEncodedPorts(null, root);
         // after loading the LanguageEntities need to be re-attached to their owning cells
         // this is handled by rewriteEncodedPorts() which needs to traverse the full graph model anyway
      }
      // remove the temporary list of unreferenced (from the graph) entities that must be preserved
      // on encode, the list is no longer needed obviously
      // on decode, reading the list elements had the important side effect of adding them to the matching global scope namespaces
      root.setValue(null);
   }

   private static final String REFERENCE_ID_PREFIX = "ncc_";

   String getExistingOrCreateFutureRefId (NameProp<?> refProp) {
      NamedEntity targetEntity = refProp.getTargetEntity();
      Integer id = rememberedEntities.putIfAbsent(targetEntity, rememberedEntities.size() + 1);
      if (id != null)
         return REFERENCE_ID_PREFIX + id;  // returns id only, if not just created
      else
         return null;
   }

   String getExistingRefId (LanguageEntity entity, boolean createIfMissing) {
      Integer id = rememberedEntities.get(entity);
      if (id == null && createIfMissing)
         rememberedEntities.put(entity, id = rememberedEntities.size() + 1);
      if (id != null)
         return REFERENCE_ID_PREFIX + id;
      else
         return null;
   }

   void rememberRefId (String ownRefId, LanguageEntity entity) {
      if (ownRefId != null)
         rememberedRefIds.put(ownRefId, entity);
   }

   LanguageEntity resolveRememberedRefId (String refId) {
      return Objects.requireNonNull(rememberedRefIds.get(refId));
   }

   Scope getGlobalScope () { return program.getGlobalScope(); }  // for resolving predefined entities

}
