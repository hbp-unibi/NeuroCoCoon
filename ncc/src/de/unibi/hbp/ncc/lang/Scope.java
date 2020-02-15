package de.unibi.hbp.ncc.lang;

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
}
