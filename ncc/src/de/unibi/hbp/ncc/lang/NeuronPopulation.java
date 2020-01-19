package de.unibi.hbp.ncc.lang;

import java.util.ArrayList;
import java.util.List;

public class NeuronPopulation extends NamedEntity<NeuronPopulation> {
   private int neuronCount;

   protected final static List<PropertyDescriptor<? extends LanguageEntity, ?>> entityProperties =
         addEntityProperties(new ArrayList<>(NamedEntity.entityProperties));

   protected static List<PropertyDescriptor<? extends LanguageEntity, ?>> addEntityProperties (List<PropertyDescriptor<? extends LanguageEntity, ?>> superProps) {
      PropertyDescriptor<NeuronPopulation, Integer> neuronCountProperty =
            new PropertyDescriptor<>("Neuron Count", Integer.class,
                                     NeuronPopulation::setNeuronCount, NeuronPopulation::getNeuronCount,
                                     NeuronPopulation::validateNeuronCount, null);

      superProps.add(neuronCountProperty);
      return superProps;
   }

   @Override
   public List<PropertyDescriptor<? extends LanguageEntity, ?>> getEntityProperties () {
      return entityProperties;
   }

   public NeuronPopulation (Namespace<NeuronPopulation> namespace, String name, int neuronCount) {
      super(namespace, name);
      this.neuronCount = neuronCount;
   }

   public int getNeuronCount () {
      return neuronCount;
   }

   public void setNeuronCount (int neuronCount) {
      this.neuronCount = neuronCount;
   }

   public static String validateNeuronCount (int neuronCount) {
      if (neuronCount < 1)
         return "Neuron count must be strictly positive";
      else
         return null;
   }
}
