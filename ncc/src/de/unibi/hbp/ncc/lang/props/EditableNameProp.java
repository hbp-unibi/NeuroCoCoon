package de.unibi.hbp.ncc.lang.props;

import de.unibi.hbp.ncc.editor.props.ComboBoxModelAdapter;
import de.unibi.hbp.ncc.lang.LanguageEntity;
import de.unibi.hbp.ncc.lang.NamedEntity;
import de.unibi.hbp.ncc.lang.Namespace;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import java.util.Objects;

public class EditableNameProp<E extends NamedEntity> extends SimpleEditableProp<E>
      implements NameProp<E> {
   private Namespace<E> targetNamespace;

   public EditableNameProp (String propName, Class<E> valueClass, LanguageEntity owner,
                            E value, Namespace<E> targetNamespace) {
      super(propName, valueClass, owner, Objects.requireNonNull(value));
      this.targetNamespace = Objects.requireNonNull(targetNamespace);
   }

   @Override
   public void setValueFromString (String encodedValue) { setValue(targetNamespace.get(encodedValue)); }

   @Override
   public EditableNameProp<E> addImpact (Impact impact) {  // to get the more precise co-variant return type
      super.addImpact(impact);
      return this;
   }

   @Override
   public Namespace<E> getTargetNamespace () { return targetNamespace; }

   @Override
   public NamedEntity getTargetEntity () { return getValue(); }

   @Override
   public TableCellEditor getTableCellEditor (JTable table) {
      JComboBox<E> comboBox = new JComboBox<>(new ComboBoxModelAdapter<>(targetNamespace.getListModel()));
      comboBox.setEditable(false);
      // comboBox.setInputVerifier();
      return new DefaultCellEditor(comboBox);
   }
}
