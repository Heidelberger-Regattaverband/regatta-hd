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
	private List<HeatRegistration> result;

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
		return getRegistrationName(0);
	}

	public Integer getFirstPoints() {
		return getPoints(0);
	}

	public String getSecond() {
		return getRegistrationName(1);
	}

	public Integer getSecondPoints() {
		return getPoints(1);
	}

	public String getThird() {
		return getRegistrationName(2);
	}

	public Integer getThirdPoints() {
		return getPoints(2);
	}

	public String getFourth() {
		return getRegistrationName(3);
	}

	public Integer getFourthPoints() {
		return getPoints(3);
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

	private String getRegistrationName(final int index) {
		List<HeatRegistration> heatResult = getEntriesSortedByRank();
		if (heatResult.size() > index && heatResult.get(index).getRegistration() != null) {
			return getRegistrationLabel(heatResult.get(index).getRegistration());
		}
		return null;
	}

	private Integer getPoints(final int index) {
		List<HeatRegistration> heatResult = getEntriesSortedByRank();
		if (heatResult.size() > index && heatResult.get(index).getFinalResult() != null) {
			return heatResult.get(index).getFinalResult().getPoints();
		}
		return null;
	}

	private List<HeatRegistration> getEntriesSortedByRank() {
		if (this.result == null) {
			this.result = this.heat.getEntriesSortedByRank();
		}
		return this.result;
	}

	// static helpers

	private static String getRegistrationLabel(Registration registration) {
		Optional<RegistrationLabel> label = registration.getLabels().stream().findAny();
		return label.isPresent() ? label.get().getLabel().getLabelShort() : null;
	}
}
