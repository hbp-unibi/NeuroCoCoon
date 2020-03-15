package de.unibi.hbp.ncc.lang.codegen;

import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import de.unibi.hbp.ncc.env.NmpiClient;
import de.unibi.hbp.ncc.graph.AbstractCellsVisitor;
import de.unibi.hbp.ncc.lang.Connectable;
import de.unibi.hbp.ncc.lang.DataPlot;
import de.unibi.hbp.ncc.lang.LanguageEntity;
import de.unibi.hbp.ncc.lang.NetworkModule;
import de.unibi.hbp.ncc.lang.NeuronConnection;
import de.unibi.hbp.ncc.lang.NeuronPopulation;
import de.unibi.hbp.ncc.lang.ProbeConnection;
import de.unibi.hbp.ncc.lang.Program;
import de.unibi.hbp.ncc.lang.Scope;
import de.unibi.hbp.ncc.lang.SynapseType;
import de.unibi.hbp.ncc.lang.utils.Iterators;
import org.stringtemplate.v4.AutoIndentWriter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;
import org.stringtemplate.v4.STWriter;

import java.util.Locale;
import java.util.Objects;

public class ProgramVisitor implements CodeGenVisitor {
   private final Program program;
   private final Scope global;
   private final mxIGraphModel graphModel;
   private NmpiClient.Platform targetPlatform;
   private final STGroupFile templateGroup;

   public ProgramVisitor (Program program, NmpiClient.Platform targetPlatform) {
      this.program = program;
      this.global = program.getGlobalScope();
      this.graphModel = program.getGraphModel();
      this.targetPlatform = targetPlatform;
      templateGroup = new STGroupFile(Objects.requireNonNull(
            getClass().getClassLoader().getResource("de/unibi/hbp/ncc/resources/python.stg")));
      for (NetworkModule moduleInstance: global.getOneModuleInstancePerUsedClass()) {
         STGroupFile moduleTemplateGroup = new STGroupFile(Objects.requireNonNull(
               getClass().getClassLoader().getResource("de/unibi/hbp/ncc/lang/modules/" +
                                                             moduleInstance.getTemplateGroupFileName())));
         templateGroup.importTemplates(moduleTemplateGroup);
      }
   }

   // TODO provide global set of referenced entities and iterators limited to the referenced entities only
   // TODO how much effort to spend on pruning referenced but unreachable from graph nodes entities?

