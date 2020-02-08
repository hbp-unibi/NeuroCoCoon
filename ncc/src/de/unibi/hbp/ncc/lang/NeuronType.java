package de.unibi.hbp.ncc.lang;

import de.unibi.hbp.ncc.lang.props.DoubleProp;
import de.unibi.hbp.ncc.lang.props.EditableEnumProp;
import de.unibi.hbp.ncc.lang.props.EditableProp;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class NeuronType extends NamedEntity<NeuronType> {

   public enum NeuronKind implements DisplayNamed {
      IF_COND_EXP("IF Cond Exp");

      private String displayName;

      NeuronKind (String displayName) {
         this.displayName = displayName;
      }

      @Override
      public String getDisplayName () { return displayName; }

      void addKindSpecificProps (List<EditableProp<?>> list, NeuronType type) {
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
   }

   private EditableEnumProp<NeuronKind> neuronKind;
   private DoubleProp eRevE, eRevI, tauSynE, tauSynI, vRest, vReset, vThresh, tauRefrac, tauM, cm;

   public NeuronType (Namespace<NeuronType> namespace, String name, NeuronKind neuronKind,
                      double vRest, double vReset, double vThresh, double eRevE, double eRevI,
                      double tauSynE, double tauSynI, double tauRefrac, double tauM, double cm) {
      super(namespace, name);
      this.neuronKind = new EditableEnumProp<NeuronKind>("Neuron Kind", NeuronKind.class, this, neuronKind)
            .setImpact(EnumSet.of(EditableProp.Impact.OTHER_PROPS_VISIBILITY));
      this.vRest = new DoubleProp("v rest", this, vRest);
      this.vReset = new DoubleProp("v reset", this, vReset);
      this.vThresh = new DoubleProp("v thresh", this, vThresh);
      this.eRevE = new DoubleProp("e rev E", this, eRevE);
      this.eRevI = new DoubleProp("e rev I", this, eRevI);
      this.tauSynE = new DoubleProp("tau syn E", this, tauSynE);
      this.tauSynI = new DoubleProp("tau syn I", this, tauSynI);
      this.tauRefrac = new DoubleProp("tau refrac", this, tauRefrac);
      this.tauM = new DoubleProp("tau m", this, tauM);
      this.cm = new DoubleProp("cm", this, cm);
   }

   public NeuronType (Namespace<NeuronType> namespace, String name) {
      this(namespace, name, NeuronKind.IF_COND_EXP, -70.0, -80.0, -65.0, 0.0, -100.0,
           2.0, 2.0, 2.0, 10.0, 0.2);
   }

   public NeuronType (NeuronType orig) {
      this(orig.getNamespace(), orig.getCopiedName(), orig.neuronKind.getValue(),
            orig.vRest.getValue(), orig.vReset.getValue(), orig.vThresh.getValue(), orig.eRevE.getValue(), orig.eRevI.getValue(),
            orig.tauSynE.getValue(), orig.tauSynI.getValue(), orig.tauRefrac.getValue(), orig.tauM.getValue(), orig.cm.getValue());
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
