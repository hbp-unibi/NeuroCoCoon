package de.unibi.hbp.ncc.lang;

import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import de.unibi.hbp.ncc.graph.AbstractCellsCollector;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Namespace<T extends NamedEntity> implements Iterable<T> {

   private Scope containingScope;  // for access to sibling namespaces
   private Namespace<T> parent;  // would be used for nested namespaces
   private Class<T> memberClazz;
   private Map<String, T> members;
   private Set<String> normalizedMemberNames;
   private T mostRecentlyAdded, firstPredefined;
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
      this.members = new TreeMap<>(getSmartNumericOrderComparator());
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

   void clear () {
      this.members.clear();
      this.normalizedMemberNames.clear();
      this.nameGenerators.clear();
      mostRecentlyAdded = firstPredefined = null;
      if (listModel != null)
         listModel.clear();
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

   // TODO make this package scoped again and provide a public safe method (with reference checking) here instead [currently in MasterDetailsEditor]
   void remove (NamedEntity member) {
      String memberName = member.getName();
      T oldValue = members.remove(memberName);
      if (oldValue == null)
         throw new LanguageException("name " + memberName + " does not exist");
      if (!normalizedMemberNames.remove(normalizedName(memberName)))
         throw new LanguageException("normalized name " + normalizedName(memberName) + " does not exist");
      if (oldValue.isPredefined())
         throw new LanguageException(oldValue, "predefined member " + memberName + " must not be removed");
      if (oldValue.equals(mostRecentlyAdded))
         mostRecentlyAdded = null;
      // firstPredefined does NOT need a similar check, because predefined members cannot be removed
      if (listModel != null)
         listModel.removeElement(oldValue);
   }

   public boolean remove (NamedEntity member, mxIGraphModel graphModel) {
      if (!this.equals(member.getNamespace()))
         throw new IllegalArgumentException("member not part of this namespace: " + member);
      if (new AbstractCellsCollector(true, true) {
         @Override
         protected boolean matches (mxICell cell, LanguageEntity entity) {
            return entity.hasReferenceTo(member);
         }
      }.haveMatchingCells(graphModel))
         return false;
      else {
         remove(member);
         return true;
      }
   }

   void renameTo (NamedEntity member, String futureName) {
      // TODO implement this with atomic model notification and use it
      if (!canRenameTo(member, futureName))
         throw new LanguageException(member, "name " + futureName + " conflicts with an existing name");
      if (futureName.equals(member.getName()))
         return;  // a no-op
      // TODO need an atomic way to rename something in the namespace (without temporarily removing it and adding it back) to avoid loss of combo box selections
      if (listModel != null)
         listModel.setNotificationsEnabled(false);
      remove(member);
      member.updateNameInternal(futureName);
      castAndAdd(member);
      if (listModel != null) {
         listModel.setNotificationsEnabled(true);
         listModel.renamedElement();
      }
   }

   public T get (String name) {
      return members.get(name);
   }

   public boolean isEmpty () {
      return members.isEmpty();
   }

   void markedAsPredefined (NamedEntity member) {
      if (!this.equals(member.getNamespace()))
         throw new IllegalArgumentException("member not part of this namespace: " + member);
      if (!member.isPredefined())
         throw new IllegalStateException("member is not marked as predefined: " + member);
      if (firstPredefined == null)
         firstPredefined = memberClazz.cast(member);
   }

   // this is only used for namespaces without predefined entities, otherwise we should support
   // marking one of the predefined entities as a universal default entity explicitly
   public T getFallbackDefault () {
      if (members.isEmpty())
         throw new IllegalStateException("empty namespace");
      if (firstPredefined != null)
         return firstPredefined;
      else if (mostRecentlyAdded != null)
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

   private boolean containsAnyVariantOf (String name) {
      return normalizedMemberNames.contains(normalizedName(name));
   }

   private static boolean areNameVariants (String nameA, String nameB) {
      return normalizedName(nameA).equals(normalizedName(nameB));
   }

   // conservative ASCII identifiers with embedded individual spaces or underscores
   // (no leading, trailing or multiple consecutive spaces or underscores allowed)
   private static final Pattern IDENTIFIER_REGEXP = Pattern.compile("[A-Za-z]([_ ]?[A-Za-z0-9])*");

   private boolean isValidName (String name) {
      return IDENTIFIER_REGEXP.matcher(name).matches();
   }

   private static final Pattern COPY_SUFFIX_REGEXP = Pattern.compile(" Copy( \\d+)?$");

   String getCopiedName (NamedEntity member) {
      String copiedName = member.getName();
      Matcher matcher = COPY_SUFFIX_REGEXP.matcher(copiedName);
      if (matcher.find())
         copiedName = copiedName.substring(0, matcher.start());
      copiedName += " Copy";
      if (containsAnyVariantOf(copiedName)) {
         int counter = 2;
         while (containsAnyVariantOf((copiedName + " " + counter)))
            counter += 1;
         copiedName += " " + counter;
      }
      return copiedName;
   }

   boolean canRenameTo (NamedEntity member, String futureName) {
      return isValidName(futureName) &&
            !containsAnyVariantOf(futureName) || areNameVariants(member.getName(), futureName);
      // allow renaming, if name does not change (after normalization), i.e. futureName "conflict" with the entity itself
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
   // generated names have one leading prefix more than normal user names
   private static final String PYTHON_STATIC_NAME_PREFIX = "_glob_";  // we disallow leading underscores

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

   public static String buildStaticPythonName (String purposeDiscriminator, String name) {
      if (purposeDiscriminator.isEmpty() || purposeDiscriminator.startsWith("_") || purposeDiscriminator.endsWith("_"))
         throw new IllegalArgumentException("invalid Python name fragment " + purposeDiscriminator);
      return PYTHON_STATIC_NAME_PREFIX + purposeDiscriminator + "_" + normalizedName(name);
   }

   private class NamespaceModel extends AbstractListModel<T> {
      private List<T> elements;
      private boolean notifyListeners;

      NamespaceModel () {
         elements = new ArrayList<>(members.values());
         elements.sort(null);
         notifyListeners = true;
      }

      void setNotificationsEnabled (boolean enabled) { notifyListeners = enabled; }

      void addElement (T element) {
         int oldSize = elements.size();
         elements.add(element);
         elements.sort(null);
         if (notifyListeners) {
            fireContentsChanged(this, 0, oldSize - 1);
            fireIntervalAdded(this, oldSize, oldSize);
         }
      }

      void renamedElement () {
         // name has already been changed, just update order
         // could try to optimize the case, where the position was not changed
         elements.sort(null);
         if (notifyListeners)
            fireContentsChanged(this, 0, elements.size() - 1);
      }

      void removeElement (T element) {
         int pos = elements.indexOf(element);
         assert pos >= 0;
         elements.remove(pos);
         if (notifyListeners)
            fireIntervalRemoved(this, pos, pos);
      }

      void clear () {
         int oldSize = elements.size();
         elements.clear();
         if (notifyListeners)
            fireIntervalRemoved(this, 0, oldSize - 1);
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

   private static Comparator<String> naturalOrderComparator = new SmartNumericSortOrder();

   public static Comparator<String> getSmartNumericOrderComparator () {
      return naturalOrderComparator;
   }

   private static class SmartNumericSortOrder implements Comparator<String> {
      @Override
      public int compare (String s1, String s2) {
         // Skip all identical characters
         int len1 = s1 != null ? s1.length() : 0;
         int len2 = s2 != null ? s2.length() : 0;
         int i = 0;
         char c1 = 0, c2 = 0;
         while (i < len1 && i < len2 && (c1 = s1.charAt(i)) == (c2 = s2.charAt(i)))
            i++;

         // Check end of string
         if (c1 == c2)
            return len1 - len2;

         // Check digit in first string
         if (Character.isDigit(c1))
         {
            // Check digit only in first string
            if (!Character.isDigit(c2))
               return i > 0 && Character.isDigit(s1.charAt(i - 1)) ? 1 : c1 - c2;

            // Scan all digits
            int x1 = i + 1, x2 = i + 1;
            while (x1 < len1 && Character.isDigit(s1.charAt(x1)))
               x1++;
            while (x2 < len2 && Character.isDigit(s2.charAt(x2)))
               x2++;

            // Longer digit run wins, first digit otherwise
            //  should be good enough although we support transparent differences in number of leading zeroes otherwise:
            // comparison applies to siblings in same directory and these should follow a consistent numbering scheme,
            // either relying on "natural" sorting or padded with leading zeroes to lexicographic digit sequences
            return x2 == x1 ? c1 - c2 : x1 - x2;
         }

         // Check digit only in second string
         if (Character.isDigit(c2))
            return i > 0 && Character.isDigit(s2.charAt(i - 1)) ? -1 : c1 - c2;

         // No digits
         return c1 - c2;
      }
   }

}
