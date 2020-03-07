package de.unibi.hbp.ncc.lang;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxGraph;
import de.unibi.hbp.ncc.graph.EdgeCollector;
import de.unibi.hbp.ncc.lang.props.EditableProp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public abstract class NetworkModule extends NamedEntity
      implements GraphCellConfigurator {
   protected final Namespace<NetworkModule> moreSpecificNamespace;

   private String resourceFileBaseName;  // TODO store file basename (shared between icon and ST4 template file)
   private List<Port> inputPorts, outputPorts;
   private boolean useWideLayout;
   // TODO provide list of supported plots

   private static Namespace<NetworkModule> globalNamespace;

   public static void setGlobalNamespace (Namespace<NetworkModule> ns) { globalNamespace = ns; }

   protected static Namespace<NetworkModule> getGlobalNamespace () { return globalNamespace; }


   public static class Port implements Serializable, Connectable, PlotDataSource {
      // TODO should Port extend LanguageEntity (and use the owning cell?) or even NamedEntity with one namespace per module instance?

      public enum Direction {IN, OUT}

      private String portName;
      private Direction portDirection;
      private int portIndex;
      private mxICell portCell;

      public Port (String portName, int portIndex, Direction portDirection) {
         this.portName = portName;
         this.portIndex = portIndex;
         this.portDirection = portDirection;
      }

      public String getName () { return portName; }
      public Direction getDirection () { return portDirection; }
      public String getDirectionAsString () { return portDirection.name().toLowerCase(); }
      public int getIndex () { return portIndex; }
      void setIndex (int portIndex) { this.portIndex = portIndex; }

      public mxICell getCell () { return portCell; }
      void setCell (mxICell portCell) { this.portCell = Objects.requireNonNull(portCell); }
      void clearCell () { this.portCell = null; }

      @Override
      public String toString () { return portName; }

      @Override
      public boolean isValidSynapseSource () { return portDirection == Direction.OUT; }

      @Override
      public boolean isValidSynapseTarget () { return portDirection == Direction.IN; }

      @Override
      public Iterable<NeuronConnection> getOutgoingSynapses () {
         return EdgeCollector.getOutgoingSynapses(getCell());
      }

      @Override
      public Iterable<NeuronConnection> getIncomingSynapses () {
         return EdgeCollector.getIncomingSynapses(getCell());
      }

      @Override
      public boolean isValidProbeTarget () { return portDirection == Direction.OUT; }

      @Override
      public Iterable<ProbeConnection> getIncomingProbes () {
         return EdgeCollector.getIncomingProbes(getCell());
      }

      // code generation needs to access the module owning a port
      // but port must be serializable without referencing the module instance
      // TODO use non-default serialization instead?
      public NetworkModule getOwningModule () {
         return (NetworkModule) portCell.getParent().getValue();
      }

      @Override
      public Collection<ProbeConnection.DataSeries> getSupportedDataSeries () {
         return getOwningModule().getPortDataSeries(portDirection, portIndex);
      }

      @Override
      public Collection<ProbeConnection.DataSeries> getRequiredDataSeries () {
         return EdgeCollector.getRequiredDataSeries(this);
      }

      public String getUnadornedPythonName () { return Namespace.buildUnadornedPythonName(portName); }
   }

   @Override
   protected List<EditableProp<?>> addEditableProps (List<EditableProp<?>> list) {
      super.addEditableProps(list);
      return list;
   }

   private Port findPortByName (List<Port> ports, String name) {
      for (Port port: ports)
         if (name.equals(port.getName()))
            return port;
      return null;
   }

   public Port adoptCellForPort (mxICell cell, boolean isInput, String portName) {
      Port port = findPortByName(isInput ? inputPorts : outputPorts, portName);
      assert port != null : "encoded port could not be resolved";
      port.setCell(cell);
      cell.setValue(port);
      return port;
   }

   private Port findPortByName (String name) {
      Port port = findPortByName(inputPorts, name);
      if (port == null)
         port = findPortByName(outputPorts, name);
      return port;
   }

   private List<Port> buildPortList (Port.Direction direction, List<Port> previousPorts) {
      List<String> portNames = getPortNames(direction);
      List<Port> ports = new ArrayList<>(portNames.size());
      int index = 0;
      for (String portName: portNames) {
         Port previousPort;
         if (previousPorts != null && (previousPort = findPortByName(previousPorts, portName)) != null) {
            previousPorts.remove(previousPort);
            // name and direction do not change, but index might be different
            previousPort.setIndex(index);
            ports.add(previousPort);
         }
         else
            ports.add(new Port(portName, index, direction));
         index++;
      }
      return ports;
   }

   private static final int PORT_SIZE = 20;
   private static final int PORT_SIZE_HALF = PORT_SIZE / 2;

   private void updatePort (mxGraph graph, mxCell moduleCell, Port port, mxGeometry portGeo, String style) {
      portGeo.setRelative(true);
      mxICell portCell = port.getCell();
      if (portCell == null) {
         mxCell cell = new mxCell(port, portGeo, style);
         cell.setVertex(true);
         graph.addCell(cell, moduleCell);
         port.setCell(cell);
      }
      else {
         final mxIGraphModel graphModel = graph.getModel();
         if (!style.equals(portCell.getStyle()))
            graphModel.setStyle(portCell, style);
         graphModel.setGeometry(portCell, portGeo);
      }
   }

   private void updatePortColumn (mxGraph graph, mxCell moduleCell,
                                  int layoutRows, List<Port> ports, double x, int xOffset, String style) {
      int currentLayoutRow = 1;
      for (Port port: ports) {
         mxGeometry portGeo = new mxGeometry(x, currentLayoutRow / (double) layoutRows, PORT_SIZE, PORT_SIZE);
         portGeo.setOffset(new mxPoint(xOffset, -PORT_SIZE_HALF));
         updatePort(graph, moduleCell, port, portGeo, style);
         currentLayoutRow += 1;
      }
   }

   private void updatePortRow (mxGraph graph, mxCell moduleCell,
                               int layoutCols, List<Port> ports, double y, int yOffset, String style) {
      int currentLayoutCol = 1;
      for (Port port: ports) {
         mxGeometry portGeo = new mxGeometry(currentLayoutCol / (double) layoutCols, y, PORT_SIZE, PORT_SIZE);
         portGeo.setOffset(new mxPoint(-PORT_SIZE_HALF, yOffset));
         updatePort(graph, moduleCell, port, portGeo, style);
         currentLayoutCol += 1;
      }
   }

   private void removePorts (mxGraph graph, List<Port> ports) {
      if (ports == null)
         return;
      for (Port port: ports) {
         mxICell portCell = port.getCell();
         if (portCell != null)
            graph.getModel().remove(portCell);
         port.clearCell();
      }
      ports.clear();
   }

   private int computeLayoutSteps () {
         return Math.max(inputPorts.size(), outputPorts.size()) + 1;
         // additional row/column is for outer gaps
   }

   private String computeCellStyle () {
      return (useWideLayout ? "moduleWide" : "module") + ";image=/de/unibi/hbp/ncc/editor/images/lang/" +
            resourceFileBaseName + ".png";
   }

   private void updatePorts (mxGraph graph, mxCell moduleCell, int layoutSteps) {
      if (useWideLayout) {
         updatePortRow(graph, moduleCell, layoutSteps, inputPorts, 0.0, 0, "portTop");
         updatePortRow(graph, moduleCell, layoutSteps, outputPorts, 1.0, -PORT_SIZE, "portBottom");
      }
      else {
         updatePortColumn(graph, moduleCell, layoutSteps, inputPorts, 0.0, 0, "portLeft");
         updatePortColumn(graph, moduleCell, layoutSteps, outputPorts, 1.0, -PORT_SIZE, "portRight");
      }
   }

   @Override
   public void configurePlaceholder (mxGraph graph, mxCell modulePlaceholder) {
      // this is called as part of a graph model update transaction
      // precondition: receiver and modulePlaceholder are connected as value object and owning cell
      assert inputPorts == null && outputPorts == null;
      inputPorts = buildPortList(Port.Direction.IN, null);
      outputPorts = buildPortList(Port.Direction.OUT, null);
      // newly placed modules always start with the non-wide/vertical port layout
      useWideLayout = false;
      int layoutSteps = computeLayoutSteps();
      mxGeometry previousModuleGeo = modulePlaceholder.getGeometry();
      mxGeometry moduleGeo = new mxGeometry(previousModuleGeo.getX(), previousModuleGeo.getY(),
                                            200, 3 * layoutSteps * PORT_SIZE_HALF);
      graph.getModel().setGeometry(modulePlaceholder, moduleGeo);
      graph.getModel().setStyle(modulePlaceholder, computeCellStyle());
      modulePlaceholder.setConnectable(false);
      updatePorts(graph, modulePlaceholder, layoutSteps);
   }

   @Override
   public void restructureExisting (mxGraph graph, mxCell existingCell) {
      // this is called as part of a graph model update transaction
      assert inputPorts != null && outputPorts != null;
      int previousLayoutSteps = computeLayoutSteps();
      List<Port> previousInputPort = inputPorts, previousOutputPorts = outputPorts;
      inputPorts = buildPortList(Port.Direction.IN, previousInputPort);
      outputPorts = buildPortList(Port.Direction.OUT, previousOutputPorts);
      removePorts(graph, previousInputPort);
      removePorts(graph, previousOutputPorts);
      int layoutSteps = computeLayoutSteps();
      if (layoutSteps != previousLayoutSteps) {
         mxGeometry previousModuleGeo = existingCell.getGeometry();
         mxGeometry moduleGeo = (mxGeometry) previousModuleGeo.clone();
         if (useWideLayout)
            moduleGeo.setWidth(layoutSteps * PORT_SIZE_HALF);
         else
            moduleGeo.setHeight(3 * layoutSteps * PORT_SIZE_HALF);
         graph.getModel().setGeometry(existingCell, moduleGeo);
      }
      // cell style does not change
      updatePorts(graph, existingCell, layoutSteps);
   }

   @Override
   public void resizeExisting (mxGraph graph, mxCell existingCell) {
      // this is called as part of a graph model update transaction
      // nothing to do: our port children have relative geometry
   }

   public void toggleLayoutOrientation (mxGraph graph, mxCell existingCell) {
      // this must be called as part of a graph model update transaction
         useWideLayout = !useWideLayout;
         graph.getModel().setStyle(existingCell, computeCellStyle());
         int layoutSteps = computeLayoutSteps();
         updatePorts(graph, existingCell, layoutSteps);
   }

   public void reviveAfterLoading (mxICell moduleCell) {
      assert inputPorts == null && outputPorts == null;
      inputPorts = buildPortList(Port.Direction.IN, null);
      outputPorts = buildPortList(Port.Direction.OUT, null);
      String cellStyle = moduleCell.getStyle();
      useWideLayout = cellStyle != null && cellStyle.startsWith("moduleWide");
   }

   protected NetworkModule (Namespace<NetworkModule> namespace, String name, String resourceFileBaseName) {
      super(namespace, name);
      moreSpecificNamespace = namespace;
      this.resourceFileBaseName = Objects.requireNonNull(resourceFileBaseName);
   }

   protected List<String> getPortNames (List<String> cache, int count, String prefix) {
      if (cache == null || cache.size() != count) {
         cache = new ArrayList<>(count);
         for (int nr = 1; nr <= count; nr++)
            cache.add(prefix + " " + nr);
      }
      return cache;
   }

   protected abstract List<String> getPortNames (Port.Direction direction);

   protected abstract int getPortDimension (Port.Direction direction, int portIndex);
   // TODO need to provide a cardinality (neuron count) per port as well

   // TODO provide a PlotDataSource info per port as well
   protected abstract Collection<ProbeConnection.DataSeries> getPortDataSeries (Port.Direction direction, int portIndex);

   protected abstract boolean getPortIsConductanceBased (Port.Direction direction, int portIndex);

   protected boolean getPortIsOptional (Port.Direction direction, int portIndex) {
      return false;
   }

   public int getPortDimension (Port port) {
      return getPortDimension(port.getDirection(), port.getIndex());
   }

   public boolean isOptionalPort (Port port) {
      return getPortIsOptional(port.getDirection(), port.getIndex());
   }

   public Iterable<Port> getInputPorts () { return inputPorts; }
   public Iterable<Port> getOutputPorts () { return outputPorts; }
   // TODO add an empty module-specific checking method to be invoked from ProgramVisitor.check

   public String getTemplateGroupFileName () { return resourceFileBaseName + ".stg"; }

   public String getDefineBuilderTemplateName () { return "define_" + resourceFileBaseName + "_builder"; }
   public String getBuildInstanceTemplateName () { return "build_" + resourceFileBaseName + "_instance"; }
}
