package com.cv.hy.opencvinvideo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.media.MediaMetadataEditor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.VideoView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.opencv.core.Core.getTickCount;
import static org.opencv.core.Core.getTickFrequency;
import static org.opencv.videoio.Videoio.CAP_PROP_FRAME_COUNT;
import static org.opencv.videoio.Videoio.CAP_PROP_POS_FRAMES;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button play;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private SvCallback callback;
    private android.graphics.Rect rect;

    private BlockingQueue<Mat> blockingQueueDetect = new ArrayBlockingQueue<>(5);

    private VideoCapture videoCapture;
    private CascadeClassifier cascadeClassifier;
    private File videoFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        OpenCVLoader.initDebug();//一定要这样初始化！！！！
        initializeOpenCVDependencies();
        initView();
        initData();
    }

    @Override
    protected void onPause() {
        callback.stopAnim();
        super.onPause();
    }

    private void initView(){
        play = (Button) findViewById(R.id.play);
        surfaceView = (SurfaceView) findViewById(R.id.surface);
    }
    private void initData(){
        surfaceHolder=surfaceView.getHolder();
        callback = new SvCallback();
        surfaceView.setZOrderOnTop(true);
        surfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        surfaceHolder.addCallback(callback);
        play.setOnClickListener(this );
    }
    private Bitmap detect(Mat frame) {
        Mat imageMat = new Mat();
        Bitmap bitmap = Bitmap.createBitmap(frame.width(), frame.height(), Bitmap.Config.ARGB_8888);
        Imgproc.cvtColor(frame, imageMat, Imgproc.COLOR_RGBA2RGB);

        MatOfRect imageDetections = new MatOfRect();
        if (cascadeClassifier != null) {
            cascadeClassifier.detectMultiScale(imageMat, imageDetections, 1.1, 2, 0, new Size(50, 50), new Size());
        }
        for (Rect rect : imageDetections.toArray()) {
            Imgproc.rectangle(imageMat, rect.tl(), rect.br(), new Scalar(0, 255, 0, 255), 3);
        }

        Utils.matToBitmap(imageMat, bitmap);
        return bitmap;
    }

    private void initializeOpenCVDependencies() {
        videoFile = new File(Environment.getExternalStorageDirectory(), "out1.avi");
        videoCapture = new VideoCapture();
        videoCapture.open(videoFile.getPath());
        try {
            // Copy the resource into a temp file so OpenCV can load it
//            InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
            InputStream is = getResources().openRawResource(R.raw.cas40);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
//            File mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
            File mCascadeFile = new File(cascadeDir, "cas40.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);


            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            // Load the cas45 classifier
            cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e("OpenCVActivity", "Error loading cascade", e);
        }
    }

    @Override
    public void onClick(View view) {
        if(videoCapture.isOpened()) {
//            double videoTime = videoCapture.get(CAP_PROP_FRAME_COUNT) / videoCapture.get(CAP_PROP_FPS);
//            Log.i("Main:timeLen:", String.valueOf(videoTime));
            callback.startAnim();
            play.setEnabled(false);
        }
    }

    private Bitmap getSmallBitmap(Bitmap bitmap) {
//        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, sampleSize);
//        bmp.copyPixelsFromBuffer(mPixelBuf);
//        String path = Environment.getExternalStorageDirectory() + "/bitmap/" + i + ".png";
//        FileOutputStream fileOutputStream = null;
//        fileOutputStream = new FileOutputStream(path);
//        bmp.compress(CompressFormat.PNG, 90, fileOutputStream);
//        bmp.recycle();
        return null;
    }

    private Mat getSmallMat(Mat mat) {
        int div = 64;
        int rows = mat.rows();
        int cols = mat.cols();
        if (mat.isContinuous()) {
            cols = cols*rows;
            rows=1;
        }
        int n = (int) (Math.log(div)/Math.log(2));
        int halfDiv = div>>2;
        int mask = 0xFF << n;
        double[] pixel;
        for(int j = 0;j<rows;j++) {
            for(int k = 0;k<cols;k++) {
                pixel=mat.get(j, k);

                for (int i = 0; null!=pixel &&i < pixel.length; i++) {
                    pixel[i] = (int) pixel[i] & mask + halfDiv;
                }
            }
        }
        return mat;
    }

    private void drawBitmap(Bitmap bitmap) throws NullPointerException{
//        if(position>=totalCount)
//        {
//            isDrawing=false;
//            decodeHandler.sendEmptyMessage(-2);
//            canvas=surfaceHolder.lockCanvas();
//            //clear surfaceView
//            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//            surfaceHolder.unlockCanvasAndPost(canvas);
//            return;
//        }
        Canvas canvas=surfaceHolder.lockCanvas(rect);
//        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawBitmap(bitmap,null,rect,null);
        surfaceHolder.unlockCanvasAndPost(canvas);
    }

    private class ProduceBitmap implements Runnable {
        @Override
        public void run() {
            for (int i = 0; i < videoCapture.get(CAP_PROP_FRAME_COUNT); i+=8) {
                Mat frame=new Mat();
                long startTime = getTickCount();
                videoCapture.set(CAP_PROP_POS_FRAMES, i);
                if (videoCapture.read(frame)) {
                    long endTime = getTickCount();
                    Log.i("Main   readTime:", (endTime - startTime) / getTickFrequency() + " s");
//                    String path = Environment.getExternalStorageDirectory() + File.separator + "test" + File.separator + i + ".jpg";
//                    Imgcodecs.imwrite(path, frame);
                    try {
                        blockingQueueDetect.put(frame);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        }
    }

    private class PlayBitmap implements Runnable {

        @Override
        public void run()
        {
//            Looper.prepare();
//            decodeHandler=new Handler(Looper.myLooper())
//            {
//                @Override
//                public void handleMessage(Message msg)
//                {
//                    super.handleMessage(msg);
//                    if(msg.what==-2)
//                    {
//                        getLooper().quit();
//                        return;
//                    }
//                    decodeBitmap(msg.what);
//                }
//            };
            while (true) {
                try {
                    Mat toDetect = blockingQueueDetect.take();
                    long startTime = getTickCount();
                    Bitmap bitmap = detect(toDetect);
                    long endTime = getTickCount();
                    Log.i("Main   detectTime:", (endTime - startTime) / getTickFrequency() + " s");

                    startTime = getTickCount();
                    drawBitmap(bitmap);
                    endTime = getTickCount();
                    Log.i("Main   drawTime:", (endTime - startTime) / getTickFrequency() + " s");
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }
    private class SvCallback implements SurfaceHolder.Callback
    {
        private Thread produceBitmap;
        private Thread playBitmap;

        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            //full screen
            rect=new android.graphics.Rect(0,0,width,height);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            stopAnim();
        }
        public void startAnim()
        {
            produceBitmap = new Thread(new ProduceBitmap());
            produceBitmap.start();
            playBitmap = new Thread(new PlayBitmap());
            playBitmap.start();
        }
        public void stopAnim()
        {
            blockingQueueDetect.clear();
            play.setEnabled(true);
            //this is necessary
            if (produceBitmap.isAlive()) {
                produceBitmap.interrupt();
            }
            if (playBitmap.isAlive()) {
                playBitmap.interrupt();
            }
        }
    }
}
// 42 71 72 72 70 71 84 100 101 100 97 90 113 115 98 39 73 73 68 81 85 91 88 88
// 42 71 72 72 70 71 84 100 101 100 97 90 113 115 98 39 73 73 68 81 85 91 88 88