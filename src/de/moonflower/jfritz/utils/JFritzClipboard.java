package de.moonflower.jfritz.utils;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

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
        	if ((text!=null) && (!text.equals("")))
        	{
            	StringSelection stringSelection = new StringSelection(text);
        		systemClip.setContents(stringSelection, stringSelection);
        		Debug.msg("JFritzClipboard.copy: "+text);
        	}
        }catch(IllegalStateException ise)
        {
        	Debug.err("Cannot copy "+text+" into clipboard (clipboard not available)");
        	Debug.errDlg("Die Zwischenablage ist nicht verfügbar!");
        }
    }

    private static boolean runTest()
    {
    	try{
    		JFritzClipboard.copy("Text to be copied into systems clipboard.");
    		JFritzClipboard.copy(null);
    		JFritzClipboard.copy("");
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
    	System.out.print("\tTesting JFritzClipboard:\t");
    	System.out.println(JFritzClipboard.runTest()?"OK":"Error");
    }


}
