package biz.dealnote.messenger.push;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Created by ruslan.kolbasa on 01.02.2017.
 * phoenix
 */
@RunWith(MockitoJUnitRunner.class)
public class OwnersInteractorTest {

    //@Mock
    //private IOtherApi otherApi;

    //@Mock
    //private IOwnersRepository ownersRepository;

    /*@Test
    public void getRxCacheFound() throws Exception {
        VKApiUser user = new VKApiUser();
        user.id = 15;

        when(otherApi.getOwners(anyCollectionOf(Integer.class), anyString(), anyString())).thenReturn(Single.just(Collections.singletonList(user)));

        when(ownersRepository.findOwnerByIdRx(anyInt(), anyInt(), anyBoolean())).thenReturn(Maybe.just(user));

        TestObserver<VKApiOwner> testSubscriber = TestObserver.create();

        OwnersInteractor rxOwner = new OwnersInteractor(otherApi, ownersRepository);
        rxOwner.getRx(0, 0)
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        testSubscriber.assertNoErrors();
        testSubscriber.assertComplete();

        VKApiOwner owner = (VKApiOwner) testSubscriber.getEvents().get(0).get(0);
        assertTrue(owner != null);
        assertTrue(owner.id == 15);
    }

    @Test
    public void getRxCacheNotFoundApiNotFound() throws Exception {
        when(otherApi.getOwners(anyCollectionOf(Integer.class), anyString(), anyString()))
                .thenReturn(Single.just(Collections.emptyList()));

        when(ownersRepository.findOwnerByIdRx(anyInt(), anyInt(), anyBoolean()))
                .thenReturn(Maybe.empty());

        TestObserver<VKApiOwner> testSubscriber = TestObserver.create();

        OwnersInteractor rxOwner = new OwnersInteractor(otherApi, ownersRepository);
        rxOwner.getRx(0, 0)
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertError(NotFoundException.class);
    }

    @Test
    public void getRxCacheNotFoundApiSuccess() throws Exception {
        VKApiUser user = new VKApiUser();
        user.id = 15;

        when(otherApi.getOwners(anyCollectionOf(Integer.class), anyString(), anyString()))
                .thenReturn(Single.just(Collections.singletonList(user)));

        when(ownersRepository.insertOwnersDataRx(anyInt(), anyCollectionOf(VKApiOwner.class), anyBoolean()))
                .thenReturn(Completable.complete());

        when(ownersRepository.findOwnerByIdRx(anyInt(), anyInt(), anyBoolean()))
                .thenReturn(Maybe.empty());

        TestObserver<VKApiOwner> testSubscriber = TestObserver.create();

        OwnersInteractor rxOwner = new OwnersInteractor(otherApi, ownersRepository);
        rxOwner.getRx(0, 0)
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        testSubscriber.assertNoErrors();
        testSubscriber.assertComplete();

        VKApiOwner owner = (VKApiOwner) testSubscriber.getEvents().get(0).get(0);
        assertTrue(owner != null);
        assertTrue(owner.id == 15);
    }

    @Test
    public void getRxCacheNotFoundApiError() throws Exception {
        VKApiUser user = new VKApiUser();
        user.id = 15;

        when(otherApi.getOwners(anyCollectionOf(Integer.class), anyString(), anyString()))
                .thenReturn(Single.error(new Exception()));

        //when(ownersRepository.insertOwnersDataRx(anyInt(), anyCollectionOf(VKApiOwner.class), anyBoolean()))
        //        .thenReturn(Completable.complete());

        when(ownersRepository.findOwnerByIdRx(anyInt(), anyInt(), anyBoolean()))
                .thenReturn(Maybe.empty());

        TestObserver<VKApiOwner> testSubscriber = TestObserver.create();

        OwnersInteractor rxOwner = new OwnersInteractor(otherApi, ownersRepository);
        rxOwner.getRx(0, 0)
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        testSubscriber.assertError(Exception.class);
    }

    @Test
    public void getRxCacheFindError() throws Exception {
        VKApiUser user = new VKApiUser();
        user.id = 15;

        when(otherApi.getOwners(anyCollectionOf(Integer.class), anyString(), anyString()))
                .thenReturn(Single.just(Collections.singletonList(user)));

        when(ownersRepository.insertOwnersDataRx(anyInt(), anyCollectionOf(VKApiOwner.class), anyBoolean()))
                .thenReturn(Completable.complete());

        when(ownersRepository.findOwnerByIdRx(anyInt(), anyInt(), anyBoolean()))
                .thenReturn(Maybe.error(new Exception()));

        TestObserver<VKApiOwner> testSubscriber = TestObserver.create();

        OwnersInteractor rxOwner = new OwnersInteractor(otherApi, ownersRepository);
        rxOwner.getRx(0, 0)
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        testSubscriber.assertNoErrors();
        testSubscriber.assertComplete();

        VKApiOwner owner = (VKApiOwner) testSubscriber.getEvents().get(0).get(0);
        assertTrue(owner != null);
        assertTrue(owner.id == 15);
    }

    @Test
    public void getRxCacheStoreError() throws Exception {
        VKApiUser user = new VKApiUser();
        user.id = 15;

        when(otherApi.getOwners(anyCollectionOf(Integer.class), anyString(), anyString()))
                .thenReturn(Single.just(Collections.singletonList(user)));

        when(ownersRepository.insertOwnersDataRx(anyInt(), anyCollectionOf(VKApiOwner.class), anyBoolean()))
                .thenReturn(Completable.error(new Exception()));

        when(ownersRepository.findOwnerByIdRx(anyInt(), anyInt(), anyBoolean()))
                .thenReturn(Maybe.empty());

        TestObserver<VKApiOwner> testSubscriber = TestObserver.create();

        OwnersInteractor rxOwner = new OwnersInteractor(otherApi, ownersRepository);
        rxOwner.getRx(0, 0)
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        testSubscriber.assertNoErrors();
        testSubscriber.assertComplete();

        VKApiOwner owner = (VKApiOwner) testSubscriber.getEvents().get(0).get(0);
        assertTrue(owner != null);
        assertTrue(owner.id == 15);
    }*/
}