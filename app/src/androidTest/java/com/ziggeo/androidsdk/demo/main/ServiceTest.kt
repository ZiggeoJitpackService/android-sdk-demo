package com.ziggeo.androidsdk.demo.main

import com.ziggeo.androidsdk.net.uploading.impl.UploadingService

class ServiceTest : UploadingService() {

//    private  receiver:SomeBroadCastReceiver;


    public override fun onCreate() {
        super.onCreate();
        //...init broadcast receiver and whatnot
    }

    @Override
    public override fun onDestroy() {
//        unregisterReceiver(receiver);
        super.onDestroy();
    }
}