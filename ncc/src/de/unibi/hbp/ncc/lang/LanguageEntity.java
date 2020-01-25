package de.unibi.hbp.ncc.lang;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class LanguageEntity implements Cloneable, Serializable {
   private boolean predefined;  // such entities cannot be deleted
   private List<LanguageEntity> referencedEntities;  // created lazily on demand

   public boolean isPredefined () { return predefined; }
   public void makePredefined () { predefined = true; }

   protected void addReferenceTo (LanguageEntity target) {
      if (referencedEntities == null)
         referencedEntities = new ArrayList<>();
      referencedEntities.add(target);  // allows multiple references to the same target
   }

   protected void removeReferenceTo (LanguageEntity target) {
      if (referencedEntities == null || !referencedEntities.remove(target))
         throw new LanguageException(this.toString() + " had no reference to " + target);
      if (referencedEntities.isEmpty())
         referencedEntities = null;
   }

   protected <E extends LanguageEntity> void changeReference (E oldTarget, E newTarget) {
      // TODO or better not enforce same type? consequences/differences vs. using LanguageEntity instead of E
      if (Objects.equals(oldTarget, newTarget))
         return;  // a no-op
      if (oldTarget == null)
         addReferenceTo(newTarget);
      else if (newTarget == null)
         removeReferenceTo(oldTarget);
      else {
         int pos;
         if (referencedEntities == null || (pos = referencedEntities.indexOf(oldTarget)) < 0)
            throw new LanguageException(this.toString() + " had no reference to " + oldTarget);
         referencedEntities.set(pos, newTarget);
      }
   }

   public abstract List<PropertyDescriptor<? extends LanguageEntity, ?>> getEntityProperties ();

   public Iterable<LanguageEntity> getReferencedEntities () {
      if (referencedEntities != null)
         return referencedEntities;
      else
         return Collections.emptyList();
   }

   public int countReferencedEntities () {
      if (referencedEntities != null)
         return referencedEntities.size();
      else
         return 0;
   }

   public boolean hasReferencedEntities () {
      return referencedEntities != null && !referencedEntities.isEmpty();
   }

   public boolean hasReferenceTo (LanguageEntity target) {
      return referencedEntities != null && referencedEntities.contains(target);
   }

   @Override
   public Object clone () {
      try {
         LanguageEntity clone = (LanguageEntity) super.clone();
         clone.predefined = false;  // a copy of a predefined entity is user modifiable
         if (this.referencedEntities != null)
            clone.referencedEntities = new ArrayList<>(this.referencedEntities);
         return clone;
      }
      catch (CloneNotSupportedException cnse) {
         // cnse.printStackTrace(System.err);
         throw new RuntimeException("LanguageEntity should be cloneable", cnse);
      }
   }
}