   @Override
   public void check (ErrorCollector diagnostics) {
      AbstractCellsVisitor edgeChecker = new AbstractCellsVisitor(true) {
         @Override
         protected void beginEntityVertex (mxICell vertex, LanguageEntity entity) {
            if (entity instanceof NeuronPopulation) {
               NeuronPopulation pop = (NeuronPopulation) entity;
               if (!pop.hasAnyOutgoingSynapses() && !pop.hasAnyIncomingProbes())
                  diagnostics.recordWarning(entity, pop.getLongDisplayName() +
                        " contributes neither to the network nor to any plot.");
               if (pop.isValidTarget(Connectable.EdgeKind.SYNAPSE) && !pop.hasAnyIncomingSynapses())
                  diagnostics.recordWarning(entity, pop.getLongDisplayName() +
                        " does not receive any input from the network.");
            }
            if (entity instanceof NetworkModule) {
               NetworkModule module = (NetworkModule) entity;
               module.checkStaticSemantics(program, diagnostics);
            }
            if (entity instanceof DataPlot) {
               DataPlot plot = (DataPlot) entity;
               if (!plot.hasAnyOutgoingProbes())
                  diagnostics.recordWarning(entity, plot.getLongDisplayName() + " will not visualise any data.");
            }
         }

         @Override
         protected void beginPortVertex (mxICell vertex, NetworkModule.Port port) {
            NetworkModule module = port.getOwningModule();
            if (!module.isOptionalPort(port)) {
               if (port.getDirection() == NetworkModule.Port.Direction.IN) {
                  if (!port.hasAnyIncomingSynapses())
                     diagnostics.recordError(module, port.getLongDisplayName() + " requires synapse input.");
               }
               else {  // unused non-optional output ports are only a warning; optional output ports are only intended for debugging or deeper analysis and never produce a warning
                  if (!port.hasAnyOutgoingSynapses() && !port.hasAnyIncomingProbes())
                     diagnostics.recordWarning(module, port.getLongDisplayName() +
                           " contributes neither to the network nor to any plot.");
               }
            }
         }

         @Override
         protected void visitOutgoingEdge (mxICell edge, mxICell source, mxICell target, LanguageEntity entity) {
            // visit each normal edge once only (just after the source vertex has been visited)
            if (entity instanceof NeuronConnection) {
               NeuronConnection connection = (NeuronConnection) entity;
               SynapseType synapseType = connection.getSynapseType();
               if (synapseType.getWeight() == 0)
                  diagnostics.recordWarning(connection, "Synapse with weight zero has no effect.");
               if (synapseType.getDelay() < program.getTimeStep())
                  diagnostics.recordWarning(connection, "Delay is smaller than time step.");
               if (synapseType.getConnectorKind() == SynapseType.ConnectorKind.ONE_TO_ONE) {
                  Integer sourceCount = AttributeUtils.getNeuronCount(source);
                  Integer targetCount = AttributeUtils.getNeuronCount(target);
                  if (AttributeUtils.definitelyNotEqual(sourceCount, targetCount))
                     diagnostics.recordError(entity, "Mismatch in neuron count: " +
                           sourceCount + " != " + targetCount);
               }
               if (AttributeUtils.definitelyNotEqual(
                     AttributeUtils.isConductanceBased(source), AttributeUtils.isConductanceBased(target)))
                  diagnostics.recordError(entity, "Connection between conductance-based and current-based neurons.");
            }
            else if (entity instanceof ProbeConnection) {
               ProbeConnection connection = (ProbeConnection) entity;
               if (!((ProbeConnection) entity).hasValidDataSeries())
                  diagnostics.recordError(entity, AttributeUtils.display(target.getValue()) +
                        " does not provide data series " + connection.getDataSeries().getDisplayName() + ".");
               Integer targetNeuronCount = AttributeUtils.getNeuronCount(target);
               if (targetNeuronCount != null) {
                  int limit = targetNeuronCount;
                  int firstIndex = connection.getFirstNeuronIndex();
                  if (firstIndex < 0 || firstIndex >= limit)
                     diagnostics.recordError(entity, "Neuron start index " + firstIndex + " is out of range [0, " + limit + ").");
                  if (connection.isEndLimited()) {
                     int probeNeuronCount = connection.getNeuronCount();
                     if (probeNeuronCount < 1)
                        diagnostics.recordError(entity, "Invalid neuron count " + probeNeuronCount + " at probe.");
                     if (firstIndex + probeNeuronCount > limit)
                        diagnostics.recordError(entity, "Neuron count " + probeNeuronCount +
                              " at probe with start index " + firstIndex + " exceeds target neuron range [0, " + limit + ").");
                  }
               }
            }
            else
               throw new IllegalArgumentException("unexpected entity at edge: " + entity);
         }

         @Override
         protected void visitLoopEdge (mxICell edge, mxICell sourceAndTarget, LanguageEntity entity) {
            if (entity instanceof NeuronConnection) {
               NeuronConnection connection = (NeuronConnection) entity;
               SynapseType synapseType = connection.getSynapseType();
               if (synapseType.getWeight() == 0)
                  diagnostics.recordWarning(connection, "Self connection with weight zero has no effect.");
               if (synapseType.getDelay() < program.getTimeStep())
                  diagnostics.recordWarning(connection, "Self connection with delay smaller than time step.");
            }
         }

         @Override
         protected void visitDanglingEdge (mxICell edge, mxICell soleConnectedCell, LanguageEntity entity,
                                           boolean isIncoming) {
            if (entity instanceof NeuronConnection) {
               diagnostics.recordError(entity, isIncoming
                     ? "Synapse is not connected to a source!"
                     : "Synapse is not connected to a target!");
            }
            else if (entity instanceof ProbeConnection) {
               diagnostics.recordError(entity, isIncoming
                     ? "Probe is not connected to a data plot!"
                     : "Probe is not connected to a subject!");
            }
            else
               throw new IllegalArgumentException("unexpected entity at edge: " + entity);
         }

         @Override
         protected void visitUnconnectedEdge (mxICell edge, LanguageEntity entity) {
            if (entity instanceof NeuronConnection) {
               diagnostics.recordError(entity, "Synapse is not connected!");
            }
            else if (entity instanceof ProbeConnection) {
               diagnostics.recordError(entity, "Probe is not connected!");
            }
            else
               throw new IllegalArgumentException("unexpected entity at edge: " + entity);
         }
      };
      edgeChecker.visitGraph(graphModel);
      if (global.getDataPlots().isEmpty())
         diagnostics.recordInfo(null, "No data from the network will be plotted.");
      if (Iterators.isEmpty(global.getSpikeSources()) && Iterators.isEmpty(global.getPoissonSources()))
         diagnostics.recordInfo(null, "No spikes will be injected into the network.");
   }

   public StringBuilder visit (StringBuilder code, ErrorCollector diagnostics) {
      ST st = templateGroup.getInstanceOf("program");
      st.add("prog", program);
      st.add("scope", global);
      st.add("target", targetPlatform);

      STWriter wr = new AutoIndentWriter(new BufferAppender(code));
      // wr.setLineWidth(STWriter.NO_WRAP);
      wr.setLineWidth(72);
      st.write(wr, Locale.getDefault(), diagnostics);
      return code;
   }

}
