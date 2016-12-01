package server;

import static org.junit.Assert.*;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class XMLReaderTest {
	private XMLReader reader;
	private List<VideoFile> videoList;

	@Before
	public void setUp() throws Exception {
		reader = new XMLReader();
		videoList = reader.getList("videoList.xml"); 
	}

	@Test
	public void createListOfVideos() {
		assertTrue(videoList instanceof List);
	}
	
	@Test
	public void listContainsVideoFiles() {
		assertTrue(videoList.get(0) instanceof VideoFile);
	 }
	
	@Test
	public void videoFileReturnsCorrectFields() {
		VideoFile videoFile = videoList.get(0);
		assertNotNull(videoFile.getID());
		assertNotNull(videoFile.getTitle());
		assertNotNull(videoFile.getFilename());
	 }
	
	@Test
	public void getMethodsReturnCorrectDataSets() {
		VideoFile videoFile = videoList.get(0);
		assertTrue(videoFile.getID().equals("20120213a2"));
		assertTrue(videoFile.getTitle().equals("Monsters Inc."));
		assertTrue(videoFile.getFilename().equals("monstersinc_high.mpg"));
	}
}
