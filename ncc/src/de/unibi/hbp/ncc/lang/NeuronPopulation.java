package de.unibi.hbp.ncc.lang;

import de.unibi.hbp.ncc.lang.props.EditableProp;
import de.unibi.hbp.ncc.lang.props.IntegerProp;
import de.unibi.hbp.ncc.lang.props.StrictlyPositiveIntegerProp;

import java.util.List;

public abstract class NeuronPopulation extends NamedEntity
      implements Connectable, PlotDataSource {
   protected final Namespace<NeuronPopulation> moreSpecificNamespace;
   private IntegerProp neuronCount;  // TODO support a multi-dimensional shape instead of a simple count

   private static Namespace<NeuronPopulation> globalNamespace;

   public static void setGlobalNamespace (Namespace<NeuronPopulation> ns) { globalNamespace = ns; }

   protected static Namespace<NeuronPopulation> getGlobalNamespace () { return globalNamespace; }

   @Override
   protected List<EditableProp<?>> addEditableProps (List<EditableProp<?>> list) {
      super.addEditableProps(list);
      list.add(neuronCount);
      return list;
   }

   protected NeuronPopulation (Namespace<NeuronPopulation> namespace, String name, int neuronCount) {
      super(namespace, name);
      moreSpecificNamespace = namespace;
      this.neuronCount = new StrictlyPositiveIntegerProp("Neuron Count", this, neuronCount);
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
