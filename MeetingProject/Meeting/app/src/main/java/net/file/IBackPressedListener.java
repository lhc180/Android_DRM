package net.file;

/**
 * Created by simaben on 2014/8/22.
 */
public interface IBackPressedListener {
    /**
     * 处理back事件。
     *
     * @return True: 表示已经处理; False: 没有处理，让基类处理。
     */
    boolean onBack();
}