package de.unibi.hbp.ncc.lang;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Namespace<T extends NamedEntity<T>> implements Iterable<T> {
   private Scope containingScope;  // for access to sibling namespaces
   private Namespace<T> parent;  // would be used for nested namespaces
   private Class<T> memberClazz;
   private Map<String, T> members;
   private String generatedNamesPrefix;
   private int nameGenerator;
   private String pythonDiscriminator;
   private int id;  // for data transfer
   private NamespaceModel model;  // created lazily on demand

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

   int getId () { return id; }

   static Namespace<?> forId (int id) { return byId.get(id); }

   // TODO should name validation (and normalization?) been done here?
   void add (T member) {
      T oldValue = members.put(member.getName(), member);
      if (oldValue != null)
         throw new LanguageException("duplicate name " + member.getName());
      if (model != null)
         model.addElement(member);
   }

   void castAndAdd (NamedEntity<T> entity) {
      add(memberClazz.cast(entity));
   }

   void remove (String memberName) {
      T oldValue = members.remove(memberName);
      if (oldValue == null)
         throw new LanguageException("name " + memberName + " does not exist");
      if (model != null)
         model.removeElement(oldValue);
   }

   public T get (String name) {
      return members.get(name);
   }

   @Override
   public Iterator<T> iterator () {
      return members.values().iterator();
   }

   public ComboBoxModel<T> getModel () {
      if (model == null)
         model = new NamespaceModel();
      return model;
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
      }
      while (members.containsKey(result));
      return result;
   }

   String getGeneratedNamesPrefix () { return generatedNamesPrefix; }

   String getPythonDiscriminator () {
      return pythonDiscriminator;
   }


   private class NamespaceModel extends AbstractListModel<T> implements ComboBoxModel<T> {
      private Object selectedObject;
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
         fireIntervalRemoved(this, pos, pos);
      }

      public void setSelectedItem(Object anObject) {
         if (!Objects.equals(anObject, selectedObject)) {
            selectedObject = anObject;
            fireContentsChanged(this, -1, -1);
         }
      }

      public Object getSelectedItem() { return selectedObject; }

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
