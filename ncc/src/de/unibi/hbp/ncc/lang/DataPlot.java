package de.unibi.hbp.ncc.lang;

import de.unibi.hbp.ncc.editor.EntityCreator;
import de.unibi.hbp.ncc.graph.EdgeCollector;
import de.unibi.hbp.ncc.lang.props.EditableProp;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class DataPlot extends NamedEntity implements Connectable {
   private final Namespace<DataPlot> moreSpecificNamespace;

   // TODO make this a visually represented entity with ProbeConnections that store the DataSeries selection

   private static Namespace<DataPlot> globalNamespace;

   public static void setGlobalNamespace (Namespace<DataPlot> ns) { globalNamespace = ns; }

   protected static Namespace<DataPlot> getGlobalNamespace () { return globalNamespace; }

   @Override
   protected List<EditableProp<?>> addEditableProps (List<EditableProp<?>> list) {
      super.addEditableProps(list);
      // currently no properties, beyond its name
      return list;
   }

   @Override
   protected String getGeneratedNamesPrefix () { return "Plot"; }

   public DataPlot (Namespace<DataPlot> namespace, String name) {
      super(namespace, name);
      moreSpecificNamespace = namespace;
   }

   public DataPlot (String name) {
      this(getGlobalNamespace(), name);
   }
   public DataPlot (Namespace<DataPlot> namespace) {
      this(namespace, null);
   }
   public DataPlot () { this((String) null); }

   protected DataPlot (DataPlot orig) {
      this(orig.moreSpecificNamespace, orig.getCopiedName());
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
   public boolean isValidProbeSource () { return true; }

   @Override
   public Iterable<ProbeConnection> getOutgoingProbes () {
      return EdgeCollector.getOutgoingProbes(getOwningCell());
   }

   public String getOutputFileName () { return Namespace.buildUnadornedPythonName(getName()) + ".png"; }

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

   private static final ProbeComparator orderByTargetAndSeries = new ProbeComparator();

   public Collection<ProbeConnection> getOutgoingProbesSorted () {  // for code generation, determines order of panels in figure
      List<ProbeConnection> probes = EdgeCollector.getOutgoingProbes(getOwningCell());
      probes.sort(orderByTargetAndSeries);
      return probes;

   }
}
