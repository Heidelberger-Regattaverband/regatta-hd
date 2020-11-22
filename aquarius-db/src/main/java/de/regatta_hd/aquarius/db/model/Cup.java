package de.regatta_hd.aquarius.db.model;

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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Auto-generated by: org.apache.openjpa.jdbc.meta.ReverseMappingTool$AnnotatedCodeGenerator
 */
@Entity
@Table(schema = "dbo", name = "Cup")
@IdClass(de.regatta_hd.aquarius.db.model.CupId.class)
public class Cup {
	@Basic
	@Column(columnDefinition = "nvarchar", length = 32)
	private String caption;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "Parent_ID")
	private Cup cup;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "PointTable_ID_FK")
	private CupPointTable cupPointTable;

	@OneToMany(targetEntity = de.regatta_hd.aquarius.db.model.Cup.class, mappedBy = "cup", cascade = CascadeType.MERGE)
	private Set<Cup> cups = new HashSet<>();

	@Id
	@Column(columnDefinition = "int identity")
	private int id;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "Race_ID_FK")
	private Offer offer;

	public Cup() {
	}

	public Cup(int id) {
		this.id = id;
	}

	public String getCaption() {
		return this.caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public Cup getCup() {
		return this.cup;
	}

	public void setCup(Cup cup) {
		this.cup = cup;
	}

	public CupPointTable getCupPointTable() {
		return this.cupPointTable;
	}

	public void setCupPointTable(CupPointTable cupPointTable) {
		this.cupPointTable = cupPointTable;
	}

	public Set<Cup> getCups() {
		return this.cups;
	}

	public void setCups(Set<Cup> cups) {
		this.cups = cups;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Offer getOffer() {
		return this.offer;
	}

	public void setOffer(Offer offer) {
		this.offer = offer;
	}
}