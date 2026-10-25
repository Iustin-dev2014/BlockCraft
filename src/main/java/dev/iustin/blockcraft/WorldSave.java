package dev.iustin.blockcraft;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Versioned local save data for the desktop edition. */
public final class WorldSave {
    private static final int VERSION=1;
    private WorldSave() {}
    public static Path directory() {
        String base=System.getenv("LOCALAPPDATA");
        if(base==null||base.isBlank())base=System.getProperty("user.home");
        return Path.of(base,"BlockCraft","worlds");
    }
    public static void save(String worldName,float x,float z,float yaw,float pitch,int selected,Map<String,Integer> edits) throws IOException {
        Files.createDirectories(directory());
        Properties p=new Properties();p.setProperty("version",Integer.toString(VERSION));p.setProperty("name",worldName);
        p.setProperty("x",Float.toString(x));p.setProperty("z",Float.toString(z));p.setProperty("yaw",Float.toString(yaw));p.setProperty("pitch",Float.toString(pitch));p.setProperty("selected",Integer.toString(selected));
        for(var e:edits.entrySet())p.setProperty("block."+Base64.getUrlEncoder().withoutPadding().encodeToString(e.getKey().getBytes(StandardCharsets.UTF_8)),Integer.toString(e.getValue()));
        Path target=directory().resolve(safe(worldName)+".properties"),tmp=target.resolveSibling(target.getFileName()+".tmp");
        try(var out=Files.newOutputStream(tmp)){p.store(out,"BlockCraft world save");}
        try{Files.move(tmp,target,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}catch(AtomicMoveNotSupportedException ex){Files.move(tmp,target,StandardCopyOption.REPLACE_EXISTING);}
    }
    public static Properties load(String name) throws IOException {
        Properties p=new Properties();Path f=directory().resolve(safe(name)+".properties");if(Files.isRegularFile(f))try(var in=Files.newInputStream(f)){p.load(in);}return p;
    }
    public static List<String> list() throws IOException {
        if(!Files.isDirectory(directory()))return List.of();
        try(var s=Files.list(directory())){return s.filter(p->p.toString().endsWith(".properties")).map(p->p.getFileName().toString().replaceFirst("\\.properties$","")).sorted().toList();}
    }
    private static String safe(String n){String s=n.replaceAll("[^A-Za-z0-9 _-]","_").trim();return s.isEmpty()?"World":s.substring(0,Math.min(48,s.length()));}
}