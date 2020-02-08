package de.unibi.hbp.ncc.lang;

public class Program {
   private final Scope global;

   public Program () {
      global = new Scope();
      NeuronPopulation.setGlobalNamespace(global.getNeuronPopulations());
      NeuronConnection.setGlobalSynapseTypeNamespace(global.getSynapseTypes());
      NeuronType defNeuronType = new NeuronType(global.getNeuronTypes(), "Default");
      defNeuronType.makePredefined();
      SynapseType defSynapseType = new SynapseType(global.getSynapseTypes(), "Default");
      defSynapseType.makePredefined();
   }

   public Scope getGlobalScope () {
      return global;
   }
}
