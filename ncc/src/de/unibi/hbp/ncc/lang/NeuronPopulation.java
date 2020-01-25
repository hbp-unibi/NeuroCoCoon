package de.unibi.hbp.ncc.lang;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NeuronPopulation extends NamedEntity {
   private NeuronType type;
   private int neuronCount;

   public NeuronPopulation (Namespace<NeuronPopulation> namespace, String name, NeuronType type, int neuronCount) {
      super(namespace, name);
      this.type = Objects.requireNonNull(type);
      this.neuronCount = neuronCount;
      addReferenceTo(type);
   }

   public NeuronPopulation (Namespace<NeuronPopulation> namespace) {
      super(namespace);
      this.type = Objects.requireNonNull(namespace.getContainingScope().getNeuronTypes().get("Default"));
      this.neuronCount = 1;
   }

   public static class Creator implements EntityCreator<NeuronPopulation> {
      @Override
      public NeuronPopulation create (Scope scope) {
         return new NeuronPopulation(scope.getNeuronPopulations());
      }

      @Override
      public String toString () {  // used by drag&drop tooltips
         return "Neuron Population";
      }
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
