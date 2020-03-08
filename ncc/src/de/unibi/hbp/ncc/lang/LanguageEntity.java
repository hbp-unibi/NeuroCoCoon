package de.unibi.hbp.ncc.lang;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import de.unibi.hbp.ncc.lang.props.EditableProp;
import de.unibi.hbp.ncc.lang.props.NameProp;
import de.unibi.hbp.ncc.lang.props.ReadOnlyProp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class LanguageEntity {
   private boolean predefined;  // such entities cannot be deleted and all their properties cannot be edited
   private mxCell owningCell;

   protected List<EditableProp<?>> addEditableProps (List<EditableProp<?>> list) {
      return list;
   }
   protected List<EditableProp<?>> addIndirectEditableProps (List<EditableProp<?>> list) { return list; }

   protected List<ReadOnlyProp<?>> addReadOnlyProps (List<ReadOnlyProp<?>> list) {
      return list;
   }
   protected List<ReadOnlyProp<?>> addIndirectReadOnlyProps (List<ReadOnlyProp<?>> list) { return list; }

   public List<ReadOnlyProp<?>> getReadOnlyProps () {
      return addReadOnlyProps(new ArrayList<>());
   }
   public List<ReadOnlyProp<?>> getIndirectReadOnlyProps () { return addIndirectReadOnlyProps(new ArrayList<>()); }
   public List<ReadOnlyProp<?>> getDirectAndIndirectReadOnlyProps () {
      return addIndirectReadOnlyProps(addReadOnlyProps(new ArrayList<>()));
   }

   public List<EditableProp<?>> getEditableProps () {
      return addEditableProps(new ArrayList<>());
   }
   public List<EditableProp<?>> getIndirectEditableProps () { return addIndirectEditableProps(new ArrayList<>()); }
   public List<EditableProp<?>> getDirectAndIndirectEditableProps () {
      return addIndirectEditableProps(addEditableProps(new ArrayList<>()));
   }

   public boolean isPredefined () { return predefined; }
   public void makePredefined () { predefined = true; }

   public mxCell getOwningCell () { return owningCell; }
   public void setOwningCell (mxCell owningCell) { this.owningCell = owningCell; }
   // override in subclasses, where cell style depends on (direct or indirect) property values
   public String getCellStyle () { return null; }

   // override this in subclasses, indirectly referenced from (multiple) cell-associated entities
   public List<mxICell> getDependentCells (mxIGraphModel graphModel) {
      return Collections.emptyList();
   }

   private List<LanguageEntity> addReferencedEntities (List<LanguageEntity> list, List<? extends ReadOnlyProp<?>> props) {
      for (ReadOnlyProp<?> prop: props) {
         if (prop instanceof NameProp)
            list.add(((NameProp<?>) prop).getTargetEntity());
      }
      return list;
   }

   protected List<LanguageEntity> addReferencedEntities (List<LanguageEntity> list) {
      return addReferencedEntities(addReferencedEntities(list, getReadOnlyProps()), getEditableProps());
   }

   public Iterable<LanguageEntity> getReferencedEntities () {
      return addReferencedEntities(new ArrayList<>());
   }

   public int countReferencedEntities () {
      return addReferencedEntities(new ArrayList<>()).size();
   }

   public boolean hasReferencedEntities () {
      return !addReferencedEntities(new ArrayList<>()).isEmpty();

   }

   public boolean hasReferenceTo (LanguageEntity target) {
      return addReferencedEntities(new ArrayList<>()).contains(target);
   }

   // @Override
   // public Object clone () { return this; }

   public abstract LanguageEntity duplicate ();
}
