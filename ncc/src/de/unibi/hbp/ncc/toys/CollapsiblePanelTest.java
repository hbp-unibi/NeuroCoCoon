package de.unibi.hbp.ncc.toys;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CollapsiblePanelTest {
}

class JCollapsiblePanel extends JPanel {
   private TitledBorder border;
   private Dimension visibleSize;
   private boolean collapsible;

   public JCollapsiblePanel(String title, Color titleCol) {
      super();

      collapsible = true;

      border = new TitledBorder(title);
      border.setTitleColor(titleCol);
      border.setBorder(new LineBorder(Color.white));
      setBorder(border);

      // as Titleborder has no access to the Label we fake the size data ;)
      final JLabel l = new JLabel(title);
      Dimension size = l.getPreferredSize();

      addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent e) {
            if (!collapsible) {
               return;
            }

            Insets i = getBorder().getBorderInsets(JCollapsiblePanel.this);
            if (e.getX() < i.left + size.width && e.getY() < i.bottom + size.height) {
               if (visibleSize == null || getHeight() > size.height) {
                  visibleSize = getSize();
               }
               if (getSize().height < visibleSize.height) {
                  setMaximumSize(new Dimension(visibleSize.width, 20000));
                  setMinimumSize(visibleSize);
               } else {
                  setMaximumSize(new Dimension(visibleSize.width, size.height));
               }
               revalidate();
               e.consume();
            }
         }
      });
   }

   public void setCollapsible(boolean collapsible) {
      this.collapsible = collapsible;
   }

   public void setTitle(String title) {
      border.setTitle(title);
   }
}