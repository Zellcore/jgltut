package fcagnin.gltut.tut05;

import fcagnin.gltut.LWJGLWindow;
import fcagnin.gltut.framework.Framework;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;


/**
 * Visit https://github.com/integeruser/gltut-lwjgl for project info, updates and license terms. info, updates and license terms.
 * <p/>
 * Part II. Positioning
 * Chapter 5. Objects in Depth
 * http://www.arcsynthesis.org/gltut/Positioning/Tutorial%2005.html
 *
 * @author integeruser
 */
public class OverlapNoDepth extends LWJGLWindow {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/fcagnin/gltut/tut05/data/";

        new OverlapNoDepth().start();
    }


    @Override
    protected void init() {
        initializeProgram();
        initializeVertexBuffer();
        initializeVertexArrayObjects();

        glEnable( GL_CULL_FACE );
        glCullFace( GL_BACK );
        glFrontFace( GL_CW );
    }

    @Override
    protected void display() {
        glClearColor( 0.0f, 0.0f, 0.0f, 0.0f );
        glClear( GL_COLOR_BUFFER_BIT );

        glUseProgram( theProgram );

        glBindVertexArray( vaoObject1 );
        glUniform3f( offsetUniform, 0.0f, 0.0f, 0.0f );
        glDrawElements( GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0 );

        glBindVertexArray( vaoObject2 );
        glUniform3f( offsetUniform, 0.0f, 0.0f, -1.0f );
        glDrawElements( GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0 );

        glBindVertexArray( 0 );
        glUseProgram( 0 );
    }

    @Override
    protected void reshape(int width, int height) {
        perspectiveMatrix[0] = frustumScale / (width / (float) height);
        perspectiveMatrix[5] = frustumScale;

        FloatBuffer perspectiveMatrixBuffer = BufferUtils.createFloatBuffer( perspectiveMatrix.length );
        perspectiveMatrixBuffer.put( perspectiveMatrix );
        perspectiveMatrixBuffer.flip();

        glUseProgram( theProgram );
        glUniformMatrix4( perspectiveMatrixUnif, false, perspectiveMatrixBuffer );
        glUseProgram( 0 );

        glViewport( 0, 0, width, height );
    }


    ////////////////////////////////
    private int theProgram;

    private int offsetUniform;
    private int perspectiveMatrixUnif;

    private float perspectiveMatrix[];
    private final float frustumScale = 1.0f;


    private void initializeProgram() {
        ArrayList<Integer> shaderList = new ArrayList<>();
        shaderList.add( Framework.loadShader( GL_VERTEX_SHADER, "Standard.vert" ) );
        shaderList.add( Framework.loadShader( GL_FRAGMENT_SHADER, "Standard.frag" ) );

        theProgram = Framework.createProgram( shaderList );

        offsetUniform = glGetUniformLocation( theProgram, "offset" );

        perspectiveMatrixUnif = glGetUniformLocation( theProgram, "perspectiveMatrix" );

        float zNear = 1.0f;
        float zFar = 3.0f;

        perspectiveMatrix = new float[16];
        perspectiveMatrix[0] = frustumScale;
        perspectiveMatrix[5] = frustumScale;
        perspectiveMatrix[10] = (zFar + zNear) / (zNear - zFar);
        perspectiveMatrix[11] = -1.0f;
        perspectiveMatrix[14] = (2 * zFar * zNear) / (zNear - zFar);

        FloatBuffer perspectiveMatrixBuffer = BufferUtils.createFloatBuffer( perspectiveMatrix.length );
        perspectiveMatrixBuffer.put( perspectiveMatrix );
        perspectiveMatrixBuffer.flip();

        glUseProgram( theProgram );
        glUniformMatrix4( perspectiveMatrixUnif, false, perspectiveMatrixBuffer );
        glUseProgram( 0 );
    }


    ////////////////////////////////
    private final int numberOfVertices = 36;

    private final float RIGHT_EXTENT = 0.8f;
    private final float LEFT_EXTENT = -RIGHT_EXTENT;
    private final float TOP_EXTENT = 0.20f;
    private final float MIDDLE_EXTENT = 0.0f;
    private final float BOTTOM_EXTENT = -TOP_EXTENT;
    private final float FRONT_EXTENT = -1.25f;
    private final float REAR_EXTENT = -1.75f;

    private final float vertexData[] = {
            // Object 1 positions
            LEFT_EXTENT, TOP_EXTENT, REAR_EXTENT,
            LEFT_EXTENT, MIDDLE_EXTENT, FRONT_EXTENT,
            RIGHT_EXTENT, MIDDLE_EXTENT, FRONT_EXTENT,
            RIGHT_EXTENT, TOP_EXTENT, REAR_EXTENT,

            LEFT_EXTENT, BOTTOM_EXTENT, REAR_EXTENT,
            LEFT_EXTENT, MIDDLE_EXTENT, FRONT_EXTENT,
            RIGHT_EXTENT, MIDDLE_EXTENT, FRONT_EXTENT,
            RIGHT_EXTENT, BOTTOM_EXTENT, REAR_EXTENT,

            LEFT_EXTENT, TOP_EXTENT, REAR_EXTENT,
            LEFT_EXTENT, MIDDLE_EXTENT, FRONT_EXTENT,
            LEFT_EXTENT, BOTTOM_EXTENT, REAR_EXTENT,

            RIGHT_EXTENT, TOP_EXTENT, REAR_EXTENT,
            RIGHT_EXTENT, MIDDLE_EXTENT, FRONT_EXTENT,
            RIGHT_EXTENT, BOTTOM_EXTENT, REAR_EXTENT,

            LEFT_EXTENT, BOTTOM_EXTENT, REAR_EXTENT,
            LEFT_EXTENT, TOP_EXTENT, REAR_EXTENT,
            RIGHT_EXTENT, TOP_EXTENT, REAR_EXTENT,
            RIGHT_EXTENT, BOTTOM_EXTENT, REAR_EXTENT,

            // Object 2 positions
            TOP_EXTENT, RIGHT_EXTENT, REAR_EXTENT,
            MIDDLE_EXTENT, RIGHT_EXTENT, FRONT_EXTENT,
            MIDDLE_EXTENT, LEFT_EXTENT, FRONT_EXTENT,
            TOP_EXTENT, LEFT_EXTENT, REAR_EXTENT,

            BOTTOM_EXTENT, RIGHT_EXTENT, REAR_EXTENT,
            MIDDLE_EXTENT, RIGHT_EXTENT, FRONT_EXTENT,
            MIDDLE_EXTENT, LEFT_EXTENT, FRONT_EXTENT,
            BOTTOM_EXTENT, LEFT_EXTENT, REAR_EXTENT,

            TOP_EXTENT, RIGHT_EXTENT, REAR_EXTENT,
            MIDDLE_EXTENT, RIGHT_EXTENT, FRONT_EXTENT,
            BOTTOM_EXTENT, RIGHT_EXTENT, REAR_EXTENT,

            TOP_EXTENT, LEFT_EXTENT, REAR_EXTENT,
            MIDDLE_EXTENT, LEFT_EXTENT, FRONT_EXTENT,
            BOTTOM_EXTENT, LEFT_EXTENT, REAR_EXTENT,

            BOTTOM_EXTENT, RIGHT_EXTENT, REAR_EXTENT,
            TOP_EXTENT, RIGHT_EXTENT, REAR_EXTENT,
            TOP_EXTENT, LEFT_EXTENT, REAR_EXTENT,
            BOTTOM_EXTENT, LEFT_EXTENT, REAR_EXTENT,

            // Object 1 colors
            0.75f, 0.75f, 1.0f, 1.0f,                                                    // GREEN
            0.75f, 0.75f, 1.0f, 1.0f,
            0.75f, 0.75f, 1.0f, 1.0f,
            0.75f, 0.75f, 1.0f, 1.0f,

            0.0f, 0.5f, 0.0f, 1.0f,                                                    // BLUE
            0.0f, 0.5f, 0.0f, 1.0f,
            0.0f, 0.5f, 0.0f, 1.0f,
            0.0f, 0.5f, 0.0f, 1.0f,

            1.0f, 0.0f, 0.0f, 1.0f,                                                    // RED
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,

            0.8f, 0.8f, 0.8f, 1.0f,                                                    // GREY
            0.8f, 0.8f, 0.8f, 1.0f,
            0.8f, 0.8f, 0.8f, 1.0f,

            0.5f, 0.5f, 0.0f, 1.0f,                                                    // BROWN
            0.5f, 0.5f, 0.0f, 1.0f,
            0.5f, 0.5f, 0.0f, 1.0f,
            0.5f, 0.5f, 0.0f, 1.0f,

            // Object 2 colors
            1.0f, 0.0f, 0.0f, 1.0f,                                                        // RED
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,

            0.5f, 0.5f, 0.0f, 1.0f,                                                    // BROWN
            0.5f, 0.5f, 0.0f, 1.0f,
            0.5f, 0.5f, 0.0f, 1.0f,
            0.5f, 0.5f, 0.0f, 1.0f,

            0.0f, 0.5f, 0.0f, 1.0f,                                                        // BLUE
            0.0f, 0.5f, 0.0f, 1.0f,
            0.0f, 0.5f, 0.0f, 1.0f,

            0.75f, 0.75f, 1.0f, 1.0f,                                                    // GREEN
            0.75f, 0.75f, 1.0f, 1.0f,
            0.75f, 0.75f, 1.0f, 1.0f,

            0.8f, 0.8f, 0.8f, 1.0f,                                                    // GREY
            0.8f, 0.8f, 0.8f, 1.0f,
            0.8f, 0.8f, 0.8f, 1.0f,
            0.8f, 0.8f, 0.8f, 1.0f
    };

    private final short indexData[] = {
            0, 2, 1,
            3, 2, 0,

            4, 5, 6,
            6, 7, 4,

            8, 9, 10,
            11, 13, 12,

            14, 16, 15,
            17, 16, 14
    };

    private int vertexBufferObject;
    private int indexBufferObject;

    private int vaoObject1, vaoObject2;


    private void initializeVertexBuffer() {
        FloatBuffer vertexDataBuffer = BufferUtils.createFloatBuffer( vertexData.length );
        vertexDataBuffer.put( vertexData );
        vertexDataBuffer.flip();

        vertexBufferObject = glGenBuffers();
        glBindBuffer( GL_ARRAY_BUFFER, vertexBufferObject );
        glBufferData( GL_ARRAY_BUFFER, vertexDataBuffer, GL_STATIC_DRAW );
        glBindBuffer( GL_ARRAY_BUFFER, 0 );

        ShortBuffer indexDataBuffer = BufferUtils.createShortBuffer( indexData.length );
        indexDataBuffer.put( indexData );
        indexDataBuffer.flip();

        indexBufferObject = glGenBuffers();
        glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, indexBufferObject );
        glBufferData( GL_ELEMENT_ARRAY_BUFFER, indexDataBuffer, GL_STATIC_DRAW );
        glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, 0 );
    }

    private void initializeVertexArrayObjects() {
        vaoObject1 = glGenVertexArrays();
        glBindVertexArray( vaoObject1 );

        int colorDataOffset = FLOAT_SIZE * 3 * numberOfVertices;

        glBindBuffer( GL_ARRAY_BUFFER, vertexBufferObject );
        glEnableVertexAttribArray( 0 );
        glEnableVertexAttribArray( 1 );
        glVertexAttribPointer( 0, 3, GL_FLOAT, false, 0, 0 );
        glVertexAttribPointer( 1, 4, GL_FLOAT, false, 0, colorDataOffset );
        glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, indexBufferObject );

        glBindVertexArray( 0 );

        vaoObject2 = glGenVertexArrays();
        glBindVertexArray( vaoObject2 );

        int posDataOffset = FLOAT_SIZE * 3 * (numberOfVertices / 2);
        colorDataOffset += FLOAT_SIZE * 4 * (numberOfVertices / 2);

        // Use the same buffer object previously bound to GL_ARRAY_BUFFER.
        glEnableVertexAttribArray( 0 );
        glEnableVertexAttribArray( 1 );
        glVertexAttribPointer( 0, 3, GL_FLOAT, false, 0, posDataOffset );
        glVertexAttribPointer( 1, 4, GL_FLOAT, false, 0, colorDataOffset );
        glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, indexBufferObject );

        glBindVertexArray( 0 );
    }
}