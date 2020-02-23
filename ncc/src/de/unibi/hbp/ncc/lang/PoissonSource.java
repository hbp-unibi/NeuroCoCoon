package de.unibi.hbp.ncc.lang;

import de.unibi.hbp.ncc.editor.EntityCreator;
import de.unibi.hbp.ncc.lang.props.DoubleProp;
import de.unibi.hbp.ncc.lang.props.EditableProp;
import de.unibi.hbp.ncc.lang.props.NonNegativeDoubleProp;
import de.unibi.hbp.ncc.lang.props.StrictlyPositiveDoubleProp;

import java.util.List;

public class PoissonSource extends NeuronPopulation {
   private DoubleProp rate, start, duration;

   @Override
   protected List<EditableProp<?>> addEditableProps (List<EditableProp<?>> list) {
      super.addEditableProps(list);
      list.add(rate);
      list.add(duration);
      list.add(start);
      return list;
   }

   @Override
   protected String getGeneratedNamesPrefix () { return "Poisson Source"; }

   public PoissonSource (Namespace<NeuronPopulation> namespace, String name, int neuronCount,
                         double rate, double start, double duration) {
      super(namespace, name, neuronCount);
      this.rate = new StrictlyPositiveDoubleProp("Rate", this, rate).setUnit("Hz");
      this.start = new NonNegativeDoubleProp("Start", this, start).setUnit("ms");
      this.duration = new StrictlyPositiveDoubleProp("Duration", this, duration).setUnit("ms");
   }

   public PoissonSource (Namespace<NeuronPopulation> namespace) {
      this(namespace, null, 1, 1.0, 0.0, 1.0e9);
   }

   public PoissonSource () { this(getGlobalNamespace()); }

   protected PoissonSource (PoissonSource orig) {
      this(orig.moreSpecificNamespace, orig.getCopiedName(), orig.getNeuronCountProp().getValue(),
           orig.rate.getValue(), orig.start.getValue(), orig.duration.getValue());
   }

   public static final EntityCreator<PoissonSource> CREATOR = new PoissonSource.Creator();

   private static class Creator implements EntityCreator<PoissonSource> {
      @Override
      public PoissonSource create () {
         return new PoissonSource();
      }

      @Override
      public String toString () {  // used by drag&drop tooltips
         return "Poisson Source";
      }

      @Override
      public String getIconFileName () { return "poissonsource.png"; }

      @Override
      public String getIconCaption () { return "Poisson"; }

      @Override
      public String getCellStyle () { return "poissonSource"; }

      @Override
      public int getInitialCellHeight () { return 80; }
   }

   @Override
   public PoissonSource duplicate () {
      return new PoissonSource(this);
   }

}
