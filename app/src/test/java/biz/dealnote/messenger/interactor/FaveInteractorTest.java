package biz.dealnote.messenger.interactor;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Created by ruslan.kolbasa on 03.02.2017.
 * phoenix
 */
@RunWith(MockitoJUnitRunner.class)
public class FaveInteractorTest {

    /*@Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private IFaveApi api;

    @Mock
    private IFaveRepository cache;

    private Items<VKApiUser> data1 = new Items<>();
    private List<VKApiUser> apiList = Collections.singletonList(new VKApiUser(1));
    private List<VKApiUser> cacheData = Collections.singletonList(new VKApiUser(2));

    @Before
    public void prepare() {
        data1.items = apiList;
        data1.count = 1;
    }

    @Test
    public void getFaveUsersCacheFoundApiFound() throws Exception {
        when(api.getUsers(any(), any(), any())).thenReturn(Single.just(data1));
        when(cache.getFaveUsers(anyInt())).thenReturn(Single.just(cacheData));
        when(cache.storeUsers(anyInt(), any(), anyBoolean())).thenReturn(Completable.complete());

        IFaveInteractor interactor = new FaveInteractor(api, cache);
        TestObserver<List<VKApiUser>> observer = TestObserver.create();

        interactor.getFaveUsers(1, IFaveInteractor.MODE_ALL)
                .subscribe(observer);

        observer.awaitTerminalEvent();

        observer.assertNoErrors();
        observer.assertComplete();

        List<Object> events = observer.getEvents().get(0);
        assertThat(events).hasSize(2);

        assertThat(events.get(0)).isSameAs(cacheData);
        assertThat(events.get(1)).isSameAs(apiList);
    }

    @Test
    public void getFaveUsersCacheEmptyApiFound() throws Exception {
        when(api.getUsers(any(), any(), any())).thenReturn(Single.just(data1));
        when(cache.getFaveUsers(anyInt())).thenReturn(Single.just(Collections.emptyList()));
        when(cache.storeUsers(anyInt(), any(), anyBoolean())).thenReturn(Completable.complete());

        IFaveInteractor interactor = new FaveInteractor(api, cache);
        TestObserver<List<VKApiUser>> observer = TestObserver.create();

        interactor.getFaveUsers(1, IFaveInteractor.MODE_ALL)
                .subscribe(observer);

        observer.awaitTerminalEvent();

        observer.assertNoErrors();
        observer.assertComplete();

        List<Object> events = observer.getEvents().get(0);
        assertThat(events).hasSize(1);

        assertThat(events.get(0)).isSameAs(apiList);
    }

    @Test
    public void getFaveUsersCacheEmptyApiError() throws Exception {
        when(api.getUsers(any(), any(), any())).thenReturn(Single.error(new Exception()));
        when(cache.getFaveUsers(anyInt())).thenReturn(Single.just(Collections.emptyList()));
        when(cache.storeUsers(anyInt(), any(), anyBoolean())).thenReturn(Completable.complete());

        IFaveInteractor interactor = new FaveInteractor(api, cache);
        TestObserver<List<VKApiUser>> observer = TestObserver.create();

        interactor.getFaveUsers(1, IFaveInteractor.MODE_ALL)
                .subscribe(observer);

        observer.awaitTerminalEvent();
        observer.assertError(Exception.class);
    }

    @Test
    public void getFaveUsersCacheFoundApiError() throws Exception {
        when(api.getUsers(any(), any(), any())).thenReturn(Single.error(new Exception()));
        when(cache.getFaveUsers(anyInt())).thenReturn(Single.just(cacheData));

        IFaveInteractor interactor = new FaveInteractor(api, cache);
        TestObserver<List<VKApiUser>> observer = TestObserver.create();

        interactor.getFaveUsers(1, IFaveInteractor.MODE_ALL)
                .subscribe(observer);

        observer.awaitTerminalEvent();
        observer.assertError(Exception.class);

        List<Object> events = observer.getEvents().get(0);
        assertThat(events).hasSize(1);

        assertThat(events.get(0)).isSameAs(cacheData);
    }

    @Test
    public void getFaveUsersApiOnlySuccess() throws Exception {
        when(api.getUsers(any(), any(), any())).thenReturn(Single.just(data1));
        when(cache.storeUsers(anyInt(), any(), anyBoolean())).thenReturn(Completable.complete());

        IFaveInteractor interactor = new FaveInteractor(api, cache);
        TestObserver<List<VKApiUser>> observer = TestObserver.create();

        interactor.getFaveUsers(1, IFaveInteractor.MODE_NET_ONLY)
                .subscribe(observer);

        observer.awaitTerminalEvent();
        observer.assertNoErrors();

        verify(cache, atLeast(1)).storeUsers(anyInt(), any(), anyBoolean());

        List<Object> events = observer.getEvents().get(0);
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isSameAs(apiList);
    }

    @Test
    public void getFaveUsersApiOnlyError() throws Exception {
        when(api.getUsers(any(), any(), any())).thenReturn(Single.error(new Exception()));
        when(cache.storeUsers(anyInt(), any(), anyBoolean())).thenReturn(Completable.complete());

        IFaveInteractor interactor = new FaveInteractor(api, cache);
        TestObserver<List<VKApiUser>> observer = TestObserver.create();

        interactor.getFaveUsers(1, IFaveInteractor.MODE_NET_ONLY)
                .subscribe(observer);

        observer.awaitTerminalEvent();
        observer.assertError(Exception.class);

        verify(cache, never()).storeUsers(anyInt(), any(), anyBoolean());

        assertThat(observer.getEvents().get(0)).hasSize(0); // items
        assertThat(observer.getEvents().get(1)).hasSize(1); // errors
    }

    @Test
    public void getFaveUsersInvalidMode() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Unsupported mode: 456");

        IFaveInteractor interactor = new FaveInteractor(api, cache);
        interactor.getFaveUsers(1, 456);
    }*/
}