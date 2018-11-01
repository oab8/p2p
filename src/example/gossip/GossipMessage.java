package example.gossip;

import java.util.List;

import peersim.core.Node;

public class GossipMessage {
	
	private Node node;
	private List<Entry> shuffleList;
	private MessageType type;
	
	public GossipMessage(Node node, List<Entry> shuffleList) {
		super();
		this.node = node;
		this.shuffleList = shuffleList;
	}

	public Node getNode() {
		return node;
	}
	public void setNode(Node node) {
		this.node = node;
	}
	public List<Entry> getShuffleList() {
		return shuffleList;
	}
	public void setShuffleList(List<Entry> shuffleList) {
		this.shuffleList = shuffleList;
	}

	public MessageType getType() {
		return type;
	}

	public void setType(MessageType type) {
		this.type = type;
	}
	
}
