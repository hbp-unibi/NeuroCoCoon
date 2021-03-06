package de.unibi.hbp.ncc.lang.props;

import de.unibi.hbp.ncc.lang.LanguageEntity;

public class StrictlyPositiveIntegerProp extends IntegerProp {

   public StrictlyPositiveIntegerProp (String propName, LanguageEntity owner, Integer value) {
      super(propName, owner, value);
   }

   @Override
   public boolean isValid (Integer proposedValue) {
      return super.isValid(proposedValue) && proposedValue > 0;
   }
}
