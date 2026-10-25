package dev.iustin.blockcraft.android;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public final class BlockCraftActivity extends Activity {
    private GLSurfaceView surface;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        surface = new GLSurfaceView(this);
        surface.setEGLContextClientVersion(2);
        surface.setRenderer(new BlockCraftRenderer());
        surface.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        setContentView(surface);
    }

    @Override
    protected void onPause() {
        if (surface != null) surface.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (surface != null) surface.onResume();
    }
}
