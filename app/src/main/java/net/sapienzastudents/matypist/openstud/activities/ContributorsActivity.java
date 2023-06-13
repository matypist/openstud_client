package net.sapienzastudents.matypist.openstud.activities;

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
import net.sapienzastudents.matypist.openstud.R;
import net.sapienzastudents.matypist.openstud.helpers.ClientHelper;
import net.sapienzastudents.matypist.openstud.helpers.LayoutHelper;
import net.sapienzastudents.matypist.openstud.helpers.ThemeEngine;
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
        // MaterialAboutCard.Builder appCardBuilder7 = new MaterialAboutCard.Builder();
        buildContributor(context, appCardBuilder1, "Leonardo Sarra", getResources().getString(R.string.author), null, "https://github.com/LithiumSR/");
        buildContributor(context, appCardBuilder2, "Luigi Russo", getResources().getString(R.string.developer), null, "https://github.com/lrusso96");
        buildContributor(context, appCardBuilder3, "Leonardo Razovic", "Logo designer", "https://www.twitter.com/lrazovic", null);
        buildContributor(context, appCardBuilder4, "Ugo Possenti", "Concept designer", "https://twitter.com/MEPoss", null);
        buildContributor(context, appCardBuilder5, "Valerio Silvestro", "Tester", null, null);
        buildContributor(context, appCardBuilder6, "Emanuele Frasca (x1, 13/06/2023)", "Bug reporters", null, null);
        buildContributor(context, appCardBuilder6, "Alessio Bandiera (x1, 12/06/2023)", "Bug reporters", null, null);
        buildContributor(context, appCardBuilder6, "Giuseppe Borracci (x1, 08/03/2023)", "Bug reporters", null, null);
        // buildContributor(context, appCardBuilder7, "SapienzaApps", getResources().getString(R.string.sapienzaapps), null, null);

        return new MaterialAboutList(
            appCardBuilder1.build(),
            appCardBuilder2.build(),
            /*appCardBuilder7.build(),*/
            appCardBuilder3.build(),
            appCardBuilder4.build(),
            appCardBuilder5.build(),
            appCardBuilder6.build()
        );
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

    private void buildContributor(Context context, MaterialAboutCard.Builder authorCardBuilder, String name, String role, String twitterLink, String githubLink) {
        int tintColor = ThemeEngine.getPrimaryTextColor(this);
        Drawable person = ContextCompat.getDrawable(context, R.drawable.ic_person_outline_black);
        Drawable email = ContextCompat.getDrawable(context, R.drawable.ic_email_black);
        Drawable github = new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_github)
                .color(tintColor)
                .sizeDp(24);
        Drawable twitter = new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_twitter)
                .color(tintColor)
                .sizeDp(24);
        LayoutHelper.setColorSrcAtop(email, tintColor);
        LayoutHelper.setColorSrcAtop(person, tintColor);
        LayoutHelper.setColorSrcAtop(twitter, tintColor);
        authorCardBuilder.title(role);
        MaterialAboutActionItem.Builder generalCard = new MaterialAboutActionItem.Builder();
        MaterialAboutActionItem.Builder githubCard = new MaterialAboutActionItem.Builder();
        MaterialAboutActionItem.Builder twitterCard = new MaterialAboutActionItem.Builder();
        generalCard.text(name).icon(person);
        authorCardBuilder.addItem(generalCard.build());
        if (twitterLink != null) {
            twitterCard.text(R.string.twitter)
                    .icon(twitter)
                    .setOnClickAction(() -> ClientHelper.createCustomTab(this, twitterLink));
            authorCardBuilder.addItem(twitterCard.build());
        }
        if (githubLink != null) {
            githubCard.text(R.string.github_profile)
                    .icon(github)
                    .setOnClickAction(() -> ClientHelper.createCustomTab(this, githubLink));
            authorCardBuilder.addItem(githubCard.build());
        }
    }


}
