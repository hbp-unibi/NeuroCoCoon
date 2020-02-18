package de.unibi.hbp.ncc.editor;

import de.unibi.hbp.ncc.lang.NetworkModule;

public abstract class ModuleInstanceCreator<M extends NetworkModule> implements EntityCreator<M> {

   @Override
   public String toString () {
      return getIconCaption() + " Module";
   }

   @Override
   public String getCellStyle () { return "module"; }

   @Override
   public int getInitialCellHeight () { return 60; }
}
