package de.regatta_hd.aquarius.impl;

import de.regatta_hd.aquarius.RegattaDAO;
import de.regatta_hd.aquarius.model.Regatta;
import java.util.EventObject;

class ActiveRegattaChangedEventImpl extends EventObject
    implements RegattaDAO.ActiveRegattaChangedEvent {

  private static final long serialVersionUID = 539273824886395874L;
  private final transient Regatta regatta;

  ActiveRegattaChangedEventImpl(RegattaDAO source, Regatta regatta) {
    super(source);
    this.regatta = regatta;
  }

  @Override
  public Regatta getActiveRegatta() {
    return this.regatta;
  }
}
