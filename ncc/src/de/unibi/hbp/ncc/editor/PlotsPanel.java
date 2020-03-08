package de.unibi.hbp.ncc.editor;

import de.unibi.hbp.ncc.lang.DataPlot;
import de.unibi.hbp.ncc.lang.Namespace;
import de.unibi.hbp.ncc.lang.utils.Images;

import javax.imageio.ImageIO;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlotsPanel {
   private PlotListModel model;
   private Component component;

   private static class PlotImage {
      BufferedImage fullImage;
      ImageIcon thumbnail;
      DataPlot plotEntity;
      String title;
      JFrame detailsWindow;

      PlotImage (BufferedImage fullImage, DataPlot plotEntity, String fallBackTitle) {
         this.fullImage = fullImage;
         // this.thumbnail created on demand
         this.plotEntity = plotEntity;
         this.title = fallBackTitle;
      }

      private static final int DUMMY_SIZE = 16;

      PlotImage (String errorMessage) {
         BufferedImage dummy = new BufferedImage(DUMMY_SIZE, DUMMY_SIZE, BufferedImage.TYPE_INT_RGB);
         Graphics2D g2 = dummy.createGraphics();
         g2.setBackground(Color.RED);
         g2.clearRect(0, 0, DUMMY_SIZE, DUMMY_SIZE);
         g2.dispose();
         this.thumbnail = new ImageIcon(dummy);
         this.title = errorMessage;
      }

      ImageIcon getThumbnail () {
         if (thumbnail == null) {
            thumbnail = new ImageIcon(Images.fit(fullImage, 360, 120));
         }
         return thumbnail;
      }

      JFrame showDetailsWindow (Component locationReference) {
         if (detailsWindow == null) {
            JFrame frame = new JFrame(title);
            // could use a JPanel with a paintComponent override to avoid creating the icon
            if (fullImage != null)
               frame.add(new JLabel(new ImageIcon(fullImage)));
            else  // just show the red marker plus the (full) error text
               frame.add(new JLabel(title, getThumbnail(), SwingConstants.CENTER));
            frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(locationReference);
            frame.setVisible(true);
            detailsWindow = frame;
         }
         else
            detailsWindow.toFront();
         return detailsWindow;
      }

      String getTitle () {
         if (plotEntity != null)
            return plotEntity.getLongDisplayName();
         else
            return title;
      }
   }

   private DataPlot findPlotFor (Namespace<DataPlot> allPlots, String outputFileName) {
      for (DataPlot plot: allPlots)
         if (outputFileName.equals(plot.getOutputFileName()))
            return plot;
      return null;
   }

   public PlotsPanel (List<File> plotImageFiles, Namespace<DataPlot> allPlots) {
      List<PlotImage> plotImages = new ArrayList<>(plotImageFiles.size());
      for (File plotImageFile: plotImageFiles) {
         PlotImage plotImage;
         try {
            BufferedImage image = ImageIO.read(plotImageFile);
            String fileName = plotImageFile.getName();
            plotImage = new PlotImage(image, findPlotFor(allPlots, fileName), fileName);
         }
         catch (IOException ioe) {
            System.err.println("File " + plotImageFile);
            ioe.printStackTrace(System.err);
            plotImage = new PlotImage(plotImageFile.getName() + ": " + ioe.getMessage());
         }
         plotImages.add(plotImage);
      }
      model = new PlotListModel(plotImages);
   }

   public boolean isEmpty () { return model.getSize() < 1; }

   public Component buildComponent () {
      if (component == null) {
         JList<PlotImage> plotList = new JList<>(model);
         plotList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         plotList.setCellRenderer(new PlotRenderer());
         plotList.addMouseListener(
               new MouseAdapter() {
                  @Override
                  public void mouseClicked (MouseEvent e) {
                     if (e.getClickCount() == 2) {
                        int index = plotList.locationToIndex(e.getPoint());
                        PlotImage plotImage = model.getElementAt(index);
                        plotImage.showDetailsWindow(plotList);
                     }
                  }
               }
         );
         component = new JScrollPane(plotList);
      }
      return component;
   }

   private static class PlotRenderer extends DefaultListCellRenderer {
      @Override
      public Component getListCellRendererComponent (JList<?> list, Object value, int index, boolean isSelected,
                                                     boolean cellHasFocus) {
         PlotImage plotImage = (PlotImage) value;
         Component component = super.getListCellRendererComponent(list, plotImage.getTitle(), index,
                                                                        isSelected, cellHasFocus);
         // DefaultListCellRenderer returns this (a JLabel) so configure it according to our needs
         setIcon(plotImage.getThumbnail());
         setToolTipText(plotImage.getTitle());
         return component;
      }
   }

   private static class PlotListModel extends AbstractListModel<PlotImage> {
      private List<PlotImage> plotImages;

      public PlotListModel (List<PlotImage> plotImages) {
         this.plotImages = plotImages;
      }

      @Override
      public int getSize () { return plotImages.size(); }

      @Override
      public PlotImage getElementAt (int index) {
         return plotImages.get(index);
      }
   }
}
