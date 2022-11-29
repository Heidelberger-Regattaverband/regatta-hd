package de.regatta_hd.aquarius;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;

import de.regatta_hd.aquarius.model.HeatRegistration;
import de.regatta_hd.aquarius.model.Registration;
import de.regatta_hd.aquarius.model.Result;
import de.regatta_hd.aquarius.util.ModelUtils;

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
	private HeatRegistration srcHeatRegistration;
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
		return ModelUtils.getBoatLabel(getRegistration());
	}

	public Short getDivisionNumber() {
		return this.srcHeatRegistration != null ? Short.valueOf(this.srcHeatRegistration.getHeat().getDivisionNumber())
				: null;
	}

	public Byte getHeatRank() {
		Result result = this.srcHeatRegistration != null ? this.srcHeatRegistration.getFinalResult() : null;
		return result != null ? Byte.valueOf(result.getRank()) : null;
	}

	public String getResult() {
		Result result = this.srcHeatRegistration != null ? this.srcHeatRegistration.getFinalResult() : null;
		return result != null ? result.getDisplayValue() : null;
	}

	public Registration getSrcRegistration() {
		if (this.srcHeatRegistration != null) {
			return this.srcHeatRegistration.getRegistration();
		}
		return this.srcRegistration;
	}
}
