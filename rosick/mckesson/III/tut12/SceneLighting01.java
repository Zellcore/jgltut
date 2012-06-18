package rosick.mckesson.III.tut12;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.*;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import rosick.LWJGLWindow;
import rosick.jglsdk.BufferableData;
import rosick.mckesson.framework.Framework;
import rosick.mckesson.framework.MousePole;
import rosick.mckesson.framework.Timer;
import rosick.jglsdk.glm.Mat4;
import rosick.jglsdk.glm.Quaternion;
import rosick.jglsdk.glm.Vec3;
import rosick.jglsdk.glm.Vec4;
import rosick.jglsdk.glutil.MatrixStack;
import rosick.jglsdk.glutil.MousePoles.*;
import rosick.mckesson.III.tut12.LightManager.LightBlock;
import rosick.mckesson.III.tut12.LightManager.SunlightValue;
import rosick.mckesson.III.tut12.LightManager.TimerTypes;
import rosick.mckesson.III.tut12.Scene.LightingProgramTypes;
import rosick.mckesson.III.tut12.Scene.ProgramData;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * III. Illumination
 * 12. Dynamic Range
 * http://www.arcsynthesis.org/gltut/Illumination/Tutorial%2012.html
 * @author integeruser
 * 
 * W,A,S,D	- move the cameras forward/backwards and left/right, relative to the camera's current orientation.
 * 				Holding SHIFT with these keys will move in smaller increments.  
 * Q,E		- raise and lower the camera, relative to its current orientation. 
 * 				Holding SHIFT with these keys will move in smaller increments.  
 * P		- toggle pausing.
 * -,=		- rewind/jump forward time by one second (of real-time).
 * T		- toggle viewing of the current target point.
 * 1,2,3	- timer commands affect both the sun and the other lights/only the sun/only the other lights.
 * L		- switch to day-optimized lighting. Pressing SHIFT+L will switch to a night-time optimized version.
 * SPACE	- print out the current sun-based time, in 24-hour notation.
 * 
 * LEFT	  CLICKING and DRAGGING			- rotate the camera around the target point, both horizontally and vertically.
 * LEFT	  CLICKING and DRAGGING + CTRL	- rotate the camera around the target point, either horizontally or vertically.
 * LEFT	  CLICKING and DRAGGING + ALT	- change the camera's up direction.
 * WHEEL  SCROLLING						- move the camera closer to it's target point or farther away. 
 */
public class SceneLighting01 extends LWJGLWindow {
	
	public static void main(String[] args) {
		Framework.CURRENT_TUTORIAL_DATAPATH = "/rosick/mckesson/III/tut12/data/";

		new SceneLighting01().start(700, 700);
	}
	
	
		
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	@Override
	protected void init() {
		initializePrograms();

		try {
			scene = new Scene() {

				@Override
				ProgramData getProgram(LightingProgramTypes eType) {
					return programs[eType.ordinal()];
				}
			};
		} catch (Exception exception) {
			exception.printStackTrace();
			System.exit(-1);
		}	
		
		setupDaytimeLighting();

		lights.createTimer("tetra", Timer.Type.TT_LOOP, 2.5f);
		
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
		glFrontFace(GL_CW);
		
		final float depthZNear = 0.0f;
		final float depthZFar = 1.0f;

		glEnable(GL_DEPTH_TEST);
		glDepthMask(true);
		glDepthFunc(GL_LEQUAL);
		glDepthRange(depthZNear, depthZFar);
		glEnable(GL_DEPTH_CLAMP);
		
		// Setup our Uniform Buffers
		lightUniformBuffer = glGenBuffers();	       
		glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, LightBlock.SIZE, GL_DYNAMIC_DRAW);	
		
