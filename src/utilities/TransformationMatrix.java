package utilities;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import entities.Camera;

public class TransformationMatrix {
	
	public static Matrix4f createModelMatrix(Vector3f position, float rotX, float rotY, float rotZ, float scale) {
		Matrix4f model = new Matrix4f().identity()
			.translate(position)
			.rotate((float)Math.toRadians(rotX), new Vector3f(1, 0, 0))
			.rotate((float)Math.toRadians(rotY), new Vector3f(0, 1, 0))
			.rotate((float)Math.toRadians(rotZ), new Vector3f(0, 0, 1))
			.scale(new Vector3f(scale, scale, scale));
		
		return model;
	}
	
	public static Matrix4f createViewMatrix(Camera camera) {
		Matrix4f view = new Matrix4f().identity()
			.translate(new Vector3f(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z));
		
		return view;
	}
	
	public static Matrix4f createProjectionMatrix(float fov, float aspectRatio, float nearPlane, float farPlane) {
		Matrix4f projection = new Matrix4f();
		projection.setPerspective(fov, aspectRatio, nearPlane, farPlane);
		
		return projection;
	}
}
