package rosick.mckesson.I.tut02;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;

import rosick.GLWindow;
import rosick.jglsdk.framework.Framework;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * I. The Basics
 * Chapter 2. Playing with Colors
 * http://www.arcsynthesis.org/gltut/Basics/Tutorial%2002.html
 * @author integeruser
 */
public class VertexColor02 extends GLWindow {
	
	public static void main(String[] args) {		
		new VertexColor02().start();
	}
	
	
	private static final String BASEPATH = "/rosick/mckesson/I/tut02/data/";

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private int theProgram; 
	private int vertexBufferObject;
	private int vao;
	
	private final float vertexData[] = {													
		 0.0f,    0.5f, 0.0f, 1.0f,
		 0.5f, -0.366f, 0.0f, 1.0f,
		-0.5f, -0.366f, 0.0f, 1.0f,
		 1.0f,    0.0f, 0.0f, 1.0f,
		 0.0f,    1.0f, 0.0f, 1.0f,
		 0.0f,    0.0f, 1.0f, 1.0f,
	};
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private void initializeProgram() {			
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, 	BASEPATH + "VertexColors.vert"));
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, BASEPATH + "VertexColors.frag"));

		theProgram = Framework.createProgram(shaderList);
	}
	
	private void initializeVertexBuffer() {
		FloatBuffer tempFloatBuffer = BufferUtils.createFloatBuffer(vertexData.length);
		tempFloatBuffer.put(vertexData);
		tempFloatBuffer.flip();
		
        vertexBufferObject = glGenBuffers();	       
		glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
	    glBufferData(GL_ARRAY_BUFFER, tempFloatBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	
	@Override
	protected void init() {
		initializeProgram();
		initializeVertexBuffer(); 

		vao = glGenVertexArrays();
		glBindVertexArray(vao);
	}
	
		
	@Override
	protected void display() {	
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClear(GL_COLOR_BUFFER_BIT);

		glUseProgram(theProgram);

		glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
		
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(0, 4, GL_FLOAT, false, 0, 0);
		glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, (Float.SIZE / 8) * 4 * 3);	// 48 = 4 (the size of a float) * 4 (the number of floats in a vec4) * 3 (the number of vec4's in the position data)

		glDrawArrays(GL_TRIANGLES, 0, 3);

		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glUseProgram(0);
	}
}