package uk.co.alt236.btlescan_mini.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import uk.co.alt236.bluetoothlelib.device.BluetoothLeDevice;
import uk.co.alt236.btlescan_mini.R;
import uk.co.alt236.btlescan_mini.services.BluetoothLeService;

/**
 * Created by Станция on 23.06.2016.
 */
public class DeviceReadMyUUIDs_mini extends AppCompatActivity {

    public static String MY_GATT_SERVICE = "398506e1-98da-11e5-a837-0800200c9a66";
    public static String MY_GATT_CHARACTERISTIC1 = "398506e2-98da-11e5-a837-0800200c9a66"; //ERRORS
    public static String MY_GATT_CHARACTERISTIC2  = "398506e3-98da-11e5-a837-0800200c9a66";//TOF_DIFF , ns
    public static String MY_GATT_CHARACTERISTIC3  = "398506e4-98da-11e5-a837-0800200c9a66";//FLOW_SPEED , mm/s
    public static String MY_GATT_CHARACTERISTIC4  = "398506e5-98da-11e5-a837-0800200c9a66";//FLOW , l/h
    public static String MY_GATT_CHARACTERISTIC5  = "398506e6-98da-11e5-a837-0800200c9a66";//VOLUME m3
    public static String MY_GATT_CHARACTERISTIC6  = "398506e7-98da-11e5-a837-0800200c9a66";////COMMANDS
    private final Handler mHandler = new Handler();
    private Runnable mTimer1;
    private Runnable mTimer2;
    private Runnable mTimer3;
    private Button buttonRead;
    private Button buttonSelectGrapf;
    private Button buttonShowPeriod;
    private Button buttonShowPar;
    private GraphView graph;
    private LineGraphSeries<DataPoint> series;
    private Float[] grafbuffer = new Float[50000];
    private Long previous=0L;
    private Integer num = 0;
    private Integer NOWSCREEN = 1;
    private Integer[] bufferPlot = new Integer[50000];
    private Integer numPlot = 0;
    private Integer grapfTimeout = 500;
    private Integer grapfUpdateType = 2;

    public static final String EXTRA_DEVICE = "extra_device";
    private final static String TAG = DeviceControlActivity.class.getSimpleName();
    private static final String LIST_NAME = "NAME";
    private static final String LIST_UUID = "UUID";
    private byte[] dataSysErrors={0,0,0,0};

    protected TextView mDataAsArray;
    protected TextView mTextView1;
    protected TextView mTextView2;
    protected TextView mTextSignal;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private BluetoothGattCharacteristic mMyCharacteristic;
    private BluetoothGattCharacteristic mMyErrors;
    private BluetoothGattCharacteristic mMyCommands;
    private BluetoothGattCharacteristic mMyFlow;
    private BluetoothGattCharacteristic mMyVolume;
    private BluetoothGattService mMyService;
    private BluetoothLeService mBluetoothLeService;
    private List<List<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();

    private String mDeviceAddress;
    private String mDeviceName;
    private boolean mConnected = false;
    private String mExportString;
    private String hextechnical;
    private String hex_error_code ="Warning! NO Input Data!";
    private boolean isWriteNow=false;
    private boolean init_ok=false;
    private boolean isTechnical=false;

    private ProgressBar flow_in_percent;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_data_mini);
        NOWSCREEN=1;
        final Intent intent = getIntent();
        final BluetoothLeDevice device = intent.getParcelableExtra(EXTRA_DEVICE);
        mDeviceName = device.getName();
        mDeviceAddress = device.getAddress();

        /*buttonRead=(Button)findViewById(R.id.buttonShow);
        buttonRead.setBackgroundColor(getResources().getColor(R.color.button_down));*/
        if (NOWSCREEN==1) {
            overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
            Typeface type = Typeface.createFromAsset(getAssets(), "fonts/a_regular.ttf");
            TextView mTextView_tmp = (TextView) findViewById(R.id.textView7);
            mTextView_tmp.setTypeface(type);
            mTextView_tmp = (TextView) findViewById(R.id.textView8);
            mTextView_tmp.setTypeface(type);
            mTextView_tmp = (TextView) findViewById(R.id.textView9);
            mTextView_tmp.setTypeface(type);
            mTextView_tmp = (TextView) findViewById(R.id.textView10);
            mTextView_tmp.setTypeface(type);
            mTextView_tmp = (TextView) findViewById(R.id.textView11);
            mTextView_tmp.setTypeface(type);
            mTextView_tmp = (TextView) findViewById(R.id.textView12);
            mTextView_tmp.setTypeface(type);
            mTextView_tmp = (TextView) findViewById(R.id.textView13);
            mTextView_tmp.setTypeface(type);
            mTextView_tmp = (TextView) findViewById(R.id.textView14);
            mTextView_tmp.setTypeface(type);
            mTextView_tmp = (TextView) findViewById(R.id.textView27);
            mTextView_tmp.setTypeface(type);
            mTextView_tmp = (TextView) findViewById(R.id.textView26);
            mTextView_tmp.setTypeface(type);
        }

        DisplayMetrics display = getResources().getDisplayMetrics();
        float inches=display.heightPixels/display.ydpi;

        if (inches < 5.3 )
        {
            /*LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 45);
            buttonShowPeriod=(Button)findViewById(R.id.Show_period);
            buttonShowPeriod.setLayoutParams(lp);
            buttonShowPar=(Button)findViewById(R.id.select_grapf);
            buttonShowPar.setLayoutParams(lp);*/
            if (NOWSCREEN==1) {
                TextView mTextView_tmp = (TextView) findViewById(R.id.textView7);
                double size2 = mTextView_tmp.getTextSize();
                double size3 = mTextView_tmp.getTextSize() * (inches / 5.3);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                double size4 = mTextView_tmp.getTextSize();

                mTextView_tmp = (TextView) findViewById(R.id.textView8);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView9);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView10);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView11);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView12);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView13);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView14);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView27);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView26);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView22);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView23);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView24);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
            }
        }
        //mTextView2 = (TextView)findViewById(R.id.textView2);
   }
