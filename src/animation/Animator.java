package animation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;

import entities.GameItem;

public class Animator {
	private List<GameItem> gameItems;
	private Map<Integer, AnimatedFrame> animatedFrames;
	
	public Animator(List<GameItem> gameItems) {
		this.gameItems = gameItems;
		this.animatedFrames = new HashMap<Integer, AnimatedFrame>();
	}
	
	public void update(float elapsedTime) {
		for(GameItem gameItem : gameItems) {
			Animation currentAnimation = gameItem.getCurrentAnimation();
			if(currentAnimation.isPlaying()) {
				gameItem.getCurrentAnimation().increaseTime(elapsedTime);
			}
			for(MaskedAnimation maskedAnimation : gameItem.getMaskedAnimations()) {
				Animation animation = maskedAnimation.getAnimation();
				if(animation.isPlaying()) {
					maskedAnimation.getAnimation().increaseTime(elapsedTime);
				}
			}
			animatedFrames.put(gameItem.getID(), buildCurrentFrame(gameItem));
		}
	}
	
	private AnimatedFrame buildCurrentFrame(GameItem gameItem) {
		Animation animation = gameItem.getCurrentAnimation();
		List<MaskedAnimation> maskedAnimations = gameItem.getMaskedAnimations();
		
		if(animation == null) {
			return null;
		}
		
		Map<Integer, Matrix4f> currentLocalPose = calculateLocalPose(animation, maskedAnimations);
			//This will need to access:
			// - Keyframes (contained in animation class, need to attach each transformation per keyframe to a bone somehow (could be a map))
			// - Access to the bones
		//globalPose - use localPose of parents to generate the global transformation (this is where the root transformation is used)
		Map<Integer, Matrix4f> currentGlobalPose = new HashMap<Integer, Matrix4f>();
		calculateGlobalPose(currentLocalPose, gameItem.getRootNode(), gameItem.getRootTransform(), currentGlobalPose);
		return buildAnimatedFrame(currentGlobalPose);
	}
	
	private Map<Integer, Matrix4f> calculateLocalPose(Animation animation, List<MaskedAnimation> maskedAnimations) {
		//Interpolate between keyframes for main animation, then interpolate for masked animation. If there is no mask this needs to be handled
		//For a mask, the bone id of the masked bone and all its children need to be known
		Keyframe[] primaryKeyframes = animation.getPreviousAndNextKeyframes();
		PrimaryInterpolationData primaryInterpolationData = new PrimaryInterpolationData(primaryKeyframes, calculateProgression(primaryKeyframes[0], primaryKeyframes[1], animation.getElapsedTime()));
		List<MaskedInterpolationData> maskedInterpolationDatas = new ArrayList<MaskedInterpolationData>();
		
		if(maskedAnimations != null) {
			for(MaskedAnimation maskedAnimation : maskedAnimations) {
				Keyframe[] maskedKeyframes = maskedAnimation.getAnimation().getPreviousAndNextKeyframes();
				float progression = calculateProgression(maskedKeyframes[0], maskedKeyframes[1], maskedAnimation.getAnimation().getElapsedTime());
				maskedInterpolationDatas.add(new MaskedInterpolationData(maskedAnimation.getMaskedBones(), maskedKeyframes, progression));
			}
		}
		
		return interpolatePoses(primaryInterpolationData, maskedInterpolationDatas);
	}
	
	private void calculateGlobalPose(Map<Integer, Matrix4f> currentLocalPose, Node node, Matrix4f parentTransformation, Map<Integer, Matrix4f> globalPose) {
		Matrix4f currentLocalTransformation = null;
		Matrix4f currentGlobalTransformation = null;
		
		Bone bone = node.getBone();
		if(bone != null) {
			currentLocalTransformation = currentLocalPose.get(bone.getID());
			currentGlobalTransformation = parentTransformation.mul(currentLocalTransformation);
		} else {
			currentGlobalTransformation = parentTransformation;
		}
		
		for(Node childNode : node.getChildren()) {
			calculateGlobalPose(currentLocalPose, childNode, new Matrix4f(currentGlobalTransformation), globalPose);
		}
		
		if(bone != null) {
			currentGlobalTransformation = currentGlobalTransformation.mul(bone.getOffsetMatrix());
			globalPose.put(bone.getID(), currentGlobalTransformation);
		}
	}
	
