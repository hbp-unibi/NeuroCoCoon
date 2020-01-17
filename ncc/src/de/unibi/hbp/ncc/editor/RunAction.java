package de.unibi.hbp.ncc.editor;

import com.eclipsesource.json.WriterConfig;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import de.unibi.hbp.ncc.env.JavaScriptBridge;
import de.unibi.hbp.ncc.env.NmpiClient;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

public class RunAction extends AbstractAction {

   private static int counter = 42;

   private static final String TOY_CODE =
         "import sys\n" +
               "print('Hello from NeuroCoCoon')\n" +
               "print(sys.version)\n";

   private static final String SAMPLE_CODE =
         "import numpy.random\n" +
         "from pyNN.utility import get_simulator\n" +
         "from pyNN.utility.plotting import Figure, Panel\n" +
         "\n" +
         "_usr_cnst_full = 0.015\n" +
         "_usr_cnst_half = 0.0075\n" +
         "_usr_cnst_inh_twice = -0.03\n" +
         "\n" +
         "neuron_params = {\n" +
         "    'e_rev_E': 0.0,\n" +
         "    'e_rev_I': -100.0,\n" +
         "    'tau_syn_E': 2.0,\n" +
         "    'tau_syn_I': 2.0,\n" +
         "    'v_rest': -70.0,\n" +
         "    'v_reset': -80.0,\n" +
         "    'v_thresh': -65.0,\n" +
         "    'tau_refrac': 2.0,\n" +
         "    'tau_m': 10,\n" +
         "    'cm': 0.2\n" +
         "}\n" +
         "\n" +
         "runtime = 300\n" +
         "\n" +
         "sim, options = get_simulator((\"--image-viewer\", \"Executable for displaying PNG images of plots\", {\"default\": '-'}))\n" +
         "\n" +
         "sim.setup(timestep=0.1)\n" +
         "\n" +
         "_usr_ana_source_x = sim.Population(1, sim.SpikeSourceArray(spike_times=[50, 150]))\n" +
         "_usr_ana_source_y = sim.Population(1, sim.SpikeSourceArray(spike_times=[100, 150]))\n" +
         "_usr_ana_pop_1 = sim.Population(1, sim.IF_cond_exp, neuron_params)\n" +
         "_usr_ana_pop_2 = sim.Population(1, sim.IF_cond_exp, neuron_params)\n" +
         "_usr_ana_output = sim.Population(1, sim.IF_cond_exp, neuron_params)\n" +
         "\n" +
         "one_to_one = sim.OneToOneConnector()\n" +
         "all_to_all = sim.AllToAllConnector()\n" +
         "\n" +
         "sim.Projection(_usr_ana_source_x, _usr_ana_pop_1, one_to_one, sim.StaticSynapse(weight=_usr_cnst_full))\n" +
         "sim.Projection(_usr_ana_source_y, _usr_ana_pop_2, one_to_one, sim.StaticSynapse(weight=_usr_cnst_half))\n" +
         "sim.Projection(_usr_ana_source_x, _usr_ana_pop_2, one_to_one, sim.StaticSynapse(weight=_usr_cnst_half))\n" +
         "sim.Projection(_usr_ana_source_y, _usr_ana_pop_1, one_to_one, sim.StaticSynapse(weight=_usr_cnst_full))\n" +
         "sim.Projection(_usr_ana_pop_1, _usr_ana_output, one_to_one, sim.StaticSynapse(weight=_usr_cnst_full))\n" +
         "sim.Projection(_usr_ana_pop_2, _usr_ana_output, one_to_one, sim.StaticSynapse(weight=_usr_cnst_inh_twice))\n" +
         "\n" +
         "_usr_ana_source_x.record('spikes')\n" +
         "_usr_ana_source_y.record('spikes')\n" +
         "_usr_ana_output.record('spikes')\n" +
         "_usr_ana_pop_1.record('v')\n" +
         "_usr_ana_pop_2.record('v')\n" +
         "\n" +
         "sim.run(runtime)\n" +
         "\n" +
         "from matplotlib.axes import Axes\n" +
         "\n" +
         "def fix_legends(pynn_fig):\n" +
         "   for panel in pynn_fig.fig.get_children():\n" +
         "      if isinstance(panel, Axes):\n" +
         "         gids = [ child.get_gid() for child in panel.get_children() if child.get_gid() ]\n" +
         "         if gids:\n" +
         "            panel.legend(gids[0].split(', '))\n" +
         "\n" +
         "\n" +
         "fig = Figure(\n" +
         "    Panel(\n" +
         "        _usr_ana_source_x.get_data().segments[0].spiketrains,\n" +
         "        xlabel=\"Time (ms)\", xticks=True,\n" +
         "        gid='source_x',\n" +
         "        line_properties= [{'color': 'b'}, ],\n" +
         "    ),\n" +
         "    Panel(\n" +
         "        _usr_ana_source_y.get_data().segments[0].spiketrains,\n" +
         "        xlabel=\"Time (ms)\", xticks=True,\n" +
         "        gid='source_y',\n" +
         "        line_properties= [{'color': 'b'}, ],\n" +
         "    ),\n" +
         "    Panel(\n" +
         "        _usr_ana_output.get_data().segments[0].spiketrains,\n" +
         "        xlabel=\"Time (ms)\", xticks=True,\n" +
         "        gid='output',\n" +
         "        line_properties= [{'color': 'b'}, ],\n" +
         "    ),\n" +
         "  title=\"XOR -- behavior\",\n" +
         "  annotations=\"Simulated with %s\" % options.simulator.upper()\n" +
         ")\n" +
         "fix_legends(fig)\n" +
         "fig.save(\"XOR_behavior.png\")\n" +
         "fig.fig.clear()\n" +
         "\n" +
         "fig = Figure(\n" +
         "    Panel(\n" +
         "        _usr_ana_pop_1.get_data().segments[0].filter(name='v')[0],\n" +
         "        xlabel=\"Time (ms)\", xticks=True,\n" +
         "        data_labels=['pop_1'],\n" +
         "        line_properties= [{'color': 'b'}, ],\n" +
         "    ),\n" +
         "    Panel(\n" +
         "        _usr_ana_pop_2.get_data().segments[0].filter(name='v')[0],\n" +
         "        xlabel=\"Time (ms)\", xticks=True,\n" +
         "        data_labels=['pop_2'],\n" +
         "        line_properties= [{'color': 'b'}, ],\n" +
         "    ),\n" +
         "  title=\"XOR -- hidden\",\n" +
         "  annotations=\"Simulated with %s\" % options.simulator.upper()\n" +
         ")\n" +
         "fix_legends(fig)\n" +
         "fig.save(\"XOR_hidden.png\")\n" +
         "fig.fig.clear()\n" +
         "\n" +
         "\n" +
         "if options.image_viewer != '-':\n" +
         "  import subprocess\n" +
         "  subprocess.run([options.image_viewer, \"XOR_behavior.png\", \"XOR_hidden.png\"])\n" +
         "\n" +
         "sim.end()\n";

