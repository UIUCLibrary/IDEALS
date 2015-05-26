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
 * Command line script to update monthly cached
 * statistics (in the monthly_bitstream_stats table).
 *
 * @author Tim Donohue
 * 
 **/
public class UpdateMonthlyStats {

	public static void main(String [] args)
    throws Exception
{
	//usage information
    String usageCmd =  "update-monthly-download-totals [start-month] [end-month]\n\n";
    String usageDescription = "-----------------------------------------------------\n" +
                              "[e.g. #1] update-monthly-download-totals 2008-09\n" +
                              "(The above command updates stats for Sept 2008 ONLY)\n" +
                              "[e.g. #2] update-monthly-download-totals 2007-09 2008-02\n" +
                              "(The above command updates stats for Sept 2007 through Feb 2008)\n" +
                              "[e.g. #2] update-monthly-download-totals -c\n" +
                              "(The above command updates stats for current month ONLY)\n" +
                              "-----------------------------------------------------\n";
    
    //initialize command line parser
    CommandLineParser parser = new PosixParser();
    
    //create an options object and populate it
    Options options = new Options();
    
    options.addOption("h", "help", false, "Display help and usage information");
    options.addOption("c", "current", false, "Update current month only");
    options.addOption("p", "previous", false, "Update previous month only");
    
    
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
        if((line.getArgs()==null || line.getArgs().length < 1) && !line.hasOption('c') && !line.hasOption('p'))
        {   
            System.out.println("At least one month (Format: YYYY-MM) or an option flag is required!\n");
            HelpFormatter myhelp = new HelpFormatter();
            myhelp.defaultSyntaxPrefix="USAGE: ";
            myhelp.defaultLeftPad = 3;
            myhelp.printHelp(usageCmd, usageDescription , options, "");
            
            System.exit(0);
        }

        boolean updateCurrentMonth=false;
        boolean updatePreviousMonth=false;
        if(line.hasOption('c'))
        	updateCurrentMonth = true;
        else if (line.hasOption('p'))
        	updatePreviousMonth = true;
        	
        if(updateCurrentMonth)
        {
        	System.out.println("\n\nUpdating for current month.\n");
	    	//Initialize statistics
	        StatsDAO stats = new StatsDAO();
	    	
	        //Update all monthly counts for current month
	    	stats.updateAllMonthlyCounts();
        }
        else if(updatePreviousMonth)
        {
        	//Determine previous month
       	 	GregorianCalendar calendar = new GregorianCalendar();
       	 	calendar.add(Calendar.MONTH, -1); //subtract one month
       	 	
       	 	System.out.println("\n\nUpdating for last month (" + calendar.get(Calendar.YEAR)+ "-" + (calendar.get(Calendar.MONTH)+1) + ").\n");
       	 	
       	 	//Initialize statistics & update previous month's stats
	        StatsDAO stats = new StatsDAO();
       	 	stats.updateAllMonthlyCounts(calendar);
        }
        else
        {
	        String startMonthString = line.getArgs()[0];
	        
	        //parse our month string
	        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
	    	Date startDate = format.parse(startMonthString);
	    	
	    	if(startDate==null)
	    		throw new Exception("The Month specified '" + startMonthString + "' doesn't seem to be a valid date!  Please specify in the format: YYYY-MM");
	    	
	    	//If multiple arguments, then we are dealing with a date range
	    	if(line.getArgs().length>1)
	    	{
	    		String endMonthString = line.getArgs()[1];
	    	       
	    		//parse out month string
	        	Date endDate = format.parse(endMonthString);
	        	
	        	if(endDate==null)
	        		throw new Exception("The Month specified '" + endMonthString + "' doesn't seem to be a valid date!  Please specify in the format: YYYY-MM");
	        	
	        	System.out.println("\n\nUpdating for All Months between '" + startMonthString + "' and '" + endMonthString + "'...This may take a while!\n");
		    	//Initialize statistics
		        StatsDAO stats = new StatsDAO();
		    	
		        //Update all monthly counts!
		    	stats.updateAllMonthlyCounts(startDate, endDate);
	    	}
	    	else
	    	{
		    	System.out.println("\n\nUpdating for Month '" + startMonthString + "'...This may take a while, depending on download activity during this month.\n");
		    	//Initialize statistics
		        StatsDAO stats = new StatsDAO();
		    	
		        //Update all monthly counts!
		    	stats.updateAllMonthlyCounts(startDate);
	    	}
        }
        System.out.println("\n\n===Updates completed successfully!===\n");
    }
    catch (Throwable t) 
    {	
    	t.printStackTrace();
    	
        System.exit(1);
    }
}//end main
	
	
}
