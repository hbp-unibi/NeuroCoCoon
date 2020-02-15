package de.unibi.hbp.ncc.lang.codegen;

public interface CodeGenVisitor {
   void check (ErrorCollector diagnostics);
   StringBuilder visit (StringBuilder code, ErrorCollector diagnostics);
}
