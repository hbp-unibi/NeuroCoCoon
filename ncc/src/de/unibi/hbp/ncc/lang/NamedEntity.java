package de.unibi.hbp.ncc.lang;

import de.unibi.hbp.ncc.lang.props.EditableProp;
import de.unibi.hbp.ncc.lang.props.StringProp;
import de.unibi.hbp.ncc.lang.serialize.SerializedEntityName;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class NamedEntity extends LanguageEntity
      implements DisplayNamed, PythonNamed, Serializable, Comparable<NamedEntity> {
   private StringProp nameProp;
   private final transient Namespace<? extends NamedEntity> namespace;

   protected Object writeReplace() throws ObjectStreamException {
      return new SerializedEntityName(namespace.getId(), getName());
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

   protected NamedEntity (Namespace<? extends NamedEntity> namespace, String name) {
      this.namespace = Objects.requireNonNull(namespace);
      if (name == null)
         name = namespace.generateName(this);
      this.nameProp = new StringProp("Name", this, name) {
         @Override
         public boolean isValid (String proposedValue) {
            return isValidName(proposedValue) && canRenameTo(proposedValue);
         }

         @Override
         public void setValue (String value) {
            renameTo(value);
         }
      };
      this.nameProp.setImpact(EditableProp.Impact.CELL_LABEL);
      this.namespace.castAndAdd(this);
      // would be problematic, if T were not the concrete NamedEntity subclass itself
   }

   protected NamedEntity (Namespace<? extends NamedEntity> namespace) {
      this(namespace, null);
   }

   private static final Pattern COPY_SUFFIX_REGEXP = Pattern.compile(" Copy( \\d+)?$");

   protected String getCopiedName () {
      String copiedName = getName();
      Matcher matcher = COPY_SUFFIX_REGEXP.matcher(copiedName);
      if (matcher.find())
         copiedName = copiedName.substring(0, matcher.start());
      copiedName += " Copy";
      if (namespace.containsAnyVariantOf(copiedName)) {
         int counter = 2;
         while (namespace.containsAnyVariantOf((copiedName + " " + counter)))
            counter += 1;
         copiedName += " " + counter;
      }
      return copiedName;
   }

   protected abstract String getGeneratedNamesPrefix ();

   protected Namespace<? extends NamedEntity> getNamespace () { return namespace; }

   public EditableProp<String> getNameProp () { return nameProp; }

   public String getName () {
      return nameProp.getValue();
   }

   // conservative ASCII identifiers with embedded individual spaces or underscores
   // (no leading, trailing or multiple consecutive spaces or underscores allowed)
   private static final Pattern IDENTIFIER_REGEXP = Pattern.compile("[A-Za-z]([_ ]?[A-Za-z0-9])*");

   private static boolean isValidName (String name) {
      return IDENTIFIER_REGEXP.matcher(name).matches();
   }

   public boolean canRenameTo (String name) {
      return !namespace.containsAnyVariantOf(name) ||
            Namespace.areNameVariants(getName(), name);
      // allow renaming, if name does not change (after normalization), i.e. name "conflict" with the entity itself
   }

   public void renameTo (String name) {
      if (!canRenameTo(name))
         throw new LanguageException(this, "name " + name + " conflicts with an existing name");
      if (name.equals(getName()))
         return;  // a no-op
      // TODO need an atomic way to rename something in the namespace (without temporarily removing it and adding it back) to avoid loss of combo box selections
      namespace.remove(this);
      nameProp.setValueInternal(name);
      namespace.castAndAdd(this);
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
      return this.getName().compareTo(other.getName());
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

   public String getPythonName () {
      return namespace.buildPythonName(this.getName());
   }

}
