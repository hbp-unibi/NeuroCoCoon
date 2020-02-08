package de.unibi.hbp.ncc.lang;

import java.io.Serializable;

public interface EntityCreator<E extends LanguageEntity> extends Serializable {
   E create ();
}
