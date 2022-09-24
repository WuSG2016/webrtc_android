package com.dds.core.serialport;

/**
 * @author WuSG
 * @date : 2022/9/18 12:38
 */
public class CmdBeanPool extends ObjectsPool<CmdBean> {
    /**
     * 创建一个对象池
     *
     * @param size 对象池最大容量
     */
    public CmdBeanPool(int size) {
        super(size);
    }

    @Override
    public synchronized CmdBean take() {
        return super.take();
    }

    @Override
    protected CmdBean newInstance() {
        return new CmdBean();
    }

    @Override
    protected CmdBean resetInstance(CmdBean obj) {
        return obj;
    }
}
