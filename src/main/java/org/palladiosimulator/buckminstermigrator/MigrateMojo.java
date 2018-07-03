package org.palladiosimulator.buckminstermigrator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.palladiosimulator.buckminstermigrator.data.Feature;
import org.palladiosimulator.buckminstermigrator.data.FolderPomName;
import org.palladiosimulator.buckminstermigrator.data.UpdateSiteCategory;
import org.palladiosimulator.buckminstermigrator.templates.CategoryXMLGenerator;
import org.palladiosimulator.buckminstermigrator.templates.EmptyProjectDefinitionGenerator;
import org.palladiosimulator.buckminstermigrator.templates.ExtensionsXMLGenerator;
import org.palladiosimulator.buckminstermigrator.templates.FolderPOM;
import org.palladiosimulator.buckminstermigrator.templates.MainPOMGenerator;
import org.palladiosimulator.buckminstermigrator.templates.TargetPlatformGenerator;
import org.palladiosimulator.buckminstermigrator.templates.UpdateSitePOMGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@Mojo(name = "migrate")
public class MigrateMojo extends AbstractMojo {
	/**
	 * Location of the file.
	 */
	@Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
	private File outputDirectory;

	@Parameter(defaultValue = "${project.basedir}", property = "baseDir", required = true)
	private File baseDirectory;
	
	@Parameter(defaultValue = "0.1.8", property = "parentVersion", required = true)
	private String parentVersion;
	
	@Parameter(defaultValue = "1.1.0", property = "tychoVersion", required = true)
	private String tychoVersion;
	
	@Parameter(defaultValue = "0.2.2", property = "tpRefresherVersion", required = true)
	private String tpRefresherVersion;
	
	@Parameter(property = "updatesiteCategory", required = true)
	private String updatesiteCategory;
	
	@Parameter(defaultValue = "0.1.0-SNAPSHOT", property = "targetVersion", required = true)
	private String targetVersion;
	
	@Parameter(property = "targetGroupId", required = true)
	private String targetGroupId;

	public void execute() throws MojoExecutionException {
		Optional<UpdateSiteCategory> category = UpdateSiteCategory.findByName(updatesiteCategory);
		UpdateSiteCategory foundCategory = category.orElseThrow(() -> new MojoExecutionException(String.format("The update site category has to be one of %s.", UpdateSiteCategory.getAllLiteralNames().stream().collect(Collectors.joining(", ")))));
		
		try {
			doExecution(foundCategory);
		} catch (IOException e) {
			throw new MojoExecutionException("Failed execution", e);
		}
	}

	private void doExecution(UpdateSiteCategory foundCategory) throws IOException {
		
		Collection<File> containedDirectories = new ArrayList<>(Arrays.asList(baseDirectory.listFiles(f -> f.isDirectory() && !".git".equals(f.getName()))));
		
		Collection<File> featureDirectories = containedDirectories.stream().filter(f -> f.getName().contains("feature")).collect(Collectors.toList());
		Collection<File> testDirectories = containedDirectories.stream().filter(f -> f.getName().contains("tests")).collect(Collectors.toList());
		File buckminsterDirectory = containedDirectories.stream().filter(f -> f.getName().contains("buckminster")).findFirst().orElseThrow(() -> new IOException("Failed to find buckminster directory"));
		
		containedDirectories.removeAll(featureDirectories);
		containedDirectories.removeAll(testDirectories);
		containedDirectories.remove(buckminsterDirectory);
		Collection<File> bundleDirectories = containedDirectories;
		
		Collection<Feature> features = extractFeatures(buckminsterDirectory);
		
		doGeneration(bundleDirectories, featureDirectories, testDirectories, foundCategory, features);
		
		
	}

