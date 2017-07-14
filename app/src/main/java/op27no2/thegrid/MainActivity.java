package op27no2.thegrid;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements ExoPlayer.EventListener {

    //general
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor edt;
    private StorageReference mStorageRef;
    private Context mContext;
    private Handler handler = new Handler();

    //media
    private ExoPlayer exoPlayer1;
    private ExoPlayer exoPlayers[] = new ExoPlayer[9];
    private BandwidthMeter bandwidthMeter;
    private TrackSelector trackSelector;
    private TrackSelection.Factory trackSelectionFactory;
    private SimpleExoPlayer player;
    private DataSource.Factory dataSourceFactory;
    private ExtractorsFactory extractorsFactory;
    private DefaultBandwidthMeter defaultBandwidthMeter;
    private MediaSource mediaSource;

    //UI
    private Button myButtons[] = new Button[9];
    private Button playButton;
    private Button audioButton;
    private Button stopButton;
    private TransferUtility transferUtility;

    //Control
    private Boolean selectPrimed = false;
    private int primeClicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        sharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        edt = sharedPreferences.edit();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        final int[] increment = {0};



        // Initialize the Amazon Cognito credentials provider
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-1:0017dc8f-a06d-4d3f-9b21-2f44d4ba7253", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );


        AmazonS3 s3 = new AmazonS3Client(credentialsProvider);

        transferUtility = new TransferUtility(s3, mContext);


        //set up button UI
        for(int i=0; i<9; i++) {
            int resID = getResources().getIdentifier("button"+(i), "id", "op27no2.thegrid");
            myButtons[i] = ((Button) findViewById(resID));
            System.out.println(resID+" "+myButtons[i]);
            setOnClick(myButtons[i], i);
        }


        Runnable mrunnable = new Runnable() {
            @Override
            public void run() {
                System.out.println("button width: " + myButtons[0].getWidth());
                LinearLayout ll1 = (LinearLayout) findViewById(R.id.row1);
                LinearLayout ll2 = (LinearLayout) findViewById(R.id.row2);
                LinearLayout ll3 = (LinearLayout) findViewById(R.id.row3);
                int height = myButtons[0].getWidth();
                ll1.getLayoutParams().height = height;
                ll2.getLayoutParams().height = height;
                ll3.getLayoutParams().height = height;

            }
        };
        handler.postDelayed(mrunnable, 50);


        stopButton = (Button) findViewById(R.id.button8);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            for(int i=0;i<9;i++){
                exoPlayers[i].setPlayWhenReady(false);
            }
            }
        });


        audioButton = (Button) findViewById(R.id.select_audio);
        audioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectPrimed = true;
                System.out.println("select primed pushed = "+selectPrimed);
            }
        });




        Uri file = Uri.fromFile(new File("path/to/images/rivers.jpg"));
        StorageReference riversRef = mStorageRef.child("images/rivers.jpg");




 /*       riversRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                    }
                });

        File localFile = File.createTempFile("images", "jpg");
        riversRef.getFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        // Successfully downloaded data to local file
                        // ...
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle failed download
                // ...
            }
        });*/


    }

    private void setOnClick(final Button btn, final int pos){
        btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    System.out.println("play pushed, primed = "+selectPrimed);

                    if(selectPrimed){
                        primeClicked = pos;
                        getAudio();
                    }else{
                        exoPlayers[pos].setPlayWhenReady(true);
                    }

                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(!selectPrimed) {
                        exoPlayers[pos].setPlayWhenReady(false);
                    }
                }


                return false;
            }
        });
    }

    public void downloadSamples(){
        TransferObserver observer = transferUtility.download(
                "thegridsamples",     /* The bucket to upload to */
                "file"+i,    /* The key for the uploaded object */
                mFile        /* The file where the data to upload exists */
        );

    }

    @Override
    public void onResume(){
        super.onResume();

    }

    private void setUpExoPlayer(File mFile, int pos) {
        bandwidthMeter = new DefaultBandwidthMeter();
        extractorsFactory = new DefaultExtractorsFactory();

        trackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);

        trackSelector = new DefaultTrackSelector(trackSelectionFactory);

        defaultBandwidthMeter = new DefaultBandwidthMeter();


        DataSpec dataSpec = new DataSpec(Uri.fromFile(mFile));
        final FileDataSource fileDataSource = new FileDataSource();
        try {
            fileDataSource.open(dataSpec);
        } catch (FileDataSource.FileDataSourceException e) {
            e.printStackTrace();
        }

        DataSource.Factory factory = new DataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                return fileDataSource;
            }
        };
        MediaSource mediaSource = new ExtractorMediaSource(fileDataSource.getUri(),
                factory, new DefaultExtractorsFactory(), null, null);


        exoPlayers[pos] = ExoPlayerFactory.newSimpleInstance(this, trackSelector);

        exoPlayers[pos].addListener(this);
        exoPlayers[pos].prepare(mediaSource);

        Log.v("TEST", "playing state : "+pos+" " + exoPlayers[pos].getPlaybackState());


    }


    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }


    private void getAudio() {
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {

            Uri uri = data.getData();
            File mFile = saveTempFile(uri, 1);

            TransferObserver observer = transferUtility.upload(
                    "thegridsamples",     /* The bucket to upload to */
                    "file"+primeClicked,    /* The key for the uploaded object */
                    mFile        /* The file where the data to upload exists */
            );
            observer.setTransferListener(new TransferListener(){

                @Override
                public void onStateChanged(int id, TransferState state) {
                    System.out.println("upload state changed");
                }

                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                    int percentage = (int) (bytesCurrent/bytesTotal * 100);
                    System.out.println("upload progress:"+percentage);
                }

                @Override
                public void onError(int id, Exception ex) {
                    System.out.println("upload error:"+ex.getMessage());
                }

            });


            setUpExoPlayer(mFile, primeClicked);
            selectPrimed = false;
        }
    }

    public static String getPathForAudio(Context context, Uri uri) {
        String result = null;
        Cursor cursor = null;

        try {
            String[] proj = {MediaStore.Audio.Media.DATA};
            cursor = context.getContentResolver().query(uri, proj, null, null, null);
            if (cursor == null) {
                result = uri.getPath();
            } else {
                cursor.moveToFirst();
                int column_index = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA);
                result = cursor.getString(column_index);
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }



    private File saveTempFile(Uri oldUri, int type) {
        ContentResolver resolver = getContentResolver();
        InputStream in = null;
        FileOutputStream out = null;
        File mFile2 = null;
        try {
            in = resolver.openInputStream(oldUri);
            // File mFile = new File(getActivity().getFilesDir().getAbsolutePath(), "temp_audio");
            if(type == 1) {
                mFile2 = File.createTempFile("testing",".wav");
            }
            if(type == 2) {
                mFile2 = File.createTempFile("testing",".jpg");
            }
            out = new FileOutputStream(mFile2, false);
            byte[] buffer = new byte[1024];
            int read;
            while((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
        } catch(IOException e) {
            System.out.println("error1 save: "+e.getMessage());
        } finally {
            if(in != null) {
                try { in.close(); } catch(IOException e) {
                    System.out.println("error2 save: " + e.getMessage());
                }
            }
            if(out != null) {
                try { out.close(); } catch(IOException e) {
                    System.out.println("error3 save: " + e.getMessage());
                }
            }
        }
        // Uri newUri = Uri.fromFile(mFile2);
        long size = mFile2.length();
        System.out.println("filesizepreupload"+size);
        return mFile2;
    }



}