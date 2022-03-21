package de.regatta_hd.aquarius;

import java.util.List;
import java.util.Optional;

import de.regatta_hd.aquarius.model.Heat;
import de.regatta_hd.aquarius.model.HeatRegistration;
import de.regatta_hd.aquarius.model.Registration;
import de.regatta_hd.aquarius.model.RegistrationLabel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class ResultEntry {

	private Heat heat;

	public int getId() {
		return this.heat.getId();
	}

	public short getNumber() {
		return this.heat.getNumber();
	}

	public short getDevisionNumber() {
		return this.heat.getDevisionNumber();
	}

	public String getDevisionLabel() {
		return this.heat.getDevisionLabel();
	}

	public String getRaceNumber() {
		return this.heat.getRaceNumber();
	}

	public String getRaceLabel() {
		return this.heat.getRaceShortLabel();
	}

	public String getFirst() {
		List<HeatRegistration> result = this.heat.getEntriesSortedByRank();
		if (!result.isEmpty()) {
			return getRegistrationLabel(result.get(0).getRegistration());
		}
		return null;
	}

	public Integer getFirstPoints() {
		List<HeatRegistration> result = this.heat.getEntriesSortedByRank();
		if (!result.isEmpty()) {
			return result.get(0).getFinalResult().getPoints();
		}
		return null;
	}

	public String getSecond() {
		List<HeatRegistration> result = this.heat.getEntriesSortedByRank();
		if (result.size() > 1) {
			return getRegistrationLabel(result.get(1).getRegistration());
		}
		return null;
	}

	public Integer getSecondPoints() {
		List<HeatRegistration> result = this.heat.getEntriesSortedByRank();
		if (result.size() > 1) {
			return result.get(1).getFinalResult().getPoints();
		}
		return null;
	}

	public String getThird() {
		List<HeatRegistration> result = this.heat.getEntriesSortedByRank();
		if (result.size() > 2) {
			return getRegistrationLabel(result.get(2).getRegistration());
		}
		return null;
	}

	public Integer getThirdPoints() {
		List<HeatRegistration> result = this.heat.getEntriesSortedByRank();
		if (result.size() > 2) {
			return result.get(2).getFinalResult().getPoints();
		}
		return null;
	}

	public String getFourth() {
		List<HeatRegistration> result = this.heat.getEntriesSortedByRank();
		if (result.size() > 3) {
			return getRegistrationLabel(result.get(3).getRegistration());
		}
		return null;
	}

	public Integer getFourthPoints() {
		List<HeatRegistration> result = this.heat.getEntriesSortedByRank();
		if (result.size() > 3) {
			return result.get(3).getFinalResult().getPoints();
		}
		return null;
	}

	public String getState() {
		switch (this.heat.getState()) {
		case 5:
			return "beendet";
		case 4:
			return "offiziel";
		default:
			return "-";
		}
	}

	// static helpers

	private static String getRegistrationLabel(Registration registration) {
		Optional<RegistrationLabel> label = registration.getLabels().stream().findAny();
		return label.isPresent() ? label.get().getLabel().getLabelShort() : null;
	}
}
