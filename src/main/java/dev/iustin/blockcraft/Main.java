package dev.iustin.blockcraft;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

/** Minimal rendering bootstrap for BlockCraft; not yet a complete voxel game. */
public final class Main {
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;

    private Main() {}

    public static void main(String[] args) {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        long window = MemoryUtil.NULL;
        try {
            glfwDefaultWindowHints();
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);
            glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
            window = glfwCreateWindow(WIDTH, HEIGHT, "BlockCraft — Voxel Prototype", MemoryUtil.NULL, MemoryUtil.NULL);
            if (window == MemoryUtil.NULL) throw new IllegalStateException("Unable to create game window");

            glfwMakeContextCurrent(window);
            glfwSwapInterval(1);
            glfwShowWindow(window);
            GL.createCapabilities();
            glEnable(GL_DEPTH_TEST);
            glClearColor(0.48f, 0.72f, 0.95f, 1f);

            while (!glfwWindowShouldClose(window)) {
                glfwPollEvents();
                if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS) glfwSetWindowShouldClose(window, true);

                int[] width = new int[1];
                int[] height = new int[1];
                glfwGetFramebufferSize(window, width, height);
                glViewport(0, 0, width[0], height[0]);
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

                glMatrixMode(GL_PROJECTION);
                glLoadIdentity();
                float aspect = height[0] == 0 ? 1f : (float) width[0] / height[0];
                glFrustum(-aspect, aspect, -1, 1, 1.5, 100);
                glMatrixMode(GL_MODELVIEW);
                glLoadIdentity();
                glTranslatef(0f, -2.3f, -13f);
                glRotatef((float) (glfwGetTime() * 8.0 % 360), 0f, 1f, 0f);

                drawGround();
                drawCube(-2.0f, 0.0f, 0f, 0.55f, 0.34f, 0.18f);
                drawCube(-1.0f, 0.0f, 0f, 0.25f, 0.65f, 0.22f);
                drawCube(0.0f, 0.0f, 0f, 0.25f, 0.65f, 0.22f);
                drawCube(1.0f, 0.0f, 0f, 0.25f, 0.65f, 0.22f);
                drawCube(2.0f, 0.0f, 0f, 0.48f, 0.48f, 0.48f);
                glfwSwapBuffers(window);
            }
        } finally {
            if (window != MemoryUtil.NULL) glfwDestroyWindow(window);
            glfwTerminate();
            GLFWErrorCallback callback = glfwSetErrorCallback(null);
            if (callback != null) callback.free();
        }
    }

    private static void drawGround() {
        for (int x = -5; x <= 5; x++) {
            for (int z = -4; z <= 2; z++) drawCube(x, -1.0f, z, 0.28f, 0.62f, 0.25f);
        }
    }

    private static void drawCube(float x, float y, float z, float r, float g, float b) {
        float s = 0.48f;
        glPushMatrix();
        glTranslatef(x, y, z);
        glBegin(GL_QUADS);
        face(r * 0.8f, g * 0.8f, b * 0.8f, -s,-s,-s, s,-s,-s, s,-s,s, -s,-s,s);
        face(r, g, b, -s,s,-s, -s,s,s, s,s,s, s,s,-s);
        face(r*0.9f, g*0.9f, b*0.9f, -s,-s,s, s,-s,s, s,s,s, -s,s,s);
        face(r*0.65f, g*0.65f, b*0.65f, -s,-s,-s, -s,-s,s, -s,s,s, -s,s,-s);
        face(r*0.75f, g*0.75f, b*0.75f, s,-s,-s, s,s,-s, s,s,s, s,-s,s);
        face(r*0.85f, g*0.85f, b*0.85f, -s,-s,-s, -s,s,-s, s,s,-s, s,-s,-s);
        glEnd();
        glPopMatrix();
    }

    private static void face(float r, float g, float b, float... vertices) {
        glColor3f(r, g, b);
        for (int i = 0; i < vertices.length; i += 3) {
            glVertex3f(vertices[i], vertices[i + 1], vertices[i + 2]);
        }
    }
}
