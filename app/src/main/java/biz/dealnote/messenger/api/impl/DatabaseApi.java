package biz.dealnote.messenger.api.impl;

import java.util.Collection;
import java.util.List;

import biz.dealnote.messenger.api.IServiceProvider;
import biz.dealnote.messenger.api.interfaces.IDatabaseApi;
import biz.dealnote.messenger.api.model.Items;
import biz.dealnote.messenger.api.model.VKApiCity;
import biz.dealnote.messenger.api.model.VKApiCountry;
import biz.dealnote.messenger.api.model.database.ChairDto;
import biz.dealnote.messenger.api.model.database.FacultyDto;
import biz.dealnote.messenger.api.model.database.SchoolClazzDto;
import biz.dealnote.messenger.api.model.database.SchoolDto;
import biz.dealnote.messenger.api.model.database.UniversityDto;
import biz.dealnote.messenger.api.services.IDatabaseService;
import io.reactivex.Single;

/**
 * Created by admin on 05.01.2017.
 * phoenix
 */
class DatabaseApi extends AbsApi implements IDatabaseApi {

    DatabaseApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<List<VKApiCity>> getCitiesById(Collection<Integer> cityIds) {
        return provideService(IDatabaseService.class)
                .flatMap(service -> service
                        .getCitiesById(join(cityIds, ",", Object::toString))
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiCountry>> getCountries(Boolean needAll, String code, Integer offset, Integer count) {
        return provideService(IDatabaseService.class)
                .flatMap(service -> service.getCountries(integerFromBoolean(needAll), code, offset, count)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<List<SchoolClazzDto>> getSchoolClasses(Integer countryId) {
        return provideService(IDatabaseService.class)
                .flatMap(service -> service
                        .getSchoolClasses(countryId)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<ChairDto>> getChairs(int facultyId, Integer offset, Integer count) {
        return provideService(IDatabaseService.class)
                .flatMap(service -> service
                        .getChairs(facultyId, offset, count)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<FacultyDto>> getFaculties(int universityId, Integer offset, Integer count) {
        return provideService(IDatabaseService.class)
                .flatMap(service -> service
                        .getFaculties(universityId, offset, count)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<UniversityDto>> getUniversities(String query, Integer countryId, Integer cityId, Integer offset, Integer count) {
        return provideService(IDatabaseService.class)
                .flatMap(service -> service
                        .getUniversities(query, countryId, cityId, offset, count)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<SchoolDto>> getSchools(String query, int cityId, Integer offset, Integer count) {
        return provideService(IDatabaseService.class)
                .flatMap(service -> service
                        .getSchools(query, cityId, offset, count)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiCity>> getCities(int countryId, Integer regionId, String query, Boolean needAll, Integer offset, Integer count) {
        return provideService(IDatabaseService.class)
                .flatMap(service -> service
                        .getCities(countryId, regionId, query, integerFromBoolean(needAll), offset, count)
                        .map(extractResponseWithErrorHandling()));
    }
}
