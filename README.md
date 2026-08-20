# Android Lottie Tester

Android 기기에서 Lottie 애니메이션의 호환성과 재생 상태를 직접 확인할 수 있는 테스트 앱입니다. 로컬 파일이나 공개 URL을 불러온 뒤 Android View와 Jetpack Compose 환경에서 각각 재생해 보실 수 있습니다.

## 주요 기능

- 로컬 JSON 및 `.lottie`/ZIP 파일을 선택하실 수 있습니다.
- 공개 HTTP(S) JSON 및 `.lottie` URL을 입력하실 수 있습니다.
- 동일한 애니메이션을 View와 Compose 렌더러에서 비교하실 수 있습니다.
- 재생·일시정지·처음부터 재생과 진행률 탐색을 지원합니다.
- 재생 속도, 반복 여부, 렌더 모드, 스케일과 미리보기 배경을 변경하실 수 있습니다.
- 캔버스 크기, FPS, 프레임 범위, 재생 시간, 이미지·폰트 수를 확인하실 수 있습니다.
- Lottie 파싱 경고와 네트워크·파일 오류를 확인하고 진단 결과를 복사하실 수 있습니다.

## 재생 방식

| 탭 | 렌더러 | 기본 설정 |
| --- | --- | --- |
| View · 스플래시 방식 | `LottieAnimationView` | 자동 렌더링, 1회 재생, Fit |
| Compose · URL 방식 | Lottie Compose | 하드웨어 렌더링, 무한 반복, Fill |

각 탭의 `기본값` 버튼을 누르시면 해당 렌더러의 초기 설정으로 복원하실 수 있습니다.

## 사용 방법

1. 앱을 실행해 주세요.
2. 공개 Lottie URL을 입력하거나 `로컬 JSON / .lottie 선택` 버튼으로 파일을 선택해 주세요.
3. View와 Compose 탭을 전환하면서 재생 결과를 비교해 주세요.
4. 필요한 경우 속도, 반복, 렌더 모드, 스케일과 배경을 변경해 주세요.
5. 화면 하단의 진단 영역에서 composition 정보와 경고를 확인해 주세요.

`.lottie` 파일에 animation JSON이 여러 개 있으면 경고가 표시됩니다. Lottie 6.6.6은 dotLottie manifest에서 특정 애니메이션을 선택하는 기능을 제공하지 않으므로, 라이브러리가 파싱한 composition을 그대로 재생합니다.

## 개발 환경

- Android compile/target SDK 36, min SDK 26
- Java 17
- Kotlin 2.2.21
- Android Gradle Plugin 9.3.1
- Gradle 9.5.0
- Airbnb Lottie 및 Lottie Compose 6.6.6
- Jetpack Compose BOM 2025.10.01

## 빌드 및 테스트

Android SDK 경로가 설정된 환경에서 다음 명령어를 실행해 주세요.

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
```

디버그 APK는 다음 경로에 생성됩니다.

```text
app/build/outputs/apk/debug/app-debug.apk
```

Android 기기나 실행 중인 에뮬레이터가 연결되어 있다면 다음 명령어로 계측 테스트를 실행하실 수 있습니다.

```bash
./gradlew connectedDebugAndroidTest
```

## 패키지

```text
com.zerodeg.lottietester
```

## 라이선스

이 프로젝트는 [MIT License](LICENSE)에 따라 배포됩니다.
