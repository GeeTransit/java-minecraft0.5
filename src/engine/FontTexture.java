/*
George Zhang
Font texture subclass.
*/

package geetransit.minecraft05.engine;

import java.util.*;
import java.nio.charset.Charset;

public class FontTexture extends Texture {
	public static final float ZPOS = 0f;
	public static final int VERTICES_PER_QUAD = 4;

	private final int cols;
	private final int rows;

	public FontTexture(String fileName, int cols, int rows) {
		super(fileName);
		this.cols = cols;
		this.rows = rows;
	}

	public int getCols() { return this.cols; }
	public int getRows() { return this.rows; }

	public Mesh buildMesh(String text) {
		byte[] charArray = text.getBytes(Charset.forName("ISO-8859-1"));

		List<Float> posList = new ArrayList<>();
		List<Float> coordList = new ArrayList<>();
		List<Integer> indexList = new ArrayList<>();

		int fontCols = this.getCols();
		int fontRows = this.getRows();
		float charWidth = (float) this.getWidth() / fontCols;
		float charLength = (float) this.getLength() / fontRows;

		int currentCol = 0;
		int currentRow = 0;
		int currentIndex = 0;
		for (int i = 0; i < charArray.length; i++) {
			byte currentChar = charArray[i];
			if (currentChar == '\n') {
				currentRow++;
				currentCol = 0;
				continue;
			}

			// Build a character tile composed by two triangles
			// 0 2
			// 1 3
			int fontCol = currentChar % fontCols;
			int fontRow = currentChar / fontCols;

			// Left Top vertex
			posList.add((currentCol + 0)*charWidth);  // x
			posList.add((currentRow + 0)*charLength);  // y
			posList.add(ZPOS);  // z
			coordList.add((float) (fontCol + 0) / fontCols);
			coordList.add((float) (fontRow + 0) / fontRows);

			// Left Bottom vertex
			posList.add((currentCol + 0)*charWidth);  // x
			posList.add((currentRow + 1)*charLength);  // y
			posList.add(ZPOS);  // z
			coordList.add((float) (fontCol + 0) / fontCols);
			coordList.add((float) (fontRow + 1) / fontRows);

			// Right Top vertex
			posList.add((currentCol + 1)*charWidth);  // x
			posList.add((currentRow + 0)*charLength);  // y
			posList.add(ZPOS);  // z
			coordList.add((float) (fontCol + 1) / fontCols);
			coordList.add((float) (fontRow + 0) / fontRows);

			// Right Bottom vertex
			posList.add((currentCol + 1)*charWidth);  // x
			posList.add((currentRow + 1)*charLength);  // y
			posList.add(ZPOS);  // z
			coordList.add((float) (fontCol + 1) / fontCols);
			coordList.add((float) (fontRow + 1) / fontRows);

			// Add indices for triangles (counter-clockwise)
			// 0   0 2
			// 1 3   3
			indexList.add(currentIndex*VERTICES_PER_QUAD + 0);
			indexList.add(currentIndex*VERTICES_PER_QUAD + 1);
			indexList.add(currentIndex*VERTICES_PER_QUAD + 3);
			indexList.add(currentIndex*VERTICES_PER_QUAD + 0);
			indexList.add(currentIndex*VERTICES_PER_QUAD + 3);
			indexList.add(currentIndex*VERTICES_PER_QUAD + 2);

			currentCol++;
			currentIndex++;
		}

		float[] posArray = Utils.floatListToArray(posList);
		float[] coordArray = Utils.floatListToArray(coordList);
		int[] indexArray = Utils.intListToArray(indexList);
		return new Mesh(posArray, indexArray, coordArray).setTexture(this);
	}
}
