package org.palladiosimulator.buckminstermigrator.templates

import org.palladiosimulator.buckminstermigrator.data.FolderPomName

class FolderPOM extends FileBasedGeneratorBase {
	
	val FolderPomName name
	val String groupId
	val String version
	val Iterable<String> moduleNames

	new(FolderPomName name, String groupId, String version, Iterable<String> moduleNames) {
		this.name = name
		this.groupId = groupId
		this.version = version
		this.moduleNames = moduleNames
	}
	
	override protected generateContent() '''
		<?xml version="1.0" encoding="UTF-8"?>
		<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
				xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
				
			<modelVersion>4.0.0</modelVersion>
			<parent>
				<groupId>«groupId»</groupId>
				<artifactId>parent</artifactId>
				<version>«version»</version>
			</parent>
			<artifactId>«name.artifactId»</artifactId>
			<packaging>pom</packaging>
			
			<modules>
				«FOR moduleName : moduleNames»
					<module>«moduleName»</module>
				«ENDFOR»
			</modules>
			
		</project>
	'''
	
}
