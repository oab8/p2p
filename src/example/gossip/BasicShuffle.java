package example.gossip;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;


/**
 * @author Lucas Provensi
 * 
 * Basic Shuffling protocol template
 * 
 * The basic shuffling algorithm, introduced by Stavrou et al in the paper: 
 * "A Lightweight, Robust P2P System to Handle Flash Crowds", is a simple 
 * peer-to-peer communication model. It forms an overlay and keeps it 
 * connected by means of an epidemic algorithm. The protocol is extremely 
 * simple: each peer knows a small, continuously changing set of other peers, 
 * called its neighbors, and occasionally contacts a random one to exchange 
 * some of their neighbors.
 * 
 * This class is a template with instructions of how to implement the shuffling
 * algorithm in PeerSim.
 * Should make use of the classes Entry and GossipMessage:
 *    Entry - Is an entry in the cache, contains a reference to a neighbor node
 *  		  and a reference to the last node this entry was sent to.
 *    GossipMessage - The message used by the protocol. It can be a shuffle
 *    		  request, reply or reject message. It contains the originating
 *    		  node and the shuffle list.
 *
 */
public class BasicShuffle  implements Linkable, EDProtocol, CDProtocol{
	
	private static final String PAR_CACHE = "cacheSize";
	private static final String PAR_L = "shuffleLength";
	private static final String PAR_TRANSPORT = "transport";

	private final int tid;

	// The list of neighbors known by this node, or the cache.
	private List<Entry> cache;
	
	// The maximum size of the cache;
	private final int size;
	
	// The maximum length of the shuffle exchange;
	private final int l;
	
	private boolean awaitResponse;
	
	private boolean nodeRemoved;
	/**
	 * Constructor that initializes the relevant simulation parameters and
	 * other class variables.
	 * 
	 * @param n simulation parameters
	 */
	public BasicShuffle(String n)
	{
		this.size = Configuration.getInt(n + "." + PAR_CACHE);
		this.l = Configuration.getInt(n + "." + PAR_L);
		this.tid = Configuration.getPid(n + "." + PAR_TRANSPORT);

		cache = new ArrayList<Entry>(size);
		
		awaitResponse = false;
		nodeRemoved = false;
	}

	/* START YOUR IMPLEMENTATION FROM HERE
	 * 
	 * The simulator engine calls the method nextCycle once every cycle 
	 * (specified in time units in the simulation script) for all the nodes.
	 * 
	 * You can assume that a node initiates a shuffling operation every cycle.
	 * 
	 * @see peersim.cdsim.CDProtocol#nextCycle(peersim.core.Node, int)
	 */
	@Override
	public void nextCycle(Node node, int protocolID) {
		// Let's name this node as P
		
		// 1. If P is waiting for a response from a shuffling operation initiated in a previous cycle, return;
		if (awaitResponse) return;
		
		// 2. If P's cache is empty, return;	
		if (cache.isEmpty()) return;
		
		// 3. Select a random neighbor (named Q) from P's cache to initiate the shuffling;
		int randomNumber = CommonState.r.nextInt(cache.size());
		Entry q = cache.get(randomNumber);
		
		// 4. If P's cache is full, remove Q from the cache;
		if (cache.size() == size) {
			cache.remove(q);
		}
		
		// 5. Select a subset of other l - 1 random neighbors from P's cache;
		//	  - l is the length of the shuffle exchange
		//    - Do not add Q to this subset	
		List<Entry> subset = new ArrayList<Entry>();
		
		List<Entry> cacheCopy = new ArrayList<Entry>();
		cacheCopy.addAll(cache);
		cacheCopy.remove(q);
			
		for (int i =1; i < l-1 && !cacheCopy.isEmpty(); i++) {
				randomNumber = CommonState.r.nextInt(cacheCopy.size());
				Entry neighbour = cacheCopy.get(randomNumber);
				cacheCopy.remove(randomNumber);
				
				// updates the neighbour so that it sends to new node, q 
				neighbour.setSentTo(q.getNode());
				
				// adds the neighbour to the q's subset
				subset.add(new Entry(neighbour.getNode()));
		}
		
		// 6. Add P to the subset;
		subset.add(new Entry(node));
		
		// 7. Send a shuffle request to Q containing the subset;
		GossipMessage message = new GossipMessage(node, subset);
		message.setType(MessageType.SHUFFLE_REQUEST);
		Transport tr = (Transport) node.getProtocol(tid);
		tr.send(node, q.getNode(), message, protocolID);
		
		//
		// 8. From this point on P is waiting for Q's response and will not initiate a new shuffle operation;
		awaitResponse = true;
		
		// The response from Q will be handled by the method processEvent.
		
	}
/*	
	private void sendMessage(Node srcNode, Node destNode, List<Entry> subset, MessageType type, int protocolID) {
		GossipMessage message = new GossipMessage(srcNode, subset);
		message.setType(type);
		Transport tr = (Transport) srcNode.getProtocol(tid);
		tr.send(srcNode, destNode, message, protocolID);
	}*/

