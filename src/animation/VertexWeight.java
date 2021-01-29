package animation;

public class VertexWeight {
	private int boneId;
	private int vertexId;
	private float weight;
	
	public VertexWeight(int boneId, int vertexId, float weight) {
		this.boneId = boneId;
		this.vertexId = vertexId;
		this.weight = weight;
	}
	
	public int getBoneID() {
		return boneId;
	}
	
	public int getVertexID() {
		return vertexId;
	}
	
	public float getWeight() {
		return weight;
	}
	
	public void setVertexId(int vertexId) {
		this.vertexId = vertexId;
	}
	
	public void setWeight(float weight) {
		this.weight = weight;
	}
}
