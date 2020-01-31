package com.fg.dji_media

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import dji.common.error.DJIError
import dji.common.flightcontroller.*
import dji.common.flightcontroller.imu.IMUState
import dji.common.flightcontroller.virtualstick.FlightControlData
import dji.common.model.LocationCoordinate2D
import dji.common.util.CommonCallbacks
import dji.sdk.flightcontroller.FlightController
import dji.sdk.products.Aircraft
import dji.sdk.sdkmanager.DJISDKManager
import kotlinx.android.synthetic.main.activity_serial_key.*
import dji.sdk.base.BaseComponent as BaseComponent1
import org.bouncycastle.asn1.x500.style.RFC4519Style.serialNumber



class SerialNum : Activity() {

//    lateinit var flightController:FlightController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_serial_key)




       getKey()

    }

    fun getKey(){
//        flightController.getSerialNumber(object : CommonCallbacks.CompletionCallbackWith<String> {
//            override fun onSuccess(s: String) {
//                textView.text = s
//                Toast.makeText(this@SerialNum, "Success", Toast.LENGTH_LONG).show()
//            }
//
//            override fun onFailure(djiError: DJIError) {
//                textView.text = djiError.description.toString() + " ///// " + djiError.errorCode.toString()
//                Toast.makeText(this@SerialNum, "DJIError", Toast.LENGTH_LONG).show()
//            }
//        })
//
//        flightController.getFirmwareVersion(object : CommonCallbacks.CompletionCallbackWith<String> {
//            override fun onSuccess(p0: String?) {
//                textView.append("\n   ${p0.toString()}")
//                Toast.makeText(this@SerialNum, "Success", Toast.LENGTH_LONG).show()
//            }
//
//            override fun onFailure(p0: DJIError?) {
//                textView.append( " ///// " + p0.toString())
//                Toast.makeText(this@SerialNum, "DJIError", Toast.LENGTH_LONG).show()
//            }
//
//        })

//        val aircraft = DJISDKManager.getInstance().product;

        val aircraft =DJISDKManager.getInstance().product as Aircraft
//        val aircraft = DJISampleApplication.getProductInstance() as Aircraft
//        if (null != aircraft.flightController) {
            val flightController = aircraft.flightController
            flightController.getSerialNumber(object : CommonCallbacks.CompletionCallbackWith<String> {
                override fun onSuccess(s: String) {
                    textView.text = s
                }

                override fun onFailure(djiError: DJIError) {
                    textView.append( " ///// " + djiError.toString())
                    Toast.makeText(this@SerialNum, "DJIError", Toast.LENGTH_LONG).show()
                }
            })
        }

//    }

}