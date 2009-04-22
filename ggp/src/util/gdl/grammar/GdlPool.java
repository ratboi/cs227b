package util.gdl.grammar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GdlPool
{

	private static final Map<String, GdlConstant> constantPool = new HashMap<String, GdlConstant>();
	private static final Map<GdlTerm, Map<GdlTerm, GdlDistinct>> distinctPool = new HashMap<GdlTerm, Map<GdlTerm, GdlDistinct>>();
	private static final Map<GdlConstant, Map<List<GdlTerm>, GdlFunction>> functionPool = new HashMap<GdlConstant, Map<List<GdlTerm>, GdlFunction>>();
	private static final Map<GdlLiteral, GdlNot> notPool = new HashMap<GdlLiteral, GdlNot>();
	private static final Map<List<GdlLiteral>, GdlOr> orPool = new HashMap<List<GdlLiteral>, GdlOr>();
	private static final Map<GdlConstant, GdlProposition> propositionPool = new HashMap<GdlConstant, GdlProposition>();
	private static final Map<GdlConstant, Map<List<GdlTerm>, GdlRelation>> relationPool = new HashMap<GdlConstant, Map<List<GdlTerm>, GdlRelation>>();
	private static final Map<GdlSentence, Map<List<GdlLiteral>, GdlRule>> rulePool = new HashMap<GdlSentence, Map<List<GdlLiteral>, GdlRule>>();
	private static final Map<String, GdlVariable> variablePool = new HashMap<String, GdlVariable>();

	public static GdlConstant getConstant(String value)
	{
		if (!constantPool.containsKey(value))
		{
			constantPool.put(value, new GdlConstant(value));
		}
		return constantPool.get(value);
	}

	public static GdlDistinct getDistinct(GdlTerm arg1, GdlTerm arg2)
	{
		if (!distinctPool.containsKey(arg1))
		{
			distinctPool.put(arg1, new HashMap<GdlTerm, GdlDistinct>());
		}
		Map<GdlTerm, GdlDistinct> bucket = distinctPool.get(arg1);
		if (!bucket.containsKey(arg2))
		{
			bucket.put(arg2, new GdlDistinct(arg1, arg2));
		}
		return bucket.get(arg2);
	}

	public static GdlFunction getFunction(GdlConstant name)
	{
		return getFunction(name, new ArrayList<GdlTerm>());
	}

	public static GdlFunction getFunction(GdlConstant name, GdlTerm[] body)
	{
		List<GdlTerm> list = new ArrayList<GdlTerm>(body.length);
		for (GdlTerm term : body)
		{
			list.add(term);
		}

		return getFunction(name, list);
	}

	public static GdlFunction getFunction(GdlConstant name, List<GdlTerm> body)
	{
		if (!functionPool.containsKey(name))
		{
			functionPool.put(name, new HashMap<List<GdlTerm>, GdlFunction>());
		}
		Map<List<GdlTerm>, GdlFunction> bucket = functionPool.get(name);
		if (!bucket.containsKey(body))
		{
			bucket.put(body, new GdlFunction(name, body));
		}
		return bucket.get(body);
	}

	public static GdlNot getNot(GdlLiteral body)
	{
		if (!notPool.containsKey(body))
		{
			notPool.put(body, new GdlNot(body));
		}
		return notPool.get(body);
	}

	public static GdlOr getOr(GdlLiteral[] disjuncts)
	{
		List<GdlLiteral> list = new ArrayList<GdlLiteral>(disjuncts.length);
		for (GdlLiteral literal : disjuncts)
		{
			list.add(literal);
		}

		return getOr(list);
	}

	public static GdlOr getOr(List<GdlLiteral> disjuncts)
	{
		if (!orPool.containsKey(disjuncts))
		{
			orPool.put(disjuncts, new GdlOr(disjuncts));
		}
		return orPool.get(disjuncts);
	}

	public static GdlProposition getProposition(GdlConstant name)
	{
		if (!propositionPool.containsKey(name))
		{
			propositionPool.put(name, new GdlProposition(name));
		}
		return propositionPool.get(name);
	}

	public static GdlRelation getRelation(GdlConstant name)
	{
		return getRelation(name, new ArrayList<GdlTerm>());
	}

	public static GdlRelation getRelation(GdlConstant name, GdlTerm[] body)
	{
		List<GdlTerm> list = new ArrayList<GdlTerm>(body.length);
		for (GdlTerm term : body)
		{
			list.add(term);
		}

		return getRelation(name, list);
	}

	public static GdlRelation getRelation(GdlConstant name, List<GdlTerm> body)
	{
		if (!relationPool.containsKey(name))
		{
			relationPool.put(name, new HashMap<List<GdlTerm>, GdlRelation>());
		}
		Map<List<GdlTerm>, GdlRelation> bucket = relationPool.get(name);
		if (!bucket.containsKey(body))
		{
			bucket.put(body, new GdlRelation(name, body));
		}
		return bucket.get(body);
	}

	public static GdlRule getRule(GdlSentence head)
	{
		return getRule(head, new ArrayList<GdlLiteral>());
	}

	public static GdlRule getRule(GdlSentence head, GdlLiteral[] body)
	{
		List<GdlLiteral> list = new ArrayList<GdlLiteral>(body.length);
		for (GdlLiteral literal : body)
		{
			list.add(literal);
		}

		return getRule(head, list);
	}

	public static GdlRule getRule(GdlSentence head, List<GdlLiteral> body)
	{
		if (!rulePool.containsKey(head))
		{
			rulePool.put(head, new HashMap<List<GdlLiteral>, GdlRule>());
		}
		Map<List<GdlLiteral>, GdlRule> bucket = rulePool.get(head);
		if (!bucket.containsKey(body))
		{
			bucket.put(body, new GdlRule(head, body));
		}
		return bucket.get(body);
	}

	public static GdlVariable getVariable(String name)
	{
		if (!variablePool.containsKey(name))
		{
			variablePool.put(name, new GdlVariable(name));
		}
		return variablePool.get(name);
	}

}
