# Custom Font

To enable the custom font, drop a TTF file named `onefizz.ttf` into this folder.

Recommended fonts (free, SIL OFL):
- Inter — https://rsms.me/inter/ — clean modern sans
- JetBrains Mono — https://www.jetbrains.com/lp/mono/ — monospaced
- Roboto — https://fonts.google.com/specimen/Roboto

If `onefizz.ttf` is missing the GUI will fall back to Minecraft's default font.

The font is registered as the identifier `onefizz:onefizz` and used by the GUI
via `Text.literal(...).styled(s -> s.withFont(Identifier.of("onefizz", "onefizz")))`.
