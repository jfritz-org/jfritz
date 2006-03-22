package de.moonflower.jfritz.utils;

public class Pair {
	Object key;
	Object value;

	Pair()
	{
		key="";
		value="";
	}

	Pair(Object key, Object value)
	{
		this.key=key;
		this.value=value;
	}

	public Object getValue()
	{
		return value;
	}

	public void setValue(Object value)
	{
		this.value=value;
	}


	public Object getKey()
	{
		return key;
	}

	public void setKey(Object key)
	{
		this.key=key;
	}

	protected static boolean runTest()
	{
		System.out.print("\tTesting Pair");

		String key="testkey";
		String value="testvalue";

		Pair pair=new Pair();
		pair.setKey(key);
		pair.setValue(value);

		System.out.print("\n\t\tget/set-methods using String\t");
		if (pair.getKey().equals(key) && pair.getValue().equals(value))
		{
			System.out.println("OK");
			return true;
		}
		else
			System.out.println("ERROR");

		return false;

	}

	public static void main(String[] args)
	{
		Pair.runTest();
	}
}


