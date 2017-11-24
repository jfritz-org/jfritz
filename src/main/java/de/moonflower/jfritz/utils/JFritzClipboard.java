package de.moonflower.jfritz.utils;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.moonflower.jfritz.messages.MessageProvider;

public class JFritzClipboard{
	private final static Logger log = Logger.getLogger(JFritzClipboard.class);

	protected MessageProvider messages = MessageProvider.getInstance();
	private Clipboard systemClip = null;

	public JFritzClipboard()
	{
		this.systemClip = Toolkit.getDefaultToolkit().getSystemClipboard();
	}

	/**
     * Copies text to clipboard.
     *
     * @author Benjamin Schmitt
     * @param text
     *            the text to be copied to clipboard
     */
    public static void copy(String text){
    	JFritzClipboard jfritzClip = new JFritzClipboard();
		jfritzClip.setContents(text);
    }

    public static Vector<String> paste() {
    	JFritzClipboard jfritzClip = new JFritzClipboard();
    	return jfritzClip.getContents();
    }

    protected Vector<String> getContents() {
    	Vector<String> response = new Vector<String>();

    	Transferable transferData = systemClip.getContents( null );
    	for(DataFlavor dataFlavor : transferData.getTransferDataFlavors())
    	{
			try {
	    		Object content = transferData.getTransferData( dataFlavor );
				if ( content instanceof String )
				{
					response.add((String)content);
				}
			} catch (UnsupportedFlavorException e) {
				log.error("ClipboardException (UnsupportedFlavorException): " + e.getLocalizedMessage());
			} catch (IOException e) {
				log.error("ClipboardException (IOException): " + e.getLocalizedMessage());
			}
    	}

    	return response;
    }

    protected void setContents(String text){
		try{
        	if ((text!=null) && (!text.equals(""))) //$NON-NLS-1$
        	{
            	StringSelection stringSelection = new StringSelection(text);
        		systemClip.setContents(stringSelection, stringSelection);
        	}
        }catch(IllegalStateException ise)
        {
        	log.error("Cannot copy "+text+" into clipboard (clipboard not available)", ise); //$NON-NLS-1$,  //$NON-NLS-2$
        	Debug.errDlg(messages.getMessage("error_clipboard_not_available")); //$NON-NLS-1$
        }
    }

    private static boolean runTest()
    {
    	try{
    		JFritzClipboard.copy("Text to be copied into systems clipboard."); //$NON-NLS-1$
    		Vector<String> response = JFritzClipboard.paste();
    		for (String resp:response) {
    			log.debug(resp);
    		}
    		JFritzClipboard.copy(null);
    		JFritzClipboard.copy(""); //$NON-NLS-1$
    		return true;
    	}catch(Exception e)
    	{
    		return false;
    	}
    }

    public static void main(String[] args)
    {
    	//Run test via console from <project>\bin with
    	//"java de.moonflower.jfritz.utils.JFritzClipboard"
    	System.out.println("Testing JFritzClipboard:\n"); //$NON-NLS-1$
    	System.out.println(JFritzClipboard.runTest()?"OK":"Error"); //$NON-NLS-1$,  //$NON-NLS-2$
    }


}
