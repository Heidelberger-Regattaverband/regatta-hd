package de.regatta_hd.aquarius.impl;

import java.util.EventObject;

import de.regatta_hd.commons.db.DBConnection;

class AquariusDBStateChangedEventImpl extends EventObject implements DBConnection.StateChangedEvent {

	private static final long serialVersionUID = 539273824886395874L;

	AquariusDBStateChangedEventImpl(DBConnection source) {
		super(source);
	}

	@Override
	public DBConnection getDBConnection() {
		return (DBConnection) super.source;
	}
}
