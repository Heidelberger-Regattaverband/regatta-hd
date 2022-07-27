package de.regatta_hd.aquarius;

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
public class SeedingListEntry {

	private int rank;
	private boolean equalCrew;
	private Registration registration;
	private HeatRegistration srcHeatRregistration;
	private Registration srcRegistration;

	// JavaFX properties
	public ObservableBooleanValue equalCrewProperty() {
		return new SimpleBooleanProperty(isEqualCrew());
	}

	public Integer getId() {
		return this.registration.getId();
	}

	public short getBib() {
		return this.registration.getBib() != null ? this.registration.getBib().shortValue() : 0;
	}

	public String getBoat() {
		return getRegistration().getBoatLabel();
	}

	public Short getDivisionNumber() {
		return this.srcHeatRregistration != null
				? Short.valueOf(this.srcHeatRregistration.getHeat().getDivisionNumber())
				: null;
	}

	public Byte getHeatRank() {
		Result result = this.srcHeatRregistration != null ? this.srcHeatRregistration.getFinalResult() : null;
		return result != null ? Byte.valueOf(result.getRank()) : null;
	}

	public String getResult() {
		Result result = this.srcHeatRregistration != null ? this.srcHeatRregistration.getFinalResult() : null;
		return result != null ? result.getDisplayValue() : null;
	}

	public Registration getSrcRegistration() {
		if (this.srcHeatRregistration != null) {
			return this.srcHeatRregistration.getRegistration();
		}
		return this.srcRegistration;
	}
}
