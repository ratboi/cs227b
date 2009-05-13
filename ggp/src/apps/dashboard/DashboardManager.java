package apps.dashboard;

import java.net.ServerSocket;
import java.util.List;

import player.gamer.Gamer;

/**
 * 
 * DashboardManager is the thread that runs queued up events.
 * It tries to start the players on port 9147, and will increment
 * up 
 * 
 * @author Sam Schreiber
 * @version 0.100 (Beta)
 *
 */
public final class DashboardManager extends Thread
{
    private List<DashboardEvent> theEvents;
    private List<Class<?>> thePlayers;
    private DashboardEventsPanel thePanel;
    private static final int GAME_PORT = 9147;

    public DashboardManager(List<DashboardEvent> theEvents, List<Class<?>> thePlayers, DashboardEventsPanel thePanel)
    {
        this.theEvents = theEvents;
        this.thePlayers = thePlayers;
        this.thePanel = thePanel;
    }

    @Override
    public void run()
    {
        try {
            for (DashboardEvent theEvent : theEvents) {
                Gamer gamer = null;				

                // Find the block of open ports closest to 9147.
                int port = GAME_PORT;
                while(true) {
                    try {
                        for(int i = 0; i < thePlayers.size(); i++) {
                            ServerSocket ss = new ServerSocket(port + i);
                            ss.close();
                        }
                        break;
                    } catch(Exception e) {
                        port++;
                    }
                }
                
                // Create the gamer instance from the class object.
                try {
                    gamer = (Gamer) thePlayers.get(0).newInstance();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
                
                // Configure parameters and run the match!
                theEvent.playerName = gamer.getName();
                thePanel.setCurrentEvent(theEvent);
                theEvent.addObserver(thePanel);
                int myScore = theEvent.runEvent(port, thePlayers);

                // Record the score, and get ready to repeat.
                thePanel.recordScore(myScore);
                Thread.sleep(500);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}