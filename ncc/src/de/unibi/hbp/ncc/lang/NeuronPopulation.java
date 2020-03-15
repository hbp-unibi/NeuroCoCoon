package de.unibi.hbp.ncc.lang;

import de.unibi.hbp.ncc.graph.EdgeCollector;
import de.unibi.hbp.ncc.lang.props.EditableProp;
import de.unibi.hbp.ncc.lang.props.IntegerProp;
import de.unibi.hbp.ncc.lang.props.StrictlyPositiveIntegerProp;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
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
   public boolean isValidSource (EdgeKind edgeKind) { return edgeKind == EdgeKind.SYNAPSE; }

   @Override
   public boolean isValidTarget (EdgeKind edgeKind) {
      return edgeKind == EdgeKind.PROBE && !getSupportedDataSeries().isEmpty();
   }

   @Override
   public @Nullable Iterable<AnyConnection> getOutgoingEdgesImpl (EdgeKind edgeKind) {
      return EdgeCollector.getOutgoingConnections(edgeKind, getOwningCell());
   }

   @Override
   public @Nullable Iterable<AnyConnection> getIncomingEdgesImpl (EdgeKind edgeKind) {
      return EdgeCollector.getIncomingConnections(edgeKind, getOwningCell());
   }

   @Override
   public Collection<ProbeConnection.DataSeries> getRequiredDataSeries () {
      return EdgeCollector.getRequiredDataSeries(this);
   }
}
