package com.zzxhdzj.douban;

import android.content.Context;
import android.content.SharedPreferences;
import com.zzxhdzj.douban.api.auth.AuthenGetCaptchaGateway;
import com.zzxhdzj.douban.api.auth.AuthenticationGateway;
import com.zzxhdzj.douban.api.channels.action.ChannelActionGateway;
import com.zzxhdzj.douban.api.channels.action.ChannelActionType;
import com.zzxhdzj.douban.api.channels.fixed.StaticChannelGateway;
import com.zzxhdzj.douban.api.channels.genre.GenreChannelGateway;
import com.zzxhdzj.douban.api.channels.recomment.LoginRecommendChannelGateway;
import com.zzxhdzj.douban.api.channels.recomment.RecommendChannelsGateway;
import com.zzxhdzj.douban.api.songs.SongsGateway;
import com.zzxhdzj.douban.api.songs.action.SongActionGateway;
import com.zzxhdzj.douban.api.songs.action.SongActionType;
import com.zzxhdzj.douban.modules.LoginParams;
import com.zzxhdzj.douban.modules.channel.Channel;
import com.zzxhdzj.douban.modules.song.Song;
import com.zzxhdzj.http.ApiGateway;
import com.zzxhdzj.http.Callback;
import com.zzxhdzj.http.util.Strings;

import java.util.ArrayList;

public class Douban {

    public static SharedPreferences sharedPreferences;
    public String captchaImageUrl;
    public String captchaId;
    public ApiRespErrorCode apiRespErrorCode;
    public ArrayList<Channel> channels;
    public ArrayList<Channel> favChannels;
    public ArrayList<Channel> recChannels;
    public Channel recommendChannel;
    public ArrayList<Song> songs;
    private final ApiGateway apiGateway;
    private Context context;

    public Douban(Context context) {
        this.context = context;
        apiGateway = new ApiGateway();
        sharedPreferences = context.getSharedPreferences(Constants.DOUBAN_AUTH, Context.MODE_PRIVATE);
    }


    public boolean isAuthenticated() {
        return !Strings.isEmptyOrWhitespace(getCookie(context));
    }

    public static String getCookie(Context context) {
        return sharedPreferences.getString(Constants.COOKIE, "");
    }

    /**
     * 获取验证码CaptchaId
     *
     * @param callback
     */
    public void fecthCaptcha(Callback callback) {
        AuthenGetCaptchaGateway authenGetCaptchaGateway = new AuthenGetCaptchaGateway(this, apiGateway);
        authenGetCaptchaGateway.newCaptchaId(callback);
    }

    /**
     * 登录
     *
     * @param loginParams 登录信息
     * @param callback
     */
    public void login(LoginParams loginParams, Callback callback) {
        AuthenticationGateway authenticationGateway = new AuthenticationGateway(this, apiGateway);
        authenticationGateway.signIn(loginParams, callback);
    }

    /**
     * 查询最热频道 通过douban.channels获取
     *
     * @param start
     * @param limit
     * @param callback
     */
    public void queryHotChannles(int start, int limit, Callback callback) {
        StaticChannelGateway staticChannelGateway = new StaticChannelGateway(this, apiGateway);
        staticChannelGateway.fetchHotChannels(start, limit, callback);
    }

    /**
     * 上升最快，通过douban.channels获取
     *
     * @param start
     * @param limit
     * @param callback
     */
    public void queryFastChannles(int start, int limit, Callback callback) {
        StaticChannelGateway staticChannelGateway = new StaticChannelGateway(this, apiGateway);
        staticChannelGateway.fetchTrendingChannels(start, limit, callback);
    }

    /**
     * 根据流派查询频道，通过douban.channels获取
     *
     * @param genreId
     * @param start
     * @param limit
     * @param callback
     */
    public void queryChannlesByGenre(int genreId, int start, int limit, Callback callback) {
        GenreChannelGateway genreChannelGateway = new GenreChannelGateway(this, apiGateway);
        genreChannelGateway.fetchChannelsByGenreId(genreId, start, limit, callback);
    }

    /**
     * 获取指定频道歌曲，通过douban.songs获取
     *
     * @param channelId
     * @param bitRate
     * @param callback
     */
    public void songsOfChannel(int channelId, int bitRate, Callback callback) {
        SongsGateway songsGateway = new SongsGateway(this, apiGateway);
        songsGateway.querySongsByChannelId(Constants.songType, channelId, bitRate, callback);
    }

