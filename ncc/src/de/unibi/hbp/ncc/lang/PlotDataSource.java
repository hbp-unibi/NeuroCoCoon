package de.unibi.hbp.ncc.lang;

import java.util.Collection;

public interface PlotDataSource {
   Collection<ProbeConnection.DataSeries> getSupportedDataSeries ();  // capabilities of the probe target (PyNN recordable)
   Collection<ProbeConnection.DataSeries> getRequiredDataSeries ();  // union over all incoming probes
}
