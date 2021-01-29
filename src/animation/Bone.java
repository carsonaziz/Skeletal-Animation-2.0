package animation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;

public class Bone {
	private final int id;
	private final Matrix4f offsetMatrix;
	
	private Map<Integer, List<BoneTransformation>> boneTransformationsByAnimation;
	private Map<Integer, List<Float>> timeStampsByAnimation;
	
	public Bone(int id, Matrix4f offsetMatrix) {
		this.id = id;
		this.offsetMatrix = offsetMatrix;
		this.boneTransformationsByAnimation = new HashMap<Integer, List<BoneTransformation>>();
		this.timeStampsByAnimation = new HashMap<Integer, List<Float>>();
	}
	
	public int getID() {
		return id;
	}
	
	public Matrix4f getOffsetMatrix() {
		return offsetMatrix;
	}
	
	public void addTransformations(int animationID, List<BoneTransformation> boneTransformations) {
		boneTransformationsByAnimation.put(animationID, boneTransformations);
	}
	
	public List<BoneTransformation> getBoneTransformations(int animationID) {
		return boneTransformationsByAnimation.get(animationID);
	}
	
	public BoneTransformation getBoneTransformation(int animationID, int frameIdx) {
		return boneTransformationsByAnimation.get(animationID).get(frameIdx);
	}
	
	public void addTimeStamps(int animationID, List<Float> timeStamps) {
		timeStampsByAnimation.put(animationID, timeStamps);
	}
	
	public List<Float> getTimeStamps(int animationID) {
		return timeStampsByAnimation.get(animationID);
	}
	
	public float getTimeStamp(int animationID, int frameIdx) {
		return timeStampsByAnimation.get(animationID).get(frameIdx);
	}
}
