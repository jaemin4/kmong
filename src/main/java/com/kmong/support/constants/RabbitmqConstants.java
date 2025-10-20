package com.kmong.support.constants;

public final class RabbitmqConstants {
    //AccessLog
    public static final String EXCHANGE_ACCESS_LOG = "esim.exchange.access.log";
    public static final String QUEUE_ACCESS_LOG_SAVE = "esim.queue.access.log.save";
    public static final String ROUTING_ACCESS_LOG_SAVE = "route.access.log.save";

    //Email
    public static final String EXCHANGE_MAIL = "esim.exchange.mail";
    public static final String QUEUE_MAIL_SEND = "esim.queue.mail.send";
    public static final String ROUTING_MAIL_SEND = "esim.route.mail.send";

    //SMS
    public static final String EXCHANGE_SMS_COOL = "esim.exchange.sms.cool";
    public static final String QUEUE_SMS_COOL = "esim.queue.sms.cool.send";
    public static final String ROUTING_SMS_SEND = "esim.route.sms.cool.send";
    private RabbitmqConstants() {
    }
}
