package animation;

import java.util.ArrayList;
import java.util.List;

public class MaskedAnimation {
	private final List<Node> maskBoneNodes;
	private final Animation animation;
	
	public MaskedAnimation(List<Node> maskBoneNodes, Animation animation) {
		this.maskBoneNodes = maskBoneNodes;
		this.animation = animation;
	}
	
	public Animation getAnimation() {
		return animation;
	}
	
	public List<Integer> getMaskedBones() {
		List<Integer> maskBoneIDs = new ArrayList<Integer>();
		for(Node maskBoneNode : maskBoneNodes) {
			findMaskBoneChildren(maskBoneNode, maskBoneIDs);
		}
		
		return maskBoneIDs;
	}
	
	private void findMaskBoneChildren(Node node, List<Integer> maskBoneIDs) {
		maskBoneIDs.add(node.getBone().getID());
		
		for(Node childNode : node.getChildren()) {
			findMaskBoneChildren(childNode, maskBoneIDs);
		}
	}
}
