package dev.iustin.blockcraft.android;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * First playable voxel-world implementation.
 * The world is 256x256 columns (65,536 generated terrain columns); only a nearby
 * window is meshed and sent to OpenGL, avoiding tens of thousands of draw calls.
 */
final class BlockCraftRenderer implements GLSurfaceView.Renderer {
    private static final int WORLD_SIZE = 256;
    private static final int VIEW_RADIUS = 22;
    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix; attribute vec4 aPosition; attribute vec4 aColor;" +
            "varying vec4 vColor; void main(){ gl_Position=uMVPMatrix*aPosition; vColor=aColor; }";
    private static final String FRAGMENT_SHADER =
            "precision mediump float; varying vec4 vColor; void main(){ gl_FragColor=vColor; }";

    private final float[] projection = new float[16];
    private final float[] view = new float[16];
    private final float[] vp = new float[16];
    private final float[] mvp = new float[16];
    private int program, positionHandle, colorHandle, matrixHandle;
    private FloatBuffer vertices, colors;
    private int vertexCount;
    private float playerX = 128.5f, playerZ = 128.5f, yaw = 0f, pitch = -12f;
    private float moveForward, moveSide, verticalVelocity;
    private boolean grounded = true;
    private long lastFrame;
    private int meshCenterX = Integer.MIN_VALUE, meshCenterZ = Integer.MIN_VALUE;
    private final Map<Long, Byte> edits = new HashMap<>();
    private int selectedBlock = 1;
    private int blocksBroken = 0;

