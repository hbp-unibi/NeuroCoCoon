package de.unibi.hbp.ncc.env;

import com.eclipsesource.json.JsonValue;
import com.leaningtech.client.Global;
import com.leaningtech.client.JSString;

public class JavaScriptBridge {

   private static String cachedToken = null;
   private static String cachedContext = null;

   public static String getHBPToken () {
      if (cachedToken == null) {
         JSString result = Global.jsCallS("getHBPToken");
         cachedToken = result != null ? Global.JavaString(result) : null;
      }
      return cachedToken;
   }

   public static String getHBPContext () {
      if (cachedContext == null) {
         JSString result = Global.jsCallS("getHBPContext");
         cachedContext = result != null ? Global.JavaString(result) : null;
      }
      return cachedContext;
   }

   public static void invalidateCache () { cachedToken = cachedContext = null; }

   public static String postRequest (String endPoint, JsonValue payload) {
      Global.jsCall("postHBPRequest", Global.JSString(endPoint), Global.JSString(payload.toString()));
      try {
         for (;;) {
            JSString result = Global.jsCallS("getHBPResponse");
            if (result != null)
               return Global.JavaString(result);
            Thread.sleep(250);
         }
      }
      catch (InterruptedException ie) {
         return null;
      }
   }
}
