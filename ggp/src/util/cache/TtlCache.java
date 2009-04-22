package util.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TtlCache<K, V>
{

	private final class Entry
	{

		public int ttl;
		public V value;

		public Entry(V value, int ttl)
		{
			this.value = value;
			this.ttl = ttl;
		}
	}

	private final Map<K, Entry> contents;
	private final int ttl;

	public TtlCache(int ttl)
	{
		this.contents = new HashMap<K, Entry>();
		this.ttl = ttl;
	}

	public synchronized boolean containsKey(K key)
	{
		return contents.containsKey(key);
	}

	public synchronized V get(K key)
	{
		Entry entry = contents.get(key);
		entry.ttl = ttl;

		return entry.value;
	}

	public synchronized void prune()
	{
		List<K> toPrune = new ArrayList<K>();
		for (K key : contents.keySet())
		{
			Entry entry = contents.get(key);
			if (entry.ttl == 0)
			{
				toPrune.add(key);
			}
			entry.ttl--;
		}

		for (K key : toPrune)
		{
			contents.remove(key);
		}
	}

	public synchronized void put(K key, V value)
	{
		contents.put(key, new Entry(value, ttl));
	}

	public synchronized int size()
	{
		return contents.size();
	}

}
