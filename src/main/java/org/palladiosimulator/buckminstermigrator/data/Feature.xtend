package org.palladiosimulator.buckminstermigrator.data

class Feature {
	val String name
	val String version
	
	new(String name, String version) {
		this.name = name
		this.version = version
	}
	
	def getName() {
		name
	}
	
	def getVersion() {
		version
	}
}
