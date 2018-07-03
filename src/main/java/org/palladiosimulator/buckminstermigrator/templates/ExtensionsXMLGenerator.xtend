package org.palladiosimulator.buckminstermigrator.templates

import org.palladiosimulator.buckminstermigrator.templates.FileBasedGeneratorBase

class ExtensionsXMLGenerator extends FileBasedGeneratorBase {
	
	val String tychoVersion
	val String tpRefresherVersion
	
	new(String tychoVersion, String tpRefresherVersion) {
		this.tychoVersion = tychoVersion
		this.tpRefresherVersion = tpRefresherVersion
	}
	
	override protected generateContent() '''
		<?xml version="1.0" encoding="UTF-8"?>
		<extensions>
			<extension>
				<groupId>org.eclipse.tycho.extras</groupId>
				<artifactId>tycho-pomless</artifactId>
				<version>«tychoVersion»</version>
			</extension>
			<extension>
				<groupId>org.palladiosimulator</groupId>
				<artifactId>tycho-tp-refresh-maven-plugin</artifactId>
				<version>«tpRefresherVersion»</version>
			</extension>
		</extensions>
	'''
	
}