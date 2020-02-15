package de.unibi.hbp.ncc.lang;

import de.unibi.hbp.ncc.lang.props.EditableProp;
import de.unibi.hbp.ncc.lang.props.IntegerProp;

import java.util.List;

public abstract class NeuronPopulation extends NamedEntity<NeuronPopulation>
      implements Connectable {
   private IntegerProp neuronCount;

   private static Namespace<NeuronPopulation> globalNamespace;

   public static void setGlobalNamespace (Namespace<NeuronPopulation> ns) { globalNamespace = ns; }

   protected static Namespace<NeuronPopulation> getGlobalNamespace () { return globalNamespace; }

   @Override
   protected List<EditableProp<?>> addEditableProps (List<EditableProp<?>> list) {
      super.addEditableProps(list);
      list.add(neuronCount);
      return list;
   }

   protected NeuronPopulation (Namespace<NeuronPopulation> namespace, String name,int neuronCount) {
      super(namespace, name);
      this.neuronCount = new IntegerProp("Neuron Count", this, neuronCount) {
         @Override
         public boolean isValid (Integer proposedValue) {
            return super.isValid(proposedValue) && proposedValue >= 1;
         }
      };
   }

   protected IntegerProp getNeuronCountProp () { return neuronCount; }

   public int getNeuronCount () { return neuronCount.getValue(); }

   // TODO use this in graph edge creation checks
   // TODO disallow dangling edges in graph
   @Override
   public boolean isValidConnectionSource () { return true; }

   @Override
   public boolean isValidConnectionTarget () { return false; }
}
