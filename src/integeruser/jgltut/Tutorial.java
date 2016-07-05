package integeruser.jgltut;

import integeruser.jgltut.commons.LightBlock;
import integeruser.jgltut.commons.MaterialBlock;
import integeruser.jgltut.commons.ProjectionBlock;
import integeruser.jgltut.commons.UnprojectionBlock;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.Platform;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;


/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 */
public abstract class Tutorial {
    protected long window;

    protected GLFWKeyCallback keyCallback;
    protected GLFWMouseButtonCallback mouseCallback;
    protected GLFWCursorPosCallback mousePosCallback;
    protected GLFWScrollCallback mouseScrollCallback;
    protected DoubleBuffer mouseBuffer1 = BufferUtils.createDoubleBuffer(1);
    protected DoubleBuffer mouseBuffer2 = BufferUtils.createDoubleBuffer(1);

    // Measured in seconds
    protected float elapsedTime;
    protected float lastFrameDuration;

    private double lastFrameTimestamp;

    protected FloatBuffer vec4Buffer = BufferUtils.createFloatBuffer(4);
    protected FloatBuffer mat3Buffer = BufferUtils.createFloatBuffer(9);
    protected FloatBuffer mat4Buffer = BufferUtils.createFloatBuffer(16);
    protected ByteBuffer projectionBlockBuffer = BufferUtils.createByteBuffer(ProjectionBlock.SIZE_IN_BYTES);
    protected ByteBuffer unprojectionBlockBuffer = BufferUtils.createByteBuffer(UnprojectionBlock.SIZE_IN_BYTES);
    protected ByteBuffer lightBlockBuffer = BufferUtils.createByteBuffer(LightBlock.SIZE_IN_BYTES);
    protected ByteBuffer materialBlockBuffer = BufferUtils.createByteBuffer(MaterialBlock.SIZE_IN_BYTES);

    ////////////////////////////////

    public final void start(int width, int height) {
        try {
            initWindow(width, height);
            printInfo();

            loop();

            glfwFreeCallbacks(window);
            glfwDestroyWindow(window);
        } finally {
            glfwTerminate();
            glfwSetErrorCallback(null).free();
        }
    }


    private void initWindow(int width, int height) {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        if (Platform.get() == Platform.MACOSX) {
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        }

        window = glfwCreateWindow(width, height, "Hello World!", NULL, NULL);
        if (window == NULL) throw new RuntimeException("Failed to create the GLFW window");

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) glfwSetWindowShouldClose(window, true);
        });

        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(window, (vidmode.width() - width) / 2, (vidmode.height() - height) / 2);

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);

        GL.createCapabilities();
        glfwShowWindow(window);
    }

    private void printInfo() {
        System.out.println();
        System.out.println("-----------------------------------------------------------");

        System.out.format("%-18s%s\n", "Running:", getClass().getName());
        System.out.format("%-18s%s\n", "OpenGL version:", glGetString(GL_VERSION));

        if (!GL.getCapabilities().OpenGL33) {
            System.out.println("You must have at least OpenGL 3.3 to run this tutorial.");
        }
    }

    private void loop() {
        long startTime = System.nanoTime();
        while (!glfwWindowShouldClose(window)) {
            elapsedTime = (float) ((System.nanoTime() - startTime) / 1000000000.0);

            double now = System.nanoTime();
            lastFrameDuration = (float) ((now - lastFrameTimestamp) / 1000000000.0);
            lastFrameTimestamp = now;

            update();
            display();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    ////////////////////////////////

    protected abstract void init();

    protected abstract void display();

    protected abstract void reshape(int w, int h);

    protected abstract void update();

    ////////////////////////////////

    protected final boolean isKeyPressed(int key) {
        return glfwGetKey(window, key) == 1;
    }

    protected final boolean isMouseButtonPressed(int key) {
        return glfwGetMouseButton(window, key) == 1;
    }
}