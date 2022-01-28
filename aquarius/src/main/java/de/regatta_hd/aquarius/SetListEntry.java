package de.regatta_hd.aquarius;

import de.regatta_hd.aquarius.model.Heat;
import de.regatta_hd.aquarius.model.HeatRegistration;
import de.regatta_hd.aquarius.model.Registration;
import de.regatta_hd.aquarius.model.Result;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class SetListEntry {

	private int rank;
	private boolean equalCrew;
	private Registration registration;
	private HeatRegistration heatRregistration;

	// JavaFX properties
	public ObservableBooleanValue equalCrewProperty() {
		return new SimpleBooleanProperty(isEqualCrew());
	}

	public short getBib() {
		return this.registration.getBib();
	}

	public String getBoat() {
		StringBuilder boatBuilder = new StringBuilder();
		boatBuilder.append(this.registration.getClub().getAbbreviation());
		if (this.registration.getBoatNumber() != null) {
			boatBuilder.append(" - Boot ").append(this.registration.getBoatNumber());
		}
		return boatBuilder.toString();
	}

	public Short getHeatNumber() {
		Heat heat = this.heatRregistration != null ? this.heatRregistration.getHeat() : null;
		return heat != null ? heat.getHeatNumber() : null;
	}

	public Byte getHeatRank() {
		Result result = this.heatRregistration != null ? this.heatRregistration.getFinalResult() : null;
		return result != null ? Byte.valueOf(result.getRank()) : null;
	}

	public String getResult() {
		Result result = this.heatRregistration != null ? this.heatRregistration.getFinalResult() : null;
		return result != null ? result.getDisplayValue() : null;
	}
}
