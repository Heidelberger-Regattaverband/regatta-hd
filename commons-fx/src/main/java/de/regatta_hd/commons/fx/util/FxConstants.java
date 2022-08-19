package de.regatta_hd.commons.fx.util;

import javafx.css.PseudoClass;

public class FxConstants {

	public static final String FX_ALIGNMENT_CENTER = "-fx-alignment: CENTER;";

	public static final String FX_ALIGNMENT_CENTER_RIGHT = "-fx-alignment: CENTER_RIGHT;";

	public static final int TABLE_BORDER_WIDTH = 5;

	public static final PseudoClass PC_HIGHLIGHTED = PseudoClass.getPseudoClass("highlighted");

	public static final PseudoClass PC_HIGHLIGHTED_SELECTED = PseudoClass.getPseudoClass("highlighted-selected");

	private FxConstants() {
		// avoid instances
	}

}
