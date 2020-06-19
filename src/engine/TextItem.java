/*
ahbejarano
Game item wrapper class.
*/

package geetransit.minecraft05.engine;

import java.util.*;
import java.nio.charset.Charset;
import org.joml.Vector4f;

public class TextItem extends Item {
	private static final float ZPOS = 0f;
	private static final int VERTICES_PER_QUAD = 4;
	
	private String text;
	private final int fontCols;
	private final int fontRows;
	
	public TextItem(String text, String fontFile, int fontCols, int fontRows) throws Exception {
		super();
		this.text = text;
		this.fontCols = fontCols;
		this.fontRows = fontRows;
		this.mesh = this.buildMesh(new Texture(fontFile));
		
	}
	
	public String getText() { return this.text; }
	public int getFontCols() { return this.fontCols; }
	public int getFontRows() { return this.fontRows; }
	
	public Item setText(String text) {
		this.text = text;
		Vector4f color = this.mesh.getColor();
		this.mesh.cleanup(false);
		this.mesh = this.buildMesh(this.mesh.getTexture());
		this.mesh.setColor(color);
		return this;
	}
	
	private Mesh buildMesh(Texture texture) {
		byte[] charArray = this.text.getBytes(Charset.forName("ISO-8859-1"));

		List<Float> posList = new ArrayList<>();
		List<Float> coordList = new ArrayList<>();
		List<Integer> indexList = new ArrayList<>();
		
		float width = (float) texture.getWidth() / this.fontCols;
		float length = (float) texture.getLength() / this.fontRows;
		
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
			int fontCol = currentChar % this.fontCols;
			int fontRow = currentChar / this.fontCols;
			
			// Left Top vertex
			posList.add(currentCol*width); // x
			posList.add(currentRow*length); // y
			posList.add(ZPOS); // z
			coordList.add((float) fontCol / this.fontCols);
			coordList.add((float) fontRow / this.fontRows);
			indexList.add(currentIndex*VERTICES_PER_QUAD + 0);
			
			// Left Bottom vertex
			posList.add(currentCol*width); // x
			posList.add(currentRow*length + length); // y
			posList.add(ZPOS); // z
			coordList.add((float) fontCol / this.fontCols);
			coordList.add((float) (fontRow + 1) / this.fontRows);
			indexList.add(currentIndex*VERTICES_PER_QUAD + 1);
			
			// Right Bottom vertex
			posList.add(currentCol*width + width); // x
			posList.add(currentRow*length + length); // y
			posList.add(ZPOS); // z
			coordList.add((float) (fontCol + 1) / this.fontCols);
			coordList.add((float) (fontRow + 1) / this.fontRows);
			indexList.add(currentIndex*VERTICES_PER_QUAD + 2);
			
			// Right Top vertex
			posList.add(currentCol*width + width); // x
			posList.add(currentRow*length); // y
			posList.add(ZPOS); // z
			coordList.add((float) (fontCol + 1) / this.fontCols);
			coordList.add((float) fontRow / this.fontRows);
			indexList.add(currentIndex*VERTICES_PER_QUAD + 3);
			
			// Add indices for left top and bottom right vertices
			indexList.add(currentIndex*VERTICES_PER_QUAD + 0);
			indexList.add(currentIndex*VERTICES_PER_QUAD + 2);
			
			currentCol++;
			currentIndex++;
		}
		
		float[] posArray = Utils.floatListToArray(posList);
		float[] coordArray = Utils.floatListToArray(coordList);
		int[] indexArray = Utils.intListToArray(indexList);
		return new Mesh(posArray, indexArray, coordArray).setTexture(texture);
	}
}
