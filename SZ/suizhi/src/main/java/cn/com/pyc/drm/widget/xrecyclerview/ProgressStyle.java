package cn.com.pyc.drm.widget.xrecyclerview;

import com.wang.avi.Indicator;
import com.wang.avi.indicators.BallBeatIndicator;
import com.wang.avi.indicators.BallClipRotateIndicator;
import com.wang.avi.indicators.BallClipRotateMultipleIndicator;
import com.wang.avi.indicators.BallClipRotatePulseIndicator;
import com.wang.avi.indicators.BallGridBeatIndicator;
import com.wang.avi.indicators.BallGridPulseIndicator;
import com.wang.avi.indicators.BallPulseIndicator;
import com.wang.avi.indicators.BallPulseRiseIndicator;
import com.wang.avi.indicators.BallPulseSyncIndicator;
import com.wang.avi.indicators.BallRotateIndicator;
import com.wang.avi.indicators.BallScaleIndicator;
import com.wang.avi.indicators.BallScaleMultipleIndicator;
import com.wang.avi.indicators.BallScaleRippleIndicator;
import com.wang.avi.indicators.BallScaleRippleMultipleIndicator;
import com.wang.avi.indicators.BallSpinFadeLoaderIndicator;
import com.wang.avi.indicators.BallTrianglePathIndicator;
import com.wang.avi.indicators.BallZigZagDeflectIndicator;
import com.wang.avi.indicators.BallZigZagIndicator;
import com.wang.avi.indicators.CubeTransitionIndicator;
import com.wang.avi.indicators.LineScaleIndicator;
import com.wang.avi.indicators.LineScalePartyIndicator;
import com.wang.avi.indicators.LineScalePulseOutIndicator;
import com.wang.avi.indicators.LineScalePulseOutRapidIndicator;
import com.wang.avi.indicators.LineSpinFadeLoaderIndicator;
import com.wang.avi.indicators.PacmanIndicator;
import com.wang.avi.indicators.SemiCircleSpinIndicator;
import com.wang.avi.indicators.SquareSpinIndicator;
import com.wang.avi.indicators.TriangleSkewSpinIndicator;

/**
 * Created by jianghejie on 15/11/23.
 */
public class ProgressStyle {
    public static final int SysProgress = -1;
    public static final int BallPulse = 0;
    public static final int BallGridPulse = 1;
    public static final int BallClipRotate = 2;
    public static final int BallClipRotatePulse = 3;
    public static final int SquareSpin = 4;
    public static final int BallClipRotateMultiple = 5;
    public static final int BallPulseRise = 6;
    public static final int BallRotate = 7;
    public static final int CubeTransition = 8;
    public static final int BallZigZag = 9;
    public static final int BallZigZagDeflect = 10;
    public static final int BallTrianglePath = 11;
    public static final int BallScale = 12;
    public static final int LineScale = 13;
    public static final int LineScaleParty = 14;
    public static final int BallScaleMultiple = 15;
    public static final int BallPulseSync = 16;
    public static final int BallBeat = 17;
    public static final int LineScalePulseOut = 18;
    public static final int LineScalePulseOutRapid = 19;
    public static final int BallScaleRipple = 20;
    public static final int BallScaleRippleMultiple = 21;
    public static final int BallSpinFadeLoader = 22;
    public static final int LineSpinFadeLoader = 23;
    public static final int TriangleSkewSpin = 24;
    public static final int Pacman = 25;
    public static final int BallGridBeat = 26;
    public static final int SemiCircleSpin = 27;

    private static Indicator mIndicatorController;

    public static Indicator applyIndicator(int style) {
        mIndicatorController = null;
        switch (style) {
            case BallPulse:
                mIndicatorController = new BallPulseIndicator();
                break;
            case BallGridPulse:
                mIndicatorController = new BallGridPulseIndicator();
                break;
            case BallClipRotate:
                mIndicatorController = new BallClipRotateIndicator();
                break;
            case BallClipRotatePulse:
                mIndicatorController = new BallClipRotatePulseIndicator();
                break;
            case SquareSpin:
                mIndicatorController = new SquareSpinIndicator();
                break;
            case BallClipRotateMultiple:
                mIndicatorController = new BallClipRotateMultipleIndicator();
                break;
            case BallPulseRise:
                mIndicatorController = new BallPulseRiseIndicator();
                break;
            case BallRotate:
                mIndicatorController = new BallRotateIndicator();
                break;
            case CubeTransition:
                mIndicatorController = new CubeTransitionIndicator();
                break;
            case BallZigZag:
                mIndicatorController = new BallZigZagIndicator();
                break;
            case BallZigZagDeflect:
                mIndicatorController = new BallZigZagDeflectIndicator();
                break;
            case BallTrianglePath:
                mIndicatorController = new BallTrianglePathIndicator();
                break;
            case BallScale:
                mIndicatorController = new BallScaleIndicator();
                break;
            case LineScale:
                mIndicatorController = new LineScaleIndicator();
                break;
            case LineScaleParty:
                mIndicatorController = new LineScalePartyIndicator();
                break;
            case BallScaleMultiple:
                mIndicatorController = new BallScaleMultipleIndicator();
                break;
            case BallPulseSync:
                mIndicatorController = new BallPulseSyncIndicator();
                break;
            case BallBeat:
                mIndicatorController = new BallBeatIndicator();
                break;
            case LineScalePulseOut:
                mIndicatorController = new LineScalePulseOutIndicator();
                break;
            case LineScalePulseOutRapid:
                mIndicatorController = new LineScalePulseOutRapidIndicator();
                break;
            case BallScaleRipple:
                mIndicatorController = new BallScaleRippleIndicator();
                break;
            case BallScaleRippleMultiple:
                mIndicatorController = new BallScaleRippleMultipleIndicator();
                break;
            case BallSpinFadeLoader:
                mIndicatorController = new BallSpinFadeLoaderIndicator();
                break;
            case LineSpinFadeLoader:
                mIndicatorController = new LineSpinFadeLoaderIndicator();
                break;
            case TriangleSkewSpin:
                mIndicatorController = new TriangleSkewSpinIndicator();
                break;
            case Pacman:
                mIndicatorController = new PacmanIndicator();
                break;
            case BallGridBeat:
                mIndicatorController = new BallGridBeatIndicator();
                break;
            case SemiCircleSpin:
                mIndicatorController = new SemiCircleSpinIndicator();
                break;
            default:
                mIndicatorController = new BallSpinFadeLoaderIndicator();
                break;
        }
        return mIndicatorController;
    }
}