	/* The simulator engine calls the method processEvent at the specific time unit that an event occurs in the simulation.
	 * It is not called periodically as the nextCycle method.
	 * 
	 * You should implement the handling of the messages received by this node in this method.
	 * 
	 * @see peersim.edsim.EDProtocol#processEvent(peersim.core.Node, int, java.lang.Object)
	 */
	@Override
	public void processEvent(Node node, int pid, Object event) {
	/*	// Let's name this node as Q;
		// Q receives a message from P;
		//	  - Cast the event object to a message:
		GossipMessage message = (GossipMessage) event;
		
		switch (message.getType()) {
		// If the message is a shuffle request:
		case SHUFFLE_REQUEST:
		//	  1. If Q is waiting for a response from a shuffling initiated in a previous cycle, send back to P a message rejecting the shuffle request; 
		//	  2. Q selects a random subset of size l of its own neighbors; 
		//	  3. Q reply P's shuffle request by sending back its own subset;
		//	  4. Q updates its cache to include the neighbors sent by P:
		//		 - No neighbor appears twice in the cache
		//		 - Use empty cache slots to add the new entries
		//		 - If the cache is full, you can replace entries among the ones sent to P with the new ones
			break;
		
		// If the message is a shuffle reply:
		case SHUFFLE_REPLY:
		//	  1. In this case Q initiated a shuffle with P and is receiving a response containing a subset of P's neighbors
		//	  2. Q updates its cache to include the neighbors sent by P:
		//		 - No neighbor appears twice in the cache
		//		 - Use empty cache slots to add new entries
		//		 - If the cache is full, you can replace entries among the ones originally sent to P with the new ones
		//	  3. Q is no longer waiting for a shuffle reply;	 
			break;
		
		// If the message is a shuffle rejection:
		case SHUFFLE_REJECTED:
		//	  1. If P was originally removed from Q's cache, add it again to the cache.
		//	  2. Q is no longer waiting for a shuffle reply;
			break;
			
		default:
			break;
		}
		*/
		// Let's name this node as Q;
				// Q receives a message from P;
				//	  - Cast the event object to a message:
				GossipMessage message = (GossipMessage) event;
				Node p = message.getNode();
				
				List<Entry> cacheCopy;
				
				switch (message.getType()) {
				// If the message is a shuffle request:
				case SHUFFLE_REQUEST:
				//	  1. If Q is waiting for a response from a shuffling initiated in a previous cycle, send back to P a message rejecting the shuffle request;
					
					if(awaitResponse) {
						GossipMessage reply = new GossipMessage(node, null);
						reply.setType(MessageType.SHUFFLE_REJECTED);
						Transport tr = (Transport) node.getProtocol(tid);
						tr.send(node, p, reply, pid);
						return;
					}
					
				//	  2. Q selects a random subset of size l of its own neighbors;
					List<Entry> subset = new ArrayList<Entry>();
					
					cacheCopy = new ArrayList<Entry>(cache);
					cacheCopy.remove(new Entry(p));
					
					for (int i = 0; i < l && !cacheCopy.isEmpty(); ++i) {
						int randomNumber = CommonState.r.nextInt(cacheCopy.size());
						
						Entry neighbour = cacheCopy.remove(randomNumber);
						neighbour.setSentTo(p);
						subset.add(new Entry(neighbour.getNode()));
					}
					
				//	  3. Q reply P's shuffle request by sending back its own subset;
					GossipMessage reply = new GossipMessage(node, subset);
					reply.setType(MessageType.SHUFFLE_REPLY);
					Transport tr = (Transport) node.getProtocol(tid);
					tr.send(node, p, reply, pid);
					
				//	  4. Q updates its cache to include the neighbors sent by P:
				//		 - No neighbor appears twice in the cache
				//		 - Use empty cache slots to add the new entries
				//		 - If the cache is full, you can replace entries among the ones sent to P with the new ones
					Queue<Integer> replacableIndices = new LinkedList<Integer>();
					List<Entry> cacheCopy1 = new ArrayList<Entry>();
				
					ListIterator<Entry> it = cache.listIterator();
					while (it.hasNext()) {
						Entry qNeighbour = it.next();
				
						cacheCopy1.add(qNeighbour);
						
						Node sentTo = qNeighbour.getSentTo();
						if (sentTo != null && p.getID() == sentTo.getID()) {
							replacableIndices.add(it.nextIndex() - 1);
						}
					}
				
					for (Entry neighbour : message.getShuffleList()) {
						boolean alreadyInCache = cacheCopy1.remove(neighbour);
				
						if (!alreadyInCache) {
							if (cache.size() < size) {
								cache.add(neighbour);
							} else if (!replacableIndices.isEmpty()) {
								cache.set(replacableIndices.poll(), neighbour);
							}
						}
					}
					break;
				
				// If the message is a shuffle reply:
				case SHUFFLE_REPLY:
				//	  1. In this case Q initiated a shuffle with P and is receiving a response containing a subset of P's neighbors
				//	  2. Q updates its cache to include the neighbors sent by P:
				//		 - No neighbor appears twice in the cache
				//		 - Use empty cache slots to add new entries
				//		 - If the cache is full, you can replace entries among the ones originally sent to P with the new ones
					updateCache(p, message.getShuffleList());
					
				//	  3. Q is no longer waiting for a shuffle reply;
					awaitResponse = false;
					
					nodeRemoved = false;
					
					for (Entry entry : cache) {
						entry.setSentTo(null);
					}
					break;
				
				// If the message is a shuffle rejection:
				case SHUFFLE_REJECTED:
					/*for (Entry entry : cache) {
						entry.setSentTo(null);
					}*/
					
				//	  1. If P was originally removed from Q's cache, add it again to the cache.
					if(nodeRemoved) {
						cache.add(new Entry(p));
						nodeRemoved = false;
					}
					
				//	  2. Q is no longer waiting for a shuffle reply;
					awaitResponse = false;
					break;
				
				default:
					break;
				}
		
	}