/*
    private void displaySelectGDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_grapf)
                .setItems(R.array.select_grapf, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        switch (which){
                            case 0:
                                //TOF_DIFF , ns
                                mBluetoothLeService.setCharacteristicNotification(mMyCharacteristic, false);
                                mMyCharacteristic =  mMyService.getCharacteristic(UUID.fromString(MY_GATT_CHARACTERISTIC2));
                                try {
                                    Thread.sleep(100, 0);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if (grapfUpdateType == 1 || grapfUpdateType == 2)mBluetoothLeService.setCharacteristicNotification(mMyCharacteristic, true);
                                num=0;
                                numPlot=0;
                                series = new LineGraphSeries<DataPoint>();
                                graph.removeAllSeries();
                                graph.addSeries(series);
                                graph.getViewport().setMinX(0);
                                buttonSelectGrapf = (Button)findViewById(R.id.select_grapf);
                                buttonSelectGrapf.setText(R.string.parTOF_DIFF);
                                break;
                            case 1:
                                //FLOW_SPEED , mm/s
                                mBluetoothLeService.setCharacteristicNotification(mMyCharacteristic, false);
                                mMyCharacteristic =  mMyService.getCharacteristic(UUID.fromString(MY_GATT_CHARACTERISTIC3));
                                try {
                                    Thread.sleep(100, 0);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if (grapfUpdateType == 1 || grapfUpdateType == 2)mBluetoothLeService.setCharacteristicNotification(mMyCharacteristic, true);
                                num=0;
                                numPlot=0;
                                series = new LineGraphSeries<DataPoint>();
                                graph.removeAllSeries();
                                graph.addSeries(series);
                                graph.getViewport().setMinX(0);
                                buttonSelectGrapf = (Button)findViewById(R.id.select_grapf);
                                buttonSelectGrapf.setText(R.string.parFLOW_SPEED);
                                break;
                            case 2:
                                //FLOW, L/H
                                mBluetoothLeService.setCharacteristicNotification(mMyCharacteristic, false);
                                mMyCharacteristic =  mMyService.getCharacteristic(UUID.fromString(MY_GATT_CHARACTERISTIC4));
                                try {
                                    Thread.sleep(100, 0);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if (grapfUpdateType == 1 || grapfUpdateType == 2)mBluetoothLeService.setCharacteristicNotification(mMyCharacteristic, true);
                                num=0;
                                numPlot=0;
                                series = new LineGraphSeries<DataPoint>();
                                graph.removeAllSeries();
                                graph.addSeries(series);
                                graph.getViewport().setMinX(0);
                                buttonSelectGrapf = (Button)findViewById(R.id.select_grapf);
                                buttonSelectGrapf.setText(R.string.parFLOW);
                                break;
                            case 3:
                                //VOLUME , m3
                                mBluetoothLeService.setCharacteristicNotification(mMyCharacteristic, false);
                                mMyCharacteristic =  mMyService.getCharacteristic(UUID.fromString(MY_GATT_CHARACTERISTIC5));
                                try {
                                    Thread.sleep(100, 0);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if (grapfUpdateType == 1 || grapfUpdateType == 2) mBluetoothLeService.setCharacteristicNotification(mMyCharacteristic, true);
                                num=0;
                                numPlot=0;
                                series = new LineGraphSeries<DataPoint>();
                                graph.removeAllSeries();
                                graph.addSeries(series);
                                graph.getViewport().setMinX(0);
                                buttonSelectGrapf = (Button)findViewById(R.id.select_grapf);
                                buttonSelectGrapf.setText(R.string.parVOLUME);
                                break;

                        }
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    */
    /*
    private void displayShowParDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.show_parameters)
                .setItems(R.array.show_parameters, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        switch (which){
                            case 0:
                                //timer
                                grapfUpdateType =0;
                                buttonShowPar = (Button)findViewById(R.id.Show_par);
                                buttonShowPar.setText(R.string.timer);
                                buttonShowPeriod.setClickable(true);
                                buttonShowPeriod.setText(R.string.par500ms);
                                grapfTimeout=500;
                                mBluetoothLeService.setCharacteristicNotification(mMyCharacteristic, false);
                                break;
                            case 1:
                                //notify
                                grapfUpdateType =1;
                                buttonShowPar = (Button)findViewById(R.id.Show_par);
                                buttonShowPar.setText(R.string.notify);
                                buttonShowPeriod = (Button)findViewById(R.id.Show_period);
                                buttonShowPeriod.setClickable(false);
                                buttonShowPeriod.setText(R.string.parPeriodAuto);
                                mBluetoothLeService.setCharacteristicNotification(mMyCharacteristic, true);
                                break;
                            case 2:
                                //notify+timer
                                grapfUpdateType =2;
                                buttonShowPar = (Button)findViewById(R.id.Show_par);
                                buttonShowPar.setText(R.string.timernotify);
                                buttonShowPeriod.setClickable(true);
                                buttonShowPeriod.setText(R.string.par500ms);
                                grapfTimeout=500;
                                mBluetoothLeService.setCharacteristicNotification(mMyCharacteristic, true);
                                break;

                        }
                        num=0;
                        numPlot=0;
                        series = new LineGraphSeries<DataPoint>();
                        graph.removeAllSeries();
                        graph.addSeries(series);
                        graph.getViewport().setMinX(0);
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
*/
    /*
    private void displayShowPeriodDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.show_period)
                .setItems(R.array.show_period, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        switch (which){
                            case 0:
                                //100ms
                                grapfTimeout=100;
                                buttonShowPeriod = (Button)findViewById(R.id.Show_period);
                                buttonShowPeriod.setText(R.string.par100ms);
                                break;
                            case 1:
                                //500ms
                                grapfTimeout=500;
                                buttonShowPeriod = (Button)findViewById(R.id.Show_period);
                                buttonShowPeriod.setText(R.string.par500ms);
                                break;
                            case 2:
                                //1000ms
                                grapfTimeout=1000;
                                buttonShowPeriod = (Button)findViewById(R.id.Show_period);
                                buttonShowPeriod.setText(R.string.par1000ms);
                                break;
                            case 3:
                                //2000ms
                                grapfTimeout=2000;
                                buttonShowPeriod = (Button)findViewById(R.id.Show_period);
                                buttonShowPeriod.setText(R.string.par2000ms);
                                break;
                            case 4:
                                //5000ms
                                grapfTimeout=5000;
                                buttonShowPeriod = (Button)findViewById(R.id.Show_period);
                                buttonShowPeriod.setText(R.string.par5000ms);
                                break;
                            case 5:
                                //10000ms
                                grapfTimeout=10000;
                                buttonShowPeriod = (Button)findViewById(R.id.Show_period);
                                buttonShowPeriod.setText(R.string.par10000ms);
                                break;
                            case 6:
                                //30000ms
                                grapfTimeout=30000;
                                buttonShowPeriod = (Button)findViewById(R.id.Show_period);
                                buttonShowPeriod.setText(R.string.par30000ms);
                                break;

                        }
                        num=0;
                        numPlot=0;
                        series = new LineGraphSeries<DataPoint>();
                        graph.removeAllSeries();
                        graph.addSeries(series);
                        graph.getViewport().setMinX(0);
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }*/
    /*
    public void onShowParClick (View view)
    {
        //view.playSoundEffect(SoundEffectConstants.CLICK);
        displayShowParDialog();
    }*/
    public void onDataClick (View view) {
        setContentView(R.layout.activity_show_data_mini);
        NOWSCREEN=1;

        if (NOWSCREEN==1) {
            Typeface type = Typeface.createFromAsset(getAssets(), "fonts/a_regular.ttf");
            TextView mTextView_tmp = (TextView) findViewById(R.id.textView7);
            mTextView_tmp.setTypeface(type);
            mTextView_tmp = (TextView) findViewById(R.id.textView8);
            mTextView_tmp.setTypeface(type);
            mTextView_tmp = (TextView) findViewById(R.id.textView9);
            mTextView_tmp.setTypeface(type);
            mTextView_tmp.setText(hextechnical);
            mTextView_tmp = (TextView) findViewById(R.id.textView10);
            mTextView_tmp.setTypeface(type);
            mTextView_tmp = (TextView) findViewById(R.id.textView11);
            mTextView_tmp.setTypeface(type);
            mTextView_tmp = (TextView) findViewById(R.id.textView12);
            mTextView_tmp.setTypeface(type);
            mTextView_tmp = (TextView) findViewById(R.id.textView13);
            mTextView_tmp.setTypeface(type);
            mTextView_tmp = (TextView) findViewById(R.id.textView14);
            mTextView_tmp.setTypeface(type);
            mTextView_tmp = (TextView) findViewById(R.id.textView27);
            mTextView_tmp.setTypeface(type);
            mTextView_tmp = (TextView) findViewById(R.id.textView26);
            mTextView_tmp.setTypeface(type);
        }

        DisplayMetrics display = getResources().getDisplayMetrics();
        float inches=display.heightPixels/display.ydpi;

        if (inches < 5.3 )
        {
            /*LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 45);
            buttonShowPeriod=(Button)findViewById(R.id.Show_period);
            buttonShowPeriod.setLayoutParams(lp);
            buttonShowPar=(Button)findViewById(R.id.select_grapf);
            buttonShowPar.setLayoutParams(lp);*/
            if (NOWSCREEN==1) {
                TextView mTextView_tmp = (TextView) findViewById(R.id.textView7);
                double size2 = mTextView_tmp.getTextSize();
                double size3 = mTextView_tmp.getTextSize() * (inches / 5.3);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                double size4 = mTextView_tmp.getTextSize();

                mTextView_tmp = (TextView) findViewById(R.id.textView8);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView9);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView10);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView11);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView12);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView13);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView14);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView27);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView26);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));

                mTextView_tmp = (TextView) findViewById(R.id.textView22);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView23);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView24);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
            }
        }
    }

    public void onErrorsClick (View view)
    {
        //view.playSoundEffect(SoundEffectConstants.CLICK);
        setContentView(R.layout.activity_show_data_mini_2);
        NOWSCREEN=3;

        Typeface type = Typeface.createFromAsset(getAssets(), "fonts/a_regular.ttf");
        TextView mTextView_tmp = (TextView) findViewById(R.id.textView32);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView33);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView34);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView35);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView36);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView37);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView38);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView39);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView40);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView41);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView42);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView43);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView44);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView45);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView46);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView47);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView48);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView49);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView50);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView51);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView52);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView53);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView54);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView55);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView56);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView57);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView58);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView59);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView60);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView61);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView62);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView63);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView64);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView65);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView66);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView67);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView68);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView69);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView70);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView71);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView72);
        mTextView_tmp.setTypeface(type);

        DisplayMetrics display = getResources().getDisplayMetrics();
        float inches=display.heightPixels/display.ydpi;

        if (inches < 5.3 )
        {
            /*LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 45);
            buttonShowPeriod=(Button)findViewById(R.id.Show_period);
            buttonShowPeriod.setLayoutParams(lp);
            buttonShowPar=(Button)findViewById(R.id.select_grapf);
            buttonShowPar.setLayoutParams(lp);*/
            if (NOWSCREEN==3) {
                mTextView_tmp = (TextView) findViewById(R.id.textView32);
                //double size2 = mTextView_tmp.getTextSize();
                //double size3 = mTextView_tmp.getTextSize() * (inches / 5.3);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                //double size4 = mTextView_tmp.getTextSize();

                mTextView_tmp = (TextView) findViewById(R.id.textView33);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView34);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView35);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView36);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView37);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));

                mTextView_tmp = (TextView) findViewById(R.id.textView38);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView39);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView40);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView41);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView42);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView43);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView44);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView45);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView46);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView47);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView48);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView49);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView50);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView51);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView52);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView53);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView54);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView55);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView56);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView57);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView58);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView59);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView60);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView61);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView62);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView63);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView64);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView65);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView66);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView67);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView68);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView69);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView70);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView71);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView72);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));

                mTextView_tmp = (TextView) findViewById(R.id.textView222);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView223);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView224);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
            }
        }
    }
    public void onGraphicClick (View view)
    {
        //view.playSoundEffect(SoundEffectConstants.CLICK);
        setContentView(R.layout.activity_show_data_mini_1);
        NOWSCREEN=2;
        // Линейный график
        graph = (GraphView) findViewById(R.id.graph);
        series = new LineGraphSeries<DataPoint>();
        graph.addSeries(series);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getGridLabelRenderer().setNumHorizontalLabels(2);

        Typeface type = Typeface.createFromAsset(getAssets(), "fonts/a_regular.ttf");
        TextView mTextView_tmp = (TextView) findViewById(R.id.textView28);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView29);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView30);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView31);
        mTextView_tmp.setTypeface(type);

        mTextView_tmp = (TextView) findViewById(R.id.textView101);
        mTextView_tmp.setTypeface(type);
        mTextView_tmp = (TextView) findViewById(R.id.textView102);
        mTextView_tmp.setTypeface(type);

        DisplayMetrics display = getResources().getDisplayMetrics();
        float inches=display.heightPixels/display.ydpi;

        if (inches < 5.3 )
        {
            /*LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 45);
            buttonShowPeriod=(Button)findViewById(R.id.Show_period);
            buttonShowPeriod.setLayoutParams(lp);
            buttonShowPar=(Button)findViewById(R.id.select_grapf);
            buttonShowPar.setLayoutParams(lp);*/
            if (NOWSCREEN==2) {
                mTextView_tmp = (TextView) findViewById(R.id.textView28);
                //double size2 = mTextView_tmp.getTextSize();
                //double size3 = mTextView_tmp.getTextSize() * (inches / 5.3);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                //double size4 = mTextView_tmp.getTextSize();

                mTextView_tmp = (TextView) findViewById(R.id.textView29);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView30);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView31);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView101);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView102);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));

                graph.setTitleTextSize((float) (graph.getTitleTextSize() * (inches / 5.3)));

                mTextView_tmp = (TextView) findViewById(R.id.textView122);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView123);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
                mTextView_tmp = (TextView) findViewById(R.id.textView124);
                mTextView_tmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (mTextView_tmp.getTextSize() * (inches / 5.3)));
            }
        }

    }
