package com.chainedminds.dataClasses;

public class InstagramData {

    public EntryData entry_data;

    public static class EntryData {

        public ProfilePageData[] ProfilePage;

    }

    public static class ProfilePageData {

        public String logging_page_id;
        public GraphQLData graphql;
    }

    public static class GraphQLData {

        public UserData user;
    }

    public static class UserData {

        public String username;
        public String biography;
        public String profile_pic_url;
        public boolean follows_viewer;
        public EdgeFollowedByData edge_followed_by;

        public EdgeOwnerToTimelineMediaData edge_owner_to_timeline_media;
    }

    public static class EdgeOwnerToTimelineMediaData {

        public int count;
        public EdgeData[] edges;
    }

    public static class EdgeData {

        public NodeData node;
    }

    public static class NodeData {

        public String id;
        public EdgeFollowedByData edge_liked_by;
        public EdgeFollowedByData edge_media_to_comment;
    }

    public static class EdgeFollowedByData {

        public int count;
    }
}