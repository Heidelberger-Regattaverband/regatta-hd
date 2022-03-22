package de.regatta_hd.aquarius.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.Data;
import lombok.ToString;

/**
 * The entity contains all information about the regatta event.
 */
@Entity
@Table(schema = "dbo", name = "Event")
// lombok
@Data
@ToString(onlyExplicitlyIncluded = true)
public class Regatta {

	@Id
	@Column(name = "Event_ID")
	@ToString.Include(rank = 20)
	private int id;

	@Column(name = "Event_Title", nullable = false, length = 64)
	@ToString.Include(rank = 18)
	private String title;

	@Column(name = "Event_StartDate", nullable = false)
	private Instant startDate;

	@Column(name = "Event_EndDate", nullable = false)
	private Instant endDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Event_Club_ID_FK")
	private Club club;

	@OneToMany(targetEntity = Heat.class, mappedBy = "regatta")
	private Set<Heat> heats;

	@OneToMany(targetEntity = Registration.class, mappedBy = "regatta")
	private List<Registration> registrations;

	@Column(name = "Event_BoardMemberA", length = 32)
	private String boardMemberA;

	@Column(name = "Event_BoardMemberB", length = 32)
	private String boardMemberB;

	@Column(name = "Event_DefaultDistance")
	private Integer defaultDistance;

	@Column(name = "Event_FootLogo", length = 64)
	private String footLogo;

	@Column(name = "Event_HeadLogo_A", length = 64)
	private String headLogoA;

	@Column(name = "Event_HeadLogo_B", length = 64)
	private String headLogoB;

	@Column(name = "Event_SubTitle", length = 32)
	private String subTitle;

	@Column(name = "Event_TrackDirection", length = 3)
	private String trackDirection;

	@Column(name = "Event_Type", length = 1)
	private String type;

	@Column(name = "Event_Url", length = 64)
	private String url;

	@Column(name = "Event_Venue", length = 32)
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

	public BooleanProperty activeProperty() {
		return new SimpleBooleanProperty(this.active);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Regatta other = (Regatta) obj;
		return this.id == other.id;
	}

	@Override
	public int hashCode() {
		return this.id;
	}

}