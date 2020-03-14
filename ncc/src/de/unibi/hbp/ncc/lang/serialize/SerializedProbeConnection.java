package de.unibi.hbp.ncc.lang.serialize;

import de.unibi.hbp.ncc.lang.ProbeConnection;

import java.io.ObjectStreamException;
import java.io.Serializable;

// TODO factor out a common SerializedAnyConnection superclass for the label
public class SerializedProbeConnection extends SerializedAnyConnection implements Serializable {
   private ProbeConnection.DataSeries dataSeries;
   private int firstNeuronIndex, neuronCount;

   public SerializedProbeConnection (ProbeConnection.DataSeries dataSeries, int firstNeuronIndex, int neuronCount,
                                     String userLabel) {
      super(userLabel);
      this.dataSeries = dataSeries;
      this.firstNeuronIndex = firstNeuronIndex;
      this.neuronCount = neuronCount;
   }

   Object readResolve() throws ObjectStreamException {
      return new ProbeConnection(dataSeries, firstNeuronIndex, neuronCount, userLabel);
   }
}
