package de.unibi.hbp.ncc.lang;

public class LanguageException extends RuntimeException {
   private LanguageEntity origin;

   public LanguageException (LanguageEntity origin, String message) {
      super(message);
      this.origin = origin;
   }

   public LanguageException (LanguageEntity origin, String message, Throwable cause) {
      super(message, cause);
      this.origin = origin;
   }

   public LanguageException (String message) {
      super(message);
   }

   public LanguageException (String message, Throwable cause) {
      super(message, cause);
   }

   public LanguageEntity getOrigin () { return origin; }
}
