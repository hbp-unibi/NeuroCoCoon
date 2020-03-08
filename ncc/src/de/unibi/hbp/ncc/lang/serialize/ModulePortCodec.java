package de.unibi.hbp.ncc.lang.serialize;

import com.mxgraph.io.mxCodec;
import com.mxgraph.io.mxObjectCodec;
import de.unibi.hbp.ncc.lang.NetworkModule;
import org.w3c.dom.Node;

public class ModulePortCodec extends mxObjectCodec {

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

   public ModulePortCodec () {
      super(new EncodedPort());  // decoding starts with a clone of this empty port description
      String myName = getName();
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
