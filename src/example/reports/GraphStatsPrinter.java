	package example.reports;

	import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import peersim.config.Configuration;
	import peersim.graph.GraphAlgorithms;
	import peersim.reports.GraphObserver;
	import peersim.util.IncrementalStats;

	
	public class GraphStatsPrinter extends GraphObserver {

	private static final String PAR_NL = "nl";

	private static final String PAR_NC = "nc";
	
	private final int nc;

	private final int nl;
		
	private static final String OUTPUT_LOCATION = "src/output.txt";
	//private PrintWriter writer;
	
	//private static final String PAR_CACHESIZE = "cacheSize";
	//private static int cacheSize;
	private static int i = 0;
	
	BufferedWriter writer;
	File file;

	public GraphStatsPrinter(String name) {

		super(name);
		nl = Configuration.getInt(name+"."+PAR_NL,0);
		nc = Configuration.getInt(name+"."+PAR_NC,0);
		
		file = new File(OUTPUT_LOCATION);
	}

	public boolean execute() {
		
		//System.out.print(name+": ");
		
		IncrementalStats stats = new IncrementalStats();
		updateGraph();
		
		try {
			writer = new BufferedWriter(new FileWriter(file, true));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		if( nc != 0 )
		{
			stats.reset();
			final int n = ( nc<0 ? g.size() : nc );
			for(int i=0; i<n && i<g.size(); ++i)
			{
				stats.add(GraphAlgorithms.clustering(g,i));
			}
			double avg=stats.getAverage();
			//System.out.println(avg+" ");
			
			try {
				writer.append(i + "\t" + avg+ "\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			i++;
		}
		
		if( nl != 0 )
		{
			stats.reset();
			final int n = ( nl<0 ? g.size() : nl );
			outerloop:
			for(int i=0; i<n && i<g.size(); ++i)
			{
				ga.dist(g,i);
				for(int j=0; j<g.size(); ++j)
				{
					if( j==i ) continue;
					if (ga.d[j] == -1)
					{
						stats.add(Double.POSITIVE_INFINITY);
						break outerloop;
					}
					else
						stats.add(ga.d[j]); 
				}
			}
			double avg=stats.getAverage();
			//System.out.println(avg+" ");
			
			try {
				writer.append(i + "\t" + avg+ "\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			i++;
		}
		
		//System.out.println();
		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

}
