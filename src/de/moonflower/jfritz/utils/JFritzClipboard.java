package de.moonflower.jfritz.utils;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import de.moonflower.jfritz.Main;

public class JFritzClipboard{

	Clipboard systemClip = null;

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


    protected void setContents(String text){
		try{
        	if ((text!=null) && (!text.equals(""))) //$NON-NLS-1$
        	{
            	StringSelection stringSelection = new StringSelection(text);
        		systemClip.setContents(stringSelection, stringSelection);
        		Debug.msg("JFritzClipboard.copy: "+text); //$NON-NLS-1$
        	}
        }catch(IllegalStateException ise)
        {
        	Debug.err("Cannot copy "+text+" into clipboard (clipboard not available)"); //$NON-NLS-1$,  //$NON-NLS-2$
        	Debug.errDlg(Main.getMessage("error_clipboard_not_available")); //$NON-NLS-1$
        }
    }

    private static boolean runTest()
    {
    	try{
    		JFritzClipboard.copy("Text to be copied into systems clipboard."); //$NON-NLS-1$
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
    	System.out.print("\tTesting JFritzClipboard:\t"); //$NON-NLS-1$
    	System.out.println(JFritzClipboard.runTest()?"OK":"Error"); //$NON-NLS-1$,  //$NON-NLS-2$
    }


}
