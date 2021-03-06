package optimumPath.object;

import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.util.FPSAnimator;

import optimumPath.common.*;
import optimumPath.algorithms.*;

public class Map {
	
	private Raster rasterMap[][][];
	private int sizeX, sizeY, sizeZ;
	private double sizeRaster;
	
	private List<Point3d> obstacleShift;
	private List<Point3d> forbiddenShift;
	private List<Point3d> pathShift;
	
	private List<Point3d> openShift;
	private List<Point3d> closedShift;
	private Point3d actualShift;
	
	private Point3d startShift;
	private Point3d endShift;
	
	private AlgorithmPerform algProcessor;
	
	private boolean isStart, isEnd;
	private boolean isAnimation;
	private int speedAnimation = 5;
	
	private Algorithm algorithm;
	private int numberIteration = 0;
	private int numberRasterPath = 0;
	private double lengthPath = 0.0;
	
	final int MAX_ANIM_SPEED = 105;
	private volatile FPSAnimator animator;
	
	
	////////////////////////////////////
	// watek dla algorytmu
	public class AlgorithmPerform implements Runnable {
		private volatile AStar algorithmAStar;
		private volatile WavePropagation algorithmWP;
		private boolean isChebyshev, isAStar;
		private volatile boolean isFinish = false;
		
		public AlgorithmPerform(boolean isAStar, boolean isChebyshev) {
		    this.isAStar = isAStar;
		    this.isChebyshev = isChebyshev;
		}
		
		public void setAStar(AStar algorithmAStar) {
			this.algorithmAStar = algorithmAStar;
		}
		
		public void setWavePropagation(WavePropagation algorithmWP) {
			this.algorithmWP = algorithmWP;
		}
		
		@Override
		public void run() {
			if (isAStar) {
				algorithmAStar.perform(isChebyshev);
				algorithmAStar.writePathToMap(rasterMap);
			} else {
				algorithmWP.perform(isChebyshev);
				algorithmWP.writePathToMap(rasterMap);
			}
			isFinish = true;
		}

		public boolean isFinish() {
			return isFinish;
		}
	}
	
	////////////////////////////////////
	// konstruktory klasy Map

	public Map() {
		sizeX = 10;
		sizeY = 10;
		sizeZ = 1;
		sizeRaster = 1.0;
		
		obstacleShift = new ArrayList<Point3d>();
		forbiddenShift = new ArrayList<Point3d>();
		pathShift = new ArrayList<Point3d>();
		
		startShift = new Point3d();
		endShift = new Point3d();
		
		openShift = new ArrayList<Point3d>();
		closedShift = new ArrayList<Point3d>();
		actualShift = new Point3d();
		
		initMap();
	}
	
	public Map(int sizeX, int sizeY, int sizeZ, double sizeRaster) {
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;
		this.sizeRaster = sizeRaster;
		
		obstacleShift = new ArrayList<Point3d>();
		forbiddenShift = new ArrayList<Point3d>();
		pathShift = new ArrayList<Point3d>();
		startShift = new Point3d();
		endShift = new Point3d();
		
		openShift = new ArrayList<Point3d>();
		closedShift = new ArrayList<Point3d>();
		actualShift = new Point3d();
		
		
		initMap();
	}
	
	/////////////////////////////////////////
	
	public void initMap() {
		rasterMap = new Raster[sizeZ][sizeY][sizeX];
		clearMap();
	}
	
	public void clearMap() {
		
		// czyszczenie mapy
		clearArray(rasterMap, sizeX, sizeY, sizeZ);
		
		obstacleShift.clear();
		forbiddenShift.clear();
		pathShift.clear();
		
		openShift.clear();
		closedShift.clear();
		
		isStart = false;
		isEnd = false;
	}
	
	public void makeShiftList() {
		// ustawienie przesuni�� dla mapy
		animator.pause();
		obstacleShift.clear();
		forbiddenShift.clear();
		pathShift.clear();
		
		isStart = false;
		isEnd = false;
		
		for(int z = 0; z < sizeZ; z++) {
			for(int y = 0; y < sizeY; y++) {
				for(int x = 0; x < sizeX; x++) {
					makeShift(x, y, z);
				}
			}
		}
		animator.resume();
	}
	
	public int[][][] rasterMapToIntMap(){
		int intMap[][][] = new int[sizeZ][sizeY][sizeX];
		
		for(int z = 0; z < sizeZ; z++) {
			for(int y = 0; y < sizeY; y++) {
				for(int x = 0; x < sizeX; x++) {
					switch(rasterMap[z][y][x]) {
						case OBSTACLE:
							intMap[z][y][x] = 1;
							break;
						case START:
							intMap[z][y][x] = 2;
							break;
						case END:
							intMap[z][y][x] = 3;
							break;
						default:
							intMap[z][y][x] = 0;
							break;
					}
				}
			}
		}
		
		return intMap;
	}
	
