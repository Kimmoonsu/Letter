/**
 * Copyright 2014 Daum Kakao Corp.
 *
 * Redistribution and modification in source or binary forms are not permitted without specific prior written permission. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kakao.usermgmt.response;

import com.kakao.auth.network.response.JSONObjectResponse;
import com.kakao.network.response.ResponseBody.ResponseBodyException;
import com.kakao.network.response.ResponseData;
import com.kakao.usermgmt.StringSet;

/**
 * @author leo.shin
 */
public class AgeAuthResponse extends JSONObjectResponse {
    private final long userId;
    private final String authenticatedAt;
    private final String ci;

    public AgeAuthResponse(ResponseData responseData) throws ResponseBodyException, ApiResponseStatusError {
        super(responseData);

        this.userId = body.optLong(StringSet.id, 0);
        this.authenticatedAt = body.optString(StringSet.authenticated_at, null);
        this.ci = body.optString(StringSet.ci, null);
    }

    /**
     * 인증 여부를 확인하는 user의 id
     * @return 인증 여부를 확인하는 user의 id
     */
    public long getUserId() {
        return userId;
    }

    /**
     * 인증 받은 시각. RFC3339 internet date/time format
     * @return 인증 받은 시각.
     */
    public String getAuthenticatedAt() {
        return authenticatedAt;
    }

    /**
     * 인증후 받은 CI 값
     * @return 인증후 받은 CI 값
     */
    public String getCI() {
        return ci;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AgeAuthResponse{");
        sb.append("userId='").append(userId).append('\'');
        sb.append("authenticatedAt='").append(authenticatedAt).append('\'');
        sb.append("CI='").append(ci).append('\'');
        return sb.toString();
    }
}