    /**
     * -----Auth required API -----*
     */
    private void checkAuth(Callback callback) {
        if (!isAuthenticated()) {
            callback.onFailure();
            return;
        }
    }

    /**
     *  获取登录后推荐频道,同时返回收藏频道和推荐频道，可通过douban.favChannels 和 douban.recChannels 获取
     * @param userId
     * @param callback
     */
    public void recommendChannelsWhenLogin(String userId, Callback callback) {
        checkAuth(callback);
        LoginRecommendChannelGateway loginRecommendChannelGateway = new LoginRecommendChannelGateway(this, apiGateway);
        loginRecommendChannelGateway.query(userId, callback);
    }

    /**
     * 求推荐：对应官方的"试试这些",可通过douban.channels获取
     * @param channelIds
     * @param callback
     */
    public void recommendChannnels(ArrayList<Integer> channelIds, Callback callback) {
        checkAuth(callback);
        RecommendChannelsGateway recommendChannelsGateway = new RecommendChannelsGateway(this, apiGateway);
        recommendChannelsGateway.query(channelIds, callback);
    }

    /**
     * 收藏频道
     * @param channelId
     * @param callback
     */
    public void favAvChannel(int channelId, Callback callback) {
        checkAuth(callback);
        ChannelActionGateway channelActionGateway = new ChannelActionGateway(this, apiGateway);
        channelActionGateway.favAChannel(ChannelActionType.FAV_CHANNEL, channelId, callback);
    }

    /**
     * 取消收藏频道
     * @param channelId
     * @param callback
     */
    public void unFavAChannel(int channelId, Callback callback) {
        checkAuth(callback);
        ChannelActionGateway channelActionGateway = new ChannelActionGateway(this, apiGateway);
        channelActionGateway.favAChannel(ChannelActionType.FAV_CHANNEL, channelId, callback);
    }

    /**
     * 私人频道，可通过douban.channels获取
     * @param bitRate
     * @param callback
     */
    public void songsOfPrivateChannels(int bitRate, Callback callback) {
        checkAuth(callback);
        SongsGateway songsGateway = new SongsGateway(this, apiGateway);
        songsGateway.querySongsByChannelId(Constants.songType, 0, bitRate, callback);
    }

    /**
     * 红心歌曲(对应红心频道)，可通过douban.songs获取
     * @param bitRate
     * @param callback
     */
    public void favSongs(int bitRate, Callback callback) {
        checkAuth(callback);
        SongsGateway songsGateway = new SongsGateway(this, apiGateway);
        songsGateway.querySongsByChannelId(Constants.songType, -3, bitRate, callback);
    }

    /**
     * 跳过当前播放，返回新的一组歌曲，可通过douban.songs获取
     * @param currentChannelId
     * @param songId
     * @param callback
     */
    public void skipSong(int currentChannelId, int songId, Callback callback) {
        checkAuth(callback);
        SongActionGateway songActionGateway = new SongActionGateway(this, apiGateway);
        songActionGateway.songAction(SongActionType.SKIP, currentChannelId, songId, callback);
    }

    /**
     * 添加红心，同时返回下组歌曲，可通过douban.songs获取
     * @param currentChannelId
     * @param songId
     * @param callback
     */
    public void favASong(int currentChannelId, int songId, Callback callback) {
        checkAuth(callback);
        SongActionGateway songActionGateway = new SongActionGateway(this, apiGateway);
        songActionGateway.songAction(SongActionType.FAV, currentChannelId, songId, callback);
    }

    /**
     * 取消红心，同时返回下组歌曲，可通过douban.songs获取
     * @param currentChannelId
     * @param songId
     * @param callback
     */
    public void unfavASong(int currentChannelId, int songId, Callback callback) {
        checkAuth(callback);
        SongActionGateway songActionGateway = new SongActionGateway(this, apiGateway);
        songActionGateway.songAction(SongActionType.UNFAV, currentChannelId, songId, callback);
    }
    /**
     * 不再播放此曲，同时返回下组歌曲，可通过douban.songs获取
     * @param currentChannelId
     * @param songId
     * @param callback
     */
    public void banASong(int currentChannelId, int songId, Callback callback) {
        checkAuth(callback);
        SongActionGateway songActionGateway = new SongActionGateway(this, apiGateway);
        songActionGateway.songAction(SongActionType.BAN, currentChannelId, songId, callback);
    }


    public Context getContext() {
        return context;
    }

    public SharedPreferences getDoubanSharedPreferences() {
        return sharedPreferences;
    }

    public void clear() {
        this.songs = null;
        this.channels = null;
        System.gc();
    }
}