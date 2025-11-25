package net.rickiekarp.shapass.core.enums

enum class FontType(private val fontFile: String) {
    ALIEN("fonts/alien.otf"),
    ANCIENT("fonts/ancient.ttf"),
    BEHISTUN("fonts/behistun.ttf"),
    DARKARTS("fonts/darkarts.ttf"),
    ENCHANTINGTABLE("fonts/enchantingtable.ttf"),
    JUNGLESLANG("fonts/jungleslang.ttf"),
    LOVECRAFTSDIARY("fonts/lovecraftsdiary.ttf"),
    LUCIUS("fonts/lucius.ttf"),
    MASONIC("fonts/masonic.ttf"),
    MEROITICDEMONIC("fonts/meroiticdemotic.ttf"),
    NYCTOGRAPHIC("fonts/nyctographic.otf"),
    OUTERRIM("fonts/outerrim.otf"),
    RUNE("fonts/rune.ttf"),
    SPACEENCOUNTER("fonts/spaceencounter.ttf"),
    UNIVERSE("fonts/universe.otf"),
    VORLON("fonts/vorlon.ttf"),
    ZODIAC("fonts/zodiac.otf");

    fun getFilePath() = fontFile
}