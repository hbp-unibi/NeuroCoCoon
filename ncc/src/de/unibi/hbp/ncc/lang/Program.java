package de.unibi.hbp.ncc.lang;

public class Program {
   private final Scope global;

   public Program () {
      global = new Scope();
   }

   public Scope getGlobalScope () {
      return global;
   }
}
