package de.unibi.hbp.ncc.lang.serialize;

import de.unibi.hbp.ncc.lang.ProbeConnection;

import java.io.ObjectStreamException;
import java.io.Serializable;

public class SerializedProbeConnection implements Serializable {
   private ProbeConnection.DataSeries dataSeries;

   public SerializedProbeConnection (ProbeConnection.DataSeries dataSeries) {
      this.dataSeries = dataSeries;
   }

   Object readResolve() throws ObjectStreamException {
      return new ProbeConnection(dataSeries);
   }
}
