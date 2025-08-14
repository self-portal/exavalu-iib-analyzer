package com.exavalu.iib.analyzer.dependencynetwork;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * Last Updated: 2023-08-21 01:45
 *
 * Description: The `SourceTypeRepository` interface extends JpaRepository to
 * provide CRUD operations for the `SourceType` entity, which represents the
 * `source_types` table in the database. It includes a custom query method
 * `fetchDependenciesAndType` to retrieve specific fields such as
 * `sourceFileProjectType` and `sourceFileDependencies` from the `SourceType`
 * entity, based on `sourceFileMasterId` and `sourceFileName`.
 */

public interface SourceTypeRepository extends JpaRepository<SourceType, String> {

	// RetrievedSourceFileInfoProjection interface
	// defines a projection to extract specific
	// fields (sourceFileProjectType and
	// sourceFileDependencies) from the SourceType.
	interface RetrievedSourceFileInfoProjection {
		String getSourceFileProjectType();

		String getSourceFileDependencies();
	}

	// Custom query method to prevent potential SQL injection vulnerabilities by
	// avoiding raw SQL queries directly in the code.
	@Query("""
            SELECT st.sourceFileDependencies as sourceFileDependencies, st.sourceFileProjectType as sourceFileProjectType \
            FROM SourceType st \
            WHERE st.sourceFileMasterId = :sourceFileMasterId \
            AND st.sourceFileName = :sourceFileName\
            """)
	RetrievedSourceFileInfoProjection fetchDependenciesAndType(@Param("sourceFileMasterId") String sourceFileMasterId,
			@Param("sourceFileName") String sourceFileName);
}
