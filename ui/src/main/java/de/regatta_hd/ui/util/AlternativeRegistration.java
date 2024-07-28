package de.regatta_hd.ui.util;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;

import de.regatta_hd.aquarius.model.Club;
import de.regatta_hd.aquarius.model.Race;
import de.rudern.schemas.service.meldungen._2010.TMeldung;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

//lombok
@Getter
@Setter
@Builder
@AllArgsConstructor
public class AlternativeRegistration {

	private TMeldung registration;

	private Race alternativeRace;

	private Race primaryRace;

	private Club club;

	private final SimpleBooleanProperty importProperty = new SimpleBooleanProperty();

	public String getExternalId() {
		return this.registration.getId();
	}

	public BooleanProperty importProperty() {
		return this.importProperty;
	}

	// alternative race

	public String getAltRaceNumber() {
		return this.alternativeRace.getNumber();
	}

	public String getAltRaceShortLabel() {
		return this.alternativeRace.getShortLabel();
	}

	// primary race

	public String getPrimaryRaceNumber() {
		return this.primaryRace.getNumber();
	}

	public String getPrimaryRaceShortLabel() {
		return this.primaryRace.getShortLabel();
	}

	public boolean isPrimaryRaceCancelled() {
		return this.primaryRace.isCancelled();
	}

	public ObservableBooleanValue primaryRaceCancelledProperty() {
		return this.primaryRace.cancelledProperty();
	}

	public String getClubAbbreviation() {
		return getClub().getAbbreviation();
	}

}
