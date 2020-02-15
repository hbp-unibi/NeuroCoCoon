package de.unibi.hbp.ncc.lang.props;

import de.unibi.hbp.ncc.lang.LanguageEntity;

public class StrictlyPositiveDoubleProp extends DoubleProp {

   public StrictlyPositiveDoubleProp (String propName, LanguageEntity owner, Double value) {
      super(propName, owner, value);
   }

   @Override
   public boolean isValid (Double proposedValue) {
      return super.isValid(proposedValue) && proposedValue > 0.0;
   }
}
