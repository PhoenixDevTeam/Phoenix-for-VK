package biz.dealnote.messenger.api.interfaces;

import android.support.annotation.CheckResult;

import java.util.Collection;
import java.util.List;

import biz.dealnote.messenger.api.model.Items;
import biz.dealnote.messenger.api.model.VKApiUser;
import io.reactivex.Single;

/**
 * Created by admin on 04.01.2017.
 * phoenix
 */
public interface IUsersApi {

    @CheckResult
    Single<VKApiUser> getUserWallInfo(int userId, String fields, String nameCase);

    @CheckResult
    Single<Items<VKApiUser>> getFollowers(Integer userId, Integer offset, Integer count,
                                          String fields, String nameCase);

    @CheckResult
    Single<Items<VKApiUser>> search(String query, Integer sort, Integer offset, Integer count,
                                    String fields, Integer city, Integer country, String hometown,
                                    Integer universityCountry, Integer university, Integer universityYear,
                                    Integer universityFaculty, Integer universityChair, Integer sex,
                                    Integer status, Integer ageFrom, Integer ageTo, Integer birthDay,
                                    Integer birthMonth, Integer birthYear, Boolean online,
                                    Boolean hasPhoto, Integer schoolCountry, Integer schoolCity,
                                    Integer schoolClass, Integer school, Integer schoolYear,
                                    String religion, String interests, String company,
                                    String position, Integer groupId, String fromList);

    @CheckResult
    Single<List<VKApiUser>> get(Collection<Integer> userIds, Collection<String> domains,
                                String fields, String nameCase);

}
