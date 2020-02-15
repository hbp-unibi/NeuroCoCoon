package de.unibi.hbp.ncc.lang.codegen;

import java.io.Writer;

public class BufferAppender extends Writer {
   private final StringBuilder buffer;

   public BufferAppender (StringBuilder buffer) {
      this.buffer = buffer;
   }

   public BufferAppender (int initialCapacity) { this(new StringBuilder(initialCapacity)); }

   private static final int DEFAULT_INITIAL_CAPACITY = 4 << 10;

   public BufferAppender () { this(DEFAULT_INITIAL_CAPACITY); }

   // overrides more methods than necessary (all methods?) to avoid locking and delegation in parent class
   @Override
   public void write (char[] cbuf, int off, int len) {
      buffer.append(cbuf, off, len);
   }

   @Override
   public void write (int c) {
      buffer.append((char) c);
   }

   @Override
   public void write (char[] cbuf) {
      buffer.append(cbuf);
   }

   @Override
   public void write (String str)  {
      buffer.append(str);
   }

   @Override
   public void write (String str, int off, int len) {
      buffer.append(str, off, len);
   }

   @Override
   public Writer append (CharSequence csq)  {
      buffer.append(csq);
      return this;
   }

   @Override
   public Writer append (CharSequence csq, int start, int end) {
      buffer.append(csq, start, end);
      return this;
   }

   @Override
   public Writer append (char c) {
      buffer.append(c);
      return this;
   }

   @Override
   public void flush () { }

   @Override
   public void close () { }

   public CharSequence getResult () { return buffer; }
}
