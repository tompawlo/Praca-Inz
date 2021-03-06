package optimumPath.JSON;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.swing.JOptionPane;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import optimumPath.algorithms.Node;

public class JsonWriteRead {

	private int sizeZfromJSON;
	private int sizeYfromJSON;
	private int sizeXfromJSON;

	public int[][][] generateMap(int[][][] map, int sizeZ, int sizeY, int sizeX) {
		Random generator = new Random();
		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					if (generator.nextInt(2) == 0) {
						map[z][y][x] = 0;
					} else {
						map[z][y][x] = 1;
					}
				}
			}
		}
		return map;
	}

	public void printMap(int[][][] map, int sizeZ, int sizeY, int sizeX) {

		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					System.out.print(Integer.toString(map[z][y][x]));
				}
				System.out.print("\n");
			}
			System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++");
			System.out.print("\n");
		}

	}
	
	public String nodeToString(Node node) {
		String nodeString;
		nodeString = "( " + Integer.toString(node.getX()) + " " +
							Integer.toString(node.getY()) + " " +
							Integer.toString(node.getZ()) + " )";
		return nodeString;
	}
	
	public void writePathToJSON(String pathFile, ArrayList<Node> path) {
		
		HashMap<Object, Object>  pathDetail = new HashMap<Object, Object>();
		
		pathDetail.put("type", "path");
		pathDetail.put("path_length", path.size());
		for(int i = 0; i < path.size(); i++) {
			pathDetail.put(i, nodeToString(path.get(i)));
		}
		
		try (FileWriter file = new FileWriter(pathFile)) {
			JSONObject JSONPath = new JSONObject(pathDetail);
			file.write(JSONPath.toString());
			file.flush();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean writeMapToJSON(String path,int outputMap[][][], int sizeZ, int sizeY, int sizeX) {

		// zapisywanie
		String temp = "";

		for (int z = 0; z < sizeZ; z++) {

			for (int y = 0; y < sizeY; y++) {
				temp += Integer.toString(outputMap[z][y][0]);
				for (int x = 1; x < sizeX; x++) {
					temp += " " + Integer.toString(outputMap[z][y][x]);
				}
				if (y == sizeY - 1 && z == sizeZ - 1) {

				} else if(y == sizeY - 1) {
					temp += "\r\n";
				}
				else {
					temp += "\n";
				}

			}
		}
		
		

		
		HashMap<String, String>  mapDetail = new HashMap<String, String>();
		mapDetail.put("type", "map");
		mapDetail.put("sizeZ", Integer.toString(sizeZ));
		mapDetail.put("sizeY", Integer.toString(sizeY));
		mapDetail.put("sizeX", Integer.toString(sizeX));
		mapDetail.put("map", temp);
		
		JSONObject JSONmap = new JSONObject(mapDetail);

		try (FileWriter file = new FileWriter(path)) {

			file.write(JSONmap.toString());
			file.flush();

		} catch (IOException e) {

			e.printStackTrace();
			return false;
		}

		return true;
	}
	
	public void errorBox(String errorMessage, String errorTitle) {
		JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);
	}

	public int[][][] loadMapFromJSON(String path) {
		// Wczytywanie
		JSONParser parser = new JSONParser();

		sizeZfromJSON = 10;
		sizeYfromJSON = 10;
		sizeXfromJSON = 10;

		int[][][] inputMap = new int[sizeZfromJSON][sizeYfromJSON][sizeXfromJSON];

		try {
			Object obj = parser.parse(new FileReader(path));
			JSONObject jsonObject = (JSONObject) obj;

			String type = jsonObject.get("type").toString();
			System.out.println(type);
			if (!type.equals("map")) {
				throw new Exception("Wczytywany plik nie jest map�");
			}
			
			sizeZfromJSON = Integer.parseInt((String) jsonObject.get("sizeZ"));
			sizeYfromJSON = Integer.parseInt((String) jsonObject.get("sizeY"));
			sizeXfromJSON = Integer.parseInt((String) jsonObject.get("sizeX"));

			String mapFromJSON = (String) jsonObject.get("map");

			// System.out.println("sizeZfromJSON: " + sizeZfromJSON);
			// System.out.println("sizeYfromJSON: " + sizeYfromJSON);
			// System.out.println("sizeXfromJSON: " + sizeXfromJSON);

			// tworzymy nowy obiekt mamy dostarej referencji
			inputMap = new int[sizeZfromJSON][sizeYfromJSON][sizeXfromJSON];

			// wiersze i wiersz iterowany
			String[] layers;
			String[] rows;
			String[] row;
			layers = mapFromJSON.split("\r\n");
			rows = mapFromJSON.split("\n");
			for (int z = 0; z < sizeZfromJSON; z++) {
				rows = layers[z].split("\n");
				for (int y = 0; y < sizeYfromJSON; y++) {
					row = rows[y].split(" ");
					for (int x = 0; x < sizeXfromJSON; x++) {
						inputMap[z][y][x] = Integer.parseInt((String) row[x]);

					}

				}

			}

			// for(int z=0;z<sizeZfromJSON;z++) {
			// for(int y=0;y<sizeYfromJSON;y++) {
			// for(int x=0;x<sizeXfromJSON;x++) {
			// System.out.print(map[z][y][x]);
			// }
			// //System.out.println();
			// }
			// }

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			errorBox(e.getLocalizedMessage(), "Error: file not found");
		} catch (IOException e) {
			e.printStackTrace();
			errorBox("Wystapi� problem z wczytaniem pliku", "IO Error");
		} catch (Exception e) {
			e.printStackTrace();
			errorBox(e.getMessage(), "Error");
		}

		return inputMap;
	}
	
	public int getSizeZfromJSON() {
		return sizeZfromJSON;
	}

	public void setSizeZfromJSON(int sizeZfromJSON) {
		this.sizeZfromJSON = sizeZfromJSON;
	}

	public int getSizeYfromJSON() {
		return sizeYfromJSON;
	}

	public void setSizeYfromJSON(int sizeYfromJSON) {
		this.sizeYfromJSON = sizeYfromJSON;
	}

	public int getSizeXfromJSON() {
		return sizeXfromJSON;
	}

	public void setSizeXfromJSON(int sizeXfromJSON) {
		this.sizeXfromJSON = sizeXfromJSON;
	}

//	public static void main(String args[]) {
//
//		int sizeX = 10;
//		int sizeY = 10;
//		int sizeZ = 10;
//		int[][][] map = new int[sizeZ][sizeY][sizeX];
//		
//		
//		JsonWriteRead jsonWriteRead = new JsonWriteRead();
//		
//		map = jsonWriteRead.generateMap(map,sizeZ, sizeY, sizeX);
//		jsonWriteRead.printMap(map,sizeZ, sizeY, sizeX);
//		jsonWriteRead.writeMapToJSON("C:\\Users\\Lotnik\\eclipse-workspace\\Praca-Inz\\cmapB.JSON", map, sizeZ, sizeY, sizeX);
//		map = jsonWriteRead.loadMapFromJSON("C:\\Users\\Lotnik\\eclipse-workspace\\Praca-Inz\\cmapB.JSON");
//		
//		
//
//	}

	
};
