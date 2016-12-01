package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.headless.HeadlessMediaPlayer;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

public class MEMEServer {
	public List<VideoFile>		videoList;
	public DataPackages			typelessVideoList	= new DataPackages("List");
	public ServerSocket			serverSocket;
	public Socket				clientSocket;
	private int					port				= 1192;
	public ObjectOutputStream	outputToClient;
	public ObjectInputStream	inputFromClient;
	private int					videoIndex; //
	private String				media				= null;
	public MediaPlayerFactory	mediaPlayerFactory;
	public HeadlessMediaPlayer	mediaPlayer;
	public String				serverAddress;
	private String				options;
	public boolean				start				= false;
	public boolean				sendLength			= true;
	public boolean				shutdown			= false;
	private Timer				timeTimer;
	public Thread				listen, socketThread;

	/* Main method simply runs the contructor to set up the server */
	public static void main(String[] args) {
		System.out.println("Server: Starting server");
		new MEMEServer();
	}

	/*
	 * Reads the XML file, Sets up a 'listen' thread which continuously receives
	 * the client-userinput for selected index, Sets up 'socket' thread which
	 * opens the socket with the client, and after the first selection
	 * continuously streams and sends timing information updates to the client
	 */

	public MEMEServer() {
		System.out.println("Server: Building XML reader and getting list.");
		XMLReader reader = new XMLReader();
		videoList = reader.getList("videoList.xml");
		typelessVideoList.setObject(videoList);

		listen = new Thread("Index receiver") {
			public void run() {
				System.out.print("Server: Starting thread: Listen\n");
				while (!shutdown) {
					try {
						getDataFromSocket();
					} catch (ClassNotFoundException e) {
						// Break the loop and shut down the thread, once the request
						// to shutdown is received
						break;
					} catch (IOException e) {
						// Close quietly. This exception is handled 
						break;
					}
				}

				// try {
				// shutdownServer();
				// } catch (IOException e) {
				// e.printStackTrace();
				// }
				System.out.println("Server: Exiting index receiver thread");
			}
		};

		// Set up and play video, thread
		socketThread = new Thread("Socket") {
			public void run() {
				System.out.print("Server: Starting thread: Socket");
				// Open the socket and set up the video player
				try {
					System.out.println("Server: Server: Opening socket.");
					openSocket();
					System.out.println("Server: Server: Writing list to socket.");
					writeDataToSocket(typelessVideoList);
					System.out.println("Server: List written to client.");
				} catch (IOException e) {
					System.out.println("Server: ERROR on socket connection.");
					e.printStackTrace();
				}
				setupPlayer();
				// Once this thread has retreived the video list, start another to just listen
				listen.start();

				while (!shutdown) {
					if (start) {
						start = false;
						playMedia();
						// Send the time to the client
						updateInfo();
					}
					if (mediaPlayer.getTime() > 1 && sendLength && shutdown != true) {
						long time = (long) mediaPlayer.getLength();
						DataPackages data = new DataPackages("Length", time);
						try {
							outputToClient.writeObject(data);
							System.out.println("Server: File length is : " + time);
						} catch (IOException e1) {
							break;
						}
						// Don't come back into this loop until the video is changed
						sendLength = false;
					}
				}
				System.out.println("Server: Exiting Socket thread");
			}
		};
		socketThread.start();

	}

