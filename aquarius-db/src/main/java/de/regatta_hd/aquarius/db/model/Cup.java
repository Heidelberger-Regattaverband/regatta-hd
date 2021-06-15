package de.regatta_hd.aquarius.db.model;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Auto-generated by: org.apache.openjpa.jdbc.meta.ReverseMappingTool$AnnotatedCodeGenerator
 */
@Entity
@Table(schema = "dbo", name = "Cup")
//lombok
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class Cup {
	@Basic
	@Column(columnDefinition = "nvarchar", length = 32)
	private String caption;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "Parent_ID")
	private Cup parent;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "PointTable_ID_FK")
	private CupPointTable pointTable;

	@OneToMany(targetEntity = Cup.class, mappedBy = "parent", cascade = CascadeType.MERGE)
	private Set<Cup> cups;

	@Id
	@Column(columnDefinition = "int identity")
	private int id;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "Race_ID_FK")
	private Offer offer;
}