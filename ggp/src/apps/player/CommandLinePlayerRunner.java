package apps.player;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

/***
 *
 * @author Ethan Dreyfuss
 *
 * Little app which uses the CommandLinePlayer to continuously start and
 * restart a gamer for competition purposes.
 */
public class CommandLinePlayerRunner {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		while(true)
		{
			ProcessBuilder pb = new ProcessBuilder("java.exe",
					"-Xmx1024M",
					"-server",
					"-classpath",
					System.getProperty("java.class.path"),
					"player.CommandLinePlayer",
                    "CloseGamer",  //CHANGE ME
                    "9150");        //OPTIONAL: CHANGE ME 
			
			pb.redirectErrorStream( true );
			
			try
			{
				Process p = pb.start();
				
				BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line = "";   //Must keep reading out from the buffers or the process will block
									//Error stream is merged with regular stream since it also must be read
									//and otherwise multiple threads would be required
				while ((line = input.readLine()) != null) {
					System.out.println(line);
				}
				p.waitFor();
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

}