	public void intMapToRasterMap(int intMap[][][], int sizeX, int sizeY, int sizeZ){
		setSize(sizeX, sizeY, sizeZ);
		initMap();
		
		for(int z = 0; z < sizeZ; z++) {
			for(int y = 0; y < sizeY; y++) {
				for(int x = 0; x < sizeX; x++) {
					switch(intMap[z][y][x]) {
						case 1:
							rasterMap[z][y][x] = Raster.OBSTACLE;
							break;
						case 2:
							rasterMap[z][y][x] = Raster.START;
							break;
						case 3:
							rasterMap[z][y][x] = Raster.END;
							break;
						default:
							rasterMap[z][y][x] = Raster.EMPTY;
							break;
					}
				}
			}
		}
		
		makeShiftList();
	}
	
	public Point3d pointFromShift(Point3d point) {
		double x = point.getX()/(double)sizeRaster;
		double y = point.getZ()/(double)sizeRaster;
		double z = point.getY()/(double)sizeRaster;
		return new Point3d(x, y, z);
	}
	
	public Point3d pointFromShift(List<Point3d> List, int index) {
		Point3d point = List.get(index);
		return pointFromShift(point);
	}
	
	synchronized public void stepAstar(ArrayList<Node> openset, ArrayList<Node> closedset, Node actualNode) {
		//isAnimation = false;
		animator.pause();
		openShift.clear();
		closedShift.clear();
		
		for (int i = 0; i < openset.size(); i++) {
			double shiftX = (double)openset.get(i).getX() * sizeRaster;
			double shiftY = (double)openset.get(i).getZ() * sizeRaster;
			double shiftZ = (double)openset.get(i).getY() * sizeRaster;
			
			openShift.add(new Point3d(shiftX, shiftY, shiftZ));
		}
		
		for (int i = 0; i < closedset.size(); i++) {
			double shiftX = (double)closedset.get(i).getX() * sizeRaster;
			double shiftY = (double)closedset.get(i).getZ() * sizeRaster;
			double shiftZ = (double)closedset.get(i).getY() * sizeRaster;
			
			closedShift.add(new Point3d(shiftX, shiftY, shiftZ));
		}
		
		actualShift = new Point3d((double)actualNode.getX(), (double)actualNode.getZ(), (double)actualNode.getY());
		animator.resume();
		//isAnimation = true;
		
		try {
	        Thread.sleep(10*(MAX_ANIM_SPEED-this.speedAnimation));
	    }
	    catch (Exception e){}
		
	}
	
	
	public void makeShift(int x, int y, int z) {
		double shiftX = (double)x * sizeRaster;
		double shiftY = (double)z * sizeRaster;
		double shiftZ = (double)y * sizeRaster;
		
		switch (rasterMap[z][y][x]) {
			case OBSTACLE:
				obstacleShift.add(new Point3d(shiftX, shiftY, shiftZ));
				break;
				
			case FORBIDDEN:
				forbiddenShift.add(new Point3d(shiftX, shiftY, shiftZ));
				break;
				
			case PATH:
				pathShift.add(new Point3d(shiftX, shiftY, shiftZ));
				break;
				
			case START:
				startShift.setPoint(shiftX, shiftY, shiftZ);
				isStart = true;
				break;
				
			case END:
				endShift.setPoint(shiftX, shiftY, shiftZ);
				isEnd = true;
				break;
				
			default:
				break;
		}
	}
	
	public void removeFromlist(List<Point3d> List, int x, int y, int z) {
		double shiftX = (double)x * sizeRaster;
		double shiftY = (double)z * sizeRaster;
		double shiftZ = (double)y * sizeRaster;
		
		for (int index = 0; index < List.size(); index++) {
			if (List.get(index).equalsPoint(shiftX, shiftY, shiftZ))
				List.remove(index);
		}
	}
	
	public boolean isPointInMap(Point3d point) {
		boolean inRangeX = point.getX() >= 0 && point.getX() < sizeX;
		boolean inRangeY = point.getY() >= 0 && point.getY() < sizeY;
		boolean inRangeZ = point.getZ() >= 0 && point.getZ() < sizeZ;
		
		if (inRangeX && inRangeY && inRangeZ)
			return true;
		return false;
	}
	
	public void clearArray(Raster[][][] inputMap, int sizeX, int sizeY, int sizeZ) {
		for(int z = 0; z < sizeZ; z++) {
			for(int y = 0; y < sizeY; y++) {
				for(int x = 0; x < sizeX; x++) {
					inputMap[z][y][x] = Raster.EMPTY;
				}
			}
		}
	}
	
