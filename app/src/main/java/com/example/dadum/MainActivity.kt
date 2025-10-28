package com.example.dadum

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.health.connect.client.HealthConnectFeatures
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.lifecycle.lifecycleScope
import com.example.dadum.network.HeartRateData
import com.example.dadum.network.LoginRequest
import com.example.dadum.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import retrofit2.Response
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

val DadumBlue = Color(0xFF4285F4)
val RedHeart = Color(0xFFE53935)
val PurpleSteps = Color(0xFF947DFF)
val GreenCalorie = Color(0xFF66BB6A)
val PinkCalorie = Color(0xFF3366FF)
val BlueDistance = Color(0xFF3399FF)
val InactiveTabColor = Color(0xFF1C1C1E)
val DadumBackground = Color(0xFFE6f3FF)

@Composable
fun HealthDataCard(
    title: String,
    value: String,
    unit: String,
    valueColor: Color
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ){
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ){
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(text = title, style = MaterialTheme.typography.bodyLarge.copy(color = Color.Black, fontSize = 13.sp))
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = valueColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    modifier = Modifier.padding(start = 2.dp)
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Gray,
                        fontSize = 10.sp,
                    ),
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
    }
}

@Composable
fun DadumHeader(onLogout: () -> Unit){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Image(
                    painter = painterResource(id=R.drawable.ic_dadum_logo),
                    contentDescription = "다듬 로고",
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "다듬",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = DadumBlue
                    )
                )
            }
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = DadumBlue),
                modifier = Modifier.height(48.dp)
            ) {
                Text(text = "로그아웃")
            }
        }
    }
}

class MainActivity : ComponentActivity() {

    private lateinit var healthConnectManager: HealthConnectManager
    private lateinit var authManager: AuthManager

    //로그인 상태 관리 변수
    private var isLoggedIn by mutableStateOf<Boolean?>(null)

    // UI에서 사용하기 위한 UI상태변수 선언
    private var stepCountData by mutableStateOf<List<Int>>(emptyList())
    private var dailyCaloriesBurnedRecord by mutableStateOf(0.0)//총 소모칼로리
    private var distanceWalkedData by mutableStateOf(0.0) //오늘 걸은 거리
    private var activeCaloriesBurnedData by mutableStateOf(0.0) //활동으로 인한 소모칼로리
    private var totalSleepMinutes by mutableStateOf(0L)
    private var deepSleepMinutes by mutableStateOf(0L)
    private var remSleepMinutes by mutableStateOf(0L)
    private var lightSleepMinutes by mutableStateOf(0L)
    private var heartRateBpm by mutableStateOf<List<HeartRateData>>(emptyList())
    private var currentTime by mutableStateOf("")

    private val permissionLauncher =
        registerForActivityResult<Set<String>, Set<String>>(
            PermissionController.createRequestPermissionResultContract() //헬스커넥트 권한 요청 처리
        ) { granted: Set<String> -> //granted되면 권한리스트에 들어감
            Log.d("HEALTH_SYNC", "권한 요청 결과: $granted")
            if (granted.containsAll(healthConnectManager.permissions)) {
                lifecycleScope.launch {
                    fetchAndSend()
                }
            } else {
                Log.e("HEALTH_SYNC", "권한 요청 실패")
            }
        }

    //permissionLauncher가 헬스커넥트 권한을 요청하고 결과를 처리하는 객체
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("HEALTH_SYNC", "앱 실행됨")
        healthConnectManager = HealthConnectManager(this)
        authManager = AuthManager(this)

        window.apply {
            WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars=true
        }

        lifecycleScope.launch {
            //앱 시작시 저장된 토큰이 있는지 확인
            val token = authManager.authToken.first()
            isLoggedIn = !token.isNullOrEmpty()
        }

