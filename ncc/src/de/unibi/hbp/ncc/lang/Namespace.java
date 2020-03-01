package de.unibi.hbp.ncc.lang;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Namespace<T extends NamedEntity> implements Iterable<T> {

   private Scope containingScope;  // for access to sibling namespaces
   private Namespace<T> parent;  // would be used for nested namespaces
   private Class<T> memberClazz;
   private Map<String, T> members;
   private Set<String> normalizedMemberNames;
   private T mostRecentlyAdded;
   private String memberDescription;  // for error messages
   private Map<String, Integer> nameGenerators;
   private String pythonDiscriminator;
   private Id id;
   private NamespaceModel listModel;  // created lazily on demand

   public static class Id implements Serializable {  // abstract this away for future expansion beyond global scope namespaces only
      private String id;

      Id (String id) { this.id = id; }

      @Override
      public boolean equals (Object other) {
         return this == other || other instanceof Id && this.id.equals(((Id) other).id);
      }

      @Override
      public int hashCode () { return Objects.hash(id); }

      @Override
      public String toString () { return "Namespace.Id " + id; }
   }

   private static Map<Id, Namespace<?>> byId = new HashMap<>();

   private static final int MAX_GENERATOR_VALUE = 9999;

   Namespace (Scope containingScope, Class<T> clazz, String memberDescription, String pythonDiscriminator) {
      this.containingScope = containingScope;
      this.memberClazz = clazz;
      this.members = new HashMap<>();
      this.normalizedMemberNames = new HashSet<>();
      this.memberDescription = memberDescription;
      this.nameGenerators = new HashMap<>();
      if (pythonDiscriminator.isEmpty() || pythonDiscriminator.startsWith("_") || pythonDiscriminator.endsWith("_"))
         throw new IllegalArgumentException("invalid Python name fragment " + pythonDiscriminator);
      this.pythonDiscriminator = pythonDiscriminator;
      this.id = new Id(pythonDiscriminator);
      Namespace<?> oldValue = byId.put(this.id, this);
      if (oldValue != null)
         throw new IllegalStateException("duplicate namespace id/discriminator: " + pythonDiscriminator +
                                               " for " + oldValue + " and " + this);
   }

   // for (temporary) serialization by mxGraph operations
   Id getId () { return id; }

   public static Namespace<?> forId (Id id) { return byId.get(id); }

   static String normalizedName (String name) {
      return name.replace(' ', '_');
   }

   // TODO should name validation been done here, too? likely yes
   void add (T member) {
      T oldValue = members.put(member.getName(), member);
      if (oldValue != null)
         throw new LanguageException(member, "duplicate name " + member.getName());
      if (!normalizedMemberNames.add(normalizedName(member.getName())))
         throw new LanguageException(member, "duplicate normalized name " + normalizedName(member.getName()));
      mostRecentlyAdded = member;
      if (listModel != null)
         listModel.addElement(member);
   }

   void castAndAdd (NamedEntity entity) {
      add(memberClazz.cast(Objects.requireNonNull(entity)));
   }

   private void remove (String memberName) {
      T oldValue = members.remove(memberName);
      if (oldValue == null)
         throw new LanguageException("name " + memberName + " does not exist");
      if (!normalizedMemberNames.remove(normalizedName(memberName)))
         throw new LanguageException("normalized name " + normalizedName(memberName) + " does not exist");
      if (oldValue.isPredefined())
         throw new LanguageException(oldValue, "predefined member " + memberName + " must not be removed");
      if (oldValue.equals(mostRecentlyAdded))
         mostRecentlyAdded = null;
      if (listModel != null)
         listModel.removeElement(oldValue);
   }

   // TODO make this package scoped again and provide a public safe method (with reference checking) here instead [currently in MasterDetailsEditor]
   public void remove (NamedEntity member) {
      if (!this.equals(member.getNamespace()))
         throw new IllegalArgumentException("member not part of this namespace: " + member);
      remove(member.getName());
   }

   public T get (String name) {
      return members.get(name);
   }

   public boolean isEmpty () {
      return members.isEmpty();
   }

   // this is only used for namespaces without predefined entities, otherwise we should support
   // marking one of the predefined entities as a universal default entity explicitly
   public T getFallbackDefault () {
      if (members.isEmpty())
         throw new IllegalStateException("empty namespace");
      if (mostRecentlyAdded != null)
         return mostRecentlyAdded;
      else
         return members.values().iterator().next();
   }

   @Override
   public Iterator<T> iterator () {
      return members.values().iterator();
   }

   public Collection<T> getAllMembers () { return Collections.unmodifiableCollection(members.values()); }

   public ListModel<T> getListModel () {
      if (listModel == null)
         listModel = new NamespaceModel();
      return listModel;
   }

   public boolean contains (String name) {
      return members.containsKey(name);
   }
   public boolean containsAnyVariantOf (String name) {
      return normalizedMemberNames.contains(normalizedName(name));
   }

   static boolean areNameVariants (String nameA, String nameB) {
      return normalizedName(nameA).equals(normalizedName(nameB));
   }

   public Scope getContainingScope () { return containingScope; }

   String generateName (NamedEntity futureMember) {
      String namePrefix = futureMember.getGeneratedNamesPrefix();
      int nameGenerator = nameGenerators.getOrDefault(namePrefix, 0);
      String result;
      do {
         result = namePrefix + " " + (++nameGenerator);
         if (nameGenerator > MAX_GENERATOR_VALUE)
            throw new LanguageException("failed to generate name for prefix " + namePrefix);
      } while (members.containsKey(result));
      nameGenerators.put(namePrefix, nameGenerator);
      return result;
   }

   public String getDescription () { return memberDescription; }

   private static final String PYTHON_USER_NAME_PREFIX = "_usr_";  // we disallow leading underscores
   private static final String PYTHON_GENERATED_NAME_PREFIX = "_gen_";  // we disallow leading underscores

   String buildPythonName (String memberName) {
      assert members.containsKey(memberName);
      return PYTHON_USER_NAME_PREFIX + pythonDiscriminator + "_" + normalizedName(memberName);
   }

   String buildDerivedPythonName (String purposeDiscriminator, String memberName) {
      if (purposeDiscriminator.isEmpty() || purposeDiscriminator.startsWith("_") || purposeDiscriminator.endsWith("_"))
         throw new IllegalArgumentException("invalid Python name fragment " + purposeDiscriminator);
      assert members.containsKey(memberName);
      return PYTHON_GENERATED_NAME_PREFIX + pythonDiscriminator + "_" + purposeDiscriminator + "_" +
            normalizedName(memberName);
   }

   public static String buildUnadornedPythonName (String name) {
      return normalizedName(name);
   }

   private class NamespaceModel extends AbstractListModel<T> {
      private List<T> elements;

      NamespaceModel () {
         elements = new ArrayList<>(members.values());
         elements.sort(null);
      }

      void addElement (T element) {
         int oldSize = elements.size();
         elements.add(element);
         elements.sort(null);
         fireContentsChanged(this, 0, oldSize - 1);
         fireIntervalAdded(this, oldSize, oldSize);
      }

      void removeElement (T element) {
         int pos = elements.indexOf(element);
         assert pos >= 0;
         elements.remove(pos);
         fireIntervalRemoved(this, pos, pos);
      }

      @Override
      public int getSize () {
         return elements.size();
      }

      @Override
      public T getElementAt (int index) {
         return elements.get(index);
      }

      // TODO destroy model, if last listener is removed?
   }
}
