package de.regatta_hd.aquarius.db;

import java.util.*;
import javax.persistence.*;

/**
 * Auto-generated by:
 * org.apache.openjpa.jdbc.meta.ReverseMappingTool$AnnotatedCodeGenerator
 */
@Entity
@Table(name="RaceMode")
@IdClass(de.regatta_hd.aquarius.db.RaceModeId.class)
public class RaceMode {
	@OneToMany(targetEntity=de.regatta_hd.aquarius.db.Event.class, mappedBy="raceMode", cascade=CascadeType.MERGE)
	private Set<Event> events = new HashSet<Event>();

	@OneToMany(targetEntity=de.regatta_hd.aquarius.db.Offer.class, mappedBy="raceMode", cascade=CascadeType.MERGE)
	private Set<Offer> offers = new HashSet<Offer>();

	@Id
	@Column(name="RaceMode_ID", columnDefinition="int identity")
	private int raceModeID;

	@Basic
	@Column(name="RaceMode_IsSystemMode")
	private boolean raceModeIsSystemMode;

	@Basic
	@Column(name="RaceMode_LaneCount")
	private short raceModeLaneCount;

	@OneToMany(targetEntity=de.regatta_hd.aquarius.db.RaceModeRange.class, mappedBy="raceMode", cascade=CascadeType.MERGE)
	private Set<RaceModeRange> raceModeRanges = new HashSet<RaceModeRange>();

	@Basic
	@Column(name="RaceMode_Title", nullable=false, length=32)
	private String raceModeTitle;


	public RaceMode() {
	}

	public RaceMode(int raceModeID) {
		this.raceModeID = raceModeID;
	}

	public Set<Event> getEvents() {
		return events;
	}

	public void setEvents(Set<Event> events) {
		this.events = events;
	}

	public Set<Offer> getOffers() {
		return offers;
	}

	public void setOffers(Set<Offer> offers) {
		this.offers = offers;
	}

	public int getRaceModeID() {
		return raceModeID;
	}

	public void setRaceModeID(int raceModeID) {
		this.raceModeID = raceModeID;
	}

	public boolean isRaceModeIsSystemMode() {
		return raceModeIsSystemMode;
	}

	public void setRaceModeIsSystemMode(boolean raceModeIsSystemMode) {
		this.raceModeIsSystemMode = raceModeIsSystemMode;
	}

	public short getRaceModeLaneCount() {
		return raceModeLaneCount;
	}

	public void setRaceModeLaneCount(short raceModeLaneCount) {
		this.raceModeLaneCount = raceModeLaneCount;
	}

	public Set<RaceModeRange> getRaceModeRanges() {
		return raceModeRanges;
	}

	public void setRaceModeRanges(Set<RaceModeRange> raceModeRanges) {
		this.raceModeRanges = raceModeRanges;
	}

	public String getRaceModeTitle() {
		return raceModeTitle;
	}

	public void setRaceModeTitle(String raceModeTitle) {
		this.raceModeTitle = raceModeTitle;
	}
}