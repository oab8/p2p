package example.reports;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;

/**
 * @author Lucas Provensi
 * 
 * Report the in-degree distribution of the network.
 * Uses the list of neighbors obtained from a linkable protocol.
 *
 */
public class InDegreeObserver implements Control
{
	private final static String PAR_PID = "protocol";
	private static final String PAR_START_PROTOCOL = "starttime";
	private static final String PAR_END_PROTOCOL = "endtime";

	private final int pid;
	private final long startTime;
	private final long endTime;

	public InDegreeObserver(String prefix)
	{
		this.pid = Configuration.getPid(prefix + "." + PAR_PID);
		this.startTime = Configuration.getLong(prefix + "." + PAR_START_PROTOCOL, Long.MIN_VALUE);
		this.endTime = Configuration.getLong(prefix + "." + PAR_END_PROTOCOL, Long.MAX_VALUE);
	}

	public boolean execute()
	{
		if ((CommonState.getTime() >= endTime) || (CommonState.getTime() < startTime))
			return false;

		// Map of all nodes and their in-degree count
		Map<Long, Integer> degreeCount = new HashMap<Long, Integer>();

		for (int i = 0; i < Network.size(); i++){
			// Get all the nodes in the network
			Node n = Network.get(i);

			if (n.isUp()){
				// Get the linkable protocol for all the running nodes
				Linkable linkable = (Linkable)n.getProtocol(pid);
				// Go through the neighbor list and update the degrees in the map 
				for (int j = 0; j < linkable.degree(); j++){
					Long nodeId = linkable.getNeighbor(j).getID();
					Integer count = degreeCount.get(nodeId);
					if(count == null)
						degreeCount.put(nodeId, 1);
					else
						degreeCount.put(nodeId, count + 1);
				}
			}
		}

		// Map of the in-degree distribution. The key is the in-degree and the
		// entry is the number of nodes having this distribution
		Map<Integer, Integer> dist = new HashMap<Integer, Integer>();

		// Fill the map with the in-degree distribution of each node
		for (int i = 0; i < Network.size(); i++){
			Long nodeId = Network.get(i).getID();
			Integer degree = degreeCount.get(nodeId);
			int value = 1;
			if(dist.containsKey(degree))
				value = dist.get(degree) + 1;
			dist.put(degree, value);
		}
		
		// Sort the distribution and print the result
		// System.out.println(dist);
		dist.remove(null);
		
		SortedSet<Integer> sortedKeys = new TreeSet<Integer>(dist.keySet());
		for(int i = 0; i <= sortedKeys.last(); i++){
			if(sortedKeys.contains(i))
				System.out.println(i + " " + dist.get(i));
			else
				System.out.println(i + " " + 0);
		}

		return false;
	}

}
