package dev.iustin.blockcraft;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/** Persistent user preferences for graphics, controls, language, and accessibility. */
public final class GameConfig {
    private static final Path FILE=Path.of(System.getenv().getOrDefault("LOCALAPPDATA",System.getProperty("user.home")),"BlockCraft","settings.properties");
    public String graphics="HIGH",language="en",forward="W",back="S",left="A",right="D";
    public int renderDistance=22,targetFps=120;
    public boolean vsync=true,fullscreen=false,subtitles=true;
    public void load(){if(!Files.isRegularFile(FILE))return;Properties p=new Properties();try(var in=Files.newInputStream(FILE)){p.load(in);graphics=p.getProperty("graphics",graphics);language=p.getProperty("language",language);renderDistance=Integer.parseInt(p.getProperty("renderDistance","22"));targetFps=Integer.parseInt(p.getProperty("targetFps","120"));vsync=Boolean.parseBoolean(p.getProperty("vsync","true"));fullscreen=Boolean.parseBoolean(p.getProperty("fullscreen","false"));subtitles=Boolean.parseBoolean(p.getProperty("subtitles","true"));}catch(Exception ignored){}}
    public void save(){Properties p=new Properties();p.setProperty("graphics",graphics);p.setProperty("language",language);p.setProperty("renderDistance",Integer.toString(renderDistance));p.setProperty("targetFps",Integer.toString(targetFps));p.setProperty("vsync",Boolean.toString(vsync));p.setProperty("fullscreen",Boolean.toString(fullscreen));p.setProperty("subtitles",Boolean.toString(subtitles));try{Files.createDirectories(FILE.getParent());try(var out=Files.newOutputStream(FILE)){p.store(out,"BlockCraft settings");}}catch(IOException e){throw new UncheckedIOException(e);}}
}