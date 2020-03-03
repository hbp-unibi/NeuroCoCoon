package de.unibi.hbp.ncc.lang;

import com.mxgraph.model.mxICell;
import de.unibi.hbp.ncc.editor.EditorToolBar;
import de.unibi.hbp.ncc.editor.EntityCreator;
import de.unibi.hbp.ncc.lang.props.EditableNameProp;
import de.unibi.hbp.ncc.lang.props.EditableProp;
import de.unibi.hbp.ncc.lang.serialize.SerializedNeuronConnection;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class NeuronConnection extends LanguageEntity implements Serializable {
   private Namespace<SynapseType> synapseTypeNamespace;
   private EditableNameProp<SynapseType> synapseType;

   private static Namespace<SynapseType> globalSynapseTypeNamespace;

   public static void setGlobalSynapseTypeNamespace (Namespace<SynapseType> ns) { globalSynapseTypeNamespace = ns; }

   protected Object writeReplace () throws ObjectStreamException {
      return new SerializedNeuronConnection(synapseType.getValue());
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
      return list;
   }

   @Override
   protected List<EditableProp<?>> addIndirectEditableProps (List<EditableProp<?>> list) {
      return synapseType.getValue().addExportedEditableProps(list);
   }

   public NeuronConnection (Namespace<SynapseType> synapseTypeNamespace, SynapseType synapseType) {
      super();
      if (synapseTypeNamespace == null)
         synapseTypeNamespace = globalSynapseTypeNamespace;
      this.synapseTypeNamespace = synapseTypeNamespace;
      if (synapseType == null)
         synapseType = globalSynapseTypeNamespace.get("All Default");
      this.synapseType = new EditableNameProp<>("Synapse Type", SynapseType.class, this,
                                                Objects.requireNonNull(synapseType), synapseTypeNamespace)
            .addImpact(EditableProp.Impact.CELL_LABEL);
   }

   public NeuronConnection (SynapseType synapseType) {
      this(null, synapseType);
   }

   public NeuronConnection () {
      this(null, null);
   }

   protected NeuronConnection (NeuronConnection orig) {
      this(orig.synapseTypeNamespace, orig.synapseType.getValue());
   }

   @Override
   public String toString () {
      SynapseType type = synapseType.getValue();
      return type.getDisplayName() + "\n(" + type.getSummary() + ")";
   }  // TODO append .getSummary() for synapse type as well?

   // @Override
   public NeuronConnection duplicate () {
      return new NeuronConnection(this);
   }

   public SynapseType getSynapseType () { return synapseType.getValue(); }

   private Object getTerminalValue (boolean source) {
      mxICell edgeCell = getOwningCell();
      if (edgeCell != null) {
         mxICell terminalCell = edgeCell.getTerminal(source);
         if (terminalCell != null)
            return terminalCell.getValue();
      }
      return null;
   }

   public LanguageEntity getSourceEntity () {
      Object sourceValue = getTerminalValue(true);
      return (sourceValue instanceof LanguageEntity) ? (LanguageEntity) sourceValue : null;
   }

   public LanguageEntity getTargetEntity () {
      Object targetValue = getTerminalValue(false);
      return (targetValue instanceof LanguageEntity) ? (LanguageEntity) targetValue : null;
   }

   public NamedEntity getSourceNamedEntity () {
      Object sourceValue = getTerminalValue(true);
      return (sourceValue instanceof NamedEntity) ? (NamedEntity) sourceValue : null;
   }

   public NamedEntity getTargetNamedEntity () {
      Object targetValue = getTerminalValue(false);
      return (targetValue instanceof NamedEntity) ? (NamedEntity) targetValue : null;
   }

   public NeuronPopulation getSourcePopulation () {
      Object sourceValue = getTerminalValue(true);
      return (sourceValue instanceof NeuronPopulation) ? (NeuronPopulation) sourceValue : null;
   }

   public NeuronPopulation getTargetPopulation () {
      Object targetValue = getTerminalValue(false);
      return (targetValue instanceof NeuronPopulation) ? (NeuronPopulation) targetValue : null;
   }

   public NetworkModule.Port getSourcePort () {
      Object sourceValue = getTerminalValue(true);
      return (sourceValue instanceof NetworkModule.Port) ? (NetworkModule.Port) sourceValue : null;
   }

   public NetworkModule.Port getTargetPort () {
      Object targetValue = getTerminalValue(false);
      return (targetValue instanceof NetworkModule.Port) ? (NetworkModule.Port) targetValue : null;
   }

   @Override
   public String getCellStyle () {
      return synapseType.getValue().getCellStyle();
   }

   public static final EntityCreator<NeuronConnection> CREATOR = new Creator();

   private static class Creator implements EntityCreator<NeuronConnection> {
      @Override
      public NeuronConnection create () {
         return new NeuronConnection();
      }

      @Override
      public String toString () {  // used by drag&drop tooltips
         return "All:All Connection";
      }

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
