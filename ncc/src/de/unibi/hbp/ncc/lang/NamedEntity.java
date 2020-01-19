package de.unibi.hbp.ncc.lang;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public abstract class NamedEntity<T extends NamedEntity<T>> extends LanguageEntity implements DisplayNamed {
   private String name;
   private Namespace<T> namespace;

   protected NamedEntity (Namespace<T> namespace, String name) {
      this.namespace = namespace;
      setName(name);
      namespace.add((T) this);
      // would be problematic, if T were no the concrete NamedEntity subclass itself
   }

   protected NamedEntity (Namespace<T> namespace) {
      this(namespace, namespace.generateName());
   }

   public String getName () {
      return name;
   }

   // conservative ASCII identifiers with embedded individual spaces or underscores
   // (no leading, trailing or multiple consecutive spaces or underscores allowed)
   private static final Pattern IDENTIFIER_REGEXP = Pattern.compile("[A-Za-z]([_ ]?[A-Za-z0-9])*");

   public static boolean isValidName (String name) {
      return IDENTIFIER_REGEXP.matcher(name).matches();
   }

   private static String normalizedName (String name) {
      return name.replace(' ', '_');
   }

   private void setName (String name) {
      if (!isValidName(name))
         throw new LanguageException("name " + name + " is invalid");
      this.name = name;
   }

   public boolean canRenameTo (String name) {
      String newNormalizedName = normalizedName(name);
      return !namespace.contains(newNormalizedName) ||
            normalizedName(getName()).equals(newNormalizedName);
      // allow renaming , if name does not change (after normalization), i.e. name "conflict" with the entity itself
   }

   public void renameTo (String name) {
      if (!canRenameTo(name))
         throw new LanguageException("name " + name + " conflicts with an existing name");
      if (getName().equals(name))
         return;  // a no-op
      namespace.remove(getName());
      setName(name);
      namespace.add((T) this);
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

   private static final String PYTHON_USER_NAME_PREFIX = "_usr_";  // we disallow leading underscores

   public String getPythonName () {
      return PYTHON_USER_NAME_PREFIX + namespace.getPythonDiscriminator() + "_" + normalizedName(getName());

   }

   protected final static List<PropertyDescriptor<? extends LanguageEntity, ?>> entityProperties = addEntityProperties(new ArrayList<>());

   protected static List<PropertyDescriptor<? extends LanguageEntity, ?>> addEntityProperties (List<PropertyDescriptor<? extends LanguageEntity, ?>> superProps) {
      PropertyDescriptor<NamedEntity<?>, String> nameProperty =
            new PropertyDescriptor<>("Name", String.class,
                                     NamedEntity::renameTo, NamedEntity::getName,
                                     NamedEntity::checkValidName, NamedEntity::checkConflictingName);

      superProps.add(nameProperty);
      return superProps;
   }

   private String checkConflictingName (String name) {
      if (!canRenameTo(name))
         return "Conflicting name " + name;
      else
         return null;
   }

   private static String checkValidName (String name) {
      if (!isValidName(name))
         return "Invalid name " + name;
      else
         return null;
   }

}
