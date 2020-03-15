package de.unibi.hbp.ncc.lang;

import de.unibi.hbp.ncc.editor.EntityCreator;
import de.unibi.hbp.ncc.graph.EdgeCollector;
import de.unibi.hbp.ncc.lang.codegen.CodeGenUse;
import de.unibi.hbp.ncc.lang.props.BooleanProp;
import de.unibi.hbp.ncc.lang.props.EditableProp;
import de.unibi.hbp.ncc.lang.utils.Iterators;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class DataPlot extends NamedEntity implements Connectable {
   private final Namespace<DataPlot> moreSpecificNamespace;
   private final BooleanProp combineSameKindData;

   private static Namespace<DataPlot> globalNamespace;

   public static void setGlobalNamespace (Namespace<DataPlot> ns) { globalNamespace = ns; }

   protected static Namespace<DataPlot> getGlobalNamespace () { return globalNamespace; }

   @Override
   protected List<EditableProp<?>> addEditableProps (List<EditableProp<?>> list) {
      super.addEditableProps(list);
      list.add(combineSameKindData);
      // currently no properties, beyond its name
      return list;
   }

   @Override
   protected String getGeneratedNamesPrefix () { return "Plot"; }

   public DataPlot (Namespace<DataPlot> namespace, String name, boolean combineSameKindData) {
      super(namespace, name);
      moreSpecificNamespace = namespace;
      this.combineSameKindData = new BooleanProp("Combine Data of Same Kind", this, combineSameKindData);
   }

   public DataPlot (Namespace<DataPlot> namespace, String name) {
      this(namespace, name, true);
   }

   public DataPlot (String name) {
      this(getGlobalNamespace(), name);
   }

   public DataPlot (Namespace<DataPlot> namespace) {
      this(namespace, null);
   }

   public DataPlot () { this((String) null); }

   protected DataPlot (DataPlot orig) {
      this(orig.moreSpecificNamespace, orig.getCopiedName(), orig.combineSameKindData.getValue());
   }

   public static final EntityCreator<DataPlot> CREATOR = new Creator();

   private static class Creator implements EntityCreator<DataPlot> {
      @Override
      public DataPlot create () {
         return new DataPlot();
      }

      @Override
      public String toString () {  // used by drag&drop tooltips
         return "Data Plot";
      }

      @Override
      public String getResourceFileBaseName () { return "plot"; }

      @Override
      public String getIconCaption () { return "Data Plot"; }

      @Override
      public String getCellStyle () { return "dataPlot"; }

      @Override
      public int getInitialCellHeight () { return 100; }
   }

   @Override
   public LanguageEntity duplicate () {
      return new DataPlot(this);
   }

   @Override
   public boolean isValidSource (EdgeKind edgeKind) { return edgeKind == EdgeKind.PROBE; }

   @Override
   public @Nullable Iterable<AnyConnection> getOutgoingEdgesImpl (EdgeKind edgeKind) {
      return EdgeCollector.getOutgoingConnections(edgeKind, getOwningCell());
   }

   @Override
   public @Nullable Iterable<AnyConnection> getIncomingEdgesImpl (EdgeKind edgeKind) {
      return null;  // no incoming edges allowed
   }

   public String getOutputFileName () { return Namespace.buildUnadornedPythonName(getName()) + ".png"; }

   @CodeGenUse
   public boolean isCombineSameKindData () { return combineSameKindData.getValue(); }

   private static class ProbeComparator implements Comparator<ProbeConnection> {

      private static String getTargetEntityName (ProbeConnection con) {
         NamedEntity target = con.getTargetNamedEntity();
         if (target != null)
            return target.getName();
         NetworkModule.Port targetPort = con.getTargetModulePort();
         if (targetPort != null)
            return targetPort.getOwningModule().getName() + "." + targetPort.getName();
         // could also sandwich in direction, but only output ports are allowed as targets
         return "";  // sorts to the front and avoids separate treatment of null values
      }

      @Override
      public int compare (ProbeConnection conA, ProbeConnection conB) {
         String nameA = getTargetEntityName(conA);
         String nameB = getTargetEntityName(conB);
         if (nameA.equals(nameB))
            return conA.getDataSeries().compareTo(conB.getDataSeries());
         else
            return Namespace.getSmartNumericOrderComparator().compare(nameA, nameB);
      }
   }

   // for plotting bundle ProbeConnection or a DataSeries with some computed formatting attributes
   public static class AnnotatedPlotItem<T> {
      private T item;
      private char cyclicColor;
      private boolean first, last;

      private static final String COLOR_SUPPLY = "bgrcmyk";

      AnnotatedPlotItem (T item, int position, int length) {
         this.item = item;
         this.cyclicColor = COLOR_SUPPLY.charAt(position % COLOR_SUPPLY.length());
         this.first = position == 0;
         this.last = position == length - 1;
      }

      @CodeGenUse
      public T getItem () { return item; }

      @CodeGenUse
      public String getCyclicColor () { return String.valueOf(cyclicColor); }

      @CodeGenUse
      public boolean isFirst () { return first; }

      @CodeGenUse
      public boolean isLast () { return last; }

   }

   public static class AnnotatedDataSeries extends AnnotatedPlotItem<ProbeConnection.DataSeries> {
      private Collection<AnnotatedPlotItem<ProbeConnection>> subsidiaryProbes;

      AnnotatedDataSeries (ProbeConnection.DataSeries item, int position, int length,
                           Collection<AnnotatedPlotItem<ProbeConnection>> subsidiaryProbes) {
         super(item, position, length);
         this.subsidiaryProbes = subsidiaryProbes;
      }

      public Collection<AnnotatedPlotItem<ProbeConnection>> getSubsidiaryProbes () {
         return subsidiaryProbes;
      }
   }

   static Collection<AnnotatedPlotItem<ProbeConnection>> annotateProbeConnections (Collection<ProbeConnection> probes) {
      int size = probes.size();
      List<AnnotatedPlotItem<ProbeConnection>> result = new ArrayList<>();
      int position = 0;
      for (ProbeConnection probe: probes)
         result.add(new AnnotatedPlotItem<>(probe, position++, size));
      return result;
   }

   Collection<AnnotatedDataSeries> annotateDataSeries (Collection<ProbeConnection.DataSeries> series) {
      int size = series.size();
      List<AnnotatedDataSeries> result = new ArrayList<>();
      int position = 0;
      for (ProbeConnection.DataSeries oneSeries: series)
         result.add(new AnnotatedDataSeries(oneSeries, position++, size, getOutgoingProbesSorted(oneSeries)));
      return result;
   }

   private static final ProbeComparator orderByTargetAndSeries = new ProbeComparator();

   @CodeGenUse
   public Collection<AnnotatedPlotItem<ProbeConnection>> getOutgoingProbesSorted (ProbeConnection.DataSeries dataSeries) {  // for code generation, determines order of data contributions in one panel of figure
      List<ProbeConnection> probes = Iterators.asList(Iterators.filter(
            getOutgoingProbes().iterator(), probe -> probe.getDataSeries() == dataSeries));
      probes.sort(orderByTargetAndSeries);
      return annotateProbeConnections(probes);
   }

   @CodeGenUse
   public Collection<AnnotatedDataSeries> getContributingDataSeries () {
      return annotateDataSeries(EdgeCollector.getContributingDataSeries(this));
   }
}
