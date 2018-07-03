package org.palladiosimulator.buckminstermigrator.templates

import java.io.File
import java.io.IOException

interface FileBasedGenerator {
	
	def void generateInto(File f) throws IOException
	
}
