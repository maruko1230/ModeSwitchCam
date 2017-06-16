package com.example.tatsuhiko.modeswitchingcamera.suppinCamera;

import android.content.Context;
import android.view.TextureView;

/**
 * Created by 22715722 on 2017/06/16.
 */

public class ViewFinderTexture extends TextureView {

    enum AspectRatio {
        SIXTEEN_TO_NINE,
        FOUR_TO_THREE,
    }

    public ViewFinderTexture(Context context) {
        super(context);
    }

}
