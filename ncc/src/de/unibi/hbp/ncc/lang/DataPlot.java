package de.unibi.hbp.ncc.lang;

import de.unibi.hbp.ncc.editor.EntityCreator;
import de.unibi.hbp.ncc.graph.EdgeCollector;
import de.unibi.hbp.ncc.lang.props.EditableEnumProp;
import de.unibi.hbp.ncc.lang.props.EditableNameProp;
import de.unibi.hbp.ncc.lang.props.EditableProp;
import de.unibi.hbp.ncc.lang.utils.Iterators;

import java.util.List;
import java.util.Objects;

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
}
