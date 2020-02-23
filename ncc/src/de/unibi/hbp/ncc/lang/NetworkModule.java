package de.unibi.hbp.ncc.lang;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxGraph;
import de.unibi.hbp.ncc.lang.props.EditableProp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class NetworkModule extends NamedEntity
      implements GraphCellConfigurator, PlotDataSource {
   protected final Namespace<NetworkModule> moreSpecificNamespace;

   private String iconFileName;  // TODO store file basename (shared between icon and ST4 template file)
   private List<Port> inputPorts, outputPorts;
   private boolean useWideLayout;
   // TODO provide list of supported plots

   private static Namespace<NetworkModule> globalNamespace;

   public static void setGlobalNamespace (Namespace<NetworkModule> ns) { globalNamespace = ns; }

   protected static Namespace<NetworkModule> getGlobalNamespace () { return globalNamespace; }


   public static class Port implements Serializable, Connectable {
      // TODO should Port extend LanguageEntity (and use the owning cell?) or even NamedEntity with one namespace per module instance?

      public enum Direction {IN, OUT}

      private String portName;
      private Direction portDirection;
      private int portIndex;
      private mxCell portCell;

      public Port (String portName, int portIndex, Direction portDirection) {
         this.portName = portName;
         this.portIndex = portIndex;
         this.portDirection = portDirection;
      }

      public String getName () { return portName; }
      public Direction getDirection () { return portDirection; }
      public int getIndex () { return portIndex; }
      void setIndex (int portIndex) { this.portIndex = portIndex; }

      public mxCell getCell () { return portCell; }
      void setCell (mxCell portCell) { this.portCell = Objects.requireNonNull(portCell); }
      void clearCell () { this.portCell = null; }

      @Override
      public String toString () { return portName; }

      @Override
      public boolean isValidConnectionSource () { return portDirection == Direction.OUT; }

      @Override
      public boolean isValidConnectionTarget () { return portDirection == Direction.IN; }
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
      mxCell portCell = port.getCell();
      if (portCell == null) {
         portCell = new mxCell(port, portGeo, style);
         // TODO label needs some padding on the left (too close to triangle)
         // TODO label on right side overlaps with triangle (caused by offset?)
         portCell.setVertex(true);
         graph.addCell(portCell, moduleCell);
         port.setCell(portCell);
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
         mxCell portCell = port.getCell();
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
      return (useWideLayout ? "moduleWide" : "module") + ";image=/de/unibi/hbp/ncc/editor/images/lang/" + iconFileName;
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
      useWideLayout = false; // moduleGeo.getWidth() >= 3 * moduleGeo.getHeight();
      // modulePlaceholder.setStyle(useWideLayout ? "moduleWide" : "module");  // module is the style of the palette cells
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

   // TODO provide a (context) menu command to toggle the narrow/wide layout?
   @Override
   public void resizeExisting (mxGraph graph, mxCell existingCell) {
      // this is called as part of a graph model update transaction
      final mxGeometry moduleGeo = existingCell.getGeometry();
      boolean changeToWideLayout = moduleGeo.getWidth() >= 3 * moduleGeo.getHeight();
      // System.err.println("resizeExisting: " + moduleGeo);
      if (changeToWideLayout != useWideLayout) {
         useWideLayout = changeToWideLayout;
         graph.getModel().setStyle(existingCell, computeCellStyle());
         int layoutSteps = computeLayoutSteps();
         updatePorts(graph, existingCell, layoutSteps);
      }
   }

   protected NetworkModule (Namespace<NetworkModule> namespace, String name, String iconFileName) {
      super(namespace, name);
      moreSpecificNamespace = namespace;
      this.iconFileName = Objects.requireNonNull(iconFileName);
   }

   protected static NeuronType ensureDefaultType (Namespace<NeuronType> neuronTypes, String typeName) {
      NeuronType neuronType = neuronTypes.get(typeName);
      if (neuronType == null) {
         neuronType = new NeuronType(neuronTypes, typeName,
                                     NeuronType.NeuronKind.IF_COND_EXP,
                                     -70.0, -80.0, -60.0, 0.0, -100.0,
                                     3.0, 3.0, 1.0, 10.0, 0.2, 0.0);
      }
      return neuronType;
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

   protected boolean getPortIsOptional (Port.Direction direction, int portIndex) {
      return false;
   }
}
