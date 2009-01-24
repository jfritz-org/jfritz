package de.moonflower.jfritz.utils;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class MultiLabel extends JComponent implements SwingConstants
{
    /**
	 *
	 */
	private static final long serialVersionUID = 2138955409962236274L;

	int numLines,lineHeight,lineAscent;
    int maxWidth  = -1;
    int textHeight = -1;
    int[] lineWidths;
    int btnMarginWidth =1;

    private String lines[]=null;

    private int horizontalAlignment = LEADING;
    private int textAlignment       = LEADING;
    private int verticalAlignment   = CENTER;

    public MultiLabel(String text)
    {
        this(text, LEADING, LEADING, CENTER);
    }
    public MultiLabel(String text, int horizontalAlignment)
    {
        this(text, horizontalAlignment, LEADING, CENTER);
    }
    public MultiLabel(String text, int horizontalAlignment, int textAlignment)
    {
        this(text, horizontalAlignment, textAlignment, CENTER);
    }
    public MultiLabel(String str, int horizontalAlignment, int textAlignment, int verticalAlignment)
    {
        this.setForeground(UIManager.getColor("Label.foreground"));
        this.setBackground(UIManager.getColor("Label.background"));
        this.setFont      (UIManager.getFont ("Label.font"));

        setText(str);
        this.horizontalAlignment = horizontalAlignment;
        this.textAlignment = textAlignment;
        this.verticalAlignment = verticalAlignment;
    }

    public void setText(final String text)
    {
    	String currentText = text;
        if (currentText == null) currentText="";

        StringTokenizer tkn = new StringTokenizer(currentText,"\n");

        numLines = tkn.countTokens();
        lines       = new String[numLines];
        lineWidths = new int   [numLines];

        for (int i=0;i<numLines;i++)
            lines[i] = tkn.nextToken();

        recalculateDimension();
    }

    private void recalculateDimension()
    {
        FontMetrics fontmetrics=getFontMetrics(getFont());

        lineHeight=fontmetrics.getHeight();
        lineAscent=fontmetrics.getAscent();

        maxWidth=0;
        for (int i=0;i<numLines;i++)
        {
            lineWidths[i] = fontmetrics.stringWidth(lines[ i ]);

            maxWidth = Math.max(maxWidth, lineWidths[i]);
        }

        maxWidth += 2*btnMarginWidth;
        textHeight=numLines*lineHeight;
   }


    public Dimension getPreferredSize()
    {
        return getMinimumSize();
    }

    public Dimension getMaximumSize()
    {
        return new Dimension(Short.MAX_VALUE, Short.MAX_VALUE);
    }

    public Dimension getMinimumSize()
    {
        if (maxWidth == -1 || textHeight == -1) recalculateDimension();

        Insets insets = getInsets();

        return new Dimension(maxWidth   + insets.left + insets.right,
                             textHeight + insets.top  + insets.bottom);
    }
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Dimension d = getSize();

        if (d.width  != maxWidth || d.height != textHeight)
            recalculateDimension();

        Insets insets = this.getInsets();

        int y = 0;

        if (verticalAlignment == TOP)
        {
            y = insets.top + lineAscent;
        }
        else if (verticalAlignment == CENTER)
        {
         //These two are must musts:
            y = insets.top + lineAscent;

          //So far it looks like the TOP case, BUT:
            int clientAreaHeight = d.height - insets.top - insets.bottom;
            y = y + (clientAreaHeight - textHeight) / 2;
        }
        else if (verticalAlignment == BOTTOM)
        {
            int clientAreaBottom = d.height - insets.bottom;

            y = clientAreaBottom - textHeight;

            y += lineAscent;
        }


        for (int i=0; i<numLines; i++)
        {
            int ha = getBidiHorizontalAlignment(horizontalAlignment);

            int x = 0;

            if (ha == LEFT)
            {
                ha = getBidiHorizontalAlignment(textAlignment);
                     if (ha == LEFT  ) x = insets.left;
                else if (ha == RIGHT ) x = maxWidth - lineWidths[i] + insets.left;
                else if (ha == CENTER) x = insets.left + (maxWidth - lineWidths[i]) / 2;
            }
            else if (ha == RIGHT)
            {
                ha = getBidiHorizontalAlignment(textAlignment);
                     if (ha == LEFT  ) x = d.width - maxWidth - insets.right;
                else if (ha == RIGHT ) x = d.width - lineWidths[i] - insets.right;
                else if (ha == CENTER) x = d.width - maxWidth - insets.right+ (maxWidth - lineWidths[i]) / 2;
            }
            else if (ha == CENTER)
            {
                ha = getBidiHorizontalAlignment(textAlignment);

             // Just imagine that ha=LEFT (much easier), and follow code
                int  clientAreaWidth = d.width - insets.left - insets.right;
                     if (ha == LEFT  ) x = insets.left + (clientAreaWidth - maxWidth) / 2;
                else if (ha == RIGHT ) x = insets.left + (clientAreaWidth - maxWidth) / 2 + (maxWidth-lineWidths[i]);
                else if (ha == CENTER) x = insets.left + (clientAreaWidth - lineWidths[i]) / 2;
            }
            x+=btnMarginWidth;
            g.drawString(lines[i],x,y);

            y+=lineHeight;
        }
   }

   private int getBidiHorizontalAlignment(final int ha)
   {
		int hAlignment = ha;
		if (hAlignment == LEADING)
		{
		    if (getComponentOrientation().isLeftToRight())
		    	hAlignment = LEFT;
		    else hAlignment = RIGHT;
		}
		else if (hAlignment == TRAILING)
		{
		    if (getComponentOrientation().isLeftToRight())
		    	hAlignment = RIGHT;
		    else hAlignment = LEFT;
		}
		return hAlignment;
    }













    public int getVerticalAlignment()
    {
        return this.verticalAlignment;
    }

    public void setVerticalAlignment(int verticalAlignment)
    {
        this.verticalAlignment = verticalAlignment;
        repaint();
    }



    public int getHorizontalAlignment()
    {
        return this.horizontalAlignment;
    }

    public void setHorizontalAlignment(int horizontalAlignment)
    {
        this.horizontalAlignment = horizontalAlignment;
        repaint();
    }



    public int getTextAlignment()
    {
        return this.textAlignment;
    }

    public void setTextAlignment(int textAlignment)
    {
        this.textAlignment = textAlignment;
        repaint();
    }
}
