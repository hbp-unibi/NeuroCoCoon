package de.unibi.hbp.ncc.lang;

import de.unibi.hbp.ncc.editor.EntityCreator;
import de.unibi.hbp.ncc.lang.props.DoubleProp;
import de.unibi.hbp.ncc.lang.props.EditableProp;
import de.unibi.hbp.ncc.lang.props.NonNegativeDoubleProp;
import de.unibi.hbp.ncc.lang.props.StrictlyPositiveDoubleProp;

import java.util.ArrayList;
import java.util.List;

public class RegularSpikeSource extends NeuronPopulation {
   private DoubleProp interval, perNeuronOffset, start, duration;

   @Override
   protected List<EditableProp<?>> addEditableProps (List<EditableProp<?>> list) {
      super.addEditableProps(list);
      list.add(interval);
      list.add(perNeuronOffset);
      list.add(start);
      list.add(duration);
      return list;
   }

   @Override
   protected String getGeneratedNamesPrefix () { return "Spike Source"; }

   public RegularSpikeSource (Namespace<NeuronPopulation> namespace, String name, int neuronCount,
                              double interval, double perNeuronOffset, double start, double duration) {
      super(namespace, name, neuronCount);
      this.interval = new StrictlyPositiveDoubleProp("Interval", this, interval).setUnit("ms");
      this.perNeuronOffset = new NonNegativeDoubleProp("Offset per Neuron", this, perNeuronOffset).setUnit("ms");
      this.start = new NonNegativeDoubleProp("Start", this, start).setUnit("ms");
      this.duration = new StrictlyPositiveDoubleProp("Duration", this, duration).setUnit("ms");
   }

   public RegularSpikeSource (Namespace<NeuronPopulation> namespace, String name) {
      this(namespace, name, 1, 100.0, 0.0, 0.0, 5000.0);
   }

   public RegularSpikeSource (String name) { this(getGlobalNamespace(), name); }
   public RegularSpikeSource () { this((String) null); }

   protected RegularSpikeSource (RegularSpikeSource orig) {
      this(orig.moreSpecificNamespace, orig.getCopiedName(), orig.getNeuronCountProp().getValue(),
           orig.interval.getValue(), orig.perNeuronOffset.getValue(), orig.start.getValue(), orig.duration.getValue());
   }

   public static final EntityCreator<RegularSpikeSource> CREATOR = new RegularSpikeSource.Creator();

   private static class Creator implements EntityCreator<RegularSpikeSource> {
      @Override
      public RegularSpikeSource create () {
         return new RegularSpikeSource();
      }

      @Override
      public String toString () {  // used by drag&drop tooltips
         return "Spike Source";
      }

      @Override
      public String getResourceFileBaseName () { return "spikesource"; }

      @Override
      public String getIconCaption () { return "Spikes"; }

      @Override
      public String getCellStyle () { return "spikeSource"; }

      @Override
      public int getInitialCellHeight () { return 100; }
   }

   @Override
   public RegularSpikeSource duplicate () {
      return new RegularSpikeSource(this);
   }

   public boolean isTwoDimensional () {
      return perNeuronOffset.getValue() != 0 && getNeuronCount() > 1;
   }

   private List<Double> getTimes (double start) {
      List<Double> result = new ArrayList<>();
      double interval = this.interval.getValue(), end = start + this.duration.getValue();
      for (int index = 0; ; index++) {
         double time = start + index * interval;
         if (time > end)
            break;
         result.add(time);
      }
      return result;
   }

   public List<Double> getSingleLevelTimes () {
      return getTimes(this.start.getValue());
   }

   public List<List<Double>> getTwoLevelTimes () {
      int neuronCount = getNeuronCount();
      List<List<Double>> result = new ArrayList<>(neuronCount);
      double start = this.start.getValue(), offset = perNeuronOffset.getValue();
      for (int n = 0; n < neuronCount; n++) {
         result.add(getTimes(start + n * offset));
      }
      return result;
   }
}
