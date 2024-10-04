package com.kcidy.discordmusic.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class TrackScheduler extends AudioEventAdapter {
  private final AudioPlayer player;
  private final BlockingQueue<AudioTrack> queueList;

  public TrackScheduler(AudioPlayer player) {
    this.player = player;
    this.queueList = new LinkedBlockingQueue<>();
  }


  public void queue(AudioTrack track) {
    if (!player.startTrack(track, true)) {
      queueList.offer(track);
    }
  }


  public void nextTrack() {
    player.startTrack(queueList.poll(), false);
  }

  @Override
  public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
    if (endReason.mayStartNext) {
      nextTrack();
    }
  }

  @Override
  public void onPlayerPause(AudioPlayer player){
    if(!player.isPaused()){
      player.setPaused(true);
    }
  }


  public void onPlayerResume(AudioPlayer player){
    if(player.isPaused()){
      player.setPaused(false);
    }
  }
}