		projectionUniformBuffer = glGenBuffers();	       
		glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, ProjectionBlock.SIZE, GL_DYNAMIC_DRAW);	
		
		// Bind the static buffers.
		glBindBufferRange(GL_UNIFORM_BUFFER, lightBlockIndex, lightUniformBuffer, 
				0, LightBlock.SIZE);
		
		glBindBufferRange(GL_UNIFORM_BUFFER, projectionBlockIndex, projectionUniformBuffer, 
				0, ProjectionBlock.SIZE);

		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}
	

	@Override
	protected void update() {
		while (Mouse.next()) {
			int eventButton = Mouse.getEventButton();
									
			if (eventButton != -1) {
				if (Mouse.getEventButtonState()) {
					// Mouse down
					MousePole.forwardMouseButton(viewPole, eventButton, true, Mouse.getX(), Mouse.getY());			
				} else {
					// Mouse up
					MousePole.forwardMouseButton(viewPole, eventButton, false, Mouse.getX(), Mouse.getY());			
				}
			} else {
				// Mouse moving or mouse scrolling
				int dWheel = Mouse.getDWheel();
				
				if (dWheel != 0) {
					MousePole.forwardMouseWheel(viewPole, dWheel, dWheel, Mouse.getX(), Mouse.getY());
				}
				
				if (Mouse.isButtonDown(0) || Mouse.isButtonDown(1) || Mouse.isButtonDown(2)) {
					MousePole.forwardMouseMotion(viewPole, Mouse.getX(), Mouse.getY());			
				}
			}
		}
		
		
		float lastFrameDuration = getLastFrameDuration() * 20 / 1000.f;

		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			viewPole.charPress(Keyboard.KEY_W, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT), lastFrameDuration);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			viewPole.charPress(Keyboard.KEY_S, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT), lastFrameDuration);
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			viewPole.charPress(Keyboard.KEY_D, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT), lastFrameDuration);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			viewPole.charPress(Keyboard.KEY_A, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT), lastFrameDuration);
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
			viewPole.charPress(Keyboard.KEY_E, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT), lastFrameDuration);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
			viewPole.charPress(Keyboard.KEY_Q, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT), lastFrameDuration);
		}
		
		
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				switch (Keyboard.getEventKey()) {
				case Keyboard.KEY_P:
					lights.togglePause(timerMode);
					break;
					
				case Keyboard.KEY_MINUS:
					lights.rewindTime(timerMode, 1.0f);
					break;

				case Keyboard.KEY_EQUALS:
					lights.fastForwardTime(timerMode, 1.0f);
					break;
					
				case Keyboard.KEY_T:
					drawCameraPos = !drawCameraPos;
					break;
					
				case Keyboard.KEY_1:
					timerMode = TimerTypes.ALL;
					System.out.printf("All\n");
					break;
					
				case Keyboard.KEY_2:
					timerMode = TimerTypes.SUN;
					System.out.printf("Sun\n");
					break;

				case Keyboard.KEY_3:
					timerMode = TimerTypes.LIGHTS;
					System.out.printf("Lights\n");
					break;
					
				case Keyboard.KEY_L:
					if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
						setupNighttimeLighting();
					} else {
						setupDaytimeLighting();
					}
					break;
					
				case Keyboard.KEY_SPACE:
					float sunAlpha = lights.getSunTime();
					float sunTimeHours = sunAlpha * 24.0f + 12.0f;
					sunTimeHours = sunTimeHours > 24.0f ? sunTimeHours - 24.0f : sunTimeHours;
					int sunHours = (int) sunTimeHours;
					float sunTimeMinutes = (sunTimeHours - sunHours) * 60.0f;
					int sunMinutes = (int) sunTimeMinutes;
					System.out.printf("%02d:%02d\n", sunHours, sunMinutes);
					break;
				
				case Keyboard.KEY_ESCAPE:
					leaveMainLoop();
					break;
				}
			}
		}
	}
	

	@Override
	protected void display() {
		lights.updateTime(getElapsedTime());
		
		Vec4 bkg = lights.getBackgroundColor();

		glClearColor(bkg.x, bkg.y, bkg.z, bkg.w);
		glClearDepth(1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		MatrixStack modelMatrix = new MatrixStack();
		modelMatrix.setMatrix(viewPole.calcMatrix());

		final Mat4 worldToCamMat = modelMatrix.top();
		LightBlock lightData = lights.getLightInformation(worldToCamMat);
		
		glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, lightData.fillAndFlipBuffer(lightBlockBuffer));
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
		
		{
			modelMatrix.push();

			scene.draw(modelMatrix, materialBlockIndex, lights.getTimerValue("tetra"));
			
			modelMatrix.pop();
		}
		
		{
			modelMatrix.push();

			// Render the sun
			{
				modelMatrix.push();

				Vec3 sunlightDir = new Vec3(lights.getSunlightDirection());
				modelMatrix.translate(sunlightDir.scale(500.0f));
				modelMatrix.scale(30.0f, 30.0f, 30.0f);

				glUseProgram(unlit.theProgram);
				glUniformMatrix4(unlit.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(mat4Buffer));

				Vec4 lightColor = lights.getSunlightIntensity();
				glUniform4(unlit.objectColorUnif, lightColor.fillAndFlipBuffer(vec4Buffer));
				scene.getSphereMesh().render("flat");
				
				modelMatrix.pop();
			}

			// Render the lights
			if (drawLights) {
				for (int light = 0; light < lights.getNumberOfPointLights(); light++) {
					modelMatrix.push();

					modelMatrix.translate(lights.getWorldLightPosition(light));

					glUseProgram(unlit.theProgram);
					glUniformMatrix4(unlit.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(mat4Buffer));

					Vec4 lightColor = lights.getPointLightIntensity(light);
					glUniform4(unlit.objectColorUnif, lightColor.fillAndFlipBuffer(vec4Buffer));
					scene.getCubeMesh().render("flat");

					modelMatrix.pop();
				}
			}
			
			if (drawCameraPos) {
				modelMatrix.push();

				modelMatrix.setIdentity();
				modelMatrix.translate(0.0f, 0.0f, - viewPole.getView().radius);

				glDisable(GL_DEPTH_TEST);
				glDepthMask(false);
				glUseProgram(unlit.theProgram);
				glUniformMatrix4(unlit.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(mat4Buffer));
				glUniform4f(unlit.objectColorUnif, 0.25f, 0.25f, 0.25f, 1.0f);
				scene.getCubeMesh().render("flat");
				glDepthMask(true);
				glEnable(GL_DEPTH_TEST);
				glUniform4f(unlit.objectColorUnif, 1.0f, 1.0f, 1.0f, 1.0f);
				scene.getCubeMesh().render("flat");
				
				modelMatrix.pop();
			}
			
			modelMatrix.pop();
		}
	}
	
	
	@Override
	protected void reshape(int width, int height) {
		MatrixStack persMatrix = new MatrixStack();
		persMatrix.perspective(45.0f, (width / (float) height), zNear, zFar);
		
		ProjectionBlock projData = new ProjectionBlock();
		projData.cameraToClipMatrix = persMatrix.top();

		glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, projData.fillAndFlipBuffer(mat4Buffer));
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
		
		glViewport(0, 0, width, height);
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private final int materialBlockIndex = 0;
	private final int lightBlockIndex = 1;
	
	private int lightUniformBuffer;
	private float zNear = 1.0f;
	private float zFar = 1000.0f;
	
	private FloatBuffer vec4Buffer 			= BufferUtils.createFloatBuffer(4);
	private FloatBuffer mat4Buffer 			= BufferUtils.createFloatBuffer(16);
	private FloatBuffer lightBlockBuffer 	= BufferUtils.createFloatBuffer(40);
	
	
	private void initializePrograms() {	
		for (int progIndex = 0; progIndex < LightingProgramTypes.MAX_LIGHTING_PROGRAM_TYPES.ordinal(); progIndex++) {
			programs[progIndex] = new ProgramData();
			programs[progIndex] = loadLitProgram(shaderFilenames[progIndex].vertexShaderFilename, shaderFilenames[progIndex].fragmentShaderFilename);
		}

		unlit = loadUnlitProgram("PosTransform.vert", "UniformColor.frag");
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private ProgramData[] programs = new ProgramData[LightingProgramTypes.MAX_LIGHTING_PROGRAM_TYPES.ordinal()];
	private Shaders[] shaderFilenames = new Shaders[] {
		new Shaders("PCN.vert", "DiffuseSpecular.frag"),
		new Shaders("PCN.vert", "DiffuseOnly.frag"),
		
		new Shaders("PN.vert", "DiffuseSpecularMtl.frag"),
		new Shaders("PN.vert", "DiffuseOnlyMtl.frag")};
	private UnlitProgData unlit;
	
	
	private class Shaders {
		String vertexShaderFilename;
		String fragmentShaderFilename;
		
		Shaders(String vertexShaderFilename, String fragmentShaderFilename) {
			this.vertexShaderFilename = vertexShaderFilename;
			this.fragmentShaderFilename = fragmentShaderFilename;
		}
	}
	
	private class UnlitProgData {
		int theProgram;

		int objectColorUnif;
		int modelToCameraMatrixUnif;
	}
	
	
	private ProgramData loadLitProgram(String vertexShaderFilename, String fragmentShaderFilename) {
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, 	vertexShaderFilename));
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER,	fragmentShaderFilename));

		ProgramData data = new ProgramData();
		data.theProgram = Framework.createProgram(shaderList);
		data.modelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "modelToCameraMatrix");

		data.normalModelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "normalModelToCameraMatrix");

		int materialBlock = glGetUniformBlockIndex(data.theProgram, "Material");
		int lightBlock = glGetUniformBlockIndex(data.theProgram, "Light");
		int projectionBlock = glGetUniformBlockIndex(data.theProgram, "Projection");

		if (materialBlock != GL_INVALID_INDEX) {				// Can be optimized out.
			glUniformBlockBinding(data.theProgram, materialBlock, materialBlockIndex);
		}
		glUniformBlockBinding(data.theProgram, lightBlock, lightBlockIndex);
		glUniformBlockBinding(data.theProgram, projectionBlock, projectionBlockIndex);

		return data;
	}
	
	private UnlitProgData loadUnlitProgram(String vertexShaderFilename, String fragmentShaderFilename) {
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, 	vertexShaderFilename));
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER,	fragmentShaderFilename));

		UnlitProgData data = new UnlitProgData();
		data.theProgram = Framework.createProgram(shaderList);
		data.modelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "modelToCameraMatrix");
		data.objectColorUnif = glGetUniformLocation(data.theProgram, "objectColor");

		int projectionBlock = glGetUniformBlockIndex(data.theProgram, "Projection");
		glUniformBlockBinding(data.theProgram, projectionBlock, projectionBlockIndex);

		return data;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
	private final Vec4 skyDaylightColor = new Vec4(0.65f, 0.65f, 1.0f, 1.0f);

	private Scene scene;
	private LightManager lights = new LightManager();
	
	private TimerTypes timerMode = TimerTypes.ALL;

	private boolean drawLights = true;
	private boolean drawCameraPos;
	
	
	////////////////////////////////
	// View setup.
	private ViewData initialViewData = new ViewData(
			new Vec3(-59.5f, 44.0f, 95.0f),
			new Quaternion(0.92387953f, 0.3826834f, 0.0f, 0.0f),
			50.0f,
			0.0f);

	private ViewScale viewScale = new ViewScale(
			3.0f, 80.0f,
			4.0f, 1.0f,
			5.0f, 1.0f,
			90.0f / 250.0f);

	
	private ViewPole viewPole = new ViewPole(initialViewData, viewScale, MouseButtons.MB_LEFT_BTN);
		
	
	private void setupDaytimeLighting() {
		SunlightValue values[] = {
				new SunlightValue( 0.0f/24.0f, new Vec4(0.2f, 0.2f, 0.2f, 1.0f), 	new Vec4(0.6f, 0.6f, 0.6f, 1.0f), 	new Vec4(skyDaylightColor)),
				new SunlightValue( 4.5f/24.0f, new Vec4(0.2f, 0.2f, 0.2f, 1.0f), 	new Vec4(0.6f, 0.6f, 0.6f, 1.0f), 	new Vec4(skyDaylightColor)),
				new SunlightValue( 6.5f/24.0f, new Vec4(0.15f, 0.05f, 0.05f, 1.0f), new Vec4(0.3f, 0.1f, 0.10f, 1.0f), 	new Vec4(0.5f, 0.1f, 0.1f, 1.0f)),
				new SunlightValue( 8.0f/24.0f, new Vec4(0.0f, 0.0f, 0.0f, 1.0f), 	new Vec4(0.0f, 0.0f, 0.0f, 1.0f), 	new Vec4(0.0f, 0.0f, 0.0f, 1.0f)),
				new SunlightValue(18.0f/24.0f, new Vec4(0.0f, 0.0f, 0.0f, 1.0f), 	new Vec4(0.0f, 0.0f, 0.0f, 1.0f), 	new Vec4(0.0f, 0.0f, 0.0f, 1.0f)),
				new SunlightValue(19.5f/24.0f, new Vec4(0.15f, 0.05f, 0.05f, 1.0f), new Vec4(0.3f, 0.1f, 0.1f, 1.0f), 	new Vec4(0.5f, 0.1f, 0.1f, 1.0f)),
				new SunlightValue(20.5f/24.0f, new Vec4(0.2f, 0.2f, 0.2f, 1.0f), 	new Vec4(0.6f, 0.6f, 0.6f, 1.0f), 	new Vec4(skyDaylightColor))};

		lights.setSunlightValues(values, 7);

		lights.setPointLightIntensity(0, new Vec4(0.2f, 0.2f, 0.2f, 1.0f));
		lights.setPointLightIntensity(1, new Vec4(0.0f, 0.0f, 0.3f, 1.0f));
		lights.setPointLightIntensity(2, new Vec4(0.3f, 0.0f, 0.0f, 1.0f));
	}

	private void setupNighttimeLighting() {
		SunlightValue values[] = {
				new SunlightValue( 0.0f/24.0f, new Vec4(0.2f, 0.2f, 0.2f, 1.0f), 	new Vec4(0.6f, 0.6f, 0.6f, 1.0f), 	new Vec4(skyDaylightColor)),
				new SunlightValue( 4.5f/24.0f, new Vec4(0.2f, 0.2f, 0.2f, 1.0f), 	new Vec4(0.6f, 0.6f, 0.6f, 1.0f), 	new Vec4(skyDaylightColor)),
				new SunlightValue( 6.5f/24.0f, new Vec4(0.15f, 0.05f, 0.05f, 1.0f), new Vec4(0.3f, 0.1f, 0.10f, 1.0f), 	new Vec4(0.5f, 0.1f, 0.1f, 1.0f)),
				new SunlightValue( 8.0f/24.0f, new Vec4(0.0f, 0.0f, 0.0f, 1.0f), 	new Vec4(0.0f, 0.0f, 0.0f, 1.0f),	new Vec4(0.0f, 0.0f, 0.0f, 1.0f)),
				new SunlightValue(18.0f/24.0f, new Vec4(0.0f, 0.0f, 0.0f, 1.0f), 	new Vec4(0.0f, 0.0f, 0.0f, 1.0f), 	new Vec4(0.0f, 0.0f, 0.0f, 1.0f)),
				new SunlightValue(19.5f/24.0f, new Vec4(0.15f, 0.05f, 0.05f, 1.0f), new Vec4(0.3f, 0.1f, 0.1f, 1.0f), 	new Vec4(0.5f, 0.1f, 0.1f, 1.0f)),
				new SunlightValue(20.5f/24.0f, new Vec4(0.2f, 0.2f, 0.2f, 1.0f), 	new Vec4(0.6f, 0.6f, 0.6f, 1.0f), 	new Vec4(skyDaylightColor))};

		lights.setSunlightValues(values, 7);

		lights.setPointLightIntensity(0, new Vec4(0.6f, 0.6f, 0.6f, 1.0f));
		lights.setPointLightIntensity(1, new Vec4(0.0f, 0.0f, 0.7f, 1.0f));
		lights.setPointLightIntensity(2, new Vec4(0.7f, 0.0f, 0.0f, 1.0f));
	}


	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private final int projectionBlockIndex = 2;

	private int projectionUniformBuffer;

	
	private class ProjectionBlock extends BufferableData<FloatBuffer> {
		Mat4 cameraToClipMatrix;
		
		static final int SIZE = Mat4.SIZE;
		
		@Override
		public FloatBuffer fillBuffer(FloatBuffer buffer) {
			return cameraToClipMatrix.fillBuffer(buffer);
		}
	}
}