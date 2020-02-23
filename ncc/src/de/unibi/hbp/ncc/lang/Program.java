package de.unibi.hbp.ncc.lang;

import com.mxgraph.model.mxIGraphModel;
import de.unibi.hbp.ncc.NeuroCoCoonEditor;
import de.unibi.hbp.ncc.lang.codegen.CodeGenVisitor;
import de.unibi.hbp.ncc.lang.codegen.ErrorCollector;
import de.unibi.hbp.ncc.lang.codegen.ProgramVisitor;
import de.unibi.hbp.ncc.lang.props.DoubleProp;
import de.unibi.hbp.ncc.lang.props.EditableProp;
import de.unibi.hbp.ncc.lang.props.StrictlyPositiveDoubleProp;
import de.unibi.hbp.ncc.lang.props.StringProp;

import java.time.LocalDateTime;
import java.util.List;

public class Program extends LanguageEntity implements DisplayNamed, PythonNamed {
   private StringProp programName;
   private DoubleProp runTime, timeStep;
   private final Scope global;
   private mxIGraphModel graphModel;
   private ErrorCollector lastDiagnostics;

   protected List<EditableProp<?>> addEditableProps (List<EditableProp<?>> list) {
      list.add(programName);
      list.add(runTime);
      list.add(timeStep);
      return list;
   }

   public Program () {
      programName = new StringProp("Program Name", this, "My Experiment");
      timeStep = new StrictlyPositiveDoubleProp("Time Step", this, 0.1).setUnit("ms");
      runTime = new StrictlyPositiveDoubleProp("Run Time", this, 2000.0).setUnit("ms");
      global = new Scope();
      NeuronPopulation.setGlobalNamespace(global.getNeuronPopulations());
      final Namespace<SynapseType> synapseTypes = global.getSynapseTypes();
      NeuronConnection.setGlobalSynapseTypeNamespace(synapseTypes);
      NeuronType defNeuronType = new NeuronType(global.getNeuronTypes(), "Default");
      defNeuronType.makePredefined();
      SynapseType defAllSynapseType = new SynapseType(synapseTypes, "All Default", SynapseType.ConnectorKind.ALL_TO_ALL);
      defAllSynapseType.makePredefined();
      SynapseType defOneSynapseType = new SynapseType(synapseTypes, "One Default", SynapseType.ConnectorKind.ONE_TO_ONE);
      defOneSynapseType.makePredefined();
      SynapseType defProbSynapseType = new SynapseType(synapseTypes, "Prob Default", SynapseType.ConnectorKind.FIXED_PROBABILITY);
      defProbSynapseType.makePredefined();
      NetworkModule.setGlobalNamespace(global.getModuleInstances());
   }

   public void setGraphModel (mxIGraphModel graphModel) { this.graphModel = graphModel; }

   public mxIGraphModel getGraphModel () { return graphModel; }

   public Scope getGlobalScope () {
      return global;
   }

   private static final int INITIAL_CAPACITY = 16 << 10;

   public StringBuilder generatePythonCode () {
      StringBuilder code = new StringBuilder(INITIAL_CAPACITY);
      code.append("# generated by NeuroCoCoon v" + NeuroCoCoonEditor.VERSION + " at ")
            .append(LocalDateTime.now())
            .append("\n\n");
      lastDiagnostics = null;
      ErrorCollector diagnostics = new ErrorCollector();
      CodeGenVisitor visitor = new ProgramVisitor(this);
      visitor.check(diagnostics);
      if (!diagnostics.hasAnyErrors()) {
         visitor.visit(code, diagnostics);
         if (!diagnostics.hasAnyErrors())
            return code;
      }
      lastDiagnostics = diagnostics;
      return null;
   }

   public ErrorCollector getLastDiagnostics () {
      return lastDiagnostics;
   }

   @Override
   public LanguageEntity duplicate () {
      throw new UnsupportedOperationException();
      // return this;
   }

   @Override
   public String getDisplayName () { return programName.getValue(); }

   @Override
   public String getPythonName () {
      return Namespace.buildTopLevelPythonName(programName.getValue());
   }

   public double getTimeStep () { return timeStep.getValue(); }
}
