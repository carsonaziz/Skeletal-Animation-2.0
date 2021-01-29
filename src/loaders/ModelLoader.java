package loaders;

import static org.lwjgl.assimp.Assimp.*;

import java.io.File;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIAnimation;
import org.lwjgl.assimp.AIBone;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMatrix4x4;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AINode;
import org.lwjgl.assimp.AINodeAnim;
import org.lwjgl.assimp.AIQuatKey;
import org.lwjgl.assimp.AIQuaternion;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.AIVectorKey;
import org.lwjgl.assimp.AIVertexWeight;

import animation.Animation;
import animation.Bone;
import animation.BoneTransformation;
import animation.Keyframe;
import animation.Node;
import animation.VertexWeight;
import entities.GameItem;
import graphics.Mesh;
import graphics.Texture;
import utilities.TextureCache;
import utilities.Utils;

public class ModelLoader {
	
	public static GameItem loadGameItem(String modelPath, String animationsDir, String texturesDir) throws Exception {
		return loadGameItem(modelPath, animationsDir, texturesDir, aiProcess_GenSmoothNormals 
				| aiProcess_JoinIdenticalVertices 
				| aiProcess_Triangulate 
				| aiProcess_FixInfacingNormals 
				| aiProcess_LimitBoneWeights);
	}
	
