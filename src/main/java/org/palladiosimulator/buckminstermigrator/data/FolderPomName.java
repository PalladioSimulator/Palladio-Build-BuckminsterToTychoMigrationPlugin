package org.palladiosimulator.buckminstermigrator.data;

public enum FolderPomName {
	BUNDLES("bundles"), FEATURES("features"), TESTS("tests"), RELENG("releng");

	private final String artifactId;

	FolderPomName(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getArtifactId() {
		return artifactId;
	}

}