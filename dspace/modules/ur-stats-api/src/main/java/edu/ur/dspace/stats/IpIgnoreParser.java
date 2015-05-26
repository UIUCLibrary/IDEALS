package edu.ur.dspace.stats;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.dspace.storage.rdbms.DatabaseManager;


/** 
 * 
 * Parses IP addresses to ignore from a Text File
 * 
 * Text File should have *one* IP address per line.
 * It may have commented lines beginning with "#".
 * A good source for these IP text files is:
 * http://www.iplists.com/
 * 
 * E.g.:
 * 
 * # Google
 * 209.185.108
 * 209.185.253
 * 209.85.238
 * 209.85.238.11
 *
 *
 * @author Tim Donohue
 * 
 **/
public class IpIgnoreParser 
{	
	public static void main (String[] args) throws IOException
	{
		IpIgnoreDAO ipIgnoreDAO = new IpIgnoreDAO();
		
		//usage information
	    String usageCmd =  "ip-ignore-parser [text-file] [bot-name] [reason]\n\n";
	    String usageDescription = "-----------------------------------------------------\n" +
	                              "[e.g.] ip-ignore-parser google.txt 'GoogleBot' 'In GoogleBot list from www.iplists.com'\n" +
	                              "(The above command loads IP Ignore info from a text file called 'google.txt')\n" +
	                              "-----------------------------------------------------\n";
	    
	    //initialize command line parser
	    CommandLineParser parser = new PosixParser();
	    
	    //create an options object and populate it
	    Options options = new Options();
	    
	    options.addOption("h", "help", false, "Display help and usage information");
	    
	    
	    
	    try 
	    {
	        //parse command line arguments
	        CommandLine cmdline = parser.parse(options, args);

	        //Check if user just requested help information
	        if (cmdline.hasOption('h'))
	        {
	            //print help and usage information
	            HelpFormatter myhelp = new HelpFormatter();
	            myhelp.defaultSyntaxPrefix="USAGE: ";
	            myhelp.defaultLeftPad = 3;
	            myhelp.printHelp(usageCmd, usageDescription , options, "");
	            
	            System.exit(0);
	        }
	        
	        // Check for required arguments
	        if(cmdline.getArgs()==null || cmdline.getArgs().length < 3)
	        {   
	            System.out.println("You must pass in the text file to parse, a bot name and reason!\n");
	            HelpFormatter myhelp = new HelpFormatter();
	            myhelp.defaultSyntaxPrefix="USAGE: ";
	            myhelp.defaultLeftPad = 3;
	            myhelp.printHelp(usageCmd, usageDescription , options, "");
	            
	            System.exit(0);
	        }

	        //Save BotName & Reason
	        String botName = cmdline.getArgs()[1];
	        String reason = cmdline.getArgs()[2];
	        	
			//Load up file and reader
			FileReader inFile = new FileReader (cmdline.getArgs()[0]);
			BufferedReader in = new BufferedReader (inFile);
			
			String line = "";
	
			//read file line-by-line
			if(in.ready())
			{
				//read first line
				line = in.readLine();
				
				//keep reading, until no more lines
				while (line != null)
				{
					//Cleanup the line
					line = cleanLine(line);
				
					//skip any lines which have no text
					// or begin with the '#' character
					if(!line.startsWith("#") && line.length()>0)
					{
						
			
						//Pattern to match a Full IP address
						//(e.g. 128.174.12.12)
						Pattern ipFullPattern = Pattern.compile("^(\\d){1,3}\\.(\\d){1,3}\\.(\\d){1,3}\\.(\\d){1,3}$");
						Matcher ipFullMatcher = ipFullPattern.matcher(line);
						
						//Pattern to match an IP mask
						//(e.g. 128.174.12)
						Pattern ipMaskPattern = Pattern.compile("^(\\d){1,3}\\.(\\d){1,3}\\.(\\d){1,3}$");
						Matcher ipMaskMatcher = ipMaskPattern.matcher(line);
						
						//If we have a full IP address
						if(ipFullMatcher.matches())
						{
							//Check if this IP address already covered
							if(!ipIgnoreDAO.isIgnored(line))
							{
								System.out.println("Adding new IP Address to Ignore List: " + line + "\n");
								
								//Ignore this new IP address!
								ipIgnoreDAO.createIp(getMask(line), getIpValue(line), getIpValue(line), botName, reason);
							}
							
						}//Else if we just have an IP Mask
						else if(ipMaskMatcher.matches())
						{
							//Check if this IP Mask is already ignored!
							Collection matchingMasks = ipIgnoreDAO.findByMask(line);
							
							//If we have a matching IP mask Mask
							if(matchingMasks!=null && !matchingMasks.isEmpty())
							{
								Iterator i = matchingMasks.iterator();
								
								//get first match, and update it!
								IpIgnore firstMatch = (IpIgnore) i.next();
								
								System.out.println("Updating existing IP Mask: " + line + "\n");		
						
								//update info in firstMatch to cover full Mask (0-255)
								firstMatch.setIpAddressStart(0);
								firstMatch.setIpAddressEnd(255);
								firstMatch.setName(botName);
								firstMatch.setReason(reason);
								ipIgnoreDAO.update(firstMatch);
								
								//If there are other matches, remove them, as we now have an entry for this full mask
								while(i.hasNext())
								{
									IpIgnore nextMatch = (IpIgnore) i.next();
									ipIgnoreDAO.delete(nextMatch.getId());
								}
								
							}
							else //Otherwise, ignore this new IP Mask
							{
								System.out.println("Adding new IP Mask to Ignore List: " + line + "\n");
								
								//Ignore this new IP Mask (for range 0-255)!
								ipIgnoreDAO.createIp(line, 0, 255, botName, reason);
							}
						}
						else //Anything else found, we just skip over
						{
							System.out.println("Could not parse line: " + line + "\n");
						}
						
					   
					}
					//get next line
					line = in.readLine();
				}//end while more lines
			}//end if file ready
	    }//end try
	    catch (Throwable t) 
	    {	
	        t.printStackTrace();
	    	
	        System.exit(1);
	    }
	   
	}//end main

	//Clean up a given line, by removing characters we don't want!
	public static String cleanLine(String line)
	{
		line = line.trim();   //trim spaces
		
		//remove all line terminator characters
		line.replaceAll("\n", ""); //new line char
		line.replaceAll("\r", ""); //carriage return
		line.replaceAll("\u0085", ""); //next-line char
		line.replaceAll("\u2028", ""); //line-separator char
		line.replaceAll("\u2029", ""); //paragraph-separator char
		
		return line;
	}
	
	//return the first three octets of the ip address
	private static String getMask(String ipAddress)
	{
		//Separate address into the first three octet and the last octet
		return ipAddress.substring(0,ipAddress.lastIndexOf("."));
       
	}
   
	//return the last octet of the address
	private static int getIpValue(String ipAddress)
	{
		return Integer.parseInt(ipAddress.substring(ipAddress.lastIndexOf(".") + 1));
	}

}