/*
    public void onShowPeriodClick (View view)
    {
        //view.playSoundEffect(SoundEffectConstants.CLICK);
        displayShowPeriodDialog();
    }

    public void onSelectGclick (View view)
    {
        //view.playSoundEffect(SoundEffectConstants.CLICK);
        displaySelectGDialog();
    }
    public void onSCANclick(View view)
    {
        //view.playSoundEffect(SoundEffectConstants.CLICK);
        if (init_ok) {
            final Intent intent = getIntent();
            final BluetoothLeDevice device = intent.getParcelableExtra(EXTRA_DEVICE);
            final Intent intent2 = new Intent(this, MainActivity.class);
            intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent2);
        }
    }

    public void onSHOWclick(View view)
    {
        //view.playSoundEffect(SoundEffectConstants.CLICK);
    }*/


    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        // super.onBackPressed();
        openQuitDialog();
    }


    private void openQuitDialog() {

        AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                DeviceReadMyUUIDs_mini.this);
        quitDialog.setTitle("Close the app?");

        quitDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                //finish();
                finishAffinity ();
            }
        });

        quitDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
            }
        });

        quitDialog.show();
    }

    public void onSysClick(View view)
    {
        /*final int paddingSizeDp = 5;
        final float scale = getResources().getDisplayMetrics().density;
        final int dpAsPixels = (int) (paddingSizeDp * scale + 0.5f);
        final TextView textView = new TextView(this);
        final SpannableString text = new SpannableString(hex_error_code);

        //view.playSoundEffect(SoundEffectConstants.CLICK);
        textView.setText(text);
        textView.setAutoLinkMask(RESULT_OK);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setPadding(dpAsPixels, dpAsPixels, dpAsPixels, dpAsPixels);

        //Linkify.addLinks(text, Linkify.ALL);
        new AlertDialog.Builder(this)
                .setTitle("Errors report")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                    }
                })
                .setView(textView)
                .show();*/

        getDialog(DeviceReadMyUUIDs_mini.this).show();
    }

    AlertDialog getDialog(Activity activity) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final SpannableString text = new SpannableString(hex_error_code);
        TextView textView_tmp;
        View view = activity.getLayoutInflater().inflate(R.layout.errors_flags, null); // Получаем layout по его ID

        textView_tmp = (TextView)view.findViewById(R.id.editText36);
        textView_tmp.setText(text);

        if ((dataSysErrors[3] & (1)) == 1){
             textView_tmp = (TextView)view.findViewById(R.id.editText4);
             textView_tmp.setText("FAIL");
             textView_tmp.setTextColor(getResources().getColor(R.color.red));
        }
        if (((dataSysErrors[3]>>1) & 1) == 1){
             textView_tmp = (TextView)view.findViewById(R.id.editText6);
             textView_tmp.setText("FAIL");
             textView_tmp.setTextColor(getResources().getColor(R.color.red));
        }
        if (((dataSysErrors[3]>>2) & 1) == 1){
             textView_tmp = (TextView)view.findViewById(R.id.editText8);
             textView_tmp.setText("FAIL");
             textView_tmp.setTextColor(getResources().getColor(R.color.red));
        }
         if (((dataSysErrors[3]>>3) & 1) == 1){
             textView_tmp = (TextView)view.findViewById(R.id.editText10);
             textView_tmp.setText("FAIL");
             textView_tmp.setTextColor(getResources().getColor(R.color.red));
         }
         if (((dataSysErrors[3]>>4) & 1) == 1){
             textView_tmp = (TextView)view.findViewById(R.id.editText12);
             textView_tmp.setText("FAIL");
             textView_tmp.setTextColor(getResources().getColor(R.color.red));
         }
         if (((dataSysErrors[3]>>5) & 1) == 1){
             textView_tmp = (TextView)view.findViewById(R.id.editText14);
             textView_tmp.setText("FAIL");
             textView_tmp.setTextColor(getResources().getColor(R.color.red));
         }
         if (((dataSysErrors[3]>>6) & 1) == 1){
             textView_tmp = (TextView)view.findViewById(R.id.editText16);
             textView_tmp.setText("FAIL");
             textView_tmp.setTextColor(getResources().getColor(R.color.red));
         }
         if (((dataSysErrors[3]>>7) & 1) == 1){
             textView_tmp = (TextView)view.findViewById(R.id.editText18);
             textView_tmp.setText("FAIL");
             textView_tmp.setTextColor(getResources().getColor(R.color.red));
         }
         if ((dataSysErrors[2] & 1) == 1){
             textView_tmp = (TextView)view.findViewById(R.id.editText20);
             textView_tmp.setText("FAIL");
             textView_tmp.setTextColor(getResources().getColor(R.color.red));
         }
         if (((dataSysErrors[2]>>1) & 1) == 1){
             textView_tmp = (TextView)view.findViewById(R.id.editText22);
             textView_tmp.setText("FAIL");
             textView_tmp.setTextColor(getResources().getColor(R.color.red));
         }
         if (((dataSysErrors[2]>>2) & 1) == 1){
             textView_tmp = (TextView)view.findViewById(R.id.editText24);
             textView_tmp.setText("FAIL");
             textView_tmp.setTextColor(getResources().getColor(R.color.red));
         }
         if (((dataSysErrors[2]>>3) & 1) == 1){
             textView_tmp = (TextView)view.findViewById(R.id.editText26);
             textView_tmp.setText("FAIL");
             textView_tmp.setTextColor(getResources().getColor(R.color.red));
         }
         if (((dataSysErrors[2]>>4) & 1) == 1){
             textView_tmp = (TextView)view.findViewById(R.id.editText28);
             textView_tmp.setText("FAIL");
             textView_tmp.setTextColor(getResources().getColor(R.color.red));
         }
         if (((dataSysErrors[2]>>5) & 1) == 1){
             textView_tmp = (TextView)view.findViewById(R.id.editText30);
             textView_tmp.setText("FAIL");
             textView_tmp.setTextColor(getResources().getColor(R.color.red));
         }
         if (((dataSysErrors[2]>>6) & 1) == 1){
             textView_tmp = (TextView)view.findViewById(R.id.editText32);
             textView_tmp.setText("FAIL");
             textView_tmp.setTextColor(getResources().getColor(R.color.red));
         }
         if (((dataSysErrors[2]>>7) & 1) == 1){
             textView_tmp = (TextView)view.findViewById(R.id.editText34);
             textView_tmp.setText("FAIL");
             textView_tmp.setTextColor(getResources().getColor(R.color.red));
         }
         if ((dataSysErrors[1] & 1) == 1){
             textView_tmp = (TextView)view.findViewById(R.id.editText38);
             textView_tmp.setText("FAIL");
             textView_tmp.setTextColor(getResources().getColor(R.color.red));
         }
         if (((dataSysErrors[1]>>1) & 1) == 1){
             textView_tmp = (TextView)view.findViewById(R.id.editText40);
             textView_tmp.setText("FAIL");
             textView_tmp.setTextColor(getResources().getColor(R.color.red));
         }
        builder.setView(view);
        builder.setTitle("System report");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { // Кнопка ОК
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);

        return builder.create();
    }
    public void updateERRORS(){
        SpannableString text = new SpannableString(hex_error_code);
        TextView textView_tmp;

        textView_tmp = (TextView)findViewById(R.id.textView32);
        textView_tmp.setText(text);

        if ((dataSysErrors[3] & (1)) == 1){
            textView_tmp = (TextView)findViewById(R.id.textView33);
            textView_tmp.setText("ERR");
            textView_tmp.setTextColor(getResources().getColor(R.color.red));
        } else
        {
            textView_tmp = (TextView)findViewById(R.id.textView33);
            textView_tmp.setText("OK");
            textView_tmp.setTextColor(getResources().getColor(R.color.black));
        }
        if (((dataSysErrors[3]>>1) & 1) == 1){
            textView_tmp = (TextView)findViewById(R.id.textView41);
            textView_tmp.setText("ERR");
            textView_tmp.setTextColor(getResources().getColor(R.color.red));
        } else
        {
            textView_tmp = (TextView)findViewById(R.id.textView41);
            textView_tmp.setText("OK");
            textView_tmp.setTextColor(getResources().getColor(R.color.black));
        }
        if (((dataSysErrors[3]>>2) & 1) == 1){
            textView_tmp = (TextView)findViewById(R.id.textView49);
            textView_tmp.setText("ERR");
            textView_tmp.setTextColor(getResources().getColor(R.color.red));
        } else
        {
            textView_tmp = (TextView)findViewById(R.id.textView49);
            textView_tmp.setText("OK");
            textView_tmp.setTextColor(getResources().getColor(R.color.black));
        }
        if (((dataSysErrors[3]>>3) & 1) == 1){
            textView_tmp = (TextView)findViewById(R.id.textView57);
            textView_tmp.setText("ERR");
            textView_tmp.setTextColor(getResources().getColor(R.color.red));
        } else
        {
            textView_tmp = (TextView)findViewById(R.id.textView57);
            textView_tmp.setText("OK");
            textView_tmp.setTextColor(getResources().getColor(R.color.black));
        }
        if (((dataSysErrors[3]>>4) & 1) == 1){
            textView_tmp = (TextView)findViewById(R.id.textView65);
            textView_tmp.setText("ERR");
            textView_tmp.setTextColor(getResources().getColor(R.color.red));
        } else
        {
            textView_tmp = (TextView)findViewById(R.id.textView65);
            textView_tmp.setText("OK");
            textView_tmp.setTextColor(getResources().getColor(R.color.black));
        }
        if (((dataSysErrors[3]>>5) & 1) == 1){
            textView_tmp = (TextView)findViewById(R.id.textView35);
            textView_tmp.setText("ERR");
            textView_tmp.setTextColor(getResources().getColor(R.color.red));
        } else
        {
            textView_tmp = (TextView)findViewById(R.id.textView35);
            textView_tmp.setText("OK");
            textView_tmp.setTextColor(getResources().getColor(R.color.black));
        }
        if (((dataSysErrors[3]>>6) & 1) == 1){
            textView_tmp = (TextView)findViewById(R.id.textView43);
            textView_tmp.setText("ERR");
            textView_tmp.setTextColor(getResources().getColor(R.color.red));
        } else
        {
            textView_tmp = (TextView)findViewById(R.id.textView43);
            textView_tmp.setText("OK");
            textView_tmp.setTextColor(getResources().getColor(R.color.black));
        }
        if (((dataSysErrors[3]>>7) & 1) == 1){
            textView_tmp = (TextView)findViewById(R.id.textView51);
            textView_tmp.setText("ERR");
            textView_tmp.setTextColor(getResources().getColor(R.color.red));
        } else
        {
            textView_tmp = (TextView)findViewById(R.id.textView51);
            textView_tmp.setText("OK");
            textView_tmp.setTextColor(getResources().getColor(R.color.black));
        }
        if ((dataSysErrors[2] & 1) == 1){
            textView_tmp = (TextView)findViewById(R.id.textView59);
            textView_tmp.setText("ERR");
            textView_tmp.setTextColor(getResources().getColor(R.color.red));
        } else
        {
            textView_tmp = (TextView)findViewById(R.id.textView59);
            textView_tmp.setText("OK");
            textView_tmp.setTextColor(getResources().getColor(R.color.black));
        }
        if (((dataSysErrors[2]>>1) & 1) == 1){
            textView_tmp = (TextView)findViewById(R.id.textView67);
            textView_tmp.setText("ERR");
            textView_tmp.setTextColor(getResources().getColor(R.color.red));
        } else
        {
            textView_tmp = (TextView)findViewById(R.id.textView67);
            textView_tmp.setText("OK");
            textView_tmp.setTextColor(getResources().getColor(R.color.black));
        }
        if (((dataSysErrors[2]>>2) & 1) == 1){
            textView_tmp = (TextView)findViewById(R.id.textView37);
            textView_tmp.setText("ERR");
            textView_tmp.setTextColor(getResources().getColor(R.color.red));
        } else
        {
            textView_tmp = (TextView)findViewById(R.id.textView37);
            textView_tmp.setText("OK");
            textView_tmp.setTextColor(getResources().getColor(R.color.black));
        }
        if (((dataSysErrors[2]>>3) & 1) == 1){
            textView_tmp = (TextView)findViewById(R.id.textView45);
            textView_tmp.setText("ERR");
            textView_tmp.setTextColor(getResources().getColor(R.color.red));
        } else
        {
            textView_tmp = (TextView)findViewById(R.id.textView45);
            textView_tmp.setText("OK");
            textView_tmp.setTextColor(getResources().getColor(R.color.black));
        }
        if (((dataSysErrors[2]>>4) & 1) == 1){
            textView_tmp = (TextView)findViewById(R.id.textView53);
            textView_tmp.setText("ERR");
            textView_tmp.setTextColor(getResources().getColor(R.color.red));
        } else
        {
            textView_tmp = (TextView)findViewById(R.id.textView53);
            textView_tmp.setText("OK");
            textView_tmp.setTextColor(getResources().getColor(R.color.black));
        }
        if (((dataSysErrors[2]>>5) & 1) == 1){
            textView_tmp = (TextView)findViewById(R.id.textView61);
            textView_tmp.setText("ERR");
            textView_tmp.setTextColor(getResources().getColor(R.color.red));
        } else
        {
            textView_tmp = (TextView)findViewById(R.id.textView61);
            textView_tmp.setText("OK");
            textView_tmp.setTextColor(getResources().getColor(R.color.black));
        }
        if (((dataSysErrors[2]>>6) & 1) == 1){
            textView_tmp = (TextView)findViewById(R.id.textView69);
            textView_tmp.setText("ERR");
            textView_tmp.setTextColor(getResources().getColor(R.color.red));
        } else
        {
            textView_tmp = (TextView)findViewById(R.id.textView69);
            textView_tmp.setText("OK");
            textView_tmp.setTextColor(getResources().getColor(R.color.black));
        }
        if (((dataSysErrors[2]>>7) & 1) == 1){
            textView_tmp = (TextView)findViewById(R.id.textView39);
            textView_tmp.setText("ERR");
            textView_tmp.setTextColor(getResources().getColor(R.color.red));
        } else
        {
            textView_tmp = (TextView)findViewById(R.id.textView39);
            textView_tmp.setText("OK");
            textView_tmp.setTextColor(getResources().getColor(R.color.black));
        }
        if ((dataSysErrors[1] & 1) == 1){
            textView_tmp = (TextView)findViewById(R.id.textView47);
            textView_tmp.setText("ERR");
            textView_tmp.setTextColor(getResources().getColor(R.color.red));
        } else
        {
            textView_tmp = (TextView)findViewById(R.id.textView47);
            textView_tmp.setText("OK");
            textView_tmp.setTextColor(getResources().getColor(R.color.black));
        }
        if (((dataSysErrors[1]>>1) & 1) == 1){
            textView_tmp = (TextView)findViewById(R.id.textView55);
            textView_tmp.setText("ERR");
            textView_tmp.setTextColor(getResources().getColor(R.color.red));
        } else
        {
            textView_tmp = (TextView)findViewById(R.id.textView55);
            textView_tmp.setText("OK");
            textView_tmp.setTextColor(getResources().getColor(R.color.black));
        }/*
        builder.setView(view);
        builder.setTitle("System report");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { // Кнопка ОК
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);

        return builder.create();*/
    }

    public void ErrorGetCharacteristic()
    {
        final int paddingSizeDp = 5;
        final float scale = getResources().getDisplayMetrics().density;
        final int dpAsPixels = (int) (paddingSizeDp * scale + 0.5f);
        final TextView textView = new TextView(this);
        final SpannableString text = new SpannableString("Fail to get carasteristic of the flowmeter!\nReboot your device and try again.\n");

        textView.setText(text);
        textView.setAutoLinkMask(RESULT_OK);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setPadding(dpAsPixels, dpAsPixels, dpAsPixels, dpAsPixels);

        //Linkify.addLinks(text, Linkify.ALL);
        new AlertDialog.Builder(this)
                .setTitle("Error!")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                    }
                })
                .setView(textView)
                .show();
    }
