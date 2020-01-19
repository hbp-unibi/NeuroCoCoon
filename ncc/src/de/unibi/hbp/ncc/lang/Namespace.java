package de.unibi.hbp.ncc.lang;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Namespace<T extends NamedEntity> implements Iterable<T> {
   private Class<T> memberClazz;
   private Map<String, T> members;
   private String generatedNamesPrefix;
   private int nameGenerator;
   private String pythonDiscriminator;

   private static final int MAX_GENERATOR_VALUE = 9999;

   Namespace (Class<T> clazz, String generatedNamesPrefix, String pythonDiscriminator) {
      if (pythonDiscriminator.isEmpty() || pythonDiscriminator.startsWith("_") || pythonDiscriminator.endsWith("_"))
         throw new IllegalArgumentException("invalid Python name fragment");
      this.pythonDiscriminator = pythonDiscriminator;
      members = new HashMap<>();
      this.generatedNamesPrefix = generatedNamesPrefix.trim() + " ";
      nameGenerator = 0;
   }

   // TODO should name validation been done here?
   void add (T member) {
      T oldValue = members.put(member.getName(), member);
      if (oldValue != null)
         throw new LanguageException("duplicate name " + member.getName());
   }

   void castAndAdd (NamedEntity entity) {
      add(memberClazz.cast(entity));
   }

   void remove (String memberName) {
      T oldValue = members.remove(memberName);
      if (oldValue == null)
         throw new LanguageException("name " + memberName + " does not exist");
   }

   T get (String name) {
      return members.get(name);
   }

   @Override
   public Iterator<T> iterator () {
      return members.values().iterator();
   }

   public boolean contains (String name) {
      return members.containsKey(name);
   }

   String generateName () {
      String result;
      do {
         result = generatedNamesPrefix + (++nameGenerator);
         if (nameGenerator > MAX_GENERATOR_VALUE)
            throw new LanguageException("failed to generate name for prefix " + generatedNamesPrefix);
      }
      while (members.containsKey(result));
      return result;
   }

   String getGeneratedNamesPrefix () { return generatedNamesPrefix; }

   String getPythonDiscriminator () {
      return pythonDiscriminator;
   }
}
