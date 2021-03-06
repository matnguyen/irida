package ca.corefacility.bioinformatics.irida.pipeline.results.impl.unit;

import ca.corefacility.bioinformatics.irida.config.data.IridaApiJdbcDataSourceConfig;
import ca.corefacility.bioinformatics.irida.config.services.IridaApiServicesConfig;
import ca.corefacility.bioinformatics.irida.exceptions.AnalysisAlreadySetException;
import ca.corefacility.bioinformatics.irida.exceptions.PostProcessingException;
import ca.corefacility.bioinformatics.irida.model.sample.MetadataTemplateField;
import ca.corefacility.bioinformatics.irida.model.sample.Sample;
import ca.corefacility.bioinformatics.irida.model.sample.metadata.MetadataEntry;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.SingleEndSequenceFile;
import ca.corefacility.bioinformatics.irida.model.workflow.analysis.Analysis;
import ca.corefacility.bioinformatics.irida.model.workflow.analysis.AnalysisOutputFile;
import ca.corefacility.bioinformatics.irida.model.workflow.submission.AnalysisSubmission;
import ca.corefacility.bioinformatics.irida.pipeline.results.impl.SISTRSampleUpdater;
import ca.corefacility.bioinformatics.irida.repositories.analysis.AnalysisRepository;
import ca.corefacility.bioinformatics.irida.repositories.analysis.submission.AnalysisSubmissionRepository;
import ca.corefacility.bioinformatics.irida.repositories.sample.SampleRepository;
import ca.corefacility.bioinformatics.irida.service.sample.MetadataTemplateService;
import ca.corefacility.bioinformatics.irida.service.sample.SampleService;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExcecutionListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SISTRSampleUpdaterTest {

	private SISTRSampleUpdater updater;

	private SampleService sampleService;
	private MetadataTemplateService metadataTemplateService;

	@Before
	public void setUp() {
		sampleService = mock(SampleService.class);
		metadataTemplateService = mock(MetadataTemplateService.class);

		updater = new SISTRSampleUpdater(metadataTemplateService, sampleService);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testUpdaterPassed() throws PostProcessingException, AnalysisAlreadySetException {
		ImmutableMap<String, String> expectedResults = ImmutableMap
				.of("SISTR serovar", "Enteritidis", "SISTR cgMLST Subspecies", "enterica", "SISTR QC Status", "PASS");

		Path outputPath = Paths.get("src/test/resources/files/sistr-predictions-pass.json");

		AnalysisOutputFile outputFile = new AnalysisOutputFile(outputPath, null, null, null);

		Analysis analysis = new Analysis(null, ImmutableMap.of("sistr-predictions", outputFile), null, null);
		AnalysisSubmission submission = AnalysisSubmission.builder(UUID.randomUUID())
				.inputFiles(ImmutableSet.of(new SingleEndSequenceFile(null))).build();

		submission.setAnalysis(analysis);

		Sample sample = new Sample();
		sample.setId(1L);

		ImmutableMap<MetadataTemplateField, MetadataEntry> metadataMap = ImmutableMap
				.of(new MetadataTemplateField("SISTR Field", "text"), new MetadataEntry("Value1", "text"));
		when(metadataTemplateService.getMetadataMap(any(Map.class))).thenReturn(metadataMap);

		updater.update(Lists.newArrayList(sample), submission);

		ArgumentCaptor<Map> mapCaptor = ArgumentCaptor.forClass(Map.class);

		//this is the important bit.  Ensures the correct values got pulled from the file
		verify(metadataTemplateService).getMetadataMap(mapCaptor.capture());
		Map<String, MetadataEntry> metadata = mapCaptor.getValue();

		int found = 0;
		for (Map.Entry<String, MetadataEntry> e : metadata.entrySet()) {

			if (expectedResults.containsKey(e.getKey())) {
				String expected = expectedResults.get(e.getKey());

				MetadataEntry value = e.getValue();

				assertEquals("metadata values should match", expected, value.getValue());
				found++;
			}
		}
		assertEquals("should have found the same number of results", expectedResults.keySet().size(), found);

		// this bit just ensures the merged data got saved
		verify(sampleService).updateFields(eq(sample.getId()), mapCaptor.capture());
		Map<MetadataTemplateField, MetadataEntry> value = (Map<MetadataTemplateField, MetadataEntry>) mapCaptor
				.getValue().get("metadata");

		assertEquals(metadataMap.keySet().iterator().next(), value.keySet().iterator().next());
	}

	@Test(expected = PostProcessingException.class)
	public void testUpdaterBadFile() throws PostProcessingException, AnalysisAlreadySetException {
		ImmutableMap<String, String> expectedResults = ImmutableMap
				.of("SISTR serovar", "Enteritidis", "SISTR cgMLST Subspecies", "enterica", "SISTR QC Status", "PASS");

		Path outputPath = Paths.get("src/test/resources/files/snp_tree.tree");

		AnalysisOutputFile outputFile = new AnalysisOutputFile(outputPath, null, null, null);

		Analysis analysis = new Analysis(null, ImmutableMap.of("sistr-predictions", outputFile), null, null);
		AnalysisSubmission submission = AnalysisSubmission.builder(UUID.randomUUID())
				.inputFiles(ImmutableSet.of(new SingleEndSequenceFile(null))).build();

		submission.setAnalysis(analysis);

		Sample sample = new Sample();
		sample.setId(1L);

		updater.update(Lists.newArrayList(sample), submission);
	}

	@Test(expected = PostProcessingException.class)
	public void testUpdaterNoFile() throws PostProcessingException, AnalysisAlreadySetException {
		ImmutableMap<String, String> expectedResults = ImmutableMap
				.of("SISTR serovar", "Enteritidis", "SISTR cgMLST Subspecies", "enterica", "SISTR QC Status", "PASS");

		Path outputPath = Paths.get("src/test/resources/files/not_really_a_file.txt");

		AnalysisOutputFile outputFile = new AnalysisOutputFile(outputPath, null, null, null);

		Analysis analysis = new Analysis(null, ImmutableMap.of("sistr-predictions", outputFile), null, null);
		AnalysisSubmission submission = AnalysisSubmission.builder(UUID.randomUUID())
				.inputFiles(ImmutableSet.of(new SingleEndSequenceFile(null))).build();

		submission.setAnalysis(analysis);

		Sample sample = new Sample();
		sample.setId(1L);

		updater.update(Lists.newArrayList(sample), submission);
	}
}