/*
    public void onCONFclick(View view)
    {
        //view.playSoundEffect(SoundEffectConstants.CLICK);
        if (init_ok) {
            final Intent intent = getIntent();
            final BluetoothLeDevice device = intent.getParcelableExtra(EXTRA_DEVICE);
            final Intent intent2 = new Intent(this, DeviceWriteMyUUIDs.class);
            intent2.putExtra(DeviceWriteMyUUIDs.EXTRA_DEVICE, device);
            intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent2);
        }
    }

    public void onINFOclick(View view)
    {
        //view.playSoundEffect(SoundEffectConstants.CLICK);
        if (init_ok) {
            final Intent intent = getIntent();
            final BluetoothLeDevice device = intent.getParcelableExtra(EXTRA_DEVICE);
            final Intent intent2 = new Intent(this, DeviceControlActivity.class);
            intent2.putExtra(DeviceControlActivity.EXTRA_DEVICE, device);
            intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent2);
        }
    }

*/

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName componentName, final IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(final ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.
    //					      this can be a result of read or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
            } else if (BluetoothLeService.ACTION_CHARACTERISTIC_WRITE.equals(action)) {
                isWriteNow = false;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // По истечении времени, запускаем read
                        mBluetoothLeService.readCharacteristic(mMyCommands);
                    }
                }, 1000);

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(mBluetoothLeService.getSupportedGattServices());

                mMyService = mBluetoothLeService.getGattService(UUID.fromString(MY_GATT_SERVICE));
                mMyCharacteristic =  mMyService.getCharacteristic(UUID.fromString(MY_GATT_CHARACTERISTIC4));
                mMyErrors = mMyService.getCharacteristic(UUID.fromString(MY_GATT_CHARACTERISTIC1));
                mMyCommands = mMyService.getCharacteristic(UUID.fromString(MY_GATT_CHARACTERISTIC6));
                mMyVolume = mMyService.getCharacteristic(UUID.fromString(MY_GATT_CHARACTERISTIC5));


               //ErrorGetCharacteristic();

                if (mBluetoothLeService!=null && mMyCharacteristic!=null)
                {
                    ReadMaxFlow();
                   //delay100ms();
                }
                else
                    ErrorGetCharacteristic();

                //buttonShowPeriod = (Button)findViewById(R.id.Show_period);
                //buttonShowPeriod.setText(R.string.par500ms);
                grapfTimeout=500;
                grapfUpdateType =0;// on timer=0 on notyfy=1 both=2

                /*buttonShowPar = (Button)findViewById(R.id.Show_par);
                buttonShowPar.setText(R.string.timer);
                buttonShowPeriod.setClickable(true);
                buttonShowPar.setVisibility(View.INVISIBLE);
                buttonShowPar.setClickable(false);*/

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                final String noData = getString(R.string.no_data);
                final String uuid = intent.getStringExtra(BluetoothLeService.EXTRA_UUID_CHAR);
                final byte[] dataArr = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA_RAW);

                if (uuid.equals(MY_GATT_CHARACTERISTIC1)) {

                    /*hex_error_code = String.format("Input HEX ERROR CODE value: 0x%02X%02X%02X%02X%02X%02X%02X%02X", dataArr[0] & 0xFF, dataArr[1] & 0xFF, dataArr[2] & 0xFF, dataArr[3] & 0xFF, dataArr[4] & 0xFF, dataArr[5] & 0xFF, dataArr[6] & 0xFF, dataArr[7] & 0xFF);
                    if (dataArr[0]==0 && dataArr[0]==0 && dataArr[0]==0 && dataArr[0]==0
                            && dataArr[0]==0 && dataArr[0]==0 && dataArr[0]==0 && dataArr[0]==0)
                    {
                        mTextSignal = (TextView)findViewById(R.id.textSignal);
                        mTextSignal.setTextColor(0xFF6E6D6D);
                        mTextSignal.setText("SYS: Ok");
                    } else {
                        mTextSignal = (TextView)findViewById(R.id.textSignal);
                        mTextSignal.setTextColor(getResources().getColor(R.color.red));
                        mTextSignal.setText("SYS: Er");
                    }*/
                    //Input HEX ERROR CODE value:
                    hex_error_code = String.format("Full ERROR code: 0x%02X%02X%02X%02X", dataArr[0] & 0xFF, dataArr[1] & 0xFF, dataArr[2] & 0xFF, dataArr[3] & 0xFF);
                    dataSysErrors[0]=dataArr[0];
                    dataSysErrors[1]=dataArr[1];
                    dataSysErrors[2]=dataArr[2];
                    dataSysErrors[3]=dataArr[3];
                    if (NOWSCREEN==1) {
                        if (dataArr[0] == 0 && dataArr[1] == 0 && dataArr[2] == 0 && dataArr[3] == 0) {
                            mTextSignal = (TextView) findViewById(R.id.textView27);
                            //mTextSignal.setTextColor(0xFF6E6D6D);
                            mTextSignal.setText("");
                        } else {
                            mTextSignal = (TextView) findViewById(R.id.textView27);
                            //mTextSignal.setTextColor(getResources().getColor(R.color.red));
                            mTextSignal.setText("ERROR");
                        }
                    }
                }

                if (uuid.equals(MY_GATT_CHARACTERISTIC2)) {

                    Long longpart = Long.valueOf(dataArr[2] & 0xFF) * 256  + Long.valueOf(dataArr[3] & 0xFF);
                    Float floatpart = longpart.floatValue() / 65535 * 1000;
                    Integer Intpart = floatpart.intValue();

                    Long longpart_1 = Long.valueOf(dataArr[0] & 0xFF) * 256  + Long.valueOf(dataArr[1] & 0xFF);
                    if (longpart_1>=(65536/2) && longpart_1 != 65535) longpart_1=longpart_1-65535;
                    Boolean isminus=false;
                    if (longpart_1>=(65536/2) && longpart_1 == 65535) {longpart_1=longpart_1-65535;isminus=true;}

                    String hexdata = String.format("Input HEX value: 0x%02X%02X%02X%02X", dataArr[0] & 0xFF, dataArr[1] & 0xFF, dataArr[2] & 0xFF, dataArr[3] & 0xFF);
                    String btdata = String.format("%d.%03d ns", /*((dataArr[0] & 0xFF)  * 256) + (dataArr[1] & 0xFF)*/longpart_1,/* (((dataArr[2] & 0xFF)  * 256) + (dataArr[3] & 0xFF)) / 100*/Intpart);
                    //mTextView1 = (TextView) findViewById(R.id.textView1);
                    //mTextView1.setText(ByteUtils.byteArrayToHexString(dataArr));
                    //mTextView1.setText(hexdata);
                    //mTextView2 = (TextView) findViewById(R.id.textView2);
                    //mTextView2.setText(btdata);

                    //Long longpart_f = Long.valueOf(dataArr[0] & 0xFF) * 256  + Long.valueOf(dataArr[1] & 0xFF);
                    if (longpart_1>0)
                        grafbuffer[num] = longpart_1.floatValue()+longpart.floatValue() / 65535;//longpart_f.floatValue() + floatpart / 1000;
                    if (longpart_1<0)
                        grafbuffer[num] = longpart_1.floatValue()-longpart.floatValue() / 65535;//longpart_f.floatValue() + floatpart / 1000;
                    if (longpart_1==0){
                        if (isminus) grafbuffer[num] = longpart_1.floatValue()-longpart.floatValue() / 65535;
                        else grafbuffer[num] = longpart_1.floatValue()+longpart.floatValue() / 65535;
                    }

                    if (num > 49998) {
                        num = 0;
                        numPlot=0;
                    }
                    num++;
                }

                if (uuid.equals(MY_GATT_CHARACTERISTIC3)) {

                    Long longpart = Long.valueOf(dataArr[2] & 0xFF) * 256  + Long.valueOf(dataArr[3] & 0xFF);
                    Float floatpart = longpart.floatValue() / 65535 * 1000;
                    Integer Intpart = floatpart.intValue();

                    Long longpart_1 = Long.valueOf(dataArr[0] & 0xFF) * 256  + Long.valueOf(dataArr[1] & 0xFF);
                    if (longpart_1>=(65536/2) && longpart_1 != 65535)longpart_1=longpart_1-65535;
                    Boolean isminus=false;
                    if (longpart_1>=(65536/2) && longpart_1 == 65535) {longpart_1=longpart_1-65535;isminus=true;}

                    String hexdata = String.format("Input HEX value: 0x%02X%02X%02X%02X", dataArr[0] & 0xFF, dataArr[1] & 0xFF, dataArr[2] & 0xFF, dataArr[3] & 0xFF);
                    String btdata = String.format("%d.%03d mm/s", /*(dataArr[0] & 0xFF) * 256) + (dataArr[1] & 0xFF)*/longpart_1,/* (((dataArr[2] & 0xFF) * 256) + (dataArr[3] & 0xFF)) / 100*/Intpart);
                    //mTextView1 = (TextView) findViewById(R.id.textView1);
                    //mTextView1.setText(hexdata);
                    //mTextView2 = (TextView) findViewById(R.id.textView2);
                    //mTextView2.setText(btdata);

                    //Long longpart_f = Long.valueOf(dataArr[0] & 0xFF) * 256  + Long.valueOf(dataArr[1] & 0xFF);
                    if (longpart_1>0)
                        grafbuffer[num] = longpart_1.floatValue()+longpart.floatValue() / 65535;//longpart_f.floatValue() + floatpart / 1000;
                    if (longpart_1<0)
                        grafbuffer[num] = longpart_1.floatValue()-longpart.floatValue() / 65535;//longpart_f.floatValue() + floatpart / 1000;
                    if (longpart_1==0){
                        if (isminus) grafbuffer[num] = longpart_1.floatValue()-longpart.floatValue() / 65535;
                        else grafbuffer[num] = longpart_1.floatValue()+longpart.floatValue() / 65535;
                    }

                    if (num > 49998) {
                        num = 0;
                        numPlot=0;
                    }
                    num++;

                }
                if (uuid.equals(MY_GATT_CHARACTERISTIC4)) {
                    //if (mMyCharacteristic.getUuid().equals(mMyFlow.getUuid())) {
                        Long longpart = Long.valueOf(dataArr[4] & 0xFF) * 256 * 256 *256 + Long.valueOf(dataArr[5] & 0xFF) * 256 * 256 + Long.valueOf(dataArr[6] & 0xFF) * 256  + Long.valueOf(dataArr[7] & 0xFF);
                        Float floatpart = longpart.floatValue() / 65535 / 65535 * 1000;
                        Integer Intpart = floatpart.intValue();
                        Long longpart_1 = Long.valueOf(dataArr[0] & 0xFF) * 256 * 256 *256 + Long.valueOf(dataArr[1] & 0xFF) * 256 * 256 + Long.valueOf(dataArr[2] & 0xFF) * 256  + Long.valueOf(dataArr[3] & 0xFF);
                        if (longpart_1>=0x80000000L && longpart_1 != 0x80000000L + 0x7FFFFFFFL )longpart_1=longpart_1-0x80000000L - 0x7FFFFFFFL;
                        Boolean isminus=false;
                        if (longpart_1>=0x80000000L && longpart_1 == 0x80000000L + 0x7FFFFFFFL ) {longpart_1=longpart_1-0x80000000L - 0x7FFFFFFFL; isminus=true;}

                        String hexdata = String.format("Input HEX value: 0x%02X%02X%02X%02X%02X%02X%02X%02X", dataArr[0] & 0xFF, dataArr[1] & 0xFF, dataArr[2] & 0xFF, dataArr[3] & 0xFF, dataArr[4] & 0xFF, dataArr[5] & 0xFF, dataArr[6] & 0xFF, dataArr[7] & 0xFF);
                        String btdata = String.format("%d.%03d", /*Long.valueOf((((dataArr[0] & 0xFF) * 256 * 256 * 256) + ((dataArr[1] & 0xFF) * 256 * 256) + ((dataArr[2] & 0xFF) * 256) + (dataArr[3] & 0xFF)))*/longpart_1, /*Long.valueOf((((dataArr[4] & 0xFF) * 256 * 256 *256) + ((dataArr[5] & 0xFF) * 256 * 256)+((dataArr[6] & 0xFF) * 256 ) + (dataArr[7] & 0xFF)))&0xFFFFFFFF */Intpart);
                        //mTextView1 = (TextView) findViewById(R.id.textView1);
                        //mTextView1.setText(hexdata);
                        if (NOWSCREEN==1) {
                            mTextView2 = (TextView) findViewById(R.id.textView7);
                            mTextView2.setText(btdata);
                            mTextView2 = (TextView) findViewById(R.id.textView14);
                            if (longpart_1 < 0 || isminus) mTextView2.setText("<=");
                            else mTextView2.setText("=>");
                        }
                        if (NOWSCREEN==2) {
                            mTextView2 = (TextView) findViewById(R.id.textView101);
                            mTextView2.setText(btdata);
                        }

                        Long now_flow = 0L;
                        now_flow = Math.abs(longpart_1);/*((dataArr[0] & 0xFF) * 256 * 256 * 256) + ((dataArr[1] & 0xFF) * 256 * 256) + ((dataArr[2] & 0xFF) * 256) + (dataArr[3] & 0xFF);*/
                        if (now_flow>0){
                            //flow_in_percent = (ProgressBar) findViewById(R.id.progressBar2);
                            //flow_in_percent.setProgress(now_flow.intValue());
                        }

                        //Long longpart_f = Long.valueOf(dataArr[0] & 0xFF) * 256 * 256 *256 + Long.valueOf(dataArr[1] & 0xFF) * 256 * 256 + Long.valueOf(dataArr[2] & 0xFF) * 256  + Long.valueOf(dataArr[3] & 0xFF);
                        if (longpart_1>0)
                            grafbuffer[num] = longpart_1.floatValue()+longpart.floatValue() / 65535 / 65535;//longpart_f.floatValue() + floatpart / 1000;
                        if (longpart_1<0)
                            grafbuffer[num] = longpart_1.floatValue()-longpart.floatValue() / 65535 / 65535;//longpart_f.floatValue() + floatpart / 1000;
                        if (longpart_1==0){
                            if (isminus) grafbuffer[num] = longpart_1.floatValue()-longpart.floatValue() / 65535 / 65535;
                            else grafbuffer[num] = longpart_1.floatValue()+longpart.floatValue() / 65535 / 65535;
                        }

                        if (num > 49998) {
                            num = 0;
                            numPlot = 0;
                        }
                        num++;
                     /*}else {
                        Long longpart = Long.valueOf(dataArr[4] & 0xFF) * 256 * 256 *256 + Long.valueOf(dataArr[5] & 0xFF) * 256 * 256 + Long.valueOf(dataArr[6] & 0xFF) * 256  + Long.valueOf(dataArr[7] & 0xFF);
                        Float floatpart = longpart.floatValue() / 65535 / 65535 * 1000;
                        Integer Intpart = floatpart.intValue();

                        Long longpart_1 = Long.valueOf(dataArr[0] & 0xFF) * 256 * 256 *256 + Long.valueOf(dataArr[1] & 0xFF) * 256 * 256 + Long.valueOf(dataArr[2] & 0xFF) * 256  + Long.valueOf(dataArr[3] & 0xFF);
                        if (longpart_1>=0x80000000L && longpart_1 != 0x80000000L + 0x7FFFFFFFL )longpart_1=longpart_1-0x80000000L - 0x7FFFFFFFL;
                        Boolean isminus=false;
                        if (longpart_1>=0x80000000L && longpart_1 == 0x80000000L + 0x7FFFFFFFL ) {longpart_1=longpart_1-0x80000000L - 0x7FFFFFFFL; isminus=true;}

                        //String hexdata = String.format("Input HEX value: 0x%02X%02X%02X%02X%02X%02X%02X%02X", dataArr[0] & 0xFF, dataArr[1] & 0xFF, dataArr[2] & 0xFF, dataArr[3] & 0xFF, dataArr[4] & 0xFF, dataArr[5] & 0xFF, dataArr[6] & 0xFF, dataArr[7] & 0xFF);
                        //String btdata = String.format("%d.%03d l/h", Long.valueOf((((dataArr[0] & 0xFF) * 256 * 256 * 256) + ((dataArr[1] & 0xFF) * 256 * 256) + ((dataArr[2] & 0xFF) * 256) + (dataArr[3] & 0xFF))), Intpart);

                        Long now_flow = 0L;
                        now_flow = longpart_1;
                        if (now_flow>0){
                            //flow_in_percent = (ProgressBar) findViewById(R.id.progressBar2);
                            //flow_in_percent.setProgress(now_flow.intValue());

                    }*/
                }
                if ( uuid.equals(MY_GATT_CHARACTERISTIC5)) {

                    Long longpart = Long.valueOf(dataArr[4] & 0xFF) * 256 * 256 *256 + Long.valueOf(dataArr[5] & 0xFF) * 256 * 256 + Long.valueOf(dataArr[6] & 0xFF) * 256  + Long.valueOf(dataArr[7] & 0xFF);
                    Float floatpart = longpart.floatValue() / 65535 / 65535 * 1000;
                    Integer Intpart = floatpart.intValue();

                    Long longpart_1 = Long.valueOf(dataArr[0] & 0xFF) * 256 * 256 *256 + Long.valueOf(dataArr[1] & 0xFF) * 256 * 256 + Long.valueOf(dataArr[2] & 0xFF) * 256  + Long.valueOf(dataArr[3] & 0xFF);
                    if (longpart_1>=0x80000000L && longpart_1 != 0x80000000L + 0x7FFFFFFFL )longpart_1=longpart_1-0x80000000L - 0x7FFFFFFFL;
                    Boolean isminus=false;
                    if (longpart_1>=0x80000000L && longpart_1 == 0x80000000L + 0x7FFFFFFFL ) {longpart_1=longpart_1-0x80000000L - 0x7FFFFFFFL; isminus=true;}

                    String hexdata = String.format("Input HEX value: 0x%02X%02X%02X%02X%02X%02X%02X%02X", dataArr[0] & 0xFF, dataArr[1] & 0xFF, dataArr[2] & 0xFF, dataArr[3] & 0xFF, dataArr[4] & 0xFF, dataArr[5] & 0xFF, dataArr[6] & 0xFF, dataArr[7] & 0xFF);
                    String btdata = String.format("%d.%03d", longpart_1/*Long.valueOf((((dataArr[0] & 0xFF) * 256 * 256 * 256) + ((dataArr[1] & 0xFF) * 256 * 256)+((dataArr[2] & 0xFF) * 256) + (dataArr[3] & 0xFF)))*/, /*Long.valueOf((((dataArr[4] & 0xFF) * 256 * 256 *256) + ((dataArr[5] & 0xFF) * 256 * 256)+((dataArr[6] & 0xFF) * 256 ) + (dataArr[7] & 0xFF)))&0xFFFFFFFF */Intpart);
                    //mTextView1 = (TextView) findViewById(R.id.textView1);
                    //mTextView1.setText(hexdata);
                    if (NOWSCREEN==1) {
                        mTextView2 = (TextView) findViewById(R.id.textView8);
                        mTextView2.setText(btdata);
                    }
                    if (NOWSCREEN==2) {
                        mTextView2 = (TextView) findViewById(R.id.textView30);
                        mTextView2.setText(btdata);
                    }
                    /*
                    //Long longpart_f = Long.valueOf(dataArr[0] & 0xFF) * 256 * 256 *256 + Long.valueOf(dataArr[1] & 0xFF) * 256 * 256 + Long.valueOf(dataArr[2] & 0xFF) * 256  + Long.valueOf(dataArr[3] & 0xFF);
                    if (longpart_1>0)
                        grafbuffer[num] = longpart_1.floatValue()+longpart.floatValue() / 65535 / 65535;//longpart_f.floatValue() + floatpart / 1000;
                    if (longpart_1<0)
                        grafbuffer[num] = longpart_1.floatValue()-longpart.floatValue() / 65535 / 65535;//longpart_f.floatValue() + floatpart / 1000;
                    if (longpart_1==0){
                        if (isminus) grafbuffer[num] = longpart_1.floatValue()-longpart.floatValue() / 65535 / 65535;
                        else grafbuffer[num] = longpart_1.floatValue()+longpart.floatValue() / 65535 / 65535;
                    }
                    if (num > 49998) {
                        num = 0;
                        numPlot = 0;
                    }
                    num++;*/
                }

                if (uuid.equals(MY_GATT_CHARACTERISTIC6)) {
                    String hexdata = String.format("Input HEX value: 0x%02X%02X%02X%02X%02X%02X%02X%02X", dataArr[0] & 0xFF, dataArr[1] & 0xFF, dataArr[2] & 0xFF, dataArr[3] & 0xFF, dataArr[4] & 0xFF, dataArr[5] & 0xFF, dataArr[6] & 0xFF, dataArr[7] & 0xFF);
                    String hexintdata = String.format("%02X%02X%02X%02X", dataArr[4] & 0xFF, dataArr[5] & 0xFF, dataArr[6] & 0xFF, dataArr[7] & 0xFF);
                    if (!isTechnical){
                        //flow_in_percent = (ProgressBar)findViewById(R.id.progressBar2);
                        //flow_in_percent.setMax((dataArr[4] & 0xFF) * 256 * 256 * 256 + (dataArr[5] & 0xFF) * 256 * 256 + (dataArr[6] & 0xFF) * 256 + (dataArr[7] & 0xFF));
                        //delay100ms();
                        //delay100ms();
                        ReadTecnicalNumber();
                    } else {
                        hextechnical = String.format("%02X%02X%02X%02X", dataArr[4] & 0xFF, dataArr[5] & 0xFF, dataArr[6] & 0xFF, dataArr[7] & 0xFF);
                        if (NOWSCREEN==1) {
                            mTextView1 = (TextView) findViewById(R.id.textView9);
                            mTextView1.setText(hextechnical);
                        }
                        //start queries
                        //if (buttonShowPar.isClickable())
                        //    mBluetoothLeService.setCharacteristicNotification(mMyCharacteristic, true);
                        init_ok=true;
                    }

                }
