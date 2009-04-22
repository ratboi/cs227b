package util.prover.aima.unifier;

import util.gdl.grammar.GdlConstant;
import util.gdl.grammar.GdlFunction;
import util.gdl.grammar.GdlSentence;
import util.gdl.grammar.GdlTerm;
import util.gdl.grammar.GdlVariable;
import util.prover.aima.substitution.Substitution;

public final class Unifier
{

	public static Substitution unify(GdlSentence x, GdlSentence y)
	{
		try
		{
			Substitution theta = new Substitution();
			unifyTerm(x.toTerm(), y.toTerm(), theta);

			return theta;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	private static void unifyTerm(GdlTerm x, GdlTerm y, Substitution theta)
	{
		if ((x instanceof GdlConstant) && (y instanceof GdlConstant))
		{
			if (!x.equals(y))
			{
				throw new RuntimeException();
			}
		}
		else if (x instanceof GdlVariable)
		{
			unifyVariable((GdlVariable) x, y, theta);
		}
		else if (y instanceof GdlVariable)
		{
			unifyVariable((GdlVariable) y, x, theta);
		}
		else if ((x instanceof GdlFunction) && (y instanceof GdlFunction))
		{
			GdlFunction xFunction = (GdlFunction) x;
			GdlFunction yFunction = (GdlFunction) y;

			unifyTerm(xFunction.getName(), yFunction.getName(), theta);
			for (int i = 0; i < xFunction.arity(); i++)
			{
				unifyTerm(xFunction.get(i), yFunction.get(i), theta);
			}
		}
		else
		{
			throw new RuntimeException();
		}
	}

	private static void unifyVariable(GdlVariable var, GdlTerm x, Substitution theta)
	{
		if (theta.contains(var))
		{
			unifyTerm(theta.get(var), x, theta);
		}
		else if ((x instanceof GdlVariable) && theta.contains((GdlVariable) x))
		{
			unifyTerm(var, theta.get((GdlVariable) x), theta);
		}
		else
		{
			theta.put(var, x);
		}
	}

}
