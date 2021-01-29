package graphics;

import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import utilities.Utils;

public class Mesh {
	
	public static final int MAX_WEIGHTS = 4;
	
	private int vaoID;
	private int vertexCount;
	
	private List<Texture> textures;
	
	public Mesh(float[] positions, float[] normals, float[] texCoords, int[] indices) {
		this(positions, normals, texCoords, indices, Mesh.createEmptyIntArray(Mesh.MAX_WEIGHTS * positions.length / 3, 0), Mesh.createEmptyFloatArray(Mesh.MAX_WEIGHTS * positions.length / 3, 0));
	}
	
	public Mesh(float[] positions, float[] normals, float[] texCoords, int[] indices, int[] boneIDs, float[] weights) {
		vaoID = glGenVertexArrays();
		vertexCount = indices.length;
		textures = new ArrayList<Texture>();
		glBindVertexArray(vaoID);
		
		//Store data in vao
		storeFloatDataInAttributesList(0, 3, positions);
		storeFloatDataInAttributesList(1, 2, texCoords);
		storeFloatDataInAttributesList(2, 3, normals);
		storeFloatDataInAttributesList(3, 4, weights);
		storeIntDataInAttributesList(4, 4, boneIDs);
		storeIndicesInElementBuffer(indices);
		
		
		glBindVertexArray(0);
	}
	
	private void storeFloatDataInAttributesList(int index, int size, float[] data) {
		FloatBuffer buffer = Utils.storeDataInFloatBuffer(data);
		
		int vboID = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
		
		glVertexAttribPointer(index, size, GL_FLOAT, false, 0, 0);
		glEnableVertexAttribArray(index);
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	private void storeIntDataInAttributesList(int index, int size, int[] data) {
		IntBuffer buffer = Utils.storeDataInIntBuffer(data);
		
		int vboID = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
		
		glVertexAttribPointer(index, size, GL_FLOAT, false, 0, 0); //This should probably be of type integer, but GL_UNSIGNED_INT doesn't work
		glEnableVertexAttribArray(index);
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	private void storeIndicesInElementBuffer(int[] data) {
		IntBuffer buffer = Utils.storeDataInIntBuffer(data);
		
		int eboID = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
	}
	
	public int getVaoID() {
		return vaoID;
	}
	
	public int getVertexCount() {
		return vertexCount;
	}
	
	public List<Texture> getTextures() {
		return textures;
	}
	
	public void setTextures(List<Texture> textures) {
		this.textures = textures;
	}

    protected static float[] createEmptyFloatArray(int length, float defaultValue) {
        float[] result = new float[length];
        Arrays.fill(result, defaultValue);
        return result;
    }

    protected static int[] createEmptyIntArray(int length, int defaultValue) {
        int[] result = new int[length];
        Arrays.fill(result, defaultValue);
        return result;
    }
}
