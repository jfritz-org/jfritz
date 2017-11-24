package de.moonflower.jfritz;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import de.moonflower.jfritz.constants.ProgramConstants;
import de.moonflower.jfritz.utils.JFritzUtils;

public class SplashScreen extends JFrame {
	/**
	 *
	 */
	private static final long serialVersionUID = -1231912446567250102L;
	private static final Logger log = Logger.getLogger(SplashScreen.class);
	private JLabel statusBar;
	private JLabel versionPanel;

	public SplashScreen(boolean show)
	{
		super();
		this.setUndecorated(true);

		ImageIcon splashImage = new ImageIcon(getClass().getClassLoader().getResource("splash/splash.png"));
		ImageIcon statusImage = new ImageIcon(getClass().getClassLoader().getResource("splash/status.png"));
		JLabel imageLabel = new JLabel(splashImage);
		getContentPane().add(imageLabel, BorderLayout.CENTER);

		BackgroundPanel statusPanel = new BackgroundPanel(statusImage);
		statusBar = new JLabel("");
		versionPanel = new JLabel("");
		versionPanel.setForeground(Color.white);
		Dimension statusSize = new Dimension(100, 20);
		statusBar.setForeground(Color.white);
		statusBar.setPreferredSize(statusSize);
		statusBar.setMaximumSize(statusSize);
		statusBar.setMaximumSize(statusSize);
		statusPanel.add(statusBar, BorderLayout.CENTER);
		statusPanel.add(versionPanel, BorderLayout.EAST);
		statusPanel.setOpaque(true);
		getContentPane().add(statusPanel, BorderLayout.SOUTH);

		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();

		this.setTitle(ProgramConstants.PROGRAM_NAME + " v"+ProgramConstants.PROGRAM_VERSION  + " Rev: " +
				ProgramConstants.REVISION);
		URL trayIcon = getClass().getClassLoader().getResource("images/trayicon.png");
		setIconImage(Toolkit.getDefaultToolkit().getImage(trayIcon)); //$NON-NLS-1$
		this.pack();
		this.setLocation((screenDim.width / 2) - (this.getWidth() / 2),
				(screenDim.height / 2) - (this.getHeight() / 2));

		if (show)
		{
			this.setVisible(true);
		}
	}

	public void setVersion(String version)
	{
		versionPanel.setText(" " + version + "   ");
	}

	public void setStatus(String status)
	{
		log.info(status);
		statusBar.setText("   " +status);
	}

	public class BackgroundPanel extends JPanel {
		private static final long serialVersionUID = 5679129084605163733L;
		private Image img ;
		public BackgroundPanel(ImageIcon background) {
			setLayout( new BorderLayout() ) ;
			img = background.getImage() ;
			if( img == null ) {
				log.error( "Image is null" );
			}
			if( img.getHeight(this) <= 0 || img.getWidth( this ) <= 0 ) {
				log.error( "Image width or height must be positive" );
				img = null;
			}
		}
		public void drawBackground( Graphics g ) {
			if (img != null)
			{
				int w = getWidth() ;
				int h = getHeight() ;
				int iw = img.getWidth( this ) ;
				int ih = img.getHeight( this ) ;
				for( int i = 0 ; i < w ; i+=iw ) {
					for( int j = 0 ; j < h ; j+= ih ) {
						g.drawImage( img , i , j , this ) ;
					}
				}
			}
		}
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			drawBackground( g ) ;
		}
	}
}
