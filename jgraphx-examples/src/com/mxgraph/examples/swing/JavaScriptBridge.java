package com.mxgraph.examples.swing;

import com.leaningtech.client.Global;
import com.leaningtech.client.JSString;

public class JavaScriptBridge {

   private static String cachedToken = null;

   public static String getHBPToken () {
      if (cachedToken == null) {
         JSString result = Global.jsCallS("getHBPToken");
         cachedToken = result != null ? Global.JavaString(result) : null;
      }
      return cachedToken;
   }

   public void invalidateCache () {
      cachedToken = null;
   }
}
