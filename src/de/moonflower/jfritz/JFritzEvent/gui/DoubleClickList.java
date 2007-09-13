package de.moonflower.jfritz.JFritzEvent.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.JList;

public class DoubleClickList extends JList {

	private static final long serialVersionUID = 7458930213942452635L;
	private Vector<ParameterClickedEvent> clickListener;
	public DoubleClickList() {
		super();
		clickListener = new Vector<ParameterClickedEvent>();
		MouseListener mouseListener = new JListMouseListener(this);
		this.addMouseListener(mouseListener);
	}

	public DoubleClickList(Vector v) {
		super(v);
		clickListener = new Vector<ParameterClickedEvent>();
		MouseListener mouseListener = new JListMouseListener(this);
		this.addMouseListener(mouseListener);
	}

	public void addDoubleClickListener(ParameterClickedEvent listener) {
		clickListener.add(listener);
	}

	public void removeDoubleClickListener(ParameterClickedEvent listener) {
		if ( clickListener.contains(listener) )
			clickListener.remove(listener);
	}

	public int getDoubleClickListenerSize() {
		return clickListener.size();
	}

	public void doubleClick(int index) {
        for (int i=0; i<clickListener.size(); i++)
        	clickListener.get(i).parameterClicked(this.getModel().getElementAt(index));
	}
}


class JListMouseListener extends MouseAdapter{
	DoubleClickList list;
	public JListMouseListener(DoubleClickList list) {
		this.list = list;
	}
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            int index = list.locationToIndex(e.getPoint());
            list.doubleClick(index);
         }
    }
}