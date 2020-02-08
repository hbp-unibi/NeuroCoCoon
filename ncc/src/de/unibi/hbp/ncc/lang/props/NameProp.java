package de.unibi.hbp.ncc.lang.props;

import de.unibi.hbp.ncc.lang.NamedEntity;
import de.unibi.hbp.ncc.lang.Namespace;

public interface NameProp<E extends NamedEntity<E>> {
   Namespace<E> getTargetNamespace ();
   NamedEntity<E> getTargetEntity ();
}
