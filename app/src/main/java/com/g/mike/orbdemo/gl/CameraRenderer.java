package com.g.mike.orbdemo.gl;

/**
 * Created by iosuser12 on 7/22/16.
 */
import java.nio.ByteBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.AttributeSet;

import com.g.mike.orbdemo.R;
import com.g.mike.orbdemo.handlers.CameraHandler;
import com.g.mike.orbdemo.iCamera;


public class CameraRenderer extends GLSurfaceView implements
        GLSurfaceView.Renderer,
        SurfaceTexture.OnFrameAvailableListener{
    private Context mContext;
    private boolean applyEffect = false;
    private boolean effectAlreadySet = false;

    private boolean alreadySwapped = true;
    private int splitPosition = 0;
    /**
     * Camera and SurfaceTexture
     */


    private SurfaceTexture mSurfaceTexture;
    //private final FBORenderTarget mRenderTarget = new FBORenderTarget();
    private final OESTexture mCameraTexture = new OESTexture();
    private  Shader defaultView = new Shader();
    private  Shader effectView = new Shader();
    private int mWidth, mHeight;
    private int effectVertex, effectFragment;


    private boolean updateTexture = false;
    /**
     * OpenGL params
     */

    private ByteBuffer mFullQuadVertices;
    private float[] mTransformM = new float[16];
    private float[] mOrientationM = new float[16];
    private float[] mRatio = new float[2];
    private boolean swapNow = false;


    private iCamera iCam =new CameraHandler();

    public CameraRenderer(Context context) {
        super(context);
        mContext = context;
        init();
    }

    private void init(){
        //Create full scene quad buffer
        final byte FULL_QUAD_COORDS[] = {-1, 1,
                -1, -1,
                1, 1,
                1, -1};
        mFullQuadVertices = ByteBuffer.allocateDirect(4 * 2);//why bytebuffor not byte[]
        mFullQuadVertices.put(FULL_QUAD_COORDS).position(0);

        setPreserveEGLContextOnPause(true); // preserves the context when  onPause called
        setEGLContextClientVersion(2);//sets to OpenGL 2
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);//wait for the requestRender() call to draw/render
    }

    @Override
    public synchronized void onFrameAvailable(SurfaceTexture surfaceTexture){
        updateTexture = true;
        requestRender();
    }


    @Override
    public synchronized void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //load and compile shader

        try {
            defaultView.setProgram(R.raw.vshader,  R.raw.fshader, mContext);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        generateCameraTexture();
    }

    @SuppressLint("NewApi")
    @Override
    public synchronized void onSurfaceChanged(GL10 gl, int width, int height) {

        mWidth = width;
        mHeight= height;
        //generate camera texture------------------------
        //set up surfacetexture------------------
        setupSurfaceTexture();
        //set camera para-----------------------------------
        iCam.releaseCamera();
        setupCamera(width,height);
    }

    private void setupCamera(int width, int height) {
        int camera_width =0;
        int camera_height =0;
        iCam.setupCamera(height,width,mSurfaceTexture);
        camera_width=iCam.getWidth();
        camera_height=iCam.getHeight();
        /*android.graphics.Matrix m = iCam.getTransformationMatrix(width,height);
        m.getValues(mOrientationM);
*/
        int version = iCam.getCameraVersion();
        //get the camera orientation and display dimension------------
        if(mContext.getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_PORTRAIT ){
            Matrix.setRotateM(mOrientationM, 0, (2-version)*90.0f, 0f, 0f, 1f);
            mRatio[1] = camera_width*1.0f/height;
            mRatio[0] = camera_height*1.0f/width;
        }
        else{
            Matrix.setRotateM(mOrientationM, 0, (version-1)*90.0f, 0f, 0f, 1f);
            mRatio[1] = camera_height*1.0f/height;
            mRatio[0] = camera_width*1.0f/width;
        }
        //start camera-----------------------------------------
        iCam.setParameters();
        iCam.startPreview();

        //start render---------------------
        requestRender();
    }

    private void generateCameraTexture(){ mCameraTexture.init();}
    private void setupSurfaceTexture(){
        SurfaceTexture oldSurfaceTexture = mSurfaceTexture;
        mSurfaceTexture = new SurfaceTexture(mCameraTexture.getTextureId());
        mSurfaceTexture.setOnFrameAvailableListener(this);
        if(oldSurfaceTexture != null){
            oldSurfaceTexture.release();
        }
    }

    @Override
    public synchronized void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        if(swapNow){
            iCam.releaseCamera();
            iCam.swapCam();
            generateCameraTexture();
            setupSurfaceTexture();
            setupCamera(mWidth,mHeight);
            swapNow=false;
        }
        if (applyEffect&&!effectAlreadySet) {
            try {
                effectView.deleteProgram();
                effectView.setProgram(effectVertex,effectFragment,mContext);
            } catch (Exception e) {
                e.printStackTrace();
            }
            effectAlreadySet=true;
        }
        //render the texture to FBO if new frame is available
        if(updateTexture&&!applyEffect){
            defaultView();

        }
        else if(updateTexture&&applyEffect){
            effectView();
        }

    }
    private void effectView(){
        mSurfaceTexture.updateTexImage();
        mSurfaceTexture.getTransformMatrix(mTransformM);

        updateTexture = false;

        GLES20.glViewport(0, 0, mWidth, mHeight);

        effectView.useProgram();

        int uTransformM = effectView.getHandle("uTransformM");
        int uOrientationM = effectView.getHandle("uOrientationM");
        int uRatioV = effectView.getHandle("ratios");
        int uSplit = effectView.getHandle("splitOrientation");
        GLES20.glUniformMatrix4fv(uTransformM, 1, false, mTransformM, 0);
        GLES20.glUniformMatrix4fv(uOrientationM, 1, false, mOrientationM, 0);
        GLES20.glUniform1i(uSplit,splitPosition);
        GLES20.glUniform2fv(uRatioV, 1, mRatio, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mCameraTexture.getTextureId());

        renderQuad(effectView.getHandle("aPosition"));
    }
    private void defaultView(){
        mSurfaceTexture.updateTexImage();
        mSurfaceTexture.getTransformMatrix(mTransformM);

        updateTexture = false;

        GLES20.glViewport(0, 0, mWidth, mHeight);

        defaultView.useProgram();

        int uTransformM = defaultView.getHandle("uTransformM");
        int uOrientationM = defaultView.getHandle("uOrientationM");
        int uRatioV = defaultView.getHandle("ratios");

        GLES20.glUniformMatrix4fv(uTransformM, 1, false, mTransformM, 0);
        GLES20.glUniformMatrix4fv(uOrientationM, 1, false, mOrientationM, 0);
        GLES20.glUniform2fv(uRatioV, 1, mRatio, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mCameraTexture.getTextureId());

        renderQuad(defaultView.getHandle("aPosition"));
    }
    private void renderQuad(int aPosition){
        GLES20.glVertexAttribPointer(aPosition, 2, GLES20.GL_BYTE, false, 0, mFullQuadVertices);
        GLES20.glEnableVertexAttribArray(aPosition);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    @Override
    public void onPause() {
        super.onPause();
        iCam.releaseCamera();
    }


    public void onDestroy(){
        updateTexture = false;
        if(mSurfaceTexture!=null)
            mSurfaceTexture.release();
        iCam.kill();
    }
    public void applyEffect(int vshader,int fshader)
    {
        effectVertex=vshader;
        effectFragment = fshader;
        applyEffect=true;
    }
    public void removeEffect(){
        effectAlreadySet = false;
        applyEffect=false;
    }
    public void swapCamera(){swapNow =!swapNow;}
    public void setSplitPosition(int bool){
        splitPosition = bool;
    }
    public int getSplitPosition(){
        return splitPosition;
    }
    public void capture(int orientation){iCam.capture(orientation);}
    public boolean getCameraId(){
        return iCam.getCameraId();
    }
    public void toggleFlash() {
        iCam.toggleFlash();
    }
    public void setICamera(iCamera iCam){
        this.iCam=iCam;
    }
}