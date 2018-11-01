package example.gossip;

import peersim.core.Node;

public class Entry {
	private Node node;
	private Node sentTo;
	
	public Entry(Node node) {
		super();
		this.node = node;
		this.sentTo = null;
	}
	
	public Node getNode() {
		return node;
	}
	
	public void setNode(Node node) {
		this.node = node;
	}
	
	public Node getSentTo() {
		return sentTo;
	}
	
	public void setSentTo(Node sentTo) {
		this.sentTo = sentTo;
	}

	public boolean equals (Object o) {
		return ((Entry) o).getNode().getID() == getNode().getID();
	}
	
}
