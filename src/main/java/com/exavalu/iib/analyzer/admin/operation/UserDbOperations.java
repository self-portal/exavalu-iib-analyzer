package com.exavalu.iib.analyzer.admin.operation;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class UserDbOperations {

	public boolean updateUser(String userName, int activeStatus, int roleId, EntityManager entityManager) {
		boolean dataUpdated = false;

		int rowAffected = 0;
		String updateStatementForUser = "UPDATE users SET active_status = ? , role_id = ? WHERE user_name = ?";
		Query query = entityManager.createNativeQuery(updateStatementForUser).setParameter(1, activeStatus)
				.setParameter(2, roleId).setParameter(3, userName);

		rowAffected = query.executeUpdate();

		if (rowAffected == 1)
			dataUpdated = true;

		return dataUpdated;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List retrieveUsers(EntityManager entityManager) {

		String statementToRetrieveAllUsers = """
                SELECT user_name, email_id, first_name, middle_name, last_name, mobile_no, job_title, \
                region, company, create_date_timestamp, updated_date_timestamp, active_status, role_id  FROM users\
                """;
		Query queryToRetrieveAllUsers = entityManager.createNativeQuery(statementToRetrieveAllUsers);
		List<Object[]> resultSetOfAllUsers = queryToRetrieveAllUsers.getResultList();

		return resultSetOfAllUsers;
	}

}
