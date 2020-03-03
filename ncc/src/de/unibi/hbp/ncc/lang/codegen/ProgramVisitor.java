package de.unibi.hbp.ncc.lang.codegen;

import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import de.unibi.hbp.ncc.graph.AbstractCellsVisitor;
import de.unibi.hbp.ncc.lang.LanguageEntity;
import de.unibi.hbp.ncc.lang.NeuronConnection;
import de.unibi.hbp.ncc.lang.Program;
import de.unibi.hbp.ncc.lang.Scope;
import de.unibi.hbp.ncc.lang.SynapseType;
import org.stringtemplate.v4.AutoIndentWriter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;
import org.stringtemplate.v4.STWriter;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class ProgramVisitor implements CodeGenVisitor {
   private final Program program;
   private final Scope global;
   private final mxIGraphModel graphModel;
   private final STGroupFile templateGroup;

   public ProgramVisitor (Program program) {
      this.program = program;
      this.global = program.getGlobalScope();
      this.graphModel = program.getGraphModel();
      templateGroup = new STGroupFile(Objects.requireNonNull(
            getClass().getClassLoader().getResource("de/unibi/hbp/ncc/resources/python.stg")));
   }

   // TODO provide global set of referenced entities and iterators limited to the referenced entities only
   // TODO how much effort to spend on pruning referenced but unreachable from graph nodes entities?

   @Override
   public void check (ErrorCollector diagnostics) {
      AbstractCellsVisitor edgeChecker = new AbstractCellsVisitor(true) {
         @Override
         protected void beginVertex (mxICell vertex, LanguageEntity entity) {
            // TODO check unused spike/poisson sources --> Warning
            // TODO check unconnected standard populations --> Warning
            // TODO check unconnected non-optional module ports --> Error
         }


         @Override
         protected void visitOutgoingEdge (mxICell edge, mxICell source, mxICell target, LanguageEntity entity) {
            // visit each normal edge once only (just after the source vertex has been visited)
            NeuronConnection connection = (NeuronConnection) entity;
            SynapseType synapseType = connection.getSynapseType();
            if (synapseType.getWeight() == 0)
               diagnostics.recordWarning(connection, "Synapse with weight zero has no effect.");
            if (synapseType.getConnectorKind() == SynapseType.ConnectorKind.ONE_TO_ONE) {
              Integer sourceCount = AttributeUtils.getNeuronCount(source);
               Integer targetCount = AttributeUtils.getNeuronCount(target);
               if (AttributeUtils.definitelyNotEqual(sourceCount, targetCount)) {
                  LanguageEntity errorEntity = connection.getTargetEntity();
                  if (errorEntity == null)
                     errorEntity = connection.getSourceEntity();
                  if (errorEntity == null)
                     errorEntity = entity;
                  diagnostics.recordError(errorEntity, "Mismatch in neuron count: " +
                        sourceCount + " != " + targetCount);
               }
            }
         }

         @Override
         protected void visitLoopEdge (mxICell edge, mxICell sourceAndTarget, LanguageEntity entity) {
            NeuronConnection connection = (NeuronConnection) entity;
            if (((NeuronConnection) entity).getSynapseType().getDelay() == 0)
               diagnostics.recordWarning(connection, "Self connection with zero delay.");
         }

         @Override
         protected void visitDanglingEdge (mxICell edge, mxICell soleConnectedCell, LanguageEntity entity,
                                           boolean isIncoming) {
            diagnostics.recordError(entity, isIncoming
                  ? "Synapse is not connected to a source!"
                  : "Synapse is not connected to a target!");
         }

         @Override
         protected void visitUnconnectedEdge (mxICell edge, LanguageEntity entity) {
            diagnostics.recordError(entity, "Synapse is not connected!");
         }
      };
      edgeChecker.visitGraph(graphModel);
   }

   public StringBuilder visit (StringBuilder code, ErrorCollector diagnostics) {
      ST st = templateGroup.getInstanceOf("program");
      st.add("prog", program);
      st.add("scope", global);

      STWriter wr = new AutoIndentWriter(new BufferAppender(code));
      // wr.setLineWidth(STWriter.NO_WRAP);
      wr.setLineWidth(72);
      st.write(wr, Locale.getDefault(), diagnostics);
      return code;
   }

}
