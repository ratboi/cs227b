package apps.dashboard;

import java.util.ArrayList;
import java.util.List;

import player.GamePlayer;
import player.gamer.Gamer;
import player.gamer.event.GamerCompletedMatchEvent;
import player.gamer.event.GamerNewMatchEvent;
import server.GameServer;
import server.event.ServerCompletedMatchEvent;
import server.event.ServerConnectionErrorEvent;
import server.event.ServerIllegalMoveEvent;
import server.event.ServerNewGameStateEvent;
import server.event.ServerTimeoutEvent;
import util.gdl.grammar.Gdl;
import util.kif.KifReader;
import util.match.Match;
import util.observer.Event;
import util.observer.Observer;
import util.observer.Subject;
import util.statemachine.Role;
import util.statemachine.StateMachine;
import util.statemachine.prover.ProverStateMachine;

/**
 * 
 * DashboardEvent encapsulates a single type of match to run, consisting of
 * a game description, a start clock, a play clock, and the distinguished role
 * of the player under observation. This class also includes methods to run a
 * match of this type and report statistics about it.
 * 
 * @author Sam Schreiber
 * @version 0.100 (Beta)
 *
 */
public class DashboardEvent implements Observer, Subject {
    // Finalized information about the match type in general
    public final String eventName;
    public final String visualName;    
    public final int playClock, startClock;
    public final int myRole;
    public final String gameFilename;
    public List<Gdl> gameDescription;
    
    // Specific information about the latest match
    public int moveCount = 0;
    public int errorCount_Timeouts = 0;
    public int errorCount_IllegalMoves = 0;
    public int errorCount_ConnectionErrors = 0;
    private List<Integer> latestGoals = null;    
    public String theStatus = "Awaiting match...";
    public String playerName;

    public DashboardEvent(String eventName, String gameFilename, int myRole, int startClock, int playClock) {
        this.eventName = eventName;
        this.gameFilename = gameFilename;
        this.visualName = gameFilename;
        this.playClock = playClock;
        this.startClock = startClock;
        this.myRole = myRole;
        resetStats();
        
        try {
            this.gameDescription = KifReader.read("./games/rulesheets/" + gameFilename + ".kif");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetStats() {
        observers.clear();
        this.playerName = "Unknown";
        this.theStatus = "Awaiting match...";
        errorCount_Timeouts = 0;
        errorCount_IllegalMoves = 0;
        errorCount_ConnectionErrors = 0;
        moveCount = 0;
        latestGoals = null;	
    }

    public int runEvent(int basePort, List<Class<?>> thePlayers) {
        try {
            notifyObservers(new GamerNewMatchEvent(null, null));

            StateMachine stateMachine = new ProverStateMachine();
            stateMachine.intialize(gameDescription);
            List<Role> roles = stateMachine.getRoles();

            Match theMatch = new Match("MatchID", startClock, playClock, gameDescription);
            
            int playerIndex = 0;
            List<String> hosts = new ArrayList<String>(roles.size());
            List<String> names = new ArrayList<String>(roles.size());
            List<Integer> ports = new ArrayList<Integer>(roles.size());
            for (int i = 0; i < roles.size(); i++) {
                int oppPort = basePort + playerIndex;
                hosts.add("localhost");
                ports.add(new Integer(oppPort));

                Gamer gamer = null;
                Class<?> gamerClass = thePlayers.get(playerIndex++);
                try {
                    gamer = (Gamer) gamerClass.newInstance();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }

                GamePlayer player = new GamePlayer(oppPort, gamer);
                names.add(player.getName());
                player.start();
                System.out.println(gamer.getName() + " on port " + oppPort);
            }

            GameServer gameServer = new GameServer(theMatch, hosts, ports, names);
            gameServer.addObserver(this);
            gameServer.start();

            theStatus = "Metagaming...";
            notifyObservers(new GamerCompletedMatchEvent());

            // Play the game!
            gameServer.join();

            // Assess win/loss.
            int myScore = latestGoals.get(myRole);

            // Cleanup!
            resetStats();
            System.gc();

            return myScore;
        } catch (Exception e) {
            e.printStackTrace();

            return -1;
        }
    }

    public String getGoalString() {
        if(latestGoals == null)
            return "";

        String myScore = "(" + latestGoals.get(myRole) + ")";

        String opposingScores = "(";
        for(int i = 0; i < latestGoals.size(); i++) {
            if(i != myRole) {
                if(opposingScores.length() > 1)
                    opposingScores += ",";
                opposingScores += "" + latestGoals.get(i);  
            }
        }
        opposingScores += ")";

        return myScore + " vs " + opposingScores;
    }

    // Observation handling code (gets info from game)
    public void observe(Event event) {
        if(event instanceof ServerCompletedMatchEvent) {
            ServerCompletedMatchEvent scme = (ServerCompletedMatchEvent) event;
            latestGoals = scme.getGoals();
            theStatus = "Finished.";
            notifyObservers(new GamerCompletedMatchEvent());
        } else if(event instanceof ServerIllegalMoveEvent) {
            errorCount_IllegalMoves++;
            notifyObservers(new GamerCompletedMatchEvent());
        } else if(event instanceof ServerTimeoutEvent) {
            errorCount_Timeouts++;
            notifyObservers(new GamerCompletedMatchEvent());
        } else if(event instanceof ServerConnectionErrorEvent) {
            errorCount_ConnectionErrors++;
            notifyObservers(new GamerCompletedMatchEvent());
        } else if(event instanceof ServerNewGameStateEvent) {	    
            if(theStatus.equals("Metagaming..."))
                theStatus = "Playing...";
            else
                moveCount++;
            notifyObservers(new GamerCompletedMatchEvent());
        }
    }

    // Observer handling code (passes info along to panel)
    private final List<Observer> observers = new ArrayList<Observer>();
    public void addObserver(Observer observer)
    {
        observers.add(observer);
    }

    public void notifyObservers(Event event)
    {
        for (Observer observer : observers)
        {
            observer.observe(event);
        }
    }
}