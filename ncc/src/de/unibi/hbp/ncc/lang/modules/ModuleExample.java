package de.unibi.hbp.ncc.lang.modules;

import de.unibi.hbp.ncc.editor.EntityCreator;
import de.unibi.hbp.ncc.editor.ModuleInstanceCreator;
import de.unibi.hbp.ncc.lang.LanguageEntity;
import de.unibi.hbp.ncc.lang.Namespace;
import de.unibi.hbp.ncc.lang.NetworkModule;

import java.util.Arrays;
import java.util.List;

public class ModuleExample extends NetworkModule {
   private List<Integer> inputPortDimensions, outputPortDimensions;

   @Override
   protected String getGeneratedNamesPrefix () { return CREATOR.getIconCaption(); }

   public ModuleExample (Namespace<NetworkModule> namespace, String name) {
      super(namespace, name, CREATOR.getIconFileName());
      inputPortDimensions = Arrays.asList(1, 2);
      outputPortDimensions = Arrays.asList(4, 8);
   }

   public ModuleExample () {
      this(getGlobalNamespace(), null);
   }

   @Override
   public LanguageEntity duplicate () {
      throw new UnsupportedOperationException("TODO");
      // return null;
   }

   @Override
   protected List<String> getPortNames (Port.Direction direction) {
      if (direction == Port.Direction.IN)
         return Arrays.asList("in X", "in Y");
      else if (direction == Port.Direction.OUT)
         return Arrays.asList("out X", "out Y", "gq M");
      else
         throw new IllegalArgumentException("Unexpected direction: " + direction);
   }

   @Override
   protected int getPortDimension (Port.Direction direction, int portIndex) {
      if (direction == Port.Direction.IN)
         return inputPortDimensions.get(portIndex);
      else if (direction == Port.Direction.OUT)
         return outputPortDimensions.get(portIndex);
      else
         throw new IllegalArgumentException("Unexpected direction: " + direction);
   }

   public static final EntityCreator<ModuleExample> CREATOR = new Creator();

   private static class Creator extends ModuleInstanceCreator<ModuleExample> {
      @Override
      public ModuleExample create () {
         return new ModuleExample();
      }

      @Override
      public String getIconFileName () { return "module.png"; }

      @Override
      public String getIconCaption () { return "Example"; }
   }

}
