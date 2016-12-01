package client;

/* This is a modified version of the VLC library control panel.
 * We have added our own properties to suit our program (such as making the buttons non-focusable)
 */

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import server.DataPackages;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.MediaPlayerEventListener;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class CustomControls extends JPanel {
	private static final long serialVersionUID = 1;
	private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
	private final EmbeddedMediaPlayer mediaPlayer;
	private JLabel timeLabel;
	private JLabel maxTime;
	private JSlider positionSlider;
	private JLabel chapterLabel;
	private JButton rewindButton;
	private JButton pauseButton;
	private JButton playButton;
	private JButton fastForwardButton;
	private JButton toggleMuteButton;
	private JSlider volumeSlider;
	private JButton fullScreenButton;
	private JButton nextChapterButton; // new
	private JButton previousChapterButton; // new
	private MEMEClient parentClient;
	private int currentIndex;
	private long time = 0;
	private boolean mousePressedPlaying = false;
	private float positionValue;

	public CustomControls(EmbeddedMediaPlayer mediaPlayer, MEMEClient client) {
		this.mediaPlayer = mediaPlayer;
		this.createUI();
		this.parentClient = client;
		this.executorService.scheduleAtFixedRate(new UpdateRunnable((MediaPlayer) mediaPlayer), 0, 1, TimeUnit.SECONDS);
	}

	private void createUI() {
		this.createControls();
		this.layoutControls();
		this.registerListeners();
	}

	private void createControls() {
		this.timeLabel = new JLabel("hh:mm:ss");
		this.maxTime = new JLabel("hh:mm:ss");
		this.positionSlider = new JSlider();
		this.positionSlider.setMinimum(0);
		this.positionSlider.setMaximum(1000);
		this.positionSlider.setValue(0);
		this.positionSlider.setToolTipText("Position");
		this.positionSlider.setFocusable(false); // added
		this.chapterLabel = new JLabel("00/00");
		this.chapterLabel.setFocusable(false); // added
		this.rewindButton = new JButton();
		this.rewindButton.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("icons/control_rewind_blue.png")));
		this.rewindButton.setToolTipText("Skip back");
		this.rewindButton.setFocusable(false); // added
		this.pauseButton = new JButton();
		this.pauseButton.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("icons/control_pause_blue.png")));
		this.pauseButton.setToolTipText("Play/pause");
		this.pauseButton.setFocusable(false); // added
		this.playButton = new JButton();
		this.playButton.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("icons/control_play_blue.png")));
		this.playButton.setToolTipText("Play");
		this.playButton.setFocusable(false); // added
		this.fastForwardButton = new JButton();
		this.fastForwardButton.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("icons/control_fastforward_blue.png")));
		this.fastForwardButton.setToolTipText("Skip forward");
		this.fastForwardButton.setFocusable(false); // added
		this.toggleMuteButton = new JButton();
		this.toggleMuteButton.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("icons/sound_mute.png")));
		this.toggleMuteButton.setToolTipText("Toggle Mute");
		this.toggleMuteButton.setFocusable(false); // added
		this.volumeSlider = new JSlider();
		this.volumeSlider.setOrientation(0);
		this.volumeSlider.setMinimum(0);
		this.volumeSlider.setMaximum(200);
		this.volumeSlider.setPreferredSize(new Dimension(100, 40));
		this.volumeSlider.setToolTipText("Change volume");
		this.volumeSlider.setFocusable(false); // added
		this.fullScreenButton = new JButton();
		this.fullScreenButton.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("icons/image.png")));
		this.fullScreenButton.setToolTipText("Toggle full-screen");
		this.fullScreenButton.setFocusable(false); // added
		this.previousChapterButton = new JButton();
		this.previousChapterButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/control_start_blue.png")));
		this.previousChapterButton.setFocusable(false); // added
		this.previousChapterButton.setToolTipText("Go to previous chapter");
		this.nextChapterButton = new JButton();
		this.nextChapterButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/control_end_blue.png")));
		this.nextChapterButton.setFocusable(false); // added
		this.nextChapterButton.setToolTipText("Go to next chapter");

	}

	private void layoutControls() {
		this.setBorder(new EmptyBorder(4, 4, 4, 4));
		this.setLayout(new BorderLayout());
		JPanel positionPanel = new JPanel();
		positionPanel.setLayout(new GridLayout(1, 1));
		positionPanel.add(this.positionSlider);
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout(8, 0));
		topPanel.add((Component) this.timeLabel, "West");
		topPanel.add((Component) positionPanel, "Center");
		topPanel.add((Component) this.maxTime, "East");
		this.add((Component) topPanel, "North");
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new FlowLayout());
		bottomPanel.add(this.previousChapterButton);
		bottomPanel.add(this.rewindButton);
		bottomPanel.add(this.pauseButton);
		bottomPanel.add(this.playButton);
		bottomPanel.add(this.fastForwardButton);
		bottomPanel.add(this.nextChapterButton);
		bottomPanel.add(this.volumeSlider);
		bottomPanel.add(this.toggleMuteButton);
		bottomPanel.add(this.fullScreenButton);
		this.add((Component) bottomPanel, "South");
	}

	private void updateUIState() {
		if (!this.mediaPlayer.isPlaying()) {
			this.mediaPlayer.play();
			if (!this.mousePressedPlaying) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				this.mediaPlayer.pause();
			}
		}

		long time = parentClient.getTimeValue();
		if (parentClient.getLength() != 0) {
			double result = ((double) parentClient.getTimeValue() / parentClient.getLength() * 1000f);
			int position = (int) result;
			updatePosition(position);
		}
		int chapter = this.mediaPlayer.getChapter();
		int chapterCount = this.mediaPlayer.getChapterCount();
		this.updateTime(time);
		this.updateChapter(chapter, chapterCount);
		updateChapter(chapter, chapterCount);
	}

	// // UNUSED
	// private void skip(int skipTime) {
	// if (this.mediaPlayer.getLength() > 0) {
	// this.mediaPlayer.skip((long) skipTime);
	// this.updateUIState();
	// }
	// }

	private void sendNewRequest() {
		mediaPlayer.stop();
		try {
			parentClient.sendCommand("STOP");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			parentClient.sendIndexToSocket(currentIndex);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		mediaPlayer.play();
		try {
			parentClient.sendCommand("Play");
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

	private void registerListeners() {
		this.previousChapterButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (parentClient.currentIndex() != 0) {
					currentIndex = parentClient.currentIndex();
					currentIndex--;
				} else {
					currentIndex = parentClient.getVideoListMax() - 1;
				}
				sendNewRequest();
			}
		});

		nextChapterButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Check if the player is currently playing the maximum video
				if (parentClient.currentIndex() != parentClient.getVideoListMax() - 1) {
					currentIndex = parentClient.currentIndex();
					currentIndex++;
				} else {
					currentIndex = 0;
				}
				sendNewRequest();
			}
		});

		this.mediaPlayer.addMediaPlayerEventListener((MediaPlayerEventListener) new MediaPlayerEventAdapter() {

			public void playing(MediaPlayer mediaPlayer) {
				CustomControls.this.updateVolume(mediaPlayer.getVolume());
			}
		});
		this.rewindButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (parentClient.getTimeValue() - 10000 > 0) {
					try {
						parentClient.sendCommand("RWD");
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					CustomControls.this.mediaPlayer.setPosition(parentClient.getTimeValue() - 10000);
				}

				if (parentClient.getTimeValue() - 10000 < 0) {
					try {
						parentClient.sendCommand("Zero");
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					CustomControls.this.mediaPlayer.setPosition(0);
				}
			}
		});
		this.pauseButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				CustomControls.this.mediaPlayer.pause();
				try {
					parentClient.sendCommand("Pause");
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}
		});
		this.playButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				CustomControls.this.mediaPlayer.pause();
				try {
					parentClient.sendCommand("Pause");
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}
		});
		this.fastForwardButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (parentClient.getTimeValue() < parentClient.getLength()) {
					try {
						parentClient.sendCommand("FFW");
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					CustomControls.this.mediaPlayer.setPosition(parentClient.getTimeValue() + 10000);
				}
				if (parentClient.getTimeValue() + 10000 > parentClient.getLength()) {
					try {
						parentClient.sendCommand("Stop");
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					CustomControls.this.positionSlider.setValue(1);
				}
			}
		});
		this.toggleMuteButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				CustomControls.this.mediaPlayer.mute();
			}
		});
		this.volumeSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				CustomControls.this.mediaPlayer.setVolume(source.getValue());
			}
		});
		this.fullScreenButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				parentClient.toggleFullScreen();
				System.out.println("FULLSCREEN");
			}
		});
		this.positionSlider.addMouseListener(new MouseAdapter() {

			public void mousePressed(MouseEvent e) {
				System.out.println("Slider is at a position of :" + positionValue);
				if (CustomControls.this.mediaPlayer.isPlaying()) {
					CustomControls.this.mousePressedPlaying = true;
					CustomControls.this.mediaPlayer.pause();
				} else {
					CustomControls.this.mousePressedPlaying = false;
				}
				setSliderBasedPosition();
			}

			public void mouseReleased(MouseEvent e) {
				CustomControls.this.setSliderBasedPosition();
				CustomControls.this.updateUIState();
			}
		});
	}

	private void setSliderBasedPosition() {
		// if (!this.mediaPlayer.isSeekable()) {
		// return;
		// }
		if (positionValue > 0.99f) {
			positionValue = 0.99f;
		}
		positionValue = positionSlider.getValue() / 1000.0f;
		System.out.println("positionValue" + positionValue);

		// Cast to int to stop serialisable errors
		int tempposVal = (int) (positionValue * 10000);
		DataPackages position = new DataPackages("Position", tempposVal);
		try {
			parentClient.sendPosition(position);
		} catch (IOException e) {
			e.printStackTrace();
		}
		CustomControls.this.mediaPlayer.setPosition(positionValue);

		mediaPlayer.stop();
		mediaPlayer.play();
	}

	private void updateTime(long millis) {
		String s = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis), TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
		this.timeLabel.setText(s);
	}

	private void maxTimeLabel(long millis) {
		String s = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis), TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
		this.timeLabel.setText(s);
		this.maxTime.setText(s);
	}

	private void updatePosition(int position) {
		CustomControls.this.positionSlider.setValue(position);
		positionValue = (float) this.positionSlider.getValue() / 1000.0f;
	}

	private void updateChapter(int chapter, int chapterCount) {
		String s = chapterCount != -1 ? "" + (chapter + 1) + "/" + chapterCount : "-";
		this.chapterLabel.setText(s);
		this.chapterLabel.invalidate();
		this.validate();
	}

	private void updateVolume(int value) {
		this.volumeSlider.setValue(value);
	}

	private final class UpdateRunnable implements Runnable {
		private final MediaPlayer mediaPlayer;

		private UpdateRunnable(MediaPlayer mediaPlayer) {
			this.mediaPlayer = mediaPlayer;
		}

		public void run() {
			final int chapter = this.mediaPlayer.getChapter();
			final int chapterCount = this.mediaPlayer.getChapterCount();
			final long maxTime = parentClient.getLength();
			maxTimeLabel(maxTime);
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					if (UpdateRunnable.this.mediaPlayer.isPlaying()) {
						CustomControls.this.updateTime(time);
						CustomControls.this.updateUIState();
						CustomControls.this.updateChapter(chapter, chapterCount);

						if (parentClient.getLength() != 0) {
							double result = ((double) parentClient.getTimeValue() / parentClient.getLength() * 1000f);
							int position = (int) result;
							updatePosition(position);
						}
					}
				}
			});
		}

	}

}
