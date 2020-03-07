package de.unibi.hbp.ncc.lang.serialize;

import de.unibi.hbp.ncc.lang.ProbeConnection;

import java.io.ObjectStreamException;
import java.io.Serializable;

public class SerializedProbeConnection implements Serializable {
   private ProbeConnection.DataSeries dataSeries;
   private int firstNeuronIndex, neuronCount;

   public SerializedProbeConnection (ProbeConnection.DataSeries dataSeries, int firstNeuronIndex, int neuronCount) {
      this.dataSeries = dataSeries;
      this.firstNeuronIndex = firstNeuronIndex;
      this.neuronCount = neuronCount;
   }

   Object readResolve() throws ObjectStreamException {
      return new ProbeConnection(dataSeries, firstNeuronIndex, neuronCount);
   }
}
