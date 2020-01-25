package de.unibi.hbp.ncc.lang;

public class Scope {
   private Namespace<NeuronPopulation> neuronPopulations;
   private Namespace<NeuronType> neuronTypes;
   private Namespace<SynapseType> synapseTypes;
   // TODO add DoubleConstant and IntegerConstant as named entities

   public Scope () {
      neuronPopulations = new Namespace<>(this, NeuronPopulation.class, "Population", "pop");
      neuronTypes = new Namespace<>(this, NeuronType.class, "Neuron Type", "nty");
      synapseTypes = new Namespace<>(this, SynapseType.class, "Synapse Type", "sty");
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
}
