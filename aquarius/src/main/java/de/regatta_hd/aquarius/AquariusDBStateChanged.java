package de.regatta_hd.aquarius;

import java.util.EventObject;

public class AquariusDBStateChanged extends EventObject {

	private static final long serialVersionUID = 539273824886395874L;

	public AquariusDBStateChanged(AquariusDB source) {
		super(source);
	}

	public AquariusDB getAquariusDB() {
		return (AquariusDB) super.source;
	}
}
