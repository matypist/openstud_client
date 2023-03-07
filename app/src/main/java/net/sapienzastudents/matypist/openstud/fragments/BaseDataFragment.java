package net.sapienzastudents.matypist.openstud.fragments;

import android.app.Activity;

import androidx.fragment.app.Fragment;

import net.sapienzastudents.matypist.openstud.data.InfoManager;
import net.sapienzastudents.matypist.openstud.helpers.ClientHelper;

import matypist.openstud.driver.core.Openstud;

public abstract class BaseDataFragment extends Fragment {
    Openstud os;

    public BaseDataFragment() {
        super();
    }

    public boolean initData() {
        Activity activity = getActivity();
        if (activity == null) return false;
        os = InfoManager.getOpenStud(activity);
        if (os == null) {
            ClientHelper.rebirthApp(activity, null);
            return false;
        }
        return true;
    }
}
