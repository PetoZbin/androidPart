package com.example.orienteering.retrofit;

import androidx.room.Delete;

import com.example.orienteering.nfts.nftToCompetition.CustomWaypointDesc;
import com.example.orienteering.retrofit.patternClasses.AddressPattern;
import com.example.orienteering.retrofit.patternClasses.CompetitionPattern;
import com.example.orienteering.retrofit.patternClasses.CompetitorPattern;
import com.example.orienteering.retrofit.patternClasses.CompetitorToCompetitionPattern;
import com.example.orienteering.retrofit.patternClasses.LeaderBoardItemPattern;
import com.example.orienteering.retrofit.patternClasses.LoginPattern;
import com.example.orienteering.retrofit.patternClasses.NotifPattern;
import com.example.orienteering.retrofit.patternClasses.RegisterPattern;
import com.example.orienteering.retrofit.responseClasses.CommonResponse;
import com.example.orienteering.retrofit.responseClasses.competition.CompetitionAddResponse;
import com.example.orienteering.retrofit.responseClasses.competition.CompetitionOverallResponse;
import com.example.orienteering.retrofit.responseClasses.competition.WaypointResponse;
import com.example.orienteering.retrofit.responseClasses.login.LoginResponse;
import com.example.orienteering.retrofit.responseClasses.notifications.NotifResponse;
import com.example.orienteering.retrofit.responseClasses.registration.RegistrationResponse;
import com.example.orienteering.retrofit.responseClasses.results.PageResultResponse;
import com.example.orienteering.retrofit.responseClasses.tokens.AddressNftsResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiInterface {

    //nacita nft tokeny pre danu adresu - server pouziva sluzbu moralis na tento ukon
    @GET("tokens/{address}/nfts")
    Call<AddressNftsResponse> getNftsByAddress (@Path("address") String address);

    //nacitanie sutazi pre danu obec, okres
    @GET("competition/local/{municipalities}")
    Call<CompetitionOverallResponse> getCompetitionsByLocality (@Path("municipalities") String municipalities);

    @GET("competition/{competitionId}")
    Call<CompetitionOverallResponse> getCompetitionById (@Path("competitionId") String competitionId);

    @GET("finished/user/{userId}/{page}/{maxItems}")
    Call<PageResultResponse> getFinishedResultsPageByUserId (@Path("userId") String userId,
                                                             @Path("page") int pageNum, @Path("maxItems") int itemsPerPage);

    //nacita nft tokeny pre danu adresu - server pouziva sluzbu moralis na tento ukon
    @GET("notification/user/{userId}")
    Call<NotifResponse> getUserNotifications (@Path("userId") String userId);

    @POST("users/register")
    Call<RegistrationResponse> postRegistration (@Body RegisterPattern registration);

    @POST("users/login")
    Call<LoginResponse> postLogin (@Body LoginPattern loginPattern);

    @POST("users/addAddress")
    Call<CommonResponse> postAddress (@Body AddressPattern addressPattern);

    @POST("competition/register")
    Call<CompetitionAddResponse> postCompetition (@Body CompetitionPattern compPattern);

    @POST("competition/competitor/add")
    Call<CommonResponse> postCompetitor (@Body CompetitorPattern compPattern); //join competition

    @POST("competition/competitor/updateLeaderboard")
    Call<CompetitionOverallResponse> postToLeaderboard (@Body LeaderBoardItemPattern leadItem);

    @PUT("competition/competitor/atStart")
    Call<WaypointResponse> putCompetitorAtStart (@Body CompetitorToCompetitionPattern compPattern); // potvrd zucastnenie sa - start

    @PUT("competition/{competitionId}/competitor/{competitorId}/leave")    //vzdanie sa pocas sutaze
    Call<CommonResponse> putCompetitorAsLeft (@Path("competitionId") String competitionId, @Path("competitorId") String userId); // odidenie pocas sutaze (nezanika clenstvo)

    @DELETE("competition/{competitionId}/owner/{competitorId}/revoke")
    Call<CommonResponse> removeCompetition (@Path("competitionId") String competitionId, @Path("competitorId") String competitorId); //remove competition by its owner

    @DELETE("competition/{competitionId}/competitor/{competitorId}/giveup")
    Call<CommonResponse> removeCompetitor (@Path("competitionId") String competitionId, @Path("competitorId") String userId); //give up competition

    @DELETE("users/{userId}/address/{address}")
    Call<CommonResponse> deleteAddress(@Path("userId") String userId, @Path("address") String address);
}
