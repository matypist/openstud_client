package com.lithium.leona.openstud.activities;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.danielstone.materialaboutlibrary.MaterialAboutActivity;
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem;
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard;
import com.danielstone.materialaboutlibrary.model.MaterialAboutList;
import com.lithium.leona.openstud.R;
import com.lithium.leona.openstud.helpers.ClientHelper;
import com.lithium.leona.openstud.helpers.LayoutHelper;
import com.lithium.leona.openstud.helpers.ThemeEngine;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;

public class ContributorsActivity extends MaterialAboutActivity {
    @NonNull
    @Override
    protected MaterialAboutList getMaterialAboutList(@NonNull Context context) {
        MaterialAboutCard.Builder appCardBuilder1 = new MaterialAboutCard.Builder();
        MaterialAboutCard.Builder appCardBuilder2 = new MaterialAboutCard.Builder();
        MaterialAboutCard.Builder appCardBuilder3 = new MaterialAboutCard.Builder();
        MaterialAboutCard.Builder appCardBuilder4 = new MaterialAboutCard.Builder();
        MaterialAboutCard.Builder appCardBuilder5 = new MaterialAboutCard.Builder();
        MaterialAboutCard.Builder appCardBuilder6 = new MaterialAboutCard.Builder();
        buildContributors(context, appCardBuilder1, "Luigi Russo", getResources().getString(R.string.developer), null);
        buildContributors(context, appCardBuilder2, "Matteo Collica", getResources().getString(R.string.developer), null);
        buildContributors(context, appCardBuilder3, "Leonardo Razovic", "Logo designer", "https://www.twitter.com/lrazovic");
        buildContributors(context, appCardBuilder4, "Valerio Silvestro", "Tester", null);
        buildContributors(context, appCardBuilder5, "Ugo Possenti", "Concept designer", "https://twitter.com/MEPoss");
        buildContributors(context, appCardBuilder6, "SapienzaApps", getResources().getString(R.string.sapienzaaps), null);
        return new MaterialAboutList(appCardBuilder1.build(), appCardBuilder2.build(), appCardBuilder6.build(), appCardBuilder3.build(), appCardBuilder4.build(), appCardBuilder5.build());
    }

    @Nullable
    @Override
    protected CharSequence getActivityTitle() {
        return getResources().getString(R.string.contributors);
    }

    protected void onCreate(Bundle savedInstanceState) {
        ThemeEngine.applyAboutTheme(this);
        super.onCreate(savedInstanceState);
    }

    private void buildContributors(Context context, MaterialAboutCard.Builder authorCardBuilder, String name, String role, String twitterLink) {
        int tintColor = ThemeEngine.getPrimaryTextColor(this);
        Drawable person = ContextCompat.getDrawable(context, R.drawable.ic_person_outline_black);
        Drawable email = ContextCompat.getDrawable(context, R.drawable.ic_email_black);
        Drawable twitter = new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_twitter)
                .color(tintColor)
                .sizeDp(24);
        LayoutHelper.setColorSrcAtop(email, tintColor);
        LayoutHelper.setColorSrcAtop(person, tintColor);
        LayoutHelper.setColorSrcAtop(twitter, tintColor);
        authorCardBuilder.title(role);
        MaterialAboutActionItem.Builder generalCard = new MaterialAboutActionItem.Builder();
        MaterialAboutActionItem.Builder twitterCard = new MaterialAboutActionItem.Builder();
        generalCard.text(name).icon(person);
        authorCardBuilder.addItem(generalCard.build());
        if (twitterLink != null) {
            twitterCard.text(R.string.twitter)
                    .icon(twitter)
                    .setOnClickAction(() -> ClientHelper.createCustomTab(this, twitterLink));
            authorCardBuilder.addItem(twitterCard.build());
        }
    }


}
