package dev.iustin.blockcraft.android;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

final class BlockCraftRenderer implements GLSurfaceView.Renderer {
    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix; attribute vec4 aPosition; attribute vec4 aColor;" +
            "varying vec4 vColor; void main(){ gl_Position=uMVPMatrix*aPosition; vColor=aColor; }";
    private static final String FRAGMENT_SHADER =
            "precision mediump float; varying vec4 vColor; void main(){ gl_FragColor=vColor; }";

    private final float[] projection = new float[16];
    private final float[] view = new float[16];
    private final float[] model = new float[16];
    private final float[] vp = new float[16];
    private final float[] mvp = new float[16];
    private int program;
    private int positionHandle;
    private int colorHandle;
    private int matrixHandle;
    private FloatBuffer vertices;
    private FloatBuffer colors;
    private int vertexCount;
    private long startTime;

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
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
        buildScene();
        startTime = System.nanoTime();
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = height == 0 ? 1f : (float) width / height;
        Matrix.frustumM(projection, 0, -ratio, ratio, -1f, 1f, 2f, 60f);
        Matrix.setLookAtM(view, 0, 0f, 4.5f, 13f, 0f, 0f, 0f, 0f, 1f, 0f);
        Matrix.multiplyMM(vp, 0, projection, 0, view, 0);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glUseProgram(program);
        Matrix.setIdentityM(model, 0);
        float seconds = (System.nanoTime() - startTime) / 1_000_000_000f;
        Matrix.rotateM(model, 0, seconds * 18f, 0f, 1f, 0f);
        Matrix.multiplyMM(mvp, 0, vp, 0, model, 0);
        GLES20.glUniformMatrix4fv(matrixHandle, 1, false, mvp, 0);

        vertices.position(0);
        colors.position(0);
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glEnableVertexAttribArray(colorHandle);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertices);
        GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, 0, colors);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(colorHandle);
    }

    private void buildScene() {
        FloatList p = new FloatList();
        FloatList c = new FloatList();
        cube(p, c, -2.2f, 0f, 0f, 0.55f, 0.34f, 0.18f);
        cube(p, c, -1.1f, 0f, 0f, 0.22f, 0.66f, 0.24f);
        cube(p, c, 0f, 0f, 0f, 0.24f, 0.68f, 0.24f);
        cube(p, c, 1.1f, 0f, 0f, 0.24f, 0.68f, 0.24f);
        cube(p, c, 2.2f, 0f, 0f, 0.52f, 0.52f, 0.52f);
        for (int x = -5; x <= 5; x++) {
            for (int z = -3; z <= 2; z++) {
                cube(p, c, x * 0.95f, -1.1f, z * 0.95f, 0.25f, 0.58f, 0.22f);
            }
        }
        vertices = buffer(p.toArray());
        colors = buffer(c.toArray());
        vertexCount = p.size() / 3;
    }

    private static void cube(FloatList p, FloatList c, float x, float y, float z,
                             float r, float g, float b) {
        float s = 0.46f;
        face(p,c,x,y,z,r*0.75f,g*0.75f,b*0.75f, -s,-s,-s, s,-s,-s, s,-s,s, -s,-s,s);
        face(p,c,x,y,z,r,g,b, -s,s,-s, -s,s,s, s,s,s, s,s,-s);
        face(p,c,x,y,z,r*0.9f,g*0.9f,b*0.9f, -s,-s,s, s,-s,s, s,s,s, -s,s,s);
        face(p,c,x,y,z,r*0.65f,g*0.65f,b*0.65f, -s,-s,-s, -s,-s,s, -s,s,s, -s,s,-s);
        face(p,c,x,y,z,r*0.8f,g*0.8f,b*0.8f, s,-s,-s, s,s,-s, s,s,s, s,-s,s);
        face(p,c,x,y,z,r*0.85f,g*0.85f,b*0.85f, -s,-s,-s, -s,s,-s, s,s,-s, s,-s,-s);
    }

    private static void face(FloatList p, FloatList c, float x, float y, float z,
                             float r, float g, float b, float... v) {
        int[] order = {0,1,2,0,2,3};
        for (int i : order) {
            p.add(x + v[i*3]); p.add(y + v[i*3+1]); p.add(z + v[i*3+2]);
            c.add(Math.min(1f,r)); c.add(Math.min(1f,g)); c.add(Math.min(1f,b)); c.add(1f);
        }
    }

    private static int compileShader(int type, String source) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] status = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] == 0) throw new IllegalStateException("BlockCraft shader compilation failed");
        return shader;
    }

    private static FloatBuffer buffer(float[] values) {
        FloatBuffer result = ByteBuffer.allocateDirect(values.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        result.put(values).position(0);
        return result;
    }

    private static final class FloatList {
        private float[] data = new float[4096];
        private int length;
        void add(float value) {
            if (length == data.length) {
                float[] bigger = new float[data.length * 2];
                System.arraycopy(data, 0, bigger, 0, data.length);
                data = bigger;
            }
            data[length++] = value;
        }
        int size() { return length; }
        float[] toArray() {
            float[] out = new float[length];
            System.arraycopy(data, 0, out, 0, length);
            return out;
        }
    }
}
