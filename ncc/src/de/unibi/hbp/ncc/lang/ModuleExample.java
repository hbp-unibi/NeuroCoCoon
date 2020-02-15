package de.unibi.hbp.ncc.lang;

import java.util.Arrays;

public class ModuleExample extends NetworkModule {
   public ModuleExample (Namespace<NetworkModule> namespace, String name) {
      super(namespace, name, Arrays.asList("in X", "in Y"), Arrays.asList("out X", "out Y"));
   }

   public ModuleExample (String namePrefix) {
      this(getGlobalNamespace(), getGlobalNamespace().generateSpecificName(namePrefix));
   }

   public ModuleExample () {
      this(getGlobalNamespace(), null);
   }

   @Override
   public LanguageEntity duplicate () {
      throw new UnsupportedOperationException("TODO");
      // return null;
   }

   public static final EntityCreator<ModuleExample> CREATOR = new Creator();

   private static class Creator implements EntityCreator<ModuleExample> {
      @Override
      public ModuleExample create () {
         return new ModuleExample(toString());
      }

      @Override
      public String toString () {  // used by drag&drop tooltips
         return "Example Module";
      }
   }

}
