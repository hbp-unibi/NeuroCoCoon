package de.unibi.hbp.ncc.lang.props;

import de.unibi.hbp.ncc.lang.NamedEntity;
import de.unibi.hbp.ncc.lang.Namespace;

public interface NameProp<E extends NamedEntity> {
   Namespace<E> getTargetNamespace ();
   NamedEntity getTargetEntity ();
}
