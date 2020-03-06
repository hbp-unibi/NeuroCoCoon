package de.unibi.hbp.ncc.lang;

import java.util.Collection;

public interface PlotDataSource {
   // TODO add method(s) to retrieve valid plot data series

   Collection<ProbeConnection.DataSeries> validDataSeries ();
}
