package de.moonflower.jfritz.callerlist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.PhoneNumberOld;

public class NumberCallMultiHashMap {

	private Map<String, List<Call>> hashMap;

	public NumberCallMultiHashMap()
	{
		hashMap = new HashMap<String, List<Call>>();
	}

	public void addCall(PhoneNumberOld number, Call call)
	{
		if (number != null)
		{
			List<Call> l = hashMap.get(number.getIntNumber());
			if (l == null)
			{
				hashMap.put(number.getIntNumber(), l=new ArrayList<Call>());
			}
			if (!l.contains(call))
			{
				l.add(call);
			}
		}
	}

	public List<Call> getCall(PhoneNumberOld number)
	{
		if (number != null)
		{
			return hashMap.get(number.getIntNumber());
		}
		else
		{
			return null;
		}
	}

	public void deleteCall(PhoneNumberOld number, Call call)
	{
		if (number != null)
		{
			List<Call> l = hashMap.get(number.getIntNumber());
			l.remove(call);
			hashMap.remove(number.getIntNumber());
			hashMap.put(number.getIntNumber(), l);
		}
	}

	public List<Call> getAllCalls()
	{
		List<Call> result = new ArrayList<Call>();
		Iterator<List<Call>> it = hashMap.values().iterator();
		while (it.hasNext())
		{
			List<Call> list = it.next();
			for (Call c:list)
			{
				if (!result.contains(c))
				{
					result.add(c);
				}
			}
		}
		return result;
	}
}
