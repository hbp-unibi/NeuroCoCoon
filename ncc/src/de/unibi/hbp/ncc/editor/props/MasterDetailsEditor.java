package de.unibi.hbp.ncc.editor.props;

import com.mxgraph.model.mxCell;
import de.unibi.hbp.ncc.NeuroCoCoonEditor;
import de.unibi.hbp.ncc.graph.AbstractCellsCollector;
import de.unibi.hbp.ncc.lang.LanguageEntity;
import de.unibi.hbp.ncc.lang.NamedEntity;
import de.unibi.hbp.ncc.lang.Namespace;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import java.awt.FlowLayout;
import java.util.function.Function;

public class MasterDetailsEditor<E extends NamedEntity<E>> {
   private final JList<E> masterList;  // TODO use 2 column table with predefined-indicator and editable name
   // TODO how to notify the details editor on rename
   private final DetailsEditor detailsEditor;
   private final JComponent component;

   public MasterDetailsEditor (Namespace<E> namespace, Function<Namespace<E>, E> entityCreator,
                               NeuroCoCoonEditor editor) {
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      masterList = new JList<E>(namespace.getListModel());
      masterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      JScrollPane scrollPane = new JScrollPane(masterList,
                                               ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                                               ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      panel.add(scrollPane);
      component = panel;
      JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
      final JButton addButton = new JButton("New");
      addButton.addActionListener(e -> {
         E entity = entityCreator.apply(namespace);
         if (entity != null)
            masterList.setSelectedValue(entity, true);
         else  // in rare cases creation may fail, e.g., DataPlot requires at least one NeuronPopulation
            JOptionPane.showMessageDialog(component,
                                          "Could not create new " + namespace.getDescription() + "!\n" +
                                                "\nPrecondition not satisfied.",
                                          "Creation failed!", JOptionPane.ERROR_MESSAGE);
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
         E candidateEntity = masterList.getSelectedValue();
         if (candidateEntity != null) {
            if (new AbstractCellsCollector() {
               @Override
               protected boolean matches (mxCell cell, LanguageEntity entity) {
                  return entity.hasReferenceTo(candidateEntity);
               }
            }.haveMatchingCells(editor.getGraphModel()))
               JOptionPane.showMessageDialog(component,
                                             "Could not delete " + namespace.getDescription() +
                                                   "'" + candidateEntity.getName() + "'" + "!\n" +
                                                   "References to this entity still exist.",
                                             "Deletion failed!", JOptionPane.ERROR_MESSAGE);
            else
               namespace.remove(candidateEntity.getName());
         }
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
   }

   public JComponent getComponent () {
      return component;
   }
}
