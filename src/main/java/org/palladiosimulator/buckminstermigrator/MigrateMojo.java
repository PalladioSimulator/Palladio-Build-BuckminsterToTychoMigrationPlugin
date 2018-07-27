package org.palladiosimulator.buckminstermigrator;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.version.Version;
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
	
	@Parameter(property = "updatesiteCategory", required = true)
	private String updatesiteCategory;
	
	@Parameter(defaultValue = "0.1.0-SNAPSHOT", property = "targetVersion", required = true)
	private String targetVersion;
	
	@Parameter(property = "targetGroupId", required = true)
	private String targetGroupId;

	@Component
    private RepositorySystem repoSystem;

	@Parameter(property = "repoSession", required=true, readonly=true, defaultValue="${repositorySystemSession}")
    private RepositorySystemSession repoSession;
    
	@Parameter(property = "remoteRepositories", required=true, readonly=true, defaultValue="${project.remoteArtifactRepositories}")
    protected List<ArtifactRepository> remoteRepositories;

	protected List<RemoteRepository> aetherRepos;
	
	
	public void execute() throws MojoExecutionException {
		Optional<UpdateSiteCategory> category = UpdateSiteCategory.findByName(updatesiteCategory);
		UpdateSiteCategory foundCategory = category.orElseThrow(() -> new MojoExecutionException(String.format("The update site category has to be one of %s.", UpdateSiteCategory.getAllLiteralNames().stream().collect(Collectors.joining(", ")))));
		
		aetherRepos = remoteRepositories.stream().map(MigrateMojo::convert).collect(Collectors.toList());
		
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

		// gather latest versions
		String parentVersion;
		String tpRefresherVersion;
		String tychoVersion;
		try {
			parentVersion = findLatestVersion("org.palladiosimulator", "eclipse-parent");
			tpRefresherVersion = findLatestVersion("org.palladiosimulator", "tycho-tp-refresh-maven-plugin");
			tychoVersion = findTychoVersion(parentVersion);			
		} catch (ArtifactResolutionException | VersionRangeResolutionException e) {
			throw new IOException("Error in determining latest versions of dependencies.", e);
		}
		
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
		
		// releng POM
		new FolderPOM(FolderPomName.RELENG, targetGroupId, targetVersion, Arrays.asList(updateSiteFolder.getName())).generateInto(new File(relengFolder, "pom.xml"));
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
	
	private String findLatestVersion(String groupId, String artifactId) throws VersionRangeResolutionException {
		VersionRangeRequest request = new VersionRangeRequest();
		request.setArtifact(new DefaultArtifact(groupId, artifactId, "jar", "(,]"));
		request.setRepositories(aetherRepos);
		VersionRangeResult result = repoSystem.resolveVersionRange(repoSession, request);
		Optional<Version> highestVersion = result.getVersions().stream().filter(v -> !v.toString().contains("SNAPSHOT")).max((v1, v2) -> v1.compareTo(v2));
		return highestVersion.get().toString();
	}
	
	private static RemoteRepository convert(ArtifactRepository repo) {
		return new RemoteRepository.Builder(repo.getId(), "default", repo.getUrl()).build();
	}
	
	private String getPOM(String groupId, String artifactId, String version) throws ArtifactResolutionException, IOException {
		DefaultArtifact artifact = new DefaultArtifact(groupId, artifactId, "pom", version);
		ArtifactRequest request = new ArtifactRequest();
        request.setArtifact( artifact );
        request.setRepositories(aetherRepos);
        ArtifactResult result = repoSystem.resolveArtifact(repoSession, request);
        return FileUtils.readFileToString(result.getArtifact().getFile(), Charset.defaultCharset());
	}

	private String findTychoVersion(String parentVersion) throws ArtifactResolutionException, IOException {
		String parentPOMText = getPOM("org.palladiosimulator", "eclipse-parent", parentVersion);
		Pattern pattern = Pattern.compile(".*<tycho\\.version>([0-9.]+)</tycho\\.version>.*", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(parentPOMText);
		matcher.find();
		String tychoVersion = matcher.group(1);
		return tychoVersion;
	}
}
