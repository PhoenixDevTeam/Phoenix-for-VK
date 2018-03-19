package biz.dealnote.messenger.db.model.entity;

import java.util.List;

import biz.dealnote.messenger.db.model.IdPairEntity;

/**
 * Created by admin on 17.09.2017.
 * phoenix
 */
public class UserDetailsEntity {

    private IdPairEntity photoId;

    private AudioEntity statusAudio;

    private int friendsCount;

    private int onlineFriendsCount;

    private int mutualFriendsCount;

    private int followersCount;

    private int groupsCount;

    private int photosCount;

    private int audiosCount;

    private int videosCount;

    private int allWallCount;

    private int ownWallCount;

    private int postponedWallCount;

    private String bdate;

    private CityEntity city;

    private CountryEntity country;

    private String homeTown;

    private String phone;

    private String homePhone;

    private String skype;

    private List<CareerEntity> careers;

    private List<MilitaryEntity> militaries;

    private List<UniversityEntity> universities;

    private List<SchoolEntity> schools;

    private List<RelativeEntity> relatives;

    private int relation;

    private int relationPartnerId;

    private String[] languages;

    private int political;

    private int peopleMain;

    private int lifeMain;

    private int smoking;

    private int alcohol;

    private String inspiredBy;

    private String religion;

    private String site;

    private String interests;

    private String music;

    private String activities;

    private String movies;

    private String tv;

    private String games;

    private String quotes;

    private String about;

    private String books;

    public String getInterests() {
        return interests;
    }

    public UserDetailsEntity setInterests(String interests) {
        this.interests = interests;
        return this;
    }

    public String getMusic() {
        return music;
    }

    public UserDetailsEntity setMusic(String music) {
        this.music = music;
        return this;
    }

    public String getActivities() {
        return activities;
    }

    public UserDetailsEntity setActivities(String activities) {
        this.activities = activities;
        return this;
    }

    public String getMovies() {
        return movies;
    }

    public UserDetailsEntity setMovies(String movies) {
        this.movies = movies;
        return this;
    }

    public String getTv() {
        return tv;
    }

    public UserDetailsEntity setTv(String tv) {
        this.tv = tv;
        return this;
    }

    public String getGames() {
        return games;
    }

    public UserDetailsEntity setGames(String games) {
        this.games = games;
        return this;
    }

    public String getQuotes() {
        return quotes;
    }

    public UserDetailsEntity setQuotes(String quotes) {
        this.quotes = quotes;
        return this;
    }

    public String getAbout() {
        return about;
    }

    public UserDetailsEntity setAbout(String about) {
        this.about = about;
        return this;
    }

    public String getBooks() {
        return books;
    }

    public UserDetailsEntity setBooks(String books) {
        this.books = books;
        return this;
    }

    public UserDetailsEntity setSite(String site) {
        this.site = site;
        return this;
    }

    public String getSite() {
        return site;
    }

    public UserDetailsEntity setAlcohol(int alcohol) {
        this.alcohol = alcohol;
        return this;
    }

    public UserDetailsEntity setInspiredBy(String inspiredBy) {
        this.inspiredBy = inspiredBy;
        return this;
    }

    public UserDetailsEntity setLifeMain(int lifeMain) {
        this.lifeMain = lifeMain;
        return this;
    }

    public UserDetailsEntity setPeopleMain(int peopleMain) {
        this.peopleMain = peopleMain;
        return this;
    }

    public UserDetailsEntity setPolitical(int political) {
        this.political = political;
        return this;
    }

    public UserDetailsEntity setReligion(String religion) {
        this.religion = religion;
        return this;
    }

    public UserDetailsEntity setSmoking(int smoking) {
        this.smoking = smoking;
        return this;
    }

    public int getAlcohol() {
        return alcohol;
    }

    public int getLifeMain() {
        return lifeMain;
    }

    public int getPeopleMain() {
        return peopleMain;
    }

    public int getPolitical() {
        return political;
    }

    public int getSmoking() {
        return smoking;
    }

    public String getInspiredBy() {
        return inspiredBy;
    }

    public String getReligion() {
        return religion;
    }

    public UserDetailsEntity setLanguages(String[] languages) {
        this.languages = languages;
        return this;
    }

    public String[] getLanguages() {
        return languages;
    }

    public UserDetailsEntity setRelation(int relation) {
        this.relation = relation;
        return this;
    }

    public UserDetailsEntity setRelationPartnerId(int relationPartnerId) {
        this.relationPartnerId = relationPartnerId;
        return this;
    }

    public int getRelation() {
        return relation;
    }

    public int getRelationPartnerId() {
        return relationPartnerId;
    }

    public UserDetailsEntity setRelatives(List<RelativeEntity> relatives) {
        this.relatives = relatives;
        return this;
    }

    public List<RelativeEntity> getRelatives() {
        return relatives;
    }

