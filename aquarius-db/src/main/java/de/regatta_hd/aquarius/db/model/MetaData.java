package de.regatta_hd.aquarius.db.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

/**
 * Auto-generated by: org.apache.openjpa.jdbc.meta.ReverseMappingTool$AnnotatedCodeGenerator
 */
@Entity
@Table(schema = "dbo", name = "MetaData")
@IdClass(de.regatta_hd.aquarius.db.model.MetaDataId.class)
public class MetaData {
	@Id
	@Column(name = "MetaData_Key", length = 32)
	private String metaDataKey;

	@Basic
	@Column(name = "MetaData_Value", length = 32)
	private String metaDataValue;

	public MetaData() {
	}

	public MetaData(String metaDataKey) {
		this.metaDataKey = metaDataKey;
	}

	public String getMetaDataKey() {
		return this.metaDataKey;
	}

	public void setMetaDataKey(String metaDataKey) {
		this.metaDataKey = metaDataKey;
	}

	public String getMetaDataValue() {
		return this.metaDataValue;
	}

	public void setMetaDataValue(String metaDataValue) {
		this.metaDataValue = metaDataValue;
	}
}