package com.chainedminds.api;

import com.chainedminds.BaseCodes;
import com.chainedminds.dataClasses.BaseData;
import com.chainedminds.dataClasses.InstagramData;
import com.chainedminds.dataClasses.account.BaseAccountData;
import com.chainedminds.utilities.Utilities;
import com.chainedminds.utilities.json.JsonHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class Instagram {

    public static Map<String, Object> getInstagramData(BaseData data) {

        Map<String, Object> response = new HashMap<>();

        response.put("response", BaseCodes.RESPONSE_NOK);

        Utilities.tryAndIgnore(() -> {

            List<Map<String, Map<String, Integer>>> accounts = new ArrayList<>();

            for (Object account : data.accounts) {

                String profileID = ((BaseAccountData) account).username;

                Map<String, Map<String, Integer>> accountData = new HashMap<>();
                accountData.put(profileID, getInstagramData(profileID));

                accounts.add(accountData);
            }

            response.put("accounts", accounts);

            response.put("response", BaseCodes.RESPONSE_OK);
        });

        return response;
    }

    public static Map<String, Integer> getInstagramData(String profileID) {

        String link = "https://www.instagram.com/" + profileID + "/";

        AtomicReference<String> loadedHtml = new AtomicReference<>();

        boolean wasSuccessful = Utilities.openConnection(link, null, (responseCode, responseMessage) -> {

            if (responseCode == 200) {

                loadedHtml.set(responseMessage);
            }

        }, false);

        if (!wasSuccessful) {

            Map<String, Integer> data = new HashMap<>();
            data.put("followers", -1);
            data.put("posts", -1);
            data.put("averageLikes", -1);
            data.put("engagementRate", -1);
            data.put("averageComments", -1);

            return data;
        }

        String json = loadedHtml.get().split("window._sharedData = ")[1].split(";</scrip")[0];

        InstagramData instagramData = JsonHelper.getObject(json, InstagramData.class);

        if (instagramData.entry_data.ProfilePage == null) {

            Map<String, Integer> data = new HashMap<>();
            data.put("followers", -1);
            data.put("posts", -1);
            data.put("averageLikes", -1);
            data.put("engagementRate", -1);
            data.put("averageComments", -1);

            return data;
        }

        InstagramData.UserData user = instagramData.entry_data.ProfilePage[0].graphql.user;

        var profilePicture = user.profile_pic_url;
        var username = user.username;
        var biography = user.biography;
        var followsViewer = (user.follows_viewer) ? "&#10004" : "NO";
        var edgeFollowedByCount = user.edge_followed_by.count;
        var edgeOwnerToTimelineMediaCount = user.edge_owner_to_timeline_media.count;

        var averageLikes = 0;
        var averageComments = 0;
        var totalLikes = 0;
        var totalComments = 0;
        float totalEngagementRate = 0;

        var edgeCounts = user.edge_owner_to_timeline_media.edges.length;

        for (var index = 1; index < edgeCounts; index++) {

            var likes = user.edge_owner_to_timeline_media.edges[index].node.edge_liked_by.count;
            var comments = user.edge_owner_to_timeline_media.edges[index].node.edge_media_to_comment.count;
            totalLikes += likes;
            totalComments += comments;
            totalEngagementRate += (float) (likes + comments) / edgeFollowedByCount;
        }

        var engagementRate = -1;

        if ((totalLikes + totalComments) > 0) {

            engagementRate = Math.round((totalEngagementRate / (edgeCounts - 1) * 100) * 100) / 100;
        }

        if (totalLikes > 0) {

            averageLikes = totalLikes / (edgeCounts - 1);
        }

        if (totalComments > 0) {

            averageComments = totalComments / (edgeCounts - 1);
        }

        var averageLikes2 = Math.round(averageLikes);
        var averageComments2 = Math.round(averageComments);
        var engagementRate2 = engagementRate + 1;

        Map<String, Integer> data = new HashMap<>();
        data.put("followers", edgeFollowedByCount);
        data.put("posts", edgeOwnerToTimelineMediaCount);
        data.put("averageLikes", averageLikes2);
        data.put("engagementRate", engagementRate2);
        data.put("averageComments", averageComments2);

        return data;
    }
}