	private void doGeneration(Collection<File> bundleDirectories, Collection<File> featureDirectories, Collection<File> testDirectories, UpdateSiteCategory foundCategory, Collection<Feature> features) throws IOException {
		// main POM
		new MainPOMGenerator(parentVersion, targetGroupId, targetVersion).generateInto(new File(outputDirectory, "pom.xml"));
		
		// extensions folder
		File mvnExtensionsFolder = createDirectory(outputDirectory, ".mvn");
		new ExtensionsXMLGenerator(tychoVersion, tpRefresherVersion).generateInto(new File(mvnExtensionsFolder, "extensions.xml"));
		
		// bundles folder
		createSubFolder(FolderPomName.BUNDLES, bundleDirectories);
		
		// features folder
		createSubFolder(FolderPomName.FEATURES, featureDirectories);
		
		// tests folder
		createSubFolder(FolderPomName.TESTS, testDirectories);

		// releng folder
		File relengFolder = createRootDirectory(FolderPomName.RELENG);
		
		// update site folder
		File updateSiteFolder = createDirectory(relengFolder, targetGroupId + ".updatesite");
		new EmptyProjectDefinitionGenerator(updateSiteFolder.getName()).generateInto(new File(updateSiteFolder, ".project"));
		new UpdateSitePOMGenerator(targetGroupId, targetVersion).generateInto(new File(updateSiteFolder, "pom.xml"));
		new CategoryXMLGenerator(foundCategory, features).generateInto(new File(updateSiteFolder, "category.xml"));

		// target platform folder
		File targetPlatformFolder = createDirectory(relengFolder, targetGroupId + ".targetplatform");
		new EmptyProjectDefinitionGenerator(targetPlatformFolder.getName()).generateInto(new File(targetPlatformFolder, ".project"));
		new TargetPlatformGenerator(targetGroupId).generateInto(new File(targetPlatformFolder, "tp.target"));
	}
	
	private void createSubFolder(FolderPomName folderName, Collection<File> content) throws IOException {
		File bundlesFolder = createRootDirectory(folderName);
		new FolderPOM(folderName, targetGroupId, targetVersion, getFileNames(content)).generateInto(new File(bundlesFolder, "pom.xml"));
		copyDirectories(content, bundlesFolder);
	}

	private static void copyDirectories(Iterable<File> directories, File destinationBase) throws IOException {
		for (File dir : directories) {
			FileUtils.copyDirectory(dir, new File(destinationBase, dir.getName()));
		}
	}
	
	private File createRootDirectory(FolderPomName folderName) {
		return createDirectory(outputDirectory, folderName.getArtifactId());
	}
	
	private static File createDirectory(File baseDirectory, String name) {
		File f = new File(baseDirectory, name);
		f.mkdir();
		return f;
	}
	
	private static Iterable<String> getFileNames(Collection<File> files) {
		return files.stream().map(File::getName).collect(Collectors.toList());
	}
	
	private Collection<Feature> extractFeatures(File buckminsterDirectory) throws IOException {
		File featureXml = new File(buckminsterDirectory, "feature.xml");
		
		Collection<Feature> features = new ArrayList<>();
		
		try {
			Document doc = getDocument(featureXml);
			NodeList includeNodes = doc.getElementsByTagName("includes");
			for (int i = 0; i < includeNodes.getLength(); ++i) {
				Element includeNode = (Element)includeNodes.item(i);
				String featureId = includeNode.getAttribute("id");
				File featureDescriptor = new File(new File(baseDirectory, featureId), "feature.xml");
				String featureVersion = getFeatureVersion(featureDescriptor);
				features.add(new Feature(featureId, featureVersion));
			}			
		} catch (ParserConfigurationException | SAXException e) {
			throw new IOException(e);
		}
		
		return features;
	}
	
	private static String getFeatureVersion(File featureXmlFile) throws ParserConfigurationException, SAXException, IOException {
		Document doc = getDocument(featureXmlFile);
		return doc.getDocumentElement().getAttribute("version");
	}
	
	private static Document getDocument(File f) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder.parse(f);
	}

}
