package util.xhtml;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.ScrollPaneConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import org.xhtmlrenderer.css.parser.property.PrimitivePropertyBuilders.Cursor;
import org.xhtmlrenderer.event.DefaultDocumentListener;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.resource.XMLResource;
import org.xhtmlrenderer.simple.FSScrollPane;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.simple.extend.XhtmlNamespaceHandler;
import org.xhtmlrenderer.swing.NaiveUserAgent;
import org.xhtmlrenderer.util.GeneralUtil;
import org.xhtmlrenderer.util.XRLog;

import util.files.FileUtils;


/**
 * A mess of code which is responsible for generating a graphical rendering of a game
 * @author Ethan
 *
 */
public class GameStateRenderPanel extends JPanel {
	
	public static JPanel getPanelfromGameXML(String gameXML, String XSL)
	{
		XHTMLPanel panel = new XHTMLPanel();
		panel.setPreferredSize(new Dimension(600,600));
		//setupUserAgentCallback(panel);
		
		String XHTML = getXHTMLfromGameXML(gameXML, XSL);
		setPanelToDisplayGameXHTML(panel, XHTML);
		
		return panel;
	}

	public static String getXHTMLfromGameXML(String gameXML, Integer turnToShow) {
		String XSLstring = findXSLT(gameXML);
		String XSL = getXSLfromFile(XSLstring, turnToShow);
		return getXHTMLfromGameXML(gameXML, XSL);		
	}
	
