package org.dainst.gazetteer.dao;

import java.util.List;
import java.util.Set;

import org.dainst.gazetteer.domain.Identifier;
import org.dainst.gazetteer.domain.Place;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PlaceRepository extends PagingAndSortingRepository<Place, String> {

	public Place getByLinksObjectAndLinksPredicate(String object, String predicate);

	public List<Place> findByPrefNameTitleAndType(String name, String type);

	public List<Place> findByThesaurus(String thesaurus);

	public List<Place> findByThesaurusAndParentIsNull(String key);

	public List<Place> findByThesaurusAndType(String key, String type);

	public Place findByIds(Identifier id);

	public List<Place> findByIdIn(Set<String> children);

}
