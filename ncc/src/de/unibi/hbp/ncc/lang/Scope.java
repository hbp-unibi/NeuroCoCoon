package de.unibi.hbp.ncc.lang;

import de.unibi.hbp.ncc.lang.utils.Iterators;

public class Scope {
   private Namespace<NeuronPopulation> neuronPopulations;
   private Namespace<NeuronType> neuronTypes;
   private Namespace<SynapseType> synapseTypes;
   private Namespace<NetworkModule> moduleInstances;
   private Namespace<DataPlot> dataPlots;

   public Scope () {
      neuronPopulations = new Namespace<>(this, NeuronPopulation.class, "Population", "pop");
      neuronTypes = new Namespace<>(this, NeuronType.class, "Neuron Type", "nty");
      synapseTypes = new Namespace<>(this, SynapseType.class, "Synapse Type", "sty");
      moduleInstances = new Namespace<>(this, NetworkModule.class, "Module", "mod");
      dataPlots = new Namespace<>(this, DataPlot.class, "Plot", "plt");
   }

   public Namespace<NeuronPopulation> getNeuronPopulations () {
      return neuronPopulations;
   }
   public Namespace<NeuronType> getNeuronTypes () {
      return neuronTypes;
   }
   public Namespace<SynapseType> getSynapseTypes () {
      return synapseTypes;
   }
   public Namespace<NetworkModule> getModuleInstances () { return moduleInstances; }
   public Namespace<DataPlot> getDataPlots () { return dataPlots; }

   public Iterable<StandardPopulation> getStandardPopulations () {
      return Iterators.partialMap(neuronPopulations,
                                  pop -> pop instanceof StandardPopulation ? (StandardPopulation) pop : null);
   }

   public Iterable<RegularSpikeSource> getSpikeSources () {
      return Iterators.partialMap(neuronPopulations,
                                  pop -> pop instanceof RegularSpikeSource ? (RegularSpikeSource) pop : null);
   }

   public Iterable<PoissonSource> getPoissonSources () {
      return Iterators.partialMap(neuronPopulations,
                                  pop -> pop instanceof PoissonSource ? (PoissonSource) pop : null);
   }
}