   private static final int POLLING_INTERVAL = 5000;  // milliseconds

   private Timer trackJobStatus;

   @Override
   public void actionPerformed (ActionEvent e) {
      // JOptionPane.showMessageDialog(null, "RunAction Source: " + e.getSource());
      BasicGraphEditor editor = EditorActions.getEditor(e);
      // JOptionPane.showMessageDialog(null, "RunAction Editor: " + editor);
      if (editor != null && trackJobStatus == null) {
         mxGraphComponent graphComponent = editor.getGraphComponent();
         mxGraph graph = graphComponent.getGraph();
         // JOptionPane.showMessageDialog(null, "RunAction Graph: " + graph);

         // JOptionPane.showMessageDialog(null, "RunAction token: " + JavaScriptBridge.getHBPToken());

         final NmpiClient client = new NmpiClient();
         // JOptionPane.showMessageDialog(null, "RunAction userId: " + client.getUserId());
         long collabId = client.getCollabId();
         // JOptionPane.showMessageDialog(null, "RunAction collabId lastQuery: " + client.lastQuery);
         // JOptionPane.showMessageDialog(null, "RunAction collabId lastResponse: " + client.lastJobResponse);

         // String map = client.getResourceMap("ignored");
         // JOptionPane.showMessageDialog(null, "RunAction map: " + map);

         final EditorToolBar toolBar = editor.getEditorToolBar();
         toolBar.setJobStatus("Submitting …");
         final long jobId = client.submitJob(
               "# submitted " + (counter++) + " at " + new Date().toString() + "\n" +
                     SAMPLE_CODE, toolBar.getCurrentPlatform());
         final Object eventSource = e.getSource();
         // JOptionPane.showMessageDialog(null, "RunAction submitJob query: " + client.lastQuery);
         // JOptionPane.showMessageDialog(null, "RunAction submitJob response: " + client.lastJobResponse);
         if (jobId >= 0) {
            toolBar.setJobStatus("Running …");
            trackJobStatus =
                  new Timer(POLLING_INTERVAL,
                            new ActionListener() {
                               @Override
                               public void actionPerformed (ActionEvent e) {
                                  String status = client.getJobStatus(jobId);
                                  if ("finished".equals(status)) {
                                     toolBar.setJobStatus(EditorToolBar.StatusLevel.GOOD, "Finished");
                                     finishedJob();
                                  }
                                  else if ("error".equals(status)) {
                                     toolBar.setJobStatus(EditorToolBar.StatusLevel.BAD, "Error!");
                                     finishedJob();

                                  }
                                  // JOptionPane.showMessageDialog(null, "timer: " + status + "\n" + eventSource);
                               }
                            });
            setEnabled(false);
            trackJobStatus.start();
         }
         else
            toolBar.setJobStatus(EditorToolBar.StatusLevel.BAD, "Job submission failed! (" + jobId + ")");

         Object parent = graph.getDefaultParent();

         graph.getModel().beginUpdate();
         try {
            // graph.insertVertex(parent, null, userText, 20, 50, 100, 45);

            graph.insertVertex(parent, null, "Job #" + jobId, 20, 250, 360, 180);
         }
         finally {
            graph.getModel().endUpdate();
         }
         // JOptionPane.showMessageDialog(null, "RunAction jobInfo: " + client.getJobInfo(jobId).toString(WriterConfig.PRETTY_PRINT));
      }
   }

   private void finishedJob () {
      if (trackJobStatus != null) {
         trackJobStatus.stop();
         trackJobStatus = null;
      }
      setEnabled(true);
   }
}