	/*
	 * Sends timing information to the client periodically so the client can
	 * update the position of the slider
	 */
	private void updateInfo() {
		timeTimer = new Timer();
		timeTimer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				// We only run this once, once the video is actually loaded and playing
				if (mediaPlayer.getTime() > 1 && mediaPlayer.isPlaying()) {
					long time = (long) mediaPlayer.getTime();
					DataPackages data = new DataPackages("TimeStamp", time);
					try {
						outputToClient.writeObject(data);
					} catch (IOException e1) {
						timeTimer.cancel();
					}
				}
			}
		}, 0, 500);
	}

	/*
	 * Encapsulates the mediaPlayer.playMedia method for simple execution and
	 * readability
	 */
	private void playMedia() {
		mediaPlayer.playMedia(media, options, ":no-sout-rtp-sap", ":no-sout-standard-sap", ":sout-all", ":sout-keep");
	}

	/* Encapsulates the outputtoclient for simple execution and readability */
	private void writeDataToSocket(DataPackages transmit) throws IOException {
		outputToClient.writeObject(transmit);
	}

	/* Opens a socket with the client */
	private void openSocket() throws IOException {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("Server: Could not listen on port : " + port);
			System.exit(-1);
		}
		System.out.println("Server: Opened socket on : " + port + ", waiting for client.");

		try {
			// Create the connection
			clientSocket = serverSocket.accept();
		} catch (IOException e) {
			System.out.println("Server: Could not accept client.");
			System.exit(-1);
		}

		outputToClient = new ObjectOutputStream(clientSocket.getOutputStream());
		inputFromClient = new ObjectInputStream(clientSocket.getInputStream());

	}

	/* Get the index from the clients selection */
	private void getDataFromSocket() throws IOException, ClassNotFoundException {

		DataPackages data = (DataPackages) inputFromClient.readObject();
		if (data.getType().equals("Index")) {
			videoIndex = (int) data.getObject();
			System.out.println("Received video index " + videoIndex);
			if (videoIndex >= 0) {
				Object filename = videoList.get(videoIndex).getFilename();
				media = "video/" + (String) filename;
				System.out.println("Server: Server received " + media);
			}
			mediaPlayer.stop();
			// Start playing the new video
			start = true;
			// Send the length of the video to the client
			sendLength = true;
		}
		if (data.getType().equals("SHUTDOWN")) {
			shutdown = true;
			shutdownServer();
		}
		if (data.getType().equals("Position")) {
			float temp = (int) data.getObject();
			// Convert the position to a time. 
			// Divide by 10000, to retreive the original value 
			float tempTime = (temp / 10000);
			// Convert to a time 
			float newTime = mediaPlayer.getLength() * tempTime;
			System.out.println("Server setting a new position of : " + newTime);
			mediaPlayer.setTime((long) newTime);
		}
		if (data.getType().equals("NewTime")) {
			long newTime = (long) data.getObject();
			System.out.println("Server setting a new time of : " + newTime);
			mediaPlayer.setPosition(newTime);
		}
		if (data.getType().equals("Pause")) {
			mediaPlayer.pause();
		}
		if (data.getType().equals("STOP")) {
			mediaPlayer.stop();
		}
		if (data.getType().equals("Play")) {
			mediaPlayer.play();
		}
		if (data.getType().equals("FFW")) {
			mediaPlayer.skip(10000);
		}
		if (data.getType().equals("RWD")) {
			mediaPlayer.skip(-10000);
		}
		if (data.getType().equals("Zero")) {
			mediaPlayer.setPosition(0);
		}

	}

	/* Close server and Tidy Up Resources */
	public void shutdownServer() throws IOException {
		// if (mediaPlayer != null) {
		// mediaPlayer.stop();
		// mediaPlayer.release();
		// mediaPlayerFactory.release();
		// }
		try {
			clientSocket.close();
			serverSocket.close();
			System.out.println("Server: Sockets closed successfully");
		} catch (Throwable e) {
			e.printStackTrace();
		}

		// Doens't happen if the timer hasn't been created
		if (timeTimer != null) {
			timeTimer.cancel();
		}
		// mediaPlayer.release();
		// System.exit(-1);
		if (listen.isAlive())
			System.out.println("server listen alive");
		if (socketThread.isAlive())
			System.out.println("socketThread listen alive");
	}

	/* Sets up the server-side (headless) media player */
	private void setupPlayer() {
		String vlcLibraryPath = "VLC/vlc-2.0.1";
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), vlcLibraryPath);
		Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
		mediaPlayerFactory = new MediaPlayerFactory();
		mediaPlayer = mediaPlayerFactory.newHeadlessMediaPlayer();
		serverAddress = "127.0.0.1";
		options = formatRtpStream(serverAddress, 5555);
	}

	/* Sets some options for streaming */
	private String formatRtpStream(String serverAddress, int serverPort) {
		StringBuilder sb = new StringBuilder(60);
		sb.append(":sout=#rtp{dst=");
		sb.append(serverAddress);
		sb.append(",port=");
		sb.append(serverPort);
		sb.append(",mux=ts}");
		return sb.toString();
	}

	// Returns the current position in the video
	public long getTime() {
		return mediaPlayer.getTime();
	}
}