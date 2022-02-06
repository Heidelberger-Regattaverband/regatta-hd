package de.regatta_hd.aquarius.impl;

import java.util.EventObject;

import de.regatta_hd.aquarius.RegattaDAO;
import de.regatta_hd.aquarius.model.Regatta;

class RegattaDAORegattaChangedEventImpl extends EventObject implements RegattaDAO.RegattaChangedEvent {

	private static final long serialVersionUID = 539273824886395874L;
	private final transient Regatta regatta;

	RegattaDAORegattaChangedEventImpl(RegattaDAO source, Regatta regatta) {
		super(source);
		this.regatta = regatta;
	}

	@Override
	public Regatta getActiveRegatta() {
		return this.regatta;
	}

}
