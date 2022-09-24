package com.dds.core.serialport;

/**
 * @author WuSG
 * @date : 2022/9/17 22:47
 */
public class SerialBeanPool extends ObjectsPool<SerialCmdBean> {
    /**
     * 创建一个对象池
     *
     * @param size 对象池最大容量
     */
    public SerialBeanPool(int size) {
        super(size);
    }

    @Override
    protected SerialCmdBean newInstance() {
        return new SerialCmdBean(new SerialCmdBean.Data());
    }

    @Override
    protected SerialCmdBean resetInstance(SerialCmdBean obj) {
        return obj;
    }
}
