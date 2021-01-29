package animation;

import java.util.ArrayList;
import java.util.List;

public class Node {
	private final String name;
	private final Node parent;
	private final List<Node> children;
	
	private Bone bone;
	
	public Node(String name, Node parent) {
		this.name = name;
		this.bone = null;
		this.parent = parent;
		this.children = new ArrayList<Node>();
	}
	
	public void addChild(Node node) {
		children.add(node);
	}
	
	public void setBone(Bone bone) {
		this.bone = bone;
	}
	
	public Node findByName(String targetName) {
		Node result = null;
		if(this.name.equals(targetName)) {
			result = this;
		} else {
			for(Node child : children) {
				result = child.findByName(targetName);
				if(result != null) {
					break;
				}
			}
		}
		return result;
	}
	
	public List<Node> getChildren() {
		return children;
	}
	
	public Bone getBone() {
		return bone;
	}
}
