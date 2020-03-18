package de.unibi.hbp.ncc.lang;

import de.unibi.hbp.ncc.editor.EntityCreator;
import de.unibi.hbp.ncc.lang.codegen.CodeGenUse;
import de.unibi.hbp.ncc.lang.props.EditableProp;
import de.unibi.hbp.ncc.lang.props.FilteredEditableEnumProp;
import de.unibi.hbp.ncc.lang.props.IntegerProp;
import de.unibi.hbp.ncc.lang.props.NonNegativeIntegerProp;
import de.unibi.hbp.ncc.lang.serialize.SerializedProbeConnection;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.List;

public class ProbeConnection extends AnyConnection implements Serializable {
   private final FilteredEditableEnumProp<DataSeries> dataSeries;
   private final IntegerProp firstNeuronIndex, neuronCount;  // count == 0 means unlimited

   public enum DataSeries implements DisplayNamed, PythonNamed {
      SPIKES("Spikes", "spikes", "Neuron index"),
      VOLTAGE("Membrane Voltage", "v", "Membrane potential (mV)"),
      GSYN_EXC("gsyn exc", "gsyn_exc", "Synaptic exc. conductance (uS)"),
      GSYN_INH("gsyn inh", "gsyn_inh", "Synaptic inh. conductance (uS)"),
      IZHIKEVICH_U("Izhikevich u", "u", "Membrane recovery (mV/ms)");

      private String pythonName, displayName, valueAxisLabel;

      DataSeries (String displayName, String pythonName, String valueAxisLabel) {
         this.displayName = displayName;
         this.pythonName = pythonName;
         this.valueAxisLabel = valueAxisLabel;
      }

      @Override
      public String getDisplayName () { return displayName; }

      @Override
      public String getPythonName () { return pythonName; }

      @CodeGenUse
      public String getValueAxisLabel () { return valueAxisLabel; }

      @CodeGenUse
      public boolean isFiltered () { return this != SPIKES; }

      public boolean isContinuous () { return isFiltered(); }  // the same subset, just by coincidence
   }

   protected Object writeReplace () throws ObjectStreamException {
      return new SerializedProbeConnection(dataSeries.getValue(), firstNeuronIndex.getValue(), neuronCount.getValue(),
                                           userLabel.getValue());
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
      list.add(userLabel);  // defined in superclass, positioned by subclass
      return list;
   }

   class ValidDataSeriesForTarget extends FilteredEditableEnumProp<DataSeries> {
      protected ValidDataSeriesForTarget (String propName, LanguageEntity owner, DataSeries value) {
         super(propName, DataSeries.class, owner, value);
      }

      @Override
      protected boolean isValidFor (DataSeries enumValue, LanguageEntity enclosingEntity) {
         PlotDataSource target = getTargetPlotDataSource();
         if (target != null)
            return target.getSupportedDataSeries().contains(enumValue);
         else
            return true;  // for dangling probes allow all data series
      }
   }

   public ProbeConnection (DataSeries dataSeries, int firstNeuronIndex, int neuronCount, String userLabel) {
      super(Connectable.EdgeKind.PROBE, userLabel);
      this.dataSeries = new ValidDataSeriesForTarget("Data Series", this, dataSeries)
            .addImpact(EditableProp.Impact.CELL_LABEL);
      this.firstNeuronIndex = new NonNegativeIntegerProp("First Neuron #", this, firstNeuronIndex);
      this.neuronCount = new NonNegativeIntegerProp("Neuron Count", this, neuronCount);
   }

   public ProbeConnection () {
      this(DataSeries.SPIKES, 0, 0, null);
   }

   protected ProbeConnection (ProbeConnection orig) {
      this(orig.dataSeries.getValue(), orig.firstNeuronIndex.getValue(), orig.neuronCount.getValue(),
           orig.userLabel.getValue());
   }

   @Override
   public String toString () {
      String label = getUserLabelOrNull();
      if (label != null)
         return label;
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

   public boolean hasValidDataSeries () { return dataSeries.hasValidValue(); }

   @CodeGenUse
   public int getFirstNeuronIndex () { return firstNeuronIndex.getValue(); }

   public int getNeuronCount () { return neuronCount.getValue(); }

   @CodeGenUse
   public boolean isExcerpt () { return firstNeuronIndex.getValue() != 0 || neuronCount.getValue() != 0; }

   @CodeGenUse
   public boolean isEndLimited () { return neuronCount.getValue() != 0; }

   // exclusive end index, as needed by Python
   @CodeGenUse
   public int getNeuronEndIndex () { return firstNeuronIndex.getValue() + neuronCount.getValue(); }
}
