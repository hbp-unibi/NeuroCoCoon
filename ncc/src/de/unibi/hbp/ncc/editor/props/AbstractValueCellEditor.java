package de.unibi.hbp.ncc.editor.props;

import de.unibi.hbp.ncc.lang.props.EditableProp;

import javax.swing.DefaultCellEditor;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public abstract class AbstractValueCellEditor<T> extends SelectAllCellEditor {
   private EditableProp<T> prop;
   // private T value;

   protected AbstractValueCellEditor (EditableProp<T> prop) {
      this(prop, SwingConstants.LEFT);
   }

   protected AbstractValueCellEditor (EditableProp<T> prop, int align) {
      this(prop, align, new JTextField());
   }

   protected AbstractValueCellEditor (EditableProp<T> prop, int align, JTextField textField) {
      super(textField);
      this.prop = prop;
      textField.setHorizontalAlignment(align);
      textField.setInputVerifier(new InputVerifier() {
         @Override
         public boolean verify (JComponent input) {
            String s = ((JTextField) input).getText();
            return convertFromString(s != null ? s : "") != null;
         }
      });
   }

   protected abstract T convertFromString (String s);  // returns null, iff s is invalid
   protected String convertToString (T value) { return value.toString(); }

   @Override
   public boolean stopCellEditing() {
      Object value = getCellEditorValue();
      ((JComponent) getComponent()).setBorder(new LineBorder(value == null ? Color.RED : Color.BLACK));
      if (value == null) {
         // System.err.println("stopCellEditing: false");
         return false;
      }
      // System.err.println("stopCellEditing: true, " + value);
      return super.stopCellEditing();
   }

   @Override
   public Component getTableCellEditorComponent(JTable table, Object value,
                                                boolean isSelected,
                                                int row, int column) {
      ((JComponent) getComponent()).setBorder(new LineBorder(Color.BLACK));
      @SuppressWarnings("unchecked")
      String s = value != null ? convertToString((T) value) : "";
      return super.getTableCellEditorComponent(table, s, isSelected, row, column);
   }

   @Override
   public Object getCellEditorValue() {
      String s = (String) super.getCellEditorValue();
      T value = convertFromString(s);
      if (value != null && !prop.isValid(value))
         value = null;
      // System.err.println("getCellEditorValue: " + value);
      return value;
   }
}
