# Dadum-Mobile

### 안드로이드 스튜디오 다운로드 -> 해당 프로젝트 실행 
### 프로젝트 실행 시킨 후 run하기 전 가장 먼저 확인 해야할 것 
## !!! 프로젝트 루트의 local.properties에서 server_ip_address를 내가 지금 연결된 네트워크의 ip로 바꾸기 !!!
### 만약 local.properties가 없다면 -> File > Project Structure > SDK Location 을 통해서 SDK경로를 설정하면 자동으로 생성된다
### 자동생성된 local.properties파일에는 server_ip_address가 없을 것이므로 sdk.dir 밑에 server_ip_address=000.000.0.0 형식으로 ip주소를 추가하기
- - - - - 

## <프로젝트 핸드폰에서 실행시키는 방법> 
#### 사전 조건 : 컴퓨터와 핸드폰 같은 네트워크로 연결되어있어야함

1. 핸드폰 설정 -> 휴대전화 정보 -> 소프트웨어 정보 -> **빌드번호** 연속 7번 클릭 후 개발자 모드 켜기
2. 설정 -> 개발자 옵션 -> **USB디버깅, 무선 디버깅** 켜기
3. 핸드폰과 노트북 연결 (*유선 연결*이 제일 확실하고 편안하다)
4. 안드로이드 스튜디오에서 실행시킨 프로젝트로 들어가면 상단 Running device에 유선으로 연결된 핸드폰이 자동으로 선택됨 (ex: samsung SM-0000)
5. 만약 medium phone api ... 등이 뜬다면 핸드폰과 연결되지 않은것이므로 실행x!
7. 제대로 연결되었다면 run버튼을 눌러 'app'을 run 시킨다
8. 프로젝트 빌드가 끝난 후 앱이 자동으로 핸드폰에 설치, 실행된다
9. 만약 코드를 수정 했다면 다시 run버튼을 눌러 실행시킬것 (local.properties의 server_ip_address는 네트워크가 바뀔때마다 바꿔주어야한다)

- - - - -

## <앱 확인 하는법> 
#### 사전 조건 : 백엔드 스프링부트 서버가 켜져있어야 함

1. 앱이 자동으로 실행되었고, 백엔드 서버도 켜져있다면 회원가입되어있는 아이디로 로그인 하면 된다
2. 해당 앱은 워치에서 데이터를 바로 읽어오는게 아니고 워치에서 실행되는 삼성헬스에 저장된 데이터를 헬스 커넥트를 통해 삼성헬스에서 읽어오는 것이므로 워치와 폰이 기본적으로 연결되어있어야 한다. 또, 헬스커넥트의 권한을 허용해주지 않으면 읽어올 수 없다. 앱을 실행시키면 헬스커넥트 사용 및 권한 허용은 자동으로 앱에서 요청 화면을 띄워준다
3. 데이터가 읽히지 않는 항목이 있다면 워치에 아직 데이터가 쌓이지 않았을 가능성이 높다. 심박수 측정, 운동, 등등 워치를 통해 운동 데이터를 먼저 쌓고 다시 버튼을 눌러서 요청하면 읽어온 데이터를 확인할 수 있다.
4. 앱을 잘 실행 시킨 후 노트북으로 해당 앱이 잘 동작되는 지 확인 하려면 안드로이드 스튜디오의 **logcat**을 켜고 (좌측 하단 고양이 아이콘) ***HEALTH_SYNC***로 검색하면 앱이 실행되면서 log로 실행 여부를 찍어둔 결과를 확인 할 수 있다 (console.log()를 확인하는 것과 같은 맥락)

- - - - -

## <프로젝트 파일 설명> 
#### 기본적으로 kotlin으로 작성되었다 (자바 혼용 자능)
##### - app/manifests/에 있는 AndroidManifest.xml -> application.yml과 같은 역할
##### - build.gradle.kts -> pom.xml과 같은 역할
##### - com.example.dadum 폴더아래의 network폴더 -> 안드로이드 앱이 웹과 네트워크 통신을 하기 위한 것들을 모아둔 폴더
* ApiService.kt: 앱이 로그인 및 데이터를 post하는 메소드 존재
* LoginRequest.kt 및 LoginResponse.kt: 로그인 요청 할때 넣어주는 requestBody 및 response 데이터를 저장하기 위한 엔티티? 정도의 역할
* RetrofitClient.kt: 백엔드 서버와 연결
##### - com.example.dadum 내의 AuthManager.kt -> 액세스 토큰 저장
##### - HealthConnectManager.kt -> 헬스커넥트를 통해 데이터를 받아오는 역할
##### - MainActivity.kt -> controller 내지 App.jsx와 같은 메인 메소드 @Composable 이 붙어있으면 UI이다 










