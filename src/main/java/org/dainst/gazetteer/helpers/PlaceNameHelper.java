package org.dainst.gazetteer.helpers;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.dainst.gazetteer.domain.PlaceName;

public class PlaceNameHelper {
		
	private Locale locale;
	
	private Locale originalLocale;
	
	private LanguagesHelper languagesHelper;
	
	private Map<String, String> localizedLanguages;

	public List<PlaceName> sortPlaceNames(Set<PlaceName> placeNames) {
			
		localizedLanguages = languagesHelper.getLocalizedLanguages(locale);
		
		List<PlaceName> result = new ArrayList<PlaceName>(placeNames);
		Collections.sort(result, new PlaceNameComparator());
		
		return result;
	}
	
	
	public Locale getLocale() {
		return locale;
	}


	public void setLocale(Locale locale) {
		this.locale = locale;
	}


	public Locale getOriginalLocale() {
		return originalLocale;
	}


	public void setOriginalLocale(Locale originalLocale) {
		this.originalLocale = originalLocale;
	}


	public LanguagesHelper getLanguagesHelper() {
		return languagesHelper;
	}


	public void setLanguagesHelper(LanguagesHelper languagesHelper) {
		this.languagesHelper = languagesHelper;
	}


	private class PlaceNameComparator implements Comparator<PlaceName> {
		public int compare(PlaceName placeName1, PlaceName placeName2) {

			int langComp;
						
			if ((placeName1.getLanguage() == null || placeName1.getLanguage().isEmpty())
					&& (placeName2.getLanguage() != null && !placeName2.getLanguage().isEmpty()))
				langComp = 1;
			else if ((placeName2.getLanguage() == null || placeName2.getLanguage().isEmpty())
					&& (placeName1.getLanguage() != null && !placeName1.getLanguage().isEmpty()))
				langComp = -1;
			else if ((placeName1.getLanguage() == null || placeName1.getLanguage().isEmpty())
					&& (placeName2.getLanguage() == null || placeName2.getLanguage().isEmpty()))
				langComp = 0;
			else if (originalLocale.getISO3Language().equals(new Locale(placeName1.getLanguage()).getISO3Language())
					&& !originalLocale.getISO3Language().equals(new Locale(placeName2.getLanguage()).getISO3Language()))
				langComp = -1;
			else if (originalLocale.getISO3Language().equals(new Locale(placeName2.getLanguage()).getISO3Language())
					&& !originalLocale.getISO3Language().equals(new Locale(placeName1.getLanguage()).getISO3Language()))
				langComp = 1;
			else {
				String localizedLanguage1 = localizedLanguages.get(placeName1.getLanguage());
				String localizedLanguage2 = localizedLanguages.get(placeName2.getLanguage());
				
				langComp = Collator.getInstance(locale).compare(localizedLanguage1, localizedLanguage2);
			}
			
			if (langComp != 0)
	            return langComp;
			else
				return Collator.getInstance(locale).compare(placeName1.getTitle(), placeName2.getTitle());
		}
	}
	
}
