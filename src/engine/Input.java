/*
George Zhang
Input class
*/

package geetransit.minecraft05.engine;

import java.util.List;
import java.util.ArrayList;

public class Input implements Inputtable {
	private List<Inputtable> inputs;

	public Input(List<Inputtable> inputs) {
		this.inputs = inputs;
	}
	public Input() { this(new ArrayList<>()); }

	public List<Inputtable> getInputs() { return this.inputs; }
	public Input addInput(Inputtable input) { this.inputs.add(input); return this; }

	@Override
	public void input(Window window) {
		for (Inputtable input : this.getInputs())
			input.input(window);
	}
}