	public void reshapeMap(int sizeX, int sizeY, int sizeZ) {
		int minSizeX = (this.sizeX > sizeX) ? sizeX : this.sizeX;
		int minSizeY = (this.sizeY > sizeY) ? sizeY : this.sizeY;
		int minSizeZ = (this.sizeZ > sizeZ) ? sizeZ : this.sizeZ;
		
		Raster newMap[][][] = new Raster[sizeZ][sizeY][sizeX];
		clearArray(newMap, sizeX, sizeY, sizeZ);
		
		for(int z = 0; z < minSizeZ; z++) {
			for(int y = 0; y < minSizeY; y++) {
				for(int x = 0; x < minSizeX; x++) {
					newMap[z][y][x] = rasterMap[z][y][x];
				}
			}
		}
		
		setSize(sizeX, sizeY, sizeZ);
		rasterMap = newMap;
		
		makeShiftList();
	}
	
	public void resetRaster(int x, int y, int z) {
		Raster type = rasterMap[z][y][x];
		rasterMap[z][y][x] = Raster.EMPTY;
		switch(type) {
			case OBSTACLE:
				removeFromlist(obstacleShift, x, y, z);
				break;
			case FORBIDDEN:
				removeFromlist(forbiddenShift, x, y, z);
				break;
			case PATH:
				removeFromlist(pathShift, x, y, z);
				break;
			case START:
				isStart = false;
				break;
			case END:
				isEnd = false;
				break;
			default:
				break;
		}
		
	}
	
	public void resetPath() {
		for(int i = 0; i < pathShift.size(); i++) {
			Point3d raster = this.pointFromShift(pathShift.get(i));
			rasterMap[(int)raster.getZ()][(int)raster.getY()][(int)raster.getX()] = Raster.EMPTY;
		}
		
		pathShift.clear();
	}
	
	public void resetForbidden() {
		for(int i = 0; i < forbiddenShift.size(); i++) {
			Point3d raster = this.pointFromShift(forbiddenShift.get(i));
			rasterMap[(int)raster.getZ()][(int)raster.getY()][(int)raster.getX()] = Raster.EMPTY;
		}
		
		forbiddenShift.clear();
	}
	
	
	/////////////////////////////////////////
	// WYKONANIE ALGORYTMOW
	
	public void performAStar(boolean isChebyshev, int zoneProhibited, int thick) {
		if(algProcessor != null)
			return;
		
		AStar algorithmAStar = new AStar(this);
		algorithm = algorithmAStar;
		algorithm.setZoneProhibited(zoneProhibited);
		algorithm.setThick(thick);
		
		algProcessor = new AlgorithmPerform(true, isChebyshev);
		algProcessor.setAStar(algorithmAStar);
		Thread algThread = new Thread(algProcessor);
		algThread.start();
		
	}
	
	public void performWavePropagation(boolean isChebyshev, int zoneProhibited, int thick) {
		if(algProcessor != null)
			return;
		
		WavePropagation algorithmWP = new WavePropagation(this);
		algorithm = algorithmWP;
		algorithm.setZoneProhibited(zoneProhibited);
		algorithm.setThick(thick);
		
		algProcessor = new AlgorithmPerform(false, isChebyshev);
		algProcessor.setWavePropagation(algorithmWP);
		Thread algThread = new Thread(algProcessor);
		algThread.start();
		
	}
	
	public void resultAlgorithm() {	
		numberIteration = algorithm.getNumberIteration();
		numberRasterPath = algorithm.getNumberRasterPath();
		lengthPath = algorithm.getLengthPath();
		
		makeShiftList();
		openShift.clear();
		closedShift.clear();
		isAnimation = false;
		algProcessor = null;
	}
	
	public void printMap() {
		for(int z = 0; z < sizeZ; z++) {
			for(int y = 0; y < sizeY; y++) {
				for(int x = 0; x < sizeX; x++) {
					switch(rasterMap[z][y][x]) {
					case OBSTACLE:
						System.out.print("# ");
						break;
					case FORBIDDEN:
						System.out.print("* ");
						break;
					case PATH:
						System.out.print("@ ");
						break;
					case START:
						System.out.print("1 ");
						break;
					case END:
						System.out.print("2 ");
						break;
					default:
						System.out.print("0 ");
						break;
					}
				}
				System.out.print('\n');
			}
			System.out.print('\n');
		}
		System.out.print('\n');
	}
	
	////////////////////////////////////
	// getters and setters
	
	public void setRaster(int x, int y, int z, Raster type) {
		if (type == rasterMap[z][y][x])
			return;
		if (type == Raster.EMPTY) {
			resetRaster(x, y, z);
			return;
		}
		
		if (type == Raster.START && isStart) {
			Point3d raster = this.pointFromShift(startShift);
			rasterMap[(int)raster.getZ()][(int)raster.getY()][(int)raster.getX()] = Raster.EMPTY;
		}
		if (type == Raster.END && isEnd) {
			Point3d raster = this.pointFromShift(endShift);
			rasterMap[(int)raster.getZ()][(int)raster.getY()][(int)raster.getX()] = Raster.EMPTY;
		}
		
		this.rasterMap[z][y][x] = type;
		makeShift(x, y, z);
	}
	
