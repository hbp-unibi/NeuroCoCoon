package de.unibi.hbp.ncc.lang.serialize;

import de.unibi.hbp.ncc.lang.AnyConnection;

public abstract class SerializedAnyConnection {
   protected String userLabel;
   protected AnyConnection.RoutingStyle routingStyle;

   protected SerializedAnyConnection (String userLabel, AnyConnection.RoutingStyle routingStyle) {
      this.userLabel = userLabel;
      this.routingStyle = routingStyle;
   }
}