	public static String getXHTMLfromGameXML(String gameXML, String XSL) {
		IOString game = new IOString();
		game.setString(gameXML);		
		IOString xslIOString = new IOString();
		xslIOString.setString(XSL);
		IOString content = new IOString();
		try {
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer(new StreamSource(xslIOString.getInputStream()));
			
			transformer.transform(	new StreamSource(game.getInputStream()), 
									new StreamResult(content.getOutputStream()));
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		String tcontent = content.getString();
		content.setString(tcontent);
		Tidy tidy = new Tidy();
		tidy.setXHTML(true);
		tidy.setShowWarnings(false);
		tidy.setQuiet(true);
		IOString tidied = new IOString();
		tidy.parse(content.getInputStream(), tidied.getOutputStream());
		tcontent = tidied.getString();
		return tcontent;
	}
	
	public static String getXSLfromFile(String XSLfileName, Integer turnToShow)
	{
		String XSL = FileUtils.readFileAsString(".\\games\\stylesheets\\"+XSLfileName);
		String CustomXSL = getCustomXSL(XSL);
		String template = FileUtils.readFileAsString(".\\src\\util\\xhtml\\template.xsl");
		XSL = template.replace("###GAME_SPECIFIC_STUFF_HERE###", CustomXSL);
		XSL = XSL.replace("###STATE_NUM_HERE###", turnToShow.toString());
		return XSL;
	}
	
	private static String findXSLT(String gameXML)
	{		
		final String toFind = "<?xml-stylesheet type='text/xsl' href='/docserver/gameserver/stylesheets/";
		int start = gameXML.indexOf(toFind)+toFind.length();
		int end = gameXML.indexOf('\'', start);
		return gameXML.substring(start, end);
	}
	
	private static String getCustomXSL(String XSL)
	{
		final String toFind = "<!-- Game specific stuff goes here -->";
		int start = XSL.indexOf(toFind)+toFind.length();
		int end = XSL.indexOf(toFind, start);
		return XSL.substring(start, end);
	}
	
	private void setPanelToDisplayGameXML(XHTMLPanel panel, String gameXML, Integer turnToShow)
	{
		String XHTML = getXHTMLfromGameXML(gameXML, turnToShow);
	    try {
			panel.setDocumentFromString(XHTML, "http://visionary.stanford.edu:4444", new XhtmlNamespaceHandler());			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void setPanelToDisplayGameXHTML(XHTMLPanel panel, String XHTML)
	{
		try {
			panel.setDocumentFromString(XHTML, "http://visionary.stanford.edu:4444", getHandler());			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static XhtmlNamespaceHandler the_handler = null;
	private static XhtmlNamespaceHandler getHandler()
	{
		if(the_handler==null)
			the_handler = new XhtmlNamespaceHandler();
		return the_handler;
	}

//For the moment no custom User Agent Callback is used, but it could be added to by default not hit visionary for graphics
/*
	private UserAgentCallback uac;
    private UserAgentCallback getUAC() {
        return uac;
    }
    private void setupUserAgentCallback(XHTMLPanel panel) {
        uac = new NaiveUserAgent() {
            //TOdO:implement this with nio.
            protected InputStream resolveAndOpenStream(String uri) {
                java.io.InputStream is = null;
                uri = resolveURI(uri);
                try {
                    final URLConnection uc = new URL(uri).openConnection();
                    
                    uc.setConnectTimeout(10 * 1000);
                    uc.setReadTimeout(30 * 1000);
                    
                    uc.connect();
                    is = uc.getInputStream();
                } catch (java.net.MalformedURLException e) {
                    XRLog.exception("bad URL given: " + uri, e);
                } catch (java.io.FileNotFoundException e) {
                    XRLog.exception("item at URI " + uri + " not found");
                } catch (java.io.IOException e) {
                    XRLog.exception("IO problem for " + uri, e);
                }
                return is;
            }
        };
        panel.getSharedContext().setUserAgentCallback(uac);
    }
*/
	
//=======Test App code=========
	private static void createAndShowGUI(GameStateRenderPanel renderPanel)
	{
		JFrame frame = new JFrame("Game State");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.setPreferredSize(new Dimension(1024, 768));
		frame.getContentPane().add(renderPanel);

		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) throws IOException
	{
		final GameStateRenderPanel me = new GameStateRenderPanel();
		javax.swing.SwingUtilities.invokeLater(new Runnable()
		{

			public void run()
			{
				createAndShowGUI(me);
			}
		});
	}
	
	String gameXML = FileUtils.readFileAsString(".\\src\\util\\xhtml\\sampleMatch.xml");
	int curTurn = 1;
	XHTMLPanel mypanel;
	private final JButton forward;
	private final JButton back;
	public GameStateRenderPanel()
	{
		super(new GridBagLayout());
		forward = new JButton(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				setPanelToDisplayGameXML(mypanel, gameXML, ++curTurn);
			}});
		back = new JButton(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				setPanelToDisplayGameXML(mypanel, gameXML, --curTurn);
			}});
		forward.setText("Forward");
		back.setText("Back");
		
		mypanel = new XHTMLPanel();
		//setupUserAgentCallback(panel);
		
		setPanelToDisplayGameXML(mypanel, gameXML, 1);
		
	    //FSScrollPane scroll = new FSScrollPane(mypanel);
	    //scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    //scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
	    
	    this.add(back, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
	    this.add(forward, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));	    
		this.add(mypanel, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
	}
	
//========IOstring code========
	public static class IOString
	{
		private StringBuffer buf;
		/** Creates a new instance of IOString */
		public IOString()
		{
			buf = new StringBuffer();
		}
		public IOString(String text)
		{
			buf = new StringBuffer(text);
		}
		public InputStream getInputStream()
		{
			return new IOString.IOStringInputStream();
		}
		public OutputStream getOutputStream()
		{
			return new IOString.IOStringOutputStream();
		}
		public String getString()
		{
			return buf.toString();
		}
		public void setString(String s)
		{
			buf = new StringBuffer(s);
		}
		class IOStringInputStream extends java.io.InputStream
		{
			private int position = 0;
			public int read() throws java.io.IOException
			{
				if (position<buf.length())
				{
					return buf.charAt(position++);
				}else
				{
					return -1;
				}
			}
		}
		class IOStringOutputStream extends java.io.OutputStream
		{
			public void write(int character) throws java.io.IOException
			{
				buf.append((char)character);
			}

		}
	}

}
