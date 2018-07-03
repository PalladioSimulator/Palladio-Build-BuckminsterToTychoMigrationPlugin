package org.palladiosimulator.buckminstermigrator.templates

class MainPOMGenerator extends FileBasedGeneratorBase {

	val String eclipseParentVersion
	val String groupId
	val String version

	new(String eclipseParentVersion, String groupId, String version) {
		this.eclipseParentVersion = eclipseParentVersion
		this.groupId = groupId
		this.version = version
	}

	override protected generateContent() '''
		<?xml version="1.0" encoding="UTF-8"?>
		<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
				xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
				
			<modelVersion>4.0.0</modelVersion>
			<parent>
				<groupId>org.palladiosimulator</groupId>
				<artifactId>eclipse-parent</artifactId>
				<version>«eclipseParentVersion»</version>
			</parent>
			<groupId>«groupId»</groupId>
			<artifactId>parent</artifactId>	
			<version>«version»</version>
			<packaging>pom</packaging>
			
			<properties>
				<org.palladiosimulator.maven.tychotprefresh.tplocation.1>${project.basedir}/releng/«groupId».targetplatform/tp.target</org.palladiosimulator.maven.tychotprefresh.tplocation.1>
			</properties>
			
			<modules>
				<module>bundles</module>
				<module>features</module>
				<module>tests</module>
				<module>releng</module>
			</modules>
			
		</project>
	'''

}
