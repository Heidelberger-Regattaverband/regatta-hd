package de.regatta_hd.aquarius.model;

import java.util.List;

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
	private Short lane;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CE_Comp_ID_FK")
	private Heat heat;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CE_Entry_ID_FK", nullable = false)
	private Registration registration;

	@OneToMany(targetEntity = Result.class, mappedBy = "heatRegistration")
	@OrderBy("rank")
	private List<Result> results;

	/**
	 * Returns result of final race.
	 *
	 * @return {@link Result} of final or <code>null</code> if not available
	 */
	public Result getFinalResult() {
		return getResults().stream().filter((Result::isFinalResult)).findFirst().orElseGet(() -> null);
	}
}