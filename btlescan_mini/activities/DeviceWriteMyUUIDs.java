package uk.co.alt236.btlescan_mini.activities;

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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import uk.co.alt236.bluetoothlelib.device.BluetoothLeDevice;
import uk.co.alt236.bluetoothlelib.resolvers.GattAttributeResolver;
import uk.co.alt236.bluetoothlelib.util.ByteUtils;
import uk.co.alt236.btlescan_mini.R;
import uk.co.alt236.btlescan_mini.services.BluetoothLeService;

/**
 * Created by Станция on 23.06.2016.
 */
public class DeviceWriteMyUUIDs extends AppCompatActivity {

    public static String MY_GATT_SERVICE = "398506e1-98da-11e5-a837-0800200c9a66";
    public static String MY_GATT_CHARACTERISTIC1 = "398506e2-98da-11e5-a837-0800200c9a66"; //ERRORS
    public static String MY_GATT_CHARACTERISTIC2  = "398506e3-98da-11e5-a837-0800200c9a66";//TOF_DIFF , ns
    public static String MY_GATT_CHARACTERISTIC3  = "398506e4-98da-11e5-a837-0800200c9a66";//FLOW_SPEED , mm/s
    public static String MY_GATT_CHARACTERISTIC4  = "398506e5-98da-11e5-a837-0800200c9a66";//FLOW , l/h
    public static String MY_GATT_CHARACTERISTIC5  = "398506e6-98da-11e5-a837-0800200c9a66";//VOLUME m3
    public static String MY_GATT_CHARACTERISTIC6  = "398506e7-98da-11e5-a837-0800200c9a66";////COMMANDS
    private final Handler mHandler = new Handler();
    private Button buttonConf;

    public static final String EXTRA_DEVICE = "extra_device";
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    protected TextView mTextView1;
    protected TextView mTextView2;
    protected EditText mEditText1;
    protected EditText mEditText2;
    protected EditText mEditText3;
    private BluetoothGattCharacteristic mMyCharacteristic;
    private BluetoothGattService mMyService;
    private BluetoothLeService mBluetoothLeService;
    private Runnable mTimer;

