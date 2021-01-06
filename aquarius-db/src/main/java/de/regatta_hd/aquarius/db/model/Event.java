package de.regatta_hd.aquarius.db.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Data;
import lombok.ToString;

/**
 * Auto-generated by:
 * org.apache.openjpa.jdbc.meta.ReverseMappingTool$AnnotatedCodeGenerator
 */
@Entity
@Table(schema = "dbo", name = "Event")
@IdClass(de.regatta_hd.aquarius.db.model.EventId.class)
// lombok
@Data
@ToString(onlyExplicitlyIncluded = true)
public class Event {
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "Event_Club_ID_FK")
	private Club club;

	@OneToMany(targetEntity = de.regatta_hd.aquarius.db.model.Comp.class, mappedBy = "event", cascade = CascadeType.MERGE)
	private Set<Comp> comps = new HashSet<>();

	@OneToMany(targetEntity = de.regatta_hd.aquarius.db.model.Entry.class, mappedBy = "event", cascade = CascadeType.MERGE)
	private Set<Entry> entrys = new HashSet<>();

	@Basic
	@Column(name = "Event_BoardMemberA", length = 32)
	private String boardMemberA;

	@Basic
	@Column(name = "Event_BoardMemberB", length = 32)
	private String boardMemberB;

	@Basic
	@Column(name = "Event_DefaultDistance")
	private Integer defaultDistance;

	@Basic
	@Column(name = "Event_EndDate", columnDefinition = "datetime", nullable = false)
	private Date endDate;

	@Basic
	@Column(name = "Event_FootLogo", length = 64)
	private String footLogo;

	@Basic
	@Column(name = "Event_HeadLogo_A", length = 64)
	private String headLogoA;

	@Basic
	@Column(name = "Event_HeadLogo_B", length = 64)
	private String headLogoB;

	@Id
	@Column(name = "Event_ID", columnDefinition = "int identity")
	@ToString.Include(rank = 20)
	private int eventID;

	@Basic
	@Column(name = "Event_StartDate", columnDefinition = "datetime", nullable = false)
	private Date startDate;

	@Basic
	@Column(name = "Event_SubTitle", length = 32)
	private String subTitle;

	@Basic
	@Column(name = "Event_Title", nullable = false, length = 64)
	@ToString.Include(rank = 18)
	private String title;

	@Basic
	@Column(name = "Event_TrackDirection", length = 3)
	private String trackDirection;

	@Basic
	@Column(name = "Event_Type", length = 1)
	private String eventType;

	@Basic
	@Column(name = "Event_Url", length = 64)
	private String eventUrl;

	@Basic
	@Column(name = "Event_Venue", length = 32)
	private String eventVenue;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "Event_Venue_Nation_ID_FK")
	private Nation nation;

	@OneToMany(targetEntity = de.regatta_hd.aquarius.db.model.Offer.class, mappedBy = "event", cascade = CascadeType.MERGE)
	private Set<Offer> offers = new HashSet<>();

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "Event_DefaultRaceMode_ID_FK")
	private RaceMode raceMode;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "Event_HeadReferee_ID_FK")
	private Referee referee;

	@ManyToMany(targetEntity = de.regatta_hd.aquarius.db.model.Referee.class, cascade = CascadeType.MERGE)
	@JoinTable(schema = "dbo", name = "EventReferee", joinColumns = @JoinColumn(name = "ER_Event_ID_FK"), inverseJoinColumns = @JoinColumn(name = "ER_Referee_ID_FK"))
	private Set<Referee> referees = new HashSet<>();

	@OneToMany(targetEntity = de.regatta_hd.aquarius.db.model.ReportInfo.class, mappedBy = "event", cascade = CascadeType.MERGE)
	private Set<ReportInfo> reportInfos = new HashSet<>();

	public Event() {
	}

	public Event(int eventID) {
		this.eventID = eventID;
	}

	public Club getClub() {
		return this.club;
	}

	public void setClub(Club club) {
		this.club = club;
	}

	public Set<Comp> getComps() {
		return this.comps;
	}

	public void setComps(Set<Comp> comps) {
		this.comps = comps;
	}

	public Set<Entry> getEntrys() {
		return this.entrys;
	}

	public void setEntrys(Set<Entry> entrys) {
		this.entrys = entrys;
	}

	public int getEventID() {
		return this.eventID;
	}

	public void setEventID(int eventID) {
		this.eventID = eventID;
	}

	public Nation getNation() {
		return this.nation;
	}

	public void setNation(Nation nation) {
		this.nation = nation;
	}

	public Set<Offer> getOffers() {
		return this.offers;
	}

	public void setOffers(Set<Offer> offers) {
		this.offers = offers;
	}

	public RaceMode getRaceMode() {
		return this.raceMode;
	}

	public void setRaceMode(RaceMode raceMode) {
		this.raceMode = raceMode;
	}

	public Referee getReferee() {
		return this.referee;
	}

	public void setReferee(Referee referee) {
		this.referee = referee;
	}

	public Set<Referee> getReferees() {
		return this.referees;
	}

	public void setReferees(Set<Referee> referees) {
		this.referees = referees;
	}

	public Set<ReportInfo> getReportInfos() {
		return this.reportInfos;
	}

	public void setReportInfos(Set<ReportInfo> reportInfos) {
		this.reportInfos = reportInfos;
	}
}