package de.unibi.hbp.ncc.lang;

import com.mxgraph.model.mxICell;
import de.unibi.hbp.ncc.graph.EdgeCollector;
import de.unibi.hbp.ncc.lang.props.EditableProp;
import de.unibi.hbp.ncc.lang.props.IntegerProp;
import de.unibi.hbp.ncc.lang.props.StrictlyPositiveIntegerProp;

import java.util.ArrayList;
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

   @Override
   public boolean isValidConnectionSource () { return true; }

   @Override
   public boolean isValidConnectionTarget () { return false; }

   public Iterable<NeuronConnection> getOutgoingConnections () {
      return EdgeCollector.getOutgoingConnections(getOwningCell());
   }

   @Override
   public Iterable<NeuronConnection> getIncomingConnections () {
      return EdgeCollector.getIncomingConnections(getOwningCell());
   }
}
