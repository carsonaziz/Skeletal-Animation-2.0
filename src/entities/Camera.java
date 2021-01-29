package entities;

import org.joml.Vector3f;

public class Camera {
	private Vector3f position;
	
	public Camera() {
		this.position = new Vector3f(0, 0, 0);
	}
	
	public Camera(Vector3f position) {
		this.position = position;
	}
	
	public Vector3f getPosition() {
		return position;
	}
}
