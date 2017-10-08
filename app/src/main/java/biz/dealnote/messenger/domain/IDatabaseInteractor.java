package biz.dealnote.messenger.domain;

import java.util.List;

import biz.dealnote.messenger.model.City;
import biz.dealnote.messenger.model.database.Chair;
import biz.dealnote.messenger.model.database.Country;
import biz.dealnote.messenger.model.database.Faculty;
import biz.dealnote.messenger.model.database.School;
import biz.dealnote.messenger.model.database.SchoolClazz;
import biz.dealnote.messenger.model.database.University;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 20.09.2017.
 * phoenix
 */
public interface IDatabaseInteractor {
    Single<List<Chair>> getChairs(int accoutnId, int facultyId, int count, int offset);
    Single<List<Country>> getCountries(int accountId, boolean ignoreCache);
    Single<List<City>> getCities(int accountId, int countryId, String q, boolean needAll, int count, int offset);
    Single<List<Faculty>> getFaculties(int accountId, int universityId, int count, int offset);
    Single<List<SchoolClazz>> getSchoolClasses(int accountId, int countryId);
    Single<List<School>> getSchools(int accountId, int cityId, String q, int count, int offset);
    Single<List<University>> getUniversities(int accoutnId, String filter, Integer cityId, Integer countyId, int count, int offset);
}