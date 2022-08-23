package de.regatta_hd.ui.util;

import de.regatta_hd.aquarius.model.Club;
import de.regatta_hd.aquarius.model.Race;
import de.rudern.schemas.service.meldungen._2010.TMeldung;
import javafx.beans.value.ObservableBooleanValue;
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