	public Raster getRaster(int x, int y, int z) {
		return rasterMap[z][y][x];
	}
	
	
//	public Point3d getObstacleShift(int index) {
//		return obstacleShift.get(index);
//	}
//	
//	public void setObstacleShift(int index, double x, double y, double z) {
//		this.obstacleShift.get(index).setPoint(x, y, z);
//	}
//	
//	public Point3d getPathShift(int index) {
//		return pathShift.get(index);
//	}
//	
//	public void setPathShift(int index, double x, double y, double z) {
//		this.pathShift.get(index).setPoint(x, y, z);
//	}
	
	
	
	public Point3d getStartShift() {
		return startShift;
	}

	public List<Point3d> getForbiddenShift() {
		return forbiddenShift;
	}

	public void setForbiddenShift(List<Point3d> forbiddenShift) {
		this.forbiddenShift = forbiddenShift;
	}

	public List<Point3d> getOpenShift() {
		return openShift;
	}

	public void setOpenShift(List<Point3d> openShift) {
		this.openShift = openShift;
	}

	public List<Point3d> getClosedShift() {
		return closedShift;
	}

	public void setClosedShift(List<Point3d> closedShift) {
		this.closedShift = closedShift;
	}

	public Point3d getActualShift() {
		return actualShift;
	}

	public void setActualShift(Point3d actualShift) {
		this.actualShift = actualShift;
	}

	public void setStartShift(double x, double y, double z) {
		this.startShift.setPoint(x, y, z);
	}

	public Point3d getEndShift() {
		return endShift;
	}

	public void setEndShift(double x, double y, double z) {
		this.endShift.setPoint(x, y, z);
	}
	
	public Raster[][][] getRasterMap() {
		return rasterMap;
	}

	public void setRasterMap(Raster[][][] rasterMap) {
		this.rasterMap = rasterMap;
	}

	public List<Point3d> getObstacleShift() {
		return obstacleShift;
	}

	public List<Point3d> getPathShift() {
		return pathShift;
	}

	public int getSizeX() {
		return sizeX;
	}

	public void setSizeX(int sizeX) {
		this.sizeX = sizeX;
	}

	public int getSizeY() {
		return sizeY;
	}

	public void setSizeY(int sizeY) {
		this.sizeY = sizeY;
	}

	public int getSizeZ() {
		return sizeZ;
	}

	public void setSizeZ(int sizeZ) {
		this.sizeZ = sizeZ;
	}
	
	public Point3d getSize() {
		return new Point3d(this.sizeX, this.sizeY, this.sizeZ);
	}
	
	public void setSize(int sizeX, int sizeY, int sizeZ) {
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;
	}

	public double getSizeRaster() {
		return sizeRaster;
	}

	public void setSizeRaster(double sizeRaster) {
		this.sizeRaster = sizeRaster;
	}
	
	public boolean isStart() {
		return isStart;
	}

	public void setStart(boolean isStart) {
		this.isStart = isStart;
	}

	public boolean isEnd() {
		return isEnd;
	}

	public void setEnd(boolean isEnd) {
		this.isEnd = isEnd;
	}

	public boolean isAnimation() {
		return isAnimation;
	}

	public void setAnimation(boolean isAnimation) {
		this.isAnimation = isAnimation;
	}

	public int getSpeedAnimation() {
		return speedAnimation;
	}

	public void setSpeedAnimation(int speedAnimation) {
		this.speedAnimation = speedAnimation;
	}

	public AlgorithmPerform getAlgProcessor() {
		return algProcessor;
	}

	public void setAlgProcessor(AlgorithmPerform algProcessor) {
		this.algProcessor = algProcessor;
	}

	public void setAnimator(FPSAnimator animator) {
		this.animator = animator;
	}

	public int getNumberIteration() {
		return numberIteration;
	}

	public void setNumberIteration(int numberIteration) {
		this.numberIteration = numberIteration;
	}

	public int getNumberRasterPath() {
		return numberRasterPath;
	}

	public void setNumberRasterPath(int numberRasterPath) {
		this.numberRasterPath = numberRasterPath;
	}

	public double getLengthPath() {
		return lengthPath;
	}

	public void setLengthPath(double lengthPath) {
		this.lengthPath = lengthPath;
	}

	public Algorithm getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(Algorithm algorithm) {
		this.algorithm = algorithm;
	}

	/////////////////////////////////////////////
	
}
