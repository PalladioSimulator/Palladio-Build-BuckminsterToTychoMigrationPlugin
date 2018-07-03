package org.palladiosimulator.buckminstermigrator.templates

import java.io.File
import java.nio.charset.StandardCharsets
import org.apache.commons.io.FileUtils
import java.io.IOException

abstract class FileBasedGeneratorBase implements FileBasedGenerator {
	
	override generateInto(File f) throws IOException {
		FileUtils.write(f, generateContent, StandardCharsets.UTF_8)
	}
	
	protected abstract def String generateContent()
}