    @Override public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glClearColor(0.48f, 0.72f, 0.95f, 1f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        program = GLES20.glCreateProgram();
        int vertex = compileShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
        int fragment = compileShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
        GLES20.glAttachShader(program, vertex);
        GLES20.glAttachShader(program, fragment);
        GLES20.glLinkProgram(program);
        int[] linked = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linked, 0);
        if (linked[0] == 0) throw new IllegalStateException("BlockCraft shader link failed");
        positionHandle = GLES20.glGetAttribLocation(program, "aPosition");
        colorHandle = GLES20.glGetAttribLocation(program, "aColor");
        matrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        rebuildMesh();
        lastFrame = System.nanoTime();
    }

    @Override public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        Matrix.perspectiveM(projection, 0, 70f, height == 0 ? 1f : (float) width / height, 0.1f, 100f);
    }

    @Override public void onDrawFrame(GL10 unused) {
        long now = System.nanoTime();
        float dt = Math.min(0.05f, (now - lastFrame) / 1_000_000_000f);
        lastFrame = now;
        updatePlayer(dt);
        int cx = Math.round(playerX), cz = Math.round(playerZ);
        if (Math.abs(cx - meshCenterX) >= 4 || Math.abs(cz - meshCenterZ) >= 4) rebuildMesh();
        float yr = (float) Math.toRadians(yaw);
        float pr = (float) Math.toRadians(pitch);
        float lookX = (float) Math.sin(yr) * (float) Math.cos(pr);
        float lookY = (float) Math.sin(pr);
        float lookZ = (float) -Math.cos(yr) * (float) Math.cos(pr);
        Matrix.setLookAtM(view, 0, playerX, terrainHeight((int) playerX, (int) playerZ) + 1.62f,
                playerZ, playerX + lookX, terrainHeight((int) playerX, (int) playerZ) + 1.62f + lookY,
                playerZ + lookZ, 0f, 1f, 0f);
        Matrix.multiplyMM(vp, 0, projection, 0, view, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glUseProgram(program);
        Matrix.setIdentityM(mvp, 0);
        float[] finalMvp = new float[16];
        Matrix.multiplyMM(finalMvp, 0, vp, 0, mvp, 0);
        GLES20.glUniformMatrix4fv(matrixHandle, 1, false, finalMvp, 0);
        vertices.position(0); colors.position(0);
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glEnableVertexAttribArray(colorHandle);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertices);
        GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, 0, colors);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(colorHandle);
    }

    private void updatePlayer(float dt) {
        float yr = (float) Math.toRadians(yaw);
        float dx = ((float)Math.sin(yr) * moveForward + (float)Math.cos(yr) * moveSide) * 5.0f * dt;
        float dz = (-(float)Math.cos(yr) * moveForward + (float)Math.sin(yr) * moveSide) * 5.0f * dt;
        float nx = clamp(playerX + dx, 1, WORLD_SIZE - 2), nz = clamp(playerZ + dz, 1, WORLD_SIZE - 2);
        if (Math.abs(terrainHeight((int)nx,(int)playerZ)-terrainHeight((int)playerX,(int)playerZ)) <= 1) playerX = nx;
        if (Math.abs(terrainHeight((int)playerX,(int)nz)-terrainHeight((int)playerX,(int)playerZ)) <= 1) playerZ = nz;
        if (!grounded) {
            verticalVelocity -= 18f * dt;
            if (verticalVelocity <= 0) { verticalVelocity = 0; grounded = true; }
        }
    }

    void setMovement(float forward, float side) { moveForward = forward; moveSide = side; }
    void turn(float dx, float dy) { yaw += dx; pitch = clamp(pitch - dy, -65f, 65f); }
    void jump() { if (grounded) { grounded = false; verticalVelocity = 6.3f; } }
    synchronized String inventoryText() {
        return "INVENTAR  |  Blocuri sparte: " + blocksBroken + "  |  Selectat: " + blockName(selectedBlock);
    }
    void nextBlock() { selectedBlock = (selectedBlock + 1) % 4; }
    void breakBlock() {
        int[] p = targetBlock();
        long key = key(p[0], p[1]);
        edits.put(key, (byte)0);
        blocksBroken++;
        rebuildMesh();
    }
    void placeBlock() {
        int[] p = targetBlock();
        if (p[1] >= 0 && p[1] < 32) {
            edits.put(key(p[0], p[1]), (byte)(selectedBlock + 1));
            rebuildMesh();
        }
    }
    private int[] targetBlock() {
        float r = (float)Math.toRadians(yaw);
        int x = Math.round(playerX + (float)Math.sin(r) * 3f);
        int z = Math.round(playerZ - (float)Math.cos(r) * 3f);
        int y = terrainHeight(x,z);
        return new int[]{x,y};
    }
    private static String blockName(int b) {
        switch (b) { case 0: return "Iarbă"; case 1: return "Pământ"; case 2: return "Piatră"; default: return "Lemn"; }
    }

    private void rebuildMesh() {
        int cx = Math.round(playerX), cz = Math.round(playerZ);
        meshCenterX = cx; meshCenterZ = cz;
        FloatList p = new FloatList(), c = new FloatList();
        int minX = Math.max(0, cx - VIEW_RADIUS), maxX = Math.min(WORLD_SIZE - 1, cx + VIEW_RADIUS);
        int minZ = Math.max(0, cz - VIEW_RADIUS), maxZ = Math.min(WORLD_SIZE - 1, cz + VIEW_RADIUS);
        for (int x=minX; x<=maxX; x++) for (int z=minZ; z<=maxZ; z++) {
            int h = terrainHeight(x,z);
            int block = blockAt(x,h,z);
            if (block == 0) continue;
            float[] top = color(block);
            face(p,c,x,h+0.5f,z,top[0],top[1],top[2], -0.5f,0.5f,-0.5f, -0.5f,0.5f,0.5f, 0.5f,0.5f,0.5f, 0.5f,0.5f,-0.5f);
            int[] nx={-1,1,0,0}, nz={0,0,-1,1};
            for (int d=0; d<4; d++) {
                int nh=terrainHeight(x+nx[d],z+nz[d]);
                if (nh >= h) continue;
                float side=0.72f;
                if(d==0) face(p,c,x,h+0.0f,z,top[0]*side,top[1]*side,top[2]*side,-0.5f,-0.5f,-0.5f,-0.5f,-0.5f,0.5f,-0.5f,0.5f,0.5f,-0.5f,0.5f,-0.5f);
                if(d==1) face(p,c,x,h+0.0f,z,top[0]*side,top[1]*side,top[2]*side,0.5f,-0.5f,-0.5f,0.5f,0.5f,-0.5f,0.5f,0.5f,0.5f,0.5f,-0.5f,0.5f);
                if(d==2) face(p,c,x,h+0.0f,z,top[0]*side,top[1]*side,top[2]*side,-0.5f,-0.5f,-0.5f,0.5f,-0.5f,-0.5f,0.5f,0.5f,-0.5f,-0.5f,0.5f,-0.5f);
                if(d==3) face(p,c,x,h+0.0f,z,top[0]*side,top[1]*side,top[2]*side,-0.5f,-0.5f,0.5f,-0.5f,0.5f,0.5f,0.5f,0.5f,0.5f,0.5f,-0.5f,0.5f);
            }
        }
        vertices=buffer(p.toArray()); colors=buffer(c.toArray()); vertexCount=p.size()/3;
    }

    private int blockAt(int x,int y,int z) {
        Byte edit=edits.get(key(x,y));
        if(edit!=null) return edit;
        int h=terrainHeight(x,z);
        if(y>h) return 0;
        if(y==h) return 1;
        return y>h-3 ? 2 : 3;
    }
    private int terrainHeight(int x,int z) {
        if(x<0||z<0||x>=WORLD_SIZE||z>=WORLD_SIZE) return 0;
        double hills=Math.sin(x*0.075)*2.3+Math.cos(z*0.061)*2.1+Math.sin((x+z)*0.035)*2.0;
        double ridge=Math.sin(x*0.019+Math.cos(z*0.024))*1.8;
        return Math.max(1,Math.min(12,4+(int)Math.round(hills+ridge)));
    }
    private static long key(int x,int y) { return (((long)x)<<32) ^ (y & 0xffffffffL); }
    private static float[] color(int block) {
        if(block==1) return new float[]{0.18f,0.63f,0.19f};
        if(block==2) return new float[]{0.43f,0.43f,0.45f};
        return new float[]{0.43f,0.25f,0.12f};
    }
    private static float clamp(float v,float lo,float hi){return Math.max(lo,Math.min(hi,v));}
    private static void face(FloatList p,FloatList c,float x,float y,float z,float r,float g,float b,float...v) {
        int[] order={0,1,2,0,2,3};
        for(int i:order){p.add(x+v[i*3]);p.add(y+v[i*3+1]);p.add(z+v[i*3+2]);c.add(r);c.add(g);c.add(b);c.add(1f);}
    }
    private static int compileShader(int type,String source) {
        int shader=GLES20.glCreateShader(type); GLES20.glShaderSource(shader,source); GLES20.glCompileShader(shader);
        int[] status=new int[1]; GLES20.glGetShaderiv(shader,GLES20.GL_COMPILE_STATUS,status,0);
        if(status[0]==0) throw new IllegalStateException("BlockCraft shader compilation failed");
        return shader;
    }
    private static FloatBuffer buffer(float[] values) {
        FloatBuffer b=ByteBuffer.allocateDirect(values.length*4).order(ByteOrder.nativeOrder()).asFloatBuffer(); b.put(values).position(0); return b;
    }
    private static final class FloatList {
        private float[] data=new float[16384]; private int length;
        void add(float v){if(length==data.length){float[] n=new float[data.length*2];System.arraycopy(data,0,n,0,data.length);data=n;}data[length++]=v;}
        int size(){return length;} float[] toArray(){float[] a=new float[length];System.arraycopy(data,0,a,0,length);return a;}
    }
}