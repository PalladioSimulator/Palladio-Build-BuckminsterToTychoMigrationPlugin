package org.palladiosimulator.buckminstermigrator.templates

import org.palladiosimulator.buckminstermigrator.data.FolderPomName

class UpdateSitePOMGenerator extends FileBasedGeneratorBase {
	
	val String groupId
	val String version

	new(String groupId, String version) {
		this.groupId = groupId
		this.version = version
	}
	
	override protected generateContent() '''
		<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
			xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
			<modelVersion>4.0.0</modelVersion>
		
			<parent>
				<groupId>«groupId»</groupId>
				<artifactId>«FolderPomName.RELENG.artifactId»</artifactId>
				<version>«version»</version>
			</parent>
		
			<artifactId>«groupId».updatesite</artifactId>
			<packaging>eclipse-repository</packaging>
		
		</project>
	'''
	
}
