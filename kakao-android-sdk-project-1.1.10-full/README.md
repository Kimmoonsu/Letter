kakao-android-sdk
=================
## Open SDK Setting

### 1. Update your home gradle.properties
`USER_HOME/.gradle/gradle.properties` 파일에 다음의 property값을 생성합니다.
maven publishing을 위해 추가적인 property를 선언해 줘야하는데 3rd 개발자들은 아무값이나 넣고 선언해도 되며, 
publishing을 할 필요가 있는 다음카카오 개발자는 `API플랫폼셀`에 문의하시면 됩니다.

```additinal_property
NEXUS_USERNAME=xxx
NEXUS_PASSWORD=1234
NEXUS_RELEASE_REPOSITORY_URL=xxx
NEXUS_SNAPSHOT_REPOSITORY_URL=xxx
```

## kakao-open-android-sdk-sample App Usage

### 1. How to use Deploy_Phase.
Kakao open android sdk는 `alpha`, `sandbox`, `release` 버전의 deploy_phase별 sdk를 제공하며,
각각 `modulename-alpha`, `modulename-sandbox`, `modulename`의 이름을 가지며 default value는 release입니다.

root project의 [gradle.properties](https://github.daumkakao.com/kops/kakao-open-android-sdk/blob/master/gradle.properties) 'DEFAULT_PHASE'에 설정을 해주면됩니다.

console에서 명령을 주고 싶을때 필요한 phase별로 아래와 같이 param을 붙여서 빌드를 하시면 됩니다.
```example
$ ./gradlew assemble -Pdeploy_phase=alpha
$ ./gradlew assemble -Pdeploy_phase=sandbox
$ ./gradlew assemble -Pdeploy_phase=release
```

### 2. how to sdk source linking.
gradle 버그로 인해서 maven repository에 있는 sources.jar를 제대로 linking하고 있지 못하여 aar-link-sources plugin을 이용한 가이드를 제공함.

단 plugin의 groovy에서 deploy_phase까지 반영을 하진 못하고 있기때문에 release에서만 source를 linking할 수 있습니다.

[AARLinkSources](https://github.com/xujiaao/AARLinkSources) : README.md 참조.

## 3rd party gradle Project Setting
### 1. global settings
```groovy
buildscript {
    repositories {
        maven { url 'http://devrepo.kakao.com:8088/nexus/content/groups/public/' }
    }
}
```
### 2. Usage
```groovy
dependencies {
    // 카카오스토리, 카카오톡 sdk를 사용하기 위해 필요.
    compile 'com.mcxiaoke.volley:library:1.0.8'
    compile group: 'com.android.support', name: 'support-v4', version: project.SUPPORTV4_VERSION

    // 카카오 링크를 사용하기 위해 필요.
    compile group: project.KAKAO_SDK_GROUP, name: getModuleName('kakaolink'), version: project.KAKAO_SDK_VERSION

    // 카카오스토리 sdk를 사용하기 위해 필요.
    compile group: KAKAO_SDK_GROUP, name: getModuleName('kakaostory'), version: project.KAKAO_SDK_VERSION

    // 카카오톡 sdk를 사용하기 위해 필요.
    compile group: project.KAKAO_SDK_GROUP, name: getModuleName('kakaotalk'), version: project.KAKAO_SDK_VERSION

    // push sdk를 사용하기 위해 필요.
    compile group: project.KAKAO_SDK_GROUP, name: getModuleName('push'), version: project.KAKAO_SDK_VERSION
}

def getModuleName(moduleName) {
    def result = moduleName
    if(project.defaultDeployPhase.toLowerCase() == 'release') {
        return result
    }

    return result + "-${project.defaultDeployPhase.toLowerCase()}"
}
```


