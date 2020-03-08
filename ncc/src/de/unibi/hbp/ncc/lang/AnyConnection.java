package de.unibi.hbp.ncc.lang;

import com.mxgraph.model.mxICell;
import de.unibi.hbp.ncc.lang.props.EditableProp;
import de.unibi.hbp.ncc.lang.props.StringProp;

import java.util.List;

public abstract class AnyConnection extends LanguageEntity {
   private StringProp userLabel;

   // TODO add a StringProperty that overrides the displayed edge label if set (just for documentary purposes)

   // TODO provide a separate addEditableProps sibling method that subclasses call after adding their own props
   protected List<EditableProp<?>> addEditablePropsAtEnd (List<EditableProp<?>> list) {
      // list.add(userLabel);
      // super call would go here probably (show unimportant props from general to specific, like the normal properties)
      // super.addEditablePropsAtEnd(list);
      return list;
   }

   private Object getTerminalValue (boolean source) {
      mxICell edgeCell = getOwningCell();
      if (edgeCell != null) {
         mxICell terminalCell = edgeCell.getTerminal(source);
         if (terminalCell != null)
            return terminalCell.getValue();
      }
      return null;
   }

   public LanguageEntity getSourceEntity () {
      Object sourceValue = getTerminalValue(true);
      return (sourceValue instanceof LanguageEntity) ? (LanguageEntity) sourceValue : null;
   }

   public LanguageEntity getTargetEntity () {
      Object targetValue = getTerminalValue(false);
      return (targetValue instanceof LanguageEntity) ? (LanguageEntity) targetValue : null;
   }

   public NamedEntity getSourceNamedEntity () {
      Object sourceValue = getTerminalValue(true);
      return (sourceValue instanceof NamedEntity) ? (NamedEntity) sourceValue : null;
   }

   public NamedEntity getTargetNamedEntity () {
      Object targetValue = getTerminalValue(false);
      return (targetValue instanceof NamedEntity) ? (NamedEntity) targetValue : null;
   }

   public NeuronPopulation getSourcePopulation () {
      Object sourceValue = getTerminalValue(true);
      return (sourceValue instanceof NeuronPopulation) ? (NeuronPopulation) sourceValue : null;
   }

   public NeuronPopulation getTargetPopulation () {
      Object targetValue = getTerminalValue(false);
      return (targetValue instanceof NeuronPopulation) ? (NeuronPopulation) targetValue : null;
   }

   public NetworkModule.Port getSourceModulePort () {
      Object sourceValue = getTerminalValue(true);
      return (sourceValue instanceof NetworkModule.Port) ? (NetworkModule.Port) sourceValue : null;
   }

   public NetworkModule.Port getTargetModulePort () {
      Object targetValue = getTerminalValue(false);
      return (targetValue instanceof NetworkModule.Port) ? (NetworkModule.Port) targetValue : null;
   }
}
