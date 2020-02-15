package de.unibi.hbp.ncc.lang;

import de.unibi.hbp.ncc.lang.props.DoubleProp;
import de.unibi.hbp.ncc.lang.props.EditableEnumProp;
import de.unibi.hbp.ncc.lang.props.EditableProp;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class NeuronType extends NamedEntity<NeuronType> {

   public enum NeuronKind implements DisplayNamed {
      IF_COND_EXP("IF Cond Exp", true),
      IF_COND_ALPHA("IF Cond Alpha", true),
      IZHIKEVICH("Izhikevich", false);

      private String displayName;
      private boolean conductanceBased;

      NeuronKind (String displayName, boolean conductanceBased) {
         this.displayName = displayName;
         this.conductanceBased = conductanceBased;
      }

      @Override
      public String getDisplayName () { return displayName; }

      public boolean isConductanceBased () { return conductanceBased; }

      void addKindSpecificProps (List<EditableProp<?>> list, NeuronType type) {
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
   }

   private EditableEnumProp<NeuronKind> neuronKind;
   private DoubleProp eRevE, eRevI, tauSynE, tauSynI, vRest, vReset, vThresh, tauRefrac, tauM, cm, iOffset;
   private DoubleProp izhikevichA, izhikevichB, izhikevichC, izhikevichD;

   public NeuronType (Namespace<NeuronType> namespace, String name, NeuronKind neuronKind,
                      double vRest, double vReset, double vThresh, double eRevE, double eRevI,
                      double tauSynE, double tauSynI, double tauRefrac, double tauM, double cm,
                      double iOffset,
                      double izhikevichA, double izhikevichB, double izhikevichC, double izhikevichD) {
      super(namespace, name);
      this.neuronKind = new EditableEnumProp<>("Neuron Kind", NeuronKind.class, this, neuronKind)
            .setImpact(EnumSet.of(EditableProp.Impact.OTHER_PROPS_VISIBILITY));
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

   public NeuronType (Namespace<NeuronType> namespace, String name) {
      this(namespace, name, NeuronKind.IF_COND_EXP,
           -70.0, -80.0, -65.0, 0.0, -100.0,
           2.0, 2.0, 2.0, 10.0, 0.2,
           0.0,
           0.02, 0.2, -65.0, 2.0);
   }

   public NeuronType (NeuronType orig) {
      this(orig.getNamespace(), orig.getCopiedName(), orig.neuronKind.getValue(),
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

}
