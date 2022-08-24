package de.regatta_hd.aquarius.model;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

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
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.SecondaryTable;
import jakarta.persistence.Table;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This entity represents a registration of a boot to a race.
 */
@Entity
@Table(schema = "dbo", name = "Entry")
@SecondaryTable(name = "HRV_Entry", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id") })
// lombok
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Registration {
	private static final ResourceBundle bundle = ResourceBundle.getBundle("aquarius_messages", Locale.GERMANY);

	/**
	 * Unique identifier of this {@link Registration registration}.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Entry_ID")
	@EqualsAndHashCode.Include
	private Integer id;

	/**
	 * The {@link Regatta regatta} to which this {@link Registration registration} belongs.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Entry_Event_ID_FK", nullable = false)
	private Regatta regatta;

	/**
	 * The {@link Race race} to which this {@link Registration registration} belongs.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Entry_Race_ID_FK", nullable = false)
	private Race race;

	/**
	 * The {@link Club club} that made this {@link Registration}.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Entry_OwnerClub_ID_FK", nullable = false)
	@ToString.Include(rank = 9)
	private Club club;

	/**
	 * The {@link Crew crews} which is assigned to this {@link Registration registration}.
	 */
	@OneToMany(targetEntity = Crew.class, mappedBy = "registration")
	@OrderBy("pos")
	private Set<Crew> crews;

	@OneToMany(targetEntity = HeatRegistration.class, mappedBy = "registration")
	private List<HeatRegistration> heatEntries;

	@Column(name = "Entry_Bib")
	@ToString.Include(rank = 10)
	private Short bib;

	@Column(name = "Entry_BoatNumber")
	@ToString.Include(rank = 7)
	private Short boatNumber;

	@Column(name = "Entry_CancelValue")
	private byte cancelValue;

	@Column(name = "Entry_Comment", length = 50)
	private String comment;

	@Column(name = "Entry_ExternID")
	private Integer externalId;

	@Column(name = "Entry_GroupValue")
	private Short groupValue;

	@Column(name = "Entry_IsLate")
	private boolean late;

	@OneToMany(targetEntity = RegistrationLabel.class, mappedBy = "registration")
	private Set<RegistrationLabel> labels;

	@Column(name = "Entry_Note", length = 128)
	private String note;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Entry_ManualLabel_ID_FK")
	@ToString.Include(rank = 8)
	private Label manualLabel;

	// Second table columns

	@Column(name = "alternativeTo", table = "HRV_Entry")
	private String alternativeTo;

	/**
	 * Returns the final crews assigned to this registration, previous changes are filtered out.
	 *
	 * @return a list with final crews
	 */
	public List<Crew> getFinalCrews() {
		return getCrews().stream()
				.filter(crew -> crew.getRoundFrom() <= Result.FINAL && Result.FINAL <= crew.getRoundTo())
				.collect(Collectors.toList());
	}

	/**
	 * Indicates whether this registration is cancelled or not.
	 *
	 * @return <code>true</code> if registration is cancelled, otherwise <code>false</code>.
	 */
	public boolean isCancelled() {
		return getCancelValue() > 0;
	}

	public String getBoatLabel() {
		String boatLabel = null;
		Optional<RegistrationLabel> boatLabelOpt = getLabel((short) 0);
		boatLabel = boatLabelOpt.isPresent() ? boatLabelOpt.get().getLabel().getLabelShort()
				: getClub().getAbbreviation();
		if (getBoatNumber() != null) {
			boatLabel += " - " + bundle.getString("registration.boatLabel") + " " + getBoatNumber();
		}
		return boatLabel;
	}

	public String getClubName() {
		if (getClub() != null) {
			return getClub().getName();
		}
		return null;
	}

	public String getClubNameAbr() {
		if (getClub() != null) {
			return getClub().getAbbreviation();
		}
		return null;
	}

	public String getClubCity() {
		if (getClub() != null) {
			return getClub().getCity();
		}
		return null;
	}

	// JavaFX properties
	public ObservableBooleanValue signedOffProperty() {
		return new SimpleBooleanProperty(getCancelValue() > 0);
	}

	/**
	 * Returns the labels for the given round which are assigned to this registration.
	 *
	 * @param the round
	 * @return a stream with final labels
	 */
	Optional<RegistrationLabel> getLabel(short round) {
		return getLabels().stream()
				.filter(regLabel -> regLabel.getRoundFrom() <= round && round <= regLabel.getRoundTo()).findFirst();
	}

}