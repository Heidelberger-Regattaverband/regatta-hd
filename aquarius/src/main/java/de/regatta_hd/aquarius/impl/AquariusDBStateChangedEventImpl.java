package de.regatta_hd.aquarius.impl;

import java.util.EventObject;

import de.regatta_hd.aquarius.AquariusDB;

class AquariusDBStateChangedEventImpl extends EventObject implements AquariusDB.StateChangedEvent {

	private static final long serialVersionUID = 539273824886395874L;

	AquariusDBStateChangedEventImpl(AquariusDB source) {
		super(source);
	}

	@Override
	public AquariusDB getAquariusDB() {
		return (AquariusDB) super.source;
	}

}
