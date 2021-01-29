package engine;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL30.*;

import org.joml.Matrix4f;

import static org.lwjgl.glfw.GLFW.*;

import animation.AnimatedFrame;
import animation.Animator;
import entities.GameItem;
import graphics.Mesh;
import shaders.ShaderProgram;
import utilities.TransformationMatrix;

public class Renderer {
	private final Window window;
	
	public Renderer(Window window) {
		this.window = window;
	}
	
	public void init() {
		glEnable(GL_DEPTH_TEST);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	}
	
	public void render(Animator animator, ShaderProgram shaderProgram, GameItem gameItem) {
		AnimatedFrame animatedFrame = animator.getAnimatedFrame(gameItem.getID());
		shaderProgram.loadMatrices("bonesMatrix", animatedFrame.getBoneMatrices());
		
		Mesh[] meshes = gameItem.getMeshes();
		for(Mesh mesh : meshes) {
			glBindTexture(GL_TEXTURE_2D, mesh.getTextures().get(0).getID());
			glBindVertexArray(mesh.getVaoID());
			glDrawElements(GL_TRIANGLES, mesh.getVertexCount(), GL_UNSIGNED_INT, 0);
			glBindVertexArray(0);
		}
	}
	
	public void update() {
		window.update();
	}
}
