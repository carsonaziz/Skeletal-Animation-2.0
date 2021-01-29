#version 330 core

const int MAX_WEIGHTS = 4;
const int MAX_BONES = 150;

layout (location = 0) in vec3 inPos;
layout (location = 1) in vec2 inTextCoord;
layout (location = 2) in vec3 inNormal;
layout (location = 3) in vec4 inBoneWeights;
layout (location = 4) in ivec4 inBoneIDs;

out vec2 outTextCoord;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
uniform mat4 bonesMatrix[MAX_BONES];

void main() {
	vec4 initPos = vec4(0, 0, 0, 0);
	
	int count = 0;
	for(int i = 0; i < MAX_WEIGHTS; i++) {
		float weight = inBoneWeights[i];
		if(weight > 0) {
			count++;
			int boneID = inBoneIDs[i];
			vec4 tmpPos = bonesMatrix[boneID] * vec4(inPos, 1.0);
			initPos += weight * tmpPos;
		}
	}
	if(count == 0) {
		initPos = vec4(inPos, 1.0);
	}

	gl_Position =  projection * view * model * initPos;
	outTextCoord = inTextCoord;
}