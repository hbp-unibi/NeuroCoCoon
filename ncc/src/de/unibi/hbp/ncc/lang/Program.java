package de.unibi.hbp.ncc.lang;

import com.mxgraph.io.mxCodecRegistry;
import com.mxgraph.io.mxObjectCodec;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import de.unibi.hbp.ncc.NeuroCoCoonEditor;
import de.unibi.hbp.ncc.editor.EditorToolBar;
import de.unibi.hbp.ncc.editor.props.Notificator;
import de.unibi.hbp.ncc.env.NmpiClient;
import de.unibi.hbp.ncc.lang.codegen.CodeGenVisitor;
import de.unibi.hbp.ncc.lang.codegen.ErrorCollector;
import de.unibi.hbp.ncc.lang.codegen.ProgramVisitor;
import de.unibi.hbp.ncc.lang.props.DoubleProp;
import de.unibi.hbp.ncc.lang.props.EditableProp;
import de.unibi.hbp.ncc.lang.props.StrictlyPositiveDoubleProp;
import de.unibi.hbp.ncc.lang.props.StringProp;
import de.unibi.hbp.ncc.lang.serialize.LanguageEntityCodec;
import de.unibi.hbp.ncc.lang.serialize.ModulePortCodec;

import java.time.LocalDateTime;
import java.util.List;

public class Program extends LanguageEntity implements DisplayNamed, PythonNamed {
   private final StringProp programName;
   private final DoubleProp runTime, timeStep;
   private final Scope global;
   private mxIGraphModel graphModel;
   private mxGraphComponent graphComponent;
   private ErrorCollector lastDiagnostics;

   protected List<EditableProp<?>> addEditableProps (List<EditableProp<?>> list) {
      list.add(programName);
      list.add(runTime);
      list.add(timeStep);
      return list;
   }

   private static boolean codecsRegistered = false;

   private static void registerCodecs () {
      if (codecsRegistered)
         return;
      mxCodecRegistry.register(new LanguageEntityCodec(), "LanguageEntity");
      // we install our remapAllLanguageEntitySubclasses hook so that (concrete) subclasses need not be registered individually
      mxCodecRegistry.setClassNameRemapper(LanguageEntityCodec::remapAllLanguageEntitySubclasses);
      mxCodecRegistry.addPackage("de.unibi.hbp.ncc.lang");
      mxCodecRegistry.addPackage("de.unibi.hbp.ncc.lang.modules");
      mxObjectCodec portCodec = new ModulePortCodec();
      mxCodecRegistry.register(portCodec);
      // we could probably just pass "Port" as the pretended template class name,
      // but our afterDecode() really returns EncodedPort template instances, before they are rewritten in the end
      mxCodecRegistry.addAlias("Port", portCodec);
      codecsRegistered = true;
   }

   private static final String DEFAULT_PROG_NAME = "My Experiment";
   private static final double DEFAULT_TIME_STEP = 0.1;
   private static final double DEFAULT_RUN_TIME = 5000.0;

   public Program () {
      registerCodecs();
      global = new Scope();
      programName = new StringProp("Program Name", this, DEFAULT_PROG_NAME);
      timeStep = new StrictlyPositiveDoubleProp("Time Step", this, DEFAULT_TIME_STEP).setUnit("ms");
      runTime = new StrictlyPositiveDoubleProp("Run Time", this, DEFAULT_RUN_TIME).setUnit("ms");
      initialize();
   }

   private void initialize () {
      // must reuse the property objects, so that inspector does not show stale values
      programName.setValue(DEFAULT_PROG_NAME);
      timeStep.setValue(DEFAULT_TIME_STEP);
      runTime.setValue(DEFAULT_RUN_TIME);
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
      DataPlot.setGlobalNamespace(global.getDataPlots());
   }

   public void clear (EditorToolBar toolBar) {
      global.clear();
      initialize();
      Notificator.getInstance().notifyListeners(this);
      toolBar.setCurrentSynapseType(global.getSynapseTypes().getFallbackDefault());
   }

   public void setGraphComponent (mxGraphComponent graphComponent) {
      this.graphComponent = graphComponent;
      this.graphModel = graphComponent.getGraph().getModel();
   }

   public mxGraphComponent getGraphComponent () { return graphComponent; }
   public mxIGraphModel getGraphModel () { return graphModel; }

   public Scope getGlobalScope () {
      return global;
   }

   private static final int INITIAL_CAPACITY = 16 << 10;

   private CodeGenVisitor checkProgramInternal (ErrorCollector.Severity minSeverity,
                                                NmpiClient.Platform targetPlatform) {
      if (lastDiagnostics == null)
         lastDiagnostics = new ErrorCollector(graphComponent);
      else
         lastDiagnostics.reset();
      CodeGenVisitor visitor = new ProgramVisitor(this, targetPlatform);
      lastDiagnostics.setMinimumLevel(minSeverity);
      visitor.check(lastDiagnostics);
      return visitor;
   }

   public StringBuilder generatePythonCode (NmpiClient.Platform targetPlatform) {
      StringBuilder code = new StringBuilder(INITIAL_CAPACITY);
      code.append("# generated by NeuroCoCoon v" + NeuroCoCoonEditor.VERSION + " at ")
            .append(LocalDateTime.now())
            .append("\n\n");
      CodeGenVisitor visitor = checkProgramInternal(ErrorCollector.Severity.WARNING, targetPlatform);
      if (!lastDiagnostics.hasAnyErrors()) {
         visitor.visit(code, lastDiagnostics);
         if (!lastDiagnostics.hasAnyErrors())
            return code;
      }
      return null;
   }

   public ErrorCollector checkProgram (NmpiClient.Platform targetPlatform) {
      checkProgramInternal(ErrorCollector.Severity.NOTE, targetPlatform);
      return lastDiagnostics;
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
      return Namespace.buildUnadornedPythonName(programName.getValue());
   }

   public double getTimeStep () { return timeStep.getValue(); }
   public double getRunTime () { return runTime.getValue(); }
}