        // Jetpack Compose로 UI 구성
        setContent {
            //로그인 상태가 결정될때까지 로딩 화면 표시
            when (isLoggedIn) {
                true -> {
                    MainContent(
                        authManager = authManager,
                        onLogoutSuccess = {
                            isLoggedIn = false
                        }
                    )
                }

                false -> {
                    Login(
                        authManager = authManager,
                        onLoginSuccess = {
                            isLoggedIn = true
                        }
                    )
                }

                null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    @Composable
    fun MainContent(
        authManager: AuthManager,
        onLogoutSuccess: () -> Unit
    ) {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        val customFormat = DateTimeFormatter.ofPattern("yy년 MM월 dd일 HH시 mm분 ss초")
        val currentDate = if (currentTime.isNotEmpty()){
            try {
                val parsedTime = ZonedDateTime.parse(currentTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                parsedTime.format(customFormat)
            } catch (e: Exception){
                Log.d("HEALTH_SYNC","시간 파싱 오류", e)
            }
        } else {
            "데이터 전송 기록 없음"
        }

        LaunchedEffect(Unit) {
            healthConnectManager.checkPermissionsAndRun(permissionLauncher)
        }

        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            Column(
                modifier = Modifier.padding(25.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DadumBackground, RoundedCornerShape(25.dp))
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DadumHeader(
                        onLogout = {
                            coroutineScope.launch {
                                authManager.clearAuthToken()
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "로그아웃 성공!", Toast.LENGTH_SHORT).show()
                                    onLogoutSuccess()
                                }
                            }
                        }
                    )
                    Text(text = currentDate.toString(), modifier = Modifier.padding(bottom = 16.dp))
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            item {
                                HealthDataCard(
                                    title = "심박수",
                                    value = if (heartRateBpm.isNotEmpty()) heartRateBpm.first().bpm.toString() else "0",
                                    unit = "bpm",
                                    valueColor = RedHeart
                                )
                            }
                            item {
                                HealthDataCard(
                                    title = "걸음 수",
                                    value = if (stepCountData.isNotEmpty()) stepCountData.joinToString(
                                        ", "
                                    ) else "0",
                                    unit = "보",
                                    valueColor = PurpleSteps
                                )
                            }
                            item {
                                HealthDataCard(
                                    title = "오늘 걸은 거리",
                                    value = "%.2f".format(dailyCaloriesBurnedRecord),
                                    unit = "m",
                                    valueColor = BlueDistance
                                )
                            }
                            item {
                                HealthDataCard(
                                    title = "칼로리",
                                    value = "%.2f".format(dailyCaloriesBurnedRecord),
                                    unit = "kcal",
                                    valueColor = GreenCalorie
                                )
                            }
                            item {
                                HealthDataCard(
                                    title = "활동 칼로리",
                                    value = "%.2f".format(activeCaloriesBurnedData),
                                    unit = "kcal",
                                    valueColor = PinkCalorie
                                )
                            }
                            item {
                                HealthDataCard(
                                    title = "총 수면 시간",
                                    value = totalSleepMinutes.toString(),
                                    unit = "분",
                                    valueColor = InactiveTabColor
                                )
                            }
                            item {
                                HealthDataCard(
                                    title = "깊은 수면 시간",
                                    value = deepSleepMinutes.toString(),
                                    unit = "분",
                                    valueColor = InactiveTabColor
                                )
                            }
                            item {
                                HealthDataCard(
                                    title = "얕은 수면 시간",
                                    value = lightSleepMinutes.toString(),
                                    unit = "분",
                                    valueColor = InactiveTabColor
                                )
                            }
                            item {
                                HealthDataCard(
                                    title = "렘 수면 시간",
                                    value = remSleepMinutes.toString(),
                                    unit = "분",
                                    valueColor = InactiveTabColor
                                )
                            }
                        }
                        Button(
                            onClick = {
                                //헬스커넥트 지원 여부 먼저 확인
                                if (!healthConnectManager.isHealthConnectAvailable()) {
                                    Log.d("HEALTH_SYNC", "HealthConnect 미지원 기기")
                                    Toast.makeText(
                                        context,
                                        "헬스커넥트 미지원 기기입니다. 데이터 연동이 불가능합니다.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    return@Button
                                }
                                // 헬스커넥트 지원기기라면, 버튼 클릭 시 권한 다시 확인 후 fetchAndSend() 실행
                                coroutineScope.launch {
                                    if (healthConnectManager.checkPermissionsAndRun(
                                            permissionLauncher
                                        )
                                    ) {
                                        fetchAndSend()
                                    } else {
                                        //권한이 없다면 토스트 알림
                                        Toast.makeText(
                                            context,
                                            "헬스커넥트 권한이 필요합니다",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                    }
                                }
                            }, colors = ButtonDefaults.buttonColors(containerColor = DadumBlue),
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(text = "데이터 가져오기 및 서버 전송")
                        }
                    }
                }
            }
        }
    }

    private suspend fun fetchAndSend() { //서버로 데이터 전송하는 함수
//      onDataFetched: (List<Int>, List<HeartRateData>) -> Unit는 함수 타입선언부분 List<Int>, List<HeartRateData>두 가지 탑을 인자로 받는 함수타입선언. Unit은 반환값이 없다는 의미
        Log.d("HEALTH_SYNC", "fetchAndSend 실행됨")

        try {
            //현재 시간 생성
            val koreaZone = ZoneId.of("Asia/Seoul")
            val nowKST = ZonedDateTime.now(koreaZone)
            val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
            val currentTimeString = nowKST.format(formatter)
            //걸음수 데이터 읽기
            val stepRecords =healthConnectManager.readStepCounts()
            val stepData = stepRecords.map{it.count.toInt()} //itdms stepRecords리스트의 각각의 요소.it은 람다 함수에서 사용하는 기본 파라미터
            //칼로리 소모량 데이터 읽기
            val caloriesBurnedRecords = healthConnectManager.readCaloriesBurned()
            val caloriesBurnedData = caloriesBurnedRecords.sumOf { it.energy.inKilocalories }
            val dailyCaloriesBurned = caloriesBurnedData
            //심박수데이터읽기
            val heartRateRecords =healthConnectManager.readHeartRates()
            val heartRateData = heartRateRecords.map{ record ->
                val sample = record.samples.firstOrNull()
                HeartRateData(
                    bpm = sample?.beatsPerMinute?.toDouble() ?:0.0,
                    time = sample?.time.toString() //bpm:xx.x ,time: 2025-03-39 몇시 이런식으로 json형식
                )
            }
            //오늘 걸은 거리
            val distanceRecords = healthConnectManager.readDistanceWalked()
            val totalDistance = distanceRecords.sumOf { it.distance.inMeters }
            //활동으로 소모한 칼로리
            val activeCalorieRecords = healthConnectManager.readActiveCaloriesBurned()
            val activeCaloriesBurned = activeCalorieRecords.sumOf { it.energy.inKilocalories }
            //수면 기록
            val sleepSessions = healthConnectManager.readSleepSessions()
            val totalSleepMillis = sleepSessions.sumOf { Duration.between(it.startTime, it.endTime).toMillis() }
            val totalSleepInMinutes = totalSleepMillis /1000 /60 //밀리초를 분으로 변환
            var remMinutes = 0L
            var deepMinutes = 0L
            var lightMinutes = 0L

            sleepSessions.forEach { session ->
                session.stages.forEach { stage ->
                    val duration = Duration.between(stage.startTime, stage.endTime).toMinutes()
                    when (stage.stage){
                        SleepSessionRecord.STAGE_TYPE_REM -> remMinutes += duration
                        SleepSessionRecord.STAGE_TYPE_DEEP -> deepMinutes += duration
                        SleepSessionRecord.STAGE_TYPE_LIGHT -> lightMinutes += duration
                    }
                }
            }
            withContext(Dispatchers.Main) {
                stepCountData = stepData
                dailyCaloriesBurnedRecord = dailyCaloriesBurned
                distanceWalkedData = totalDistance
                activeCaloriesBurnedData = activeCaloriesBurned
                this@MainActivity.totalSleepMinutes = totalSleepInMinutes
                this@MainActivity.deepSleepMinutes = deepMinutes
                this@MainActivity.remSleepMinutes = remMinutes
                this@MainActivity.lightSleepMinutes = lightMinutes
                heartRateBpm = heartRateData
                currentTime = currentTimeString
            }

            Log.d("HEALTH_SYNC", "걸음수 데이터: ${stepData.joinToString(", ")}")
            Log.d("HEALTH_SYNC", "심박수 데이터: ${heartRateRecords.size}개")
            Log.d("HEALTH_SYNC", "총 소모 칼로리 레코드 수: ${caloriesBurnedRecords.size}")
            Log.d("HEALTH_SYNC", "걸은 거리 레코드 수: ${distanceRecords.size}")
            Log.d("HEALTH_SYNC", "활동 칼로리 레코드 수: ${activeCalorieRecords.size}")
            Log.d("HEALTH_SYNC", "칼로리 소모량 데이터: $dailyCaloriesBurned")
            Log.d("HEALTH_SYNC", "걸은 거리: $totalDistance m")
            Log.d("HEALTH_SYNC", "활동 칼로리: $activeCaloriesBurned")
            Log.d("HEALTH_SYNC", "총 수면 시간: $totalSleepInMinutes 분")
            Log.d("HEALTH_SYNC", "깊은 수면: $deepMinutes 분")
            Log.d("HEALTH_SYNC", "렘 수면: $remMinutes 분")
            Log.d("HEALTH_SYNC", "얕은 수면: $lightMinutes 분")
            Log.d("HEALTH_SYNC","전송 시간:$currentTimeString")

            //서버 전송 로직
            val healthData = HealthData( //아레에서 HealthData 코틀린클래스를 정의함
                stepData = stepData,
                heartRateData = heartRateData,
                caloriesBurnedData = dailyCaloriesBurned,
                distanceWalked = totalDistance,
                activeCaloriesBurned = activeCaloriesBurned,
                totalSleepMinutes = totalSleepInMinutes,
                deepSleepMinutes = deepMinutes,
                remSleepMinutes = remMinutes,
                lightSleepMinutes = lightMinutes,
                currentTime = currentTimeString
            )

            val token = authManager.authToken.first()
            if(token != null){
                val authorizationHeader = "Bearer $token"
                // 서버로 전송
                val response= RetrofitClient.apiService.sendHealthData(authorizationHeader, healthData)

                if(response.isSuccessful){
                    Log.d("HEALTH_SYNC","데이터 서버 전송 성공")
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@MainActivity, "전송 성공!", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Log.d("HEALTH_SYNC","데이터 서버 전송 실패:${response.code()} ${response.message()}")
                    withContext(Dispatchers.Main){
                        val errorBody = response.errorBody()?.string() ?:"알 수 없는 오류"
                        Toast.makeText(this@MainActivity,"전송 실패: ${response.code()} ($errorBody)",
                            Toast.LENGTH_LONG).show()
                    }
                }
            }else{
                Log.d("HEALTH_SYNC","토큰이 없습니다")
                withContext(Dispatchers.Main){
                    Toast.makeText(this@MainActivity, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("HEALTH_SYNC", "에러 발생", e)
            withContext(Dispatchers.Main){
                Toast.makeText(this@MainActivity,"데이터 처리 또는 네트워크 오류", Toast.LENGTH_LONG).show()
            }
        }
    }
}

// HealthData 클래스를 정의
data class HealthData(
    val stepData: List<Int>,
    val heartRateData: List<HeartRateData>,
    val caloriesBurnedData: Double,
    val distanceWalked: Double,
    val activeCaloriesBurned: Double,
    val totalSleepMinutes: Long,
    val deepSleepMinutes: Long,
    val remSleepMinutes: Long,
    val lightSleepMinutes: Long,
    val currentTime:String
)

@Composable
fun Login(
    authManager: AuthManager,
    onLoginSuccess: () -> Unit
){
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("")}
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val DadumBlue = Color(0xFF4285F4)
    val focusedBlueTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = DadumBlue,
        focusedLabelColor = DadumBlue,
        cursorColor = DadumBlue,
        unfocusedBorderColor = Color.LightGray
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Row(
            modifier = Modifier.padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Image(
                painter = painterResource(id=R.drawable.ic_dadum_logo),
                contentDescription = "다듬 로고",
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "다듬",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = DadumBlue
                )
            )
        }

        Text(
            text = "루틴을 관리하고 자세를 분석해보세요",
            style = MaterialTheme.typography.bodyLarge.copy( color = Color.Gray),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        //이메일 입력
        OutlinedTextField(
            value = email,
            onValueChange = {email=it},
            label = {Text("아이디")},
            colors = focusedBlueTextFieldColors,
            modifier = Modifier.padding(16.dp)
        )

        //비밀번호 입력
        OutlinedTextField(
            value = password,
            onValueChange = {password=it},
            label = {Text("비밀번호")},
            colors = focusedBlueTextFieldColors,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.padding(16.dp)
        )

        Button(
            onClick = {
                isLoading=true
                coroutineScope.launch(Dispatchers.IO){
                    try{
                        val request = LoginRequest(email, password)
                        val response = RetrofitClient.apiService.login(request)

                        if (response.isSuccessful && response.body() != null){
                            val loginResponse = response.body()!!
                            //로그인 성공, 엑세스 토큰 저장
                            if (loginResponse!=null) {
                                authManager.saveAuthToken(loginResponse.accessToken)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "로그인 성공!", Toast.LENGTH_SHORT).show()
                                    Log.d("HEALTH_SYNC","로그인 성공")
                                    onLoginSuccess()
                                }
                            }else{
                                withContext(Dispatchers.Main){
                                    Toast.makeText(context,"로그인 실패", Toast.LENGTH_LONG).show()
                                    Log.d("HEALTH_SYNC","로그인 실패: 서버 응답 오류")
                                    isLoading = false
                                }
                            }
                        }else{
                            //API호출 실패
                            withContext(Dispatchers.Main){
                                val errorMessage = response.errorBody()?.string() ?:"알 수 없는 오류"
                                Toast.makeText(context, "로그인 실패", Toast.LENGTH_LONG).show()
                                Log.d("HEALTH_SYNC","로그인 실패: ${errorMessage}")
                                isLoading=false
                            }
                        }
                    }catch (e: Exception){
                        withContext(Dispatchers.Main){
                            Toast.makeText(context,"네트워크 오류", Toast.LENGTH_SHORT).show()
                            Log.d("HEALTH_SYNC","네트워크 오류: ${e.message}")
                            isLoading=false
                        }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = DadumBlue),
            modifier = Modifier.padding(top=16.dp),
            enabled = !isLoading
        ) {
            Text(text=if (isLoading) "로그인 중..." else "로그인")
        }
    }
}
