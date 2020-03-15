package de.unibi.hbp.ncc.editor;

import de.unibi.hbp.ncc.env.JavaScriptBridge;
import de.unibi.hbp.ncc.env.NmpiClient;
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
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PlotsPanel {
   private PlotListModel model;
   private Component component;

   private static class PlotImage {
      BufferedImage fullImage;
      ImageIcon thumbnail;
      DataPlot plotEntity;
      String title, url;
      JFrame detailsWindow;

      PlotImage (BufferedImage fullImage, DataPlot plotEntity, String fallBackTitle) {
         this.fullImage = fullImage;
         // this.thumbnail created on demand
         this.plotEntity = plotEntity;
         this.title = fallBackTitle;
      }

      private static final int DUMMY_SIZE = 24;

      PlotImage (Color marker, String message) {
         BufferedImage dummy = new BufferedImage(DUMMY_SIZE, DUMMY_SIZE, BufferedImage.TYPE_INT_RGB);
         Graphics2D g2 = dummy.createGraphics();
         g2.setBackground(marker);
         g2.clearRect(0, 0, DUMMY_SIZE, DUMMY_SIZE);
         g2.dispose();
         this.thumbnail = new ImageIcon(dummy);
         this.title = message;
      }

      private static final Color WEB_PLACEHOLDER_COLOR = new Color(0x8888cc);

      PlotImage (DataPlot plotEntity, String url) {
         this(WEB_PLACEHOLDER_COLOR, url);
         this.plotEntity = plotEntity;
         this.url = url;
      }

      ImageIcon getThumbnail () {
         if (thumbnail == null) {
            thumbnail = new ImageIcon(Images.fit(fullImage, 360, 120));
         }
         return thumbnail;
      }

      JFrame showDetailsWindow (Component locationReference) {
         if (url != null) {
            JavaScriptBridge.showOnPage(url, "plotDisplay");
            // ncc.html provides an img element with id plotDisplay
            return null;
         }
         if (detailsWindow == null) {
            JFrame frame = new JFrame(title);
            // could use a JPanel with a paintComponent override to avoid creating the icon
            if (fullImage != null)
               frame.add(new JLabel(new ImageIcon(fullImage)));
            else  // just show the colored marker plus the (full) (error) text
               frame.add(new JLabel(title, getThumbnail(), SwingConstants.CENTER));
            frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(locationReference);
            frame.setVisible(true);
            detailsWindow = frame;
         }
         else {
            detailsWindow.setVisible(true);
            detailsWindow.toFront();
         }
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
            plotImage = new PlotImage(Color.RED, plotImageFile.getName() + ": " + ioe.getMessage());
         }
         plotImages.add(plotImage);
      }
      model = new PlotListModel(plotImages);
   }

   private static String extractFileName (String url) {
      int lastSlash = url.lastIndexOf('/');
      if (lastSlash >= 0)
         return url.substring(lastSlash + 1);
      else
         return url;  // keep full URL, if there is no slash
   }

   // boolean parameter only supplied to make the constructor signatures different (after erasure)
   public PlotsPanel (List<String> plotImageURLs, Namespace<DataPlot> allPlots, boolean externalURLs) {
      List<PlotImage> plotImages = new ArrayList<>(plotImageURLs.size());
      for (String plotImageURL: plotImageURLs) {
         String fileName = extractFileName(plotImageURL);
         PlotImage plotImage = new PlotImage(findPlotFor(allPlots, fileName), plotImageURL);
         plotImages.add(plotImage);
      }
      model = new PlotListModel(plotImages);
   }

   public PlotsPanel (List<URL> plotImageURLs, Namespace<DataPlot> allPlots, NmpiClient client) {
      List<PlotImage> plotImages = new ArrayList<>(plotImageURLs.size());
      for (URL plotImageURL: plotImageURLs) {
         PlotImage plotImage;
         JavaScriptBridge.getRequest(plotImageURL.toString());
         try (InputStream in = client.authorizedInput(plotImageURL)) {
            BufferedImage image = ImageIO.read(in);
            String fileName = plotImageURL.getPath();
            plotImage = new PlotImage(image, findPlotFor(allPlots, fileName), fileName);
         }
         catch (IOException ioe) {
            System.err.println("URL " + plotImageURL);
            ioe.printStackTrace(System.err);
            plotImage = new PlotImage(Color.RED, plotImageURL.getPath() + ": " + ioe.getMessage());
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
