package player;

import java.util.ArrayList;
import java.util.List;

import player.event.PlayerReceivedMessageEvent;
import player.event.PlayerSentMessageEvent;
import player.gamer.Gamer;
import util.observer.Event;
import util.observer.Observer;
import util.reflection.ProjectSearcher;

/*** 
 * @author Ethan Dreyfuss
 * A class used for automation.  Allows you to start a player by specifying a gamer and port
 */

public class CommandLinePlayer implements Observer {

	private GamePlayer player = null;
	
	private static List<Class<?>> gamers;
	private static List<String> gamerNames;
	
	public CommandLinePlayer()
	{
		gamers = ProjectSearcher.getAllClassesThatAre(Gamer.class);
		gamerNames = new ArrayList<String>();
		if(gamerNames.size()!=gamers.size())
		{
			for(Class<?> c : gamers)
				gamerNames.add(c.getName().replaceAll("^.*\\.",""));
		}
	}
	
	/**
	 * @param args
	 * Command line arguments:
	 *  CommandLinePlayer gamer port
	 */
	public static void main(String[] args) {
		if(!(args.length==2 ))
		{
			System.err.println("Usage is: \n\tCommandLinePlayer gamer port");
			return;
		}
		int port = 9147;
		Gamer gamer = null;
		try
		{
			port = Integer.valueOf(args[1]);
		} catch(Exception e) {
			System.err.println(args[1]+" is not a valid port.");
			return;
		}
		
		CommandLinePlayer clp = new CommandLinePlayer();
		
		int idx = gamerNames.indexOf(args[0]);
		if(idx == -1)
		{
			System.err.println(args[0]+" is not a subclass of gamer.  Valid options are:");
			for(String s : gamerNames)
				System.err.println("\t"+s);
			return;
		}
		try
		{
			gamer = (Gamer)(gamers.get(idx).newInstance());
		} catch(Exception ex) {
			System.err.println("Cannot create instance of "+args[0]);
			return;
		}		
		
		clp.start(port, gamer);			
	}

	private void start(int port, Gamer gamer) {
		try
		{
			player = new GamePlayer(port, gamer);
		} catch(Exception ex) {
			System.err.println("Error starting player.");
			ex.printStackTrace();
			return;
		}
		
		player.addObserver(this);
		player.start();	
	}

	public void observe(Event event) {
		if (event instanceof PlayerReceivedMessageEvent)
		{
			observe((PlayerReceivedMessageEvent) event);
		}
		else if (event instanceof PlayerSentMessageEvent)
		{
			//observe((PlayerSentMessageEvent) event);
		}
	}
	
	private void observe(PlayerReceivedMessageEvent event)
	{
		if(event.getMessage().contains("STOP"))
		{
			System.exit(0);
		}
	}
}
