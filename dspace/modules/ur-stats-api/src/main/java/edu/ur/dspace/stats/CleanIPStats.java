package edu.ur.dspace.stats;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

/** 
 * 
 * Command line script to clean the ip_bitstream_stats
 * table, for months whose stats have already been
 * saved/archived in monthly_bitstream_stats table.
 * <P>
 * The 'ip_bitstream_stats' table should be cleaned
 * every few months or so.  Otherwise it will likely
 * increase drastically, and may cause serious performance
 * problems.
 *
 * @author Tim Donohue
 * 
 **/
public class CleanIPStats {

	public static void main(String [] args)
    throws Exception
{
	//usage information
    String usageCmd =  "clean-ip-stats [number-months-to-keep]\n\n";
    String usageDescription = "-----------------------------------------------------\n" +
                              "[e.g.] clean-ip-stats 2\n" +
                              "(The above command keeps the last two FULL months IP statistics" +
                              " and deletes all the rest.  For example, if run on Sept 9, 2008," +
                              " this will keep stats from July, August and Sept.)\n" +
                              "-----------------------------------------------------\n";
    
    //initialize command line parser
    CommandLineParser parser = new PosixParser();
    
    //create an options object and populate it
    Options options = new Options();
    
    options.addOption("h", "help", false, "Display help and usage information");
    
    try 
    {
        //parse command line arguments
        CommandLine line = parser.parse(options, args);

        //Check if user just requested help information
        if (line.hasOption('h'))
        {
            //print help and usage information
            HelpFormatter myhelp = new HelpFormatter();
            myhelp.defaultSyntaxPrefix="USAGE: ";
            myhelp.defaultLeftPad = 3;
            myhelp.printHelp(usageCmd, usageDescription , options, "");
            
            System.exit(0);
        }
        
        // Check for required arguments
        if(line.getArgs()==null || line.getArgs().length < 1)
        {   
            System.out.println("Number of months to keep is required!\n");
            HelpFormatter myhelp = new HelpFormatter();
            myhelp.defaultSyntaxPrefix="USAGE: ";
            myhelp.defaultLeftPad = 3;
            myhelp.printHelp(usageCmd, usageDescription , options, "");
            
            System.exit(0);
        }

        String monthsToKeep = line.getArgs()[0];
        int numMonthsToKeep = Integer.parseInt(monthsToKeep);
        
        //Number of months to keep must be greater than or equal to zero!
        if(numMonthsToKeep<0)
        {
        	System.out.println("Number of months to keep MUST be zero or more!\n");
            HelpFormatter myhelp = new HelpFormatter();
            myhelp.defaultSyntaxPrefix="USAGE: ";
            myhelp.defaultLeftPad = 3;
            myhelp.printHelp(usageCmd, usageDescription , options, "");
            
            System.exit(0);
        }
        
       
    	System.out.println("\n\nDeleting IP statistics prior to the last " + monthsToKeep + " month(s)...This may take a while!\n");
    	//Initialize statistics
        StatsDAO stats = new StatsDAO();
    	
        //Clean our IP statistics
    	stats.cleanIPStats(numMonthsToKeep);
	
        System.out.println("\n\n===IP statistics cleaned successfully!===\n");
    }
    catch (Throwable t) 
    {	
    	t.printStackTrace();
    	
        System.exit(1);
    }
}//end main
	
	
}
