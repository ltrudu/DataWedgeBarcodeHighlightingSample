package com.zebra.datawedgebarcodehighlightingsample;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "BarcodeHighlight";
    TextView txtStatus = null;
    private String statusString = "";
    private ScrollView sv_status = null;

    EditText et_red     = null;
    EditText et_green   = null;
    EditText et_blue    = null;

    EditText et_contains = null;
    EditText et_color = null;
    Spinner spSymbology = null;

    String sSelectedSymbology = null;

    /*
        Handler and runnable to scroll down views
     */
    private Handler mScrollDownHandler = null;
    private Runnable mScrollDownRunnable = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtStatus = findViewById(R.id.txtStatus);
        sv_status = findViewById(R.id.sv_status);
        et_red = findViewById(R.id.et_red);
        et_green = findViewById(R.id.et_green);
        et_blue = findViewById(R.id.et_blue);
        et_contains = findViewById(R.id.et_reportcontains);
        et_color = findViewById(R.id.et_reportcolor);
        spSymbology = findViewById(R.id.sp_symbology);
        spSymbology.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
              @Override
              public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                  sSelectedSymbology = adapterView.getItemAtPosition(i).toString();
              }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                sSelectedSymbology = "decoder_qrcode";
            }
        });
        sSelectedSymbology = "decoder_qrcode";
        spSymbology.setSelection(17);
        fillSpinner();
        registerReceivers();
    }



    @Override
    protected void onResume() {
        super.onResume();
        mScrollDownHandler = new Handler(Looper.getMainLooper());
        queryProfileList();
    }

    @Override
    protected void onPause() {
        if(mScrollDownRunnable != null)
        {
            mScrollDownHandler.removeCallbacks(mScrollDownRunnable);
            mScrollDownRunnable = null;
            mScrollDownHandler = null;
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterReceivers();
    }

    private void fillSpinner() {
        ArrayList<String> triggerActionTypeList = new ArrayList<String>(){{
            //add(Constants.SHARED_PREFERENCES_UNSELECTED);
            //add("decoder_australian_postal");
            //add("decoder_aztec");
            //add("decoder_canadian_postal");
            //add("decoder_chinese_2of5");
            add("decoder_codabar");
            add("decoder_code11");
            add("decoder_code128");
            add("decoder_code39");
            add("decoder_code93");
            //add("decoder_composite_ab");
            //add("decoder_composite_c");
            //add("decoder_d2of5");
            add("decoder_datamatrix");
            //add("decoder_dutch_postal");
            add("decoder_ean13");
            add("decoder_ean8");
            add("decoder_gs1_databar");
            add("decoder_gs1_qrcode");
            //add("decoder_hanxin");
            add("decoder_i2of5");
            //add("decoder_japanese_postal");
            //add("decoder_korean_3of5");
            //add("decoder_mailmark");
            add("decoder_matrix_2of5");
            add("decoder_maxicode");
            add("decoder_micropdf");
            add("decoder_microqr");
            //add("decoder_msi");
            add("decoder_pdf417");
            add("decoder_qrcode");
            //add("decoder_signature");
            //add("decoder_tlc39");
            //add("decoder_trioptic39");
            //add("decoder_uk_postal");
            add("decoder_upca");
            add("decoder_upce0");
            add("decoder_upce1");
            //add("decoder_us4state");
            //add("decoder_usplanet");
            //add("decoder_uspostnet");
            //add("decoder_webcode");

        }};
        ArrayAdapter<String> actionTypeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, triggerActionTypeList);
        actionTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSymbology.setAdapter(actionTypeAdapter);
    }

    private void queryProfileList(){
        Intent i = new Intent();
        i.setAction(IntentKeys.DATAWEDGE_API_ACTION);
        i.setPackage(IntentKeys.DATAWEDGE_PACKAGE);
        i.putExtra(IntentKeys.EXTRA_GET_PROFILES_LIST, "");
        sendBroadcast(i);
    }

    private void deleteProfile() {
        Intent intent = new Intent();
        intent.setAction(IntentKeys.DATAWEDGE_API_ACTION);
        intent.setPackage(IntentKeys.DATAWEDGE_PACKAGE);
        intent.putExtra(IntentKeys.EXTRA_DELETE_PROFILE,IntentKeys.PROFILE_NAME);
        this.sendBroadcast(intent);
    }

    private void registerReceivers(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(IntentKeys.RESULT_ACTION);
        intentFilter.addAction(IntentKeys.NOTIFICATION_ACTION);
        intentFilter.addAction(IntentKeys.INTENT_OUTPUT_ACTION);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(broadcastReceiver, intentFilter);
        registerUnregisterForNotifications(true, "WORKFLOW_STATUS");
        registerUnregisterForNotifications(true, "SCANNER_STATUS");
    }

    private void unRegisterReceivers(){
        registerUnregisterForNotifications(false, "WORKFLOW_STATUS");
        registerUnregisterForNotifications(false, "SCANNER_STATUS");
        unregisterReceiver(broadcastReceiver);
    }

    void registerUnregisterForNotifications(boolean register, String type) {
        Bundle b = new Bundle();
        b.putString("com.symbol.datawedge.api.APPLICATION_NAME", getPackageName());
        b.putString("com.symbol.datawedge.api.NOTIFICATION_TYPE", type);
        Intent i = new Intent();
        i.putExtra("APPLICATION_PACKAGE", getPackageName());
        i.setAction("com.symbol.datawedge.api.ACTION");
        i.setPackage("com.symbol.datawedge");
        if (register)
            i.putExtra("com.symbol.datawedge.api.REGISTER_FOR_NOTIFICATION", b);
        else
            i.putExtra("com.symbol.datawedge.api.UNREGISTER_FOR_NOTIFICATION", b);
        this.sendBroadcast(i);
    }

    public void onClickClearStatusLog(View view)
    {
        statusString = "";
        setStatus("");
    }

    public void onBarcodeHighlighting(View view)
    {
        switchToBarcodeHighlighting(true);
    }

     public void onBarcodeHighlightingCamera(View view)
    {
        switchToBarcodeHighlighting(false);
    }

    public void onRegularScan(View view)
    {
        switchToRegularScan();
    }

    public void onBarcodeReporting(View view)
    {
        switchToBarcodeReporting(true);
    }

    public void onBarcodeReportingCamera(View view) { switchToBarcodeReporting(false); }

    public void onClickScan(View view) {
        Intent i = new Intent();
        i.setPackage(IntentKeys.DATAWEDGE_PACKAGE);
        i.setAction(IntentKeys.DATAWEDGE_API_ACTION);
        i.putExtra(IntentKeys.EXTRA_SOFT_SCAN_TRIGGER, "TOGGLE_SCANNING");
        this.sendBroadcast(i);
    }

    private void createProfile(){
        Bundle bMain = new Bundle();

        Bundle barcodeParams = new Bundle();
        ArrayList<Bundle> bundlePluginConfig = new ArrayList<>();
        barcodeParams.putString("scanner_selection", "auto"); //Make scanner selection as auto
        barcodeParams.putString("scanner_input_enabled","true");

        /*###Configuration for Intent Output[Start]###*/
        Bundle intentConfig = new Bundle();
        Bundle intentParams = new Bundle();
        intentConfig.putString("PLUGIN_NAME", "INTENT"); //Plugin name as intent
        intentConfig.putString("RESET_CONFIG", "true"); //Reset existing configurations of intent output plugin
        intentParams.putString("intent_output_enabled", "true"); //Enable intent output plugin
        intentParams.putString("intent_action", IntentKeys.INTENT_OUTPUT_ACTION); //Set the intent action
        intentParams.putString("intent_category", IntentKeys.INTENT_CATEGORY); //Set a category for intent
        intentParams.putInt("intent_delivery", IntentKeys.INTENT_DELIVERY); // Set intent delivery mechanism
        intentParams.putString("intent_use_content_provider", "false"); //Enable content provider
        intentConfig.putBundle("PARAM_LIST", intentParams);
        bundlePluginConfig.add(intentConfig);
        /*### Configurations for Intent Output[Finish] ###*/

        //Putting the INTENT and BARCODE plugin settings to the PLUGIN_CONFIG extra
        bMain.putParcelableArrayList("PLUGIN_CONFIG", bundlePluginConfig);

        /*### Associate this application to the profile [Start] ###*/
        Bundle appConfig = new Bundle();
        appConfig.putString("PACKAGE_NAME",getPackageName());//Get Package name of the application
        appConfig.putStringArray("ACTIVITY_LIST", new String[]{"*"});//Add all activities of this application
        bMain.putParcelableArray("APP_LIST", new Bundle[]{
                appConfig
        });
        /*### Associate this application to the profile [Finish] ###*/

        bMain.putString("PROFILE_NAME", IntentKeys.PROFILE_NAME); //Initialize the profile name
        bMain.putString("PROFILE_ENABLED","true");//Enable the profile
        bMain.putString("CONFIG_MODE", "CREATE_IF_NOT_EXIST");
        bMain.putString("RESET_CONFIG", "true");//Enable reset configuration if already exist

        Intent intent = new Intent();
        intent.setAction(IntentKeys.DATAWEDGE_API_ACTION);
        intent.setPackage(IntentKeys.DATAWEDGE_PACKAGE);
        intent.putExtra(IntentKeys.DATAWEDGE_CONFIG, bMain);
        intent.putExtra("SEND_RESULT", "COMPLETE_RESULT");
        intent.putExtra(IntentKeys.COMMAND_IDENTIFIER_EXTRA,
                IntentKeys.COMMAND_IDENTIFIER_CREATE_PROFILE);
        this.sendBroadcast(intent);

    }

    private void switchToBarcodeHighlighting(boolean internalImager)
    {
        //Specify the DataWedge action and SWITCH_DATACAPTURE API parameters
        Intent i = new Intent();
        i.setAction("com.symbol.datawedge.api.ACTION");
        i.putExtra("APPLICATION_PACKAGE", getPackageName());
        i.setPackage("com.symbol.datawedge");
        i.putExtra("SEND_RESULT", "LAST_RESULT");
        i.putExtra("com.symbol.datawedge.api.SWITCH_DATACAPTURE", "BARCODE");

        Bundle paramList = new Bundle();
        //Specify the scanner to use (Only internal imager and camera are supported currently)
        if(internalImager)
        {
            paramList.putString("scanner_selection_by_identifier", "INTERNAL_IMAGER");
        }
        else
        {
            paramList.putString("scanner_selection_by_identifier", "INTERNAL_CAMERA");
        }
        //Enable barcode highlighting
        paramList.putString("barcode_highlighting_enabled", "true");

        // Create a bundle ignore case for later use (in "contains" criteria)
        Bundle bundleIgnoreCase = new Bundle();
        bundleIgnoreCase.putString("criteria_key","ignore_case");
        bundleIgnoreCase.putString("criteria_value","true");

        //Create a barcode highlighting Rule 1 [Start]
        Bundle ruleHighlightData = new Bundle();
        ruleHighlightData.putString("rule_name", "Rule1");
        Bundle ruleHighlightDataCriteria = new Bundle();

        //Set the criteria/condition. Specify the contains parameter.
        Bundle bundleContains1 = new Bundle();
        bundleContains1.putString("criteria_key", "contains");
        if(et_red.getText().toString().isEmpty() == false)
            bundleContains1.putString("criteria_value", et_red.getText().toString());
        else
            bundleContains1.putString("criteria_value", "http");



        //Container is just one parameter of identifier group.
        // There are other params such as ignore case, min length, max length
        ArrayList<Bundle> identifierParamList = new ArrayList<>();
        identifierParamList.add(bundleContains1);
        identifierParamList.add(bundleIgnoreCase);

        //Add the parameters of "identifier" group as a ParcelableArrayList to criteria list
        ruleHighlightDataCriteria.putParcelableArrayList("identifier", identifierParamList);

        //Add the criteria to Rule bundle
        ruleHighlightData.putBundle("criteria", ruleHighlightDataCriteria);

        //Set up the action bundle by specifying the color to be highlight
        Bundle bundleFillColor = new Bundle();
        bundleFillColor.putString("action_key", "fillcolor");
        //bundleFillColor.putString("action_value", "#CEF04E6E");
        bundleFillColor.putString("action_value", "#CEFF0000");

        ArrayList<Bundle> ruleHighlightDataActions = new ArrayList<>();
        ruleHighlightDataActions.add(bundleFillColor);
        ruleHighlightData.putParcelableArrayList("actions", ruleHighlightDataActions);
        //Create a barcode highlighting Rule 1 [Finish]

        //Create a barcode highlighting Rule 2 [Start]
        Bundle ruleReportData = new Bundle();
        ruleReportData.putString("rule_name", "Rule2");
        Bundle ruleReportDataCriteria = new Bundle();

        Bundle bundleContains2 = new Bundle();
        bundleContains2.putString("criteria_key", "contains");

        if(et_green.getText().toString().isEmpty() == false)
            bundleContains2.putString("criteria_value", et_green.getText().toString());
        else
            bundleContains2.putString("criteria_value", "A");

        ArrayList<Bundle> identifierParamList2 = new ArrayList<>();
        identifierParamList2.add(bundleContains2);
        identifierParamList2.add(bundleIgnoreCase);

        ruleReportDataCriteria.putParcelableArrayList("identifier", identifierParamList2);

        ruleReportData.putBundle("criteria", ruleReportDataCriteria);
        Bundle ruleReportDataBundleStrokeColor = new Bundle();
        ruleReportDataBundleStrokeColor.putString("action_key", "fillcolor");
        //ruleReportDataBundleStrokeColor.putString("action_value", "#CE7F2714");
        ruleReportDataBundleStrokeColor.putString("action_value", "#CEFFFF00");
        ArrayList<Bundle> ruleReportDataActions = new ArrayList<>();
        ruleReportDataActions.add(ruleReportDataBundleStrokeColor);
        ruleReportData.putParcelableArrayList("actions", ruleReportDataActions);
        //Create a barcode highlighting Rule 2 [Finish]

        //Create a barcode highlighting Rule 3 [Start]
        Bundle rule3 = new Bundle();
        rule3.putString("rule_name", "Rule3");
        Bundle rule3Criteria = new Bundle();

        Bundle bundleContains3 = new Bundle();
        bundleContains3.putString("criteria_key", "contains");

        if(et_blue.getText().toString().isEmpty() == false)
            bundleContains3.putString("criteria_value", et_blue.getText().toString());
        else
            bundleContains3.putString("criteria_value", "0");

        ArrayList<Bundle> identifierParamList3 = new ArrayList<>();
        identifierParamList3.add(bundleContains3);

        rule3Criteria.putParcelableArrayList("identifier", identifierParamList3);

        rule3.putBundle("criteria", rule3Criteria);
        Bundle rule3BundleStrokeColor = new Bundle();
        rule3BundleStrokeColor.putString("action_key", "fillcolor");
        //rule3BundleStrokeColor.putString("action_value", "#CE7F2714");
        rule3BundleStrokeColor.putString("action_value", "#CE0000FF");
        ArrayList<Bundle> rule3Actions = new ArrayList<>();
        rule3Actions.add(rule3BundleStrokeColor);
        rule3.putParcelableArrayList("actions", rule3Actions);
        //Create a barcode highlighting Rule 2 [Finish]

        //Add the two created rules to the rule list
        ArrayList<Bundle> ruleList = new ArrayList<>();
        ruleList.add(ruleHighlightData);
        ruleList.add(ruleReportData);
        ruleList.add(rule3);


        //Assign the rule list to barcode_overlay parameter
        Bundle ruleBundlebarcodeOverlay = new Bundle();
        ruleBundlebarcodeOverlay.putString("rule_param_id", "barcode_overlay");
        ruleBundlebarcodeOverlay.putParcelableArrayList("rule_list", ruleList);


        ArrayList<Bundle> ruleParamList = new ArrayList<>();
        ruleParamList.add(ruleBundlebarcodeOverlay);

        paramList.putParcelableArrayList("rules", ruleParamList);

        i.putExtra("PARAM_LIST", paramList);

        sendBroadcast(i);
    }


    private void switchToBarcodeReporting(boolean internalImager) {
        //Specify the DataWedge action and SWITCH_DATACAPTURE API parameters
        Intent i = new Intent();
        i.setAction("com.symbol.datawedge.api.ACTION");
        i.putExtra("APPLICATION_PACKAGE", getPackageName());
        i.setPackage("com.symbol.datawedge");
        i.putExtra("SEND_RESULT", "LAST_RESULT");
        i.putExtra("com.symbol.datawedge.api.SWITCH_DATACAPTURE", "BARCODE");

        String contains = et_contains.getText().toString();

        Bundle paramList = new Bundle();
        //Specify the scanner to use (Only internal imager and camera are supported currently)
        if(internalImager)
        {
            paramList.putString("scanner_selection_by_identifier", "INTERNAL_IMAGER");
        }
        else
        {
            paramList.putString("scanner_selection_by_identifier", "INTERNAL_CAMERA");
        }

        //Enable barcode highlighting
        paramList.putString("barcode_highlighting_enabled", "true");


        //Create a barcode highlighting Rule 1 [Start]
        Bundle ruleHighlightData = new Bundle();
        ruleHighlightData.putString("rule_name", "Rule1");
        Bundle ruleHighlightDataCriteria = new Bundle();

        //Set the criteria/condition. Specify the contains parameter.
        Bundle bundleContains1 = new Bundle();
        bundleContains1.putString("criteria_key", "contains");
        bundleContains1.putString("criteria_value", contains);

        Bundle ruleHighlightDataBundleIgnoreCase = new Bundle();
        ruleHighlightDataBundleIgnoreCase.putString("criteria_key","ignore_case");
        ruleHighlightDataBundleIgnoreCase.putString("criteria_value","true");

        //Container is just one parameter of identifier group.
        // There are other params such as ignore case, min length, max length
        ArrayList identifierParamList = new ArrayList<>();
        identifierParamList.add(bundleContains1);
        identifierParamList.add(ruleHighlightDataBundleIgnoreCase);


        //Add the parameters of "identifier" group as a ParcelableArrayList to criteria list
        ruleHighlightDataCriteria.putParcelableArrayList("identifier", identifierParamList);
        ruleHighlightDataCriteria.putStringArray("symbology",new String[]{sSelectedSymbology});

        //Add the criteria to Rule bundle
        ruleHighlightData.putBundle("criteria", ruleHighlightDataCriteria);

        //Set up the action bundle by specifying the color to be highlight
        Bundle bundleFillColor = new Bundle();
        bundleFillColor.putString("action_key", "fillcolor");
        bundleFillColor.putString("action_value", "#CEFF0000");

        ArrayList ruleHighlightDataActions = new ArrayList<>();
        ruleHighlightDataActions.add(bundleFillColor);
        ruleHighlightData.putParcelableArrayList("actions", ruleHighlightDataActions);
        //Create a barcode highlighting Rule 1 [Finish]

        ArrayList<Bundle> highlightDataRuleList = new ArrayList<>();
        highlightDataRuleList.add(ruleHighlightData);
        //reportDataRuleList.add(ruleHighlightData);


        //Assign the rule list to barcode_overlay parameter
        Bundle ruleBundlebarcodeOverlay = new Bundle();
        ruleBundlebarcodeOverlay.putString("rule_param_id", "barcode_overlay");
        ruleBundlebarcodeOverlay.putParcelableArrayList("rule_list", highlightDataRuleList);

        /**
         * report data
         */
        /*##### Report data for rule Start #####*/
        Bundle ruleReportData = new Bundle();
        ruleReportData.putString("rule_name","Rule2");
        Bundle ruleReportDataCriteria = new Bundle();

        Bundle ruleReportDataBundleContains = new Bundle();
        ruleReportDataBundleContains.putString("criteria_key","contains");
        ruleReportDataBundleContains.putString("criteria_value",contains);

        Bundle ruleReportDataBundleIgnoreCase = new Bundle();
        ruleReportDataBundleIgnoreCase.putString("criteria_key","ignore_case");
        ruleReportDataBundleIgnoreCase.putString("criteria_value","true");

        ArrayList<Bundle> ruleReportDataIdentifierParamList = new ArrayList<>();
        ruleReportDataIdentifierParamList.add(ruleReportDataBundleContains);
        ruleReportDataIdentifierParamList.add(ruleReportDataBundleIgnoreCase);

        ruleReportDataCriteria.putParcelableArrayList("identifier",ruleReportDataIdentifierParamList);
        ruleReportDataCriteria.putStringArray("symbology",new String[]{sSelectedSymbology});
        ruleReportData.putBundle("criteria",ruleReportDataCriteria);

        Bundle ruleReportDataBundleReport = new Bundle();
        ruleReportDataBundleReport.putString("action_key","report");

        /*
        Bundle ruleReportDataBundleReportStrokeColor = new Bundle();
        ruleReportDataBundleReportStrokeColor.putString("action_key", "fillcolor");
        String color = et_color.getText().toString();
        if(color.isEmpty())
            color = "#CE00FF00";
        ruleReportDataBundleReportStrokeColor.putString("action_value", color);
        */

        ArrayList<Bundle> ruleReportDataActions = new ArrayList<>();
        ruleReportDataActions.add(ruleReportDataBundleReport);
        //ruleReportDataActions.add(ruleReportDataBundleReportStrokeColor);
        ruleReportData.putParcelableArrayList("actions",ruleReportDataActions);

        ArrayList<Bundle> reportDataRuleList = new ArrayList<>();
        reportDataRuleList.add(ruleReportData);
        //reportDataRuleList.add(ruleHighlightData);

        Bundle ruleBundleReportData = new Bundle();
        ruleBundleReportData.putString("rule_param_id","report_data");
        ruleBundleReportData.putParcelableArrayList("rule_list", reportDataRuleList);

        /*##### Report data for rule End #####*/

        ArrayList<Bundle> ruleParamList = new ArrayList<>();
        ruleParamList.add(ruleBundlebarcodeOverlay);
        ruleParamList.add(ruleBundleReportData);
        /*##### Rules configuration end #####*/

        paramList.putParcelableArrayList("rules", ruleParamList);

        i.putExtra("PARAM_LIST", paramList);

        sendBroadcast(i);
    }


    private void switchToRegularScan()
    {
        //Specify the DataWedge action and SWITCH_DATACAPTURE API parameters
        Intent i = new Intent();
        i.setAction("com.symbol.datawedge.api.ACTION");
        i.putExtra("APPLICATION_PACKAGE", getPackageName());
        i.setPackage("com.symbol.datawedge");
        i.putExtra("SEND_RESULT", "LAST_RESULT");
        i.putExtra("com.symbol.datawedge.api.SWITCH_DATACAPTURE", "BARCODE");

        Bundle paramList = new Bundle();
        //Specify the scanner to use (Only internal imager and camera are supported currently)
        paramList.putString("scanner_selection_by_identifier", "INTERNAL_IMAGER");
        //Disable barcode highlighting
        paramList.putString("barcode_highlighting_enabled", "false");

        i.putExtra("PARAM_LIST", paramList);

        sendBroadcast(i);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                setStatus("OnBroadcastReceived, Action: " + action);
                Bundle extras = intent.getExtras();

                if (intent.hasExtra(IntentKeys.EXTRA_RESULT_GET_PROFILES_LIST)){
                    String[] arrayProfileList = extras.getStringArray(IntentKeys.EXTRA_RESULT_GET_PROFILES_LIST);
                    List<String> profileList = Arrays.asList(arrayProfileList);
                    //check whether the profile is exist or not
                    if(profileList.contains(IntentKeys.PROFILE_NAME)){
                        //if the profile is already exist
                        setStatus("Profile already exists, not creating the profile");
                        //switchToRegularScan();
                    }else{
                        //if the profile doest exist
                        setStatus("Profile does not exists. Creating the profile..");
                        createProfile();
                    }
                }
                else if(extras.containsKey(IntentKeys.COMMAND_IDENTIFIER_EXTRA)){
                    /*## Processing the result of CREATE_PROFILE[Start] ###*/
                    if(extras.getString(IntentKeys.COMMAND_IDENTIFIER_EXTRA)
                            .equalsIgnoreCase(IntentKeys.COMMAND_IDENTIFIER_CREATE_PROFILE)){
                        ArrayList<Bundle> bundleList = intent.getParcelableArrayListExtra("RESULT_LIST");
                        if (bundleList != null && bundleList.size()>0){
                            boolean allSuccess = true;
                            StringBuilder resultInfo = new StringBuilder();
                            //Iterate through the result list for each module
                            for(Bundle bundle : bundleList){
                                if (bundle.getString("RESULT")
                                        .equalsIgnoreCase(IntentKeys.INTENT_RESULT_CODE_FAILURE)){
                                    //if the profile creation failure for that module, provide more information on that
                                    allSuccess = false;
                                    resultInfo.append("Module Name : ")
                                            .append(bundle.getString("MODULE"))
                                            .append("\n"); //Name of the module

                                    resultInfo.append("Result code: ").
                                            append(bundle.getString("RESULT_CODE")).
                                            append("\n");//Information of the moule

                                    if(bundle.containsKey("SUB_RESULT_CODE")) {
                                        resultInfo.append("\tSub Result code: ")
                                                .append(bundle.getString("SUB_RESULT_CODE"))
                                                .append("\n");
                                    }
                                    break; // Breaking the loop as there is a failure
                                }else {
                                    //if the profile creation success for the module.
                                    resultInfo.append("Module: " )
                                            .append(bundle.getString("MODULE"))
                                            .append("\n");

                                    resultInfo.append("Result: ")
                                            .append(bundle.getString("RESULT"))
                                            .append("\n");
                                }
                            }
                            if (allSuccess) {
                                setStatus("Profile created successfully");
                                switchToRegularScan();
                            } else {
                                setStatus("Profile creation failed!\n\n" + resultInfo);
                                deleteProfile();
                            }
                        }
                    }
                    /*### Processing the result of CREATE_PROFILE [Finish] ###*/
                }
                else if(action.equals(IntentKeys.INTENT_OUTPUT_ACTION)){
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String data = intent.getStringExtra(IntentKeys.DATA_STRING_TAG);
                            String source = intent.getStringExtra(IntentKeys.SOURCE_TAG);
                            String sLabelType = intent.getStringExtra(IntentKeys.LABEL_TYPE_TAG);
                            if(data != null)
                            {
                                // we have some data, so let's get it's symbology
                                // check if the string is empty
                                if (sLabelType != null && sLabelType.length() > 0) {
                                    // format of the label type string is LABEL-TYPE-SYMBOLOGY
                                    // so let's skip the LABEL-TYPE- portion to get just the symbology
                                    sLabelType = sLabelType.substring(11);
                                }
                                else {
                                    // the string was empty so let's set it to "Unknown"
                                    sLabelType = "Unknown";
                                }
                                setStatus("------- Scan Received ---------");
                                if(source != null && source.isEmpty() == false)
                                    setStatus("Source: " + source);
                                if(sLabelType != null && sLabelType.isEmpty() == false && sLabelType.equalsIgnoreCase("unknown") == false)
                                    setStatus("Type: " + sLabelType);
                                if(data != null && data.isEmpty() == false)
                                    setStatus("Value: " + data);
                                setStatus("-------------------------------");
                            }
                            else {
                                String dataToDecode = intent.getStringExtra(IntentKeys.DECODE_DATA);
                                if (dataToDecode != null) {
                                    setStatus("Decode data:" + dataToDecode);
                                }
                            }
                        }
                    });
                    thread.start();
                }
                else if(action.equals("com.symbol.datawedge.api.NOTIFICATION_ACTION"))
                {
                    if (intent.hasExtra("com.symbol.datawedge.api.NOTIFICATION")) {
                        Bundle b = intent.getBundleExtra("com.symbol.datawedge.api.NOTIFICATION");
                        String NOTIFICATION_TYPE = b.getString("NOTIFICATION_TYPE");
                        if (NOTIFICATION_TYPE != null) {
                            switch (NOTIFICATION_TYPE) {
                                case "WORKFLOW_STATUS":
                                case "SCANNER_STATUS":

                                    String status = b.getString("STATUS");
                                    setStatus("Status: " + status);
                                    break;
                            }
                        }
                    }
                }
            }catch (Exception ex){
                Log.e(TAG, "onReceive: ", ex);
            }
        }
    };


    @SuppressLint("Range")
    private byte[] processUri(String uri)
    {
        Cursor cursor = getContentResolver().query(Uri.parse(uri),null,null,null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if(cursor != null)
        {
            cursor.moveToFirst();
            try {
                baos.write(cursor.getBlob(cursor.getColumnIndex("raw_data")));
            } catch (IOException e) {
                Log.w(TAG, "Output Stream Write error " + e.getMessage());
            }
            String nextURI = cursor.getString(cursor.getColumnIndex("next_data_uri"));
            while (nextURI != null && !nextURI.isEmpty())
            {
                Cursor cursorNextData = getContentResolver().query(Uri.parse(nextURI),
                        null,null,null);
                if(cursorNextData != null)
                {
                    cursorNextData.moveToFirst();
                    try {
                        baos.write(cursorNextData.getBlob(cursorNextData.
                                getColumnIndex("raw_data")));
                    } catch (IOException e) {
                        Log.w(TAG, "Output Stream Write error " + e.getMessage());
                    }
                    nextURI = cursorNextData.getString(cursorNextData.
                            getColumnIndex("next_data_uri"));

                    cursorNextData.close();
                }
            }
            cursor.close();
        }
        return baos.toByteArray();
    }


    private String getImageFormat(int type){
        String imageFormat = "";
        switch (type){
            case 1:
                imageFormat = "JPEG";
                break;
            case 3:
                imageFormat = "BMP";
                break;
            case 4:
                imageFormat = "TIFF";
                break;
            case 5:
                imageFormat = "YUV";
                break;
        }
        return imageFormat;
    }

    private void setStatus(final String lineToAdd)
    {
        statusString += lineToAdd + "\n";
        Log.d(TAG, lineToAdd);
        updateAndScrollDownTextView();
    }

    private void updateAndScrollDownTextView() {
        if (mScrollDownRunnable == null) {
            mScrollDownRunnable = new Runnable() {
                @Override
                public void run() {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            txtStatus.setText(statusString);
                            sv_status.post(new Runnable() {
                                @Override
                                public void run() {
                                    sv_status.fullScroll(ScrollView.FOCUS_DOWN);
                                }
                            });
                        }
                    });
                }
            };
        } else {
            // A new line has been added while we were waiting to scroll down
            // reset handler to repost it....
            if(mScrollDownHandler != null) {
                mScrollDownHandler.removeCallbacks(mScrollDownRunnable);
            }
        }
        if(mScrollDownHandler != null)
            mScrollDownHandler.postDelayed(mScrollDownRunnable, 300);
    }

}