package de.unibi.hbp.ncc.lang.codegen;

import com.mxgraph.model.mxIGraphModel;
import de.unibi.hbp.ncc.lang.Program;
import de.unibi.hbp.ncc.lang.Scope;
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
   private final STGroupFile templateGroup;

   public ProgramVisitor (Program program) {
      this.program = program;
      this.global = program.getGlobalScope();
      this.graphModel = program.getGraphModel();
      templateGroup = new STGroupFile(Objects.requireNonNull(
            getClass().getClassLoader()
                  .getResource("de/unibi/hbp/ncc/resources/python.stg")));
   }

   @Override
   public void check (ErrorCollector diagnostics) {

   }

   public StringBuilder visit (StringBuilder code, ErrorCollector diagnostics) {
      ST st = templateGroup.getInstanceOf("program");
      st.add("prog", program);
      st.add("pops", global.getNeuronPopulations());

      STWriter wr = new AutoIndentWriter(new BufferAppender(code));
      wr.setLineWidth(STWriter.NO_WRAP);
      st.write(wr, Locale.getDefault());
      return code;
   }

}
