package ca.corefacility.bioinformatics.irida.service.impl.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;

import javax.validation.Validator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.task.TaskExecutor;

import ca.corefacility.bioinformatics.irida.exceptions.EntityNotFoundException;
import ca.corefacility.bioinformatics.irida.model.sample.Sample;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.SequenceFile;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.SequenceFilePair;
import ca.corefacility.bioinformatics.irida.processing.FileProcessingChain;
import ca.corefacility.bioinformatics.irida.repositories.joins.sample.SampleSequenceFileJoinRepository;
import ca.corefacility.bioinformatics.irida.repositories.sequencefile.SequenceFilePairRepository;
import ca.corefacility.bioinformatics.irida.repositories.sequencefile.SequenceFileRepository;
import ca.corefacility.bioinformatics.irida.service.SequenceFileService;
import ca.corefacility.bioinformatics.irida.service.impl.SequenceFileServiceImpl;

/**
 * Test the behaviour of {@link SequenceFileService}.
 * 
 * @author Franklin Bristow <franklin.bristow@phac-aspc.gc.ca>
 * @author Thomas Matthews <thomas.matthews@phac-aspc.gc.ca>
 *
 */
public class SequenceFileServiceTest {
	private SequenceFileService sequenceFileService;
	private SampleSequenceFileJoinRepository ssfRepository;
	private SequenceFileRepository sequenceFileRepository;
	private SequenceFilePairRepository pairRepository;
	private TaskExecutor executor;
	private FileProcessingChain fileProcessingChain;
	private Validator validator;

	@Before
	public void setUp() {
		this.ssfRepository = mock(SampleSequenceFileJoinRepository.class);
		this.sequenceFileRepository = mock(SequenceFileRepository.class);
		pairRepository = mock(SequenceFilePairRepository.class);
		this.executor = mock(TaskExecutor.class);
		this.fileProcessingChain = mock(FileProcessingChain.class);
		this.validator = mock(Validator.class);
		this.sequenceFileService = new SequenceFileServiceImpl(sequenceFileRepository, ssfRepository, pairRepository,
				executor, fileProcessingChain, validator);
	}

	@Test
	public void testCreateSequenceFileInSample() {
		Sample s = new Sample();
		SequenceFile sf = new SequenceFile();

		when(sequenceFileRepository.save(sf)).thenReturn(sf);

		sequenceFileService.createSequenceFileInSample(sf, s);

		// verify that we're only actually running one file processor on the new
		// sequence file.
		verify(executor, times(1)).execute(any(Runnable.class));
	}

	@Test
	public void testGetPairForSequenceFile() {
		SequenceFile file = new SequenceFile(Paths.get("/file1"));
		SequenceFile pairFile = new SequenceFile(Paths.get("/file2"));
		SequenceFilePair pair = new SequenceFilePair(file, pairFile);

		when(pairRepository.getPairForSequenceFile(file)).thenReturn(pair);

		SequenceFile pairForSequenceFile = sequenceFileService.getPairForSequenceFile(file);

		assertEquals(pairFile, pairForSequenceFile);
		verify(pairRepository).getPairForSequenceFile(file);
	}

	@Test(expected = EntityNotFoundException.class)
	public void testGetPairForSequenceFileNotExists() {
		SequenceFile file = new SequenceFile(Paths.get("/file1"));

		when(pairRepository.getPairForSequenceFile(file)).thenReturn(null);

		sequenceFileService.getPairForSequenceFile(file);
	}

	@Test
	public void testCreateSequenceFilePair() {
		SequenceFile file1 = new SequenceFile(Paths.get("/file1"));
		SequenceFile file2 = new SequenceFile(Paths.get("/file2"));

		sequenceFileService.createSequenceFilePair(file1, file2);

		ArgumentCaptor<SequenceFilePair> pairCaptor = ArgumentCaptor.forClass(SequenceFilePair.class);
		verify(pairRepository).save(pairCaptor.capture());
		SequenceFilePair pair = pairCaptor.getValue();

		assertTrue(pair.getFiles().contains(file1));
		assertTrue(pair.getFiles().contains(file2));
	}
}
