package dev.iustin.blockcraft;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

/** Playable desktop voxel prototype: WASD movement, arrows to look, space to jump,
 * left click to mine, right click to place, 1-4 to choose block, ESC to exit. */
public final class Main {
    private static final int WIDTH=1280, HEIGHT=720, WORLD=256, RADIUS=22;
    private static float px=128.5f, pz=128.5f, yaw=0f, pitch=-12f, vy=0f;
    private static boolean grounded=true;
    private static int selected=1, broken=0;
    private static boolean paused=false, chatOpen=false, creativeMode=false, survivalMode=true;
    private static boolean escWasDown=false, tWasDown=false;
    private static String worldName="World", chatText="", status="Ready";
    private static final GameConfig config=new GameConfig();
    private static double worldTime=0;
    private static final java.util.Map<String,Integer> edits=new java.util.HashMap<>();
    private Main() {}
    public static void main(String[] args) {
        GLFWErrorCallback.createPrint(System.err).set();
        if(!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");
        long window=MemoryUtil.NULL;
        try {
            glfwDefaultWindowHints(); glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR,2); glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR,1);
            glfwWindowHint(GLFW_RESIZABLE,GLFW_TRUE);
            window=glfwCreateWindow(WIDTH,HEIGHT,"BlockCraft — Voxel World",MemoryUtil.NULL,MemoryUtil.NULL);
            if(window==MemoryUtil.NULL) throw new IllegalStateException("Unable to create game window");
            config.load();
            glfwMakeContextCurrent(window); glfwSwapInterval(config.vsync?1:0); glfwShowWindow(window); GL.createCapabilities();
            glfwSetWindowTitle(window,"BlockCraft — "+worldName+" | WASD move · Space jump · T chat · ESC menu");
            glfwSetCharCallback(window,(win,codepoint)->{if(chatOpen){if(codepoint>=32&&codepoint<127)chatText+=Character.toString((char)codepoint);}});
            glfwSetKeyCallback(window,(win,key,scancode,action,mods)->{
                if(action==GLFW_PRESS&&key==GLFW_KEY_BACKSPACE&&chatOpen&&!chatText.isEmpty())chatText=chatText.substring(0,chatText.length()-1);
                if(action==GLFW_PRESS&&key==GLFW_KEY_ENTER&&chatOpen){status=chatText.startsWith("/")?"Command: "+chatText:"Chat: "+chatText;chatText="";chatOpen=false;}
            });
            glEnable(GL_DEPTH_TEST); glClearColor(0.48f,0.72f,0.95f,1f);
            double last=glfwGetTime();
            while(!glfwWindowShouldClose(window)) {
                glfwPollEvents();
                boolean escDown=glfwGetKey(window,GLFW_KEY_ESCAPE)==GLFW_PRESS;
                if(escDown&&!escWasDown){if(chatOpen){chatOpen=false;chatText="";}else{paused=!paused;glfwSetWindowTitle(window,paused?"BlockCraft — PAUSED | ESC resume · F5 save · Q menu":"BlockCraft — "+worldName+" | WASD move · Space jump · T chat · ESC menu");if(paused)saveWorld();}}
                escWasDown=escDown;
                boolean tDown=glfwGetKey(window,GLFW_KEY_T)==GLFW_PRESS;
                if(tDown&&!tWasDown&&!paused){chatOpen=!chatOpen;chatText="";glfwSetWindowTitle(window,chatOpen?"BlockCraft Chat — type and press ENTER":"BlockCraft — "+worldName+" | WASD move · T chat · ESC menu");}
                tWasDown=tDown;
                if(glfwGetKey(window,GLFW_KEY_F5)==GLFW_PRESS)saveWorld();
                if(glfwGetKey(window,GLFW_KEY_F11)==GLFW_PRESS){config.graphics=config.graphics.equals("HIGH")?"ULTRA":"HIGH";config.save();status="Graphics preset: "+config.graphics;}
                if(paused&&glfwGetKey(window,GLFW_KEY_Q)==GLFW_PRESS){saveWorld();paused=false;glfwSetWindowTitle(window,"BlockCraft — "+worldName);}
                if(paused){glClearColor(0.025f,0.045f,0.08f,1f);glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);glfwSwapBuffers(window);continue;}
                double now=glfwGetTime(); float dt=(float)Math.min(0.05,now-last); last=now;
                worldTime=(worldTime+dt)%1200.0;
                if(glfwGetKey(window,GLFW_KEY_LEFT)==GLFW_PRESS) yaw-=100*dt;
                if(glfwGetKey(window,GLFW_KEY_RIGHT)==GLFW_PRESS) yaw+=100*dt;
                if(glfwGetKey(window,GLFW_KEY_UP)==GLFW_PRESS) pitch=Math.min(55,pitch+70*dt);
                if(glfwGetKey(window,GLFW_KEY_DOWN)==GLFW_PRESS) pitch=Math.max(-55,pitch-70*dt);
                float a=(float)Math.toRadians(yaw), speed=6*dt;
                float forward=(glfwGetKey(window,GLFW_KEY_W)==GLFW_PRESS?1:0)-(glfwGetKey(window,GLFW_KEY_S)==GLFW_PRESS?1:0);
                float side=(glfwGetKey(window,GLFW_KEY_D)==GLFW_PRESS?1:0)-(glfwGetKey(window,GLFW_KEY_A)==GLFW_PRESS?1:0);
                float nx=clamp(px+(float)Math.sin(a)*forward*speed+(float)Math.cos(a)*side*speed,1,WORLD-2);
                float nz=clamp(pz-(float)Math.cos(a)*forward*speed+(float)Math.sin(a)*side*speed,1,WORLD-2);
                if(Math.abs(height((int)nx,(int)pz)-height((int)px,(int)pz))<=1) px=nx;
                if(Math.abs(height((int)px,(int)nz)-height((int)px,(int)pz))<=1) pz=nz;
                if(glfwGetKey(window,GLFW_KEY_SPACE)==GLFW_PRESS&&grounded){vy=7;grounded=false;}
                if(!grounded){vy-=18*dt;if(vy<=0){vy=0;grounded=true;}}
                if(glfwGetKey(window,GLFW_KEY_1)==GLFW_PRESS) selected=1;
                if(glfwGetKey(window,GLFW_KEY_2)==GLFW_PRESS) selected=2;
                if(glfwGetKey(window,GLFW_KEY_3)==GLFW_PRESS) selected=3;
                if(glfwGetKey(window,GLFW_KEY_4)==GLFW_PRESS) selected=4;
                if(!chatOpen&&glfwGetMouseButton(window,GLFW_MOUSE_BUTTON_LEFT)==GLFW_PRESS) edits.put(targetKey(false),0);
                if(!chatOpen&&glfwGetMouseButton(window,GLFW_MOUSE_BUTTON_RIGHT)==GLFW_PRESS) edits.put(targetKey(true),selected);
                int[] w={1},h={1}; glfwGetFramebufferSize(window,w,h); glViewport(0,0,w[0],h[0]);
                float daylight=(float)(0.5+0.5*Math.sin(worldTime*Math.PI/600.0));
                glClearColor(0.025f+0.42f*daylight,0.035f+0.64f*daylight,0.09f+0.83f*daylight,1f);
                glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);
                glMatrixMode(GL_PROJECTION); glLoadIdentity(); float aspect=h[0]==0?1:(float)w[0]/h[0]; glFrustum(-aspect,aspect,-1,1,1.2,100);
                glMatrixMode(GL_MODELVIEW); glLoadIdentity();
                float lookX=(float)Math.sin(a)*(float)Math.cos(Math.toRadians(pitch));
                float lookY=(float)Math.sin(Math.toRadians(pitch));
                float lookZ=-(float)Math.cos(a)*(float)Math.cos(Math.toRadians(pitch));
                gluLookAt(px-lookX*5,height((int)px,(int)pz)+3.6f-lookY*5,pz-lookZ*5,px+lookX*3,height((int)px,(int)pz)+1.2f+lookY*3,pz+lookZ*3);
                drawWorld();
                drawCube(px,height((int)px,(int)pz)+0.9f,pz,0.2f,0.35f,0.9f,0.85f);
                if(chatOpen)glfwSetWindowTitle(window,"CHAT: "+chatText+" | ENTER send · BACKSPACE delete · ESC close");
                else glfwSetWindowTitle(window,"BlockCraft | "+worldName+" | "+(creativeMode?"CREATIVE":"SURVIVAL")+" | Blocks "+BlockRegistry.all().size()+" | "+status);
                glfwSwapBuffers(window);
            }
        } finally { if(window!=MemoryUtil.NULL)glfwDestroyWindow(window);glfwTerminate();GLFWErrorCallback cb=glfwSetErrorCallback(null);if(cb!=null)cb.free(); }
    }
    private static void gluLookAt(float ex,float ey,float ez,float cx,float cy,float cz) {
        float fx=cx-ex,fy=cy-ey,fz=cz-ez,fl=(float)Math.sqrt(fx*fx+fy*fy+fz*fz);fx/=fl;fy/=fl;fz/=fl;
        float sx=-fz,sy=0,sz=fx,sl=(float)Math.sqrt(sx*sx+sz*sz);sx/=sl;sz/=sl;
        float ux=sy*fz-sz*fy,uy=sz*fx-sx*fz,uz=sx*fy-sy*fx;
        float[] m={sx,ux,-fx,0, sy,uy,-fy,0, sz,uz,-fz,0,0,0,0,1};
        glMultMatrixf(m);glTranslatef(-ex,-ey,-ez);
    }
    private static void saveWorld() {
        try { WorldSave.save(worldName,px,pz,yaw,pitch,selected,edits); status="World saved"; }
        catch(Exception e){status="Save failed: "+e.getMessage();}
    }
    private static void drawWorld() {
        int cx=(int)px,cz=(int)pz;
        for(int x=Math.max(0,cx-RADIUS);x<=Math.min(WORLD-1,cx+RADIUS);x++)
            for(int z=Math.max(0,cz-RADIUS);z<=Math.min(WORLD-1,cz+RADIUS);z++) {
                int y=height(x,z); Integer edit=edits.get(key(x,y,z));
                if(edit==null||edit!=0) { float[] col=edit==null?new float[]{0.18f,0.63f,0.19f}:color(edit);drawCube(x,y,z,col[0],col[1],col[2],0.5f); }
                if(height(x-1,z)<y)drawCube(x-0.5f,y-0.5f,z,0.32f,0.42f,0.18f,0.5f);
                if(height(x+1,z)<y)drawCube(x+0.5f,y-0.5f,z,0.32f,0.42f,0.18f,0.5f);
                if(height(x,z-1)<y)drawCube(x,y-0.5f,z-0.5f,0.32f,0.42f,0.18f,0.5f);
                if(height(x,z+1)<y)drawCube(x,y-0.5f,z+0.5f,0.32f,0.42f,0.18f,0.5f);
            }
        for(java.util.Map.Entry<String,Integer> e:edits.entrySet()) if(e.getValue()>0) {
            String[] v=e.getKey().split(",");int x=Integer.parseInt(v[0]),y=Integer.parseInt(v[1]),z=Integer.parseInt(v[2]);
            if(Math.abs(x-cx)<=RADIUS&&Math.abs(z-cz)<=RADIUS){float[] c=color(e.getValue());drawCube(x,y,z,c[0],c[1],c[2],0.5f);}
        }
    }
    private static int height(int x,int z){if(x<0||z<0||x>=WORLD||z>=WORLD)return 0;double hills=Math.sin(x*.075)*2.3+Math.cos(z*.061)*2.1+Math.sin((x+z)*.035)*2+Math.sin(x*.019+Math.cos(z*.024))*1.8;return Math.max(1,Math.min(12,4+(int)Math.round(hills)));}
    private static String targetKey(boolean place){int x=Math.max(1,Math.min(WORLD-2,Math.round(px+(float)Math.sin(Math.toRadians(yaw))*3)));int z=Math.max(1,Math.min(WORLD-2,Math.round(pz-(float)Math.cos(Math.toRadians(yaw))*3)));return key(x,height(x,z)+(place?1:0),z);}
    private static String key(int x,int y,int z){return x+","+y+","+z;}
    private static float[] color(int b){if(b==1)return new float[]{0.18f,0.63f,0.19f};if(b==2)return new float[]{0.43f,0.43f,0.45f};if(b==3)return new float[]{0.43f,0.25f,0.12f};return new float[]{0.45f,0.27f,0.13f};}
    private static float clamp(float v,float lo,float hi){return Math.max(lo,Math.min(hi,v));}
    private static void drawCube(float x,float y,float z,float r,float g,float b,float s) {
        glPushMatrix();glTranslatef(x,y,z);glBegin(GL_QUADS);
        face(r*.8f,g*.8f,b*.8f,-s,-s,-s,s,-s,-s,s,-s,s,-s,-s,s);
        face(r,g,b,-s,s,-s,-s,s,s,s,s,s,s,s,-s);
        face(r*.9f,g*.9f,b*.9f,-s,-s,s,s,-s,s,s,s,s,-s,s,s);
        face(r*.65f,g*.65f,b*.65f,-s,-s,-s,-s,-s,s,-s,s,s,-s,s,-s);
        face(r*.75f,g*.75f,b*.75f,s,-s,-s,s,s,-s,s,s,s,s,-s,s);
        face(r*.85f,g*.85f,b*.85f,-s,-s,-s,-s,s,-s,s,s,-s,s,-s,-s);
        glEnd();glPopMatrix();
    }
    private static void face(float r,float g,float b,float...v){glColor3f(r,g,b);for(int i=0;i<v.length;i+=3)glVertex3f(v[i],v[i+1],v[i+2]);}
}