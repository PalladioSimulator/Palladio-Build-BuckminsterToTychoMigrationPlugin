package org.palladiosimulator.buckminstermigrator.templates

import org.palladiosimulator.buckminstermigrator.data.Feature
import org.palladiosimulator.buckminstermigrator.data.UpdateSiteCategory

class CategoryXMLGenerator extends FileBasedGeneratorBase {
	
	val UpdateSiteCategory category
	val Iterable<Feature> features
	
	new(UpdateSiteCategory category, Iterable<Feature> features) {
		this.category = category
		this.features = features
	}
	
	override protected generateContent() '''
		<?xml version="1.0" encoding="UTF-8"?>
		<site>
			«FOR feature : features»
				<feature url="features/«feature.name»_«feature.version».jar" id="«feature.name»" version="«feature.version»">
				   <category name="«category.categoryId»"/>
				</feature>
				<feature url="features/«feature.name».source_«feature.version».jar" id="«feature.name».source" version="«feature.version»">
				   <category name="«category.categorySourceId»"/>
				</feature>
			«ENDFOR»
			<category-def name="«category.categoryId»" label="«category.getName()»">
			   <description>
			      «category.description»
			   </description>
			  </category-def>
			  <category-def name="«category.categorySourceId»" label="«category.sourceName»">
			     <description>
			        «category.sourceDescription»
			     </description>
			  </category-def>
		</site>
	'''
	
}