    public UserDetailsEntity setSchools(List<SchoolEntity> schools) {
        this.schools = schools;
        return this;
    }

    public List<SchoolEntity> getSchools() {
        return schools;
    }

    public UserDetailsEntity setUniversities(List<UniversityEntity> universities) {
        this.universities = universities;
        return this;
    }

    public List<UniversityEntity> getUniversities() {
        return universities;
    }

    public UserDetailsEntity setMilitaries(List<MilitaryEntity> militaries) {
        this.militaries = militaries;
        return this;
    }

    public List<MilitaryEntity> getMilitaries() {
        return militaries;
    }

    public UserDetailsEntity setCareers(List<CareerEntity> careers) {
        this.careers = careers;
        return this;
    }

    public List<CareerEntity> getCareers() {
        return careers;
    }

    public UserDetailsEntity setSkype(String skype) {
        this.skype = skype;
        return this;
    }

    public String getSkype() {
        return skype;
    }

    public UserDetailsEntity setHomePhone(String homePhone) {
        this.homePhone = homePhone;
        return this;
    }

    public String getHomePhone() {
        return homePhone;
    }

    public UserDetailsEntity setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public String getPhone() {
        return phone;
    }

    public UserDetailsEntity setHomeTown(String homeTown) {
        this.homeTown = homeTown;
        return this;
    }

    public String getHomeTown() {
        return homeTown;
    }

    public UserDetailsEntity setCountry(CountryEntity country) {
        this.country = country;
        return this;
    }

    public CountryEntity getCountry() {
        return country;
    }

    public UserDetailsEntity setCity(CityEntity city) {
        this.city = city;
        return this;
    }

    public CityEntity getCity() {
        return city;
    }

    public UserDetailsEntity setBdate(String bdate) {
        this.bdate = bdate;
        return this;
    }

    public String getBdate() {
        return bdate;
    }

    public IdPairEntity getPhotoId() {
        return photoId;
    }

    public UserDetailsEntity setPhotoId(IdPairEntity photoId) {
        this.photoId = photoId;
        return this;
    }

    public AudioEntity getStatusAudio() {
        return statusAudio;
    }

    public UserDetailsEntity setStatusAudio(AudioEntity statusAudio) {
        this.statusAudio = statusAudio;
        return this;
    }

    public int getFriendsCount() {
        return friendsCount;
    }

    public UserDetailsEntity setFriendsCount(int friendsCount) {
        this.friendsCount = friendsCount;
        return this;
    }

    public int getOnlineFriendsCount() {
        return onlineFriendsCount;
    }

    public UserDetailsEntity setOnlineFriendsCount(int onlineFriendsCount) {
        this.onlineFriendsCount = onlineFriendsCount;
        return this;
    }

    public int getMutualFriendsCount() {
        return mutualFriendsCount;
    }

    public UserDetailsEntity setMutualFriendsCount(int mutualFriendsCount) {
        this.mutualFriendsCount = mutualFriendsCount;
        return this;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public UserDetailsEntity setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
        return this;
    }

    public int getGroupsCount() {
        return groupsCount;
    }

    public UserDetailsEntity setGroupsCount(int groupsCount) {
        this.groupsCount = groupsCount;
        return this;
    }

    public int getPhotosCount() {
        return photosCount;
    }

    public UserDetailsEntity setPhotosCount(int photosCount) {
        this.photosCount = photosCount;
        return this;
    }

    public int getAudiosCount() {
        return audiosCount;
    }

    public UserDetailsEntity setAudiosCount(int audiosCount) {
        this.audiosCount = audiosCount;
        return this;
    }

    public int getVideosCount() {
        return videosCount;
    }

    public UserDetailsEntity setVideosCount(int videosCount) {
        this.videosCount = videosCount;
        return this;
    }

    public int getAllWallCount() {
        return allWallCount;
    }

    public UserDetailsEntity setAllWallCount(int allWallCount) {
        this.allWallCount = allWallCount;
        return this;
    }

    public int getOwnWallCount() {
        return ownWallCount;
    }

    public UserDetailsEntity setOwnWallCount(int ownWallCount) {
        this.ownWallCount = ownWallCount;
        return this;
    }

    public int getPostponedWallCount() {
        return postponedWallCount;
    }

    public UserDetailsEntity setPostponedWallCount(int postponedWallCount) {
        this.postponedWallCount = postponedWallCount;
        return this;
    }

    public static final class RelativeEntity {

        private int id;

        private String type;

        private String name;

        public RelativeEntity setName(String name) {
            this.name = name;
            return this;
        }

        public String getName() {
            return name;
        }

        public RelativeEntity setId(int id) {
            this.id = id;
            return this;
        }

        public RelativeEntity setType(String type) {
            this.type = type;
            return this;
        }

        public int getId() {
            return id;
        }

        public String getType() {
            return type;
        }
    }
}