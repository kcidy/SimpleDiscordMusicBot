package com.kcidy.discordmusic.event;

import com.kcidy.discordmusic.lavaplayer.GuildMusicManager;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class EventHandler extends ListenerAdapter {

    private final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

    private final Map<Long, GuildMusicManager> musicManagerMap = new HashMap<>();

    private final String music_start , music_unpause , music_pause , music_skip , music_no_matches;


    public EventHandler(@Value("${bot.token}") String token ,
                        @Value("${bot.playing}") String play ,
                        @Value("${bot.message.start}") String music_start ,
                        @Value("${bot.message.unpause}") String music_unpause ,
                        @Value("${bot.message.pause}") String music_pause,
                        @Value("${bot.message.skip}") String music_skip,
                        @Value("${bot.message.no_matches}") String music_no_matches) {
        this.music_no_matches = music_no_matches;
        this.music_skip = music_skip;
        this.music_start = music_start;
        this.music_pause = music_pause;
        this.music_unpause = music_unpause;
        JDABuilder.createLight(token,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_VOICE_STATES)
                .addEventListeners(this)
                .setActivity(Activity.playing(play))
                .build();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    public void onMessageReceived(MessageReceivedEvent event) {

        if(event.getAuthor().isBot()) return;

        final var command = event.getMessage().getContentRaw().split(" ", 2);
        final var channel = event.getChannel();
        final var musicManager = getGuildAudioPlayer(event.getGuild());

        switch(command[0]){
            case "!play" -> playerManager.loadItemOrdered(musicManager, command[1], new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    channel.sendMessage(music_start.replace("%title%" , track.getInfo().title)).queue();

                    play(event.getGuild(), musicManager, track);
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    AudioTrack firstTrack = playlist.getSelectedTrack();

                    if (firstTrack == null) {
                        firstTrack = playlist.getTracks().getFirst();
                    }

                    channel.sendMessage(music_start.replace("%title%" , firstTrack.getInfo().title)).queue();

                    play(event.getGuild(), musicManager, firstTrack);
                }

                @Override
                public void noMatches() {
                    channel.sendMessage(music_no_matches.replace("%title%" , command[1])).queue();
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    channel.sendMessage(exception.getMessage()).queue();
                }
            });
            case "!skip" -> {
                musicManager.scheduler.nextTrack();
                event.getChannel().sendMessage(music_skip).queue();
            }
            case "!pause" -> {
                musicManager.scheduler.onPlayerPause(musicManager.getPlayer());
                event.getChannel().sendMessage(music_pause).queue();
            }
            case "!unpause" -> {
                musicManager.scheduler.onPlayerResume(musicManager.getPlayer());
                event.getChannel().sendMessage(music_unpause).queue();
            }
        }
        super.onMessageReceived(event);
    }


    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());

        GuildMusicManager musicManager = musicManagerMap.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagerMap.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }


    private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track) {
        final var audioManager = guild.getAudioManager();

        if (!audioManager.isConnected()) {
            for (VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
                audioManager.openAudioConnection(voiceChannel);
                break;
            }
        }
        musicManager.scheduler.queue(track);
    }
}
