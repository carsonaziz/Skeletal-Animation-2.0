package utilities;

import java.util.HashMap;
import java.util.Map;

import graphics.Texture;

public class TextureCache {
	
	private static TextureCache INSTANCE;
	
	private Map<String, Texture> texturesMap;
	
	private TextureCache() {
		texturesMap = new HashMap<String, Texture>();
	}
	
	public static synchronized TextureCache getInstance() {
		if(INSTANCE == null) {
			INSTANCE = new TextureCache();
		}
		
		return INSTANCE;
	}
	
	public Texture getTexture(String path) throws Exception {
		Texture texture = texturesMap.get(path);
		if(texture == null) {
			texture = new Texture(path);
			texturesMap.put(path, texture);
		}
		
		return texture;
	}
}
