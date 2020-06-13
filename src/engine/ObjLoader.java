/*
ahbejarano
.obj mesh loader class.
*/

package geetransit.minecraft05.engine;

import java.util.*;
import java.util.stream.*;
import org.joml.Vector3f;
import org.joml.Vector2f;

public class ObjLoader {
	public static Mesh loadMesh(String file) throws Exception {
		Stream<String> lines = Utils.loadLinesStream(file.substring(1));
		
		List<Vector3f> vertices = new ArrayList<>();
		List<Vector2f> textures = new ArrayList<>();
		List<Vector3f> normals = new ArrayList<>();
		List<Face> faces = new ArrayList<>();
		
		lines.forEach(line -> {
			String[] tokens = line.split("\\s+");
			switch (tokens[0]) {
			case "v":
				// Geometric vertex
				vertices.add(new Vector3f(
					Float.parseFloat(tokens[1]),
					Float.parseFloat(tokens[2]),
					Float.parseFloat(tokens[3])
				));
				break;
			case "vt":
				// Texture coordinate
				textures.add(new Vector2f(
					Float.parseFloat(tokens[1]),
					Float.parseFloat(tokens[2])
				));
				break;
			case "vn":
				// Vertex normal
				normals.add(new Vector3f(
					Float.parseFloat(tokens[1]),
					Float.parseFloat(tokens[2]),
					Float.parseFloat(tokens[3])
				));
				break;
			case "f":
				Face face = new Face(tokens[1], tokens[2], tokens[3]);
				faces.add(face);
				break;
			default:
				// Ignore other lines
				break;
			}
		});
		return reorderLists(vertices, textures, normals, faces);
	}
	
	private static Mesh reorderLists(
		List<Vector3f> vertexList,
		List<Vector2f> coordList,
		List<Vector3f> normalList,
		List<Face> faceList
	) {
		List<Integer> posList = new ArrayList<>();
		// Create position array in the order it has been declared
		float[] posArray = new float[vertexList.size() * 3];
		for (int i = 0; i < vertexList.size(); i++) {
			Vector3f pos = vertexList.get(i);
			posArray[i*3 + 0] = pos.x;
			posArray[i*3 + 1] = pos.y;
			posArray[i*3 + 2] = pos.z;
		}
		float[] coordArray = new float[vertexList.size() * 2];
		float[] normalArray = new float[vertexList.size() * 3];

		for (Face face : faceList)
			for (IndexGroup group : face.groups)
				processFaceVertex(
					group, coordList, normalList,
					posList, coordArray, normalArray
				);
		
		// int[] indicesArray = new int[indices.size()];
		int[] indicesArray = posList.stream().mapToInt((Integer i) -> i).toArray();
		return new Mesh(posArray, indicesArray, coordArray, normalArray);
	}

	private static void processFaceVertex(
		IndexGroup group,
		List<Vector2f> coordList,
		List<Vector3f> normalList,
		List<Integer> posList,
		float[] coordArray,
		float[] normalArray
	) {
		// Set pos for vertex coordinates
		int pos = group.pos;
		posList.add(pos);

		// Reorder texture coordinates
		if (group.coord != IndexGroup.NO_VALUE) {
			Vector2f coord = coordList.get(group.coord);
			coordArray[pos*2 + 0] = coord.x;
			coordArray[pos*2 + 1] = 1 - coord.y;
		}
		if (group.normal != IndexGroup.NO_VALUE) {
			// Reorder normal vectors
			Vector3f normal = normalList.get(group.normal);
			normalArray[pos*3 + 0] = normal.x;
			normalArray[pos*3 + 1] = normal.y;
			normalArray[pos*3 + 2] = normal.z;
		}
	}
	
	protected static class IndexGroup {
		public static final int NO_VALUE = -1;
		public int pos;
		public int coord;
		public int normal;
		
		public IndexGroup() {
			this.pos = NO_VALUE;
			this.coord = NO_VALUE;
			this.normal = NO_VALUE;
		}
	}
	
	protected static class Face {
		// List of pos groups for a face triangle (3 vertices per face).
		public final IndexGroup[] groups;

		public Face(String v1, String v2, String v3) {
			this.groups = new IndexGroup[3];
			// Parse the lines
			this.groups[0] = parseLine(v1);
			this.groups[1] = parseLine(v2);
			this.groups[2] = parseLine(v3);
		}

		private IndexGroup parseLine(String line) {
			IndexGroup group = new IndexGroup();

			String[] tokens = line.split("/");
			int length = tokens.length;
			
			group.pos = Integer.parseInt(tokens[0]) - 1;
			if (length <= 1)
				return group;
			
			// It can be empty if the obj does not define text coords
			if (tokens[1].length() != 0)
				group.coord = Integer.parseInt(tokens[1]) - 1;
			if (length <= 2)
				return group;
			
			group.normal = Integer.parseInt(tokens[2]) - 1;
			return group;
		}
	}
}