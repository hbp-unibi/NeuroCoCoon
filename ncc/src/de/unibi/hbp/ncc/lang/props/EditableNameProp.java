package de.unibi.hbp.ncc.lang.props;

import de.unibi.hbp.ncc.lang.LanguageEntity;
import de.unibi.hbp.ncc.lang.NamedEntity;
import de.unibi.hbp.ncc.lang.Namespace;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import java.util.Objects;

public class EditableNameProp<E extends NamedEntity<E>> extends SimpleEditableProp<E> implements NameProp<E> {
   private Namespace<E> targetNamespace;

   public EditableNameProp (String propName, Class<E> valueClass, LanguageEntity owner,
                            E value, Namespace<E> targetNamespace) {
      super(propName, valueClass, owner, value);
      this.targetNamespace = Objects.requireNonNull(targetNamespace);
   }

   @Override
   public boolean isValid (E proposedValue) {
      return super.isValid(proposedValue) && proposedValue != null;
   }

   @Override
   public Namespace<E> getTargetNamespace () { return targetNamespace; }

   @Override
   public NamedEntity<E> getTargetEntity () {
      return getValue();
   }

   @Override
   public TableCellEditor getTableCellEditor (JTable table) {
      JComboBox<E> comboBox = new JComboBox<>(targetNamespace.getModel());
      comboBox.setEditable(false);
      // comboBox.setInputVerifier();
      return new DefaultCellEditor(comboBox);
   }
}
