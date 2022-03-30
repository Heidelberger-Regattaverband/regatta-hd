package de.regatta_hd.aquarius.impl;

import java.util.EventObject;

import de.regatta_hd.aquarius.AquariusDB;
import de.regatta_hd.commons.db.DBConnection;

class AquariusDBStateChangedEventImpl extends EventObject implements DBConnection.StateChangedEvent {

	private static final long serialVersionUID = 539273824886395874L;

	AquariusDBStateChangedEventImpl(AquariusDB source) {
		super(source);
	}

	@Override
	public AquariusDB getDBConnection() {
		return (AquariusDB) super.source;
	}
}
