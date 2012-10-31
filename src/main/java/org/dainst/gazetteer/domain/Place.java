package org.dainst.gazetteer.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Place {

	@Id
	private String id;

	private Set<Link> links = new HashSet<Link>();
	
	private PlaceName prefName;

	private List<PlaceName> names = new ArrayList<PlaceName>();
	
	private String type;

	private Set<Location> locations = new HashSet<Location>();

	private String parent;

	private Set<String> children = new HashSet<String>();

	private Set<String> relatedPlaces = new HashSet<String>();
	
	private Set<Comment> comments = new HashSet<Comment>();
	
	private Set<Tag> tags = new HashSet<Tag>();
	
	private Set<Identifier> ids = new HashSet<Identifier>();
	
	private String thesaurus;
	
	private boolean needsReview = false;
	
	private boolean deleted = false;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Set<Link> getLinks() {
		return links;
	}

	public void setLinks(Set<Link> links) {
		this.links = links;
	}
	
	public void addLink(Link link) {
		this.links.add(link);
	}

	public List<PlaceName> getNames() {
		return names;
	}

	public Map<String, PlaceName> getNameMap() {
		HashMap<String, PlaceName> result = new HashMap<String, PlaceName>();
		for (PlaceName name : names) {
			result.put(name.getLanguage(), name);
		}
		return result;
	}

	public void setNames(List<PlaceName> names) {
		this.names = names;
	}
	
	public void addName(PlaceName name) {
		names.add(name);
	}

	public Set<Location> getLocations() {
		return locations;
	}

	public void setLocations(Set<Location> locations) {
		this.locations = locations;
	}
	
	public void addLocation(Location location) {
		locations.add(location);
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}
	
	public Set<String> getChildren() {
		return children;
	}

	public void setChildren(Set<String> children) {
		this.children = children;
	}
	
	public void addChild(String child) {
		children.add(child);
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void removeName(PlaceName name) {
		if (names.contains(name)) {
			names.remove(name);
		}
	}

	public void removeLocation(Location location) {
		if (locations.contains(location)) {
			locations.remove(location);
		}
	}

	public Set<String> getRelatedPlaces() {
		return relatedPlaces;
	}

	public void setRelatedPlaces(Set<String> relatedPlaces) {
		this.relatedPlaces = relatedPlaces;
	}

	public void addRelatedPlace(String relatedPlace) {
		relatedPlaces.add(relatedPlace);
	}

	public Set<Comment> getComments() {
		return comments;
	}

	public void setComments(Set<Comment> comments) {
		this.comments = comments;
	}
	
	public void addComment(Comment comment) {
		this.comments.add(comment);
	}

	public Set<Tag> getTags() {
		return tags;
	}

	public void setTags(Set<Tag> tags) {
		this.tags = tags;
	}
	
	public void addTag(Tag tag) {
		this.tags.add(tag);
	}

	public Set<Identifier> getIdentifiers() {
		return ids;
	}

	public void setIdentifiers(Set<Identifier> ids) {
		this.ids = ids;
	}
	
	public void addIdentifier(Identifier id) {
		this.ids.add(id);
	}

	public String getThesaurus() {
		return thesaurus;
	}

	public void setThesaurus(String thesaurus) {
		this.thesaurus = thesaurus;
	}

	public boolean isNeedsReview() {
		return needsReview;
	}

	public void setNeedsReview(boolean needsReview) {
		this.needsReview = needsReview;
	}
	
	public String toString() {
		return String.format("Place(id: %s, name: %s, type: %s)",
				getId(), getPrefName().getTitle(), getType());
	}

	public PlaceName getPrefName() {
		return prefName;
	}

	public void setPrefName(PlaceName prefName) {
		this.prefName = prefName;
	}
	
}
