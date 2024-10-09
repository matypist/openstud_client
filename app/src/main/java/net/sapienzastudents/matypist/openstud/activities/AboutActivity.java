package net.sapienzastudents.matypist.openstud.activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.danielstone.materialaboutlibrary.ConvenienceBuilder;
import com.danielstone.materialaboutlibrary.MaterialAboutActivity;
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem;
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard;
import com.danielstone.materialaboutlibrary.model.MaterialAboutList;
import net.sapienzastudents.matypist.openstud.BuildConfig;
import net.sapienzastudents.matypist.openstud.R;
import net.sapienzastudents.matypist.openstud.helpers.ClientHelper;
import net.sapienzastudents.matypist.openstud.helpers.LayoutHelper;
import net.sapienzastudents.matypist.openstud.helpers.ThemeEngine;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.Scanner;

public class AboutActivity extends MaterialAboutActivity {

    @NonNull
    @Override
    protected MaterialAboutList getMaterialAboutList(@NonNull Context context) {
        MaterialAboutCard.Builder appCardBuilder = new MaterialAboutCard.Builder();
        buildApp(context, appCardBuilder);
        MaterialAboutCard.Builder maintainerCardBuilder = new MaterialAboutCard.Builder();
        buildMaintainer(context, maintainerCardBuilder);
        MaterialAboutCard.Builder miscCardBuilder = new MaterialAboutCard.Builder();
        buildMisc(context, miscCardBuilder);
        return new MaterialAboutList(appCardBuilder.build(), miscCardBuilder.build(), maintainerCardBuilder.build());
    }

    @Nullable
    @Override
    protected CharSequence getActivityTitle() {
        return "OpenStud+";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeEngine.applyAboutTheme(this);
        super.onCreate(savedInstanceState);
    }

    private String getLatestGitHubReleaseTag() {
        String apiUrl = String.format("https://api.github.com/repos/%s/%s/releases", "matypist", "openstud_client");

        try {
            URL url = new URL(apiUrl);
            Scanner scanner = new Scanner(url.openStream(), "UTF-8").useDelimiter("\\A");
            String response = scanner.next();

            JSONArray releases = new JSONArray(response);
            JSONObject latestRelease = releases.getJSONObject(0);

            return latestRelease.getString("tag_name");
        } catch(Exception ex) {
            return null;
        }
    }

    private void buildApp(Context context, MaterialAboutCard.Builder appCardBuilder) {
        int tintColor = ThemeEngine.getPrimaryTextColor(this);
        Drawable version = ContextCompat.getDrawable(context, R.drawable.ic_update_black);
        LayoutHelper.setColorSrcAtop(version, tintColor);
        appCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text(getResources().getString(R.string.version))
                .icon(version).subText(BuildConfig.VERSION_NAME).build());

        String latestGitHubReleaseTag = getLatestGitHubReleaseTag();

        if(latestGitHubReleaseTag != null && !latestGitHubReleaseTag.equals(BuildConfig.VERSION_NAME)) {
            Drawable download_latest_version = new IconicsDrawable(this)
                    .icon(FontAwesome.Icon.faw_download)
                    .color(tintColor)
                    .sizeDp(20);

            appCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                    .text(getResources().getString(R.string.download_latest_version))
                    .icon(download_latest_version).subText(latestGitHubReleaseTag)
                    .setOnClickAction(() -> ClientHelper.createCustomTab(this, "https://osapk.sapienzastudents.net"))
                    .build());

            Drawable latest_version_changelog = new IconicsDrawable(this)
                    .icon(FontAwesome.Icon.faw_info)
                    .color(tintColor)
                    .sizeDp(20);

            appCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                    .text(getResources().getString(R.string.latest_version_changelog))
                    .icon(latest_version_changelog).subText(latestGitHubReleaseTag)
                    .setOnClickAction(() -> ClientHelper.createCustomTab(this, "https://github.com/matypist/openstud_client/releases/latest"))
                    .build());
        }
    }

    private void buildMaintainer(Context context, MaterialAboutCard.Builder maintainerCardBuilder) {
        int tintColor = ThemeEngine.getPrimaryTextColor(this);
        Drawable person = ContextCompat.getDrawable(context, R.drawable.ic_person_outline_black);
        Drawable email = ContextCompat.getDrawable(context, R.drawable.ic_email_black);
        Drawable telegram = new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_telegram)
                .color(tintColor)
                .sizeDp(22);
        LayoutHelper.setColorSrcAtop(email, tintColor);
        LayoutHelper.setColorSrcAtop(person, tintColor);

        maintainerCardBuilder.title(R.string.fork_maintainer);
        maintainerCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                        .text("Matteo Collica (Matypist)")
                        .icon(person)
                        .build())
                .addItem(ConvenienceBuilder.createEmailItem(context, email,
                        getString(R.string.send_email), true, getString(R.string.fork_maintainer_email_address), getString(R.string.question_concerning_openstud)))
                .addItem(new MaterialAboutActionItem.Builder()
                        .text(R.string.send_message)
                        .icon(telegram)
                        .setOnClickAction(() -> {
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=Matypist"));
                                startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                e.printStackTrace();
                                ClientHelper.createCustomTab(context, "https://t.me/Matypist");
                            }
                        }).build()).build();
    }

    private void buildMisc(Context context, MaterialAboutCard.Builder miscCardBuilder) {
        int tintColor = ThemeEngine.getPrimaryTextColor(this);
        Drawable libraries = ContextCompat.getDrawable(context, R.drawable.ic_extension_black_24dp);
        Drawable telegram_channel = new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_bullhorn)
                .color(tintColor)
                .sizeDp(20);
        Drawable telegram_group = new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_users)
                .color(tintColor)
                .sizeDp(20);
        Drawable github = new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_github)
                .color(tintColor)
                .sizeDp(20);
        Drawable heart = new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_heart)
                .color(tintColor)
                .sizeDp(20);
        LayoutHelper.setColorSrcAtop(heart, tintColor);
        LayoutHelper.setColorSrcAtop(github, tintColor);
        LayoutHelper.setColorSrcAtop(libraries, tintColor);
        int id_theme = ThemeEngine.getAboutTheme(this);
        miscCardBuilder.title(R.string.about)
                .addItem(new MaterialAboutActionItem.Builder()
                        .text(R.string.fork_telegram_channel)
                        .subText(R.string.fork_telegram_channel_description)
                        .icon(telegram_channel)
                        .setOnClickAction(() -> ClientHelper.createCustomTab(this, "https://telegram.me/OpenStud"))
                        .build())
                .addItem(new MaterialAboutActionItem.Builder()
                        .text(R.string.fork_telegram_group)
                        .subText(R.string.fork_telegram_group_description)
                        .icon(telegram_group)
                        .setOnClickAction(() -> ClientHelper.createCustomTab(this, "https://osgroup.sapienzastudents.net"))
                        .build())
                .addItem(new MaterialAboutActionItem.Builder()
                        .text(R.string.fork_github)
                        .subText(R.string.fork_github_description)
                        .icon(github)
                        .setOnClickAction(() -> ClientHelper.createCustomTab(this, "https://www.github.com/matypist/openstud_client"))
                        .build())
                .addItem(new MaterialAboutActionItem.Builder()
                        .text(R.string.contributors)
                        .icon(heart)
                        .setOnClickAction(() -> startActivity(new Intent(this, ContributorsActivity.class)))
                        .build())
                .addItem(new MaterialAboutActionItem.Builder()
                        .text(R.string.open_source_libs)
                        .icon(libraries)
                        .setOnClickAction(() -> new LibsBuilder()
                                .withAutoDetect(true)
                                .withActivityTitle(this.getResources().getString(R.string.open_source_libs))
                                .withAboutIconShown(true)
                                .withAboutVersionShown(true)
                                .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                                .withActivityTheme(id_theme)
                                .start(this))
                        .build());

    }
}