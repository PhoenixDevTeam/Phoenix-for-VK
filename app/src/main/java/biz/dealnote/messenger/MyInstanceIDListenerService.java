/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package biz.dealnote.messenger;

import com.google.android.gms.iid.InstanceIDListenerService;

import biz.dealnote.messenger.push.IPushRegistrationResolver;
import biz.dealnote.messenger.util.RxUtils;

public class MyInstanceIDListenerService extends InstanceIDListenerService {

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        final IPushRegistrationResolver registrationResolver = Injection.providePushRegistrationResolver();

        registrationResolver.resolvePushRegistration()
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> {}, Throwable::printStackTrace);

        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        //RequestHelper.checkPushRegistration(this);
    }
    // [END refresh_token]
}
