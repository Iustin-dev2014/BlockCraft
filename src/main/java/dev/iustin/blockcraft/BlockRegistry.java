package dev.iustin.blockcraft;

import java.util.ArrayList;
import java.util.List;

/** Data-driven block catalog. Entries are generated from families; individual art assets are added separately. */
public final class BlockRegistry {
    public record Block(int id, String key, String displayName, float r, float g, float b, boolean solid, boolean transparent) {}
    private static final List<Block> BLOCKS = build();
    private BlockRegistry() {}
    private static List<Block> build() {
        List<Block> out = new ArrayList<>(1000);
        String[] families = {"grass","dirt","stone","granite","marble","sand","red_sand","gravel","clay","mud",
            "oak_planks","birch_planks","spruce_planks","jungle_planks","acacia_planks","dark_oak_planks",
            "oak_log","birch_log","spruce_log","jungle_log","acacia_log","dark_oak_log","leaves","glass",
            "water","ice","snow","coal_ore","iron_ore","copper_ore","gold_ore","redstone_ore","lapis_ore",
            "diamond_ore","emerald_ore","obsidian","bricks","mossy_stone","sandstone","nether_brick"};
        for (int i=0;i<1000;i++) {
            String family=families[i%families.length];
            int tier=i/families.length;
            float hue=(float)((i*0.61803398875)%1.0);
            float r=0.28f+0.55f*(float)Math.abs(Math.sin(hue*6.283));
            float g=0.25f+0.55f*(float)Math.abs(Math.sin((hue+0.33)*6.283));
            float b=0.22f+0.55f*(float)Math.abs(Math.sin((hue+0.66)*6.283));
            String key=family+(tier==0?"":"_"+tier);
            out.add(new Block(i,key,pretty(family)+(tier==0?"":" Variant "+tier),r,g,b,!family.equals("water")&&!family.equals("glass"),family.equals("water")||family.equals("glass")));
        }
        return List.copyOf(out);
    }
    private static String pretty(String s) { String[] p=s.split("_"); StringBuilder b=new StringBuilder(); for(String x:p){if(b.length()>0)b.append(' ');b.append(Character.toUpperCase(x.charAt(0))).append(x.substring(1));}return b.toString(); }
    public static List<Block> all() { return BLOCKS; }
    public static Block get(int id) { return BLOCKS.get(Math.floorMod(id,BLOCKS.size())); }
}