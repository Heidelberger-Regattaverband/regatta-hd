package de.regatta_hd.aquarius.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Auto-generated by: org.apache.openjpa.jdbc.meta.ReverseMappingTool$AnnotatedCodeGenerator
 */
@Entity
@Table(schema = "dbo", name = "MetaData")
//lombok
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class MetaData {

	@Id
	@Column(name = "MetaData_Key")
	@ToString.Include(rank = 2)
	private String key;

	@Column(name = "MetaData_Value")
	@ToString.Include(rank = 1)
	private String value;

}