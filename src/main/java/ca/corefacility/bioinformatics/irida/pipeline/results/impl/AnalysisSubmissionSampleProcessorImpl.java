package ca.corefacility.bioinformatics.irida.pipeline.results.impl;

import ca.corefacility.bioinformatics.irida.exceptions.PostProcessingException;
import ca.corefacility.bioinformatics.irida.model.enums.AnalysisType;
import ca.corefacility.bioinformatics.irida.model.sample.Sample;
import ca.corefacility.bioinformatics.irida.model.workflow.analysis.Analysis;
import ca.corefacility.bioinformatics.irida.model.workflow.submission.AnalysisSubmission;
import ca.corefacility.bioinformatics.irida.pipeline.results.AnalysisSampleUpdater;
import ca.corefacility.bioinformatics.irida.pipeline.results.AnalysisSubmissionSampleProcessor;
import ca.corefacility.bioinformatics.irida.repositories.sample.SampleRepository;
import ca.corefacility.bioinformatics.irida.service.AnalysisSubmissionService;
import ca.corefacility.bioinformatics.irida.service.analysis.annotations.RunAsUser;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Updates samples from an {@link AnalysisSubmission} with results from the
 * analysis.
 */
@Component
public class AnalysisSubmissionSampleProcessorImpl implements AnalysisSubmissionSampleProcessor {

	private static final Logger logger = LoggerFactory.getLogger(AnalysisSubmissionSampleProcessorImpl.class);

	private final Map<AnalysisType, AnalysisSampleUpdater> analysisSampleUpdaterMap;
	private final SampleRepository sampleRepository;
	private final AnalysisSubmissionService analysisSubmissionService;

	/**
	 * Builds a new {@link AnalysisSubmissionSampleProcessorImpl}.
	 *
	 * @param sampleRepository              The {@link SampleRepository}.
	 * @param analysisSubmissionService     The {@link AnalysisSubmissionService}
	 * @param analysisSampleUpdaterServices A list of {@link AnalysisSampleUpdater}s to use for updating
	 *                                      samples.
	 */
	@Autowired
	public AnalysisSubmissionSampleProcessorImpl(SampleRepository sampleRepository, AnalysisSubmissionService analysisSubmissionService,
			List<AnalysisSampleUpdater> analysisSampleUpdaterServices) {
		checkNotNull(analysisSampleUpdaterServices, "assemblySampleUpdaterService is null");
		this.sampleRepository = sampleRepository;
		this.analysisSampleUpdaterMap = Maps.newHashMap();
		this.analysisSubmissionService = analysisSubmissionService;

		for (AnalysisSampleUpdater analysisSampleUpdaterService : analysisSampleUpdaterServices) {
			AnalysisType analysisType = analysisSampleUpdaterService.getAnalysisType();
			checkArgument(!analysisSampleUpdaterMap.containsKey(analysisType),
					"Error: already have registered " + analysisSampleUpdaterService.getClass() + " for AnalysisType " + analysisType);

			analysisSampleUpdaterMap.put(analysisSampleUpdaterService.getAnalysisType(), analysisSampleUpdaterService);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasRegisteredAnalysisSampleUpdater(AnalysisType analysisType) {
		return analysisSampleUpdaterMap.keySet().contains(analysisType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@PreAuthorize("hasPermission(#analysisSubmission, 'canUpdateSamplesFromAnalysisSubmission')")
	public void updateSamples(AnalysisSubmission analysisSubmission) throws PostProcessingException {

		logger.debug("Updating sample from results for submission=" + analysisSubmission);

		Set<Sample> samples = sampleRepository.findSamplesForAnalysisSubmission(analysisSubmission);
		Analysis analysis = analysisSubmission.getAnalysis();

		checkNotNull(analysis, "No analysis associated with submission " + analysisSubmission);
		checkNotNull(samples, "No samples associated with submission " + analysisSubmission);

		AnalysisSampleUpdater analysisSampleUpdaterService = analysisSampleUpdaterMap.get(analysis.getAnalysisType());

		if (analysisSampleUpdaterService != null) {
			// re-reading submission to ensure file paths are correct
			analysisSubmission = analysisSubmissionService.read(analysisSubmission.getId());

			analysisSampleUpdaterService.update(samples, analysisSubmission);
		} else {
			logger.debug(
					"No associated object for updating samples for analysis of type " + analysis.getAnalysisType());
		}

	}
}
