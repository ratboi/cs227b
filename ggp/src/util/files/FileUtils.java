package util.files;

import java.io.BufferedReader;
import java.io.FileReader;

public class FileUtils {
	/** 
	 * @param filePath the name of the file to open.
	*/ 
	public static String readFileAsString(String filePath)
	{
		try
		{
			StringBuilder fileData = new StringBuilder(10000);
			BufferedReader reader = new BufferedReader(
					new FileReader(filePath));
			char[] buf = new char[1024];
			int numRead=0;
			while((numRead=reader.read(buf)) != -1){
				fileData.append(buf, 0, numRead);
			}
			reader.close();
			return fileData.toString();
		}
		catch(Exception ex)
		{
			//ex.printStackTrace();
			return "";
		}
	}

}
