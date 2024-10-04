# Music Bot

A simple music bot for Discord written in Java. This bot allows users to control music playback using commands. It supports the following commands: `!play`, `!skip`, `!pause`, and `!unpause`. Configuration is done through the `application.configuration` file.

## Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/kcidy/SimpleDiscordMusicBot.git
   cd SimpleDiscordMusicBot
   ```

2. Make sure you have [Maven](https://maven.apache.org/) installed. Build the project using:

   ```bash
   mvn clean install
   ```

3. Configure the `application.configuration` file:

   ```properties
  bot.token=your_bot_token
  bot.playing=music
  bot.message.start=music start %title%
  bot.message.pause=music pause
  bot.message.unpause=music unpause
  bot.message.skip=music skip
  bot.message.no_matches=music no matches
   ```

   Replace `your_bot_token` with your Discord bot token.

## Commands

- `!play <URL>`: Play music from the specified URL (e.g., a YouTube link).
- `!skip`: Skip the current song.
- `!pause`: Pause the current playback.
- `!unpause`: Resume playback.

## Running the Bot

Run the bot using the following command:

```bash
java -jar target/your-bot.jar
```

Make sure to replace `your-bot.jar` with the actual name of the built JAR file.

## Notes

- Ensure that you have the necessary permissions to manage voice channels on your Discord server.
- The bot uses the [JDA (Java Discord API)](https://github.com/discord-jda/JDA) library to interact with the Discord API.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contributing

If you would like to contribute to this project, please fork the repository, make your changes, and submit a pull request.

---

If you have any questions or suggestions, feel free to open an issue in the repository!
