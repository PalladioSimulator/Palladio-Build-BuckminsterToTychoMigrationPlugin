package org.palladiosimulator.buckminstermigrator.templates

import org.palladiosimulator.buckminstermigrator.templates.FileBasedGeneratorBase

class TargetPlatformGenerator extends FileBasedGeneratorBase {
	
	val String groupId
	
	new(String groupId) {
		this.groupId = groupId;
	}
	
	override protected generateContent() '''
		<?xml version="1.0" encoding="UTF-8" standalone="no"?>
		<?pde version="3.8"?><target name="«groupId» Target Platform" sequenceNumber="1">
		<locations>
		</locations>
		</target>
	'''
	
} 