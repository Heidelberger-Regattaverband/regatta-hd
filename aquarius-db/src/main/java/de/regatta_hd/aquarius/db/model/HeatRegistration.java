package de.regatta_hd.aquarius.db.model;

import java.util.List;
import java.util.Optional;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Assigns a {@link Registration registration} to a {@link Heat heat}.
 */
@Entity
@Table(schema = "dbo", name = "CompEntries")
//lombok
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class HeatRegistration {
	@Basic
	@Column(name = "CE_Lane")
	@ToString.Include(rank = 10)
	private Short lane;

	@Id
	@Column(name = "CE_ID", columnDefinition = "int identity")
	private int id;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "CE_Comp_ID_FK")
	private Heat heat;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "CE_Entry_ID_FK", nullable = false)
	private Registration registration;

	@OneToMany(targetEntity = Result.class, mappedBy = "heatRegistration", cascade = CascadeType.MERGE)
	@OrderBy("rank")
	private List<Result> results;

	/**
	 * Returns result of final race.
	 *
	 * @return {@link Result} of final or <code>null</code> if not available
	 */
	public Result getFinalResult() {
		Optional<Result> resultOpt = getResults().stream().filter((Result::isFinalResult)).findFirst();
		if (resultOpt.isPresent()) {
			return resultOpt.get();
		}
		return null;
	}
}