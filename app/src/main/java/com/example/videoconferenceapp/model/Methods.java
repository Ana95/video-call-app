package com.example.videoconferenceapp.model;

import android.graphics.Color;

import com.example.videoconferenceapp.R;

public class Methods {

    public void setColorTheme(int selectedColor){
        switch (selectedColor){
            case -14654801:
                Constant.theme = R.style.AppTheme_darkblue;
                Constant.color = R.color.darkblue_colorPrimary;
                break;
            case -10965321:
                Constant.theme = R.style.AppTheme_shadowOfBlue1;
                Constant.color = R.color.shadowOfBlue1_colorPrimary;
                break;
            case -740056:
                Constant.theme = R.style.AppTheme_orange;
                Constant.color = R.color.orange_colorPrimary;
                break;
            case -11419154:
                Constant.theme = R.style.AppTheme_skyblue;
                Constant.color = R.color.skyblue_colorPrimary;
                break;
            case -2277816:
                Constant.theme = R.style.AppTheme_pink;
                Constant.color = R.color.pink_colorPrimary;
                break;
            case -4224594:
                Constant.theme = R.style.AppTheme_purple;
                Constant.color = R.color.purple_colorPrimary;
                break;
            case -10712898:
                Constant.theme = R.style.AppTheme_shadowOfBlue2;
                Constant.color = R.color.shadowOfBlue2_colorPrimary;
                break;
            case -10896368:
                Constant.theme = R.style.AppTheme_green;
                Constant.color = R.color.green_colorPrimaryDark;
                break;
            case -1544140:
                Constant.theme = R.style.AppTheme_darkorange;
                Constant.color = R.color.darkorange_colorPrimary;
                break;
            case -504764:
                Constant.theme = R.style.AppTheme_red;
                Constant.color = R.color.red_colorPrimary;
                break;
            case -7583749:
                Constant.theme = R.style.AppTheme_purple1;
                Constant.color = R.color.purple1_colorPrimary;
                break;

            case -4024195:
                Constant.theme = R.style.AppTheme_brown;
                Constant.color = R.color.brown_colorPrimary;
                break;
            case -7305542:
                Constant.theme = R.style.AppTheme_violet;
                Constant.color = R.color.violet_colorPrimary;
                break;
            case -7551917:
                Constant.theme = R.style.AppTheme_lightgreen;
                Constant.color = R.color.lightgreen_colorPrimary;
                break;
            case -3246217:
                Constant.theme = R.style.AppTheme_darkred;
                Constant.color = R.color.darkred_colorPrimary;
                break;
            default:
                Constant.theme = R.style.AppTheme;
                Constant.color = R.color.colorPrimary;
                break;
        }
    }

}
