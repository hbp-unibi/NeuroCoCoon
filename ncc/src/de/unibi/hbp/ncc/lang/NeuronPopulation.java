package de.unibi.hbp.ncc.lang;

import java.util.ArrayList;
import java.util.List;

public class NeuronPopulation extends NamedEntity {
   private int neuronCount;

   public NeuronPopulation (Namespace<NeuronPopulation> namespace, String name, int neuronCount) {
      super(namespace, name);
      this.neuronCount = neuronCount;
   }

   public NeuronPopulation (Namespace<NeuronPopulation> namespace, String name) {
      this(namespace, name, 1);
   }

   public NeuronPopulation (Namespace<NeuronPopulation> namespace, int neuronCount) {
      super(namespace);
      this.neuronCount = neuronCount;
   }

   public NeuronPopulation (Namespace<NeuronPopulation> namespace) {
      this(namespace, 1);
   }

   public int getNeuronCount () {
      return neuronCount;
   }

   public void setNeuronCount (int neuronCount) {
      this.neuronCount = neuronCount;
   }

   private static List<PropertyDescriptor<? extends LanguageEntity, ?>> entityProperties;

   public List<PropertyDescriptor<? extends LanguageEntity, ?>> getEntityProperties () {
      if (entityProperties == null) {
         IntegerPropertyDescriptor<NeuronPopulation> neuronCountProperty =
               new IntegerPropertyDescriptor<>(NeuronPopulation.class, "Neuron Count",
                                               NeuronPopulation::setNeuronCount, NeuronPopulation::getNeuronCount,
                                               NeuronPopulation::checkNeuronCount);

         List<PropertyDescriptor<? extends LanguageEntity, ?>> list =
               new ArrayList<>(super.getEntityProperties());
         list.add(neuronCountProperty);
         entityProperties = list;
      }
      return entityProperties;
   }

   public static String checkNeuronCount (int neuronCount) {
      if (neuronCount < 1)
         return "Neuron count must be strictly positive";
      else
         return null;
   }
}
