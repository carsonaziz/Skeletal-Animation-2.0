package engine;

import static org.lwjgl.glfw.GLFW.*;

import java.util.ArrayList;
import java.util.List;

import entities.GameItem;

public class Mouse {
	private List<GameItem> gameItems;
	
	public Mouse(List<GameItem> gameItems) {
		this.gameItems = gameItems;
	}
	
	public void init(Window window) {
		glfwSetMouseButtonCallback(window.getWindowHandle(), (windowHandle, button, action, mode) -> {
			if(button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
				List<String> nodeNames = new ArrayList<String>();
//				nodeNames.add("Armature_Right_Clavicle");
//				nodeNames.add("Armature_Left_Clavicle");
				nodeNames.add("Armature_Chest");
				gameItems.get(0).addMaskedAnimation(1, nodeNames, false);
			}
		});
	}
}
