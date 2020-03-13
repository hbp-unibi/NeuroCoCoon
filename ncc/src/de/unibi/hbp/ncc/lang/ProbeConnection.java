package de.unibi.hbp.ncc.lang;

import de.unibi.hbp.ncc.editor.EntityCreator;
import de.unibi.hbp.ncc.lang.props.EditableEnumProp;
import de.unibi.hbp.ncc.lang.props.EditableProp;
import de.unibi.hbp.ncc.lang.props.IntegerProp;
import de.unibi.hbp.ncc.lang.props.NonNegativeIntegerProp;
import de.unibi.hbp.ncc.lang.serialize.SerializedProbeConnection;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.List;

public class ProbeConnection extends AnyConnection implements Serializable {
   private EditableEnumProp<DataSeries> dataSeries;
   private IntegerProp firstNeuronIndex, neuronCount;  // count == 0 means unlimited

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

      public boolean isFiltered () { return this != SPIKES; }

      public boolean isContinuous () { return isFiltered(); }  // the same subset, just by coincidence
   }

   protected Object writeReplace () throws ObjectStreamException {
      return new SerializedProbeConnection(dataSeries.getValue(), firstNeuronIndex.getValue(), neuronCount.getValue());
   }

   // readObject method for the serialization proxy pattern
   // See Effective Java, Second Ed., Item 78.
   private void readObject (java.io.ObjectInputStream stream) throws InvalidObjectException {
      throw new InvalidObjectException("SerializedProbeConnection required");
   }

   @Override
   protected List<EditableProp<?>> addEditableProps (List<EditableProp<?>> list) {
      super.addEditableProps(list);
      list.add(dataSeries);
      list.add(firstNeuronIndex);
      list.add(neuronCount);
      return list;
   }

   public ProbeConnection (DataSeries dataSeries, int firstNeuonIndex, int neuronCount) {
      this.dataSeries = new EditableEnumProp<>("Data Series", DataSeries.class, this, dataSeries)
            .addImpact(EditableProp.Impact.CELL_LABEL);
      // TODO can the JComboBox be limited to data series supported by the current target of the probe?
      this.firstNeuronIndex = new NonNegativeIntegerProp("First Neuron #", this, firstNeuonIndex);
      this.neuronCount = new NonNegativeIntegerProp("Neuron Count", this, neuronCount);
   }

   public ProbeConnection () {
      this(DataSeries.SPIKES, 0, 0);
   }

   protected ProbeConnection (ProbeConnection orig) {
      this(orig.dataSeries.getValue(), orig.firstNeuronIndex.getValue(), orig.neuronCount.getValue());
   }

   @Override
   public String toString () {
      StringBuilder sb = new StringBuilder("Probe ").append(dataSeries.getValue().getDisplayName());
      int startIndex = firstNeuronIndex.getValue();
      int count = neuronCount.getValue();
      if (startIndex != 0 || count != 0) {
         sb.append('[').append(startIndex).append(':');
         if (count > 0)
            sb.append(startIndex + count);
         else
            sb.append("end");
         sb.append(']');
      }
      return sb.toString();
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

   public int getFirstNeuronIndex () { return firstNeuronIndex.getValue(); }
   public int getNeuronCount () { return neuronCount.getValue(); }
   public boolean isExcerpt () { return firstNeuronIndex.getValue() != 0 || neuronCount.getValue() != 0; }
   public boolean isEndLimited () { return neuronCount.getValue() != 0; }
   // exclusive end index, as needed by Python
   public int getNeuronEndIndex () { return firstNeuronIndex.getValue() + neuronCount.getValue(); }
}