	private void updateCache(Node source, List<Entry> neighbours) {
		Queue<Integer> replacableIndices = new LinkedList<Integer>();
		List<Entry> cacheCopy = new ArrayList<Entry>();
	
		ListIterator<Entry> it = cache.listIterator();
		while (it.hasNext()) {
			Entry q_neighbour = it.next();
	
			cacheCopy.add(q_neighbour);
			
			Node sentTo = q_neighbour.getSentTo();
			if (sentTo != null && source.getID() == sentTo.getID()) {
				replacableIndices.add(it.nextIndex() - 1);
			}
		}
	
		for (Entry neighbour : neighbours) {
			boolean alreadyInCache = cacheCopy.remove(neighbour);
	
			if (!alreadyInCache) {
				if (cache.size() < size) {
					cache.add(neighbour);
				} else if (!replacableIndices.isEmpty()) {
					cache.set(replacableIndices.poll(), neighbour);
				}
			}
		}
	}
	
/* The following methods are used only by the simulator and don't need to be changed */
	
	@Override
	public int degree() {
		return cache.size();
	}

	@Override
	public Node getNeighbor(int i) {
		return cache.get(i).getNode();
	}

	@Override
	public boolean addNeighbor(Node neighbour) {
		if (contains(neighbour))
			return false;

		if (cache.size() >= size)
			return false;

		Entry entry = new Entry(neighbour);
		cache.add(entry);

		return true;
	}

	@Override
	public boolean contains(Node neighbor) {
		return cache.contains(new Entry(neighbor));
	}

	public Object clone()
	{
		BasicShuffle gossip = null;
		try { 
			gossip = (BasicShuffle) super.clone(); 
		} catch( CloneNotSupportedException e ) {
			
		} 
		gossip.cache = new ArrayList<Entry>();

		return gossip;
	}

	@Override
	public void onKill() {
		// TODO Auto-generated method stub		
	}

	@Override
	public void pack() {
		// TODO Auto-generated method stub	
	}
}
