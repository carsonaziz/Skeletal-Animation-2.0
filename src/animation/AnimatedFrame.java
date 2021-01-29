package animation;

import java.util.Arrays;

import org.joml.Matrix4f;

public class AnimatedFrame {
	private static final Matrix4f IDENTITY_MATRIX = new Matrix4f();
	public static final int MAX_BONES = 150;
	private final Matrix4f[] boneMatrices;
	
	public AnimatedFrame() {
		boneMatrices = new Matrix4f[MAX_BONES];
		Arrays.fill(boneMatrices, IDENTITY_MATRIX);
	}
	
	public Matrix4f[] getBoneMatrices() {
		return boneMatrices;
	}
	
	public void setMatrix(int boneID, Matrix4f boneMatrix) {
		boneMatrices[boneID] = boneMatrix;
	}
}
