package org.dainst.gazetteer.dao;

import java.util.List;

import org.dainst.gazetteer.domain.GroupRole;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface GroupRoleRepository extends PagingAndSortingRepository<GroupRole, String> {
	
	public List<GroupRole> findByGroupId(String groupId);
	
	public List<GroupRole> findByUserId(String userId);
	
	public GroupRole findByGroupIdAndUserId(String groupId, String userId);
	
	@Query(value = "{ 'groupId': ?0 }", count = true)
	public long getCountByGroupId(String groupId);
}
