package de.unibi.hbp.ncc.lang;

public class Program {
   private final Scope global;

   public Program () {
      global = new Scope();
      NeuronPopulation.setGlobalNamespace(global.getNeuronPopulations());
      final Namespace<SynapseType> synapseTypes = global.getSynapseTypes();
      NeuronConnection.setGlobalSynapseTypeNamespace(synapseTypes);
      NeuronType defNeuronType = new NeuronType(global.getNeuronTypes(), "Default");
      defNeuronType.makePredefined();
      SynapseType defAllSynapseType = new SynapseType(synapseTypes, "All Default", SynapseType.SynapseKind.ALL_TO_ALL);
      defAllSynapseType.makePredefined();
      SynapseType defOneSynapseType = new SynapseType(synapseTypes, "One Default", SynapseType.SynapseKind.ONE_TO_ONE);
      defOneSynapseType.makePredefined();
      SynapseType defProbSynapseType = new SynapseType(synapseTypes, "Prob Default", SynapseType.SynapseKind.FIXED_PROBABILITY);
      defProbSynapseType.makePredefined();
   }

   public Scope getGlobalScope () {
      return global;
   }
}
