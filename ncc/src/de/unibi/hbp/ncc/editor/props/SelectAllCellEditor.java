package de.unibi.hbp.ncc.editor.props;

import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class SelectAllCellEditor extends DefaultCellEditor {
   public SelectAllCellEditor() { this(new JTextField()); }

   public SelectAllCellEditor(final JTextField textField) {
      super(textField);
      textField.addFocusListener( new FocusAdapter() {
         public void focusGained( final FocusEvent e ) {
            textField.selectAll();
         }

         @Override
         public void focusLost (FocusEvent e) {
            // System.err.println("focusLost: " + getCellEditorValue());
            // TODO do this only, iff the containing table is currently editing?
            if (getCellEditorValue() != null)
               stopCellEditing();
            else
               cancelCellEditing();
         }
      });
   }
}
