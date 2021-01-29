package entities;
import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import animation.Animation;
import animation.Bone;
import animation.MaskedAnimation;
import animation.Node;
import graphics.Mesh;

public class GameItem {
	private static int numGameItems = 0;
	
	private final int id;
	private final Node rootNode;
	private final Matrix4f rootTransformation;
	
	private List<Animation> animations;
	private Animation currentAnimation;
	private List<MaskedAnimation> maskedAnimations;
	
	private Mesh[] meshes;
	
	private Vector3f position;
	private Vector3f rotation;
	private float scale;
	
	public GameItem(Mesh[] meshes, List<Animation> animations, Node rootNode, Matrix4f rootTransformation) {
		//It would probably be cleaner to create a model class that stores the rootNode, rootTransform, 
		//animations, currentAnimation, maskedAnimations and the meshes and then just give GameItem a model as a property
		this.id = numGameItems++; //This line very well could just not work
		this.meshes = meshes;
		this.animations = animations;
		this.rootNode = rootNode;
		this.rootTransformation = rootTransformation;
		this.currentAnimation = animations.size() > 0 ? animations.get(2) : null;
		this.currentAnimation.setLoop(true);
		this.maskedAnimations = new ArrayList<MaskedAnimation>();
		
		this.position = new Vector3f(0, 0, 0);
		this.rotation = new Vector3f(0, 0, 0);
		this.scale = 1;
	}
	
	public void update() {
		for(int i = 0; i < maskedAnimations.size(); i++) {
			if(!maskedAnimations.get(i).getAnimation().isPlaying()) {
				maskedAnimations.remove(i);
			}
		}
	}
	
	public Animation getCurrentAnimation() {
		return currentAnimation;
	}
	
	public void addMaskedAnimation(int animationID, List<String> maskBoneNames, boolean loop) {
		Animation animation = animations.get(animationID);
		animation.setLoop(loop);
		animation.playAnimation();
		
		List<Node> nodes = new ArrayList<Node>();
		for(String maskBoneName : maskBoneNames) {
			nodes.add(rootNode.findByName(maskBoneName));
		}
		
		boolean animationExists = false;
		for(MaskedAnimation maskedAnimation : maskedAnimations) {
			if(maskedAnimation.getAnimation().getName() == animation.getName()) {
				animationExists = true;
			}
		}
		
		if(!animationExists) {
			maskedAnimations.add(new MaskedAnimation(nodes, animation));
		}
	}
	
	public List<MaskedAnimation> getMaskedAnimations() {
		return maskedAnimations;
	}
	
	public int getID() {
		return id;
	}
	
	public Node getRootNode() {
		return rootNode;
	}
	
	public Matrix4f getRootTransform() {
		return rootTransformation;
	}
	
	public Mesh[] getMeshes() {
		return meshes;
	}
}
