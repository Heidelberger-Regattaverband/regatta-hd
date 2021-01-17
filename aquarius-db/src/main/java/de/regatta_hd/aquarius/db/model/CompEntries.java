package de.regatta_hd.aquarius.db.model;

import java.util.Set;

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
 * Auto-generated by:
 * org.apache.openjpa.jdbc.meta.ReverseMappingTool$AnnotatedCodeGenerator
 */
@Entity
@Table(schema = "dbo", name = "CompEntries")
//lombok
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class CompEntries {
	@Basic
	@Column(name = "CE_Lane")
	@ToString.Include(rank = 10)
	private Short lane;

	@Id
	@Column(name = "CE_ID", columnDefinition = "int identity")
	private int id;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "CE_Comp_ID_FK")
	private Comp comp;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "CE_Entry_ID_FK", nullable = false)
	private Entry entry;

	@OneToMany(targetEntity = Result.class, mappedBy = "compEntries", cascade = CascadeType.MERGE)
	@OrderBy("rank")
	private Set<Result> results;
}