package org.palladiosimulator.buckminstermigrator.templates

class EmptyProjectDefinitionGenerator extends FileBasedGeneratorBase {
	
	val String name
	
	new(String name) {
		this.name = name
	}
	
	override protected generateContent() '''
		<?xml version="1.0" encoding="UTF-8"?>
		<projectDescription>
			<name>«name»</name>
			<comment></comment>
			<projects>
			</projects>
			<buildSpec>
			</buildSpec>
			<natures>
			</natures>
		</projectDescription>
	'''
	
}
