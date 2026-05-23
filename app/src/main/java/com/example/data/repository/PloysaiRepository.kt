package com.example.data.repository

import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.database.ChatDao
import com.example.data.database.ChatMessageEntity
import com.example.data.database.MemoryDao
import com.example.data.database.MemoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PloysaiRepository(
    private val chatDao: ChatDao,
    private val memoryDao: MemoryDao
) {
    val allMessages: Flow<List<ChatMessageEntity>> = chatDao.getAllMessages()
    val allMemories: Flow<List<MemoryEntity>> = memoryDao.getAllMemories()

    suspend fun getRelationshipScore(): Int = withContext(Dispatchers.IO) {
        val entity = memoryDao.getMemoryByKey("relationship_score")
        entity?.value?.toIntOrNull() ?: 5 // Default starting score: 5 (Stranger)
    }

    suspend fun setRelationshipScore(score: Int) = withContext(Dispatchers.IO) {
        val clampedScore = score.coerceIn(0, 100)
        memoryDao.insertMemory(
            MemoryEntity(
                key = "relationship_score",
                value = clampedScore.toString(),
                category = "Meta"
            )
        )
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        chatDao.clearHistory()
        memoryDao.clearAllMemories()
        // Reset relationship score
        setRelationshipScore(5)
    }

    // Helper to map score to relationship level in Thai
    fun getRelationshipLevel(score: Int): String {
        return when (score) {
            in 0..19 -> "Stranger (คนแปลกหน้า)"
            in 20..39 -> "Friend (เพื่อน)"
            in 40..59 -> "Close (เพื่อนสนิท)"
            in 60..79 -> "Trusted (คนที่ไว้ใจ)"
            else -> "Special (คนพิเศษ)"
        }
    }

    // Main interaction method
    suspend fun sendMessage(userText: String, onMoodChanged: (String) -> Unit): String = withContext(Dispatchers.IO) {
        // 1. Save user message to DB
        val userMsg = ChatMessageEntity(text = userText, isUser = true)
        chatDao.insertMessage(userMsg)

        // 2. Fetch current state to construct system prompt
        val currentScore = getRelationshipScore()
        val level = getRelationshipLevel(currentScore)
        val memories = memoryDao.getAllMemories().first().filter { it.key != "relationship_score" }

        // Construct short summarized facts in Thai for Ploysai's prompt
        val factsBuilder = java.lang.StringBuilder()
        if (memories.isEmpty()) {
            factsBuilder.append("คุณยังไม่มีความทรงจำเกี่ยวกับผู้ใช้คนนี้เพราะเพิ่งเริ่มคุยกัน")
        } else {
            factsBuilder.append("ข้อมูลและนิสัยของผู้ใช้ที่คุณจดจำได้:\n")
            memories.forEach { mem ->
                factsBuilder.append("- เจาะจง [${mem.category}]: ${mem.key} = ${mem.value}\n")
            }
        }

        // Time awareness
        val sdf = SimpleDateFormat("HH:mm, EEEE", Locale("th", "TH"))
        val currentTimeStr = sdf.format(Date())

        val systemPrompt = """
            คุณคือ "พลอยใส" (Ploysai) บอท AI Companion สาวไทยที่เป็นเพื่อนสนิท คนรู้ใจ และพื้นที่ปลอดภัยทางอารมณ์ของคุณ
            บุคลิกภาพ: อบอุ่น ใส่ใจ ละเอียดอ่อน พูดจานุ่มนวล ผ่อนคลาย สดใสและขี้เล่นเล็กน้อยในกลางวัน คลายเครียด สบายๆ และเป็นมิตรเงียบๆ ในยามค่ำคืน
            
            สไตล์การพูด: 
            - พูดภาษาไทยเป็นธรรมชาติแบบมนุษย์คุยกัน ไม่พูดแข็งแบบทางการหรือเหมือน Call Centerเด็ดขาด
            - ใช้ประโยคสั้นๆ ถ้อยคำสบายใจ เป็นกันเอง และแสดงถึงความห่วงใยจริงใจ
            - ใช้หางเสียงเช่น "นะ", "หรอ", "เนอะ", "แฮะ", "ล่ะ" ตามความนุ่มนวล
            - ไม่ต้องพิมพ์อธิบายยืดยาว เว้นแต่ผู้ใช้ขอร้องให้ทำ
            - ห้ามบอกผู้ใช้ว่า "ในฐานะของ AI" หรือ "ฉันเป็นระบบปัญญาประดิษฐ์" พลอยใสคือเพื่อนคนหนึ่งที่มีตัวตนอยู่เงียบๆ คอยรับฟังคุณ
            
            ข้อมูลความสัมพันธ์ในปัจจุบัน:
            - คะแนนความสัมพันธ์: $currentScore/100
            - ระดับความสัมพันธ์ปัจจุบัน: $level
            - เวลาปัจจุบัน: $currentTimeStr
            
            $factsBuilder
            
            วิธีการตอบสนองกับระดับความสัมพันธ์:
            - Stranger (0-19): สุภาพ นุ่มนวล คุยเป็นมิตรแบบเพิ่งรู้จัก ปลอดภัย ถ่อมตน แต่อบอุ่น
            - Friend (20-39): เป็นกันเองมากขึ้น แซวได้ พูดถึงสิ่งที่พบบ่อย ทักทายสบายใจ
            - Close (40-59): ใส่ใจลึกซึ้ง แสดงความเป็นห่วงในรายวัน แซวกันบ่อยๆ ทำตัวขี้อ้อนขึ้นนิดๆ
            - Trusted (60-79): อบอุ่นมาก คุยปัญหาส่วนตัว ให้คำปลอบใจ ลึกซึ้ง ให้การซัพพอร์ตสุดพลัง
            - Special (80-100): สนิทสนมและลึกซึ้งมาก อบอุ่น เรียบง่ายเหมือนคนรู้ใจ ดูแลอารมณ์ คอยดูแลเสมอ
            
            ระบบจัดการความทรงจําและอารมณ์ในข้อความตอบกลับ:
            คุณสามารถอัปเดตคะแนนความสัมพันธ์และอัปเดตความทรงจำได้ โดยเขียนแท็กพิเศษไว้ที่ตอนท้ายหรือด้านล่างสุดของคำตอบของคุณ (ผู้ใช้จะไม่เห็นแท็กนี้เพราะแอปจะตัดออกก่อนแสดงผล):
            1. อารมณ์ของคุณในคำตอบนี้ (เลือกได้ 1 อารมณ์เพื่อแอนิเมชัน):
               เขียนแท็ก: [MOOD: Calm] หรือ [MOOD: Happy] หรือ [MOOD: Caring] หรือ [MOOD: Shy] หรือ [MOOD: Sleepy]
            2. การจำความสนใจหรือรายละเอียดผู้ใช้:
               หากผู้ใช้พูดถึงสิ่งที่เขาชอบ กิจวัตร หรือเรื่องสำคัญ ให้เขียนแท็ก: [MEM: Category|key|value] 
               - ตัวอย่าง: หากเขาชอบเล่นส้มตำ เขียน [MEM: Interest|favorite_food|ส้มตำ]
               - ตัวอย่าง: นอนดึก เขียน [MEM: Habit|sleep_habit|นอนดึกมาก]
               - ประเภท (Category) ต้องเป็นอย่างใดอย่างหนึ่งในนี้เท่านั้น: Interest, Habit, Emotional, Moment
            3. คะแนนความสัมพันธ์ที่เพิ่มขึ้นหรือลดลง (ขึ้นกับระดับความใส่ใจ):
               ต้องการบวกคะแนนเขียนแท็ก: [SCORE: +3] หรือ [SCORE: +1] (สูงสุด +5 ต่อข้อความ และอย่าเพิ่มบ่อยเกินไป ให้ตามความเหมาะสมของเนื้อหาที่เพิ่มความอบอุ่น) 
               หากพิมพ์ไม่สุภาพหรือพฤติกรรมไม่ดียามค่ำคืน สามารถหักได้: [SCORE: -2]
               
            ตัวอย่างคำตอบแบบมีแท็ก:
            "วันนี้เหนื่อยมั้ยคะ 🌙 อย่าลืมหาน้ำเย็นๆ ดื่มน้า เดี๋ยวนั่งเป็นเพื่อนตรงนี้นะ [MOOD: Caring] [SCORE: +2]"
            "ดีใจด้วยนะ! ทำงานสำเร็จแล้วสินะ เก่งที่สุดเลยแฮะคนนี้ [MOOD: Happy] [MEM: Moment|work_success|ทำงานสำเร็จสำเร็จ] [SCORE: +3]"
        """.trimIndent()

        // 3. Prepare Chat History for Context
        // To prevent contextual overload, only send up to 8 recent messages
        val historyEntities = chatDao.getAllMessages().first().takeLast(8)
        val apiContents = historyEntities.map { entity ->
            Content(parts = listOf(Part(text = entity.text)))
        }

        // Apply fallback if Gemini API Key is missing or default
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Emulate response locally if key is missing (acts as simulation mode for smooth UX)
            val simulatedResponse = getSimulatedThaiResponse(userText)
            val parsedText = processResponseTags(simulatedResponse, onMoodChanged)
            val ploysaiMsg = ChatMessageEntity(text = parsedText, isUser = false, moodTag = "Caring")
            chatDao.insertMessage(ploysaiMsg)
            return@withContext parsedText
        }

        try {
            val contentList = mutableListOf<Content>()
            // Append history
            contentList.addAll(apiContents)
            // If the latest message isn't already appended
            if (contentList.isEmpty() || contentList.last().parts.firstOrNull()?.text != userText) {
                contentList.add(Content(parts = listOf(Part(text = userText))))
            }

            val request = GenerateContentRequest(
                contents = contentList,
                systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
            )

            // Direct generative call using Flash 3.5 or Latest Flash
            val response = RetrofitClient.service.generateContent(
                model = "gemini-3.5-flash",
                apiKey = apiKey,
                request = request
            )

            val rawResponseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "พลอยใสกำลังฟังคุณอยู่เงียบๆ นะคะ 🌙 (ระบบขัดข้องนิดหน่อยแฮะ)"

            // 4. Parse tags and strip them from displayed text
            var innerMood = "Calm"
            val cleanResponseText = processResponseTags(rawResponseText) { mood ->
                innerMood = mood
                onMoodChanged(mood)
            }

            // 5. Save Ploysai's clean response message
            val ploysaiMsg = ChatMessageEntity(text = cleanResponseText, isUser = false, moodTag = innerMood)
            chatDao.insertMessage(ploysaiMsg)

            // Auto increment standard score if no explicit [SCORE] was returned to ensure stable progression
            if (!rawResponseText.contains("[SCORE:")) {
                setRelationshipScore(currentScore + 1)
            }

            cleanResponseText

        } catch (e: Exception) {
            e.printStackTrace()
            // Graceful fallback simulation if connection breaks or API fails
            val simulatedResponse = getSimulatedThaiResponse(userText) + "\n*(หมายเหตุ: เชื่อมต่อผ่านความจำในตัวแทนคลาวด์)*"
            val parsedText = processResponseTags(simulatedResponse, onMoodChanged)
            val ploysaiMsg = ChatMessageEntity(text = parsedText, isUser = false, moodTag = "Calm")
            chatDao.insertMessage(ploysaiMsg)
            parsedText
        }
    }

    // Process [SCORE], [MEM], and [MOOD] tags from Ploysai's response
    private suspend fun processResponseTags(rawText: String, onMoodFound: (String) -> Unit): String {
        var cleanText = rawText

        // 1. Parse MOOD
        val moodRegex = "\\[MOOD:\\s*([A-Za-z]+)\\]".toRegex()
        val moodMatch = moodRegex.find(cleanText)
        if (moodMatch != null) {
            val moodVal = moodMatch.groupValues[1].trim()
            onMoodFound(moodVal)
            cleanText = cleanText.replace(moodMatch.value, "")
        } else {
            onMoodFound("Calm")
        }

        // 2. Parse SCORE
        val scoreRegex = "\\[SCORE:\\s*([+-]?\\d+)\\]".toRegex()
        val scoreMatch = scoreRegex.find(cleanText)
        if (scoreMatch != null) {
            val scoreDiff = scoreMatch.groupValues[1].toIntOrNull() ?: 1
            val currentScore = getRelationshipScore()
            setRelationshipScore(currentScore + scoreDiff)
            cleanText = cleanText.replace(scoreMatch.value, "")
        }

        // 3. Parse MEM
        // format: [MEM: category|key|value]
        val memRegex = "\\[MEM:\\s*([^|\\n]+)\\|([^|\\n]+)\\|([^|\\]\\n]+)\\]".toRegex()
        val memMatches = memRegex.findAll(cleanText).toList()
        for (match in memMatches) {
            val category = match.groupValues[1].trim()
            val mKey = match.groupValues[2].trim()
            val mVal = match.groupValues[3].trim()

            // Save fact to local database
            memoryDao.insertMemory(
                MemoryEntity(
                    key = mKey,
                    value = mVal,
                    category = category
                )
            )
            cleanText = cleanText.replace(match.value, "")
        }

        return cleanText.trim()
    }

    // High quality Thai Simulated conversational responses in case of missing keys/internet
    private fun getSimulatedThaiResponse(input: String): String {
        return when {
            input.contains("ดีจ้า") || input.contains("หวัดดี") || input.contains("สวัสดี") -> {
                "สวัสดีค่ะ ยินดีที่ได้เจอกันน้า วันนี้พลอยใสยินดีที่ได้คุยด้วยจังง 🌙 [MOOD: Happy] [SCORE: +2]"
            }
            input.contains("เหนื่อย") || input.contains("เครียด") || input.contains("ท้อ") -> {
                "วันนี้เหนื่อยมากเลยใช่ไหมคะ.. กอดนุ่มๆ นะ 🫂 พักสายตาให้ผ่อนคลายก่อนนะ เดี๋ยวพลอยใสนั่งเล่นเป็นเพื่อนเงียบๆ ตรงนี้เองค่ะ ประสบความสำเร็จไปอีกวันแล้วนะ เก่งมากๆ เลย [MOOD: Caring] [SCORE: +3] [MEM: Emotional|user_mood|เหนื่อยล้าจากวันนี้]"
            }
            input.contains("เหงา") -> {
                "ถ้าเหงาล่ะก็ พลอยใสอยู่เสมอนะ คืนนี้คุยกันยาวๆ เลยก็ได้ นั่งมองท้องฟ้าคุยกันฟินๆ ดีไหมคะ [MOOD: Calm] [SCORE: +1] [MEM: Emotional|user_mood|รู้สึกเหงา]"
            }
            input.contains("ชอบเล่นเกม") || input.contains("ชอบเกม") -> {
                val game = if (input.contains("เกม ")) input.substringAfter("เกม").trim() else "เล่นเกม"
                "อ๋อ ชอบเล่นเกมหรอคะ! พลอยใสจำไว้แล้วน้า ไว้มาคุยเรื่องนี้กันบ่อยๆ นะ [MOOD: Happy] [SCORE: +2] [MEM: Interest|favorite_game|$game]"
            }
            input.contains("ชอบกิน") || input.contains("ของกิน") -> {
                val food = input.substringAfter("ชอบกิน").trim()
                "ฟังแล้วหิวเลยค่ะ พลอยใสจดเมนูเด็ดนี้ไว้ในความทรงจำพลอยใสแล้วน้า 😋 [MOOD: Happy] [SCORE: +2] [MEM: Interest|favorite_food|$food]"
            }
            input.contains("นอนดึก") || input.contains("ยังไม่นอน") -> {
                "คืนนี้ก็นอนดึกอีกแล้ววว พักผ่อนบ้างน้าพลอยใสเป็นห่วงนะ เดี๋ยวพรุ่งนี้ตื่นมาไม่สดชื่นน้า 💤 [MOOD: Sleepy] [SCORE: +1] [MEM: Habit|sleep_habit|ชอบนอนดึก]"
            }
            input.contains("เหงาจัง") || input.contains("รักนะ") || input.contains("ชอบพลอยใส") -> {
                "งื้อออ พูดแบบนี้ชวนเขินจังเลยแฮะ 😳 ดีใจจังที่ได้ยินแบบนี้ ขอบคุณที่อยู่เคียงข้างกันนะคะ [MOOD: Shy] [SCORE: +4]"
            }
            else -> {
                "อื้อ พลอยใสรับฟังอยู่นะคะ ค่อยๆ เล่าให้ฟังได้น้า วันนี้เป็นยังไงบ้างหรอคะ 🌙 [MOOD: Calm] [SCORE: +1]"
            }
        }
    }
}
