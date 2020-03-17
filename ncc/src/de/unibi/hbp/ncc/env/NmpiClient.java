package de.unibi.hbp.ncc.env;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.WriterConfig;
import de.unibi.hbp.ncc.lang.DisplayNamed;
import de.unibi.hbp.ncc.lang.utils.Iterators;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NmpiClient {

   private final static boolean DO_DISCONNECT = false;

   private static final String IDENTITY_SERVICE = "https://services.humanbrainproject.eu/idm/v1/api";
   private static final String COLLAB_SERVICE = "https://services.humanbrainproject.eu/collab/v0";
   private static final String JOB_SERVICE_ROOT = "https://nmpi.hbpneuromorphic.eu";
   private static final String JOB_SERVICE = JOB_SERVICE_ROOT + "/api/v2/";

   public enum Platform implements DisplayNamed {
      BRAINSCALES("BrainScaleS"), NEST("NEST"),
      SPIKEY("Spikey"), SPINNAKER("SpiNNaker"), SOURCE_CODE("Source only");

      private String pynnName;

      Platform (String pynnName) { this.pynnName = pynnName; }

      String getPynnName () { return pynnName; }

      @Override
      public String toString () {
         return getDisplayName();
      }  // for the combo box

      @Override
      public String getDisplayName () { return pynnName; }  // maybe make this distinct from the PyNN API name

      public boolean worksOutsideWebApp () { return this == NEST || this == SOURCE_CODE; }
      public boolean worksInWebApp () { return this != NEST; }
   }

   private String authToken;
   private long userId = -1;
   private long collabId = -1;
   private Map<String, String> resourceMap;

   public NmpiClient () {
      // everything is initialized lazily
   }

   private String getAuthToken () {
      if (authToken == null)
         authToken = JavaScriptBridge.getHBPToken();
      return authToken;
   }

   public static class Response {
      private JsonValue jsonValue;
      private String errorText;
      private int statusCode;
      private Throwable errorCause;
      private String locationHeader;

      Response (JsonValue jsonValue) { this.jsonValue = jsonValue; }

      Response (int statusCode) {
         this.statusCode = statusCode;
         if (statusCode < 200 || statusCode > 299)
            this.errorText = "Status " + statusCode;
      }

      Response (String errorText) { this.errorText = errorText != null ? errorText : "Error <null>"; }

      Response (Throwable errorCause) { this.errorCause = errorCause; }

      Response (String locationHeader, boolean isLocation) {
         assert isLocation;  // just to distinguish this from the error constructor
         this.locationHeader = locationHeader != null ? locationHeader : "<null location>";
      }

      public boolean isSuccess () { return errorText == null && errorCause == null; }
      public boolean isError () { return errorText != null || errorCause != null; }
      public boolean hasLocation () { return locationHeader != null; }

      public int getStatusCode () { return statusCode; }

      public String getLocation () { return locationHeader; }

      public JsonValue getJsonValue () { return jsonValue; }

      private static String convertToLines (StackTraceElement[] trace) {
         if (trace == null || trace.length < 1)
            return "<no stack trace>";
         List<String> lines = new ArrayList<>(trace.length);
         for (StackTraceElement entry: trace)
            lines.add(entry.toString());
         return String.join("\n", lines);
      }

      @Override
      public String toString () {
         /*
         System.err.println("errorText: " + errorText);
         System.err.println("locationHeader: " + locationHeader);
         System.err.println("jsonValue: " + jsonValue);
         System.err.println("errorCause: " + errorCause);
         if (true) return "[a Response]";
          */
         if (errorCause != null)
            return "[Exception] " + errorCause;
            // return "[" + errorCause.getClass().getTypeName() + "] " + errorCause + "\n" + convertToLines(errorCause.getStackTrace());
         if (errorText != null)
            return "[Error] " + errorText;
         if (locationHeader != null)
            return "[Location] " + locationHeader;
         if (jsonValue != null)
            return "[JSON] " + jsonValue.toString(WriterConfig.PRETTY_PRINT);
         return "[null]";
      }
   }

   public InputStream authorizedInput (URL url)
         throws IOException {
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Authorization", "Bearer " + getAuthToken());
      int statusCode = conn.getResponseCode();
      System.err.println("authorizedInput: " + url + ", status = " + statusCode);
      return conn.getInputStream();
   }

   private Response getRequest (String apiEndPoint) {
      try {
         URL url = new URL(apiEndPoint);
         HttpURLConnection conn = (HttpURLConnection) url.openConnection();
         conn.setRequestMethod("GET");
         conn.setRequestProperty("Accept", "application/json");
         conn.setRequestProperty("Authorization", "Bearer " + getAuthToken());
         int statusCode = conn.getResponseCode();
         if (statusCode != 200)
            return new Response(statusCode);
         try (InputStreamReader in = new InputStreamReader(conn.getInputStream())) {
            Response result = new Response(Json.parse(in));
            if (DO_DISCONNECT) conn.disconnect();
            return result;
         }
      }
      catch (Exception excp) {
         return new Response(excp);
      }
   }

   private Response postRequest (String apiEndPoint, JsonValue payload) {
      try {
         URL url = new URL(apiEndPoint);
         HttpURLConnection conn = (HttpURLConnection) url.openConnection();
         conn.setRequestMethod("POST");
         conn.setRequestProperty("Authorization", "Bearer " + getAuthToken());
         conn.setDoOutput(true);
         conn.setDoInput(true);
         // conn.setUseCaches(false);
         // conn.setInstanceFollowRedirects(false);
         // conn.setFixedLengthStreamingMode();
         conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
         // conn.connect();
         try (Writer out = new OutputStreamWriter(conn.getOutputStream())) {
            payload.writeTo(out);
            out.flush();
         }
         // conn.connect();
         int status = conn.getResponseCode();
         if (status == HttpURLConnection.HTTP_OK) {
            try (Reader in = new InputStreamReader(conn.getInputStream())) {
               Response result = new Response(Json.parse(in));
               if (DO_DISCONNECT) conn.disconnect();
               return result;
            }
         }
         else if (status == HttpURLConnection.HTTP_CREATED) {
            String locationHeader = conn.getHeaderField("Location");
            return new Response(locationHeader, true);
            // return new Response(conn.getHeaderField("Location"), true);
         }
         else {
            InputStream errorStream = conn.getErrorStream();
            if (errorStream != null) {
               try (BufferedReader err = new BufferedReader(new InputStreamReader(errorStream))) {
                  StringBuilder errorText = new StringBuilder();
                  String line;
                  while ((line = err.readLine()) != null)
                     errorText.append(line).append('\n');
                  // if (DO_DISCONNECT) conn.disconnect();  // never in this error case
                  return new Response(errorText.toString());
               }
            }
            else
               return new Response(status);
         }
      }
      catch (Exception excp) {
         return new Response(excp);
      }
   }

   private String fetchUserInfoLowLevel () {
      try {
         URL url = new URL(IDENTITY_SERVICE + "/user/me");
         HttpURLConnection conn = (HttpURLConnection) url.openConnection();
         conn.setRequestMethod("GET");
         conn.setRequestProperty("Accept", "application/json");
         conn.setRequestProperty("Authorization", "Bearer " + getAuthToken());
         if (conn.getResponseCode() != 200) {
            return "Failed : HTTP Error code : " + conn.getResponseCode();
         }
         InputStreamReader in = new InputStreamReader(conn.getInputStream());
         BufferedReader br = new BufferedReader(in);
         StringBuilder result = new StringBuilder();
         String line;
         while ((line = br.readLine()) != null) {
            result.append(line).append('\n');
         }
         if (DO_DISCONNECT) conn.disconnect();
         return result.toString();
      }
      catch (Exception e) {
         return "Exception in fetchUserInfoLowLevel: " + e;
      }
   }

   public long getUserId () {
      if (userId == -1) {
         Response result = getRequest(IDENTITY_SERVICE + "/user/me");
         if (result.isSuccess()) {
            JsonValue userInfo = result.getJsonValue();
            userId = Long.parseLong(userInfo.asObject().getString("id", "-1"));
         }
      }
      return userId;
      // return getRequest(IDENTITY_SERVICE + "/user/me").toString();
   }

   public long getCollabId () {
      if (collabId == -1) {
         String context = JavaScriptBridge.getHBPContext();
         // https://services.humanbrainproject.eu/collab/v0/collab/context/' + ctx + '/'
         Response result = getRequest(COLLAB_SERVICE + "/collab/context/" + context + "/");
         if (result.isSuccess()) {
            JsonValue contextInfo = result.getJsonValue();
            collabId = contextInfo.asObject().get("collab").asObject().getLong("id", -1);
         }
      }
      return collabId;
   }

   public String getResourceEndPoint (String key) {
      if (resourceMap == null) {
         Response result = getRequest(JOB_SERVICE);
         if (result.isSuccess()) {
            resourceMap = new HashMap<>();
            JsonObject mappingInfo = result.getJsonValue().asObject();
            for (JsonObject.Member member: mappingInfo)
               resourceMap.put(member.getName(), member.getValue().asObject().getString("list_endpoint", "/unknown"));
            // return result.getJsonValue().toString(WriterConfig.PRETTY_PRINT);  // FIXME for inspecting the result only
         }
         else
            return "/failed/" + result;
      }
      return JOB_SERVICE_ROOT + resourceMap.get(key);
   }

   private long extractJobId (String jobURL) {
      int lastSlash = jobURL.lastIndexOf('/');
      if (lastSlash >= 0)
         jobURL = jobURL.substring(lastSlash + 1);
      return Long.parseLong(jobURL);
      // TODO make this more robust against slash at the very end and non-numeric chars in last part
   }

   public List<Long> getJobIds (boolean completedJobs) {
      String apiEndPoint;
      if (completedJobs)
         apiEndPoint = getResourceEndPoint("results") + "?collab_id=" + getCollabId();
      else
         apiEndPoint = getResourceEndPoint("queue") + "/submitted/?user_id=" + getUserId();
      Response result = getRequest(apiEndPoint);
      if (result.isSuccess()) {
         List<Long> jobIds = new ArrayList<>();
         JsonArray jobInfos = result.getJsonValue().asObject().get("objects").asArray();
         for (JsonValue jobInfo: jobInfos) {
            String jobURL = jobInfo.asObject().getString("resource_uri", "-1");
            jobIds.add(extractJobId(jobURL));
         }
         System.err.println("getJobIds: " + jobIds);
         return jobIds;
      }
      return Collections.emptyList();
   }

   public long submitJob (String code, Platform platform) {

      JsonObject jobData = new JsonObject();
      jobData.add("command", "run.py {system}");
      jobData.add("hardware_platform", platform.getPynnName());
      jobData.add("collab_id", getCollabId());
      jobData.add("user_id", String.valueOf(getUserId()));

      jobData.add("code", code);
      // TODO handle input_data
      // TODO handle hardware_config
      // TODO handle tags?
      // String jobURL = postRequest(getResourceEndPoint("queue"), jobData).getLocation();
      // our Java-based postRequest method submits the very same job multiple (4?) times for some reason
      String jobURL = JavaScriptBridge.postRequest(getResourceEndPoint("queue"), jobData);
      if (jobURL.startsWith("Status 2")) {
         // request succeeded, but CORS prevented us from reading the location response header
         List<Long> jobIds = getJobIds(false);
         if (jobIds.isEmpty())
            jobIds = getJobIds(true);
         int count = jobIds.size();
         if (count > 0)
            return jobIds.get(count - 1);  // or better get the maximum value?
         else
            return -2;
      }
      else if (jobURL.startsWith("Status "))
         return -3;
      return extractJobId(jobURL);
   }

   public static class JobInfo {
      private final JsonObject jobInfo;

      JobInfo (JsonObject jobInfo) { this.jobInfo = jobInfo; }

      public String getJobStatus () {
         return jobInfo.getString("status", "unknown");
      }

      public JsonArray getOutputData () { return jobInfo.get("output_data").asArray(); }

      public Iterable<String> getOutputURLs () {
         return Iterators.partialMap(getOutputData(),
                                     jv -> jv.asObject().getString("url", null));
      }
   }

   public String getJobLog (long jobId) {
      Response result = getRequest(getResourceEndPoint("log") + "/" + jobId);
      if (result.isError())
         return null;
      JsonObject jobLog = result.getJsonValue().asObject();
      return jobLog.getString("content", "No log content!");
   }

   public JobInfo getJobInfo (long jobId) {
      Response result = getRequest(getResourceEndPoint("queue") + "/" + jobId);
      if (result.isError() && result.getStatusCode() == 404)
         result = getRequest(getResourceEndPoint("results") + "/" + jobId);
      if (result.isError())
         throw new RuntimeException("No such job: " + jobId);
      JsonObject jobInfo = result.getJsonValue().asObject();
      if (jobInfo.getLong("id", -1) != jobId)
         throw new IllegalStateException("unexpected job info for id " + jobInfo.get("id"));
      return new JobInfo(jobInfo);
   }

}
