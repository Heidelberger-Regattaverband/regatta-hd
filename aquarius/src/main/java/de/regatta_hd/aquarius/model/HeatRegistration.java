package de.regatta_hd.aquarius.model;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Assigns a {@link Registration registration} to a {@link Heat heat}.
 */
@Entity
@Table(schema = "dbo", name = "CompEntries")
//lombok
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class HeatRegistration {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "CE_ID")
	private int id;

	@Column(name = "CE_Lane")
	@ToString.Include(rank = 10)
	private short lane;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CE_Comp_ID_FK")
	private Heat heat;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CE_Entry_ID_FK", nullable = false)
	private Registration registration;

	@OneToMany(targetEntity = Result.class, mappedBy = "heatRegistration")
	@OrderBy("rank")
	private Set<Result> results;

	@Transient
	@Getter(value = AccessLevel.NONE)
	@Setter(value = AccessLevel.NONE)
	private Result finalResult;

	/**
	 * Returns result of final race.
	 *
	 * @return {@link Result} of final or <code>null</code> if not available
	 */
	public Result getFinalResult() {
		if (this.finalResult == null) {
			this.finalResult = getResults().stream().filter((Result::isFinalResult)).findFirst().orElseGet(() -> null);
		}
		return this.finalResult;
	}

	public String getBib() {
		Registration reg = getRegistration();
		if (reg.getBib() != null) {
			return reg.getBib().toString();
		}
		return null;
	}

	public String getBoatLabel() {
		return getRegistration().getBoatLabel();
	}

	public String getResultDisplayValue() {
		if (getFinalResult() != null) {
			return getFinalResult().getDisplayValue();
		}
		return null;
	}

	public String getResultRank() {
		Result result = getFinalResult();
		if (result != null && result.getRank() > 0) {
			return Byte.toString(getFinalResult().getRank());
		}
		return null;
	}

	public String getPoints() {
		Result result = getFinalResult();
		if (result != null && result.getPoints() != null) {
			return result.getPoints().toString();
		}
		return null;
	}
}