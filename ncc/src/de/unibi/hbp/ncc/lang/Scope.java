package de.unibi.hbp.ncc.lang;

public class Scope {
   private Namespace<NeuronPopulation> neuronPopulations;
   private Namespace<NeuronType> neuronTypes;

   public Scope () {
      neuronPopulations = new Namespace<>(NeuronPopulation.class, "Population", "pop");
      neuronTypes = new Namespace<>(NeuronType.class, "Neuron Type", "nty");
   }

   public Namespace<NeuronPopulation> getNeuronPopulations () {
      return neuronPopulations;
   }

   public Namespace<NeuronType> getNeuronTypes () {
      return neuronTypes;
   }
}
