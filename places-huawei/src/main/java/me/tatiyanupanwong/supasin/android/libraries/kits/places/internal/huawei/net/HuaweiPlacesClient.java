/*
 * Copyright 2020 Supasin Tatiyanupanwong
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

package me.tatiyanupanwong.supasin.android.libraries.kits.places.internal.huawei.net;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import com.huawei.hms.site.api.SearchResultListener;
import com.huawei.hms.site.api.SearchService;
import com.huawei.hms.site.api.SearchServiceFactory;
import com.huawei.hms.site.api.model.DetailSearchResponse;
import com.huawei.hms.site.api.model.QuerySuggestionResponse;
import com.huawei.hms.site.api.model.SearchStatus;

import java.io.IOException;

import me.tatiyanupanwong.supasin.android.libraries.kits.internal.huawei.tasks.MutableFuture;
import me.tatiyanupanwong.supasin.android.libraries.kits.internal.huawei.tasks.HuaweiTask;
import me.tatiyanupanwong.supasin.android.libraries.kits.places.net.FetchPlaceRequest;
import me.tatiyanupanwong.supasin.android.libraries.kits.places.net.FetchPlaceResponse;
import me.tatiyanupanwong.supasin.android.libraries.kits.places.net.FindAutocompletePredictionsRequest;
import me.tatiyanupanwong.supasin.android.libraries.kits.places.net.FindAutocompletePredictionsResponse;
import me.tatiyanupanwong.supasin.android.libraries.kits.places.net.PlacesClient;
import me.tatiyanupanwong.supasin.android.libraries.kits.tasks.Task;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public final class HuaweiPlacesClient implements PlacesClient {

    private final SearchService mDelegate;

    public HuaweiPlacesClient(@NonNull Context context) {
        mDelegate = SearchServiceFactory.create(context);
    }

    @Override
    public @NonNull Task<FindAutocompletePredictionsResponse> findAutocompletePredictions(
            final @NonNull FindAutocompletePredictionsRequest request) {
        final MutableFuture<FindAutocompletePredictionsResponse> future = new MutableFuture<>();

        mDelegate.querySuggestion(
                HuaweiFindAutocompletePredictionsRequest.unwrap(request),
                new SearchResultListener<QuerySuggestionResponse>() {
                    @Override
                    public void onSearchResult(QuerySuggestionResponse response) {
                        future.set(HuaweiFindAutocompletePredictionsResponse.wrap(response));
                    }

                    @Override
                    public void onSearchError(SearchStatus status) {
                        if (TextUtils.isEmpty(request.getQuery())) {
                            // Empty query string will be treated as error by Huawei Site Kit.
                            future.set(HuaweiFindAutocompletePredictionsResponse.wrap(null));
                        } else {
                            future.setException(new IOException(status.getErrorMessage()));
                        }
                    }
                });

        return new HuaweiTask<>(future);
    }

    @Override
    public @NonNull
    Task<FetchPlaceResponse> fetchPlace(final @NonNull FetchPlaceRequest request) {
        final MutableFuture<FetchPlaceResponse> future = new MutableFuture<>();

        mDelegate.detailSearch(
                HuaweiFetchPlaceRequest.unwrap(request),
                new SearchResultListener<DetailSearchResponse>() {
                    @Override
                    public void onSearchResult(DetailSearchResponse response) {
                        future.set(HuaweiFetchPlaceResponse.wrap(response));
                    }

                    @Override
                    public void onSearchError(SearchStatus status) {
                        future.setException(new IOException(status.getErrorMessage()));
                    }
                });

        return new HuaweiTask<>(future);
    }

}
