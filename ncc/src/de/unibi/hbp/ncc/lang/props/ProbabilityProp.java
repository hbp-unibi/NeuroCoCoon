package de.unibi.hbp.ncc.lang.props;

import de.unibi.hbp.ncc.lang.LanguageEntity;

public class ProbabilityProp extends NonNegativeDoubleProp {

   public ProbabilityProp (String propName, LanguageEntity owner, Double value) {
      super(propName, owner, value);
   }

   @Override
   public boolean isValid (Double proposedValue) { return super.isValid(proposedValue) && proposedValue <= 1.0; }

}
