package client;

import static org.junit.Assert.*;

import javax.swing.JOptionPane;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import server.MEMEServer;
import server.VideoFile;

public class MEMEClientTest {
	private MEMEClient client;
	private MEMEServer server;

	/*
	 * Set up THE server BEFORE setting up an instance of client
	 */
	@Before
	public void setUp() throws Exception {
		server = new MEMEServer();
		client = new MEMEClient();
	}
	
	/* Checks that the videoList received is not empty. If not, it means the
	 * server has delivered the list.
	 */
	@Test
	public void displayNoVideosAvailable() {

		System.out.println("CLIENT TEST 1: \n");
		assertFalse(client.videoList.isEmpty());
		System.out.println("CLIENT TEST 1 COMPLETE. \n");
	}
	
	/* Checks that the videoList contains the right data for all 3 films */
	@Test
	public void videoFileReturnsCorrectValue() {

		System.out.println("CLIENT TEST 2: \n");

		VideoFile videoFile0 = client.videoList.get(0);
		assertEquals("20120213a2", videoFile0.getID());
		assertEquals("Monsters Inc.", videoFile0.getTitle());
		assertEquals("monstersinc_high.mpg", videoFile0.getFilename());

		VideoFile videoFile1 = client.videoList.get(1);
		assertEquals("20120102b7", videoFile1.getID());
		assertEquals("Avengers", videoFile1.getTitle());
		assertEquals("avengers-featurehp.mp4", videoFile1.getFilename());

		VideoFile videoFile2 = client.videoList.get(2);
		assertEquals("20120102b4", videoFile2.getID());
		assertEquals("Prometheus", videoFile2.getTitle());
		assertEquals("prometheus-featureukFhp.mp4", videoFile2.getFilename());

		System.out.println("CLIENT TEST 2 COMPLETE. \n");
	}

	/* Successive tests to determine media player functionality.
	 * They work in a specific order i.e. the first check is to see
	 * if the GUI is visible- you can't test anything else if that
	 * isn't visible.
	 * I.e. it tests to see if a video plays before asking about the
	 * audio, and the video control. etc.
	 * The user is given an amount of time to for each test case.
	 */
	@Test
	public void userTests() throws InterruptedException {
		
		int time;
		
		/* GUI visible check */
		int guiVisible = JOptionPane.showConfirmDialog(null, "Are the following visible? \nControl Bar, Video List, and Option Bar.");
		if (guiVisible != 0) {
			System.out.println("TEST FAILED: GUI not visible");
		}
		assertTrue(guiVisible == 0);

		/* video playing check */
		time = 8000;
		JOptionPane.showMessageDialog(null, "Select a video from the list on the right: (you will have " + time/1000 + "seconds)");
		Thread.sleep(time);
		int videoPlaying = JOptionPane.showConfirmDialog(null, "Did Video Play?");
		if (videoPlaying != 0) {
			System.out.println("TEST FAILED: Video did not play");
		}
		assertTrue(videoPlaying == 0); // if the user selects "yes" then the above will return '0'

		/* audio checks */
		int audioPlaying = JOptionPane.showConfirmDialog(null, "Is there audio?");
		if (audioPlaying != 0) {
			System.out.println("TEST FAILED: Audio not playing");
		}
		assertTrue(audioPlaying == 0);
		
		time = 10000;
		JOptionPane.showMessageDialog(null, "Adjust audio volume and try muting and un-muting the audio. (you will have " + time/1000 + "seconds)");
		Thread.sleep(time);
		int audioControl = JOptionPane.showConfirmDialog(null, "Did the audio controls work?");
		if (audioControl != 0) {
			System.out.println("TEST FAILED: Audio controls not working");
		}
		assertTrue(audioControl == 0);

		/* Play, pause, fast-forward, rewind tests */
		JOptionPane.showMessageDialog(null, "Try Playing/pausing (also try spacebar). \n Also try fast-forwarding and re-winding. \n Expect some slight buffer latency issues. (you will have " + time/1000 + "seconds)");
		Thread.sleep(time);
		int playPauseFFRWD = JOptionPane.showConfirmDialog(null, "Did the play/pause and fastforward/rewind controls work?");
		if (playPauseFFRWD != 0) {
			System.out.println("TEST FAILED: Play/pause or fastforward/rewind not working");
		}
		assertTrue(playPauseFFRWD == 0);

		/* Skip forward and skip back tests */
		time = 18000;
		JOptionPane.showMessageDialog(null, "Next, try skipping forward and back. \n Also, check that skipping forward at the end of the list results in the first video being played , and vice versa. (you will have " + time/1000 + "seconds)");
		Thread.sleep(time);
		int skipButtons = JOptionPane.showConfirmDialog(null, "Did the skipping controls work?");
		if (skipButtons != 0) {
			System.out.println("TEST FAILED: Skip forward/back buttons not working");
		}
		assertTrue(skipButtons == 0);

		/* Option bar tests */
		time = 10000;
		JOptionPane.showMessageDialog(null, "Test the two options within the dropdown at the top of the screen. \n These should show/hide the two components of the GUI. (you will have " + time/1000 + "seconds)");
		Thread.sleep(time);
		int optionBar = JOptionPane.showConfirmDialog(null, "Did the Option Bar toggles work?");
		if (optionBar != 0) {
			System.out.println("TEST FAILED: Option bar is faulty.");
		}
		assertTrue(optionBar == 0);

		/* fullscreen tests */
		JOptionPane.showMessageDialog(null, "Almost there! \nTest the fullscreen functionality. The 'escape' key should exit fullscreen. (you will have " + time/1000 + "seconds)");
		Thread.sleep(time);
		int fullscreenTest = JOptionPane.showConfirmDialog(null, "Does fullscreen functionality work?");
		if (fullscreenTest != 0) {
			System.out.println("TEST FAILED: Fullscreen not functional");
		}
		assertTrue(fullscreenTest == 0);

		/* slider tests */
		time = 15000;
		JOptionPane.showMessageDialog(null, "Final Test! \nDoes the slider bar track along with the video? \nAlso, can you use the slider bar to navigate through the video? \nExpect slight buffering issues. (you will have " + time/1000 + "seconds)");
		Thread.sleep(time);
		int sliderTest = JOptionPane.showConfirmDialog(null, "Does slider bar work?");
		if (sliderTest != 0) {
			System.out.println("TEST FAILED: Slider bar faulty.");
		}
		assertTrue(sliderTest == 0);
	}
	
	
	/* Tests that the server listen thread is alive */
	@Test
	public void threadTest(){
		assertTrue(client.listen.isAlive());
	}
	
	/* Checks that the server socket is open, and is connected */
	@Test
	public void checkPortAvailability() {
		assertTrue(!client.serverSocket.isClosed());
		assertTrue(client.serverSocket.isConnected());
	}
	
	/* Checks the input and output streams */
	@Test
	public void checkStreams() {
		assertTrue(!client.inputFromServer.equals(null));
		assertTrue(!client.outputToServer.equals(null));		
	}
	
	/* Checks that the current time on the client is the 
	 * same as the current time on the server
	 */
	@Test
	public void checkTimeSync(){
		client.getButton1().doClick();
		while(client.getTimeValue() < 1){}
		assertTrue(server.getTime() == client.getTimeValue());
	}

	/* closes down the client and server after tests are complete */
	@After
	public void shutdownAfterTests() {
		client.shutdownClient();
	}
}