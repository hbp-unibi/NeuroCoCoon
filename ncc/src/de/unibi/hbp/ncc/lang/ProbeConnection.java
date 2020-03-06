package de.unibi.hbp.ncc.lang;

import de.unibi.hbp.ncc.editor.EntityCreator;
import de.unibi.hbp.ncc.lang.props.EditableEnumProp;
import de.unibi.hbp.ncc.lang.props.EditableProp;
import de.unibi.hbp.ncc.lang.serialize.SerializedProbeConnection;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.util.List;

public class ProbeConnection extends AnyConnection {
   private EditableEnumProp<DataSeries> dataSeries;

   public enum DataSeries implements DisplayNamed, PythonNamed {
      SPIKES("Spikes", "spikes"),
      VOLTAGE("Membrane Voltage", "v"),
      GSYN_EXC("gsyn exc", "gsyn_exc"),
      GSYN_INH("gsyn inh", "gsyn_inh"),
      IZHIKEVICH_U("Izhikevich u", "u");

      private String pythonName, displayName;

      DataSeries (String displayName, String pythonName) {
         this.displayName = displayName;
         this.pythonName = pythonName;
      }

      @Override
      public String getDisplayName () { return displayName; }

      @Override
      public String getPythonName () { return pythonName; }
   }

   protected Object writeReplace () throws ObjectStreamException {
      return new SerializedProbeConnection(dataSeries.getValue());
   }

   // readObject method for the serialization proxy pattern
   // See Effective Java, Second Ed., Item 78.
   private void readObject (java.io.ObjectInputStream stream) throws InvalidObjectException {
      throw new InvalidObjectException("SerializedNeuronConnection required");
   }

   @Override
   protected List<EditableProp<?>> addEditableProps (List<EditableProp<?>> list) {
      super.addEditableProps(list);
      list.add(dataSeries);
      return list;
   }

   public ProbeConnection (DataSeries dataSeries) {
      this.dataSeries = new EditableEnumProp<>("Data Series", DataSeries.class, this, dataSeries)
            .addImpact(EditableProp.Impact.CELL_LABEL);
   }

   public ProbeConnection () {
      this(DataSeries.SPIKES);
   }

   protected ProbeConnection (ProbeConnection orig) {
      this(orig.dataSeries.getValue());
   }

   @Override
   public String toString () {
      return "Probe " + dataSeries.getValue().getDisplayName();
   }

   @Override
   public ProbeConnection duplicate () { return new ProbeConnection(this); }

   public static final EntityCreator<ProbeConnection> CREATOR = new Creator();

   private static class Creator implements EntityCreator<ProbeConnection> {
      @Override
      public ProbeConnection create () { return new ProbeConnection(); }

      @Override
      public String toString () { return "Data Probe"; }  // used by drag&drop tooltips

      @Override
      public String getResourceFileBaseName () { return "probe"; }

      @Override
      public String getIconCaption () { return "Data Probe"; }

      @Override
      public String getCellStyle () { return "dataProbe"; }

      @Override
      public int getInitialCellHeight () { return 60; }

      @Override
      public int getInitialCellWidth () { return 60; }
   }

   public DataSeries getDataSeries () { return dataSeries.getValue(); }
}