	private AnimatedFrame buildAnimatedFrame(Map<Integer, Matrix4f> currentGlobalPose) {
		AnimatedFrame animatedFrame = new AnimatedFrame();
		for(Map.Entry<Integer, Matrix4f> matrixEntry : currentGlobalPose.entrySet()) {
			int boneID = matrixEntry.getKey();
			Matrix4f matrix = matrixEntry.getValue();
			animatedFrame.setMatrix(boneID, matrix);
		}
		
		return animatedFrame;
	}
	
	private float calculateProgression(Keyframe previousKeyframe, Keyframe nextKeyframe, float elapsedTime) {
		float timeBetweenFrames = nextKeyframe.getTimeStamp() - previousKeyframe.getTimeStamp();
		float progressionTime = elapsedTime - previousKeyframe.getTimeStamp();
		return progressionTime / timeBetweenFrames;
	}
	
	private Map<Integer, Matrix4f> interpolatePoses(PrimaryInterpolationData primaryInterpolationData, List<MaskedInterpolationData> maskedInterpolationDatas) {
		Map<Integer, Matrix4f> currentLocalPose = new HashMap<Integer, Matrix4f>();
		
		Keyframe previousPrimaryKeyframe = primaryInterpolationData.getKeyframes()[0];
		Keyframe nextPrimaryKeyframe = primaryInterpolationData.getKeyframes()[1];
		float primaryProgression = primaryInterpolationData.getProgression();
		
		for(int i = 0; i < previousPrimaryKeyframe.getBoneTransformations().length; i++) {
			if(maskedInterpolationDatas.size() > 0) {
				for(MaskedInterpolationData maskedInterpolationData : maskedInterpolationDatas) {
					Keyframe previousMaskedKeyframe = maskedInterpolationData.getKeyframes()[0];
					Keyframe nextMaskedKeyframe = maskedInterpolationData.getKeyframes()[1];
					float maskedProgression = maskedInterpolationData.getProgression();
					if(maskedInterpolationData.getMaskedBones().contains(i)) {
						BoneTransformation previousBoneTransformation = previousMaskedKeyframe.getBoneTransformations()[i];
						BoneTransformation nextBoneTransformation = nextMaskedKeyframe.getBoneTransformations()[i];
						BoneTransformation currentBoneTransformation = BoneTransformation.interpolate(previousBoneTransformation, nextBoneTransformation, maskedProgression);
						currentLocalPose.put(i, currentBoneTransformation.convertToMatrix());
					} else {
						BoneTransformation previousBoneTransformation = previousPrimaryKeyframe.getBoneTransformations()[i];
						BoneTransformation nextBoneTransformation = nextPrimaryKeyframe.getBoneTransformations()[i];
						BoneTransformation currentBoneTransformation = BoneTransformation.interpolate(previousBoneTransformation, nextBoneTransformation, primaryProgression);
						currentLocalPose.put(i, currentBoneTransformation.convertToMatrix());
					}
				}
			} else {
				BoneTransformation previousBoneTransformation = previousPrimaryKeyframe.getBoneTransformations()[i];
				BoneTransformation nextBoneTransformation = nextPrimaryKeyframe.getBoneTransformations()[i];
				BoneTransformation currentBoneTransformation = BoneTransformation.interpolate(previousBoneTransformation, nextBoneTransformation, primaryProgression);
				currentLocalPose.put(i, currentBoneTransformation.convertToMatrix());
			}
		}
		
		return currentLocalPose;
	}
	
	public AnimatedFrame getAnimatedFrame(int gameItemID) {
		return animatedFrames.get(gameItemID);
	}
	
	class MaskedInterpolationData {
		private List<Integer> maskedBones;
		private Keyframe[] keyframes;
		private float progression;
		
		public MaskedInterpolationData(List<Integer> maskedBones, Keyframe[] keyframes, float progression) {
			this.maskedBones = maskedBones;
			this.keyframes = keyframes;
			this.progression = progression;
		}
		
		public List<Integer> getMaskedBones() {
			return maskedBones;
		}
		
		public Keyframe[] getKeyframes() {
			return keyframes;
		}
		
		public float getProgression() {
			return progression;
		}
	}
	
	class PrimaryInterpolationData {
		private Keyframe[] keyframes;
		private float progression;
		
		public PrimaryInterpolationData(Keyframe[] keyframes, float progression) {
			this.keyframes = keyframes;
			this.progression = progression;
		}
		
		public Keyframe[] getKeyframes() {
			return keyframes;
		}
		
		public float getProgression() {
			return progression;
		}
	}
	
}
