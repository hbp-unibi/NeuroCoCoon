package de.unibi.hbp.ncc.lang;

import de.unibi.hbp.ncc.lang.codegen.CodeGenUse;
import de.unibi.hbp.ncc.lang.props.DoubleProp;
import de.unibi.hbp.ncc.lang.props.EditableEnumProp;
import de.unibi.hbp.ncc.lang.props.EditableProp;
import de.unibi.hbp.ncc.lang.props.ReadOnlyProp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public class NeuronType extends NamedEntity {

   public enum NeuronKind implements DisplayNamed, PythonNamed {
      IF_COND_EXP("IF Cond Exp", "IF_cond_exp", true,
                  EnumSet.of(ProbeConnection.DataSeries.SPIKES, ProbeConnection.DataSeries.VOLTAGE,
                             ProbeConnection.DataSeries.GSYN_EXC, ProbeConnection.DataSeries.GSYN_INH)),
      IF_COND_ALPHA("IF Cond Alpha", "IF_cond_alpha", true,
                    EnumSet.of(ProbeConnection.DataSeries.SPIKES, ProbeConnection.DataSeries.VOLTAGE,
                               ProbeConnection.DataSeries.GSYN_EXC, ProbeConnection.DataSeries.GSYN_INH)),
      IZHIKEVICH("Izhikevich", "Izhikevich", false,
                 EnumSet.of(ProbeConnection.DataSeries.SPIKES, ProbeConnection.DataSeries.VOLTAGE,
                            ProbeConnection.DataSeries.IZHIKEVICH_U)),
      IF_CURR_EXP("IF Curr Exp", "IF_curr_exp", false,
                  EnumSet.of(ProbeConnection.DataSeries.SPIKES, ProbeConnection.DataSeries.VOLTAGE)),
      IF_CURR_ALPHA("IF Curr Alpha", "IF_curr_alpha", false,
                    EnumSet.of(ProbeConnection.DataSeries.SPIKES, ProbeConnection.DataSeries.VOLTAGE));

      private final String displayName, pythonName;
      private final boolean conductanceBased;
      private final EnumSet<ProbeConnection.DataSeries> supportedSeries;

      NeuronKind (String displayName, String pythonName, boolean conductanceBased,
                  EnumSet<ProbeConnection.DataSeries> supportedSeries) {
         this.displayName = displayName;
         this.pythonName = pythonName;
         this.conductanceBased = conductanceBased;
         this.supportedSeries = supportedSeries;
      }

      @Override
      public String getDisplayName () { return displayName; }


      boolean isConductanceBased () { return conductanceBased; }

      Collection<ProbeConnection.DataSeries> getSupportedDataSeries () { return supportedSeries; }

      void addKindSpecificProps (List<? super EditableProp<?>> list, NeuronType type) {
         if (this == IZHIKEVICH) {
            list.add(type.izhikevichA);
            list.add(type.izhikevichB);
            list.add(type.izhikevichC);
            list.add(type.izhikevichD);
         }
         else {
            list.add(type.vRest);
            list.add(type.vReset);
            list.add(type.vThresh);
            list.add(type.eRevE);
            list.add(type.eRevI);
            list.add(type.tauSynE);
            list.add(type.tauSynI);
            list.add(type.tauRefrac);
            list.add(type.tauM);
            list.add(type.cm);
         }
         list.add(type.iOffset);
      }

      @Override
      public String getPythonName () { return pythonName; }

   }

   private final Namespace<NeuronType> moreSpecificNamespace;

   private EditableEnumProp<NeuronKind> neuronKind;
   private DoubleProp eRevE, eRevI, tauSynE, tauSynI, vRest, vReset, vThresh, tauRefrac, tauM, cm, iOffset;
   private DoubleProp izhikevichA, izhikevichB, izhikevichC, izhikevichD;

   @Override
   protected String getGeneratedNamesPrefix () { return "Neuron Type"; }

   protected static Namespace<NeuronType> getGlobalNamespace () {
      return NeuronPopulation.getGlobalNamespace().getContainingScope().getNeuronTypes();
   }

   public NeuronType (Namespace<NeuronType> namespace, String name, NeuronKind neuronKind,
                      double vRest, double vReset, double vThresh, double eRevE, double eRevI,
                      double tauSynE, double tauSynI, double tauRefrac, double tauM, double cm,
                      double iOffset,
                      double izhikevichA, double izhikevichB, double izhikevichC, double izhikevichD) {
      super(namespace, name);
      moreSpecificNamespace = namespace;
      this.neuronKind = new EditableEnumProp<>("Neuron Kind", NeuronKind.class, this, neuronKind)
            .addImpact(EditableProp.Impact.OTHER_PROPS_VISIBILITY);
      this.vRest = new DoubleProp("v rest", this, vRest).setUnit("mV");
      this.vReset = new DoubleProp("v reset", this, vReset).setUnit("mV");
      this.vThresh = new DoubleProp("v thresh", this, vThresh).setUnit("mV");
      this.eRevE = new DoubleProp("e rev E", this, eRevE).setUnit("mV");
      this.eRevI = new DoubleProp("e rev I", this, eRevI).setUnit("mV");
      this.tauSynE = new DoubleProp("tau syn E", this, tauSynE).setUnit("ms");
      this.tauSynI = new DoubleProp("tau syn I", this, tauSynI).setUnit("ms");
      this.tauRefrac = new DoubleProp("tau refrac", this, tauRefrac).setUnit("ms");
      this.tauM = new DoubleProp("tau m", this, tauM).setUnit("ms");
      this.cm = new DoubleProp("cm", this, cm).setUnit("nF");
      this.iOffset = new DoubleProp("i offset", this, iOffset).setUnit("nA");
      this.izhikevichA = new DoubleProp("a", this, izhikevichA).setUnit("ms⁻¹");
      this.izhikevichB = new DoubleProp("b", this, izhikevichB).setUnit("ms⁻¹");
      this.izhikevichC = new DoubleProp("c", this, izhikevichC).setUnit("mV");
      this.izhikevichD = new DoubleProp("d", this, izhikevichD).setUnit("mV/ms");
   }

   public NeuronType (Namespace<NeuronType> namespace, String name, NeuronKind neuronKind,
                      double vRest, double vReset, double vThresh, double eRevE, double eRevI,
                      double tauSynE, double tauSynI, double tauRefrac, double tauM, double cm,
                      double iOffset) {
      this(namespace, name, neuronKind, vRest, vReset, vThresh, eRevE, eRevI,
           tauSynE, tauSynI, tauRefrac, tauM, cm,
           iOffset,
           0.02, 0.2, -65.0, 2.0);
   }

   public NeuronType (Namespace<NeuronType> namespace, String name) {
      // matches the PyNN default values
      this(namespace, name, NeuronKind.IF_COND_EXP,
           -65.0, -65.0, -50.0, 0.0, -70.0,
           5.0, 5.0, 0.1, 20.0, 1.0,
           0.0);
   }

   public NeuronType (String name) {
      this(getGlobalNamespace(), name);
   }
   public NeuronType (Namespace<NeuronType> namespace) { this(namespace, null); }
   public NeuronType () { this((String) null); }

   protected NeuronType (NeuronType orig) {
      this(orig.moreSpecificNamespace, orig.getCopiedName(), orig.neuronKind.getValue(),
           orig.vRest.getValue(), orig.vReset.getValue(), orig.vThresh.getValue(), orig.eRevE.getValue(), orig.eRevI.getValue(),
           orig.tauSynE.getValue(), orig.tauSynI.getValue(), orig.tauRefrac.getValue(), orig.tauM.getValue(), orig.cm.getValue(),
           orig.iOffset.getValue(),
           orig.izhikevichA.getValue(), orig.izhikevichB.getValue(), orig.izhikevichC.getValue(), orig.izhikevichD.getValue());
   }

   @Override
   protected List<EditableProp<?>> addEditableProps (List<EditableProp<?>> list) {
      super.addEditableProps(list);
      list.add(neuronKind);
      neuronKind.getValue().addKindSpecificProps(list, this);
      return list;
   }

   protected List<EditableProp<?>> addExportedEditableProps (List<EditableProp<?>> list) {
      list.add(neuronKind);
      neuronKind.getValue().addKindSpecificProps(list, this);
      return list;
   }

   // @Override
   public NeuronType duplicate () {
      return new NeuronType(this);
   }

   public NeuronKind getNeuronKind () { return neuronKind.getValue(); }

   public boolean isConductanceBased () { return getNeuronKind().isConductanceBased(); }

   public Collection<ProbeConnection.DataSeries> getSupportedDataSeries () {
      return getNeuronKind().getSupportedDataSeries();
   }

   @CodeGenUse
   public String getParametersPythonName () { return getDerivedPythonName("npm"); }

   @CodeGenUse
   public Iterable<ReadOnlyProp<?>> getNeuronParameters () {
      List<ReadOnlyProp<?>> result = new ArrayList<>();
      neuronKind.getValue().addKindSpecificProps(result, this);
      return result;
   }

}
