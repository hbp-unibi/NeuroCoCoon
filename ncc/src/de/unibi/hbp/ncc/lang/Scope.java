package de.unibi.hbp.ncc.lang;

import de.unibi.hbp.ncc.lang.codegen.CodeGenUse;
import de.unibi.hbp.ncc.lang.utils.Iterators;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

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

   public void clear () {
      neuronPopulations.clear();
      neuronTypes.clear();
      synapseTypes.clear();
      moduleInstances.clear();
      dataPlots.clear();
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

   // helpers used by code generation

   @CodeGenUse
   public Iterable<StandardPopulation> getStandardPopulations () {
      return Iterators.partialMap(neuronPopulations,
                                  pop -> pop instanceof StandardPopulation ? (StandardPopulation) pop : null);
   }

   @CodeGenUse
   public Iterable<RegularSpikeSource> getSpikeSources () {
      return Iterators.partialMap(neuronPopulations,
                                  pop -> pop instanceof RegularSpikeSource ? (RegularSpikeSource) pop : null);
   }

   @CodeGenUse
   public Iterable<PoissonSource> getPoissonSources () {
      return Iterators.partialMap(neuronPopulations,
                                  pop -> pop instanceof PoissonSource ? (PoissonSource) pop : null);
   }

   @CodeGenUse
   public Iterable<SynapseType.ConnectorKind> getConnectorKinds () {
      return Arrays.asList(SynapseType.ConnectorKind.values());
   }

   @CodeGenUse
   public Iterable<NetworkModule> getOneModuleInstancePerUsedClass () {
      Map<String, NetworkModule> representativePerClass = new TreeMap<>();
      for (NetworkModule moduleInstance: moduleInstances)
         representativePerClass.putIfAbsent(moduleInstance.getClass().getName(), moduleInstance);
      return representativePerClass.values();
   }
}
