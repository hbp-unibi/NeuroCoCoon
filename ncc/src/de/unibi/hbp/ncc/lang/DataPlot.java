package de.unibi.hbp.ncc.lang;

import de.unibi.hbp.ncc.lang.props.EditableEnumProp;
import de.unibi.hbp.ncc.lang.props.EditableNameProp;
import de.unibi.hbp.ncc.lang.props.EditableProp;

import java.util.List;
import java.util.Objects;

public class DataPlot extends NamedEntity {
   private final Namespace<DataPlot> moreSpecificNamespace;
   private EditableNameProp<NeuronPopulation> population;
   private EditableEnumProp<DataSeries> series;

   public enum DataSeries { SPIKES, VOLTAGE; }  // TODO must implement DisplayNamed (and PythonNamed?)

   @Override
   protected List<EditableProp<?>> addEditableProps (List<EditableProp<?>> list) {
      super.addEditableProps(list);
      list.add(population);
      list.add(series);
      return list;
   }

   @Override
   protected String getGeneratedNamesPrefix () { return "Plot"; }

   protected DataPlot (Namespace<DataPlot> namespace, String name, NeuronPopulation population, DataSeries series) {
      super(namespace, name);
      moreSpecificNamespace = namespace;
      Namespace<NeuronPopulation> neuronPopulations = namespace.getContainingScope().getNeuronPopulations();
      if (population == null)
         population = neuronPopulations.getFallbackDefault();
      if (series == null)
         series = DataSeries.SPIKES;  // TODO pick first valid data series for population instead
      this.population = new EditableNameProp<>("Neuron Population", NeuronPopulation.class, this,
                                               Objects.requireNonNull(population), neuronPopulations);
      this.series = new EditableEnumProp<>("Data Series", DataSeries.class, this,
                                           Objects.requireNonNull(series));
   }

   public DataPlot (Namespace<DataPlot> namespace) {
      this(namespace, null, null, null);
   }

   public DataPlot createIfPossible (Namespace<DataPlot> namespace) {
      if (namespace.getContainingScope().getNeuronPopulations().isEmpty())
         return null;  // would cause construction of a default configured DataPlot to fail
      else
         return new DataPlot(namespace);
   }

   protected DataPlot (DataPlot orig) {
      this(orig.moreSpecificNamespace, orig.getCopiedName(), orig.population.getValue(), orig.series.getValue());
   }

   @Override
   public LanguageEntity duplicate () {
      return new DataPlot(this);
   }
}
