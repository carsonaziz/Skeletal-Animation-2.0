package graphics;

import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.system.MemoryStack;

import utilities.Utils;

public class Texture {

	private final int id;
	private final int width;
	private final int height;
	
	public Texture(String fileName) throws Exception {
		this(Utils.ioResourceToByteBuffer(fileName, 1024));
	}
	
	public Texture(ByteBuffer imageData) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer avChannels = stack.mallocInt(1);

            // Decode texture image into a byte buffer
            ByteBuffer decodedImage = stbi_load_from_memory(imageData, w, h, avChannels, 4);

            this.width = w.get();
            this.height = h.get();

            // Create a new OpenGL texture 
            this.id = glGenTextures();
            // Bind the texture
            glBindTexture(GL_TEXTURE_2D, this.id);

            // Tell OpenGL how to unpack the RGBA bytes. Each component is 1 byte size
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            // Upload the texture data
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, this.width, this.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, decodedImage);
            // Generate Mip Map
            glGenerateMipmap(GL_TEXTURE_2D);
            
            //Unbind texture
            glBindTexture(GL_TEXTURE_2D, 0);
            
            //Free image memory
            stbi_image_free(imageData);
        }
	}
	
	public int getID() {
		return id;
	}
}
