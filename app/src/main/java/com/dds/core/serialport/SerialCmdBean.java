package com.dds.core.serialport;

/**
 * @author WuSG
 * @date : 2022/9/17 22:36
 */
public class SerialCmdBean {
    //send-->{"data":{"__cmd_top":"0","__cmd_level":"0","room":"c8e8bd18-de37-4448-a586-416a214330fe1663424794902","__cmd_data":"1"},"eventName":"__cmd"}


    private String eventName;
    private Data data;

    public SerialCmdBean(Data data) {
        this.data = data;
    }

    public Data getData() {
        return data;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        private String __cmd_top;
        private String __cmd_level;
        private String room;
        private String __cmd_data;
        private String cmd_angle;

        public void setCmd_angle(String cmd_angle) {
            this.cmd_angle = cmd_angle;
        }

        public void set__cmd_top(String __cmd_top) {
            this.__cmd_top = __cmd_top;
        }

        public void set__cmd_level(String __cmd_level) {
            this.__cmd_level = __cmd_level;
        }

        public void setRoom(String room) {
            this.room = room;
        }

        public void set__cmd_data(String __cmd_data) {
            this.__cmd_data = __cmd_data;
        }

//        @Override
//        public String toString() {
//            return "{" +
//                    "cmd_top:'" + __cmd_top + '\'' +
//                    ", cmd_level:'" + __cmd_level + '\'' +
//                    ", room:'" + room + '\'' +
//                    ", cmd_data:'" + __cmd_data + '\'' +
//                    '}';
//        }

        @Override
        public String toString() {
            return "{" +
                    "cmd_top:'" + __cmd_top + '\'' +
                    ", cmd_level:'" + __cmd_level + '\'' +
                    ", room:'" + room + '\'' +
                    ", cmd_data:'" + __cmd_data + '\'' +
                    ", cmd_angle:'" + cmd_angle + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "{" +
                "eventName:'" + eventName + '\'' +
                ", data:" + data +
                '}';
    }


}
