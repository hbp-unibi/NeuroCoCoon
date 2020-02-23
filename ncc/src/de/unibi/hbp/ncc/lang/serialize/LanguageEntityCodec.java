package de.unibi.hbp.ncc.lang.serialize;

import com.mxgraph.io.mxCodec;
import com.mxgraph.io.mxObjectCodec;
import de.unibi.hbp.ncc.lang.LanguageEntity;
import org.w3c.dom.Node;

import java.util.Map;

public class LanguageEntityCodec extends mxObjectCodec {

   public LanguageEntityCodec (LanguageEntity template, String[] exclude, String[] idrefs,
                               Map<String, String> mapping) {
      super(template, exclude, idrefs, mapping);
   }

   public LanguageEntityCodec (LanguageEntity template) {
      super(template);
   }

   @Override
   public String getName () {
      return "nccle";
   }

   // FIXME why does this not traverse all declared non-transient fields by default?

   @Override
   public Object beforeEncode (mxCodec enc, Object obj, Node node) {
      return super.beforeEncode(enc, obj, node);
   }

   @Override
   public Node afterEncode (mxCodec enc, Object obj, Node node) {
      return super.afterEncode(enc, obj, node);
   }

   @Override
   public Node beforeDecode (mxCodec dec, Node node, Object obj) {
      return super.beforeDecode(dec, node, obj);
   }

   @Override
   public Object afterDecode (mxCodec dec, Node node, Object obj) {
      return super.afterDecode(dec, node, obj);
   }
}
