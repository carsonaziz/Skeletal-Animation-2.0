package animation;

public class Animation {
	private float elapsedTime;
	private boolean playing = false;
	
	private final int id;
	private final String name;
	private final Keyframe[] keyframes;
	private final float duration;
	
	private boolean loop = false;
	
	public Animation(int id, String name, Keyframe[] keyframes, float duration) {
		this.elapsedTime = 0.0f;
		this.id = id;
		this.name = name;
		this.keyframes = keyframes;
		this.duration = duration;
	}
	
	public void playAnimation() {
		if(!playing) {
			elapsedTime = 0.0f;
			playing = true;
		}
	}
	
	public void increaseTime(float delta) {
		if(!loop) {
		//this code will stop the animation when its done
			if(playing) {
				if(elapsedTime + delta > duration) {
					//elapsedTime = duration - 0.011f;
					playing = false;
				} else {
					elapsedTime += delta;	
				}
			}
		} else {
			//This code below will loop the animation
			elapsedTime += delta;
			if(elapsedTime > duration) {
				elapsedTime %= duration;
			}
		}
	}
	
	public void setLoop(boolean loop) {
		this.loop = loop;
	}
	
	public Keyframe[] getPreviousAndNextKeyframes() {
		Keyframe previousKeyframe = keyframes[0];
		Keyframe nextKeyframe = keyframes[0];
		for(int i = 1; i < keyframes.length; i++) {
			nextKeyframe = keyframes[i];
			if(nextKeyframe.getTimeStamp() > elapsedTime) {
				break;
			}
			previousKeyframe = keyframes[i];
		}
		
		return new Keyframe[] { previousKeyframe, nextKeyframe };
	}
	
	public float getElapsedTime() {
		return elapsedTime;
	}
	
	public String getName() {
		return name;
	}
	
	public Keyframe[] getKeyframes() {
		return keyframes;
	}
	
	public float getDuration() {
		return duration;
	}
	
	public boolean isPlaying() {
		return playing;
	}
}