/*
                if (grapfUpdateType == 1) {
                    if (numPlot > 49998) {
                        series = new LineGraphSeries<DataPoint>();
                        graph.removeAllSeries();
                        graph.addSeries(series);
                        numPlot = 0;
                        num = 0;
                    }
                    if (num > 1) {
                        series.appendData(new DataPoint(num, grafbuffer[num - 1]), true, 150);
                        bufferPlot[numPlot] = num;
                        numPlot++;
                        if (numPlot > 150) graph.getViewport().setMinX(bufferPlot[numPlot - 150]);
                        else graph.getViewport().setMinX(0);
                        graph.getViewport().setMaxX(num);
                    }
                }*/
            }
        }
    };

    private void clearUI() {

    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isWriteNow) {
            mBluetoothLeService.setCharacteristicNotification(mMyCharacteristic, false);
            unregisterReceiver(mGattUpdateReceiver);
            mHandler.removeCallbacks(mTimer2);
            mHandler.removeCallbacks(mTimer1);
            mHandler.removeCallbacks(mTimer3);

            unbindService(mServiceConnection);
            mBluetoothLeService = null;
        }
    }

    public void ReadMaxFlow(){

        byte[] mass = new byte[8];
        mass[0] = 1;//address_hex_inbytes[0];
        mass[1] = 5;//address_hex_inbytes[1];
        mass[2] = 2;//address_hex_inbytes[2];
        mass[3] = 0;//address_hex_inbytes[3];
        mass[4] = 0;//num_intpart_hex_inbytes[0];
        mass[5] = 0;//num_intpart_hex_inbytes[1];
        mass[6] = 0;//num_floatpart_hex_inbytes[0];
        mass[7] = 0;//num_floatpart_hex_inbytes[1];

        mMyService = mBluetoothLeService.getGattService(UUID.fromString(MY_GATT_SERVICE));
        mMyCommands = mMyService.getCharacteristic(UUID.fromString(MY_GATT_CHARACTERISTIC6));
        mMyCommands.setValue(mass);
        mBluetoothLeService.writeCharacteristic(mMyCommands);
        isWriteNow = true;
        //delay100ms();
        //mBluetoothLeService.readCharacteristic(mMyCommands);
    }

    public void ReadTecnicalNumber(){

        byte[] mass = new byte[8];
        mass[0] = 1;//address_hex_inbytes[0];
        mass[1] = 6;//address_hex_inbytes[1];
        mass[2] = 2;//address_hex_inbytes[2];
        mass[3] = 0;//address_hex_inbytes[3];
        mass[4] = 0;//num_intpart_hex_inbytes[0];
        mass[5] = 0;//num_intpart_hex_inbytes[1];
        mass[6] = 0;//num_floatpart_hex_inbytes[0];
        mass[7] = 0;//num_floatpart_hex_inbytes[1];

        mMyService = mBluetoothLeService.getGattService(UUID.fromString(MY_GATT_SERVICE));
        mMyCommands = mMyService.getCharacteristic(UUID.fromString(MY_GATT_CHARACTERISTIC6));
        mMyCommands.setValue(mass);
        mBluetoothLeService.writeCharacteristic(mMyCommands);
        isWriteNow = true;
        isTechnical = true;
        //delay100ms();
        //mBluetoothLeService.readCharacteristic(mMyCommands);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!isWriteNow) {

            isTechnical=false;
            init_ok=false;
            //mTextView2.setText("Initializing...");

            final Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
            bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

            registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
            if (mBluetoothLeService != null) {
                final boolean result = mBluetoothLeService.connect(mDeviceAddress);
                Log.d(TAG, "Connect request result=" + result);
            }
            /*
            num = 0;
            numPlot = 0;
            series = new LineGraphSeries<DataPoint>();
            graph.removeAllSeries();
            graph.addSeries(series);
            graph.getViewport().setMinX(0);
            */
            mTimer2 = new Runnable() {
                @Override
                public void run() {

                        //if (grapfUpdateType == 0) {
                            if (init_ok)
                                mBluetoothLeService.readCharacteristic(mMyCharacteristic);
                        //}
                    if (NOWSCREEN==2) {
                        if (grapfUpdateType == 0 || grapfUpdateType == 2) {
                            if (numPlot > 49998) {
                                series = new LineGraphSeries<DataPoint>();
                                graph.removeAllSeries();
                                graph.addSeries(series);
                                numPlot = 0;
                                num = 0;
                            }
                            if (num > 1) {
                                series.appendData(new DataPoint(num, grafbuffer[num - 1]), true, 150);
                                bufferPlot[numPlot] = num;
                                numPlot++;
                                if (numPlot > 150)
                                    graph.getViewport().setMinX(bufferPlot[numPlot - 150]);
                                else graph.getViewport().setMinX(0);
                                graph.getViewport().setMaxX(num);
                            }
                        }
                    }
                    /*
                    delay100ms();
                    mBluetoothLeService.readCharacteristic(mMyErrors);
                    if (!mMyCharacteristic.getUuid().equals(mMyFlow.getUuid())){
                        delay100ms();
                        mBluetoothLeService.readCharacteristic(mMyFlow);}*/
                    mHandler.postDelayed(this, 600);
                }
            };
            mHandler.postDelayed(mTimer2, 600);

            mTimer1 = new Runnable() {
                @Override
                public void run() {
                    if (init_ok)
                        mBluetoothLeService.readCharacteristic(mMyErrors);
                    if (NOWSCREEN==3) {
                        updateERRORS();
                    }
                    mHandler.postDelayed(this, 600);
                }
            };
            mHandler.postDelayed(mTimer1, 400);

            mTimer3 = new Runnable() {
                @Override
                public void run() {
                    if (init_ok)
                        //if (!mMyCharacteristic.getUuid().equals(mMyVolume.getUuid())){
                            mBluetoothLeService.readCharacteristic(mMyVolume);//}
                    mHandler.postDelayed(this, 600);
                }
            };
            mHandler.postDelayed(mTimer3, 200);

        } else isWriteNow=false;
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_CHARACTERISTIC_WRITE);
        return intentFilter;
    }

    private static String tryString(final String string, final String fallback) {
        if (string == null) {
            return fallback;
        } else {
            return string;
        }
    }

    private  static void delay100ms()
    {
       try {
            Thread.sleep(100, 0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
