package de.unibi.hbp.ncc.lang;

import de.unibi.hbp.ncc.lang.props.EditableProp;
import de.unibi.hbp.ncc.lang.props.StringProp;
import de.unibi.hbp.ncc.lang.serialize.SerializedEntityName;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class NamedEntity<E extends NamedEntity<E>> extends LanguageEntity
      implements DisplayNamed, PythonNamed, Serializable, Comparable<NamedEntity<?>> {
   private StringProp nameProp;
   private Namespace<E> namespace;

   protected Object writeReplace() throws ObjectStreamException {
      return new SerializedEntityName(namespace.getId(), getName());
   }

   protected List<EditableProp<?>> addEditableProps (List<EditableProp<?>> list) {
      list.add(nameProp);
      return list;
   }

   protected NamedEntity (Namespace<E> namespace, String name) {
      this.namespace = namespace;
      if (name == null)
         name = namespace.generateName();
      this.nameProp = new StringProp("Name", this, name) {
         @Override
         public boolean isValid (String proposedValue) {
            return isValidName(proposedValue) && canRenameTo(proposedValue);
         }

         @Override
         public EnumSet<Impact> getChangeImpact () {
            return EnumSet.of(Impact.CELL_LABEL);
         }
      };
      this.namespace.castAndAdd(this);
      // would be problematic, if T were not the concrete NamedEntity subclass itself
   }

   protected NamedEntity (Namespace<E> namespace) {
      this(namespace, null);
   }

   private static final Pattern COPY_SUFFIX_REGEXP = Pattern.compile(" Copy( \\d+)?$");

   protected String getCopiedName () {
      String copiedName = getName();
      Matcher matcher = COPY_SUFFIX_REGEXP.matcher(copiedName);
      if (matcher.find())
         copiedName = copiedName.substring(0, matcher.start());
      copiedName += " Copy";
      if (namespace.contains(normalizedName(copiedName))) {
         int counter = 2;
         while (namespace.contains(normalizedName((copiedName + " " + counter))))
            counter += 1;
         copiedName += " " + counter;
      }
      return copiedName;
   }

   protected Namespace<E> getNamespace () { return namespace; }

   public String getName () {
      return nameProp.getValue();
   }

   // conservative ASCII identifiers with embedded individual spaces or underscores
   // (no leading, trailing or multiple consecutive spaces or underscores allowed)
   private static final Pattern IDENTIFIER_REGEXP = Pattern.compile("[A-Za-z]([_ ]?[A-Za-z0-9])*");

   private static boolean isValidName (String name) {
      return IDENTIFIER_REGEXP.matcher(name).matches();
   }

   private static String normalizedName (String name) {
      return name.replace(' ', '_');
   }

   private void setName (String name) {
      nameProp.setValue(name);
   }

   public boolean canRenameTo (String name) {
      String newNormalizedName = normalizedName(name);
      return !namespace.contains(newNormalizedName) ||
            normalizedName(getName()).equals(newNormalizedName);
      // allow renaming, if name does not change (after normalization), i.e. name "conflict" with the entity itself
   }

   public void renameTo (String name) {
      if (!canRenameTo(name))
         throw new LanguageException(this, "name " + name + " conflicts with an existing name");
      if (getName().equals(name))
         return;  // a no-op
      namespace.remove(getName());
      setName(name);
      this.namespace.castAndAdd(this);
   }

   @Override
   public String getDisplayName () {
      return getName();
   }

   protected String getDisplayNamePrefix () { return namespace.getGeneratedNamesPrefix(); }

   @Override
   public String getLongDisplayName () {
      String prefix = getDisplayNamePrefix();
      if (prefix != null && !prefix.isEmpty())
         return prefix + " " + getName();
      else
         return getName();
   }

   @Override
   public int compareTo (NamedEntity<?> other) {
      return this.getName().compareTo(other.getName());
   }

   @Override
   public String toString () {  // used for tooltips
      return getDisplayName();
   }

   private static final String PYTHON_USER_NAME_PREFIX = "_usr_";  // we disallow leading underscores

   public String getPythonName () {
      return buildPythonName(namespace.getPythonDiscriminator(), getName());
   }

   static String buildPythonName (String discriminator, String name) {
      assert !discriminator.isEmpty() && !discriminator.startsWith("_") && !discriminator.endsWith("_");
      assert isValidName(name);
      return PYTHON_USER_NAME_PREFIX + discriminator + "_" + normalizedName(name);
   }

   static String buildTopLevelPythonName (String name) {
      assert isValidName(name);
      return normalizedName(name);
   }
}
