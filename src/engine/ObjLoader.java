/*
ahbejarano
.obj mesh loader class.
*/

package geetransit.minecraft05.engine;

import java.util.*;
import org.joml.*;

public class ObjLoader {
	public static Mesh loadMesh(String file) {
		List<Vector3f> posList = new ArrayList<>();
		List<Integer> indexList = new ArrayList<>();
		List<Vector2f> coordList = new ArrayList<>();
		List<Face> faceList = new ArrayList<>();

		Utils.loadLinesStream(file).forEach(line -> {
			String[] tokens = line.split("\\s+");
			switch (tokens[0]) {
			case "v":
				// Geometric vertex
				posList.add(new Vector3f(
					Float.parseFloat(tokens[1]),
					Float.parseFloat(tokens[2]),
					Float.parseFloat(tokens[3])
				));
				break;
			case "vt":
				// Texture coordinate
				coordList.add(new Vector2f(
					Float.parseFloat(tokens[1]),
					Float.parseFloat(tokens[2])
				));
				break;
			case "f":
				Face face = new Face(tokens[1], tokens[2], tokens[3]);
				faceList.add(face);
				break;
			default:
				// Ignore other lines
				break;
			}
		});

		// Create position array in the order it has been declared
		float[] posArray = new float[posList.size() * 3];
		float[] coordArray = new float[posList.size() * 2];
		for (int i = 0; i < posList.size(); i++) {
			Vector3f pos = posList.get(i);
			posArray[i*3 + 0] = pos.x;
			posArray[i*3 + 1] = pos.y;
			posArray[i*3 + 2] = pos.z;
		}
		for (int i = 0; i < coordArray.length; i++)
			coordArray[i] = -1;

		for (Face face : faceList) {
			for (Group group : face.groups) {
				int index = group.index - 1;

				// Set pos for vertex coordinates
				indexList.add(index);

				// Reorder texture coordinates
				if (group.coord != Group.NO_VALUE) {
					int coord = group.coord - 1;
					Vector2f coordVec = coordList.get(coord);

					float cX = coordVec.x;
					float cY = 1 - coordVec.y;
					float aX = coordArray[index*2 + 0];
					float aY = coordArray[index*2 + 1];

					if ((aX != -1 && aX != cX) || (aX != -1 && aY != cY))
						System.err.println(
							"ObjLoader texture coord already defined ["+aX+","+aY+"]: "
							+file+" f "+group+" ["+cX+","+cY+"]"
						);
					coordArray[index*2 + 0] = cX;
					coordArray[index*2 + 1] = cY;
				}
			}
		}

		int[] indexArray = Utils.intListToArray(indexList);
		return new Mesh(posArray, indexArray, coordArray);
	}

	protected static class Group {
		public static final int NO_VALUE = -1;
		public int index;
		public int coord;
		public int normal;

		public Group() {
			this.index = NO_VALUE;
			this.coord = NO_VALUE;
			this.normal = NO_VALUE;
		}

		public String toString() {
			return (
				this.index
				+"/"+(this.coord != NO_VALUE ? this.coord : "")
				+"/"+(this.normal != NO_VALUE ? this.normal : "")
			);
		}
	}

	protected static class Face {
		// List of pos groups for a face triangle (3 vertices per face).
		public final Group[] groups;

		public Face(String v1, String v2, String v3) {
			this.groups = new Group[3];
			// Parse the lines
			this.groups[0] = parseLine(v1);
			this.groups[1] = parseLine(v2);
			this.groups[2] = parseLine(v3);
		}

		private static Group parseLine(String line) {
			Group group = new Group();

			String[] tokens = line.split("/");
			int length = tokens.length;

			group.index = Integer.parseInt(tokens[0]);
			if (length <= 1)
				return group;

			// can be empty
			if (tokens[1].length() != 0)
				group.coord = Integer.parseInt(tokens[1]);
			if (length <= 2)
				return group;

			if (tokens[2].length() != 0)
				group.normal = Integer.parseInt(tokens[2]);
			if (length <= 3)
				return group;

			return group;
		}
	}
}
