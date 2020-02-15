package de.unibi.hbp.ncc.lang;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxGraph;
import de.unibi.hbp.ncc.lang.props.EditableProp;

import java.util.ArrayList;
import java.util.List;

public abstract class NetworkModule extends NamedEntity<NetworkModule> implements GraphCellPostProcessor {
   private List<Port> inputPorts, outputPorts;

   private static Namespace<NetworkModule> globalNamespace;

   public static void setGlobalNamespace (Namespace<NetworkModule> ns) { globalNamespace = ns; }

   protected static Namespace<NetworkModule> getGlobalNamespace () { return globalNamespace; }

   public enum Direction {IN, OUT}

   public class Port {

      private String portName;
      private Direction portDirection;
      private mxCell portCell;

      public Port (String portName, Direction portDirection) {
         this.portName = portName;
         this.portDirection = portDirection;
      }
   }

   @Override
   protected List<EditableProp<?>> addEditableProps (List<EditableProp<?>> list) {
      super.addEditableProps(list);
      return list;
   }

   private List<Port> buildPortList (List<String> portNames, Direction direction) {
      List<Port> ports = new ArrayList<>(portNames.size());
      for (String portName: portNames)
         ports.add(new Port(portName, direction));
      return ports;
   }

   private static final int PORT_SIZE = 20;
   private static final int PORT_SIZE_HALF = PORT_SIZE / 2;


   @Override
   public void adjustAndAdoptGraphCell (mxGraph graph, Object parent, mxCell modulePlaceholder) {
      // this is called as part of a graph model update transaction
      // precondition: this is the value object of modulePlaceholder
      this.setOwningCell(modulePlaceholder);
      final mxGeometry moduleGeometry = modulePlaceholder.getGeometry();
      moduleGeometry.setWidth(200);
      moduleGeometry.setHeight(100);
      modulePlaceholder.setConnectable(false);
      // mxGeometry geo = graph.getModel().getGeometry(modulePlaceholder);
      // The size of the rectangle when the minus sign is clicked
      // geo.setAlternateBounds(new mxRectangle(20, 20, 100, 50));

      mxGeometry geo1 = new mxGeometry(0, 0.5, PORT_SIZE, PORT_SIZE);
      // Because the origin is at upper left corner, need to translate to
      // position the center of port correctly
      geo1.setOffset(new mxPoint(0, -PORT_SIZE_HALF));
      geo1.setRelative(true);

      mxCell port1 = new mxCell("P1", geo1, "portleft");
      port1.setVertex(true);

      mxGeometry geo2 = new mxGeometry(1.0, 0.5, PORT_SIZE, PORT_SIZE);
      geo2.setOffset(new mxPoint(-PORT_SIZE, -PORT_SIZE_HALF));
      geo2.setRelative(true);

      mxCell port2 = new mxCell("P2", geo2, "portright");
      // FIXME vertical label centering is slightly too high
      port2.setVertex(true);

      graph.addCell(port1, modulePlaceholder);
      graph.addCell(port2, modulePlaceholder);

   }

   protected NetworkModule (Namespace<NetworkModule> namespace, String name,
                            List<String> inputPortNames, List<String> outputPortNames) {
      super(namespace, name);
      inputPorts = buildPortList(inputPortNames, Direction.IN);
      outputPorts = buildPortList(outputPortNames, Direction.OUT);
   }

}
