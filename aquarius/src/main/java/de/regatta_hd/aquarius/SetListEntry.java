package de.regatta_hd.aquarius;

import de.regatta_hd.aquarius.model.Registration;
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

	private String boat;

	public String getBoat() {
		if (this.boat == null) {
			StringBuilder boatBuilder = new StringBuilder();
			boatBuilder.append(this.registration.getClub().getAbbreviation());
			if (this.registration.getBoatNumber() != null) {
				boatBuilder.append(" - Boot ").append(this.registration.getBoatNumber());
			}
			this.boat = boatBuilder.toString();
		}
		return this.boat;
	}
}
