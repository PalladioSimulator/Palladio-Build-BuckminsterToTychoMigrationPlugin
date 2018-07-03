package org.palladiosimulator.buckminstermigrator.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public enum UpdateSiteCategory {
	SUPPORT("org.palladiosimulator.support", "Palladio Supporting Features", "Features that support the Palladio Bench tooling, but also could be used independently"),
	QUAL("org.palladiosimulator.qual", "Quality Analysis Lab", "The Quality Analysis Lab (QuAL) is responsible for taking measurements from experiments or simulations and their persistance"),
	CORE("org.palladiosimulator.corefeatures", "This category contains all features which belong to a complete installation of the Palladio Bench Core Features", "This category contains all features which belong to a complete installation of the Palladio Bench Core Features"),
	ADDONS("org.palladiosimulator.addons", "Palladio Bench Addons", "Palladio Bench Addons");
	
	private final String categoryIdPrefix;
	private final String name;
	private final String description;

	private UpdateSiteCategory(String categoryIdPrefix, String name, String description) {
		this.categoryIdPrefix = categoryIdPrefix;
		this.name = name;
		this.description = description;
	}
	
	public String getCategoryId() {
		return categoryIdPrefix + ".category";
	}
	
	public String getCategorySourceId() {
		return categoryIdPrefix + ".source.category";
	}
	
	public String getName() {
		return name;
	}
	
	public String getSourceName() {
		return name + " Source";
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getSourceDescription() {
		return getSourceName();
	}

	public static Optional<UpdateSiteCategory> findByName(String updatesiteCategory) {
		return Arrays.asList(values()).stream().filter(c -> c.name().toLowerCase().equalsIgnoreCase(updatesiteCategory)).findFirst();
	}
	
	public static Collection<String> getAllLiteralNames() {
		return Arrays.asList(values()).stream().map(UpdateSiteCategory::name).map(String::toLowerCase).collect(Collectors.toList());
	}
	
}