    private boolean isWriteNow=false;
    private String mDeviceAddress;
    private String mDeviceName;
    private boolean mConnected = false;
    private String mExportString;
    private Integer autochange=0;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_data);

        final Intent intent = getIntent();
        final BluetoothLeDevice device = intent.getParcelableExtra(EXTRA_DEVICE);
        mDeviceName = device.getName();
        mDeviceAddress = device.getAddress();

        overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);

        buttonConf=(Button)findViewById(R.id.buttonConf);
        buttonConf.setBackgroundColor(getResources().getColor(R.color.button_down));

        DisplayMetrics display = getResources().getDisplayMetrics();

        mEditText1 = (EditText)findViewById(R.id.editText);
        mEditText2 = (EditText)findViewById(R.id.editText2);
        mEditText3 = (EditText)findViewById(R.id.editText3);

        TextWatcher onEditText2Changed = new TextWatcher(){
	        public void afterTextChanged(Editable s) {
		    //your business logic after text is changed
                if (!mEditText2.getText().toString().equals("") && autochange==0) {
                    //your business logic while text has changed
                    //mEditText1 = (EditText)findViewById(R.id.editText);
                    //mEditText2 = (EditText) findViewById(R.id.editText2);
                    float number = Float.valueOf(mEditText2.getText().toString());

                    Double number_1=0.000;
                    if (number>=0) number_1 = Math.floor(number);
                    else number_1 = Math.ceil(number);

                    Integer number_1_1 = Math.abs(number_1.intValue());
                    Integer number_2 = Math.abs(Math.round((number - number_1.intValue()) * 65536));

                    //String address_hex = String.format("%s", mEditText1.getText().toString());
                    String num_intpart_hex = "";
                    if (number>=0)num_intpart_hex = String.format("%04X", number_1_1);
                    else num_intpart_hex = String.format("%04X", 65535-number_1_1);
                    String num_floatpart_hex = String.format("%04X", number_2);

                    //byte[] address_hex_inbytes = new byte[4];
                    //address_hex_inbytes = hexStringToSizedByteArray(address_hex,4);
                    byte[] num_intpart_hex_inbytes = new byte[2];
                    num_intpart_hex_inbytes = hexStringToSizedByteArray(num_intpart_hex, 2);
                    byte[] num_floatpart_hex_inbytes = new byte[2];
                    num_floatpart_hex_inbytes = hexStringToSizedByteArray(num_floatpart_hex, 2);

                    String hexdata = String.format("%02X%02X%02X%02X",
                            num_intpart_hex_inbytes[0], num_intpart_hex_inbytes[1],
                            num_floatpart_hex_inbytes[0], num_floatpart_hex_inbytes[1]);

                    autochange=1;
                    mEditText3 = (EditText) findViewById(R.id.editText3);
                    mEditText3.setText(hexdata);
                }
                else
                if (autochange==1)autochange=0;
	        }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){
                //your business logic before text is changed
            }

            public void onTextChanged(CharSequence s, int start, int before, int count){

            }
        };
        mEditText2.addTextChangedListener(onEditText2Changed);

        TextWatcher onEditText3Changed = new TextWatcher(){
            public void afterTextChanged(Editable s) {
                //your business logic after text is changed
                //your business logic while text has changed
                //mEditText1 = (EditText)findViewById(R.id.editText);
                if (!mEditText3.getText().toString().equals("") && mEditText3.getText().toString().length()==8 && autochange==0) {
                    //mEditText3 = (EditText) findViewById(R.id.editText3);
                    byte[] hex_input = new byte[4];
                    byte minus=' ';
                    hex_input = hexStringToSizedByteArray(mEditText3.getText().toString(), 4);
                    Integer hex_input_intpart = (hex_input[0] & 0xFF) * 256 + (hex_input[1] & 0xFF);
                    if (hex_input_intpart>(65536/2) && hex_input_intpart!=65535)hex_input_intpart=hex_input_intpart-65535;
                    if (hex_input_intpart>(65536/2) && hex_input_intpart==65535){hex_input_intpart=hex_input_intpart-65535; minus='-';}
                    Integer hex_input_floatpart = (hex_input[2]  & 0xFF) * 256 + (hex_input[3] & 0xFF);
                    Double number = (hex_input_floatpart.doubleValue() / 65536) * 1000;

                    Double number_1 = Math.floor(number);
                    Integer number_1_1 = number_1.intValue();

                    String float_string = "";
                    if (minus=='-')float_string =  String.format("%c%d.%03d", minus, hex_input_intpart, number_1_1);
                    else float_string = String.format("%d.%03d",hex_input_intpart, number_1_1);

                    autochange=1;
                    mEditText2 = (EditText) findViewById(R.id.editText2);
                    mEditText2.setText(float_string);
                }
                else
                if (autochange==1)autochange=0;
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){
                //your business logic before text is changed
            }

            public void onTextChanged(CharSequence s, int start, int before, int count){

            }
        };
        mEditText3.addTextChangedListener(onEditText3Changed);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isWriteNow){
            unregisterReceiver(mGattUpdateReceiver);
            unbindService(mServiceConnection);
            mBluetoothLeService = null;
            mHandler.removeCallbacks(mTimer);
        }
    }
    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        // super.onBackPressed();
        openQuitDialog();
    }


    private void openQuitDialog() {

        AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                DeviceWriteMyUUIDs.this);
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
    @Override
    protected void onResume() {
        super.onResume();
        if (!isWriteNow) {
            final Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
            bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
            registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
            if (mBluetoothLeService != null) {
                final boolean result = mBluetoothLeService.connect(mDeviceAddress);
                Log.d(TAG, "Connect request result=" + result);
            }
            isWriteNow=false;
        }
        /*
        mTimer = new Runnable() {
            @Override
            public void run() {
                String input;
                String output;
                input = mEditText1.getText().toString();
                output = input.replaceAll("[^a-fA-F0-9]+", "");
                if (!input.equals(output)) mEditText1.setTextColor(getResources().getColor(R.color.red));
                else mEditText1.setTextColor(getResources().getColor(R.color.black));
                input = mEditText2.getText().toString();
                output = input.replaceAll("[^0-9.-]+", "");
                mEditText2.setText(output);
                input = mEditText3.getText().toString();
                output = input.replaceAll("[^a-fA-F0-9]+", "");
                if (!input.equals(output)) mEditText3.setTextColor(getResources().getColor(R.color.red));
                else mEditText3.setTextColor(getResources().getColor(R.color.black));
                mHandler.postDelayed(this, 500);


            }
        };
        mHandler.postDelayed(mTimer, 500);*/
    }


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
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
            } else if (BluetoothLeService.ACTION_CHARACTERISTIC_WRITE.equals(action)) {
                isWriteNow = false;
                //litle delay 300ms
                try {
                    Thread.sleep(1000, 0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //auto read after write cycle
                mBluetoothLeService.readCharacteristic(mMyService.getCharacteristic(UUID.fromString(MY_GATT_CHARACTERISTIC6)));
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                final String noData = getString(R.string.no_data);
                final String uuid = intent.getStringExtra(BluetoothLeService.EXTRA_UUID_CHAR);
                final byte[] dataArr = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA_RAW);

                if (uuid.equals(MY_GATT_CHARACTERISTIC6)) {
                    String hexdata = String.format("Input HEX value: 0x%02X%02X%02X%02X%02X%02X%02X%02X", dataArr[0] & 0xFF, dataArr[1] & 0xFF, dataArr[2] & 0xFF, dataArr[3] & 0xFF, dataArr[4] & 0xFF, dataArr[5] & 0xFF, dataArr[6] & 0xFF, dataArr[7] & 0xFF);
                    String hexintdata = String.format("%02X%02X%02X%02X", dataArr[4] & 0xFF, dataArr[5] & 0xFF, dataArr[6] & 0xFF, dataArr[7] & 0xFF);
                    mTextView1 = (TextView)findViewById(R.id.textView1);
                    mTextView1.setText(hexdata);
                    mEditText3 = (EditText) findViewById(R.id.editText3);
                    mEditText3.setText(hexintdata);
                }
            }
        }
    };

    private void displayImpulseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.show_outputtype_pi)
                .setItems(R.array.show_outputtype_pi, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        switch (which){
                            case 0:
                                //Select output
                                mEditText1.setText("01010200");
                                mEditText3.setText("00000000");
                                break;
                            case 1:
                                //Write Imp/l
                                mEditText1.setText("01010201");
                                mEditText3.setText("00000000");
                                break;
                            case 2:
                                //Read Imp/l
                                mEditText1.setText("01010202");
                                mEditText3.setText("00000000");
                                break;

                        }
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void displayFreequencyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.show_outputtype_pf)
                .setItems(R.array.show_outputtype_pf, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        switch (which){
                            case 0:
                                //Select output
                                mEditText1.setText("01010100");
                                mEditText3.setText("00000000");
                                break;
                            case 1:
                                //Write MAX frequency
                                mEditText1.setText("01010101");
                                mEditText3.setText("00000000");
                                break;
                            case 2:
                                //Read MAX frequency
                                mEditText1.setText("01010102");
                                mEditText3.setText("00000000");
                                break;
                            case 3:
                                //Write OFFSET
                                mEditText1.setText("01010103");
                                mEditText3.setText("00000000");
                                break;
                            case 4:
                                //Read OFFSET
                                mEditText1.setText("01010104");
                                mEditText3.setText("00000000");
                                break;
                        }
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void displayOutputtypeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.show_outputtype)
                .setItems(R.array.show_outputtype, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        switch (which){
                            case 0:
                                //Freequency output
                                displayFreequencyDialog();
                                break;
                            case 1:
                                //Impule output
                                displayImpulseDialog();
                                break;
                        }
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void displayGKDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.show_gk)
                .setItems(R.array.show_gk, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        switch (which){
                            case 0:
                                //Write GK
                                mEditText1.setText("01020100");
                                mEditText3.setText("00000000");
                                break;
                            case 1:
                                //Read GK
                                mEditText1.setText("01020200");
                                mEditText3.setText("00000000");
                                break;
                        }
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void displayZeroDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.Set_zero)
                .setItems(R.array.Set_zero, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        switch (which){
                            case 0:
                                //Set zero point
                                mEditText1.setText("01030100");
                                mEditText3.setText("00000000");
                                break;
                            case 1:
                                //Reset to 0
                                mEditText1.setText("01030200");
                                mEditText3.setText("00000000");
                                break;
                        }
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void displayQminDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.Set_qmin)
                .setItems(R.array.Set_qmin, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        switch (which){
                            case 0:
                                //Write Qmin
                                mEditText1.setText("01040100");
                                mEditText3.setText("00000000");
                                break;
                            case 1:
                                //Read Qmin
                                mEditText1.setText("01040200");
                                mEditText3.setText("00000000");
                                break;
                        }
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void displayQmaxDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.Set_qmax)
                .setItems(R.array.Set_qmax, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        switch (which){
                            case 0:
                                //Write Qmax
                                mEditText1.setText("01050100");
                                mEditText3.setText("00000000");
                                break;
                            case 1:
                                //Read Qmax
                                mEditText1.setText("01050200");
                                mEditText3.setText("00000000");
                                break;
                        }
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void displayParDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.show_wparameters)
                .setItems(R.array.show_wparameters, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        switch (which){
                            case 0:
                                //Output_type
                                displayOutputtypeDialog();
                                break;
                            case 1:
                                //GK
                                displayGKDialog();
                                break;
                            case 2:
                                //zero
                                displayZeroDialog();
                                break;
                            case 3:
                                //Qmin
                                displayQminDialog();
                                break;
                            case 4:
                                //Qmax
                                displayQmaxDialog();
                                break;
                            case 5:
                                //Tech_number
                                mEditText1.setText("01060200");
                                mEditText3.setText("00000000");
                                break;
                            case 6:
                                //Preset_settings
                                mEditText1.setText("01FD0000");
                                mEditText3.setText("00000000");
                                break;
                            case 7:
                                //Flash_operations_read
                                mEditText1.setText("01FE0000");
                                mEditText3.setText("00000000");
                                break;
                            case 8:
                                //Flash_operations_write
                                mEditText1.setText("01FF0000");
                                mEditText3.setText("00000000");
                                break;
                            case 9:
                                //Authorization
                                mEditText1.setText("FFFF0000");
                                mEditText3.setText("00000000");
                                break;

                        }
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void onSCANclick(View view)
    {
        final Intent intent = getIntent();
        final BluetoothLeDevice device = intent.getParcelableExtra(EXTRA_DEVICE);
        final Intent intent2 = new Intent(this, MainActivity.class);
        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent2);
    }

    public void onParClick(View view)
    {
        displayParDialog();
    }


    public void onSHOWclick(View view)
    {
        final Intent intent = getIntent();
        final BluetoothLeDevice device = intent.getParcelableExtra(EXTRA_DEVICE);
        final Intent intent2 = new Intent(this, DeviceReadMyUUIDs.class);
        intent2.putExtra(DeviceReadMyUUIDs.EXTRA_DEVICE, device);
        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent2);
    }

    public void onCONFclick(View view)
    {

    }

    public void onINFOclick(View view)
    {
        final Intent intent = getIntent();
        final BluetoothLeDevice device = intent.getParcelableExtra(EXTRA_DEVICE);
        final Intent intent2 = new Intent(this, DeviceControlActivity.class);
        intent2.putExtra(DeviceControlActivity.EXTRA_DEVICE, device);
        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent2);
    }

    public void onConfRead(View veiw){
        mEditText1 = (EditText)findViewById(R.id.editText);
        mEditText2 = (EditText)findViewById(R.id.editText2);
        mEditText3 = (EditText)findViewById(R.id.editText3);
        String input;
        String output;
        boolean error_edit1, error_edit2, error_edit3;
        boolean error_in=false;

        input = mEditText1.getText().toString();
        output = input.replaceAll("[^a-fA-F0-9]+", "");
        if (!input.equals(output) || input.length()!=8 ) {
            mEditText1.setTextColor(getResources().getColor(R.color.red));
            error_edit1=true;
        }
        else {
            mEditText1.setTextColor(getResources().getColor(R.color.black));
            error_edit1=false;
        }
        error_in = error_edit1 /*| error_edit2 | error_edit3*/;

        if (!error_in) {
            mBluetoothLeService.readCharacteristic(mMyService.getCharacteristic(UUID.fromString(MY_GATT_CHARACTERISTIC6)));
        } else
            Toast.makeText(this, "ERROR: Check  address and parameters", Toast.LENGTH_SHORT).show();
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
                    .digit(s.charAt(i + 1), 16));
        }

        return data;
    }

    public static byte[] hexStringToSizedByteArray(String s, Integer size) {
        int len = s.length();
        byte[] data = new byte[size];
        if ( len % 2 == 0 && len/2 == size) {
            for (int i = 0; i < size*2; i += 2) {
                data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
                        .digit(s.charAt(i + 1), 16));
            }
        }
        return data;
    }

    public void onConfWrite(View veiw){
        mEditText1 = (EditText)findViewById(R.id.editText);
        mEditText2 = (EditText)findViewById(R.id.editText2);
        mEditText3 = (EditText)findViewById(R.id.editText3);
        String input;
        String output;
        boolean error_edit1, error_edit2, error_edit3;
        boolean error_in=false;

        input = mEditText1.getText().toString();
        output = input.replaceAll("[^a-fA-F0-9]+", "");
        if (!input.equals(output) || input.length()!=8 ) {
            mEditText1.setTextColor(getResources().getColor(R.color.red));
            error_edit1=true;
        }
        else {
            mEditText1.setTextColor(getResources().getColor(R.color.black));
            error_edit1=false;
        }

        input = mEditText2.getText().toString();
        output = input.replaceAll("[^0-9.-]+", "");
        if (!input.equals(output)) {
            mEditText2.setTextColor(getResources().getColor(R.color.red));
            error_edit2=true;
        }
        else {
            mEditText2.setTextColor(getResources().getColor(R.color.black));
            error_edit2=false;
        }

        input = mEditText3.getText().toString();
        output = input.replaceAll("[^a-fA-F0-9]+", "");
        if (!input.equals(output) || input.length()!=8) {
            mEditText3.setTextColor(getResources().getColor(R.color.red));
            error_edit3=true;
        }
        else {
            mEditText3.setTextColor(getResources().getColor(R.color.black));
            error_edit3=false;
        }

        error_in = error_edit1 | error_edit2 | error_edit3;

        if (!error_in) {

            /*float number = Float.valueOf(mEditText2.getText().toString());

            Double number_1 = Math.floor(number);
            Integer number_1_1 = number_1.intValue();
            Integer number_2 = Math.round((number - number_1.intValue()) * 65536);

            String address_hex = String.format("%s", mEditText1.getText().toString());
            String num_intpart_hex = String.format("%04X", number_1_1);
            String num_floatpart_hex = String.format("%04X", number_2);


            byte[] address_hex_inbytes = new byte[4];
            address_hex_inbytes = hexStringToSizedByteArray(address_hex, 4);
            byte[] num_intpart_hex_inbytes = new byte[2];
            num_intpart_hex_inbytes = hexStringToSizedByteArray(num_intpart_hex, 2);
            byte[] num_floatpart_hex_inbytes = new byte[2];
            num_floatpart_hex_inbytes = hexStringToSizedByteArray(num_floatpart_hex, 2);

            */
            byte[] address_hex_inbytes = new byte[4];
            address_hex_inbytes = hexStringToSizedByteArray(mEditText1.getText().toString(), 4);
            byte[] num_hex_inbytes = new byte[4];
            num_hex_inbytes = hexStringToSizedByteArray(mEditText3.getText().toString(), 4);


            String hexdata = String.format("Output HEX value: 0x%02X%02X%02X%02X%02X%02X%02X%02X",
                    address_hex_inbytes[0], address_hex_inbytes[1], address_hex_inbytes[2],
                    address_hex_inbytes[3], num_hex_inbytes[0], num_hex_inbytes[1],
                    num_hex_inbytes[2], num_hex_inbytes[3]);

            mTextView2 = (TextView) findViewById(R.id.textView2);
            mTextView2.setText(hexdata);
            byte[] mass = new byte[8];
            mass[0] = address_hex_inbytes[0];
            mass[1] = address_hex_inbytes[1];
            mass[2] = address_hex_inbytes[2];
            mass[3] = address_hex_inbytes[3];
            mass[4] = num_hex_inbytes[0];
            mass[5] = num_hex_inbytes[1];
            mass[6] = num_hex_inbytes[2];
            mass[7] = num_hex_inbytes[3];

            mMyService = mBluetoothLeService.getGattService(UUID.fromString(MY_GATT_SERVICE));
            mMyCharacteristic = mMyService.getCharacteristic(UUID.fromString(MY_GATT_CHARACTERISTIC6));
            mMyCharacteristic.setValue(mass);
            mBluetoothLeService.writeCharacteristic(mMyCharacteristic);
            isWriteNow = true;

        }
        else
            Toast.makeText(this, "ERROR: Check  address and parameters", Toast.LENGTH_SHORT).show();
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
}
