package de.regatta_hd.aquarius.model;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The entity contains all information about the {@link Regatta} event.
 */
@Entity
@Table(schema = "dbo", name = "Event")
// lombok
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Regatta {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Event_ID")
	@ToString.Include(rank = 20)
	@EqualsAndHashCode.Include
	private int id;

	@Column(name = "Event_Title")
	@ToString.Include(rank = 18)
	private String title;

	@Column(name = "Event_StartDate")
	private Instant startDate;

	@Column(name = "Event_EndDate")
	private Instant endDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Event_Club_ID_FK")
	private Club club;

	@OneToMany(targetEntity = Heat.class, mappedBy = "regatta")
	private Set<Heat> heats;

	@OneToMany(targetEntity = Registration.class, mappedBy = "regatta")
	private List<Registration> registrations;

	@Column(name = "Event_BoardMemberA")
	private String boardMemberA;

	@Column(name = "Event_BoardMemberB")
	private String boardMemberB;

	@Column(name = "Event_DefaultDistance")
	private Integer defaultDistance;

	@Column(name = "Event_FootLogo")
	private String footLogo;

	@Column(name = "Event_HeadLogo_A")
	private String headLogoA;

	@Column(name = "Event_HeadLogo_B")
	private String headLogoB;

	@Column(name = "Event_SubTitle")
	private String subTitle;

	@Column(name = "Event_TrackDirection")
	private String trackDirection;

	@Column(name = "Event_Type")
	private String type;

	@Column(name = "Event_Url")
	private String url;

	@Column(name = "Event_Venue")
	private String venue;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Event_Venue_Nation_ID_FK")
	private Nation nation;

	@OneToMany(targetEntity = Race.class, mappedBy = "regatta")
	@OrderBy("number")
	private Set<Race> races;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Event_DefaultRaceMode_ID_FK")
	private RaceMode raceMode;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Event_HeadReferee_ID_FK")
	private Referee referee;

	@ManyToMany(targetEntity = Referee.class)
	@JoinTable(schema = "dbo", name = "EventReferee", joinColumns = @JoinColumn(name = "ER_Event_ID_FK"), inverseJoinColumns = @JoinColumn(name = "ER_Referee_ID_FK"))
	private Set<Referee> referees;

	@OneToMany(targetEntity = ReportInfo.class, mappedBy = "regatta")
	private Set<ReportInfo> reportInfos;

	@Transient
	private boolean active;

	// JavaFX properties
	public BooleanProperty activeProperty() {
		return new SimpleBooleanProperty(this.active);
	}

}