package util.symbol.grammar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SymbolPool
{

	private final static Map<String, SymbolAtom> atomPool = new HashMap<String, SymbolAtom>();
	private final static Map<List<Symbol>, SymbolList> listPool = new HashMap<List<Symbol>, SymbolList>();

	public synchronized static SymbolAtom getAtom(String value)
	{
		if (!atomPool.containsKey(value))
		{
			atomPool.put(value, new SymbolAtom(value));
		}
		return atomPool.get(value);
	}

	public synchronized static SymbolList getList(List<Symbol> contents)
	{
		if (!listPool.containsKey(contents))
		{
			listPool.put(contents, new SymbolList(contents));
		}
		return listPool.get(contents);
	}

	public synchronized static SymbolList getList(Symbol[] contents)
	{
		List<Symbol> list = new ArrayList<Symbol>(contents.length);
		for (Symbol symbol : contents)
		{
			list.add(symbol);
		}

		return getList(list);
	}

}
