package de.regatta_hd.aquarius.db.model;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Assigns a {@link Referee referee} to a {@link Heat heat}.
 */
@Entity
@Table(schema = "dbo", name = "CompReferee")
//lombok
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class HeatReferee {
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "CompReferee_Comp_ID_FK", nullable = false)
	private Heat heat;

	@Id
	@Column(name = "CompReferee_ID", columnDefinition = "int identity")
	private int id;

	@Basic
	@Column(name = "CompReferee_Role", length = 1)
	private String role;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "CompReferee_Referee_ID_FK", nullable = false)
	private Referee referee;
}