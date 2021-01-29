package animation;

import java.util.Arrays;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Keyframe {
	public static final int MAX_BONES = 150;
	
	private float timeStamp;
	private final BoneTransformation[] boneTransformations;
	
	public Keyframe() {
		timeStamp = -1;
		boneTransformations = new BoneTransformation[MAX_BONES];	//This is probably very inefficient. the array size should be the # of bones in the model
		Arrays.fill(boneTransformations, new BoneTransformation(new Vector3f(1, 1, 1), new Quaternionf(0, 0, 0, 1), new Vector3f(1, 1, 1)));
	}
	
	public BoneTransformation[] getBoneTransformations() {
		return boneTransformations;
	}
	
	public void setBoneTransformation(int frameIdx, BoneTransformation boneTransformation) {
		boneTransformations[frameIdx] = boneTransformation;
	}
	
	public float getTimeStamp() {
		return timeStamp;
	}
	
	public void setTimeStamp(float timeStamp) {
		this.timeStamp = timeStamp;
	}
}
