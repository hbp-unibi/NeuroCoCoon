package de.unibi.hbp.ncc.env;

import com.eclipsesource.json.JsonValue;
import com.leaningtech.client.Document;
import com.leaningtech.client.Element;
import com.leaningtech.client.Global;
import com.leaningtech.client.HTMLImageElement;
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

   // POST requests via standard Java code trigger multiple job submissions per call?!
   // delegating the actual POST request to a global JavaScript function avoids this
   public static String postRequest (String endPoint, JsonValue payload) {
      Global.jsCall("postHBPRequest", Global.JSString(endPoint), Global.JSString(payload.toString()));
      try {
         for (;;) {
            JSString result = Global.jsCallS("getHBPResponse");
            if (result != null)
               return Global.JavaString(result);
            Thread.sleep(500);  // was 250
         }
      }
      catch (InterruptedException ie) {
         return "Status 999";  // fake a real response similar to a HTTP status code
      }
   }

   public static void showOnPage (String url, String targetId) {
      Document d = Global.document;
      Element e = d.getElementById(Global.JSString(targetId));
      if (!(e instanceof HTMLImageElement))
         throw new IllegalArgumentException("no <img> element found for id " + targetId);
      HTMLImageElement img = (HTMLImageElement) e;
      img.set_src(Global.JSString(url));
   }

   /*
   import com.leaningtech.client.Document;
import com.leaningtech.client.Element;
import com.leaningtech.client.Global;
import com.leaningtech.client.JSString;

public class DomExample
{
        public static void main(String[] a)
        {
                // Retrieve the global document object, it comes from the global namespace of the browser.
                Document d = Global.document;
                // Retries a known element from the page using it's id
                // NOTE: Java Strings must be converted to JavaScript string before being used
                Element e = d.getElementById(Global.JSString("existingNode"));
                // Currently, setter/getters must be used to access properties
                e.set_textContent(Global.JSString("sometext"));
                Element newDiv = Global.document.createElement(Global.JSString("p"));
                // Initialize the new element with text derived from the previous one
                newDiv.set_textContent(e.get_textContent().substring(3).toUpperCase())
                // Add it to the document body
                Global.document.get_body().appendChild(newDiv);
                JSString divContent = newDiv.get_textContent();
                // This logs directly to the browser console
                Global.console.log(divContent);
        }
}
    */
   public static String getRequest (String endPoint) {
      Global.jsCall("getHBPRequest", Global.JSString(endPoint));
      return null; // TODO return the real result
   }

   private static Boolean cachedWebPlatform = null;

   public static boolean isWebPlatform () {
      if (cachedWebPlatform == null)
         cachedWebPlatform = System.getProperty("java.vendor", "?").startsWith(("Leaning Technologies"));
      return cachedWebPlatform;
   }
}
