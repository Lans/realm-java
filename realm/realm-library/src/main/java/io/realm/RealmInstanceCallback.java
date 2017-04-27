/*
 * Copyright 2017 Realm Inc.
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

package io.realm;

/**
 * To return the results to get the Realm instance asynchronously by calling
 * {@link Realm#getInstanceAsync(RealmConfiguration, RealmInstanceCallback)} or
 * {@link DynamicRealm#getInstanceAsync(RealmConfiguration, RealmInstanceCallback)}.
 * <p>
 * Before creating the first Realm instance in a process, there are some initialization work need to be done such as
 * creating or validating schemas, run migration if needed,
 * copy asset file if {@link io.realm.RealmConfiguration.Builder#assetFile(String)} is supplied and execute the
 * {@link io.realm.RealmConfiguration.Builder#initialData(Realm.Transaction)} if necessary. Those work may takes time
 * and block the caller thread for a while. To avoid {@code getInstance()} call blocking the main thread, the
 * {@code getInstanceAsync()} can be used instead which will do the initialization work in the background thread and
 * deliver a Realm instance to the caller thread.
 * <p>
 * If a Realm instance of a specific {@link RealmConfiguration} is opened on any thread, it is not needed to
 * use {@code getInstanceAsync()} call anymore since all initialization work has been done before. {@code getInstance()}
 * is fast enough for this case.
 * <p>
 * Here is an example of using {@code getInstanceAsync()} when the app starts the first activity:
 * <pre>
 * public class MainActivity extends Activity {
 *
 *   private Realm realm = null;
 *   private RealmAsyncTask realmAsyncTask;
 *
 *   \@Override
 *   protected void onCreate(Bundle savedInstanceState) {
 *     super.onCreate(savedInstanceState);
 *     setContentView(R.layout.layout_main);
 *     realmAsyncTask = Realm.getDefaultInstanceAsync(new RealmInstanceCallback<Realm>() {
 *         \@Override
 *         public void onSuccess(Realm realm) {
 *             if (isDestroyed()) {
 *                 // If the activity is destroyed, the Realm instance should be closed immediately to avoid leaks.
 *                 realm.close();
 *             } else {
 *                 MainActivity.this.realm = realm;
 *                 // Remove the spinner and start the real UI.
 *             }
 *         }
 *     });
 *
 *     // Show a spinner before Realm instance returned by the callback.
 *   }
 *
 *   \@Override
 *   protected void onDestroy() {
 *     super.onDestroy();
 *     if (realm != null) {
 *         realm.close();
 *         realm = null;
 *     } else if (realmAsyncTask != null) {
 *         // Calling cancel() will not guarantee that the task will stop delivery.
 *         // Needs to close the Realm instance in the onSuccess() callback if it doesn't.
 *         realmAsyncTask.cancel();
 *     }
 *   }
 * }
 * </pre>
 *
 * @param <T> {@link Realm} or {@link DynamicRealm}.
 */
public abstract class RealmInstanceCallback<T extends BaseRealm> {

    /**
     * Deliver a Realm instance to the caller thread.
     *
     * @param realm the Realm instance for the caller thread.
     */
    public abstract void onSuccess(T realm);

    /**
     * Deliver the error happens when creating the Realm instance to the caller thread. The default implementation will
     * throw the exception on the caller thread.
     *
     * @param exception happens when creating the Realm instance.
     */
    public void onError(RuntimeException exception) {
        throw exception;
    }
}
