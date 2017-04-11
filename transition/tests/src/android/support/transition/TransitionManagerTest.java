/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.support.transition;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;

import android.support.test.annotation.UiThreadTest;
import android.support.test.filters.MediumTest;
import android.support.transition.test.R;
import android.view.ViewGroup;

import org.junit.Before;
import org.junit.Test;

@MediumTest
public class TransitionManagerTest extends BaseTest {

    private Scene[] mScenes = new Scene[2];

    @Before
    public void prepareScenes() {
        TransitionActivity activity = rule.getActivity();
        ViewGroup root = activity.getRoot();
        mScenes[0] = Scene.getSceneForLayout(root, R.layout.support_scene0, activity);
        mScenes[1] = Scene.getSceneForLayout(root, R.layout.support_scene1, activity);
    }

    @Test
    public void testSetup() {
        assertThat(mScenes[0], is(notNullValue()));
        assertThat(mScenes[1], is(notNullValue()));
    }

    @Test
    @UiThreadTest
    public void testGo_enterAction() {
        CheckCalledRunnable runnable = new CheckCalledRunnable();
        mScenes[0].setEnterAction(runnable);
        assertThat(runnable.wasCalled(), is(false));
        TransitionManager.go(mScenes[0]);
        assertThat(runnable.wasCalled(), is(true));
    }

    @Test
    public void testGo_exitAction() throws Throwable {
        final CheckCalledRunnable enter = new CheckCalledRunnable();
        final CheckCalledRunnable exit = new CheckCalledRunnable();
        mScenes[0].setEnterAction(enter);
        mScenes[0].setExitAction(exit);
        rule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                assertThat(enter.wasCalled(), is(false));
                assertThat(exit.wasCalled(), is(false));
                TransitionManager.go(mScenes[0]);
                assertThat(enter.wasCalled(), is(true));
                assertThat(exit.wasCalled(), is(false));
            }
        });
        // Let the main thread catch up with the scene change
        rule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TransitionManager.go(mScenes[1]);
                assertThat(exit.wasCalled(), is(true));
            }
        });
    }

    @Test
    public void testGo_transitionListenerStart() throws Throwable {
        final SyncTransitionListener listener =
                new SyncTransitionListener(SyncTransitionListener.EVENT_START);
        rule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Transition transition = new AutoTransition();
                transition.setDuration(0);
                assertThat(transition.addListener(listener), is(sameInstance(transition)));
                TransitionManager.go(mScenes[0], transition);
            }
        });
        assertThat("Timed out waiting for the TransitionListener",
                listener.await(), is(true));
    }

    @Test
    public void testGo_transitionListenerEnd() throws Throwable {
        final SyncTransitionListener listener =
                new SyncTransitionListener(SyncTransitionListener.EVENT_END);
        rule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Transition transition = new AutoTransition();
                transition.setDuration(0);
                assertThat(transition.addListener(listener), is(sameInstance(transition)));
                TransitionManager.go(mScenes[0], transition);
            }
        });
        assertThat("Timed out waiting for the TransitionListener",
                listener.await(), is(true));
    }

    @Test
    public void testGo_nullParameter() throws Throwable {
        final ViewGroup root = rule.getActivity().getRoot();
        rule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TransitionManager.go(mScenes[0], null);
                assertThat(Scene.getCurrentScene(root), is(mScenes[0]));
                TransitionManager.go(mScenes[1], null);
                assertThat(Scene.getCurrentScene(root), is(mScenes[1]));
            }
        });
    }

}
