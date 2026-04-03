package audio;

import java.io.IOException;
import java.net.URL;
import java.util.Random;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioPlayer {

	public static int MENU_1 = 0;
	public static int LEVEL_1 = 1;
	public static int LEVEL_2 = 2;

	public static int DIE = 0;
	public static int JUMP = 1;
	public static int GAMEOVER = 2;
	public static int LVL_COMPLETED = 3;
	public static int ATTACK_ONE = 4;
	public static int ATTACK_TWO = 5;
	public static int ATTACK_THREE = 6;

	private Clip[] songs, effects;
	private int currentSongId;
	private float volume = 0.5f;
	private boolean songMute, effectMute;
	private Random rand = new Random();
	private boolean audioAvailable = true;

	public AudioPlayer() {
		System.out.println("NEW AUDIOPLAYER VERSION LOADED");

		try {
			loadSongs();
			loadEffects();

			if (audioAvailable && songs != null && songs[MENU_1] != null) {
				playSong(MENU_1);
			} else {
				System.out.println("Audio is unavailable. Running game without sound.");
			}
		} catch (Exception e) {
			System.out.println("Audio initialization failed. Running without sound.");
			audioAvailable = false;
		}
	}

	private void loadSongs() {
		String[] names = { "menu", "level1", "level2" };
		songs = new Clip[names.length];
		for (int i = 0; i < songs.length; i++) {
			songs[i] = getClip(names[i]);
		}
	}

	private void loadEffects() {
		String[] effectNames = { "die", "jump", "gameover", "lvlcompleted", "attack1", "attack2", "attack3" };
		effects = new Clip[effectNames.length];
		for (int i = 0; i < effects.length; i++) {
			effects[i] = getClip(effectNames[i]);
		}

		if (audioAvailable) {
			updateEffectsVolume();
		}
	}

	private Clip getClip(String name) {
		URL url = getClass().getResource("/audio/" + name + ".wav");

		if (url == null) {
			System.out.println("Missing audio resource: " + name);
			audioAvailable = false;
			return null;
		}

		try {
			AudioInputStream audio = AudioSystem.getAudioInputStream(url);
			Clip c = AudioSystem.getClip();
			c.open(audio);
			return c;

		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException | IllegalArgumentException e) {
			System.out.println("Could not load audio clip: " + name + " (" + e.getMessage() + ")");
			audioAvailable = false;
			return null;
		}
	}

	public void setVolume(float volume) {
		this.volume = volume;
		if (!audioAvailable)
			return;
		updateSongVolume();
		updateEffectsVolume();
	}

	public void stopSong() {
		if (!audioAvailable || songs == null || currentSongId < 0 || currentSongId >= songs.length || songs[currentSongId] == null)
			return;

		if (songs[currentSongId].isActive())
			songs[currentSongId].stop();
	}

	public void setLevelSong(int lvlIndex) {
		if (!audioAvailable)
			return;

		if (lvlIndex % 2 == 0)
			playSong(LEVEL_1);
		else
			playSong(LEVEL_2);
	}

	public void lvlCompleted() {
		if (!audioAvailable)
			return;

		stopSong();
		playEffect(LVL_COMPLETED);
	}

	public void playAttackSound() {
		if (!audioAvailable)
			return;

		int start = 4;
		start += rand.nextInt(3);
		playEffect(start);
	}

	public void playEffect(int effect) {
		if (!audioAvailable || effects == null || effect < 0 || effect >= effects.length || effects[effect] == null)
			return;

		if (effects[effect].getMicrosecondPosition() > 0)
			effects[effect].setMicrosecondPosition(0);
		effects[effect].start();
	}

	public void playSong(int song) {
		if (!audioAvailable || songs == null || song < 0 || song >= songs.length || songs[song] == null)
			return;

		stopSong();

		currentSongId = song;
		updateSongVolume();
		songs[currentSongId].setMicrosecondPosition(0);
		songs[currentSongId].loop(Clip.LOOP_CONTINUOUSLY);
	}

	public void toggleSongMute() {
		if (!audioAvailable || songs == null)
			return;

		this.songMute = !songMute;
		for (Clip c : songs) {
			if (c == null)
				continue;
			BooleanControl booleanControl = (BooleanControl) c.getControl(BooleanControl.Type.MUTE);
			booleanControl.setValue(songMute);
		}
	}

	public void toggleEffectMute() {
		if (!audioAvailable || effects == null)
			return;

		this.effectMute = !effectMute;
		for (Clip c : effects) {
			if (c == null)
				continue;
			BooleanControl booleanControl = (BooleanControl) c.getControl(BooleanControl.Type.MUTE);
			booleanControl.setValue(effectMute);
		}
		if (!effectMute)
			playEffect(JUMP);
	}

	private void updateSongVolume() {
		if (!audioAvailable || songs == null || currentSongId < 0 || currentSongId >= songs.length || songs[currentSongId] == null)
			return;

		FloatControl gainControl = (FloatControl) songs[currentSongId].getControl(FloatControl.Type.MASTER_GAIN);
		float range = gainControl.getMaximum() - gainControl.getMinimum();
		float gain = (range * volume) + gainControl.getMinimum();
		gainControl.setValue(gain);
	}

	private void updateEffectsVolume() {
		if (!audioAvailable || effects == null)
			return;

		for (Clip c : effects) {
			if (c == null)
				continue;
			FloatControl gainControl = (FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN);
			float range = gainControl.getMaximum() - gainControl.getMinimum();
			float gain = (range * volume) + gainControl.getMinimum();
			gainControl.setValue(gain);
		}
	}
}