	private static GameItem loadGameItem(String modelPath, String animationsDir, String texturesDir, int flags) throws Exception {
		AIScene aiScene = aiImportFile(modelPath, flags);
		
		if(aiScene == null) {
			throw new Exception("Error loading model");
		}
		
		AINode aiRootNode = aiScene.mRootNode();
		Matrix4f rootTransformation = toMatrix4f(aiRootNode.mTransformation());
		Node rootNode = processNodesHierarchy(aiRootNode, null);
		
		int currentBoneID = 0;
		int numMeshes = aiScene.mNumMeshes();
		PointerBuffer aiMeshes = aiScene.mMeshes();
		Mesh[] meshes = new Mesh[numMeshes];
		for(int i = 0; i < numMeshes; i++) {
			AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));
			Mesh mesh = processMesh(aiMesh, rootNode, currentBoneID);
			meshes[i] = mesh;
			mesh.setTextures(processTextures(texturesDir));
		}
		List<Animation> animations = processAnimations(animationsDir, rootNode);	//Masked animations will be added to a game item. This information is not in .dae
		
		return new GameItem(meshes, animations, rootNode, rootTransformation);
	}
	
	private static List<Texture> processTextures(String texturesDir) {
		List<Texture> textures = new ArrayList<Texture>();
		
		File dir = new File(texturesDir);
		File[] textureFiles = dir.listFiles();
		if(textureFiles != null) {
			for(File textureFile : textureFiles) {
				Texture texture = null;
				try {
					texture = TextureCache.getInstance().getTexture(textureFile.getPath());
				} catch (Exception e) {
					e.printStackTrace();
				}
				textures.add(texture);
			}
		}
		
		return textures;
	}
	
	private static List<Animation> processAnimations(String animationsDir, Node rootNode) throws Exception {
		List<Animation> animations = new ArrayList<Animation>();
		
		File dir = new File(animationsDir);
		File[] animationFiles = dir.listFiles();
		if(animationFiles != null) {
			for(int i = 0; i < animationFiles.length; i++) {
				File animationFile = animationFiles[i];
				String path = animationFile.getPath();
				Animation animation = processAnimation(i, path, rootNode);
				animations.add(animation);
			}
		}
		
		return animations;
	}
	
	private static Animation processAnimation(int animationID, String path, Node rootNode) throws Exception {
		return processAnimation(animationID, path, rootNode, aiProcess_GenSmoothNormals 
				| aiProcess_JoinIdenticalVertices 
				| aiProcess_Triangulate 
				| aiProcess_FixInfacingNormals 
				| aiProcess_LimitBoneWeights);
	}
	
	private static Animation processAnimation(int animationID, String path, Node rootNode, int flags) throws Exception {
		AIScene aiScene = aiImportFile(path, flags);
		
		if(aiScene == null) {
			throw new Exception("Error loading animation");
		}
		
		AIAnimation aiAnimation = AIAnimation.create(aiScene.mAnimations().get(0));
		String name = path;
		
		int numChannels = aiAnimation.mNumChannels();
		PointerBuffer aiChannels = aiAnimation.mChannels();
		for(int i = 0; i < numChannels; i++) {
			AINodeAnim aiNodeAnim = AINodeAnim.create(aiChannels.get(i));
			String nodeName = aiNodeAnim.mNodeName().dataString();
			Bone bone = rootNode.findByName(nodeName).getBone();
			buildTransformationMatrices(animationID, aiNodeAnim, bone);
		}
		
		int numKeyframes = AINodeAnim.create(aiChannels.get(0)).mNumPositionKeys();
		Keyframe[] keyframes = createKeyframes(animationID, rootNode, numKeyframes);
		return new Animation(animationID, name, keyframes, (float)aiAnimation.mDuration());
	}
	
	private static void buildTransformationMatrices(int animationID, AINodeAnim aiNodeAnim, Bone bone) {
		List<BoneTransformation> boneTransformations = new ArrayList<BoneTransformation>();
		List<Float> timeStamps = new ArrayList<Float>();
		
		int numFrames = aiNodeAnim.mNumPositionKeys();

		AIVectorKey.Buffer positionKeys = aiNodeAnim.mPositionKeys();
		AIQuatKey.Buffer rotationKeys = aiNodeAnim.mRotationKeys();
		AIVectorKey.Buffer scalingKeys = aiNodeAnim.mScalingKeys();
		
		for(int i = 0; i < numFrames; i++) {
			//Get position data
			AIVectorKey aiVecKey = positionKeys.get(i);
			AIVector3D vecPos = aiVecKey.mValue();

			//Get rotation data
			AIQuatKey quatKey = rotationKeys.get(i);
			AIQuaternion aiQuat = quatKey.mValue();
			Quaternionf quat = new Quaternionf(aiQuat.x(), aiQuat.y(), aiQuat.z(), aiQuat.w());
			
			//Get scaling data
			Vector3f vecScale = new Vector3f(1, 1, 1);
			if(i < aiNodeAnim.mNumScalingKeys()) {
				aiVecKey = scalingKeys.get(i);
				vecScale = new Vector3f(aiVecKey.mValue().x(), aiVecKey.mValue().y(), aiVecKey.mValue().z());
			}
			
			float timeStamp = (float)aiVecKey.mTime();
			BoneTransformation boneTransformation = new BoneTransformation(new Vector3f(vecPos.x(), vecPos.y(), vecPos.z()), quat, new Vector3f(vecScale.x(), vecScale.y(), vecScale.z()));
			
			timeStamps.add(timeStamp);
			boneTransformations.add(boneTransformation);
		}
		bone.addTimeStamps(animationID, timeStamps);
		bone.addTransformations(animationID, boneTransformations);
	}
	
	private static Keyframe[] createKeyframes(int animationID, Node rootNode, int numKeyframes) {
		Keyframe[] keyframeArray = new Keyframe[numKeyframes];
		for(int frameIdx = 0; frameIdx < numKeyframes; frameIdx++) {
			Keyframe keyframe = new Keyframe();
			keyframeArray[frameIdx] = keyframe;
			
			createKeyframe(animationID, rootNode, keyframe, frameIdx);
		}
		
		return keyframeArray;
	}
	
	private static void createKeyframe(int animationID, Node node, Keyframe keyframe, int frameIdx) {
		Bone bone = node.getBone();
		if(bone != null) {
			BoneTransformation boneTransformation = bone.getBoneTransformation(animationID, frameIdx);
			float timeStamp = bone.getTimeStamp(animationID, frameIdx);
			
			keyframe.setBoneTransformation(bone.getID(), boneTransformation);
			keyframe.setTimeStamp(timeStamp);
		}

		for(Node childNode : node.getChildren()) {
			createKeyframe(animationID, childNode, keyframe, frameIdx);
		}
	}
	
	private static Mesh processMesh(AIMesh aiMesh, Node rootNode, int currentBoneID) {
		List<Float> vertices = new ArrayList<Float>();
        List<Float> textCoords = new ArrayList<>();
        List<Float> normals = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        List<Integer> boneIDs = new ArrayList<>();
        List<Float> weights = new ArrayList<>();
        
        processVertices(aiMesh, vertices);
        processTextCoords(aiMesh, textCoords);
        processNormals(aiMesh, normals);
        processIndices(aiMesh, indices);
        processBones(aiMesh, rootNode, currentBoneID, boneIDs, weights);
        
        //Texture coordinates may not have been populated. We need at least the empty slots
        if ( textCoords.size() == 0) {
            int numElements = (vertices.size() / 3) * 2;
            for (int i=0; i<numElements; i++) {
                textCoords.add(0.0f);
            }
        }
        
        Mesh mesh = new Mesh(Utils.listToArray(vertices), 
        		Utils.listToArray(normals), 
        		Utils.listToArray(textCoords), 
        		Utils.listIntToArray(indices), 
        		Utils.listIntToArray(boneIDs), 
        		Utils.listToArray(weights)); 
        
        return mesh;
	}
	
	private static void processVertices(AIMesh aiMesh, List<Float> vertices) {
		AIVector3D.Buffer aiVertices = aiMesh.mVertices();
		
		while(aiVertices.remaining() > 0) {
			AIVector3D aiVertex = aiVertices.get();
			vertices.add(aiVertex.x());
			vertices.add(aiVertex.y());
			vertices.add(aiVertex.z());
		}
	}
	
	private static void processTextCoords(AIMesh aiMesh, List<Float> texCoords) {
        AIVector3D.Buffer aiTextCoords = aiMesh.mTextureCoords(0);
        int numTextCoords = aiTextCoords != null ? aiTextCoords.remaining() : 0;
        for (int i = 0; i < numTextCoords; i++) {
            AIVector3D aiTextCoord = aiTextCoords.get();
            texCoords.add(aiTextCoord.x());
            texCoords.add(1 - aiTextCoord.y());
        }
	}
	
	private static void processNormals(AIMesh aiMesh, List<Float> normals) {
		AIVector3D.Buffer aiNormals = aiMesh.mNormals();
		
		while(aiNormals != null && aiNormals.remaining() > 0) {
			AIVector3D aiNormal = aiNormals.get();
            normals.add(aiNormal.x());
            normals.add(aiNormal.y());
            normals.add(aiNormal.z());
		}
	}
	
	private static void processIndices(AIMesh aiMesh, List<Integer> indices) {
        int numFaces = aiMesh.mNumFaces();
        AIFace.Buffer aiFaces = aiMesh.mFaces();
        for (int i = 0; i < numFaces; i++) {
            AIFace aiFace = aiFaces.get(i);
            IntBuffer buffer = aiFace.mIndices();
            while (buffer.remaining() > 0) {
                indices.add(buffer.get());
            }
        }
	}
	
	private static void processBones(AIMesh aiMesh, Node rootNode, int currentBoneID, List<Integer> boneIDs, List<Float> weights) {
		Map<Integer, List<VertexWeight>> weightSet = new HashMap<Integer, List<VertexWeight>>();
		
		int numBones = aiMesh.mNumBones();
		PointerBuffer aiBones = aiMesh.mBones();
		for(int i = 0; i < numBones; i++) {
			AIBone aiBone = AIBone.create(aiBones.get(i));
			
			int boneID = currentBoneID;
			currentBoneID++;
			String boneName = aiBone.mName().dataString();
			
			Bone bone = new Bone(boneID, toMatrix4f(aiBone.mOffsetMatrix()));
			Node node = rootNode.findByName(boneName);
			node.setBone(bone);
			
			processWeights(aiBone, bone.getID(), weightSet);
			
		}
		
		int numVertices = aiMesh.mNumVertices();
		for(int i = 0; i < numVertices; i++) {
			List<VertexWeight> vertexWeightList = weightSet.get(i);
			int size = vertexWeightList != null ? vertexWeightList.size() : 0;
			for(int j = 0; j < Mesh.MAX_WEIGHTS; j++) {
				if(j < size) {
					VertexWeight vw = vertexWeightList.get(j);
					weights.add(vw.getWeight());
					boneIDs.add(vw.getBoneID());
				} else {
					weights.add(0.0f);
					boneIDs.add(0);
				}
			}
		}
	}
	
	private static void processWeights(AIBone aiBone, int boneID, Map<Integer, List<VertexWeight>> weightSet) {
		int numWeights = aiBone.mNumWeights();
		AIVertexWeight.Buffer aiWeights = aiBone.mWeights();
		for(int i = 0; i < numWeights; i++) {
			AIVertexWeight aiWeight = aiWeights.get(i);
			VertexWeight vw = new VertexWeight(boneID, aiWeight.mVertexId(), aiWeight.mWeight());
			List<VertexWeight> vertexWeightList = weightSet.get(vw.getVertexID());
			
			if(vertexWeightList == null) {
				vertexWeightList = new ArrayList<VertexWeight>();
				weightSet.put(vw.getVertexID(),  vertexWeightList);
			}
			vertexWeightList.add(vw);
		}
	}
	
	private static Node processNodesHierarchy(AINode aiNode, Node parentNode) {
		String nodeName = aiNode.mName().dataString();
		Node node = new Node(nodeName, parentNode);
		
		int numChildren = aiNode.mNumChildren();
		PointerBuffer aiChildren = aiNode.mChildren();
		for(int i = 0; i < numChildren; i++) {
			AINode aiChildNode = AINode.create(aiChildren.get(i));
			Node childNode = processNodesHierarchy(aiChildNode, node);
			node.addChild(childNode);
		}
		
		return node;
	}
	
	private static Matrix4f toMatrix4f(AIMatrix4x4 aiMatrix4x4) {
		Matrix4f result = new Matrix4f();
        result.m00(aiMatrix4x4.a1());
        result.m10(aiMatrix4x4.a2());
        result.m20(aiMatrix4x4.a3());
        result.m30(aiMatrix4x4.a4());
        result.m01(aiMatrix4x4.b1());
        result.m11(aiMatrix4x4.b2());
        result.m21(aiMatrix4x4.b3());
        result.m31(aiMatrix4x4.b4());
        result.m02(aiMatrix4x4.c1());
        result.m12(aiMatrix4x4.c2());
        result.m22(aiMatrix4x4.c3());
        result.m32(aiMatrix4x4.c4());
        result.m03(aiMatrix4x4.d1());
        result.m13(aiMatrix4x4.d2());
        result.m23(aiMatrix4x4.d3());
        result.m33(aiMatrix4x4.d4());

        return result;
	}
}
