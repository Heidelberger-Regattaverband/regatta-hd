package de.regatta_hd.aquarius.model;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * This entity represents a registration of a boot to a race.
 */
@Entity
@Table(schema = "dbo", name = "Entry")
// lombok
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class Registration {

	@Id
	@Column(name = "Entry_ID")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Entry_OwnerClub_ID_FK", nullable = false)
	@ToString.Include(rank = 9)
	private Club club;

	@OneToMany(targetEntity = HeatRegistration.class, mappedBy = "registration")
	private List<HeatRegistration> heatEntries;

	@OneToMany(targetEntity = Crew.class, mappedBy = "registration")
	@OrderBy("pos")
	private Set<Crew> crews;

	@Column(name = "Entry_Bib")
	@ToString.Include(rank = 10)
	private short bib;

	@Column(name = "Entry_BoatNumber")
	@ToString.Include(rank = 7)
	private Short boatNumber;

	@Column(name = "Entry_CancelValue")
	private byte cancelValue;

	@Column(name = "Entry_Comment", length = 50)
	private String comment;

	@Column(name = "Entry_ExternID")
	private Integer externId;

	@Column(name = "Entry_GroupValue")
	private Short groupValue;

	@Column(name = "Entry_IsLate")
	private boolean late;

	@OneToMany(targetEntity = RegistrationLabel.class, mappedBy = "registration")
	private Set<RegistrationLabel> labels;

	@Column(name = "Entry_Note", length = 128)
	private String note;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Entry_Event_ID_FK", nullable = false)
	private Regatta regatta;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Entry_ManualLabel_ID_FK")
	@ToString.Include(rank = 8)
	private Label label;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Entry_Race_ID_FK", nullable = false)
	private Race race;

	public List<Crew> getFinalCrews() {
		return getCrews().stream().filter(crew -> {
			boolean finalCrew = crew.getRoundFrom() <= Result.FINAL && Result.FINAL <= crew.getRoundTo() ;
			return finalCrew;
		}).collect(Collectors.toList());
	}
}