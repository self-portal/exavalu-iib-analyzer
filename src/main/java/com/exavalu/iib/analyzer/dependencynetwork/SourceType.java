package com.exavalu.iib.analyzer.dependencynetwork;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Date;

/**
 *
 * Last Updated: 2023-08-21 13:12
 *
 * Description: Annotated with JPA (Jakarta Persistence API) the `SourceType`
 * class is mapped to the "source_types" table in the database. This class
 * includes attributes like `sourceFileMasterId`, `sourceFileName`,
 * `sourceFileProjectType`, `sourceFileDependencies`, `updatedBy`, and
 * `updatedDateTimestamp`, which are bound to the corresponding columns in the
 * database table.
 */

@Entity
@Table(name = "source_file_info", uniqueConstraints = { @UniqueConstraint(columnNames = "source_file_name") })
public class SourceType {

	@Id
	@Column(name = "source_file_master_id", nullable = false)
	private String sourceFileMasterId;

	@Column(name = "source_file_name", nullable = false)
	private String sourceFileName;

	@Column(name = "source_file_project_type", nullable = false, columnDefinition = "LONGTEXT")
	private String sourceFileProjectType;

	@Column(name = "source_file_dependencies", columnDefinition = "LONGTEXT")
	private String sourceFileDependencies;

	@Column(name = "updated_by", nullable = false)
	private String updatedBy;

	@Column(name = "updated_date_timestamp", nullable = false)
	private Date updatedDateTimestamp;

	public String getSourceFileMasterId() {
		return sourceFileMasterId;
	}

	public void setSourceFileMasterId(String sourceFileMasterId) {
		this.sourceFileMasterId = sourceFileMasterId;
	}

	public String getSourceFileName() {
		return sourceFileName;
	}

	public void setSourceFileName(String sourceFileName) {
		this.sourceFileName = sourceFileName;
	}

	public String getSourceFileProjectType() {
		return sourceFileProjectType;
	}

	public void setSourceFileProjectType(String sourceFileProjectType) {
		this.sourceFileProjectType = sourceFileProjectType;
	}

	public String getSourceFileDependencies() {
		return sourceFileDependencies;
	}

	public void setSourceFileDependencies(String sourceFileDependencies) {
		this.sourceFileDependencies = sourceFileDependencies;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public Date getUpdatedDateTimestamp() {
		return updatedDateTimestamp;
	}

	public void setUpdatedDateTimestamp(Date updatedDateTimestamp) {
		this.updatedDateTimestamp = updatedDateTimestamp;
	}
}
