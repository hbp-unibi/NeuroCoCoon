package de.unibi.hbp.ncc.lang;

import com.mxgraph.model.mxICell;
import de.unibi.hbp.ncc.editor.EditorToolBar;
import de.unibi.hbp.ncc.editor.EntityConfigurator;
import de.unibi.hbp.ncc.editor.EntityCreator;
import de.unibi.hbp.ncc.lang.props.EditableNameProp;
import de.unibi.hbp.ncc.lang.props.EditableProp;
import de.unibi.hbp.ncc.lang.serialize.SerializedNeuronConnection;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class NeuronConnection extends AnyConnection implements Serializable {
   private Namespace<SynapseType> synapseTypeNamespace;
   private EditableNameProp<SynapseType> synapseType;

   private static Namespace<SynapseType> globalSynapseTypeNamespace;

   public static void setGlobalSynapseTypeNamespace (Namespace<SynapseType> ns) { globalSynapseTypeNamespace = ns; }

   protected Object writeReplace () throws ObjectStreamException {
      return new SerializedNeuronConnection(synapseType.getValue(), userLabel.getValue());
   }

   // readObject method for the serialization proxy pattern
   // See Effective Java, Second Ed., Item 78.
   private void readObject (java.io.ObjectInputStream stream) throws InvalidObjectException {
      throw new InvalidObjectException("SerializedNeuronConnection required");
   }

   @Override
   protected List<EditableProp<?>> addEditableProps (List<EditableProp<?>> list) {
      super.addEditableProps(list);
      list.add(synapseType);
      list.add(userLabel);  // defined in superclass, positioned by subclass
      return list;
   }

   @Override
   protected List<EditableProp<?>> addIndirectEditableProps (List<EditableProp<?>> list) {
      return synapseType.getValue().addExportedEditableProps(list);
   }

   public NeuronConnection (Namespace<SynapseType> synapseTypeNamespace, SynapseType synapseType, String userLabel) {
      super(userLabel);
      if (synapseTypeNamespace == null)
         synapseTypeNamespace = globalSynapseTypeNamespace;
      this.synapseTypeNamespace = synapseTypeNamespace;
      if (synapseType == null)
         synapseType = globalSynapseTypeNamespace.get("All Default");
      this.synapseType = new EditableNameProp<>("Synapse Type", SynapseType.class, this,
                                                Objects.requireNonNull(synapseType), synapseTypeNamespace)
            .addImpact(EditableProp.Impact.CELL_LABEL)
            .addImpact(EditableProp.Impact.CELL_STYLE)
            .addImpact(EditableProp.Impact.OTHER_PROPS_VISIBILITY);
   }

   public NeuronConnection (SynapseType synapseType) {
      this(null, synapseType, null);
   }

   public NeuronConnection () {
      this(null, null, null);
   }

   protected NeuronConnection (NeuronConnection orig) {
      this(orig.synapseTypeNamespace, orig.synapseType.getValue(), orig.userLabel.getValue());
   }

   @Override
   public String toString () {
      String label = getUserLabelOrNull();
      if (label != null)
         return label;
      SynapseType type = synapseType.getValue();
      return type.getDisplayName() + "\n(" + type.getSummary() + ")";
   }  // TODO append .getSummary() for synapse type as well?

   @Override
   public NeuronConnection duplicate () {
      return new NeuronConnection(this);
   }

   public SynapseType getSynapseType () { return synapseType.getValue(); }

   @Override
   public String getCellStyle () {
      return synapseType.getValue().getCellStyle();
   }

   public static final EntityCreator<NeuronConnection> CREATOR = new Creator();

   private static class Creator implements EntityCreator<NeuronConnection>, EntityConfigurator {
      @Override
      public NeuronConnection create () {
         return new NeuronConnection();
      }

      @Override
      public void configureEntityAndCell (LanguageEntity entity, mxICell owningCell, EditorToolBar globalState) {
         NeuronConnection connection = (NeuronConnection) entity;
         connection.synapseType.setValue(globalState.getCurrentSynapseType());
         owningCell.setStyle(connection.getCellStyle());
      }

      @Override
      public String toString () { return "Synapse Connection"; }  // used by drag&drop tooltips

      @Override
      public String getResourceFileBaseName () { return "synapse"; }

      @Override
      public String getIconCaption () { return "Synapse"; }

      // TODO can this be made to get the synapse type from the toolbar selection?
      @Override
      public String getCellStyle () { return "allToAll"; }

      @Override
      public int getInitialCellHeight () { return 60; }

      @Override
      public int getInitialCellWidth () { return 60; }
   }
}
