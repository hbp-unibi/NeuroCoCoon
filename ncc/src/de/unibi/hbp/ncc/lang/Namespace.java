package de.unibi.hbp.ncc.lang;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Namespace<T extends NamedEntity<T>> implements Iterable<T> {
   private Scope containingScope;  // for access to sibling namespaces
   private Namespace<T> parent;  // would be used for nested namespaces
   private Class<T> memberClazz;
   private Map<String, T> members;
   private T mostRecentlyAdded;
   private String generatedNamesPrefix;
   private int nameGenerator;
   private String pythonDiscriminator;
   private int id;  // for data transfer
   private NamespaceModel listModel;  // created lazily on demand

   private static List<Namespace<?>> byId = new ArrayList<>();

   private static final int MAX_GENERATOR_VALUE = 9999;

   Namespace (Scope containingScope, Class<T> clazz, String generatedNamesPrefix, String pythonDiscriminator) {
      this.containingScope = containingScope;
      this.memberClazz = clazz;
      this.members = new HashMap<>();
      this.generatedNamesPrefix = generatedNamesPrefix.trim() + " ";
      this.nameGenerator = 0;
      if (pythonDiscriminator.isEmpty() || pythonDiscriminator.startsWith("_") || pythonDiscriminator.endsWith("_"))
         throw new IllegalArgumentException("invalid Python name fragment");
      this.pythonDiscriminator = pythonDiscriminator;
      this.id = byId.size();
      byId.add(this);
   }

   // for (temporary) serialization by mxGraph operations
   int getId () { return id; }

   public static Namespace<?> forId (int id) { return byId.get(id); }

   // TODO should name validation (and normalization?) been done here?
   void add (T member) {
      T oldValue = members.put(member.getName(), member);
      if (oldValue != null)
         throw new LanguageException(member, "duplicate name " + member.getName());
      mostRecentlyAdded = member;
      if (listModel != null)
         listModel.addElement(member);
   }

   void castAndAdd (NamedEntity<T> entity) {
      add(memberClazz.cast(entity));
   }

   // TODO make this package scoped again and provide a public safe method (with reference checking) here instead [currently in MasterDetailsEditor]
   public void remove (String memberName) {
      T oldValue = members.remove(memberName);
      if (oldValue == null)
         throw new LanguageException("name " + memberName + " does not exist");
      if (oldValue.isPredefined())
         throw new LanguageException(oldValue, "predefined member " + memberName + " must not be removed");
      if (oldValue.equals(mostRecentlyAdded))
         mostRecentlyAdded = null;
      if (listModel != null)
         listModel.removeElement(oldValue);
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

   public Scope getContainingScope () { return containingScope; }

   String generateName () {
      String result;
      do {
         result = generatedNamesPrefix + (++nameGenerator);
         if (nameGenerator > MAX_GENERATOR_VALUE)
            throw new LanguageException("failed to generate name for prefix " + generatedNamesPrefix);
      } while (members.containsKey(result));
      return result;
   }

   String generateSpecificName (String namePrefix) {
      namePrefix = namePrefix.trim() + " ";
      int numberGenerator = 0;
      String result;
      do {
         result = namePrefix + (++numberGenerator);
         if (numberGenerator > MAX_GENERATOR_VALUE)
            throw new LanguageException("failed to generate name for specific prefix " + namePrefix);
      } while (members.containsKey(result));
      return result;
   }

   String getGeneratedNamesPrefix () { return generatedNamesPrefix; }

   // maybe this needs to be distinct from the generated names prefix?
   public String getDescription () { return generatedNamesPrefix.toLowerCase(); }

   String getPythonDiscriminator () {
      return pythonDiscriminator;
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
