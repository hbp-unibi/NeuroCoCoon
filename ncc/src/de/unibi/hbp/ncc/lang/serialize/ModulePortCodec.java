package de.unibi.hbp.ncc.lang.serialize;

import com.mxgraph.io.mxCodec;
import com.mxgraph.io.mxObjectCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import de.unibi.hbp.ncc.lang.LanguageEntity;
import de.unibi.hbp.ncc.lang.NetworkModule;
import de.unibi.hbp.ncc.lang.Program;
import org.w3c.dom.Node;

public class ModulePortCodec extends mxObjectCodec {
   private final Program program;

   public static class EncodedPort {
      private String name;
      private boolean input;

      public EncodedPort () { }

      public EncodedPort (boolean input, String name) {
         this.name = name;
         this.input = input;
      }

      public String getName () { return name; }

      public void setName (String name) { this.name = name; }

      public boolean isInput () { return input; }

      public void setInput (boolean input) { this.input = input; }

      @Override
      public String toString () { return name; }
   }

   public ModulePortCodec (Program program) {
      super(new EncodedPort());  // decoding starts with a clone of this empty port description
      this.program = program;
      String myName = getName();
   }

   private void rewriteEncodedPorts (NetworkModule enclosingModule, mxICell cell) {
      Object value = cell.getValue();
      if (value instanceof EncodedPort) {
         EncodedPort encodedPort = (EncodedPort) value;
         NetworkModule.Port port = enclosingModule.adoptCellForPort(cell, encodedPort.isInput(), encodedPort.getName());
         // we would normally do the following to announce the change of label for the port cell:
         // program.getGraphComponent().labelChanged(cell, port, null);
         // but our EncodedPort happens to have the same toString()-based label representation as the real Port instance
         // so we cheat and do not force an update now
      }
      else if (value instanceof LanguageEntity) {
         ((LanguageEntity) value).setOwningCell((mxCell) cell);
         // FIXME can the owningCell of a LanguageEntity be converted to type mxICell??
         if (value instanceof NetworkModule) {
            enclosingModule = (NetworkModule) value;
            enclosingModule.reviveAfterLoading(cell);
         }
      }
      int count = cell.getChildCount();
      for (int i = 0; i < count; i++)
         rewriteEncodedPorts(enclosingModule, cell.getChildAt(i));
   }

   public void announceEncodeDecodeDone (boolean encodeDone) {
      if (!encodeDone) {
         // after decode: need to replace all encoded ports with the real instances
         rewriteEncodedPorts(null, (mxICell) program.getGraphModel().getRoot());
      }
   }

   @Override
   public String getName () { return "ncc_port"; }

   @Override
   public Object beforeEncode (mxCodec enc, Object obj, Node node) {
//      System.err.println("ModulePortCodec.beforeEncode:" + obj + ", class=" + obj.getClass());
      // this codec is formally registered for EncodedPort, but such objects should never exist before this
      // transformation/rewriting method is called
      NetworkModule.Port port = (NetworkModule.Port) obj;
      return new EncodedPort(port.getDirection() == NetworkModule.Port.Direction.IN, port.getName());
   }

   // we replace this with the real port later in reviveAfterLoading after decoding everything
//   @Override
//   public Object afterDecode (mxCodec dec, Node node, Object obj) {
//      return obj;
//   }
}
