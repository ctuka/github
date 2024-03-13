package com.tkoseli.securecapita.utils;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SmsUtils {
    public static final String FROM_NUMBER = "+18888618022";
    public static final String SID_KEY = "ACab424ffaaf04326f25ca7859949154a0";
    public static final String TOKEN_KEY = "69676ec8c5c600f49f6337a06565b2f9";

    public static void sendSms(String to, String messageBody)
    {

        Twilio.init(SID_KEY, TOKEN_KEY);
        Message message = Message.creator(new PhoneNumber("+"+ to), new PhoneNumber(FROM_NUMBER), messageBody).create();
        System.out.println(message);
    }
}
