package de.regatta_hd.aquarius.impl;

import java.util.EventObject;

import de.regatta_hd.aquarius.AquariusDB;

class AquariusDBStateChanged extends EventObject implements AquariusDB.StateChangedEvent {

	private static final long serialVersionUID = 539273824886395874L;

	AquariusDBStateChanged(AquariusDB source) {
		super(source);
	}

	public AquariusDB getAquariusDB() {
		return (AquariusDB) super.source;
	}
}
