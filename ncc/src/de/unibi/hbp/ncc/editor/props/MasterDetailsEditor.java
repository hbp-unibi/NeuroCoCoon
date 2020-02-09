package de.unibi.hbp.ncc.editor.props;

import de.unibi.hbp.ncc.lang.NamedEntity;
import de.unibi.hbp.ncc.lang.Namespace;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import java.awt.FlowLayout;
import java.util.function.Function;

public class MasterDetailsEditor<E extends NamedEntity<E>> {
   private final JList<E> masterList;
   private final DetailsEditor detailsEditor;
   private final JComponent component;

   public MasterDetailsEditor (Namespace<E> namespace, Function<Namespace<E>, E> entityCreator) {
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      masterList = new JList<E>(namespace.getListModel());
      masterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      JScrollPane scrollPane = new JScrollPane(masterList,
                                               ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                                               ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      panel.add(scrollPane);
      JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
      final JButton addButton = new JButton("New");
      addButton.addActionListener(e -> {
         E entity = entityCreator.apply(namespace);
         masterList.setSelectedValue(entity, true);
      });
      final JButton duplicateButton = new JButton("Duplicate");
      duplicateButton.addActionListener(e -> {
         E entity = masterList.getSelectedValue();
         if (entity != null)
            entity.duplicate();
         // masterList.setSelectedValue(duplicated, true);
         // preserve the current selection for further duplicates
      });
      final JButton deleteButton = new JButton("Delete");
      deleteButton.addActionListener(e -> {
         // must also check for existing references
         // TODO button functionality
      });
      buttonBar.add(addButton);
      buttonBar.add(duplicateButton);
      buttonBar.add(deleteButton);
      panel.add(buttonBar);
      detailsEditor = new DetailsEditor();
      panel.add(detailsEditor.getComponent());
      masterList.addListSelectionListener(e -> {
         E entity = masterList.getSelectedValue();
         detailsEditor.setSubject(entity);
         duplicateButton.setEnabled(entity != null);
         deleteButton.setEnabled(entity != null && !entity.isPredefined());
      });
      component = panel;
   }

   public JComponent getComponent () {
      return component;
   }
}
