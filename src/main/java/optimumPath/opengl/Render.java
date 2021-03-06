/**
 * 
 */
package optimumPath.opengl;

import java.awt.DisplayMode;
import java.util.List;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;

import optimumPath.common.Point3d;
import optimumPath.object.Camera;
import optimumPath.object.Map;
import optimumPath.object.Material;
import optimumPath.object.MaterialsList;
import optimumPath.window.WindowMain;

/**
 * @author
 *
 */

public class Render implements GLEventListener {

	private Map renderMap;
	private MapEdition editionMap;
	private Camera camera;
	private ScreenCapture screenshot;
	private MaterialsList materials;
	
	private GLU glu;
	final private GLCanvas glcanvas;

	private int offsetLayer = 0;
	private boolean isMapCreation = false;
	private boolean isAnimation = false;
	private boolean isAStar = false;

	public static DisplayMode dm, dm_old;
	private WindowMain window;

	public Render() {
		// getting the capabilities object of GL2 profile
		final GLProfile profile = GLProfile.getMaxProgrammable(true);
		GLCapabilities capabilities = new GLCapabilities(profile);
		
		capabilities.setNumSamples(2);
		capabilities.setSampleBuffers(true);

		this.camera = new Camera(); // Kamera dla glcanvas
		this.renderMap = new Map(); // Mapa zawierj�ca informacje o przeszkodach �cie�ce itp.
		this.editionMap = new MapEdition(); //Obiekt zawieraj�cy metody do modyfikacji mapy np. odczyt wspolrzednych mapy
		this.screenshot = new ScreenCapture(); //do zapisu okna openGL do pliku PNG
		this.materials = new MaterialsList();
		
		this.glcanvas = new GLCanvas(capabilities); // canvas
		this.glu = new GLU(); // glu

		// inicjalizacja prametrow
		camera.setPointPos(new Point3d(0.0, 0.0, 5.0));

		glcanvas.addGLEventListener(this);
		glcanvas.addMouseListener(camera);
		glcanvas.addMouseMotionListener(camera);
		glcanvas.addMouseWheelListener(camera);

		glcanvas.addMouseListener(editionMap);
		glcanvas.addMouseMotionListener(editionMap);
		
		//glcanvas.addKeyListener(this);
		glcanvas.setSize(100, 100);
		
		final FPSAnimator animator = new FPSAnimator(glcanvas, 60, true);
		renderMap.setAnimator(animator);
		
		animator.start();
	}

	@Override
	public void display(GLAutoDrawable drawable) {

		final GL2 gl = drawable.getGL().getGL2();
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

		if (editionMap.isClicked() && isMapCreation && editionMap.getMouseButton() == 1) {
			editionMap.getCoordinatesFromCanvas(gl, glu);
			editionMap.modifcationMap(renderMap, offsetLayer);
		}

		drawBase(gl);
		if (isMapCreation)
			drawGrid(gl, offsetLayer + 1);
		else
			drawGrid(gl, 0);

		drawObsticles(gl);
		drawStratEnd(gl);
		drawList(gl, renderMap.getPathShift(), materials.getMatPath());
		drawList(gl, renderMap.getForbiddenShift(), materials.getMatForbidden());
		
		if (renderMap.isAnimation()) {
			if (isAStar)
				drawActual(gl);
			
			drawList(gl, renderMap.getClosedShift(), materials.getMatClosed());
			drawList(gl, renderMap.getOpenShift(), materials.getMatOpen());
			
		}
		
		if (renderMap.getAlgProcessor() != null)
			if(renderMap.getAlgProcessor().isFinish()) {
				renderMap.resultAlgorithm();
				window.setComponentsEnableAlg(true);
				window.getResults();
			}
		
		materials.setGLBackground(gl);
		//gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();

		glu.gluLookAt(camera.getPointPos().getX(), camera.getPointPos().getY(), camera.getPointPos().getZ(),
				camera.getPointCenter().getX(), camera.getPointCenter().getY(), camera.getPointCenter().getZ(),
				camera.getVectorUp().getX(), camera.getVectorUp().getY(), camera.getVectorUp().getZ());

		gl.glFlush();
		
		//screenshot mapy
		if (screenshot.isTakingScreenshot()) {
			screenshot.getSizeFromCanvas(gl);
			screenshot.takeScreenshot(gl);
		}
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		// method body
	}

	@Override
	public void init(GLAutoDrawable drawable) {

		final GL2 gl = drawable.getGL().getGL2();
		gl.glShadeModel(GL2.GL_SMOOTH);
		materials.setGLBackground(gl);
		gl.glClearDepth(1.0f);
		// przezroczystosc
		gl.glEnable(GL2.GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glDepthFunc(GL2.GL_LESS);
		//gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
		//gl.glEnable (GL2.GL_COLOR_MATERIAL);
		gl.glColorMaterial(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE);
		gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 100.0f);
		
		gl.glEnable(GL2.GL_LINE_SMOOTH);      
	    gl.glEnable(GL2.GL_POLYGON_SMOOTH);
	    gl.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_NICEST);
	    
