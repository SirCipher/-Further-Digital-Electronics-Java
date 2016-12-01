package server;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import client.MEMEClient;

public class MEMEServerTest {
	private MEMEServer server;
	private MEMEClient client;

	/* set up a server before tests */
	@Before
	public void setUp() throws Exception {
		server = new MEMEServer();
		client = new MEMEClient();
	}

	/* check the server has access to the video files */
	@Test
	public void videoFilesPresent() {

		File video0 = new File("video/monstersinc_high.mpg");
		assertTrue(video0.exists());

		File video1 = new File("video/avengers-featurehp.mp4");
		assertTrue(video1.exists());

		File video2 = new File("video/prometheus-featureukFhp.mp4");
		assertTrue(video2.exists());
	}
	
	/*
	 * The fact the client checks the components of the list it receives, there
	 * is no need to also check that the server gets the right information from
	 * the XML file
	 */
	@Test
	public void threadTest(){
		assertTrue(server.listen.isAlive());
		assertTrue(server.socketThread.isAlive());
	}

	/* Test for presence and type of relevant variables */
	@Test
	public void presenceOfVariables() {
		assertTrue(server.typelessVideoList instanceof DataPackages);
		assertTrue(server.videoList instanceof List);
		assertTrue(server.serverAddress.equals("127.0.0.1"));
		assertTrue(!server.start);
		assertTrue(server.sendLength);
		assertTrue(!server.shutdown);
	}
	
	/* Checks the sockets are open and connect to the client */
	@Test
	public void checkPortAvailability() {
		assertTrue(!server.clientSocket.isClosed());
		assertTrue(server.clientSocket.isConnected());
		assertTrue(!server.serverSocket.isClosed());
	}
	
	/* Asserts that the input/output streams are present */
	@Test
	public void checkStreams() {
		assertTrue(!server.outputToClient.equals(null));
		assertTrue(!server.inputFromClient.equals(null));		
	}
	
	/* Checks that the video list is not empty */
	@Test
	public void videoListPopulated() {
		assertTrue(!server.videoList.isEmpty());
	}

	/* check the media player is correct created, and plays */
	@Test
	public void mediaPlayerCreated() {
		client.getButton1().doClick();
		while(server.mediaPlayer.getTime() < 1){}
		assertTrue(server.mediaPlayer.getTime() > 1);
		server.mediaPlayer.stop();
	}

	/* closes down the server after tests are complete */
	@After
	public void shutdownAfterTests() throws IOException {
		client.shutdownClient();
		server.shutdownServer();
	}

}
