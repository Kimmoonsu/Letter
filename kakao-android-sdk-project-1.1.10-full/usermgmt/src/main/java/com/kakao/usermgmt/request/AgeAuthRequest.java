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
package com.kakao.usermgmt.request;

import com.kakao.network.ServerProtocol;
import com.kakao.auth.network.request.ApiRequest;

/**
 * 토큰으로 인증날짜와 CI값을 얻는다. 게임 사업부가 저장하고 있는 정보를 내려준다.
 * @author leo.shin
 */
public class AgeAuthRequest extends ApiRequest {
    @Override
    public String getMethod() {
        return GET;
    }

    @Override
    public String getUrl() {
        return createBaseURL(ServerProtocol.API_AUTHORITY, ServerProtocol.USER_AGE_AUTH);
    }
}