	    gl.glShadeModel(GLLightingFunc.GL_SMOOTH); 
	    
		// definicja �wiat�a
		float light_ambient[] = { 0.0f, 0.0f, 0.0f, 1.0f };
		float light_diffuse[] = { 1.0f, 1.0f, 1.0f, 1.0f };
		float light_position[] = { 6.0f, 9.0f, 10.0f, 9.0f };

		float lmodel_ambient[] = { 0.4f, 0.4f, 0.4f, 1.0f };
		float local_view[] = { 0.0f };

		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, light_ambient, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, light_diffuse, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, light_position, 0);
		gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, lmodel_ambient, 0);
		gl.glLightModelfv(GL2.GL_LIGHT_MODEL_LOCAL_VIEWER, local_view, 0);

		gl.glEnable(GL2.GL_LIGHT0);
		gl.glEnable(GL2.GL_LIGHTING);
		gl.glLoadIdentity();
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		final GL2 gl = drawable.getGL().getGL2();
		if (height == 0)
			height = 1;

		final float h = (float) width / (float) height;
		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();

		glu.gluPerspective(45.0f, h, 1.0, 200.0);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
	}
	

	///////////////////////////////////////////////////
	// rysowanie podstawy
	public void drawBase(GL2 gl) {
		float halfSizeRaster = (float) renderMap.getSizeRaster() / 2;
		
		materials.getMatBase().setGLMaterial(gl);
		
		gl.glPushMatrix();
		gl.glBegin(GL2.GL_QUADS);
		
		gl.glVertex3f(-halfSizeRaster, -halfSizeRaster, -halfSizeRaster);
		gl.glVertex3f(-halfSizeRaster, -halfSizeRaster,
				(float) renderMap.getSizeRaster() * (float) renderMap.getSizeY() - halfSizeRaster);
		gl.glVertex3f((float) renderMap.getSizeRaster() * (float) renderMap.getSizeX() - halfSizeRaster,
				-halfSizeRaster,
				(float) renderMap.getSizeRaster() * (float) renderMap.getSizeY() - halfSizeRaster);
		gl.glVertex3f((float) renderMap.getSizeRaster() * (float) renderMap.getSizeX() - halfSizeRaster,
				-halfSizeRaster, -halfSizeRaster);

		gl.glEnd();
		gl.glPopMatrix();
	}

	////////////////////////
	// rysowanie siatki
	public void drawGrid(GL2 gl, int layer) {

		float halfSizeRaster = (float) renderMap.getSizeRaster() / 2;
		float offset = (float) layer * (float) renderMap.getSizeRaster();

		materials.getMatGrid().setGLMaterial(gl);
		
		gl.glPushMatrix();

		for (int i = 0; i < renderMap.getSizeX() + 1; i++) {
			gl.glBegin(GL2.GL_LINES);

			gl.glVertex3f(-halfSizeRaster + i * (float) renderMap.getSizeRaster(), -halfSizeRaster + 0.002f + offset,
					-halfSizeRaster);
			gl.glVertex3f(-halfSizeRaster + i * (float) renderMap.getSizeRaster(), -halfSizeRaster + 0.002f + offset,
					(float) renderMap.getSizeRaster() * (float) renderMap.getSizeY() - halfSizeRaster);

			gl.glEnd();
		}

		for (int i = 0; i < renderMap.getSizeY() + 1; i++) {
			gl.glBegin(GL2.GL_LINES);

			gl.glVertex3f(-halfSizeRaster, -halfSizeRaster + 0.002f + offset,
					-halfSizeRaster + i * (float) renderMap.getSizeRaster());
			gl.glVertex3f((float) renderMap.getSizeRaster() * (float) renderMap.getSizeX() - halfSizeRaster,
					-halfSizeRaster + 0.002f + offset, -halfSizeRaster + i * (float) renderMap.getSizeRaster());

			gl.glEnd();
		}
		gl.glPopMatrix();

	}

	////////////////////////
	// rysowanie przeszk�d
	public void drawObsticles(GL2 gl) {
		GLUT glut = new GLUT();

		materials.getMatObstacle().setGLMaterial(gl);
		for (int index = 0; index < renderMap.getObstacleShift().size(); index++) {
			materials.getMatObstacle().setGLDiffuse(gl);
			// kostka o okre�lonym materiale
			Point3d translate = renderMap.getObstacleShift().get(index);
			Point3d raster = renderMap.pointFromShift(renderMap.getObstacleShift(), index);
			if (this.isMapCreation && (int) raster.getZ() > offsetLayer)
				continue;
			if (this.isMapCreation && (int) raster.getZ() == offsetLayer)
				materials.getMatObstacleMod().setGLDiffuse(gl);
				
			gl.glPushMatrix();
			gl.glTranslatef((float) translate.getX(), (float) translate.getY(), (float) translate.getZ());

			glut.glutSolidCube((float) renderMap.getSizeRaster());
			gl.glPopMatrix();
		}
	}

	/////////////////////////////////
	// rysowanie punktu start i stop
	public void drawStratEnd(GL2 gl) {
		GLUT glut = new GLUT();

		if (renderMap.isStart()) {
			materials.getMatStart().setGLDiffuse(gl);
			Point3d translate = renderMap.getStartShift();
			Point3d raster = renderMap.pointFromShift(renderMap.getStartShift());
			if (!(this.isMapCreation && (int) raster.getZ() > offsetLayer)) {
				gl.glPushMatrix();
				gl.glTranslatef((float) translate.getX(), (float) translate.getY(), (float) translate.getZ());
				glut.glutSolidCube((float) renderMap.getSizeRaster());
				gl.glPopMatrix();
			}
		}

		if (renderMap.isEnd()) {
			materials.getMatEnd().setGLDiffuse(gl);
			Point3d translate = renderMap.getEndShift();
			Point3d raster = renderMap.pointFromShift(renderMap.getEndShift());
			if (!(this.isMapCreation && (int) raster.getZ() > offsetLayer)) {
				gl.glPushMatrix();
				gl.glTranslatef((float) translate.getX(), (float) translate.getY(), (float) translate.getZ());
				glut.glutSolidCube((float) renderMap.getSizeRaster());
				gl.glPopMatrix();
			}
		}

	}
	
	public void drawList(GL2 gl, List<Point3d> listShift, Material material) {
		GLUT glut = new GLUT();

		material.setGLDiffuse(gl);

		for (int index = 0; index < listShift.size(); index++) {
			// kostka o okre�lonym materiale
			Point3d translate = listShift.get(index);

			gl.glPushMatrix();
			gl.glTranslatef((float) translate.getX(), (float) translate.getY(), (float) translate.getZ());
			glut.glutSolidCube((float) renderMap.getSizeRaster());
			gl.glPopMatrix();
		}
	}

	/////////////////////////////////////////////
	// rysowanie aktualn� przetwarzany raster
	public void drawActual(GL2 gl) {
		GLUT glut = new GLUT();

		materials.getMatActual().setGLDiffuse(gl);

		// kostka o okre�lonym materiale
		Point3d translate = renderMap.getActualShift();

		gl.glPushMatrix();
		gl.glTranslatef((float) translate.getX(), (float) translate.getY(), (float) translate.getZ());
		glut.glutSolidCube((float) renderMap.getSizeRaster());
		gl.glPopMatrix();
		
	}

	

	////////////////////////////////////////
	// ---------- get i set --------------

	public Map getRenderMap() {
		return renderMap;
	}

	public void setRenderMap(Map renderMap) {
		this.renderMap = renderMap;
	}

	public Camera getCamera() {
		return camera;
	}

	public void setCamera(Camera camera) {
		this.camera = camera;
	}

	public GLU getGlu() {
		return glu;
	}

	public void setGlu(GLU glu) {
		this.glu = glu;
	}

	public GLCanvas getGlcanvas() {
		return glcanvas;
	}

	public int getOffsetLayer() {
		return offsetLayer;
	}

	public void setOffsetLayer(int offsetLayer) {
		this.offsetLayer = offsetLayer;
	}

	public boolean isMapCreation() {
		return isMapCreation;
	}

	public void setMapCreation(boolean isMapCreation) {
		this.isMapCreation = isMapCreation;
		this.camera.setMapCreation(isMapCreation);
	}

	public MapEdition getEditionMap() {
		return editionMap;
	}

	public void setEditionMap(MapEdition editionMap) {
		this.editionMap = editionMap;
	}

	public boolean isAnimation() {
		return isAnimation;
	}

	public void setAnimation(boolean isAnimation) {
		this.isAnimation = isAnimation;
		this.renderMap.setAnimation(isAnimation);
	}

	public boolean isAStar() {
		return isAStar;
	}

	public void setAStar(boolean isAStar) {
		this.isAStar = isAStar;
	}

	public void setWindow(WindowMain window) {
		this.window = window;
	}

	public MaterialsList getMaterials() {
		return materials;
	}

	public void setMaterials(MaterialsList materials) {
		this.materials = materials;
	}
	
	public ScreenCapture getScreenshot() {
		return screenshot;
	}

	public void setScreenshot(ScreenCapture screenshot) {
		this.screenshot = screenshot;
	}

	//////////////////////////////////////////////////////////
	/*@Override
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		
		switch( keyCode ) { 
	        case KeyEvent.VK_P:
	            renderMap.printMap();
	            break;
		}
	}*/
	
	//////////////////

}
