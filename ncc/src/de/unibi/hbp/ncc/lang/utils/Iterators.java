package de.unibi.hbp.ncc.lang.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public final class Iterators {

   private Iterators () { }

   private static abstract class IteratorSequence<E> implements Iterator<E> {
      protected Iterator<? extends E> currentIterator;

      protected abstract Iterator<? extends E> findNextNonEmptyIteratorOrNull ();

      @Override
      public boolean hasNext () {
         return currentIterator != null && currentIterator.hasNext();
      }

      @Override
      public E next () {
         E elem = currentIterator.next();
         if (!currentIterator.hasNext())
            currentIterator = findNextNonEmptyIteratorOrNull();
         return elem;
      }
   }

   private static abstract class RemovableIteratorSequence<E> extends IteratorSequence<E> {
      private Iterator<? extends E> lastElemIterator;

      @Override
      public E next () {
         lastElemIterator = currentIterator;
         return super.next();
      }

      @Override
      public void remove () {
         lastElemIterator.remove();
      }

   }

   private static class ConcatIterator<E, S> extends RemovableIteratorSequence<E> {
      private S[] sources;
      private Function<S, Iterator<? extends E>> sourceMapper;
      private int currentIndex;

      ConcatIterator (S[] sources, Function<S, Iterator<? extends E>> sourceMapper) {
         this.sources = sources;
         this.sourceMapper = sourceMapper;
         currentIndex = -1;
         currentIterator = findNextNonEmptyIteratorOrNull();
      }

      protected Iterator<? extends E> findNextNonEmptyIteratorOrNull () {
         currentIndex += 1;
         while (currentIndex < sources.length) {
            Iterator<? extends E> iter = sourceMapper.apply(sources[currentIndex]);
            if (iter.hasNext())
               return iter;
            currentIndex += 1;
         }
         return null;
      }
   }

   @SafeVarargs
   public static <E> Iterator<E> concat (Iterator<? extends E>... iters) {
      return new ConcatIterator<>(iters, Function.identity());
   }

   @SafeVarargs
   public static <E> Iterable<E> concat (Iterable<? extends E>... sources) {
      return () -> new ConcatIterator<>(sources, Iterable::iterator);
   }

   private static class ExpandingIterator<T, E> extends IteratorSequence<E> {

      private Iterator<? extends T> inputIter;
      private Function<T, Collection<? extends E>> expander;

      ExpandingIterator (Iterator<? extends T> inputIter, Function<T, Collection<? extends E>> expander) {
         this.inputIter = inputIter;
         this.expander = expander;
         currentIterator = findNextNonEmptyIteratorOrNull();
      }

      protected Iterator<? extends E> findNextNonEmptyIteratorOrNull () {
         while (inputIter.hasNext()) {
            Iterator<? extends E> iter = expander.apply(inputIter.next()).iterator();
            if (iter.hasNext())
               return iter;
         }
         return null;
      }
   }

   public static <T, E> Iterator<E> expand (Iterator<? extends T> iter, Function<T, Collection<? extends E>> expander) {
      return new ExpandingIterator<>(iter, expander);
   }

   public static <T, E> Iterable<E> expand (Iterable<? extends T> source, Function<T, Collection<? extends E>> expander) {
      return () -> new ExpandingIterator<>(source.iterator(), expander);
   }

   private static class FilteringIterator<E> implements Iterator<E> {
      private Iterator<? extends E> baseIter;
      private Predicate<E> filter;  // keeps (preserves) only elements where the filter predicate returns true
      private E nextElem;
      private boolean moreElements;

      FilteringIterator (Iterator<? extends E> baseIter, Predicate<E> filter) {
         this.baseIter = baseIter;
         this.filter = filter;
         moreElements = true;
         findNextUnfilteredElement();
      }

      protected void findNextUnfilteredElement() {
         while (baseIter.hasNext()) {
            nextElem = baseIter.next();
            if (filter.test(nextElem))
               return;
         }
         moreElements = false;
      }

      @Override
      public boolean hasNext () {
         return moreElements;
      }

      @Override
      public E next () {
         E elem = nextElem;
         findNextUnfilteredElement();
         return elem;
      }
   }

   public static <E> Iterator<E> filter (Iterator<? extends E> iter, Predicate<E> filter) {
      return new FilteringIterator<>(iter, filter);
   }

   public static <E> Iterable<E> filter (Iterable<? extends E> source, Predicate<E> filter) {
      return () -> new FilteringIterator<>(source.iterator(), filter);
   }

   public static <E> boolean hasAny (Iterator<E> iter, Predicate<E> filter) {
      while (iter.hasNext())
         if (filter.test(iter.next()))
            return true;
      return false;
   }

   public static <E> boolean hasAll (Iterator<E> iter, Predicate<E> filter) {
      while (iter.hasNext())
         if (!filter.test(iter.next()))
            return false;
      return true;
   }

   private static class MappingIterator<E, T> implements Iterator<T> {
      private Iterator<? extends E> baseIter;
      private Function<E, T> mapper;

      MappingIterator (Iterator<? extends E> baseIter, Function<E, T> mapper) {
         this.baseIter = baseIter;
         this.mapper = mapper;
      }

      @Override
      public boolean hasNext () {
         return baseIter.hasNext();
      }

      @Override
      public T next () {
         return mapper.apply(baseIter.next());
      }

      @Override
      public void remove () {
         baseIter.remove();
      }
   }

   public static <E, T> Iterator<T> map (Iterator<? extends E> iter, Function<E, T> mapper) {
      return new MappingIterator<>(iter, mapper);
   }

   public static <E, T> Iterable<T> map (Iterable<? extends E> source, Function<E, T> mapper) {
      return () -> new MappingIterator<>(source.iterator(), mapper);
   }

   private static class PartialMappingIterator<E, T> implements Iterator<T> {
      private Iterator<? extends E> baseIter;
      private Function<E, T> partialMapper;
      private T nextMappedElem;
      private boolean moreElements;

      PartialMappingIterator (Iterator<? extends E> baseIter, Function<E, T> partialMapper) {
         this.baseIter = baseIter;
         this.partialMapper = partialMapper;
         moreElements = true;
         findNextMappedElement();
      }

      protected void findNextMappedElement() {
         while (baseIter.hasNext()) {
            nextMappedElem = partialMapper.apply(baseIter.next());
            if (nextMappedElem != null)
               return;
         }
         moreElements = false;
      }

      @Override
      public boolean hasNext () {
         return moreElements;
      }

      @Override
      public T next () {
         T mappedElem = nextMappedElem;
         findNextMappedElement();
         return mappedElem;
      }
   }

   public static <E, T> Iterator<T> partialMap (Iterator<? extends E> iter, Function<E, T> mapper) {
      return new PartialMappingIterator<>(iter, mapper);
   }

   public static <E, T> Iterable<T> partialMap (Iterable<? extends E> source, Function<E, T> mapper) {
      return () -> new PartialMappingIterator<>(source.iterator(), mapper);
   }

   private static class SplitStringIterator implements Iterator<String> {
      private String fullText;
      private char separator;
      private int lastSeparatorPos;

      public SplitStringIterator (String fullText, char separator) {
         this.fullText = fullText;
         this.separator = separator;
         lastSeparatorPos = -1;
      }

      @Override
      public boolean hasNext () {
         return lastSeparatorPos < fullText.length();
      }

      @Override
      public String next () {
         int nextSeparatorPos = fullText.indexOf(separator, lastSeparatorPos + 1);
         if (nextSeparatorPos < 0) nextSeparatorPos = fullText.length();
         String elem = fullText.substring(lastSeparatorPos + 1, nextSeparatorPos);
         lastSeparatorPos = nextSeparatorPos;
         return elem;
      }
   }

   public static Iterator<String> splitIterator (String s, char separator) {
      if (s.isEmpty())
         return Collections.emptyIterator();
      else
         return new SplitStringIterator(s, separator);
   }

   public static Iterable<String> split (String s, char separator) {
      if (s.isEmpty())
         return Collections::emptyIterator;
      else
         return () -> new SplitStringIterator(s, separator);
   }

   public static <T> boolean collect (Iterator<T> iter, T[] destination, boolean exactFit) {
      int len = destination.length;
      for (int i = 0; i < len; i++)
         if (iter.hasNext())
            destination[i] = iter.next();
         else
            return false;  // too few elements in iteration
      return !exactFit || !iter.hasNext();  // all elements consumed or caller does not care
   }

   public static <T> List<T> asList (Iterator<T> iter) {
      List<T> result = new ArrayList<>();
      while (iter.hasNext())
         result.add(iter.next());
      return result;
   }

   public static <T> List<T> asList (Iterable<T> iterable) {
      return asList(iterable.iterator());
   }

   public static <T> void addAll (Collection<? super T> target, Iterator<T> iter) {
      while (iter.hasNext())
         target.add(iter.next());
   }

   public static <T> void addAll (Collection<? super T> target, Iterable<T> iterable) {
      addAll(target, iterable.iterator());
   }

   public static <T> Iterable<T> emptyIterable () {
      return Collections::emptyIterator;
   }
}