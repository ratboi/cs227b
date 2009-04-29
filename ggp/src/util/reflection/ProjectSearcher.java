package util.reflection;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

import player.gamer.Gamer;

public class ProjectSearcher {
	
	
	public static void main(String[] args)
	{
		System.out.println(getAllClassesThatAre(Gamer.class));
	}
	
	public static List<Class<?>> getAllClassesThatAre(Class<?> ofThisType) 
	{
		return getAllClassesThatAre(ofThisType, true);
	}
	public static List<Class<?>> getAllClassesThatAre(Class<?> ofThisType, boolean mustBeConcrete)
	{
		List<Class<?>> rval = new ArrayList<Class<?>>();
		for(String name : allClasses)
		{
			Class<?> c = null;
			try
			{	
				c = Class.forName(name);
			} catch (ClassNotFoundException ex) 
			{ 
				
				throw new RuntimeException(ex); 
			}
			
			if(ofThisType.isAssignableFrom(c) && (!mustBeConcrete || !Modifier.isAbstract(c.getModifiers())) )
				rval.add(c);	
		}
		return rval;
	}
	
	private static List<String> allClasses = findAllClasses();
	
	private static List<String> findAllClasses()
	{
		FilenameFilter filter = new FilenameFilter() {
	        public boolean accept(File dir, String name) {
	            return !name.startsWith(".");
	        }
	    };
		
		List<String> rval = new ArrayList<String>();
		String CP = System.getProperty(("java.class.path"));
		CP = CP.split(System.getProperty("path.separator"))[0];
		File f = new File(CP);
		Stack<File> toProcess = new Stack<File>();
		toProcess.add(f);
		while(!toProcess.empty())
		{
			f = toProcess.pop();
			if(!f.exists())
				System.out.println(f);
			if(f.isDirectory())
				toProcess.addAll(Arrays.asList(f.listFiles(filter)));
			else
			{
				if(f.getName().endsWith(".class"))
				{					
					String fullyQualifiedName = f.getAbsolutePath().replace(CP, "");
					fullyQualifiedName = fullyQualifiedName.replaceAll("\\.class$","");
					fullyQualifiedName = fullyQualifiedName.replaceAll("^[\\\\/]", "");
					fullyQualifiedName = fullyQualifiedName.replaceAll("[\\\\/]", ".");
					rval.add(fullyQualifiedName);
				}
					
				//System.out.println(f.getName());
			}
		}
		
		return rval;
	}
}
