package de.unibi.hbp.ncc.lang;

import com.mxgraph.model.mxIGraphModel;
import de.unibi.hbp.ncc.lang.props.EditableProp;
import de.unibi.hbp.ncc.lang.props.StringProp;
import de.unibi.hbp.ncc.lang.serialize.SerializedNamedEntity;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public abstract class NamedEntity extends LanguageEntity
      implements DisplayNamed, PythonNamed, Serializable, Comparable<NamedEntity> {
   private StringProp nameProp;
   private final Namespace<? extends NamedEntity> namespace;

   protected Object writeReplace() throws ObjectStreamException {
      return new SerializedNamedEntity(this);
   }

   // readObject method for the serialization proxy pattern
   // See Effective Java, Second Ed., Item 78.
   private void readObject(java.io.ObjectInputStream stream) throws InvalidObjectException {
      throw new InvalidObjectException("SerializedEntityName required");
   }

   protected List<EditableProp<?>> addEditableProps (List<EditableProp<?>> list) {
      list.add(nameProp);
      return list;
   }

   public static final String NAME_PROPERTY_NAME = "Name";

   protected NamedEntity (Namespace<? extends NamedEntity> namespace, String name) {
      this.namespace = Objects.requireNonNull(namespace);
      if (name == null)
         name = namespace.generateName(this);
      this.nameProp = new StringProp(NAME_PROPERTY_NAME, this, name) {
         @Override
         public boolean isValid (String proposedValue) {
            return getNamespace().canRenameTo(NamedEntity.this, proposedValue);
         }

         @Override
         public void setValue (String value) {
            renameTo(value);
         }
      };
      this.nameProp.addImpact(EditableProp.Impact.CELL_LABEL);
      this.namespace.castAndAdd(this);
      // would be problematic, if T were not the concrete NamedEntity subclass itself
   }

   protected NamedEntity (Namespace<? extends NamedEntity> namespace) {
      this(namespace, null);
   }

   protected String getCopiedName () {
      return namespace.getCopiedName(this);
   }

   protected abstract String getGeneratedNamesPrefix ();

   protected Namespace<? extends NamedEntity> getNamespace () { return namespace; }

   public Namespace.Id getNamespaceId () { return namespace.getId(); }

   protected NamedEntity addNamePropImpact (EditableProp.Impact impact) {
      nameProp.addImpact(impact);
      return this;
   }

   @Override
   public void makePredefined () {
      super.makePredefined();
      this.namespace.markedAsPredefined(this);
   }

   public EditableProp<String> getNameProp () { return nameProp; }

   public String getName () {
      return nameProp.getValue();
   }

   void updateNameInternal (String futureName) {
      nameProp.setValueInternal(futureName);
   }

   public void renameTo (String futureName) {
      namespace.renameTo(this, futureName);
   }

   public boolean delete (mxIGraphModel graphModel) {
      return namespace.remove(this, graphModel);
   }

   @Override
   public String getDisplayName () {
      return getName();
   }

   protected String getDisplayNamePrefix () { return getGeneratedNamesPrefix(); }

   @Override
   public String getLongDisplayName () {
      String prefix = getDisplayNamePrefix();
      String name = getName();
      if (prefix != null && !prefix.isEmpty() && !name.startsWith(prefix))
         return prefix + " " + name;
      else
         return name;
   }

   @Override
   public int compareTo (NamedEntity other) {
      return Namespace.getSmartNumericOrderComparator().compare(this.getName(), other.getName());
   }

   @Override
   public boolean equals (Object obj) {
      NamedEntity other;
      return obj instanceof NamedEntity &&
            this.getName().equals((other = (NamedEntity) obj).getName()) &&
            this.getNamespace().equals(other.getNamespace());
   }

   @Override
   public String toString () {  // used for tooltips
      return getDisplayName();
   }

   @Override
   public String getPythonName () {
      return namespace.buildPythonName(this.getName());
   }

   public String getDerivedPythonName (String purposeDiscriminator) {
      return namespace.buildDerivedPythonName(purposeDiscriminator, this.getName());
   }

   public String getUnadornedPythonName () {
      return Namespace.buildUnadornedPythonName(this.getName());
   }
}
