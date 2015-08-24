package de.moonflower.jfritz.callerlist;

import javax.swing.table.TableColumn;

import de.moonflower.jfritz.messages.MessageProvider;

public class JFritzTableColumn {

	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1502212011284425475L;

	private boolean visible;

	private String name;

	private TableColumn column;
	protected MessageProvider messages = MessageProvider.getInstance();

	public JFritzTableColumn(String columnName)
	{
		name = columnName;
		visible = true;
	}

	public String getName()
	{
		return name;
	}

	public String getI18NName()
	{
		return messages.getMessage(name);
	}

	public boolean isVisible()
	{
		return visible;
	}

	public void setVisible(boolean vis)
	{
		visible = vis;
	}

	public void setColumn(TableColumn col)
	{
		column = col;
	}

	public TableColumn getColumn()
	{
		return column;
	}
}
